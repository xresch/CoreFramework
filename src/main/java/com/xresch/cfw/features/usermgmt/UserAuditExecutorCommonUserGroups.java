package com.xresch.cfw.features.usermgmt;

import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.credentials.CFWCredentials;
import com.xresch.cfw.features.credentials.FeatureCredentials;
import com.xresch.cfw.features.spaces.FeatureSpaces;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.UserAuditExecutor;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024 
 * @license MIT-License
 **************************************************************************************************************/

public class UserAuditExecutorCommonUserGroups implements UserAuditExecutor {

	private CFWObject object = null;
	private String objectName = null;
	
	public UserAuditExecutorCommonUserGroups(CFWObject object, String objectName) {
		this.object = object;
		this.objectName = objectName;
	}
	
	@Override
	public String name() {
		return objectName + ": By Groups";
	}
	
	@Override
	public String description() {
		return "<p>Checks on which " + objectName + " the users has access by being part of a group. If you miss access:</p>"
				+"<ul>"
					+"<li><b>Space:</b>Check if you have access to the space, one of its sub spaces or the space is global.</li>"
					+"<li><b>Is Shared:</b>Check with the owner of the " + objectName +" if the credential is set to be shared.</li>"
				+"</ul>";
	}
	
	@Override
	public JsonArray executeAudit(User user) {
		
		//-----------------------------------
		// Check User is Shared/Editor
		String sqlFilter = CFW.Files.readPackageResource(FeatureUserManagement.PACKAGE_RESOURCE, "sql_permissionAuditByUsersGroups.sql");
		sqlFilter = sqlFilter.replace("{tablename}", object.getTableName() );
		
		return new CFWSQL(object)
			// .queryCache() // do not cache here
			.custom(sqlFilter, 
					user.id()
				)
			.and().append(FeatureSpaces.getSQLFilterInclusiveByUser(user.id()))
			.getAsJSONArray();

	}
}
