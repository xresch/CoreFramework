package com.xresch.cfw.features.jobs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.xresch.cfw.logging.CFWLog;

public class CFWJobRegistry {

	private static Logger logger = CFWLog.getLogger(CFWJobRegistry.class.getName());
	
	// UniqueName and JobTask
	private static LinkedHashMap<String, Class<? extends CFWJobTask>> jobtasksMap = new LinkedHashMap<>();

	/*************************************************************************
	 * 
	 *************************************************************************/
	public static void registerJobTask(CFWJobTask jobtasks) {
		
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
	 * Returns null if the  is undefined.
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
}
