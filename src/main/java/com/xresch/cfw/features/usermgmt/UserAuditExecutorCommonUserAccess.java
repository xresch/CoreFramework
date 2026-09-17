package com.xresch.cfw.features.usermgmt;

import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.credentials.CFWCredentials;
import com.xresch.cfw.features.credentials.FeatureCredentials;
import com.xresch.cfw.features.spaces.CFWSpace;
import com.xresch.cfw.features.spaces.FeatureSpaces;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.UserAuditExecutor;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024 
 * @license MIT-License
 **************************************************************************************************************/

public class UserAuditExecutorCommonUserAccess implements UserAuditExecutor {

	private CFWObject object = null;
	private String objectName = null;
	private String foreignKeyName = null;
	
	public UserAuditExecutorCommonUserAccess(CFWObject object, String foreignKeyName, String objectName) {
		this.object = object;
		this.objectName = objectName;
		this.foreignKeyName = foreignKeyName;
	}
	
	@Override
	public String name() {
		return objectName + " Access";
	}
	
	@Override
	public String description() {
		return "<p>Checks on which " + objectName + " the users has direct access(not by being part of a group). If you miss access:</p>"
				+"<ul>"
					+"<li><b>Space:&nbsp;</b>Check if you have access to the space, one of its sub spaces or the space is global .</li>"
					+"<li><b>Is Shared:&nbsp;</b>Check with the owner of the " + objectName +" if the credential is set to be shared.</li>"
				+"</ul>";
	}
	
	@Override
	public JsonArray executeAudit(User user) {
		
		//-----------------------------------
		// Check User is Shared/Editor
		String likeID = "%\""+user.id()+"\":%";
		
		String sqlFilter = CFW.Files.readPackageResource(FeatureUserManagement.PACKAGE_RESOURCE, "sql_permissionAuditUserAccess.sql");
		sqlFilter = sqlFilter.replace("{tablename}", object.getTableName() );
		sqlFilter = sqlFilter.replace("{foreignKeyName}", foreignKeyName );
		
		//-----------------------------------
		// Check FK_ID_USER
		if(object.hasField("FK_ID_USER")) {
			sqlFilter = sqlFilter.replace("FK_ID_OWNER", "FK_ID_USER" );
		}
		
		//-----------------------------------
		// Check User is Shared/Editor
		int userID = user.id();

		return new CFWSQL(new CFWSpace())
			//.queryCache() // do not cache
			.custom(sqlFilter
					, userID
					, likeID
					, likeID
					, userID
					, userID
					, userID
					, userID
					, userID
					, userID
					, userID
					)
			.and().append(FeatureSpaces.getSQLFilterInclusiveByUser(user.id()))
			.getAsJSONArray();

	}
}
