package com.xresch.cfw.features.jobs;

import java.util.logging.Logger;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

public abstract class CFWJobTask implements Job {
	
	private static Logger logger = CFWLog.getLogger(CFWJobTask.class.getName());
	
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
	public abstract boolean hasPermission();
	
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
	 * The defined task parameters can be retrieved as follows:
	 * <pre><code>
	 * JobDataMap data = context.getTrigger().getJobDataMap();
	 * String myParam = data.getString("myParam");
	 * </code></pre>
	 * 
	 *************************************************************************/
	public abstract void executeTask(JobExecutionContext context) throws JobExecutionException;
	
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
			executeTask(context);
			
		}catch(JobExecutionException e) {
			new CFWLog(logger)
				.silent(true)
				.contextless(true)
				.custom("jobid", jobID)
				.custom("taskname", this.uniqueName())
				.severe("Exception while executing task '"+this.uniqueName()+"': "+e.getMessage(), e);
			
			throw e;
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
					.custom("taskname", this.uniqueName())
					.severe("Error while writing last execution time to DB.");
			}
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
			CFW.Context.Request.addAlertMessage(MessageType.INFO, 
				"The defined schedule will make the job execute once or never."
			);
			return true;
		}
		
		if(scheduleIntervalSec < this.minIntervalSeconds()) {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, 
					"The minimum time interval for the selected task is "+this.minIntervalSeconds()+" second(s). "
					+"Your current schedule has an interval of "+scheduleIntervalSec+" second(s)"
			);
			return false;
		}
		
		return true;
	}
}
