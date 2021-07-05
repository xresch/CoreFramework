package com.xresch.cfw.features.usermgmt;

import com.google.gson.JsonArray;

public interface UserAuditExecutor {

	/***************************************************
	 * Return the name for the section
	 * (e.g. Dashboards, Permissions etc...)
	 * 
	 ***************************************************/
	public abstract String name();
	
	/***************************************************
	 * A description of what this audit checks.
	 * 
	 ***************************************************/
	public abstract String description();
	
	/***************************************************
	 * execute the audit and return a JsonArray containing
	 * JsonObjects. JsonObjects must all have the same 
	 * fields.
	 ***************************************************/
	public abstract JsonArray executeAudit(User user);
}
