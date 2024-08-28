package com.xresch.cfw.extensions.databases.postgres;

import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.extensions.databases.CFWQuerySourceDatabase;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.usermgmt.User;
	
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourcePostgres extends CFWQuerySourceDatabase {


	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourcePostgres(CFWQuery parent) {
		super(parent, PostgresEnvironment.SETTINGS_TYPE);
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public DBInterface getDatabaseInterface(int environmentID) {
		PostgresEnvironment environment =
				PostgresEnvironmentManagement.getEnvironment(environmentID);
		
		if(environment == null) { return null; }
		
		return environment.getDBInstance();
	
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public boolean isUpdateAllowed(int environmentID) {
		PostgresEnvironment environment =
				PostgresEnvironmentManagement.getEnvironment(environmentID);

		if(environment == null) { return false; }
		
		return environment.isUpdateAllowed();
	
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String getTimezone(int environmentID) {
		PostgresEnvironment environment =
				PostgresEnvironmentManagement.getEnvironment(environmentID);
		
		if(environment == null) { return null; }
		
		return environment.timezone();
	}

	/******************************************************************
	 *
	 ******************************************************************/
	public String uniqueName() {
		return "postgres";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Fetches data from a Postgres database.";
	}
		
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionRequiredPermission() {
		return FeatureDBExtensionsPostgres.PERMISSION_POSTGRES;
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureDBExtensionsPostgres.PERMISSION_POSTGRES);
	}
	
}
