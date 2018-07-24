package net.dewcloud.simplerpc.common.handle;

/**
 * 封装 RPC 响应
 * 封装相应object
 */
public class RpcResponse {
	 private String requestId; //请求ID
	 private Throwable error;  //异常情况
	 private Object result;//返回结果 
	 public boolean isError() {
		 return error != null;
	 }
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public Throwable getError() {
		return error;
	}
	public void setError(Throwable error) {
		this.error = error;
	}
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}
	 
}
