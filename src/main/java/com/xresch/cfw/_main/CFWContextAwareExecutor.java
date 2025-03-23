package com.xresch.cfw._main;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import com.xresch.cfw._main.CFWContextRequest.CFWContextObject;

public class CFWContextAwareExecutor extends ThreadPoolExecutor {

	CFWContextObject requestContextReference = CFW.Context.Request.getContext();
	
	/**************************************************************************
	 * 
	 **************************************************************************/
	public static CFWContextAwareExecutor createExecutor(String poolName, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
		LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();

		BasicThreadFactory factory = new BasicThreadFactory.Builder()
				.namingPattern("[Pool:"+poolName+"-"+CFW.Random.stringAlphaNum(6)+"] thread-%d")
				.daemon(false)
				.priority(Thread.NORM_PRIORITY)
				.build();
		
		return new CFWContextAwareExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, factory);
		
	}
	
	/**************************************************************************
	 * 
	 **************************************************************************/
	public CFWContextAwareExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
		this.allowCoreThreadTimeOut(true);
	}

	/**************************************************************************
	 * 
	 **************************************************************************/
	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		CFW.Context.Request.setContext(requestContextReference);
	}
	
	/**************************************************************************
	 * 
	 **************************************************************************/
	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		CFW.Context.Request.clearRequestContext();
	}
	

}
