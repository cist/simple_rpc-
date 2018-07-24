package net.dewcloud.simplerpc.service;

import net.dewcloud.simplerpc.entry.Person;

//业务逻辑的接口
public interface HelloService {
	String hello(String name);
	String hello(Person person);
}
