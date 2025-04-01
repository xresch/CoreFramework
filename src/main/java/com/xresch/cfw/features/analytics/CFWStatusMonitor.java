package com.xresch.cfw.features.analytics;

import java.util.HashMap;

import com.google.gson.JsonObject;
import com.xresch.cfw.utils.CFWState.CFWStateOption;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2025
 * @license MIT-License
 **************************************************************************************************************/
public interface CFWStatusMonitor {
	
	/******************************************************************
	 * Return the category of this monitor.
	 * This will be used to group the monitors in the UI.
	 * The category will be used as the label for each group.
	 * 
	 * @return String category
	 ******************************************************************/
	public String category();
	
	
	/******************************************************************
	 * Return the unique name of this monitor.
	 * This will be used to identify this monitor 
	 * 
	 * @return String name
	 ******************************************************************/
	public String uniqueName();
	
	/******************************************************************
	 * Return the status of this monitor.
	 * The JsonObject key contains details of the monitor, the value is 
	 * the status of the monitor.
	 * The JsonObject should contain a field "name", which will be used
	 * for displaying the status.
	 * The JsonObject should NOT contain the fields "monitor", "category", and "status" 
	 * as this will be added by the framework using:
	 * 	- "monitor": uniqueName()
	 * 	- "category": category()
	 * 	- "status": status returned by this method.
	 * 
	 * You can return multiple statuses with a single monitor.
	 * 
	 * @return HasMap
	 ******************************************************************/
	public HashMap<JsonObject, CFWStateOption> getStatuses();

}
