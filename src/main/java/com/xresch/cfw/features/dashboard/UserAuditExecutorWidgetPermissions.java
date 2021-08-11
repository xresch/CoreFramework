package com.xresch.cfw.features.dashboard;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.UserAuditExecutor;

public class UserAuditExecutorWidgetPermissions implements UserAuditExecutor {

	@Override
	public String name() {
		return "Widget Permissions";
	}
	
	@Override
	public String description() {
		return "Checks which Dashboard Widgets the user can create and edit.";
	}
	
	@Override
	public JsonArray executeAudit(User user) {
		
		JsonArray result = new JsonArray(); 
		
		HashMap<String, Permission> permissions = CFW.DB.Users.selectPermissionsForUser(user);
		
		if(permissions.containsKey(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
			JsonObject adminObject = new JsonObject();
			adminObject.addProperty("Message", "The user is Dashboard Administrator and has access to every widget.");
			result.add(adminObject);
			return result;
		}
		
		//---------------------------------
		// Fetch Data
		LinkedHashMap<String, WidgetDefinition> definitions = CFW.Registry.Widgets.getWidgetDefinitions();
		
		for(WidgetDefinition current : definitions.values()) {
			JsonObject widgetObject = new JsonObject();
			widgetObject.addProperty("TYPE", current.getWidgetType());
			widgetObject.addProperty("HAS_PERMISSION", current.hasPermission(user));
			
			result.add(widgetObject);
		}
		
		return result;
	
	}
}
