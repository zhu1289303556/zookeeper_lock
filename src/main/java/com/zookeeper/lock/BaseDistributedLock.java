package com.zookeeper.lock;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

public class BaseDistributedLock {
	
	//Zookeeper客户端
	private final ZkClient client;
	//用于保存Zookeeper中实现分布式锁的节点，例如/locker节点，该节点是持久节点，该节点下面创建顺序节点
	private final String basePath;
	//子节点
	private final String path;
	
	public BaseDistributedLock(ZkClient client, String basePath, String path) {
		super();
		this.client = client;
		this.basePath = basePath;
		this.path = path;
	}
	
	/**
	 * 删除节点
	 * @param path
	 */
	private void deletePath(String path){
		this.client.delete(path);
	}
	
	/**
	 * 创建临时顺序节点
	 * @param path
	 * @return
	 */
	private String createEphemeralSequential(String path){
		if(!this.client.exists(basePath)){
			this.client.createPersistent(basePath);
		}
		return this.client.createEphemeralSequential(basePath.concat("/").concat(path), null);
	}
	/**
	 * 获取锁的核心方法
	 * @param startMillis
	 * @param millisToWait
	 * @param path
	 * @return
	 * @throws Exception 
	 */
	private boolean waitToLock(String outPath) throws Exception{
		//获取锁标志
		boolean haveTheLock = false; 
		
		//如果没有获取到锁
		while(!haveTheLock){
			 // 获取/locker节点下的所有顺序节点，并且从小到大排序
            List<String> children = getSortedChildren();
            // 获取当前的子节点，如：/locker/node_0000000003返回node_0000000003
            String sequenceNodeName = outPath.substring(basePath.length() + 1);
            
            //获取子节点在列表中的位置
            int index = children.indexOf(sequenceNodeName);
            
            if (index < 0) {
                throw new ZkNoNodeException("节点没有找到: " + sequenceNodeName);
            }
            
            // 如果当前客户端创建的节点在locker子节点列表中位置大于0，表示其它客户端已经获取了锁
            // 排在list列表中的第一个，表示获得了锁
            boolean isGetTheLock = (index == 0);           
            
            //如果得到锁
            if (isGetTheLock) {
                haveTheLock = true;
            } else {
            	// 如果没有得到锁，就监听前一个节点
            	// 使用CountDownLatch来实现等待
            	String pathToWatch = isGetTheLock ? null : children.get(index - 1); //得到前一个节点
                String previousSequencePath = basePath.concat("/").concat(pathToWatch);
                final CountDownLatch latch = new CountDownLatch(1);
     
                //监听比自己次小的那个节点
                client.subscribeDataChanges(previousSequencePath, new IZkDataListener() {
					
                	 /**
                     * 指定节点删除时触发该方法
                     */
                    public void handleDataDeleted(String dataPath)
                            throws Exception {
                        latch.countDown();
                    }

                    /**
                     * 指定节点的数据发生变化触发该方法
                     * 
                     */
                    public void handleDataChange(String dataPath,
                            Object data) throws Exception {

                    }
				}); 
                latch.await();
            }
		}
		return haveTheLock;
	}
	
	/**
	 * 获取排序后的子节点
	 * @return
	 */
	private List<String> getSortedChildren(){
		List<String> children = this.client.getChildren(basePath);
		
		Collections.sort(children, new Comparator<String>() {

			public int compare(String o1, String o2) {
				String str1 = o1.substring(o1.length()-10, o1.length());
				String str2 = o2.substring(o2.length()-10, o2.length());
				return str1.compareTo(str2);
			}
		});
		return children;
	}
	
	 protected boolean getLock(){
		boolean hasTheLock = false;
		try {
			String ourPath = createEphemeralSequential(path);
			hasTheLock = waitToLock(ourPath);
			
			System.out.println(ourPath + "锁获取" + (hasTheLock ? "成功" : "失败"));
			releaseLock(ourPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hasTheLock;
	 }
	
	
    /**
     * 释放锁
     * 
     * @param lockPath
     * @throws Exception
     */
    protected void releaseLock(String lockPath) throws Exception {
        deletePath(lockPath);
    }
}
