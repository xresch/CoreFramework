package com.xresch.cfw._main;

import java.io.File;
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
import com.xresch.cfw.features.core.ServletHierarchy;
import com.xresch.cfw.features.dashboard.CFWDBDashboard;
import com.xresch.cfw.features.dashboard.CFWDBDashboardWidget;
import com.xresch.cfw.features.dashboard.CFWRegistryWidgets;
import com.xresch.cfw.features.dashboard.FeatureDashboard;
import com.xresch.cfw.features.dashboard.parameters.CFWDBDashboardParameter;
import com.xresch.cfw.features.dashboard.parameters.CFWRegistryDashboardParameters;
import com.xresch.cfw.features.jobs.CFWDBJob;
import com.xresch.cfw.features.jobs.CFWRegistryJobs;
import com.xresch.cfw.features.jobs.FeatureJobs;
import com.xresch.cfw.features.keyvaluepairs.CFWDBKeyValuePairs;
import com.xresch.cfw.features.keyvaluepairs.FeatureKeyValuePairs;
import com.xresch.cfw.features.keyvaluepairs.KeyValuePair;
import com.xresch.cfw.features.manual.CFWRegistryManual;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.spaces.CFWDBSpace;
import com.xresch.cfw.features.spaces.CFWDBSpaceGroup;
import com.xresch.cfw.features.spaces.FeatureSpaces;
import com.xresch.cfw.features.spaces.Space;
import com.xresch.cfw.features.spaces.SpaceGroup;
import com.xresch.cfw.features.usermgmt.CFWDBPermission;
import com.xresch.cfw.features.usermgmt.CFWDBRole;
import com.xresch.cfw.features.usermgmt.CFWDBRolePermissionMap;
import com.xresch.cfw.features.usermgmt.CFWDBUser;
import com.xresch.cfw.features.usermgmt.CFWDBUserRoleMap;
import com.xresch.cfw.features.usermgmt.CFWRegistryAudit;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.mail.CFWMail;
import com.xresch.cfw.schedule.CFWTaskScheduler;
import com.xresch.cfw.utils.CFWDump;
import com.xresch.cfw.utils.CFWFiles;
import com.xresch.cfw.utils.CFWHttp;
import com.xresch.cfw.utils.CFWRandom;
import com.xresch.cfw.utils.CFWScripting;
import com.xresch.cfw.utils.CFWSecurity;
import com.xresch.cfw.utils.CFWUtilsAnalysis;
import com.xresch.cfw.utils.CFWUtilsArray;
import com.xresch.cfw.utils.CFWUtilsText;
import com.xresch.cfw.utils.CFWUtilsTime;
import com.xresch.cfw.utils.json.CFWJson;
import com.xresch.cfw.validation.CFWValidation;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2020
 **************************************************************************************************************/
public class CFW {
	
	private static Logger logger;
	
	private static boolean isConfigFolderPrepared = false;
	
	public static String MODE_FULL = "FULL";
	public static String MODE_APP = "APP";
	public static String MODE_DATABASE = "DB";
	
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
		public static class DashboardParameters extends CFWDBDashboardParameter{};
		public static class Jobs extends CFWDBJob{};
		public static class KeyValuePairs extends CFWDBKeyValuePairs{};
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
	public static class Files extends CFWFiles {}
	public static class HTTP extends CFWHttp {}
	public static class JSON extends CFWJson {}
	public static class Localization extends CFWLocalization {}
	public static class Mail extends CFWMail {}
	public static class Messages extends CFWMessages {}
	public static class Properties extends CFWProperties {}
	public static class Random extends CFWRandom {}
	public static class Registry {
		public static class API extends CFWRegistryAPI {}
		public static class Audit extends CFWRegistryAudit {} 
		public static class Components extends CFWRegistryComponents {} 
		public static class ContextSettings extends CFWRegistryContextSettings {} 
		public static class Features extends CFWRegistryFeatures {} 
		public static class Jobs extends CFWRegistryJobs {} 
		public static class Manual extends CFWRegistryManual {} 
		public static class Objects extends CFWRegistryObjects {} 
		public static class Parameters extends CFWRegistryDashboardParameters {} 
		public static class Widgets extends CFWRegistryWidgets {} 
	}
	public static class Schedule extends CFWTaskScheduler {}
	public static class Scripting extends CFWScripting {}
	public static class Security extends CFWSecurity {}
	public static class Utils {
		public static class Analysis extends CFWUtilsAnalysis {}
		public static class Array extends CFWUtilsArray {}
		public static class Text extends CFWUtilsText {}
		public static class Time extends CFWUtilsTime {}
	}
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
		
		//logger has to be initalized after arguments are loaded
		logger = CFWLog.getLogger(CFW.class.getName());
		
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
		prepareConfigFolder();
		String folder = CFW.CLI.getValue(CFW.CLI.VM_CONFIG_FOLDER);
		String filename = CFW.CLI.getValue(CFW.CLI.VM_CONFIG_FILENAME);
		CFW.Properties.loadProperties(folder+File.separator+filename);
		

		
	}
	
	public static void prepareConfigFolder() {
		
		if(!isConfigFolderPrepared) {
			//------------------------------------
			// Create Target Folder
			File targetFolder = new File(CFW.CLI.getValue(CFW.CLI.VM_CONFIG_FOLDER));
			if(!targetFolder.exists() 
			|| !targetFolder.isDirectory()) {
				targetFolder.mkdirs();
			}
			
			//------------------------------------
			// Iterate Files and copy if not exists
			File defaultFolder = new File(CFW.CLI.getValue(CFW.CLI.VM_CONFIG_FOLDER_DEFAULT));
			
			if(!defaultFolder.equals(targetFolder)) {
				
				CFW.Files.mergeFolderInto(defaultFolder, targetFolder, false);
			}
			
			isConfigFolderPrepared=true;
		}
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
	   			@Override public void settings() { 
	   					CFW.AppSettings.enableDashboarding(true); 
	   					CFW.AppSettings.enableSpaces(true); 
	   			}
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
		
		String mode = CFW.Properties.MODE;
		
	    //--------------------------------
	    // Handle Shutdown request.
	    if (CFW.CLI.isArgumentLoaded(CLI.STOP)) {
    		CFWApplicationExecutor.sendStopRequest();
    		appToStart.stopApp();
			System.exit(0);
    		return;
	    }
		
		//---------------------------
		// Initialize Database Server and/or Connection
		CFW.DB.initializeDB(); 
		
		//Needed here for Feature management
		if(mode.contains(MODE_FULL) || mode.contains(MODE_APP) ) {
			new KeyValuePair().createTable();
		}
		
	    //--------------------------------
	    // Load Application settings
	    appToStart.settings();
	    
	    //--------------------------------
	    // Register Components
	    doRegister(appToStart);
	    
	    ArrayList<CFWAppFeature> features = CFW.Registry.Features.getFeatureInstances();
	    ArrayList<CFWObject> objectArray = CFW.Registry.Objects.getCFWObjectInstances();
	    
	    //--------------------------------
	    // Initialize Database with Data
	    // from Features and CFWObjects
	    if(mode.contains(MODE_FULL) || mode.contains(MODE_APP) ) {
	    	initializeDatabase(appToStart, features, objectArray);
	    }
		
		//---------------------------
		// Load Hierarchy definitions
		for(CFWObject object : objectArray) {
			if(object.isHierarchical()) {
				ServletHierarchy.addConfig(object.getHierarchyConfig());
			}
		}
		
		//---------------------------
		// Load API Definitions
		if(mode.contains(MODE_FULL) || mode.contains(MODE_APP) ) {
			loadAPIDefinitions(objectArray);
		}
		
	    //--------------------------------
	    // Start Application
    	if(mode.contains(MODE_FULL) || mode.contains(MODE_APP) ) {
			CFWApplicationExecutor executor = new CFWApplicationExecutor(appToStart);
			
			for(CFWAppFeature feature : features) {
				if(feature.isFeatureEnabled()) {
					feature.addFeature(executor);
				}
			}
			
			appToStart.startApp(executor);
			
			
			try {
				executor.start();
			} catch (Exception e) {
				new CFWLog(logger)
					.severe("Exception occured during application startup.", e);
			}
				    	
    	}
    	
	    //--------------------------------
	    // Start Scheduled Tasks
    	initializeScheduledTasks(appToStart, features);
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
		CFW.Registry.Features.addFeature(FeatureKeyValuePairs.class);
		CFW.Registry.Features.addFeature(FeatureCore.class);	
				
		CFW.Registry.Features.addFeature(FeatureUserManagement.class);	
		
		if(CFW.AppSettings.isContextSettingsEnabled()) {
			CFW.Registry.Features.addFeature(FeatureContextSettings.class);	
		}
		
		CFW.Registry.Features.addFeature(FeatureAPI.class);	
		CFW.Registry.Features.addFeature(FeatureSystemAnalytics.class);		
		CFW.Registry.Features.addFeature(FeatureManual.class);	
		
		if(CFW.AppSettings.isDashboardingEnabled()) {
			CFW.Registry.Features.addFeature(FeatureDashboard.class);	
		}
		
		CFW.Registry.Features.addFeature(FeatureJobs.class);
		
		if(CFW.AppSettings.isSpacesEnabled()) {
			CFW.Registry.Features.addFeature(FeatureSpaces.class);	
		}
		
		//---------------------------
		// Application Register
		appToStart.register();
		
		//---------------------------
		// Load Extentions
		loadExtensionFeatures();
		
		//---------------------------
		// Load Features Register
		ArrayList<CFWAppFeature> features = CFW.Registry.Features.getFeatureInstances();
		for(CFWAppFeature feature : features) {
			if(feature.getNameForFeatureManagement() != null) {
				//------------------------------------
				// Handle Managed Features
				CFW.DB.KeyValuePairs.oneTimeCreate(
						new KeyValuePair()
							.key(CFWAppFeature.KEY_VALUE_PREFIX+feature.getNameForFeatureManagement())
							.value(""+feature.activeByDefault())
							.description(feature.getDescriptionForFeatureManagement())
							.category(CFWAppFeature.KEY_VALUE_CATEGORY)
						);
				
				if(feature.isFeatureEnabled()) {
					feature.register();
				}
			}else {
				//------------------------------------
				// Load Managed features Features
				feature.register();
			}
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
	private static void initializeDatabase(CFWAppInterface appToStart, ArrayList<CFWAppFeature> features, ArrayList<CFWObject> objectArray) {
				
		//---------------------------
		// Iterate over Registered Objects
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
			if(feature.isFeatureEnabled()) {
				feature.initializeDB();
			}
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
			if(feature.isFeatureEnabled()) {
				feature.startTasks();
			}
		}
		
		//---------------------------
		// Do Application init
		appToStart.startTasks();
		
	
	}
	
}
