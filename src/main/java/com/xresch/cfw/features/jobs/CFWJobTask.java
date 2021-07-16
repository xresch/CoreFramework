package com.xresch.cfw.features.jobs;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.xresch.cfw.logging.CFWLog;

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
	 * Return a list of CFWJob Properties with default values.
	 *************************************************************************/
	public abstract ArrayList<JobTaskProperty> jobProperties();
	
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
	 *************************************************************************/
	public abstract void executeTask(JobExecutionContext context) throws JobExecutionException;
	
	/*************************************************************************
	 * Wrap original method to add logging.
	 *************************************************************************/
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		CFWLog log = new CFWLog(logger)
			.contextless(true)
			.custom("jobid", context.getJobDetail().getKey().getName())
			.custom("taskname", this.uniqueName())
			.start();
		
		try{
			executeTask(context);
		}catch(JobExecutionException e) {
			new CFWLog(logger)
				.silent(true)
				.contextless(true)
				.custom("jobid", context.getJobDetail().getKey().getName())
				.custom("taskname", this.uniqueName())
				.severe("Exception while executing task '"+this.uniqueName()+"': "+e.getMessage(), e);
			
			throw e;
		}finally {
			log.end();
		}
	}
	
	
	/*************************************************************************
	 *************************************************************************
	 * INNER CLASS
	 *************************************************************************
	 *************************************************************************/
	public class JobTaskProperty{
		
		private String key;
		private String value;
		private String description;
		
		public JobTaskProperty(String key, String value, String description) {
			this.key = key;
			this.value = value;
			this.description = description;
		}

		public String getKey() {
			return key;
		}

		public JobTaskProperty setKey(String key) {
			this.key = key;
			return this;
		}

		public String getValue() {
			return value;
		}

		public JobTaskProperty setValue(String value) {
			this.value = value;
			return this;
		}

		public String getDescription() {
			return description;
		}

		public JobTaskProperty setDescription(String description) {
			this.description = description;
			return this;
		}
		
		
	}

}
