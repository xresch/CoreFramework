package com.xresch.cfw.features.usermgmt;

import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;

public class UserAuditExecutorPermissions implements UserAuditExecutor {

	@Override
	public String name() {
		return "Permissions";
	}
	
	@Override
	public String description() {
		return "Lists the users permission and from which role or group he got the permission.";
	}
	@Override
	public JsonArray executeAudit(User user) {
		
		//---------------------------------
		// Fetch Data
		return CFW.DB.RolePermissionMap.getPermissionOverview(user);
		

		
		
	}

}
