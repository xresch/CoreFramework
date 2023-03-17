package com.xresch.cfw.extensions.databases.generic;

import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.extensions.databases.CFWQuerySourceDatabase;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.usermgmt.User;
	
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourceGenericJDBC extends CFWQuerySourceDatabase {

	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceGenericJDBC(CFWQuery parent) {
		super(parent, GenericJDBCEnvironment.SETTINGS_TYPE);
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public DBInterface getDatabaseInterface(int environmentID) {
		GenericJDBCEnvironment environment =
				GenericJDBCEnvironmentManagement.getEnvironment(environmentID);

		if(environment == null) { return null; }
		
		return environment.getDBInstance();
	
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String getTimezone(int environmentID) {
		GenericJDBCEnvironment environment =
				GenericJDBCEnvironmentManagement.getEnvironment(environmentID);
		
		if(environment == null) { return null; }
		
		return environment.timezone();
	}

	/******************************************************************
	 *
	 ******************************************************************/
	public String uniqueName() {
		return "jdbc";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Fetches data from a JDBC database.";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionRequiredPermission() {
		return FeatureDBExtensionsGenericJDBC.PERMISSION_GENERICJDBC;
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureDBExtensionsGenericJDBC.PERMISSION_GENERICJDBC);
	}
	
}
