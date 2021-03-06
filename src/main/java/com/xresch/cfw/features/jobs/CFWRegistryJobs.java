package com.xresch.cfw.features.jobs;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWSchedule;
import com.xresch.cfw.logging.CFWLog;

public class CFWRegistryJobs {

	private static Logger logger = CFWLog.getLogger(CFWRegistryJobs.class.getName());
	
	private static Scheduler scheduler = null;
	
	// UniqueName and JobTask
	private static LinkedHashMap<String, Class<? extends CFWJobTask>> jobtasksMap = new LinkedHashMap<>();

	/*************************************************************************
	 * 
	 *************************************************************************/
	public static void registerTask(CFWJobTask jobtasks) {
		
		if( jobtasksMap.containsKey(jobtasks.uniqueName()) ) {
			new CFWLog(logger).severe("A JobTask with the name '"+jobtasks.uniqueName()+"' has already been registered. Please change the name or prevent multiple registration attempts.");
			return;
		}
		
		jobtasksMap.put(jobtasks.uniqueName(), jobtasks.getClass());
		
	}
	
	
	/***********************************************************************
	 * Get a list of all executor instances.
	 * 
	 ***********************************************************************/
	public static Set<String> getTaskNames()  {
		return jobtasksMap.keySet();
	}
	
	/***********************************************************************
	 * Get a list of all executor instances.
	 * 
	 ***********************************************************************/
	private static ArrayList<CFWJobTask> getAllTaskInstances()  {
		ArrayList<CFWJobTask> instanceArray = new ArrayList<>();
		
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
