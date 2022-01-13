package com.xresch.cfw.features.query;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.features.query.commands.CFWQueryCommandSource;
import com.xresch.cfw.features.query.sources.CFWQuerySourceRandom;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.spi.CFWAppFeature;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureQuery extends CFWAppFeature {
	
	private static final String URI_QUERY = "/app/query";
	public static final String RESOURCE_PACKAGE = "com.xresch.cfw.features.query.resources";
	public static final String PERMISSION_QUERY_USER = "Query: User";
	public static final String PERMISSION_QUERY_ADMIN = "Query: Admin";
	
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		
		//----------------------------------
		// Register Objects
		//CFW.Registry.Objects.addCFWObject(CFWJob.class);
    	
		//----------------------------------
		// Register Commands
		CFW.Registry.Query.registerCommand(new CFWQueryCommandSource(null));
		
		
		//----------------------------------
		// Register Sources
		CFW.Registry.Query.registerSource(new CFWQuerySourceRandom(null));
		
		//----------------------------------
		// Register Job Tasks
		//CFW.Registry.Jobs.registerTask(new CFWJobTaskSendMail());

		
		//----------------------------------
    	// Register Menu				
//		CFW.Registry.Components.addToolsMenuItem(
//				(MenuItem)new MenuItem("Query")
//					.faicon("fas fa-play-circle")
//					.addPermission(PERMISSION_QUERY_USER)
//					.addPermission(PERMISSION_QUERY_ADMIN)
//					.href("/app/jobs")
//				, null);
		
	}

	@Override
	public void initializeDB() {
		
		//----------------------------------
    	// Permissions
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_QUERY_USER, FeatureUserManagement.CATEGORY_USER)
					.description("User can view and edit his own queries."),
					true,
					false
			);
		
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_QUERY_ADMIN, FeatureUserManagement.CATEGORY_USER)
					.description("User can view and edit all queries in the system."),
					true,
					false
			);
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		
		//app.addAppServlet(ServletJobs.class,  URI_QUERY);
		
	}

	@Override
	public void startTasks() {
		//do nothing
	}

	@Override
	public void stopFeature() {
		// do nothing
	}
	
	public static String getQueryURI() {
		return URI_QUERY;
	}

}
