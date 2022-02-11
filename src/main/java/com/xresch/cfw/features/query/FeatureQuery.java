package com.xresch.cfw.features.query;

import java.util.TreeMap;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.query.commands.CFWQueryCommandComment;
import com.xresch.cfw.features.query.commands.CFWQueryCommandDisplay;
import com.xresch.cfw.features.query.commands.CFWQueryCommandDistinct;
import com.xresch.cfw.features.query.commands.CFWQueryCommandFormatField;
import com.xresch.cfw.features.query.commands.CFWQueryCommandKeep;
import com.xresch.cfw.features.query.commands.CFWQueryCommandMetadata;
import com.xresch.cfw.features.query.commands.CFWQueryCommandRemove;
import com.xresch.cfw.features.query.commands.CFWQueryCommandRename;
import com.xresch.cfw.features.query.commands.CFWQueryCommandSource;
import com.xresch.cfw.features.query.commands.CFWQueryCommandTail;
import com.xresch.cfw.features.query.commands.CFWQueryCommandTop;
import com.xresch.cfw.features.query.sources.CFWQuerySourceApplog;
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
	public static final String PACKAGE_MANUAL =    "com.xresch.cfw.features.query.manual";
	public static final String PERMISSION_QUERY_USER = "Query: User";
	public static final String PERMISSION_QUERY_ADMIN = "Query: Admin";
	
	public static final String CONFIG_FETCH_LIMIT_DEFAULT = "Fetch Limit Default";
	public static final String CONFIG_FETCH_LIMIT_MAX = "Fetch Limit Max";
	public static final String CONFIG_QUERY_RECORD_LIMIT = "Query Record Limit";
	public static final String CONFIG_QUERY_COMMAND_LIMIT = "Query Command Limit";
	
	public static final ManualPage ROOT_MANUAL_PAGE = CFW.Registry.Manual.addManualPage(null, 
			new ManualPage("Query")
				.faicon("fas fa-terminal")
				.addPermission(PERMISSION_QUERY_USER)
				.addPermission(PERMISSION_QUERY_ADMIN)
				.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "000_query.html"))
				;
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCES);
		CFW.Files.addAllowedPackage(PACKAGE_MANUAL);
		
		//----------------------------------
		// Register Objects
		//CFW.Registry.Objects.addCFWObject(CFWJob.class);
    	
		//----------------------------------
		// Register Commands
		CFW.Registry.Query.registerCommand(new CFWQueryCommandSource(null));
		
		CFW.Registry.Query.registerCommand(new CFWQueryCommandComment(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandDisplay(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandDistinct(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandFormatField(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandKeep(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandMetadata(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandRemove(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandRename(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandTail(null));
		CFW.Registry.Query.registerCommand(new CFWQueryCommandTop(null));
		
		
		//----------------------------------
		// Register Sources
		CFW.Registry.Query.registerSource(new CFWQuerySourceJson(null));
		CFW.Registry.Query.registerSource(new CFWQuerySourceRandom(null));
		CFW.Registry.Query.registerSource(new CFWQuerySourceApplog(null));
		
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
		
		//============================================================
		// CONFIGURATION
		//============================================================
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Query", CONFIG_FETCH_LIMIT_DEFAULT)
				.description("The default fetch limit for number of records that are allowed per source.")
				.type(FormFieldType.NUMBER)
				.value("50000")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Query", CONFIG_FETCH_LIMIT_MAX)
				.description("The maximum fetch limit for number of records that are allowed per source. Helps to limit load on your sources.")
				.type(FormFieldType.NUMBER)
				.value("250000")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Query", CONFIG_QUERY_RECORD_LIMIT)
				.description("The maximum number of records that are allowed per query(sum of all source limits). Helps to reduce performance impact on this application.")
				.type(FormFieldType.NUMBER)
				.value("500000")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
				new Configuration("Query", CONFIG_QUERY_COMMAND_LIMIT)
				.description("The maximum number of commands that are allowed per query. Limits the number of threads started per query.")
				.type(FormFieldType.NUMBER)
				.value("30")
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
			
		
		//----------------------------------
		// Source Main Page
		ROOT_MANUAL_PAGE.addChild(new ManualPage("Cheat Sheet")
				.faicon("fas fa-star")
				.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "010_query_cheatsheet.html")
			);
		
		CFWQuery pseudoQuery = new CFWQuery();
		
		//----------------------------------
		// Source Main Page
		ManualPage sourcePage = new ManualPage("Sources")
				.faicon("fas fa-star-of-life")
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
				.faicon("fas fa-cogs")
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
