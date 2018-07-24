package net.dewcloud.simplerpc.client.sample;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.dewcloud.simplerpc.client.RpcProxy;
import net.dewcloud.simplerpc.service.HelloService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring.xml")
public class StartRpcClient {

	@Autowired
	private RpcProxy rpcProxy;
	
	@Test
	public void helloTest1() {
		// 调用代理的create方法，代理HelloService接口
		HelloService helloService = rpcProxy.create(HelloService.class);
		//调用代理方法 执行invoke
		
		String result = helloService.hello("World");
		System.out.println("服务器返回  :");
		System.out.println(result);
	}
}
