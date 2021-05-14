package com.xresch.cfw.features.jobs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.xresch.cfw.logging.CFWLog;

public class RegistryJobs {

	private static Logger logger = CFWLog.getLogger(RegistryJobs.class.getName());
	
	// UniqueName and JobTask
	private static LinkedHashMap<String, Class<? extends JobTask>> executorMap = new LinkedHashMap<>();

	/*************************************************************************
	 * 
	 *************************************************************************/
	public static void registerJobTask(JobTask executor) {
		
		if( executorMap.containsKey(executor.uniqueName()) ) {
			new CFWLog(logger).severe("A JobTask with the name '"+executor.uniqueName()+"' has already been registered. Please change the name or prevent multiple registration attempts.");
			return;
		}
		
		executorMap.put(executor.uniqueName(), executor.getClass());
		
	}
	
	/***********************************************************************
	 * Get a list of all executor instances.
	 * 
	 ***********************************************************************/
	private static ArrayList<JobTask> getAllTaskInstances()  {
		ArrayList<JobTask> instanceArray = new ArrayList<>();
		
		for(Class<? extends JobTask> clazz : executorMap.values()) {
			try {
				JobTask instance = clazz.newInstance();
				instanceArray.add(instance);
			} catch (Exception e) {
				new CFWLog(logger).severe("Issue creating instance for Class '"+clazz.getName()+"': "+e.getMessage(), e);
			}
		}
		return instanceArray;
	}
	
	/***********************************************************************
	 * Get a new instance for the specified task.
	 * Returns null if the  is undefined.
	 ***********************************************************************/
	public static JobTask createTaskInstance(String uniqueName)  {
		
		JobTask instance = null;
		Class<? extends JobTask> clazz =  executorMap.get(uniqueName);
		try {
			if(clazz != null) {
				instance = clazz.newInstance();
			}
		} catch (Exception e) {
			new CFWLog(logger).severe("Issue creating instance for Class '"+clazz.getName()+"': "+e.getMessage(), e);
		}
		
		return instance;
	}
}
