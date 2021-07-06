package com.xresch.cfw.features.dashboard;

import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.UserAuditExecutor;

public class UserAuditExecutorDashboardUserGroups implements UserAuditExecutor {

	@Override
	public String name() {
		return "Dashboard(By Groups)";
	}
	
	@Override
	public String description() {
		return "Checks on which dashboards the users has access by being part of a group.";
	}
	
	@Override
	public JsonArray executeAudit(User user) {
		
		//---------------------------------
		// Fetch Data
		return CFW.DB.Dashboards.permissionAuditByUsersGroups(user);
	
	}
}
