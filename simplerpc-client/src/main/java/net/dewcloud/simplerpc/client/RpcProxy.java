package net.dewcloud.simplerpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

import net.dewcloud.simplerpc.common.ServiceDiscovery;
import net.dewcloud.simplerpc.common.handle.RpcRequest;
import net.dewcloud.simplerpc.common.handle.RpcResponse;

/**
 * RPC 代理（用于创建 RPC 服务代理）
 *
 */
public class RpcProxy {
    private String serverAddress;
    private ServiceDiscovery serviceDiscovery;
	public RpcProxy(String serverAddress) {
		
		this.serverAddress = serverAddress;
	}
	public RpcProxy(ServiceDiscovery serviceDiscovery) {
		 
		this.serviceDiscovery = serviceDiscovery;
	}
    
	/**
	 * 创建代理
	 * 
	 * @param interfaceClass
	 * @return
	 */
	public <T> T create(Class<?> interfaceClass){
		return (T)Proxy.newProxyInstance(interfaceClass.getClassLoader(), 
				new Class<?> [] {interfaceClass}, new InvocationHandler() {
					
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						//创建RpcRequest ,封装被代理类的属性
						
						RpcRequest request = new RpcRequest();
						request.setRequestId(UUID.randomUUID().toString());
						request.setClassName(method.getDeclaringClass().getName());
						request.setMethodName(method.getName());
						request.setParameterTypes(method.getParameterTypes());
						request.setParameters(args);
						//查找服务
						if(serviceDiscovery != null) {
							serverAddress = serviceDiscovery.discover();
						}
						//随机获取服务的地址
						String [] array = serverAddress.split(":");
						String host = array[0];
						int port = Integer.parseInt(array[1]);
						//创建Netty 实现RpcClient 创建服务端
						
						RpcClient client = new RpcClient(host,port);
						RpcResponse response = client.send(request);
						if(response.isError()) {
							throw response.getError();
						}
						else {return response.getResult();}
					}
				});
	}
}
