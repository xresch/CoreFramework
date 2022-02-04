package com.xresch.cfw.pipeline;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.xresch.cfw.logging.CFWLog;

// http://www.informit.com/articles/article.aspx?p=366887&seqNum=8

public abstract class PipelineAction<I, O> extends Thread {
	
	private static final Logger logger = CFWLog.getLogger(PipelineAction.class.getName());
	
	
	protected Object synchLock = new Object();
	protected Pipeline<O, ?> parent = null;
	protected PipelineAction<?, I> previousAction = null;
	protected PipelineAction<O, ?> nextAction = null;
	
	protected LinkedBlockingQueue<I> inQueue = new LinkedBlockingQueue<I>();
	protected LinkedBlockingQueue<O> outQueue;
	
	protected PipelineActionContext context;
	
	CountDownLatch latch;

	protected boolean done;
	

	/****************************************************************************
	 * This is the main method of the Pipeline Action.
	 * The code in this method will be called by the thread until the action
	 * is finished.
	 ****************************************************************************/
	public abstract void execute(PipelineActionContext context) throws Exception, InterruptedException;

	/****************************************************************************
	 * 
	 ****************************************************************************/
	void initializeAction() throws Exception {}

	/****************************************************************************
	 * 
	 ****************************************************************************/
	void terminateAction() throws Exception { }

	/****************************************************************************
	 * 
	 ****************************************************************************/
	@Override
	public void run() {
		try {
			this.initializeAction();

				while (!done) {
					
					//---------------------------
					// Execute
					this.execute(context);
					
					//---------------------------
					// Wake up next action
					if(nextAction != null) {
						synchronized(nextAction.synchLock) {
							nextAction.synchLock.notifyAll();
						}
					}
					
					//---------------------------
					// Wait for more input
					this.waitForInput(100);
				}
				
			this.terminateAction();
			
		} catch (InterruptedException e) { 
			// do nothing, expected exception caused by commands like top
		} catch (Exception e) {
			new CFWLog(logger).severe("Unexpected exception occured.", e);
			e.printStackTrace();
		}  finally {
			latch.countDown();
		}
	}

	/****************************************************************************
	 * 
	 ****************************************************************************/
	public PipelineAction<?, I> getPreviousAction() {
		return previousAction;
	}

	/****************************************************************************
	 * 
	 ****************************************************************************/
	public PipelineAction<I, O> setPreviousAction(PipelineAction<?, I> previousAction) {
		this.previousAction = previousAction;
		return this;
	}

	/****************************************************************************
	 * 
	 ****************************************************************************/
	public PipelineAction<O, ?> getNextAction() {
		return nextAction;
	}

	/****************************************************************************
	 * 
	 ****************************************************************************/
	public PipelineAction<I, O> setNextAction(PipelineAction<O, ?> nextAction) {
		this.nextAction = nextAction;
		return this;
	}

	/****************************************************************************
	 * 
	 ****************************************************************************/
	public LinkedBlockingQueue<I> getInQueue() {
		return inQueue;
	}

	/****************************************************************************
	 * 
	 ****************************************************************************/
	public PipelineAction<I, O> setInQueue(LinkedBlockingQueue<I> in) {
		this.inQueue = in;
		return this;
	}

	/****************************************************************************
	 * 
	 ****************************************************************************/
	public LinkedBlockingQueue<O> getOutQueue() {
		return outQueue;
	}

	/****************************************************************************
	 * 
	 ****************************************************************************/
	public PipelineAction<I, O> setOutQueue(LinkedBlockingQueue<O> out) {
		this.outQueue = out;
		return this;
	}

	/****************************************************************************
	 * Check if the current action should still read from the inQueue or if 
	 * the thread was interrupted.
	 ****************************************************************************/
	public boolean keepPolling() throws InterruptedException {
		
		if(this.isInterrupted()) {
			throw new InterruptedException();
		}
		
		
		if(previousAction == null){
			return false; 
		}
		
		if(!inQueue.isEmpty()
		) {
			return true; 
		}
		
		return false;
	}
	/****************************************************************************
	 * 
	 ****************************************************************************/
	public boolean isPreviousDone() {
		
		if(previousAction == null || ( previousAction.isDone())) {
			return true; 
		}
		
		return false;
	}
	
	/****************************************************************************
	 * 
	 ****************************************************************************/
	public boolean isDone() {
		return done;
	}
	
	/****************************************************************************
	 * 
	 ****************************************************************************/
	protected PipelineAction<I, O> setDone(boolean done) {
		this.done = done;
		return this;
	}
	
	/****************************************************************************
	 * 
	 ****************************************************************************/
	public CountDownLatch getLatch() {
		return latch;
	}
	
	/****************************************************************************
	 * 
	 ****************************************************************************/
	public PipelineAction<I, O> setLatch(CountDownLatch latch) {
		this.latch = latch;
		return this;
	}

	/****************************************************************************
	 * 
	 ****************************************************************************/
	public void setDoneIfPreviousDone() {
		
		if(isPreviousDone()) {
			this.setDone(true);
		}
	}
	
	/****************************************************************************
	 * 
	 ****************************************************************************/
	public void interruptAllPrevious() {
		
		if(previousAction != null) {
			previousAction.interrupt();
			
			previousAction.setDone(true);

			previousAction.interruptAllPrevious();
			
		}

	}
	
	
	/****************************************************************************
	 * 
	 ****************************************************************************/
	public void waitForInput(long millis) throws InterruptedException {
		if(previousAction != null && !previousAction.isDone()) {
			synchronized(this.synchLock) {

				this.synchLock.wait(millis);
			}
		}
	}

	/****************************************************************************
	 * 
	 ****************************************************************************/
	public Pipeline<O, ?> getParent() {
		return parent;
	}

	/****************************************************************************
	 * 
	 ****************************************************************************/
	public PipelineAction<I, O> setParent(Pipeline<O, ?> parent) {
		this.parent = parent;
		return this;
	}
	
	/****************************************************************************
	 * 
	 ****************************************************************************/
	public PipelineActionContext getContext() {
		return context;
	}

	/****************************************************************************
	 * 
	 ****************************************************************************/
	public PipelineAction<I, O> setContext(PipelineActionContext context) {
		this.context = context;
		return this;
	}
	
	
	
	
}
