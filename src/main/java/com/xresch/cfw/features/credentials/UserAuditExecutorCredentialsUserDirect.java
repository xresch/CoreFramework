package com.xresch.cfw.features.credentials;

import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.UserAuditExecutor;

public class UserAuditExecutorCredentialsUserDirect implements UserAuditExecutor {

	@Override
	public String name() {
		return "Credentials(Direct)";
	}
	
	@Override
	public String description() {
		return "Checks on which credentialss the users has direct access(not by being part of a group).";
	}
	
	@Override
	public JsonArray executeAudit(User user) {
		
		//---------------------------------
		// Fetch Data
		return CFW.DB.Credentials.permissionAuditByUser(user);
	
	}
}
