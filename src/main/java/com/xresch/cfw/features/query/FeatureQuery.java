package com.xresch.cfw.features.query;

import java.util.TreeMap;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.query.commands.CFWQueryCommandDistinct;
import com.xresch.cfw.features.query.commands.CFWQueryCommandSource;
import com.xresch.cfw.features.query.sources.CFWQuerySourceJson;
import com.xresch.cfw.features.query.sources.CFWQuerySourceRandom;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.response.bootstrap.MenuItem;
import com.xresch.cfw.spi.CFWAppFeature;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureQuery extends CFWAppFeature {
	
	private static final String URI_QUERY = "/app/query";
	public static final String PACKAGE_RESOURCES = "com.xresch.cfw.features.query.resources";
	public static final String PACKAGE_MANUAL = "com.xresch.cfw.features.query.manual";
	public static final String PERMISSION_QUERY_USER = "Query: User";
	public static final String PERMISSION_QUERY_ADMIN = "Query: Admin";
	
	public static final ManualPage ROOT_MANUAL_PAGE = CFW.Registry.Manual.addManualPage(null, 
			new ManualPage("Query")
				.faicon("fas fa-terminal")
				.addPermission(PERMISSION_QUERY_USER)
				.addPermission(PERMISSION_QUERY_ADMIN)
		);
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCES);
		
		//----------------------------------
		// Register Objects
		//CFW.Registry.Objects.addCFWObject(CFWJob.class);
    	
		//----------------------------------
		// Register Commands
		CFW.Registry.Query.registerCommand(new CFWQueryCommandSource(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandDistinct(null));
		
		
		//----------------------------------
		// Register Sources
		CFW.Registry.Query.registerSource(new CFWQuerySourceJson(null));
		CFW.Registry.Query.registerSource(new CFWQuerySourceRandom(null));
		
		//----------------------------------
		// Register Job Tasks
		//CFW.Registry.Jobs.registerTask(new CFWJobTaskSendMail());

		
		//----------------------------------
    	// Register Menu				
		CFW.Registry.Components.addToolsMenuItem(
				(MenuItem)new MenuItem("Query")
					.faicon("fas fa-terminal")
					.addPermission(PERMISSION_QUERY_USER)
					.addPermission(PERMISSION_QUERY_ADMIN)
					.href("/app/query")
				, null);
		
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
		
		app.addAppServlet(ServletQuery.class,  URI_QUERY);
		
		//-----------------------------------------------
    	// Register Manual: Done here after all Sources, 
		// Commands etc... are registered.
		registerManual();
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
	
	
	
	/******************************************************************
	 *
	 ******************************************************************/
	public void registerManual() {
			
		CFWQuery pseudoQuery = new CFWQuery();
		
		//----------------------------------
		// Source Main Page
		ManualPage sourcePage = new ManualPage("Sources")
				.faicon("fas fa-star")
				.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_01_sources.html");
		
		ROOT_MANUAL_PAGE.addChild(sourcePage);
		
		//----------------------------------
		// Pages for each Source
		TreeMap<String, Class<? extends CFWQuerySource>> sourcelist = CFW.Registry.Query.getSourceList();
		
		
		for(String sourceName : sourcelist.keySet()) {
			
			CFWQuerySource current = CFW.Registry.Query.createSourceInstance(pseudoQuery, sourceName);
			
			CFWQueryManualPageSource page = new CFWQueryManualPageSource(sourceName, current);
			
			sourcePage.addChild(page);
			
		}
		
		//----------------------------------
		// Commands Main Page
		ManualPage commandsPage = new ManualPage("Commands")
				.faicon("fas fa-star")
				.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_02_commands.html");
		
		ROOT_MANUAL_PAGE.addChild(commandsPage);
		
		//----------------------------------
		// Pages for each Command
		TreeMap<String, Class<? extends CFWQueryCommand>> commandlist = CFW.Registry.Query.getCommandList();
		
		
		for(String commandName : commandlist.keySet()) {
			
			CFWQueryCommand current = CFW.Registry.Query.createCommandInstance(pseudoQuery, commandName);
			
			new CFWQueryManualPageCommand(commandsPage, commandName, current);
						
		}
		


				
	}

}
