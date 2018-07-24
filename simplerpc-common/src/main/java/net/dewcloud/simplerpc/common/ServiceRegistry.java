package net.dewcloud.simplerpc.common;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event;
import org.apache.zookeeper.ZooDefs.Ids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * zookeeper 注册服务 ZK 在该架构中扮演了“服务注册表”的角色，用于注册所有服务器的地址与端口，并对客户端提供服务发现的功能
 * 
 * @author 57871
 *
 */
public class ServiceRegistry {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ServiceRegistry.class);

	//使用它来提示线程的执行程度   用法：每当一个任务线程执行完毕，就将计数器减1 countdownlatch.countDown()，当计数器的值变为0时，在CountDownLatch上 await() 的线程就会被唤醒。
	private CountDownLatch latch = new CountDownLatch(1);
	
	private String registryAddress;

	public ServiceRegistry(String registryAddress) {
		this.registryAddress = registryAddress;
	}

	/**
	 * 创建zookeeper链接
	 * 
	 * @param data
	 */
	public void register(String data) {
		if (data != null) {
			ZooKeeper zk = connectServer();
			if (zk != null) {
				createNode(zk, data);
			}

		}
	}

	/**
	 * 创建zookeeper链接，监听
	 * 
	 * @return ZK
	 */

	private ZooKeeper connectServer() {
		ZooKeeper zk =null;
		try {
			zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT,new  Watcher() {

				public void process(WatchedEvent event) {
					/*  KeeperState.SynConnected means 
					 * The client is in the connected state - it is connected to a server in the
					 * ensemble (one of the servers specified in the host connection parameter
					 * during ZooKeeper client creation).
					 */
					if (event.getState() == Event.KeeperState.SyncConnected) {
						latch.countDown();
					}
				}
				
			});
			latch.await();
		} catch (IOException e) {
			
			LOGGER.error("io exception "+e.getMessage());
		} catch (InterruptedException e) {
		
			LOGGER.error("interrupt exception " + e.getMessage());
			
		}
		
		return zk;
	}

	/**
	 * 创建节点
	 * 
	 * @param zk
	 * @param data
	 */
	private void createNode(ZooKeeper zk, String data) {
		
		byte [] bytes = data.getBytes();
		
		try {
			if(zk.exists(Constant.ZK_REGISTRY_PATH, null) == null) {
				zk.create(Constant.ZK_REGISTRY_PATH, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			
			String path = zk.create(Constant.ZK_DATA_PATH, bytes, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			LOGGER.debug("create zookeeper node ({} => {})", path, data);
		} catch (KeeperException e) {
			LOGGER.error("keeperExcetion "+e.getMessage());
		} catch (InterruptedException e) {
			LOGGER.error("interrupution excetion  "+e.getMessage());
		}

	}

}


