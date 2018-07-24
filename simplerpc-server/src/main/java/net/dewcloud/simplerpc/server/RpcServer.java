package net.dewcloud.simplerpc.server;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.dewcloud.simplerpc.common.ServiceRegistry;
import net.dewcloud.simplerpc.common.handle.RpcDecoder;
import net.dewcloud.simplerpc.common.handle.RpcEncoder;
import net.dewcloud.simplerpc.common.handle.RpcHandler;
import net.dewcloud.simplerpc.common.handle.RpcRequest;
import net.dewcloud.simplerpc.common.handle.RpcResponse;

/**
 * 框架的RPC 服务器（用于将用户系统的业务类发布为 RPC 服务）
 * 使用时可由用户通过spring-bean的方式注入到用户的业务系统中
 * 由于本类实现了ApplicationContextAware InitializingBean
 * spring构造本对象时会调用setApplicationContext()方法，从而可以在方法中通过自定义注解获得用户的业务接口和实现
 * 还会调用afterPropertiesSet()方法，在方法中启动netty服务器
 * @author cist
 *
 */
public class RpcServer implements ApplicationContextAware,InitializingBean 
{

	private final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);
	private String serverAddress;
	private ServiceRegistry serviceRegistry;
	
	private Map <String,Object> handleMap = new HashMap<String,Object>();
	
	
	
	public RpcServer(String serverAddress) {
		
		this.serverAddress = serverAddress;
	}

	
	
	public RpcServer(String serverAddress, ServiceRegistry serviceRegistry) {
		this.serverAddress = serverAddress;
		//用于向zookeeper注册名称服务的工具类
		this.serviceRegistry = serviceRegistry;
	}

	/**
	 * 通过注解，获取标注了rpc服务注解的业务类的----接口及impl对象，将它放到handlerMap中
	 */
	
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
		if(MapUtils.isNotEmpty(serviceBeanMap)) {
			for(Object serviceBean : serviceBeanMap.values()) {
				//从业务实现类上的自定义注解中获取到value，从来获取到业务接口的全名
				String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();
				handleMap.put(interfaceName, serviceBean);
			}
		}	
			
	}

	/**
	 * 在此启动netty服务，绑定handle流水线：
	 * 1、接收请求数据进行反序列化得到request对象
	 * 2、根据request中的参数，让RpcHandler从handlerMap中找到对应的业务imple，调用指定方法，获取返回结果
	 * 3、将业务调用结果封装到response并序列化后发往客户端
	 *
	 */
	public void afterPropertiesSet() throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		NioEventLoopGroup workGroup = new NioEventLoopGroup();
		try {			
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap
			.group(bossGroup,workGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(new ChannelInitializer<SocketChannel>() {
				
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					
					ch.pipeline()
					.addLast(new RpcDecoder(RpcRequest.class)) //In-1
					.addLast(new RpcEncoder(RpcResponse.class))//OUT
					.addLast(new RpcHandler(handleMap)); //In-2
					
				}
			}).option(ChannelOption.SO_BACKLOG, 128)
			.childOption(ChannelOption.SO_KEEPALIVE, true);

		String [] array = serverAddress.split(":");
			String host = array[0];
			int port = Integer.parseInt(array[1]);
			ChannelFuture future = bootstrap.bind(host,port).sync();
			LOGGER.debug("server started on port {}", port);

			if (serviceRegistry != null) {
				serviceRegistry.register(serverAddress);
			}

			future.channel().closeFuture().sync();
			
			
			
			
			
		} finally {
			workGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

}
