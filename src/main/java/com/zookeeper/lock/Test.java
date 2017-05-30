package com.zookeeper.lock;

import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.ZkClient;

public class Test {
	
	private final static String host = "192.168.161.138";
	private final static String basePath = "/registry";
	private final static int timeout = 3000;
	
	public static void main(String[] args) {
		
		ZkClient zk = new ZkClient(host , timeout);
		BaseDistributedLock lock = new BaseDistributedLock(zk, basePath, "test");
		
		for (int i = 0; i < 10; i++) {
			try {
				lock.getLock();
				System.out.println("正在进行运算操作：" + System.currentTimeMillis());
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}
}
