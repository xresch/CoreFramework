package com.xresch.cfw.features.jobs;

import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;

import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.utils.CFWMonitor;

import io.prometheus.client.Counter;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public abstract class CFWJobTask implements InterruptableJob {
	
	private static Logger logger = CFWLog.getLogger(CFWJobTask.class.getName());
	
	private static final Counter executionCounter = Counter.build()
	         .name("cfw_job_executions_total")
	         .help("Number of jobs executed in total.")
	         .register();
	
	private boolean isInterrupted = false;
	
	/*************************************************************************
	 * Return a unique name for this executor.
	 * This will be used in the UI and as ID in the database.
	 * Changing this name afterwards will corrupt existing Jobs using this
	 * executor.
	 *************************************************************************/
	public abstract String uniqueName();
	
	/*************************************************************************
	 * Return a description for the job executor.
	 *************************************************************************/
	public abstract String taskDescription();
	
	/*************************************************************************
	 * Return a CFWObject containing Fields used as parameters for the task
	 * execution. Feel free to specify default values on the fields.
	 * 
	 * See {@link CFWJobTask#executeTask} for how to use the parameters
	 * when executing the task.
	 *************************************************************************/
	public abstract CFWObject getParameters();
	
	/*************************************************************************
	 * Return the minimum schedule interval required for this tasks.
	 * Needed to prevent users to define tasks which are executed extensively.
	 * 
	 *************************************************************************/
	public abstract int minIntervalSeconds();
	
	/*************************************************************************
	 * Return if the user is able to select this executor for creating Jobs.
	 *************************************************************************/
	public abstract boolean hasPermission(User user);
	
	/************************************************************
	 * Return the file definitions of the client side part of the 
	 * script.
	 * @return file definition
	 ************************************************************/
	public abstract HashMap<Locale, FileDefinition> getLocalizationFiles();
	
	/*************************************************************************
	 * Override this method if you do not want to allow users to choose the 
	 * task from the UI.
	 * If this method returns false, hasPermission() is ignored.
	 *************************************************************************/
	public boolean createableFromUI() {
		return true;
	}
	/*************************************************************************
	 * Returns if the job was interrupted.
	 * 
	 *************************************************************************/
	public boolean isInterrupted() {
		return isInterrupted;
	}
	
	/*************************************************************************
	 * Set the thread as interrupted.
	 *************************************************************************/
    public void interrupt() throws UnableToInterruptJobException {
        isInterrupted = true;
    }
    
	/*************************************************************************
	 * Implement the actions your task should execute.
	 * Do not store any values locally in the instance of the class implementing 
	 * this method. If you need to store data related to the task, you might want
	 * to put the data into a cache based on the jobID.
	 * <br>
	 * The jobID(the primary key from CFW_JOB table as string) can be retrieved
	 * as follows:
	 * <pre><code>
	 * context.getJobDetail().getKey().getName();
	 * </code></pre>
	 * 
	 * The defined task parameters have to be retrieved as follows. There are
	 * other methods to retrieve the JobDataMap, but if you use those you
	 * might not always get the parameters in the map.
	 * <pre><code>
	 * JobDataMap data = context.getMergedJobDataMap();
	 * String myParam = data.getString("myParam");
	 * </code></pre>
	 * 
	 * To add messages to the execution, use the method provided by CFW.Messages.
	 * This will be displayed as an icon in the first column of the table on 
	 * the user interface.
	 * 
	 * <pre><code>
	 * CFW.Messages.addMessage(MessageType.INFO, "Example Message");
	 * CFW.Messages.add*Message("Example Message INFO/SUCCESS/WARNING/ERROR");
	 * </code></pre>
	 * 
	 * @param context the context of the job
	 * @param monitor use the check()-method so monitor if the job is still running(true) or if it has been interrupted(false)
	 * 
	 *************************************************************************/
	public abstract void executeTask(JobExecutionContext context, CFWMonitor monitor) throws JobExecutionException;
	
	/*************************************************************************
	 * Wraps the original method of the Job class to add logging and last run.
	 *************************************************************************/
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		String jobID = context.getJobDetail().getKey().getName();
		//---------------------------------------
		// Start Log
		CFWLog log = new CFWLog(logger)
			.contextless(true)
			.custom("jobid", jobID)
			.custom("taskname", this.uniqueName())
			.start();
		
		try{
			

			//---------------------------------------
			// Execute Task Implementation
			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						
						CFWMonitor monitor = new CFWMonitor() {
							@Override
							protected boolean monitorCondition() { return !isInterrupted();}
						};
						
						executeTask(context, monitor);
					}catch(JobExecutionException e) {
						new CFWLog(logger)
							.silent(true)
							.contextless(true)
							.custom("jobid", jobID)
							.custom("taskname", uniqueName())
							.severe("Exception while executing task '"+uniqueName()+"': "+e.getMessage(), e);

					}finally {
						//---------------------------------------
						// Write duration
						log.end();
						
						//---------------------------------------
						// Update Last Run
						if(!CFW.DB.Jobs.updateLastRun(jobID)) {
							new CFWLog(logger)
								.silent(true)
								.contextless(true)
								.custom("jobid", jobID)
								.custom("taskname", uniqueName())
								.severe("Error while writing last execution time to DB.");
						}
						
						executionCounter.inc();
					}
					}
					 
				});
			
			thread.start();
			
			while(thread.isAlive()) {
				Thread.sleep(1000);
				if(isInterrupted) {
					thread.interrupt();
					//thread.stop(); 
				}
			}
			
		}catch(InterruptedException e){
			new CFWLog(logger)
				.silent(true)
				.contextless(true)
				.custom("jobid", jobID)
				.custom("taskname", uniqueName())
				.severe("Job was interrupted.");
		}
	}
	
	/*************************************************************************
	 * Check if the provided schedule Interval in seconds does fulfill
	 * the minimum Interval seconds required for this task.
	 * This method may add alert messages to the request context.
	 * @return true if valid, false otherwise
	 *************************************************************************/
	public boolean isMinimumIntervalValid(int scheduleIntervalSec) {
		
		if(scheduleIntervalSec == -1) {
			CFW.Messages.addInfoMessage(
				"The defined schedule will make the job execute once or never."
			);
			return true;
		}
		
		if(scheduleIntervalSec < this.minIntervalSeconds()) {
			CFW.Messages.addErrorMessage(
					"The minimum time interval for the selected task is "+this.minIntervalSeconds()+" second(s). "
					+"Your current schedule has an interval of "+scheduleIntervalSec+" second(s)"
			);
			return false;
		}
		
		return true;
	}
}
