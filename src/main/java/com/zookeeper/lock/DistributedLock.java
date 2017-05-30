package com.zookeeper.lock;

import java.util.concurrent.TimeUnit;

public interface DistributedLock {
	
	/**
	 * 获取锁
	 */
	public void getLock();
	
	/**
	 * 获取锁  是否获取到锁
	 * @param time
	 * @param unit
	 * @return
	 */
	public boolean getLock(long time, TimeUnit unit);

	/**
	 * 释放锁
	 */
	public void releaseLock();
}
