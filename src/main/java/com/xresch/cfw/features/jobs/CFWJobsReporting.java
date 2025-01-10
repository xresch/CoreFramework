package com.xresch.cfw.features.jobs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public class CFWJobsReporting {

	private static Logger logger = CFWLog.getLogger(CFWJobsReporting.class.getName());
	
	// UniqueName and JobTask
	private static LinkedHashMap<String, Class<? extends CFWJobsReportingChannel>> channelMap = new LinkedHashMap<>();

	private static ArrayList<CFWJobsReportingChannel> instanceArray;

	/*************************************************************************
	 * 
	 *************************************************************************/
	public static void registerChannel(CFWJobsReportingChannel channel) {
		
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
			for(CFWJobsReportingChannel channel : getAllChannelInstances()) {
	
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
	public static ArrayList<CFWJobsReportingChannel> getAllChannelInstances()  {
		if(instanceArray != null) {
			return instanceArray;
		}
		
		instanceArray = new ArrayList<>();
		
		for(Class<? extends CFWJobsReportingChannel> clazz : channelMap.values()) {
			try {
				CFWJobsReportingChannel instance = clazz.newInstance();
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
	public static CFWJobsReportingChannel createChannelInstance(String uniqueName)  {
		
		CFWJobsReportingChannel instance = null;
		Class<? extends CFWJobsReportingChannel> clazz =  channelMap.get(uniqueName);
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
	public static Class<? extends CFWJobsReportingChannel> getChannelClass(String uniqueName)  {
		
		return channelMap.get(uniqueName);
	}
}
