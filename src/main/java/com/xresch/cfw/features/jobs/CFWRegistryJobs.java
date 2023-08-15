package com.xresch.cfw.features.jobs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.datahandling.CFWSchedule;
import com.xresch.cfw.logging.CFWLog;

public class CFWRegistryJobs {

	private static Logger logger = CFWLog.getLogger(CFWRegistryJobs.class.getName());
	
	private static Scheduler scheduler = null;
	
	// UniqueName and JobTask
	private static LinkedHashMap<String, Class<? extends CFWJobTask>> jobtasksMap = new LinkedHashMap<>();

	private static ArrayList<CFWJobTask> instanceArray;

	/*************************************************************************
	 * 
	 *************************************************************************/
	public static void registerTask(CFWJobTask jobtasks) {
		
		if( jobtasksMap.containsKey(jobtasks.uniqueName()) ) {
			new CFWLog(logger).severe("A JobTask with the name '"+jobtasks.uniqueName()+"' has already been registered. Please change the name or prevent multiple registration attempts.");
			return;
		}
		
		HashMap<Locale, FileDefinition> localeFiles = jobtasks.getLocalizationFiles();
		if(localeFiles != null) {
			for(Entry<Locale, FileDefinition> entry : localeFiles.entrySet()) {
				CFW.Localization.registerLocaleFile(entry.getKey(), FeatureJobs.getJobsURI(), entry.getValue());
			}
		}
		
		jobtasksMap.put(jobtasks.uniqueName(), jobtasks.getClass());
		instanceArray = null;
	}
	
	
//	/***********************************************************************
//	 * Get a list of all task names that the current user has access to.
//	 * 
//	 ***********************************************************************/
//	public static Set<String> getTaskNamesForUI()  {
//		Set<String> taskNames = new HashSet<>();
//		
//		for(CFWJobTask task : getAllTaskInstances()) {
//			if(task.createableFromUI()
//			&& (
//					task.hasPermission(CFW.Context.Request.getUser())
//					|| CFW.Context.Request.hasPermission(FeatureJobs.PERMISSION_JOBS_ADMIN)
//				)
//			) {
//				taskNames.add(task.uniqueName());
//			}
//		}
//		return taskNames;
//	}

	/***********************************************************************
	 * Get a list of all executor instances.
	 * 
	 ***********************************************************************/
	public static JsonArray getTasksForUserAsJson()  {
		JsonArray taskArray = new JsonArray();
		
		for(CFWJobTask current : getAllTaskInstances()) {
			if(current.createableFromUI()
					&& (
							current.hasPermission(CFW.Context.Request.getUser())
							|| CFW.Context.Request.hasPermission(FeatureJobs.PERMISSION_JOBS_ADMIN)
						)
					) {
				JsonObject object = new JsonObject();
				object.addProperty("NAME", current.uniqueName());
				object.addProperty("DESCRIPTION", current.taskDescription());
				
				taskArray.add(object);
			}
		}
		
		return taskArray;
	}
		
	/***********************************************************************
	 * Get a list of all executor instances.
	 * 
	 ***********************************************************************/
	public static ArrayList<CFWJobTask> getAllTaskInstances()  {
		if(instanceArray != null) {
			return instanceArray;
		}
		
		instanceArray = new ArrayList<>();
		
		for(Class<? extends CFWJobTask> clazz : jobtasksMap.values()) {
			try {
				CFWJobTask instance = clazz.newInstance();
				instanceArray.add(instance);
			} catch (Exception e) {
				new CFWLog(logger).severe("Issue creating instance for Class '"+clazz.getName()+"': "+e.getMessage(), e);
			}
		}
		return instanceArray;
	}
	
	
	/***********************************************************************
	 * Get a new instance for the specified task.
	 * Returns null if the task is undefined.
	 ***********************************************************************/
	public static CFWJobTask createTaskInstance(String uniqueName)  {
		
		CFWJobTask instance = null;
		Class<? extends CFWJobTask> clazz =  jobtasksMap.get(uniqueName);
		try {
			if(clazz != null) {
				instance = clazz.newInstance();
			}
		} catch (Exception e) {
			new CFWLog(logger).severe("Issue creating instance for Class '"+clazz.getName()+"': "+e.getMessage(), e);
		}
		
		return instance;
	}
	
	/***********************************************************************
	 * Return the class for the specified task.
	 * Returns null if the task is undefined.
	 ***********************************************************************/
	public static Class<? extends CFWJobTask> getTaskClass(String uniqueName)  {
		
		return jobtasksMap.get(uniqueName);
	}
	
	/***********************************************************************
	 * Return the class for the specified task.
	 * Returns null if the task is undefined.
	 ***********************************************************************/
	private static Scheduler getScheduler() {
		
		if(scheduler == null) {
			StdSchedulerFactory factory = new StdSchedulerFactory();
			
			Properties props = new Properties();
			
			//--------------------------
			// General Properties
			props.put("org.quartz.scheduler.instanceName", "CFWScheduler");
			props.put("org.quartz.threadPool.threadCount", ""+CFW.Properties.JOB_THREADS);

			//--------------------------
			// JDBC Store Properties
			String datasourceName = "cfwDB";

			props.put("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
			props.put("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate"); // For H2
			props.put("org.quartz.jobStore.dataSource", datasourceName);
			props.put("org.quartz.jobStore.tablePrefix", "CFW_QUARTZ_");
			props.put("org.quartz.jobStore.isClustered", "true");

			props.put("org.quartz.dataSource."+datasourceName+".connectionProvider.class", "com.xresch.cfw.features.jobs.QuartzConnectionProvider");

			try {
				factory.initialize(props);
				scheduler = factory.getScheduler();
				scheduler.start();
			} catch (SchedulerException e) {
				new CFWLog(logger).severe("Error occured while starting Quartz Scheduler: "+e.getMessage(), e);
			}
			
		}
		
		return scheduler;
	}
	
	/***********************************************************************
	 * Executes a job manually.
	 * Will write a message to inform user that the job was triggered.
	 ***********************************************************************/
	protected static void executeJobManually(String ID)  {
		
		executeJobManually(CFW.DB.Jobs.selectByID(ID));
	}
	

	/***********************************************************************
	 * Executes a job manually.
	 * Will write a message to inform user that the job was triggered.
	 ***********************************************************************/
	protected static void executeJobManually(CFWJob job)  {
		
		if(job == null) {
			new CFWLog(logger).warn("Job cannot be executed as it was null.");
			return;
		}
		
		if(!job.isEnabled()) {
			new CFWLog(logger).warn("Job is not enabled and cannot be executed.");
			return;
		}
		
		try {
			JobDetail jobDetail = job.createJobDetail();
			Trigger trigger = job.createJobTrigger(jobDetail);
			JobDataMap data = trigger.getJobDataMap();
			
			scheduler.triggerJob(job.createJobKey(), data);

		} catch (SchedulerException e) {
			new CFWLog(logger).severe("Error occured while manually executing Job: "+e.getMessage(), e);
			return;
		}
		
		CFW.Messages.addInfoMessage("Job triggered!");
	}

	/***********************************************************************
	 * Start the Job if:
	 *   - it is enabled
	 *   - the schedule is valid
	 *   - the trigger has remaining executions
	 *   
	 * @return false if exception occurs, false otherwise.
	 * 
	 ***********************************************************************/
	protected static boolean addJob(CFWJob job)  {
		
		if(job.isEnabled()) {
			
			CFWSchedule cfwSchedule = job.schedule();
			
			if(cfwSchedule.validate()){
				
				//-----------------------------------
				// Create JobDetail and Trigger
				JobDetail jobDetail = job.createJobDetail();
				Trigger trigger = job.createJobTrigger(jobDetail);
				
				//-----------------------------------
				// Ignore if no future executions 
				if(trigger.getFireTimeAfter(new Date()) == null) {
					return true;
				}
				
				//-----------------------------------
				// Schedule Job
				
				try {
					//getScheduler().scheduleJob(jobDetail, trigger);
					Scheduler scheduler = getScheduler();
					if( !scheduler.checkExists(jobDetail.getKey())) {
						scheduler.scheduleJob(jobDetail, trigger);
					}else {
						scheduler.addJob(jobDetail, true, true);
						if(scheduler.getTrigger(trigger.getKey()) == null) {
							scheduler.scheduleJob(trigger);
						}
					}
					
				} catch (SchedulerException e) {
					new CFWLog(logger).severe("Error occured while scheduling Quartz Job: "+e.getMessage(), e);
					return false;
				}
			}
		}
		
		return true;
		
	}
	
	/***********************************************************************
	 * Updates the job:
	 *   - it is enabled
	 *   - the schedule is valid
	 ***********************************************************************/
	protected static boolean updateJob(CFWJob job)  {
		
		try {
			Scheduler scheduler = getScheduler();
			JobKey key = job.createJobKey();

			if( !scheduler.checkExists(key) ){
				//------------------------------
				// Job was enabled
				return addJob(job);
				
			}else {

				
				if(job.isEnabled()) {
					//-----------------------------------
					// Restart Job
					removeJob(job);
					addJob(job);
					
				}else {
					//--------------------------
					// Job was disabled, remove
					removeJob(job);
				}
			}
			
			return true;
			
		} catch (SchedulerException e) {
			new CFWLog(logger).severe("Error occured while updating Quartz Job: "+e.getMessage(), e);
			return false;
		}
	}
	
	/***********************************************************************
	 * Updates the job:
	 *   - it is enabled
	 *   - the schedule is valid
	 ***********************************************************************/
	protected static boolean removeJob(CFWJob job)  {
		
		try {
			Scheduler scheduler = getScheduler();
			JobKey key = job.createJobKey();

			if( scheduler.checkExists(key) ){
				scheduler.deleteJob(key);
			}
			
			return true;
			
		} catch (SchedulerException e) {
			new CFWLog(logger).severe("Error occured while stopping Quartz Job: "+e.getMessage(), e);
			return false;
		}
	}
	
	
}
