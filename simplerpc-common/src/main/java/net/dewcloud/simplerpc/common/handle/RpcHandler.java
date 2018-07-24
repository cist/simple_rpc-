package net.dewcloud.simplerpc.common.handle;

import java.lang.reflect.Method;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest>{

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcHandler.class);
	
	private final Map<String, Object> handlerMap;

	
	
	public RpcHandler(Map<String, Object> handlerMap) {
		this.handlerMap = handlerMap;
	}



	/**
	 * 接收消息，处理消息，返回结果
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
		RpcResponse response = new RpcResponse();
		response.setRequestId(request.getRequestId());
		//根据request来处理具体的业务调用
		try {
			Object result = handle(request);
			response.setResult(result);
		} catch (Throwable e) {
			response.setError(e);
		}
		
	}


	/**
	 * 根据request来处理具体的业务调用
	 * 调用是通过反射的方式来完成
	 * 
	 * @param request
	 * @return
	 * @throws Throwable
	 */
	private Object handle(RpcRequest request) throws Throwable{
		String className = request.getClassName();
		//拿到实现类对象
		Object serviceBean = handlerMap.get(className);
		//拿到要调用的方法名,参数类型,参数值.
		String methodName = request.getMethodName();
		Class<?>[] parameterTypes = request.getParameterTypes();
		Object [] parameters = request.getParameters();
		//拿到接口类
		Class<?> forName = Class.forName(className);
		Method method = forName.getMethod(methodName, parameterTypes);
		return method.invoke(serviceBean, parameters);
	}

}
