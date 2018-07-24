package net.dewcloud.simplerpc.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 本类用于client发现server节点的变化 ，实现负载均衡
 *
 */
public class ServiceDiscovery {

	
	private static final Logger  LOGGER = LoggerFactory.getLogger(ServiceDiscovery.class);
	
	private CountDownLatch latch = new CountDownLatch(1);
	
	private volatile List<String> dataList = new ArrayList<String>();
	
	private String registryAddress;

	/**
	 * zk链接
	 * 
	 * @param registryAddress
	 */
	public ServiceDiscovery(String registryAddress) {
		this.registryAddress = registryAddress;
		ZooKeeper zk = connectServer();
		
		if(zk != null) {
			watchNode(zk);
		}
		
	
	
	}

	/**
	 * 发现新节点
	 * 
	 * @return
	 */
	public String discover() {
		String data = null;
		
		int dataSize = dataList.size();
		if (dataSize > 0) {
			if(dataSize == 1) {
				data = dataList.get(0);
				LOGGER.debug("using only data: {}", data);
			}else {
				data = dataList.get(ThreadLocalRandom.current().nextInt(dataSize));
				LOGGER.debug("using random data: {}", data);
			}
			
		}
		return data;
		
	}
	
	
	
	
	

	/**
	 * 链接
	 * 
	 * @return
	 */
	private ZooKeeper connectServer() {
		ZooKeeper zk = null;
		
		try {
			zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
				
				public void process(WatchedEvent event) {
					if(event.getState() == Event.KeeperState.SyncConnected) {
						
						latch.countDown();
					}
					
				}
				
			});
			latch.await();
		} catch (IOException e) {
			LOGGER.error("class:/simplerpc-common/src/main/java/net/dewcloud/simplerpc/common/ServiceDiscovery.java exception：IOException "+e.getMessage());
		} catch (InterruptedException e) {
			LOGGER.error("class:/simplerpc-common/src/main/java/net/dewcloud/simplerpc/common/ServiceDiscovery.java InterruptedException "+e.getMessage());
			 
		}
		return zk;
	}
	
	
	
	
	
	/**
	 * 监听
	 * 
	 * @param zk
	 */
	public void watchNode(final ZooKeeper zk) {
	 try {
		List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH, new Watcher() {

			public void process(WatchedEvent event) {
				 if(event.getType() == Event.EventType.NodeChildrenChanged){
					 watchNode(zk);
				 }
				
			}
		});
		List<String> dataList = new ArrayList<String>();
		
		for(String node : nodeList) {
			byte[] data = zk.getData(Constant.ZK_REGISTRY_PATH+"/"+node, false,null);
			
			dataList.add(new String(data));
			
		}
		LOGGER.debug("node data: {}", dataList);
		this.dataList = dataList;
	} catch (KeeperException e) {
		
		LOGGER.error("class:/simplerpc-common/src/main/java/net/dewcloud/simplerpc/common/ServiceDiscovery.java KeeperException "+e.getMessage());	
	} catch (InterruptedException e) {
	
		LOGGER.error("class:/simplerpc-common/src/main/java/net/dewcloud/simplerpc/common/ServiceDiscovery.java InterruptedException "+e.getMessage());
	}
		
	}
}
