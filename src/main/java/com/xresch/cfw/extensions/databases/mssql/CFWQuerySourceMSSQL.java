package com.xresch.cfw.extensions.databases.mssql;

import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.extensions.databases.CFWQuerySourceDatabase;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.usermgmt.User;
	
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourceMSSQL extends CFWQuerySourceDatabase {


	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceMSSQL(CFWQuery parent) {
		super(parent, MSSQLEnvironment.SETTINGS_TYPE);
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public DBInterface getDatabaseInterface(int environmentID) {
		MSSQLEnvironment environment =
				MSSQLEnvironmentManagement.getEnvironment(environmentID);

		if(environment == null) { return null; }
		
		return environment.getDBInstance();
	
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public boolean isUpdateAllowed(int environmentID) {
		MSSQLEnvironment environment =
				MSSQLEnvironmentManagement.getEnvironment(environmentID);

		if(environment == null) { return false; }
		
		return environment.isUpdateAllowed();
	
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String getTimezone(int environmentID) {
		MSSQLEnvironment environment =
				MSSQLEnvironmentManagement.getEnvironment(environmentID);
		
		if(environment == null) { return null; }
		
		return environment.timezone();
	}

	/******************************************************************
	 *
	 ******************************************************************/
	public String uniqueName() {
		return "mssql";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Fetches data from a MSSQL database.";
	}
	
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionRequiredPermission() {
		return FeatureDBExtensionsMSSQL.PERMISSION_MSSQL;
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureDBExtensionsMSSQL.PERMISSION_MSSQL);
	}
	
}
