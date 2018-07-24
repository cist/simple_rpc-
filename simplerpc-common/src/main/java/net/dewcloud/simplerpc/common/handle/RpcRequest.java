package net.dewcloud.simplerpc.common.handle;

/**
 * 封装 RPC 请求
 *	封装发送的object的反射属性
 */
public class RpcRequest {

	private String requestId; //请求Id
    private String className; //请求的接口类
    private String methodName;//方法名称
    private Class<?>[] parameterTypes;//参数类型
    private Object[] parameters;//参数值
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}
	public void setParameterTypes(Class<?>[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}
	public Object[] getParameters() {
		return parameters;
	}
	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}
	
    
    
}
