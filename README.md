# zookeeper_lock
简单实现分布式锁

#用到的技术

ZkClient

ZooKeeper

#伪代码

	protected boolean getLock(){
	    //创建一个临时节点
	    String ourPath = createEphemeralSequential(path);
	    //该节点用没有得到锁
	    hasTheLock = waitToLock(ourPath);
	}

	private boolean waitToLock(String outPath){
	    //如果没有获取到锁
			while(!haveTheLock){
		//子节点排序，查看当前节点是不是第一个
		List<String> children = getSortedChildren();
		//如果是第一个说明得到了锁

		//否则一直监听当前节点的前一个节点，有没有变化（删除掉）

	}


