package com.xresch.cfw.features.query.store;

import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.UserAuditExecutor;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024 
 * @license MIT-License
 **************************************************************************************************************/
public class UserAuditExecutorStoredQueryUserGroups implements UserAuditExecutor {

	@Override
	public String name() {
		return "StoredQuery(By Groups)";
	}
	
	@Override
	public String description() {
		return "Checks on which storedQuery the users has access by being part of a group.";
	}
	
	@Override
	public JsonArray executeAudit(User user) {
		
		//---------------------------------
		// Fetch Data
		return CFW.DB.StoredQuery.permissionAuditByUsersGroups(user);
	
	}
}
