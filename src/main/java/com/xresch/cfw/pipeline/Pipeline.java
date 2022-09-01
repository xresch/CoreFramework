package com.xresch.cfw.pipeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWContextAwareExecutor;
import com.xresch.cfw.features.analytics.TaskCPUSampling;
import com.xresch.cfw.logging.CFWLog;

public class Pipeline<I, O> {
	private static Logger logger = CFWLog.getLogger(TaskCPUSampling.class.getName());
	
	@SuppressWarnings("rawtypes")
	protected ArrayList<PipelineAction> actionArray = new ArrayList<PipelineAction>();
	protected ArrayList<LinkedBlockingQueue<?>> queues = new ArrayList<LinkedBlockingQueue<?>>();
	protected CountDownLatch latch;
	
	protected LinkedBlockingQueue<I> firstQueue = null;
	protected LinkedBlockingQueue<O> lastQueue = new LinkedBlockingQueue<O>();

	protected ThreadPoolExecutor defaultThreadPoolExecutor = 
			CFWContextAwareExecutor.createExecutor("PipelineDefaultPool", 1, 20, 500, TimeUnit.MILLISECONDS);
	
	
	/*************************************************************************************
	 * Constructor
	 *************************************************************************************/
	protected Pipeline() {
		
	}
	

	/*************************************************************************************
	 * Start all the actions as separate threads.
	 * @param maxExecTimeSec TODO
	 * @param doWait if true, stay in this method until execution is complete. If false, 
	 *   the caller of the method will manually use method waitForComplete() or isComplete()
	 *   to check completion status.
	 * @return
	 *************************************************************************************/
	public Pipeline<I, O> execute(long maxExecTimeSec, boolean doWait) {
				
		//-----------------------------------
		// Check has Actions
		if(actionArray.size() == 0) {
			new CFWLog(logger)
				.warn("No actions in pipeline.", new Throwable());
			
			return null;
		}

		//-----------------------------------
		// Initialize
		latch = new CountDownLatch(actionArray.size());

		actionArray.get(actionArray.size()-1).setOutQueue(lastQueue);
		
		//-----------------------------------
		// Initialize
		for (PipelineAction action : actionArray) {
			action.setLatch(latch);
			
			defaultThreadPoolExecutor.submit(action);
			//action.start();
			
			//------------------------------
			// Wait until initialized
			while (!action.isInitialized) {
				try {
					Thread.sleep(10);
				}catch (InterruptedException e) {
					new CFWLog(logger).warn("Pipeline execution was interupted.", e);
					Thread.currentThread().interrupt();
					return null;
				}	
			}
		}
		
		//-----------------------------------
		// Wait until Pipeline is Complete
		if(doWait) {
			return waitForComplete(maxExecTimeSec);
		}else {
			return this;
		}
		
	}
	
	/*************************************************************************************
	 * Cancel the execution of this pipeline and all associated actions.
	 *************************************************************************************/
	public void cancelExecution() {
				
		for(PipelineAction<I,O> action : actionArray) {
			action.setDone();
			action.inQueue.clear();
			action.outQueue.clear();
			action.interrupt();
		}
		
		Thread.currentThread().interrupt();
			
	}
	
	
	/*************************************************************************************
	 * Waits until all actions have completed or thread is interrrupted.
	 * @param maxExecTimeSec TODO
	 * @param args
	 * @return
	 *************************************************************************************/
	public Pipeline<I, O> waitForComplete(long maxExecTimeSec) {
		try {
			if(maxExecTimeSec > 0) {
				boolean hasCompleted = latch.await(maxExecTimeSec, TimeUnit.SECONDS);
				if(!hasCompleted) {
					new CFWLog(logger).warn("Pipeline execution reached time limit of "+maxExecTimeSec+" second(s), execution aborted.");
					this.cancelExecution();
				}
			}else {
				latch.await();
			}
		} catch (InterruptedException e) {
			new CFWLog(logger).warn("Pipeline execution was interupted.", e);
			this.cancelExecution();
			return null;
		}	
		
		return this;
	}
	
	/*************************************************************************************
	 * Returns true if all actions have completed.
	 * 
	 *************************************************************************************/
	public boolean isComplete() {
		return latch.getCount() == 0;
	}
	
	/*************************************************************************************
	 * Returns true if all actions have completed and the last queue is empty.
	 * Useful to poll everything from the last queue manually.
	 * 
	 *************************************************************************************/
	public boolean isFullyDrained() {
		
		return this.isComplete() && lastQueue.isEmpty();
	}
	
	/*************************************************************************************
	 * Start all the actions as separate threads.
	 * @param args
	 * @return
	 *************************************************************************************/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void add(PipelineAction nextAction) {
		
		if(actionArray.size() > 0) {
			PipelineAction previousAction = actionArray.get(actionArray.size()-1);
			previousAction.setOutQueue(nextAction.getInQueue());
			previousAction.setNextAction(nextAction);
			
			nextAction.setPreviousAction(previousAction);
			
		}else {
			this.firstQueue = nextAction.getInQueue();
		}
		
		nextAction.setParent(this);
		actionArray.add(nextAction);
		queues.add(nextAction.getInQueue());
				
	}
	
	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public  Pipeline<I, O> data(I[] data) {
		if(firstQueue != null) {
			firstQueue.addAll(Arrays.asList(data));
		}
		return this;
	}
	
	/*************************************************************************************
	 * 
	 * @param args
	 * @return
	 *************************************************************************************/
	public String resultToString() {
		
		StringBuilder builder = new StringBuilder();
		
		while(!lastQueue.isEmpty()) {
			builder.append(lastQueue.poll().toString()).append("\n");
		}
		
		return builder.toString();
	}
	
	/*************************************************************************************
	 * @param args
	 * @return
	 *************************************************************************************/
	public LinkedBlockingQueue<O> getLastQueue() {
		
		return lastQueue;

	}
	
	/*************************************************************************************
	 * Returns the last action of the pipeline
	 * returns null if the action array is empty
	 *************************************************************************************/
	@SuppressWarnings("rawtypes")
	public PipelineAction getLastAction() {
		
		if(actionArray.isEmpty()) return null;
		
		return actionArray.get(actionArray.size()-1);

	}
	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public void dumpSysoutLoop(int intervalSeconds, int count) {
		
		for(int i = 0; i < count; i++) {
			System.err.println("========================");
			System.err.println(dumpActionStatus());
			
			try {
				Thread.sleep(1000L*intervalSeconds);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}
	}
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public String dumpActionStatus() {
		
		StringBuilder builder = new StringBuilder();
		
		for(PipelineAction action : actionArray) {
			builder
				.append(action.getClass().getSimpleName())
				.append(" {isDone: ").append(action.isDone()).append(", ")
				.append("inQueueSize: ").append(action.inQueue.size()).append(", ")
				.append("outQueueSize: ").append(action.outQueue.size()).append("}\n");
		}
		return builder.toString();
	}
}
