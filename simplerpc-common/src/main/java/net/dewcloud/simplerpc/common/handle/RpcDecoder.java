package net.dewcloud.simplerpc.common.handle;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * RPC 解码器
 *
 */
public class RpcDecoder extends ByteToMessageDecoder {

	
	private Class<?> genericClass;
	
	//构造函数传入向反序列化的class
	public RpcDecoder(Class<?> genericClass) {
		this.genericClass = genericClass;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if(in.readableBytes() < 4) {
			return ;
		}
		in.markReaderIndex();
		int dataLength = in.readInt();
		if(dataLength < 0) {
			ctx.close();
		}
		if(in.readableBytes() < dataLength) {
			in.resetReaderIndex();
		}
		byte [] data = new byte[dataLength];
		in.readBytes(data);
		//将data转换成Object
		Object object = SerializationUtil.deserialize(data,genericClass);
		out.add(object);
		
		
	}

}
