package com.xresch.cfw._main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ServiceLoader;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.xresch.cfw.caching.CFWCacheManagement;
import com.xresch.cfw.cli.ArgumentsException;
import com.xresch.cfw.cli.CFWCommandLineInterface;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWRegistryObjects;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.extensions.databases.FeatureDBExtensions;
import com.xresch.cfw.extensions.databases.generic.FeatureDBExtensionsGenericJDBC;
import com.xresch.cfw.extensions.databases.mssql.FeatureDBExtensionsMSSQL;
import com.xresch.cfw.extensions.databases.mysql.FeatureDBExtensionsMySQL;
import com.xresch.cfw.extensions.databases.oracle.FeatureDBExtensionsOracle;
import com.xresch.cfw.extensions.web.FeatureWebExtensions;
import com.xresch.cfw.features.analytics.FeatureSystemAnalytics;
import com.xresch.cfw.features.api.CFWRegistryAPI;
import com.xresch.cfw.features.api.FeatureAPI;
import com.xresch.cfw.features.config.CFWDBConfig;
import com.xresch.cfw.features.config.FeatureConfig;
import com.xresch.cfw.features.contextsettings.CFWDBContextSettings;
import com.xresch.cfw.features.contextsettings.CFWRegistryContextSettings;
import com.xresch.cfw.features.contextsettings.FeatureContextSettings;
import com.xresch.cfw.features.core.CFWLocalization;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.features.core.ServletHierarchy;
import com.xresch.cfw.features.credentials.CFWDBCredentials;
import com.xresch.cfw.features.credentials.CFWDBCredentialsEditorGroupsMap;
import com.xresch.cfw.features.credentials.CFWDBCredentialsEditorsMap;
import com.xresch.cfw.features.credentials.CFWDBCredentialsSharedGroupsMap;
import com.xresch.cfw.features.credentials.CFWDBCredentialsSharedUserMap;
import com.xresch.cfw.features.credentials.FeatureCredentials;
import com.xresch.cfw.features.dashboard.CFWDBDashboard;
import com.xresch.cfw.features.dashboard.CFWDBDashboardEditorGroupsMap;
import com.xresch.cfw.features.dashboard.CFWDBDashboardEditorsMap;
import com.xresch.cfw.features.dashboard.CFWDBDashboardFavoriteMap;
import com.xresch.cfw.features.dashboard.CFWDBDashboardSharedGroupsMap;
import com.xresch.cfw.features.dashboard.CFWDBDashboardSharedUserMap;
import com.xresch.cfw.features.dashboard.CFWDBDashboardWidget;
import com.xresch.cfw.features.dashboard.CFWRegistryWidgets;
import com.xresch.cfw.features.dashboard.FeatureDashboard;
import com.xresch.cfw.features.datetime.CFWDBDate;
import com.xresch.cfw.features.datetime.FeatureDateTime;
import com.xresch.cfw.features.eav.CFWDBEAVAttribute;
import com.xresch.cfw.features.eav.CFWDBEAVEntity;
import com.xresch.cfw.features.eav.CFWDBEAVStats;
import com.xresch.cfw.features.eav.CFWDBEAVValue;
import com.xresch.cfw.features.eav.FeatureEAV;
import com.xresch.cfw.features.jobs.CFWDBJob;
import com.xresch.cfw.features.jobs.CFWJobsAlerting;
import com.xresch.cfw.features.jobs.CFWRegistryJobs;
import com.xresch.cfw.features.jobs.FeatureJobs;
import com.xresch.cfw.features.keyvaluepairs.CFWDBKeyValuePairs;
import com.xresch.cfw.features.keyvaluepairs.FeatureKeyValuePairs;
import com.xresch.cfw.features.keyvaluepairs.KeyValuePair;
import com.xresch.cfw.features.manual.CFWRegistryManual;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.notifications.CFWDBNotifications;
import com.xresch.cfw.features.notifications.FeatureNotifications;
import com.xresch.cfw.features.notifications.Notification;
import com.xresch.cfw.features.parameter.CFWDBParameter;
import com.xresch.cfw.features.parameter.CFWRegistryParameters;
import com.xresch.cfw.features.parameter.FeatureParameter;
import com.xresch.cfw.features.query.CFWQueryRegistry;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.spaces.CFWDBSpace;
import com.xresch.cfw.features.spaces.CFWDBSpaceGroup;
import com.xresch.cfw.features.spaces.FeatureSpaces;
import com.xresch.cfw.features.spaces.Space;
import com.xresch.cfw.features.spaces.SpaceGroup;
import com.xresch.cfw.features.usermgmt.CFWDBPermission;
import com.xresch.cfw.features.usermgmt.CFWDBRole;
import com.xresch.cfw.features.usermgmt.CFWDBRoleEditorsMap;
import com.xresch.cfw.features.usermgmt.CFWDBRolePermissionMap;
import com.xresch.cfw.features.usermgmt.CFWDBUser;
import com.xresch.cfw.features.usermgmt.CFWDBUserRoleMap;
import com.xresch.cfw.features.usermgmt.CFWRegistryAudit;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.schedule.CFWTaskScheduler;
import com.xresch.cfw.spi.CFWAppFeature;
import com.xresch.cfw.spi.CFWAppInterface;
import com.xresch.cfw.utils.CFWDump;
import com.xresch.cfw.utils.CFWFiles;
import com.xresch.cfw.utils.CFWHTML;
import com.xresch.cfw.utils.CFWHttp;
import com.xresch.cfw.utils.CFWMath;
import com.xresch.cfw.utils.CFWRandom;
import com.xresch.cfw.utils.CFWScripting;
import com.xresch.cfw.utils.CFWSecurity;
import com.xresch.cfw.utils.CFWState;
import com.xresch.cfw.utils.CFWTime;
import com.xresch.cfw.utils.CFWUtilsAnalysis;
import com.xresch.cfw.utils.CFWUtilsArray;
import com.xresch.cfw.utils.CFWUtilsText;
import com.xresch.cfw.utils.CFWXML;
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
		
		public static class Credentials extends CFWDBCredentials{};
		public static class CredentialsEditors extends CFWDBCredentialsEditorsMap{};
		public static class CredentialsEditorGroups extends CFWDBCredentialsEditorGroupsMap{};
		public static class CredentialsSharedGroups extends CFWDBCredentialsSharedGroupsMap{};
		public static class CredentialsSharedUsers extends CFWDBCredentialsSharedUserMap{};
		
		public static class Dashboards extends CFWDBDashboard{};
		public static class DashboardWidgets extends CFWDBDashboardWidget{};
		public static class DashboardFavorites extends CFWDBDashboardFavoriteMap{};
		public static class DashboardEditors extends CFWDBDashboardEditorsMap{};
		public static class DashboardEditorGroups extends CFWDBDashboardEditorGroupsMap{};
		public static class DashboardSharedGroups extends CFWDBDashboardSharedGroupsMap{};
		public static class DashboardSharedUsers extends CFWDBDashboardSharedUserMap{};
		
		public static class Date extends CFWDBDate{};
		public static class EAVEntity extends CFWDBEAVEntity{};
		public static class EAVAttribute extends CFWDBEAVAttribute{};
		public static class EAVValue extends CFWDBEAVValue{};
		public static class EAVStats extends CFWDBEAVStats{};
		public static class Jobs extends CFWDBJob{};
		public static class KeyValuePairs extends CFWDBKeyValuePairs{};
		public static class Notifications extends CFWDBNotifications{};
		public static class Parameters extends CFWDBParameter{};
		public static class Permissions extends CFWDBPermission{};
		public static class Roles extends CFWDBRole{};
		public static class RoleEditors extends CFWDBRoleEditorsMap{};
		public static class RolePermissionMap extends CFWDBRolePermissionMap{};
		public static class Users extends CFWDBUser{};
		public static class UserRoleMap extends CFWDBUserRoleMap{};
		public static class Spaces extends CFWDBSpace{};
		public static class SpaceGroups extends CFWDBSpaceGroup{};
	}
	public static class Caching extends CFWCacheManagement {}
	
	public static class Conditions extends CFWState {}
	
	public static class Context {
		public static class App extends CFWContextApp{};
		public static class Request extends CFWContextRequest{};
		public static class Session extends CFWContextSession{};
	}
	
	public static class CLI extends CFWCommandLineInterface {}
	public static class Dump extends CFWDump {}
	public static class Files extends CFWFiles {}
	public static class HTTP extends CFWHttp {}
	public static class HTML extends CFWHTML {}
	public static class JSON extends CFWJson {}
	public static class Localization extends CFWLocalization {}
	public static class Math extends CFWMath {}
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
		public static class JobsAlerting extends CFWJobsAlerting {} 
		public static class Manual extends CFWRegistryManual {} 
		public static class Objects extends CFWRegistryObjects {} 
		public static class Parameters extends CFWRegistryParameters {} 
		public static class Query extends CFWQueryRegistry {} 
		public static class Widgets extends CFWRegistryWidgets {} 
	}
	public static class Schedule extends CFWTaskScheduler {}
	public static class Scripting extends CFWScripting {}
	public static class Security extends CFWSecurity {}
	public static class Utils {
		public static class Analysis extends CFWUtilsAnalysis {}
		public static class Array extends CFWUtilsArray {}
		public static class Text extends CFWUtilsText {}
	}
	public static class Validation extends CFWValidation {}
	
	
	public static class Time extends CFWTime {}
	public static class XML extends CFWXML {}


	//##############################################################################
	// GLOBALS
	//##############################################################################
	public static final String REQUEST_ATTR_ID = "requestID";
	public static final String SESSION_DATA = "sessionData";	
	
	/***********************************************************************
	 * Note: This will be called by initializeApp().
	 * Do not use this method except for testing purposes.
	 * 
	 * Initializes core stuff:
	 *   - read command line arguments and validate
	 *   - Load cfw.properties
	 *   
	 ***********************************************************************/
	public static void initializeCore(String[] args) throws ArgumentsException, IOException{
				
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
	public static CFWAppInterface loadExtensionApplication() {
		
       ServiceLoader<CFWAppInterface> loader = ServiceLoader.load(CFWAppInterface.class);
       for(CFWAppInterface instance : loader) {
    	   new CFWLog(logger).info("Load CFW Application:"+instance.getClass().getName());
    	   return instance;

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
       
       ServiceLoader<CFWAppFeature> loader = ServiceLoader.load(CFWAppFeature.class);
       for(CFWAppFeature feature : loader) {
    	   new CFWLog(logger).info("Load CFW Feature:"+feature.getClass().getName());
    	   CFW.Registry.Features.addFeature(feature.getClass());
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
	   				CFW.AppSettings.enableContextSettings(true); 
	   				//CFW.AppSettings.enableSpaces(true); 
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
	    
	    //--------------------------------
	    // Load Application settings
	    // before DB to load DB related settings as well
	    appToStart.settings();
	    
		//---------------------------------------
    	// Set JVM TimeZone, needed for H2 database to 
		// properly handle epoch times
		CFW.Time.setMachineTimeZone(TimeZone.getDefault());
		TimeZone.setDefault(TimeZone.getTimeZone(CFWProperties.JVM_TIMEZONE));

		//---------------------------
		// Initialize Database Server and/or Connection
		CFW.DB.initializeDB(); 
		
		//Needed here for Feature management
		if(mode.contains(MODE_FULL) || mode.contains(MODE_APP) ) {
			new KeyValuePair().createTable();
		}
			    
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
	    // Start Scheduled Tasks
    	initializeScheduledTasks(appToStart, features);
    	
	    //--------------------------------
	    // Start Application: must be last
    	// will be kept in a thread-loop
    	if(mode.contains(MODE_FULL) || mode.contains(MODE_APP) ) {
			
    		//----------------------------------------
			// Add Features
    		CFWApplicationExecutor executor = new CFWApplicationExecutor(appToStart);
			
			for(CFWAppFeature feature : features) {
				if(feature.isFeatureEnabled()) {
					// surround with try-catch to make sure a single corrupt feature cannot kill the app.
					try{
						feature.addFeature(executor);
						new CFWLog(logger).info("Feature Loaded: "+feature.getClass().getName());
					}catch(Throwable e) {
						new CFWLog(logger).severe("Unexpected error while initializing feature: "+feature.getClass().getName(), e);
					}
				}
			}
						
			//----------------------------------------
			// Handle Startup Notifications
			String category = "CFW_STARTUP_NOTIFICATION";
			
			CFW.DB.Notifications.deleteAllByCategory(category);
			Notification templateNotification = 
					new Notification()
							.category(category)
							.messageType(MessageType.INFO)
							.title("Application Started")
							.message("The application was started at the given time.");
			
			ArrayList<User> ultimateUsers = CFW.DB.Roles.getAdminsAndSuperusers();
			CFW.DB.Notifications.createForUsers(ultimateUsers, templateNotification);
			
			//----------------------------------------
			// Handle Startup Notifications
			appToStart.startApp(executor);
			try {
				executor.start();
			} catch (Exception e) {
				new CFWLog(logger)
					.severe("Exception occured during application startup.", e);
			}
				    	
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
		CFW.Registry.Features.addFeature(FeatureConfig.class);
		CFW.Registry.Features.addFeature(FeatureKeyValuePairs.class);
		CFW.Registry.Features.addFeature(FeatureCore.class);	
		CFW.Registry.Features.addFeature(FeatureDateTime.class);	
		CFW.Registry.Features.addFeature(FeatureEAV.class);
				
		CFW.Registry.Features.addFeature(FeatureUserManagement.class);	
		
		if(CFW.AppSettings.isContextSettingsEnabled()) {
			CFW.Registry.Features.addFeature(FeatureContextSettings.class);	
		}
		
		CFW.Registry.Features.addFeature(FeatureAPI.class);	
		CFW.Registry.Features.addFeature(FeatureSystemAnalytics.class);		
		CFW.Registry.Features.addFeature(FeatureManual.class);	
		CFW.Registry.Features.addFeature(FeatureNotifications.class);	
		CFW.Registry.Features.addFeature(FeatureParameter.class);	
		CFW.Registry.Features.addFeature(FeatureCredentials.class);
		
		if(CFW.AppSettings.isDashboardingEnabled()) {
			CFW.Registry.Features.addFeature(FeatureDashboard.class);	
		}
		
		CFW.Registry.Features.addFeature(FeatureQuery.class);
		
		CFW.Registry.Features.addFeature(FeatureJobs.class);
		
		if(CFW.AppSettings.isSpacesEnabled()) {
			CFW.Registry.Features.addFeature(FeatureSpaces.class);	
		}
		
		//---------------------------
		// Register Extensions
		CFW.Registry.Features.addFeature(FeatureDBExtensions.class);
		CFW.Registry.Features.addFeature(FeatureDBExtensionsMySQL.class);
		CFW.Registry.Features.addFeature(FeatureDBExtensionsMSSQL.class);
		CFW.Registry.Features.addFeature(FeatureDBExtensionsOracle.class);
		CFW.Registry.Features.addFeature(FeatureDBExtensionsGenericJDBC.class);
		CFW.Registry.Features.addFeature(FeatureWebExtensions.class);
		
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
