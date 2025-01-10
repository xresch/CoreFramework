package com.xresch.cfw.features.jobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.UserAuditExecutor;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public class UserAuditExecutorJobTask implements UserAuditExecutor {

	@Override
	public String name() {
		return "Task Permissions";
	}
	
	@Override
	public String description() {
		return "Checks which tasks a user is allowed to select and define jobs for.";
	}
	
	@Override
	public JsonArray executeAudit(User user) {
		
		JsonArray result = new JsonArray(); 
		
		HashMap<String, Permission> permissions = CFW.DB.Users.selectPermissionsForUser(user);
		
		if(permissions.containsKey(FeatureJobs.PERMISSION_JOBS_ADMIN)) {
			JsonObject adminObject = new JsonObject();
			adminObject.addProperty("Message", "The user is Jobs Administrator and is allowed to create every task.");
			result.add(adminObject);
			return result;
		}
		
		//---------------------------------
		// Fetch Data
		ArrayList<CFWJobTask> tasksArray = CFW.Registry.Jobs.getAllTaskInstances();
		
		for(CFWJobTask current : tasksArray) {
			
			JsonObject widgetObject = new JsonObject();
			boolean hasPermission = current.createableFromUI() && current.hasPermission(user);
			widgetObject.addProperty("TASK", current.uniqueName());
			widgetObject.addProperty("HAS_PERMISSION", hasPermission);
			
			result.add(widgetObject);
		}
		
		return result;
	
	}
}
