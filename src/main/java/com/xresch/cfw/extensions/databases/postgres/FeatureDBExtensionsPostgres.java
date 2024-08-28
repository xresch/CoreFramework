package com.xresch.cfw.extensions.databases.postgres;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.spi.CFWAppFeature;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureDBExtensionsPostgres extends CFWAppFeature {
	
	public static final String PACKAGE_RESOURCE = "com.xresch.cfw.extensions.databases.postgres.resources";
	
	public static final String PERMISSION_POSTGRES = "Database: Postgres";
	
	/************************************************************************************
	 * Override to make it managed and return something else then null.
	 ************************************************************************************/
	@Override
	public String getNameForFeatureManagement() {
		return "DB Extensions: Postgres";
	};
	
	/************************************************************************************
	 * Register a description for the feature management.
	 ************************************************************************************/
	@Override
	public String getDescriptionForFeatureManagement() {
		return "Use Postgres database extensions.(Dashboard Widgets, Query Source, Tasks ...)";
	};
	
	/************************************************************************************
	 * Return if the feature is active by default or if the admin has to enable it.
	 ************************************************************************************/
	public boolean activeByDefault() {
		return true;
	};
	
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public void register() {
		//----------------------------------
		// Register Settings
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCE);
		
		//----------------------------------
		// Register Context Settings
		CFW.Registry.ContextSettings.register(PostgresEnvironment.SETTINGS_TYPE, PostgresEnvironment.class);
		
		//----------------------------------
		// Register Widgets
		CFW.Registry.Widgets.add(new WidgetPostgresQueryChart());
		CFW.Registry.Widgets.add(new WidgetPostgresQueryStatus());
		
		//----------------------------------
		// Register Widget Parameters
		CFW.Registry.Parameters.add(new ParameterDefinitionPostgresEnvironment());
		
		//----------------------------------
		// Register Query Source
		CFW.Registry.Query.registerSource(new CFWQuerySourcePostgres(null));
		
		//----------------------------------
		// Register Job Task
		CFW.Registry.Jobs.registerTask(new CFWJobTaskPostgresQueryStatus());
		
	}

	@Override
	public void initializeDB() {
		//----------------------------------
		// Permissions
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_POSTGRES, FeatureUserManagement.CATEGORY_USER)
					.description("Create and Edit Postgres Widgets."),
				true,
				true);
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		PostgresEnvironmentManagement.initialize();
	}

	@Override
	public void startTasks() {
		/* do nothing */
	}

	@Override
	public void stopFeature() {
		/* do nothing */
	}

}
