package net.dewcloud.simplerpc.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.dewcloud.simplerpc.common.handle.RpcDecoder;
import net.dewcloud.simplerpc.common.handle.RpcEncoder;
import net.dewcloud.simplerpc.common.handle.RpcRequest;
import net.dewcloud.simplerpc.common.handle.RpcResponse;

/**
 * 框架的RPC 客户端（用于发送 RPC 请求）
 *
 */
public class RpcClient  extends SimpleChannelInboundHandler<RpcResponse>{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);
	
	private String host;
	private int port;
	private RpcResponse response;
	private final Object obj = new Object();
	public RpcClient(String host, int port) {
		
		this.host = host;
		this.port = port;
	}
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
		 this.response=response;
		 
		 synchronized(obj) {
			 obj.notifyAll();
		 }
	}
	
	

	/**
	 * 链接服务端，发送消息
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public RpcResponse send(RpcRequest request) throws Exception {
		EventLoopGroup  group = new NioEventLoopGroup();
		try {
			
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
				
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new RpcEncoder(RpcRequest.class)).addLast(new RpcDecoder(RpcResponse.class)).addLast(RpcClient.this);
					
				}
				
				
			}).option(ChannelOption.SO_KEEPALIVE, true);
			
			ChannelFuture future = bootstrap.connect(host,port).sync();
			//将request对象写入outbundle处理后发出（即RpcEncoder编码器）

			future.channel().writeAndFlush(request).sync();
			// 用线程等待的方式决定是否关闭连接
			// 其意义是：先在此阻塞，等待获取到服务端的返回后，被唤醒，从而关闭网络连接	
			synchronized (obj) {
				obj.wait();
			}
			if (response != null) {
				future.channel().closeFuture().sync();
			}
			return response;
		} finally {
			group.shutdownGracefully();
		}
		
	}
	
	/**
	 * 异常处理
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		LOGGER.error("client caught exception", cause);
		ctx.close();
	}
}
