package com.xresch.cfw.features.contextsettings;

import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.UserAuditExecutor;

public class UserAuditExecutorContextSettings implements UserAuditExecutor {

	@Override
	public String name() {
		return "Context Settings";
	}
	
	@Override
	public String description() {
		return "Checks to which context settings the users has access to.";
	}
	
	@Override
	public JsonArray executeAudit(User user) {
		
		//---------------------------------
		// Fetch Data
		return CFW.DB.ContextSettings.auditAccessToContextSettings(user);
	
	}
}
