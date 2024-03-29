package com.xresch.cfw.pipeline;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFWContextAwareExecutor;
import com.xresch.cfw.logging.CFWLog;

// http://www.informit.com/articles/article.aspx?p=366887&seqNum=8

public abstract class PipelineAction<I, O> extends Thread {
	
	private static final Logger logger = CFWLog.getLogger(PipelineAction.class.getName());
	
	private boolean isInitialized = false;

	protected Object synchLock = new Object();
	protected Pipeline<O, ?> parent = null;
	private PipelineAction<?, I> previousAction = null;
	private PipelineAction<O, ?> nextAction = null;
	
	protected LinkedBlockingQueue<I> inQueue = new LinkedBlockingQueue<I>();
	protected LinkedBlockingQueue<O> outQueue;
	
	protected PipelineActionContext context;
	
	protected ArrayList<PipelineActionListener> listenerArray = new ArrayList<>();
	
	protected CountDownLatch latch;

	protected boolean done;
	
	/****************************************************************************
	 * This is the main method of the Pipeline Action.
	 * The code in this method will be called by the thread until the action
	 * is finished.
	 * 
	 * IMPORTANT: 
	 *   This method will be called multiple times if "while(keepPolling()){}" is used.
	 *   Whenever the inQueue gets empty, the loop be exited and the pipeline will wait until there 
	 *   are more items in the inQueue before calling this method again.
	 *   So it is important to not store any information in local(method) variables if the info should
	 *   persist over multiple calls. 
	 ****************************************************************************/
	public abstract void execute(PipelineActionContext context) throws Exception, InterruptedException;

	/****************************************************************************
	 * Override to initialize the action. 
	 * This method is guaranteed to execute before any following action is 
	 * started.
	 ****************************************************************************/
	public void initializeAction() throws Exception {}

	/****************************************************************************
	 * 
	 ****************************************************************************/
	public void terminateAction() throws Exception { }

	/****************************************************************************
	 * Handle exceptions that occured during pipeline execution 
	 ****************************************************************************/
	public void handleException(Throwable e){ 
		new CFWLog(logger).severe(e.getMessage(), e);
	}

	/****************************************************************************
	 * 
	 ****************************************************************************/
	@Override
	public void run() {
		try {
			//------------------------------
			// Wait until previous initialized
			while ( this.getPreviousAction() != null
			&& !this.getPreviousAction().isInitialized()
			&& !this.parent.isCancelled()) {
				try {
					Thread.sleep(1);
				}catch (InterruptedException e) {
					new CFWLog(logger).warn("Pipeline execution was interupted.", e);
					Thread.currentThread().interrupt();
					return;
				}
			}
			
			//------------------------------
			// Initialize this
			this.initializeAction();
			this.isInitialized = true;
			
			//------------------------------
			// RUUUUUUUUNNNN!!!!
			while (!done && !this.isInterrupted()) {
					
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
					this.waitForInput(10);
				}
				
			this.terminateAction();
			
		} catch (InterruptedException e) { 
			// do nothing, expected exception caused by commands like top
		} catch (Throwable e) {
			this.handleException(e);
			parent.cancelExecution();
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
		if(previousAction != null) {
			previousAction.setOutQueue(this.getInQueue());
		}
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
	@SuppressWarnings("rawtypes")
	public PipelineAction getLastAction() {
		return this.getParent().getLastAction();
	}

	/****************************************************************************
	 * 
	 ****************************************************************************/
	public PipelineAction<I, O> setNextAction(PipelineAction<O, ?> nextAction) {
		this.nextAction = nextAction;
		if(nextAction != null) {
			this.setOutQueue(nextAction.getInQueue());
		}
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
	 * 
	 ****************************************************************************/
	public boolean isInitialized() {
		return isInitialized;
	}

	/****************************************************************************
	 * Check if the current action should still read from the inQueue or if 
	 * the thread was interrupted.
	 ****************************************************************************/
	public boolean keepPolling() throws InterruptedException {
		
		if(this.isInterrupted()) {
			throw new InterruptedException();
		}
		
		if(!inQueue.isEmpty()
		) {
			return true; 
		}
		
		if(previousAction == null){
			return false; 
		}
		
		return false;
	}
		
	/****************************************************************************
	 * 
	 ****************************************************************************/
	public boolean isPreviousDone() {
		
		if(previousAction == null 
		|| previousAction.isDone()
		|| previousAction.isInterrupted()) {
			return true; 
		}
		
		return false;
	}
	
	/****************************************************************************
	 * 
	 ****************************************************************************/
	public boolean isDone() {
		return done || this.isInterrupted();
	}
	
	/****************************************************************************
	 * Set this action to done
	 ****************************************************************************/
	protected PipelineAction<I, O> setDone() {
		
		for(PipelineActionListener listener : listenerArray) {
			listener.onDone();
		}
		
		this.done = true;
	
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
			this.setDone();
		}
	}
	
	/****************************************************************************
	 * 
	 ****************************************************************************/
	public void interruptAllPrevious() {
		
		if(previousAction != null) {
			previousAction.interrupt();
			
			previousAction.setDone();

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
	public void addListener(PipelineActionListener listener) {
		this.listenerArray.add(listener);
	}
	
	/****************************************************************************
	 * 
	 ****************************************************************************/
	public void removeListener(PipelineActionListener listener) {
		this.listenerArray.remove(listener);
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
