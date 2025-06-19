package com.xresch.cfw.features.analytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.utils.CFWState;
import com.xresch.cfw.utils.CFWState.CFWStateOption;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2025
 *@license PESA License
 **************************************************************************************************************/
public class CFWStatusMonitorRegistry {
	
	private static final Logger logger = CFWLog.getLogger(CFWStatusMonitorRegistry.class.getName());
	
	// monitor name and instance
	private static TreeMap<String,  CFWStatusMonitor> statusMonitorMap = new TreeMap<>();

	private static JsonObject cachedStatusList;
	private static CFWStateOption cachedWorstStatus = CFWStateOption.NONE;

	/***********************************************************************
	 * Adds a CFWStatusMonitor class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static void registerStatusMonitor(CFWStatusMonitor monitor)  {
		
		String lowercaseName = monitor.uniqueName().trim().toLowerCase();
		
		if( statusMonitorMap.containsKey(lowercaseName) ) {
			new CFWLog(logger).severe("A example with the name '"+monitor.uniqueName()+"' has already been registered. Please change the name or prevent multiple registration attempts.");
			return;
		}
		
		statusMonitorMap.put(lowercaseName, monitor);
		
	}
	
	/***********************************************************************
	 * Removes a CFWStatusMonitor class to the registry.
	 * @param objectClass
	 ***********************************************************************/
	public static void removeStatusMonitor(String statusMonitorName)  {
		statusMonitorMap.remove(statusMonitorName.trim().toLowerCase());
	}
	
	/***********************************************************************
	 * Returns a clone of the map of example names and example classes.
	 * @param objectClass
	 ***********************************************************************/
	public static TreeMap<String, CFWStatusMonitor> getStatusMonitorList()  {
		TreeMap<String, CFWStatusMonitor> clone = new TreeMap<>();
		
		clone.putAll(statusMonitorMap);
		return clone;
	}
	
	
	/***********************************************************************
	 * Returns the specified monitor.
	 * Returns null if the  is undefined.
	 ***********************************************************************/
	public static CFWStatusMonitor getStatusMonitor(String statusMonitorName)  {

		return statusMonitorMap.get(statusMonitorName);
		
	}
	
	
	/***********************************************************************
	 * Get a list of all registered QueryExamples.
	 * 
	 ***********************************************************************/
	public static ArrayList<String> getStatusMonitorNames()  {
		ArrayList<String> Array = new ArrayList<String>();
		Array.addAll(statusMonitorMap.keySet());
		
		return Array;
	}
	
	/***********************************************************************
	 * Return true if a example with the specified name is registered, false
	 * otherwise.
	 * 
	 ***********************************************************************/
	public static boolean statusMonitorExists(String statusMonitorName)  {
		return statusMonitorMap.containsKey(statusMonitorName.trim().toLowerCase());
	}
	
	/***********************************************************************
	 * Returns the current worst status
	 * 
	 ***********************************************************************/
	public static CFWStateOption getWorstStatus()  {
		boolean colorize = CFW.DB.Config.getConfigAsBoolean(FeatureSystemAnalytics.CATEGORY_STATUS_MONITOR, FeatureSystemAnalytics.CONFIG_INTERVAL_MINUTES);
		
		if(!colorize) { return CFWStateOption.NONE; }
		
		return cachedWorstStatus;
	}
	/***********************************************************************
	 * Returns the list of statuses with their categories.
	 * { "categoryA": 
	 * 		[ 	{statusObject}
	 * 		  , {statusObject}
	 * 		  , ...
	 * 		]
	 * , "categoryB": ...
	 * }
	 * 
	 ***********************************************************************/
	public static JsonObject getStatusListCategorized()  {
		return cachedStatusList;
	}
	
	/***********************************************************************
	 * INTERNAL: Use the method getStatusListCategorized();
	 * 
	 * Returns the list of statuses with their categories.
	 * {
	 * worstStatus: "ORANGE",
	 * categories:
	 * 		{ "categoryA": 
	 * 			[ 	{statusObject}
	 * 			  , {statusObject}
	 * 			  , ...
	 * 			]
	 * 		, "categoryB": ...
	 * 		}
	 * }
	 * 
	 ***********************************************************************/
	protected static void loadStatusListAndCache()  {
		
		JsonObject result = new JsonObject();
		TreeMap<String, JsonArray> categoryMap = new TreeMap<>();
		
		CFWStateOption worstStatus = CFWStateOption.NONE;
		for(CFWStatusMonitor monitor : statusMonitorMap.values()) {
			
			
			//------------------------
			// Get Array for Category
			String category = monitor.category();
			
			if( !categoryMap.containsKey(category)) {
				categoryMap.put(category, new JsonArray());
			}
			
			JsonArray categoryArray = categoryMap.get(category);
		
			try {	
				//------------------------
				// Get Array for Category
				HashMap<JsonObject, CFWStateOption> statuses = monitor.getStatuses();
				if(statuses != null) {
					for(Entry<JsonObject, CFWStateOption> status : statuses.entrySet()) {
						
						JsonObject object = status.getKey();
						CFWStateOption state = status.getValue();
						
						object.addProperty("category", monitor.category() );
						object.addProperty("monitor", monitor.uniqueName() );
						object.addProperty("status", state.toString() );
						
						categoryArray.add(object);
						
						worstStatus = CFWState.compareAndGetMoreDangerous(worstStatus, state);
					};
				}
			}catch(Exception e){
				JsonObject object = new JsonObject();
				
				object.addProperty("category", monitor.category() );
				object.addProperty("monitor", monitor.uniqueName() );
				object.addProperty("status", CFWStateOption.RED.toString() );
				object.addProperty("error", e.getMessage() );
				
				categoryArray.add(object);
				
				new CFWLog(logger)
					.silent(true)
					.warn("Error occured in Status Monitor '"+monitor.uniqueName()+"' with message: "+e.getMessage(), e);
			}

		}
		
		cachedWorstStatus = worstStatus;
		
		result.addProperty("worstStatus", worstStatus.toString());
		result.addProperty("time", System.currentTimeMillis());
		result.add("categories", CFW.JSON.toJSONElement(categoryMap).getAsJsonObject() );
		
		cachedStatusList = result;
		
	}
		
}
