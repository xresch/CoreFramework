package com.xresch.cfw.features.dashboard;

import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.UserAuditExecutor;

public class UserAuditExecutorDashboardUser implements UserAuditExecutor {

	@Override
	public String name() {
		return "Dashboard(Direct)";
	}
	
	@Override
	public String description() {
		return "Checks on which dashboards the users has direct access(not by being part of a group).";
	}
	
	@Override
	public JsonArray executeAudit(User user) {
		
		//---------------------------------
		// Fetch Data
		return CFW.DB.Dashboards.permissionAuditByUser(user);
	
	}
}
