package com.xresch.cfw.features.usermgmt;

import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;

public class UserAuditExecutorGroups implements UserAuditExecutor {

	@Override
	public String name() {
		return "Groups";
	}
	
	@Override
	public String description() {
		return "Lists the groups the user is part of.";
	}
	@Override
	public JsonArray executeAudit(User user) {
		
		//---------------------------------
		// Fetch Data
		return CFW.DB.UserRoleMap.getGroupsForUser(user);
		

		
		
	}

}
