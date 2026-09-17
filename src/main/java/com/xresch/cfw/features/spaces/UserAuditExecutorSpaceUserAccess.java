package com.xresch.cfw.features.spaces;

import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.UserAuditExecutor;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024 
 * @license MIT-License
 **************************************************************************************************************/

public class UserAuditExecutorSpaceUserAccess implements UserAuditExecutor {

	@Override
	public String name() {
		return "Space Access";
	}
	
	@Override
	public String description() {
		return "Checks on which Spaces the user has been granted access to. Does not list any parent spaces who's entities he can access because of inheritance.";
	}
	
	@Override
	public JsonArray executeAudit(User user) {
		
		//---------------------------------
		// Fetch Data
		return CFW.DB.Spaces.permissionAuditByUser(user);
	
	}
}
