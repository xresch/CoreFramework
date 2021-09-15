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

public class UserAuditExecutorAlertChannel implements UserAuditExecutor {

	@Override
	public String name() {
		return "Alert Channel Permissions";
	}
	
	@Override
	public String description() {
		return "Checks which alert channels a user is allowed to select for alertings.";
	}
	
	@Override
	public JsonArray executeAudit(User user) {
		
		JsonArray result = new JsonArray(); 
		
		HashMap<String, Permission> permissions = CFW.DB.Users.selectPermissionsForUser(user);
		
		if(permissions.containsKey(FeatureJobs.PERMISSION_JOBS_ADMIN)) {
			JsonObject adminObject = new JsonObject();
			adminObject.addProperty("Message", "The user is Jobs Administrator and is allowed to select every alert channel.");
			result.add(adminObject);
			return result;
		}
		
		//---------------------------------
		// Fetch Data
		ArrayList<CFWJobsAlertingChannel> channelArray = CFWJobsAlerting.getAllChannelInstances();
		
		for(CFWJobsAlertingChannel current : channelArray) {
			
			JsonObject widgetObject = new JsonObject();
			widgetObject.addProperty("CHANNEL", current.uniqueName());
			widgetObject.addProperty("HAS_PERMISSION", current.hasPermission(user));
			
			result.add(widgetObject);
		}
		
		return result;
	
	}
}
