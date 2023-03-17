package com.xresch.cfw.extensions.databases.mysql;

import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.extensions.databases.CFWQuerySourceDatabase;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.usermgmt.User;
	
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourceMySQL extends CFWQuerySourceDatabase {


	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceMySQL(CFWQuery parent) {
		super(parent, MySQLEnvironment.SETTINGS_TYPE);
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public DBInterface getDatabaseInterface(int environmentID) {
		MySQLEnvironment environment =
				MySQLEnvironmentManagement.getEnvironment(environmentID);
		
		if(environment == null) { return null; }
		
		return environment.getDBInstance();
	
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String getTimezone(int environmentID) {
		MySQLEnvironment environment =
				MySQLEnvironmentManagement.getEnvironment(environmentID);
		
		if(environment == null) { return null; }
		
		return environment.timezone();
	}

	/******************************************************************
	 *
	 ******************************************************************/
	public String uniqueName() {
		return "mysql";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Fetches data from a MySQL database.";
	}
		
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionRequiredPermission() {
		return FeatureDBExtensionsMySQL.PERMISSION_MYSQL;
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureDBExtensionsMySQL.PERMISSION_MYSQL);
	}
	
}
