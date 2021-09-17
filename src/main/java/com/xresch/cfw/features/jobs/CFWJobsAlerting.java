package com.xresch.cfw.features.jobs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;

public class CFWJobsAlerting {

	private static Logger logger = CFWLog.getLogger(CFWJobsAlerting.class.getName());
	
	// UniqueName and JobTask
	private static LinkedHashMap<String, Class<? extends CFWJobsAlertingChannel>> channelMap = new LinkedHashMap<>();

	private static ArrayList<CFWJobsAlertingChannel> instanceArray;

	/*************************************************************************
	 * 
	 *************************************************************************/
	public static void registerChannel(CFWJobsAlertingChannel channel) {
		
		if( channelMap.containsKey(channel.uniqueName()) ) {
			new CFWLog(logger).severe("An alert channel with the name '"+channel.uniqueName()+"' has already been registered. Please change the name or prevent multiple registration attempts.");
			return;
		}
		
		channelMap.put(channel.uniqueName(), channel.getClass());
		instanceArray = null;
	}
	
	/***********************************************************************
	 * Get a list of all task names that the current user has access to.
	 * 
	 ***********************************************************************/
	public static Set<String> getChannelNamesForUI()  {
		Set<String> channelNames = new HashSet<>();
		
		User user = CFW.Context.Request.getUser();
		
		//only return for UI, ignore if run by job
		if(user != null) {
			for(CFWJobsAlertingChannel channel : getAllChannelInstances()) {
	
				if(channel.hasPermission(CFW.Context.Request.getUser())
				|| CFW.Context.Request.hasPermission(FeatureJobs.PERMISSION_JOBS_ADMIN)	
				) {
					channelNames.add(channel.uniqueName());
				}
			}
		}
		return channelNames;
	}
	
	/***********************************************************************
	 * Get a list of all executor instances.
	 * 
	 ***********************************************************************/
	public static ArrayList<CFWJobsAlertingChannel> getAllChannelInstances()  {
		if(instanceArray != null) {
			return instanceArray;
		}
		
		instanceArray = new ArrayList<>();
		
		for(Class<? extends CFWJobsAlertingChannel> clazz : channelMap.values()) {
			try {
				CFWJobsAlertingChannel instance = clazz.newInstance();
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
	public static CFWJobsAlertingChannel createChannelInstance(String uniqueName)  {
		
		CFWJobsAlertingChannel instance = null;
		Class<? extends CFWJobsAlertingChannel> clazz =  channelMap.get(uniqueName);
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
	public static Class<? extends CFWJobsAlertingChannel> getChannelClass(String uniqueName)  {
		
		return channelMap.get(uniqueName);
	}
}
