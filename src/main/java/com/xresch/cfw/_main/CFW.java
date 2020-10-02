package com.xresch.cfw._main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;

import org.reflections.Reflections;

import com.xresch.cfw.caching.CFWCacheManagement;
import com.xresch.cfw.cli.ArgumentsException;
import com.xresch.cfw.cli.CFWCommandLineInterface;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWRegistryObjects;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.db.spaces.CFWDBSpace;
import com.xresch.cfw.db.spaces.CFWDBSpaceGroup;
import com.xresch.cfw.db.spaces.Space;
import com.xresch.cfw.db.spaces.SpaceGroup;
import com.xresch.cfw.features.analytics.FeatureSystemAnalytics;
import com.xresch.cfw.features.api.CFWRegistryAPI;
import com.xresch.cfw.features.api.FeatureAPI;
import com.xresch.cfw.features.config.CFWDBConfig;
import com.xresch.cfw.features.config.FeatureConfiguration;
import com.xresch.cfw.features.contextsettings.CFWDBContextSettings;
import com.xresch.cfw.features.contextsettings.CFWRegistryContextSettings;
import com.xresch.cfw.features.contextsettings.FeatureContextSettings;
import com.xresch.cfw.features.core.CFWLocalization;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.features.dashboard.CFWDBDashboard;
import com.xresch.cfw.features.dashboard.CFWDBDashboardWidget;
import com.xresch.cfw.features.dashboard.CFWRegistryWidgets;
import com.xresch.cfw.features.dashboard.FeatureDashboard;
import com.xresch.cfw.features.manual.CFWRegistryManual;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.usermgmt.CFWDBPermission;
import com.xresch.cfw.features.usermgmt.CFWDBRole;
import com.xresch.cfw.features.usermgmt.CFWDBRolePermissionMap;
import com.xresch.cfw.features.usermgmt.CFWDBUser;
import com.xresch.cfw.features.usermgmt.CFWDBUserRoleMap;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.mail.CFWMail;
import com.xresch.cfw.schedule.CFWSchedule;
import com.xresch.cfw.utils.CFWDump;
import com.xresch.cfw.utils.CFWFiles;
import com.xresch.cfw.utils.CFWHttp;
import com.xresch.cfw.utils.CFWRandom;
import com.xresch.cfw.utils.CFWScripting;
import com.xresch.cfw.utils.CFWSecurity;
import com.xresch.cfw.utils.CFWTime;
import com.xresch.cfw.utils.json.CFWJson;
import com.xresch.cfw.validation.CFWValidation;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2020
 **************************************************************************************************************/
public class CFW {
	
	private static final Logger logger = CFWLog.getLogger(CFW.class.getName());
	
	private CFW() {
		// hide constructor
	}
	//##############################################################################
	// Hierarchical Binding
	//##############################################################################
	public static String L(String key, String defaultText) { return CFW.Localization.getLocalized(key, defaultText); }
	public static String L(String key, String defaultTextWithPlaceholders, Object placeholderValues) { return CFW.Localization.getLocalized(key, defaultTextWithPlaceholders, placeholderValues); }
	
	public static class AppSettings extends CFWAppSettings {}
	public static class DB extends CFWDB {
		public static class Config extends CFWDBConfig{};
		public static class ContextSettings extends CFWDBContextSettings{};
		public static class Dashboards extends CFWDBDashboard{};
		public static class DashboardWidgets extends CFWDBDashboardWidget{};
		public static class Users extends CFWDBUser{};
		public static class Roles extends CFWDBRole{};
		public static class UserRoleMap extends CFWDBUserRoleMap{};
		public static class Permissions extends CFWDBPermission{};
		public static class RolePermissionMap extends CFWDBRolePermissionMap{};
		public static class Spaces extends CFWDBSpace{};
		public static class SpaceGroups extends CFWDBSpaceGroup{};
	}
	public static class Caching extends CFWCacheManagement {}
	
	public static class Context {
		public static class App extends CFWContextApp{};
		public static class Request extends CFWContextRequest{};
		public static class Session extends CFWContextSession{};
	}
	
	public static class CLI extends CFWCommandLineInterface {}
	public static class Dump extends CFWDump {}
	public static class Security extends CFWSecurity {}
	public static class Files extends CFWFiles {}
	public static class HTTP extends CFWHttp {}
	public static class JSON extends CFWJson {}
	public static class Localization extends CFWLocalization {}
	public static class Mail extends CFWMail {}
	public static class Properties extends CFWProperties {}
	public static class Random extends CFWRandom {}
	public static class Registry {
		public static class API extends CFWRegistryAPI {}
		public static class Components extends CFWRegistryComponents {} 
		public static class ContextSettings extends CFWRegistryContextSettings {} 
		public static class Features extends CFWRegistryFeatures {} 
		public static class Manual extends CFWRegistryManual {} 
		public static class Objects extends CFWRegistryObjects {} 
		public static class Widgets extends CFWRegistryWidgets {} 
	}
	public static class Schedule extends CFWSchedule {}
	public static class Scripting extends CFWScripting {}
	public static class Time extends CFWTime {}
	public static class Validation extends CFWValidation {}
	
	
	//##############################################################################
	// GLOBALS
	//##############################################################################
	public static final String REQUEST_ATTR_ID = "requestID";
	public static final String SESSION_DATA = "sessionData";	
	
	private static void initializeCore(String[] args) throws ArgumentsException, IOException{
		
		//------------------------------------
		// Command Line Arguments
		CFW.CLI.readArguments(args);

		if (!CFW.CLI.validateArguments()) {
			new CFWLog(logger)
				.severe("Issues loading arguments: "+CFW.CLI.getInvalidMessagesAsString());
			
			CFW.CLI.printUsage();
			throw new ArgumentsException(CFW.CLI.getInvalidMessages());
		}
	    
		//------------------------------------
		// Add allowed Packages
		CFW.Files.addAllowedPackage("com.xresch.cfw.resources");
		
		//------------------------------------
		// Load Configuration
		CFW.Properties.loadProperties(CFW.CLI.getValue(CFW.CLI.CONFIG_FILE));
		

		
	}
	
	/***********************************************************************
	 * Searches all classes annotated with @CFWExtentionApplication and 
	 * returns an instance of the first finding or an empty default application.
	 * 
	 ***********************************************************************/
	public static CFWAppInterface loadExtentionApplication() {
		
//       Reflections reflections = new Reflections(new ConfigurationBuilder()
//            //.filterInputsBy(new FilterBuilder().exclude(FilterBuilder.prefix("com.xresch.cfw.")))
//            .filterInputsBy(new FilterBuilder().exclude(FilterBuilder.prefix("java.")))
//            .setUrls(ClasspathHelper.forClassLoader())
//            //.setScanners(new SubTypesScanner(), new TypeAnnotationsScanner())
//       );
		
       Reflections reflections = new Reflections("");
       Set<Class<?>> types = reflections.getTypesAnnotatedWith(CFWExtensionApplication.class);
       
       for(Class<?> clazz : types) {
    	   if(CFWAppInterface.class.isAssignableFrom(clazz)) {
    		   new CFWLog(logger).info("Load CFW Extension Application:"+clazz.getName());
    		   
    		  try {
				CFWAppInterface instance = (CFWAppInterface)clazz.newInstance();
				return instance;
			} catch (InstantiationException | IllegalAccessException e) {
				new CFWLog(logger).severe("Error loading CFW Extension Application:"+clazz.getName(), e);
			}
    	   }
       }
       
       return null;
	}
	
	/***********************************************************************
	 * Searches all classes annotated with @CFWExtentionFeature and adds 
	 * them to the registry.
	 * 
	 ***********************************************************************/
	@SuppressWarnings("unchecked")
	private static void loadExtensionFeatures() {
		
//       Reflections reflections = new Reflections(new ConfigurationBuilder()
//            //.filterInputsBy(new FilterBuilder().exclude(FilterBuilder.prefix("com.xresch.cfw.")))
//            .filterInputsBy(new FilterBuilder().exclude(FilterBuilder.prefix("java.")))
//            .setUrls(ClasspathHelper.forClassLoader())
//            //.setScanners(new SubTypesScanner(), new TypeAnnotationsScanner())
//       );
	   Reflections reflections = new Reflections("");
       Set<Class<?>> types = reflections.getTypesAnnotatedWith(CFWExtensionFeature.class);
       for(Class<?> clazz : types) {
    	   if(CFWAppFeature.class.isAssignableFrom(clazz)) {
    		   new CFWLog(logger).info("Load CFW Extension:"+clazz.getName());
    		   CFW.Registry.Features.addFeature((Class<? extends CFWAppFeature>)clazz);
    	   }
       }
	}

	/***********************************************************************
	 * Create an instance of the CFWDefaultApp.
	 * @param args command line arguments
	 * @throws Exception 
	 ***********************************************************************/
	public static void initializeApp(CFWAppInterface appToStart, String[] args) throws Exception {
		
	   	//------------------------------------
	   	// Create empty Default if null
		if(appToStart == null) {
	   		appToStart = new CFWAppInterface() {
	   			@Override public void settings() { CFW.AppSettings.setEnableDashboarding(true); }
					@Override public void startApp(CFWApplicationExecutor executor) { executor.setDefaultURL("/dashboard/list", true); }
					@Override public void register() { /* Do nothing */ }
					@Override public void initializeDB() { /* Do nothing */ }
					@Override public void startTasks() { /* Do nothing */ }
					@Override public void stopApp() { /* Do nothing */ }
				};
			
			new CFWLog(logger).warn("Application to start is null, using default empty application.");
		}
	    //--------------------------------
	    // Initialize Core
		CFW.initializeCore(args);
		
	    //--------------------------------
	    // Handle Shutdown request.
	    if (CFW.CLI.isArgumentLoaded(CLI.STOP)) {
    		CFWApplicationExecutor.sendStopRequest();
    		appToStart.stopApp();
			System.exit(0);
    		return;
	    }
		
		//---------------------------
		// Start Database 
		CFW.DB.startDBServer(); 
		
	    //--------------------------------
	    // Register Components
	    appToStart.settings();
	    
	    //--------------------------------
	    // Register Components
	    doRegister(appToStart);
	    
	    ArrayList<CFWAppFeature> features = CFW.Registry.Features.getFeatureInstances();
	    
	    //--------------------------------
	    // Start Database 	
    	initializeDatabase(appToStart, features);
		
		//---------------------------
		// Load API Definitions
		ArrayList<CFWObject> objectArray = CFW.Registry.Objects.getCFWObjectInstances();
		loadAPIDefinitions(objectArray);

	    //--------------------------------
	    // Start Scheduled Tasks
    	initializeScheduledTasks(appToStart, features);
    	
	    //--------------------------------
	    // Start Application
		CFWApplicationExecutor executor = new CFWApplicationExecutor(appToStart);
		
		for(CFWAppFeature feature : features) {
			feature.addFeature(executor);
		}
		
		appToStart.startApp(executor);
		
		
		try {
			executor.start();
		} catch (Exception e) {
			new CFWLog(logger)
				.severe("Exception occured during application startup.", e);
		}
	}
	
	/***********************************************************************
	 * Starts and initializes the Database. Iterates over all Objects in the 
	 * Registry and add
	 * @param features 
	 * @param CFWAppInterface application to start
	 ***********************************************************************/
	private static void doRegister(CFWAppInterface appToStart) {
		
		//---------------------------
		// Register Objects 
		CFW.Registry.Objects.addCFWObject(SpaceGroup.class);
		CFW.Registry.Objects.addCFWObject(Space.class);
		
		//---------------------------
		// Register Features
		CFW.Registry.Features.addFeature(FeatureConfiguration.class);
		CFW.Registry.Features.addFeature(FeatureCore.class);	
			
		if(CFW.AppSettings.isContextSettingsEnabled()) {
			CFW.Registry.Features.addFeature(FeatureContextSettings.class);	
		}
		
		CFW.Registry.Features.addFeature(FeatureUserManagement.class);	
		CFW.Registry.Features.addFeature(FeatureAPI.class);	
		CFW.Registry.Features.addFeature(FeatureSystemAnalytics.class);		
		CFW.Registry.Features.addFeature(FeatureManual.class);	
		
		if(CFW.AppSettings.isDashboardingEnabled()) {
			CFW.Registry.Features.addFeature(FeatureDashboard.class);	
		}
		
		
		//---------------------------
		// Application Register
		appToStart.register();
		
		//---------------------------
		// Load Extentions
		loadExtensionFeatures();
		
		//---------------------------
		// Feature Register
		ArrayList<CFWAppFeature> features = CFW.Registry.Features.getFeatureInstances();
		for(CFWAppFeature feature : features) {
			feature.register();
		}
		
	}
	
	/***********************************************************************
	 * 
	 ***********************************************************************/
	private static void loadAPIDefinitions(ArrayList<CFWObject> objectArray) {
		for(CFWObject object : objectArray) {
			CFW.Registry.API.addAll(object.getAPIDefinitions());
		}
	}
	
	/***********************************************************************
	 * Starts and initializes the Database. Iterates over all Objects in the 
	 * Registry and add
	 * @param features 
	 * @param CFWAppInterface application to start
	 ***********************************************************************/
	private static void initializeDatabase(CFWAppInterface appToStart, ArrayList<CFWAppFeature> features) {
				
		//---------------------------
		// Iterate over Registered Objects
    	ArrayList<CFWObject> objectArray = CFW.Registry.Objects.getCFWObjectInstances();
    	
    	for(CFWObject object : objectArray) {
    		if(object.getTableName() != null) {
    			object.migrateTable();
    			
    		}
    	}
    	
    	for(CFWObject object : objectArray) {
    		if(object.getTableName() != null) {
    			object.createTable();
    			
    		}
    	}
    	
    	for(CFWObject object : objectArray) {
    		if(object.getTableName() != null) {
    			object.updateTable();
    			
    		}
    	}
		
    	for(CFWObject object : objectArray) {
    		if(object.getTableName() != null) {
    			object.initDB();
    			
    		}
    	}
    	
    	for(CFWObject object : objectArray) {
    		if(object.getTableName() != null) {
    			object.initDBSecond();
    		}
    	}
    	
    	for(CFWObject object : objectArray) {
    		if(object.getTableName() != null) {
    			object.initDBThird();
    		}
    	}
    	
		//---------------------------
		// Reset Admin PW if configured
    	CFWDB.resetAdminPW();
    
		//---------------------------
		// Feature Initialize
		for(CFWAppFeature feature : features) {
			feature.initializeDB();
		}
		
		//---------------------------
		// Do Application init
		appToStart.initializeDB();
		
	}
	
	/***********************************************************************
	 * Starts and initializes the scheduled tasks.
	 * @param features 
	 * @param CFWAppInterface application to start
	 ***********************************************************************/
	private static void initializeScheduledTasks(CFWAppInterface appToStart, ArrayList<CFWAppFeature> features) {
		
		//---------------------------
		// Feature Initialize
		for(CFWAppFeature feature : features) {
			feature.startTasks();
		}
		
		//---------------------------
		// Do Application init
		appToStart.startTasks();
		
	
	}
	
}
