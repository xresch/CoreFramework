package com.xresch.cfw.pipeline;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import com.xresch.cfw._main.CFW;

// http://www.informit.com/articles/article.aspx?p=366887&seqNum=8

public abstract class PipelineAction<I, O> extends Thread {
	
	protected Object synchLock = new Object();
	protected Pipeline<O, ?> parent = null;
	protected PipelineAction<?, I> previousAction = null;
	protected PipelineAction<O, ?> nextAction = null;
	
	protected LinkedBlockingQueue<I> inQueue = new LinkedBlockingQueue<I>();
	protected LinkedBlockingQueue<O> outQueue;
	
	protected PipelineActionContext context;
	
	CountDownLatch latch;

	protected boolean done;
	
	// override to specify compute step
	public abstract void execute(PipelineActionContext context) throws Exception;
	
	void initializeAction() throws Exception {}

	void terminateAction() throws Exception { }

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
					this.waitForInput(50);
				}
				
			this.terminateAction();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			latch.countDown();
		}
	}

	public PipelineAction<?, I> getPreviousAction() {
		return previousAction;
	}

	public PipelineAction<I, O> setPreviousAction(PipelineAction<?, I> previousAction) {
		this.previousAction = previousAction;
		return this;
	}

	public PipelineAction<O, ?> getNextAction() {
		return nextAction;
	}

	public PipelineAction<I, O> setNextAction(PipelineAction<O, ?> nextAction) {
		this.nextAction = nextAction;
		return this;
	}

	public LinkedBlockingQueue<I> getInQueue() {
		return inQueue;
	}

	public PipelineAction<I, O> setInQueue(LinkedBlockingQueue<I> in) {
		this.inQueue = in;
		return this;
	}

	public LinkedBlockingQueue<O> getOutQueue() {
		return outQueue;
	}

	public PipelineAction<I, O> setOutQueue(LinkedBlockingQueue<O> out) {
		this.outQueue = out;
		return this;
	}

	public boolean isPreviousDone() {
		
		if(previousAction == null || previousAction.isDone()) {
			return true; 
		}
		
		return false;
	}
	
	public boolean isDone() {
		return done;
	}

	protected PipelineAction<I, O> setDone(boolean done) {
		this.done = done;
		return this;
	}

	public CountDownLatch getLatch() {
		return latch;
	}

	public PipelineAction<I, O> setLatch(CountDownLatch latch) {
		this.latch = latch;
		return this;
	}


	public void setDoneIfPreviousDone() {
		
		if(previousAction == null) {
			this.setDone(true);
		}else if(previousAction.isDone()) {
			this.setDone(true);
		}
	}
	
	public void waitForInput(long millis) {
		if(previousAction != null && !previousAction.isDone()) {
			synchronized(this.synchLock) {
				try {
					this.synchLock.wait(millis);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	public Pipeline<O, ?> getParent() {
		return parent;
	}

	public PipelineAction<I, O> setParent(Pipeline<O, ?> parent) {
		this.parent = parent;
		return this;
	}
	
	public PipelineActionContext getContext() {
		return context;
	}

	public PipelineAction<I, O> setContext(PipelineActionContext context) {
		this.context = context;
		return this;
	}
	
	
	
	
}
