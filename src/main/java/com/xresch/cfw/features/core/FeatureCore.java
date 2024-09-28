package com.xresch.cfw.features.core;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw._main.CFWProperties;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.TaskDatabaseBackup;
import com.xresch.cfw.features.analytics.FeatureSystemAnalytics;
import com.xresch.cfw.features.config.ConfigChangeListener;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.features.config.FeatureConfig;
import com.xresch.cfw.features.core.auth.SSOProviderSettingsManagement;
import com.xresch.cfw.features.core.auth.ServletChangePassword;
import com.xresch.cfw.features.core.auth.ServletLogin;
import com.xresch.cfw.features.core.auth.ServletLogout;
import com.xresch.cfw.features.core.auth.openid.SSOProviderSettingsOpenID;
import com.xresch.cfw.features.core.auth.openid.ServletSSOOpenIDCallback;
import com.xresch.cfw.features.core.auth.saml.SSOProviderSettingsSAML;
import com.xresch.cfw.features.core.auth.saml.ServletSAML2AssertionConsumerService;
import com.xresch.cfw.features.core.auth.saml.ServletSAML2Login;
import com.xresch.cfw.features.core.auth.saml.ServletSAML2Metadata;
import com.xresch.cfw.features.dashboard.Dashboard;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.logging.CFWAuditLog;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.MenuItem;
import com.xresch.cfw.spi.CFWAppFeature;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureCore extends CFWAppFeature {
	
	private static Logger logger = CFWLog.getLogger(FeatureCore.class.getName());
	
	public static final String SERVLET_PATH_LOGIN = "/app/login";
	public static final String SERVLET_PATH_SSO_OPENID = "/sso/callback/openidconnect";

	public static final String RESOURCE_PACKAGE = "com.xresch.cfw.features.core.resources";
		
	public static final String PERMISSION_FEATURE_MGMT = "Feature Management";
	public static final String PERMISSION_ALLOW_HTML = "Allow HTML";
	public static final String PERMISSION_ALLOW_JAVASCRIPT = "Allow Javascript";
	
	public static final String CONFIG_BROWSER_RESOURCE_MAXAGE = "Browser Resource Max Age";
	
	private static final LinkedHashMap<String, String> CHART_TYPES = new LinkedHashMap<>();
	static {
		CHART_TYPES.put("area"			, "Area");
		CHART_TYPES.put("line"			, "Line");
		CHART_TYPES.put("bar"			, "Bar");
		CHART_TYPES.put("scatter"		, "Scatter");
		CHART_TYPES.put("steppedarea"	, "Stepped Area");
		CHART_TYPES.put("steppedline"	, "Stepped Line");
		CHART_TYPES.put("sparkarea"		, "Spark Area");
		CHART_TYPES.put("sparkline"		, "Spark Line");
		CHART_TYPES.put("sparkbar"		, "Spark Bar");
		CHART_TYPES.put("pie"			, "Pie");
		CHART_TYPES.put("doughnut"		, "Doughnut");
		CHART_TYPES.put("radar"			, "Radar");
		CHART_TYPES.put("polar"			, "Polar");
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public void register() {
		
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		
		//----------------------------------
		// Register Languages
		CFW.Localization.registerLocaleFile(Locale.ENGLISH, "", new FileDefinition(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".lang", "lang_en.properties"));
		CFW.Localization.registerLocaleFile(Locale.GERMAN, "", new FileDefinition(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".lang", "lang_de.properties"));		
		
		//----------------------------------
		// Register Global CSS
		HandlingType HANDLE_JAR = FileDefinition.HandlingType.JAR_RESOURCE;
		String CSS_PATH = FeatureCore.RESOURCE_PACKAGE + ".css";
		CFW.Registry.Components.addGlobalCSSFile(HANDLE_JAR, CSS_PATH, "bootstrap-tagsinput.css");
		CFW.Registry.Components.addGlobalCSSFile(HANDLE_JAR, CSS_PATH, "summernote-bs4.css");
		CFW.Registry.Components.addGlobalCSSFile(HANDLE_JAR, CSS_PATH, "jquery-ui.min.css");
		CFW.Registry.Components.addGlobalCSSFile(HANDLE_JAR, CSS_PATH, "font-awesome.css");
		CFW.Registry.Components.addGlobalCSSFile(HANDLE_JAR, CSS_PATH, "chartjs.css");
		CFW.Registry.Components.addGlobalCSSFile(HANDLE_JAR, CSS_PATH, "cfw.css");
		CFW.Registry.Components.addGlobalCSSFile(FileDefinition.HandlingType.FILE, "./resources/css", "custom.css");
		
		//------------------------------------------
		// Register Global Javascript
		CFW.Registry.Components.addGlobalJavascript(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "jquery-3.6.0.min.js");
		CFW.Registry.Components.addGlobalJavascript(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "jquery-ui-1.12.3.min.js");
		CFW.Registry.Components.addGlobalJavascript(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "bootstrap.bundle.min.js");
		CFW.Registry.Components.addGlobalJavascript(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "bootstrap-tagsinput.js");
		
		CFW.Registry.Components.addGlobalJavascript(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "summernote-bs4.min.js");
		CFW.Registry.Components.addGlobalJavascript(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "highlight.min.js");
		CFW.Registry.Components.addGlobalJavascript(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "lodash-full-4.17.15.min.js");
		//CFW.Registry.Components.addGlobalJavascript(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "chartjs-2.93.min.js"); 
		CFW.Registry.Components.addGlobalJavascript(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "chartjs-v4.4.1.umd.js"); 
		CFW.Registry.Components.addGlobalJavascript(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "moment-v2.29.4.min.js"); // required by ChartJS
		CFW.Registry.Components.addGlobalJavascript(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "chartjs-adapter-moment-v1.0.1.js"); // required by ChartJS
		
		CFW.Registry.Components.addGlobalJavascript(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "cfw_components.js");
		CFW.Registry.Components.addGlobalJavascript(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js", "cfw.js");
		
		CFW.Registry.Components.addGlobalJavascript(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js.rendering", "cfw_renderer.js");
		CFW.Registry.Components.addGlobalJavascript(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js.rendering", "cfw_renderer_chart.js");
		CFW.Registry.Components.addGlobalJavascript(FileDefinition.HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE + ".js.rendering", "cfw_renderer_properties.js");
		
		CFW.Registry.Components.addGlobalJavascript(FileDefinition.HandlingType.FILE, "./resources/js", "custom.js");
		
		//----------------------------------
		// Register Objects
		CFW.Registry.Objects.addCFWObject(CFWAuditLog.class);
    	
		//----------------------------------
		// Register Context Settings
		CFW.Registry.ContextSettings.register(SSOProviderSettingsOpenID.SETTINGS_TYPE, SSOProviderSettingsOpenID.class);
		SSOProviderSettingsManagement.register(SSOProviderSettingsOpenID.class);
		
		CFW.Registry.ContextSettings.register(SSOProviderSettingsSAML.SETTINGS_TYPE, SSOProviderSettingsSAML.class);
		SSOProviderSettingsManagement.register(SSOProviderSettingsSAML.class);
    
    	//----------------------------------
    	// Register Admin Menu
		CFW.Registry.Components.addAdminCFWMenuItem(
				(MenuItem)new MenuItem("Feature Management")
					.faicon("fas fa-star")
					.addPermission(FeatureCore.PERMISSION_FEATURE_MGMT)	
					.href("/app/featuremanagement")
					.addAttribute("id", "cfwMenuAdmin-FeatureManagement")
				, null);
		
		
				
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public void initializeDB() {

		//============================================================
		// TEMP: MIGRATE CFWState
		//============================================================
		new CFWLog(logger)
			.off("Migration: Updating threshold fields and names in database...");
		
		int updateCount = new CFWSQL(null)
			.loadSQLResource(RESOURCE_PACKAGE+".sql", "cfw_state_migration_batch_script.sql")
			.executeBatch()
			;
		
		if(updateCount != -1) {
			new CFWLog(logger).off("Migration: Updated "+updateCount+" rows");
		}
		
		//============================================================
		// PERMISSIONS
		//============================================================
		
		//-----------------------------------
		// 
		CFW.DB.Permissions.oneTimeCreate(
		new Permission(PERMISSION_FEATURE_MGMT, FeatureUserManagement.CATEGORY_USER)
			.description("Allows to enable and disable certain features with application restart."),
			true,
			false
		);	
		
		//-----------------------------------
		// 
		CFW.DB.Permissions.oneTimeCreate(
		new Permission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS, FeatureUserManagement.CATEGORY_USER)
			.description("Analyze the application status with tools like cpu sampling."),
			true,
			false
		);	

		//-----------------------------------
		// 
		CFW.DB.Permissions.oneTimeCreate(
		new Permission(PERMISSION_ALLOW_HTML, FeatureUserManagement.CATEGORY_USER)
			.description("Allow the user to enter HTML code in any of the text fields. As this is a potential security issue, handle this permission with care."),
			false,
			false
		);	
		//-----------------------------------
		// 
		CFW.DB.Permissions.oneTimeCreate(
		new Permission(PERMISSION_ALLOW_JAVASCRIPT, FeatureUserManagement.CATEGORY_USER)
			.description("Allow the user to enter javascript code in any of the text fields. As this is a potential security issue, handle this permission with care. This permission requires 'Allow HTML' to work."),
			false,
			false
		);	
		
    	//----------------------------------
    	// Register Configurations
		CFW.DB.Config.oneTimeCreate(
				new Configuration(FeatureConfig.CATEGORY_TIMEOUTS, CONFIG_BROWSER_RESOURCE_MAXAGE)
					.description("The maximum time in seconds resources(js, css, images etc...) should be cached in the client's browser.")
					.type(FormFieldType.NUMBER)
					.value("36000")
			);
		
    	//----------------------------------
    	// Migrate Context Settings
		SSOProviderSettingsOpenID.renameExistingSettings();
	}

	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		
		//-----------------------------------------
		// Initialize SSO Providers
		SSOProviderSettingsManagement.initialize();
		
		//-----------------------------------------
		// Authentication Servlets
	    if(CFWProperties.AUTHENTICATION_ENABLED) {
	    	app.addAppServlet(ServletLogin.class, SERVLET_PATH_LOGIN);
	        app.addAppServlet(ServletLogout.class,  "/logout");
	        
	    	app.addUnsecureServlet(ServletSSOOpenIDCallback.class, SERVLET_PATH_SSO_OPENID);
	    	
	        app.addUnsecureServlet(ServletSAML2Metadata.class,	"/cfw/saml2/metadata");
	        app.addUnsecureServlet(ServletSAML2Login.class,	"/cfw/saml2/login");
	        app.addUnsecureServlet(ServletSAML2AssertionConsumerService.class,	"/cfw/saml2/acs");
	        
	    }
	    
		//-----------------------------------------
		// User Profile Servlets
	    app.addAppServlet(ServletChangePassword.class,  "/changepassword");
	    
		//-----------------------------------------
		// Secured Core Servlets
	    app.addAppServlet(ServletHierarchy.class,  "/hierarchy");
	    app.addAppServlet(ServletFeatureManagement.class,  "/featuremanagement");
	    
	    //-----------------------------------------
		// Unsecured Core Servlets
		app.addUnsecureServlet(ServletLocalization.class,  	"/cfw/locale");
		app.addUnsecureServlet(ServletFormHandler.class,	"/cfw/formhandler");
		app.addUnsecureServlet(ServletAutocomplete.class,  	"/cfw/autocomplete");
		app.addUnsecureServlet(ServletAssembly.class, 		"/cfw/assembly"); 
		app.addUnsecureServlet(ServletJARResource.class, 	"/cfw/jarresource");
		app.addUnsecureServlet(ServletShutdown.class, 		"/cfw/shutdown");
	}

	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public void startTasks() {
		
		//-------------------------------
		// Create Change Listener
		ConfigChangeListener listener = new ConfigChangeListener(
				FeatureConfig.CONFIG_DB_BACKUP_ENABLED,
				FeatureConfig.CONFIG_DB_BACKUP_INTERVAL,
				FeatureConfig.CONFIG_DB_BACKUP_TIME
			) {
			
			@Override
			public void onChange() {
				TaskDatabaseBackup.setupTask();
			}
		};
		
		CFW.DB.Config.addChangeListener(listener);
		
		//-------------------------------
		// Initialize Backup Task
		TaskDatabaseBackup.setupTask();
		
	}

	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public void stopFeature() {
		// nothing to do
	}
	
	
	/************************************************************************************
	 * Returns a map of keys and labels for the available chart types.
	 * 
	 ************************************************************************************/
	public static LinkedHashMap<String, String> getChartTypes() {
		LinkedHashMap<String, String> clone = new LinkedHashMap<>();
		clone.putAll(CHART_TYPES);
		return clone;
	}
	
	

}
