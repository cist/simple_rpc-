package net.dewcloud.simplerpc.common.handle;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * RPC 编码器
 *
 */
public class RpcEncoder extends MessageToByteEncoder {

	private Class<?> gennericClass;
	
	// 构造函数传入向反序列化的class
	public RpcEncoder(Class<?> gennericClass) {
		this.gennericClass = gennericClass;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		if(gennericClass.isInstance(msg)) {
			
			byte[] data = SerializationUtil.serialize(msg);
			out.writeInt(data.length);
			out.writeBytes(data);
		}
		
	}

}
