package com.xresch.cfw.features.core;

import java.util.Locale;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw._main.CFWProperties;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.db.TaskDatabaseBackup;
import com.xresch.cfw.features.analytics.FeatureSystemAnalytics;
import com.xresch.cfw.features.config.ConfigChangeListener;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.features.config.FeatureConfiguration;
import com.xresch.cfw.features.core.auth.ServletChangePassword;
import com.xresch.cfw.features.core.auth.ServletLogin;
import com.xresch.cfw.features.core.auth.ServletLogout;
import com.xresch.cfw.features.core.auth.ServletSAML2AssertionConsumerService;
import com.xresch.cfw.features.core.auth.ServletSAML2Login;
import com.xresch.cfw.features.core.auth.ServletSAML2Metadata;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.response.bootstrap.MenuItem;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureCore extends CFWAppFeature {

	public static final String RESOURCE_PACKAGE = "com.xresch.cfw.features.core.resources";
		
	public static final String PERMISSION_FEATURE_MGMT = "Feature Management";
	public static final String PERMISSION_ALLOW_HTML = "Allow HTML";
	public static final String PERMISSION_ALLOW_JAVASCRIPT = "Allow Javascript";
	
	public static final String CONFIG_BROWSER_RESOURCE_MAXAGE = "Browser Resource Max Age";
	
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
		// Register Objects
		//CFW.Registry.Objects.addCFWObject(Configuration.class);
    			
    	//----------------------------------
    	// Register Admin Menu
		CFW.Registry.Components.addAdminCFWMenuItem(
				(MenuItem)new MenuItem("Feature Management")
					.faicon("fas fa-star")
					.addPermission(FeatureCore.PERMISSION_FEATURE_MGMT)	
					.href("/app/featuremanagement")
				, null);
				
	}

	@Override
	public void initializeDB() {

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
				new Configuration("Timeouts", CONFIG_BROWSER_RESOURCE_MAXAGE)
					.description("The maximum time in seconds resources(js, css, images etc...) should be cached in the client's browser.")
					.type(FormFieldType.NUMBER)
					.value("36000")
			);
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		
		//-----------------------------------------
		// Authentication Servlets
	    if(CFWProperties.AUTHENTICATION_ENABLED) {
	    	app.addAppServlet(ServletLogin.class, "/login");
	        app.addAppServlet(ServletLogout.class,  "/logout");
	        
	        if(CFWProperties.AUTHENTICATION_SAML2_ENABLED) {
	        	app.addUnsecureServlet(ServletSAML2Metadata.class,	"/cfw/saml2/metadata");
	        	app.addUnsecureServlet(ServletSAML2Login.class,	"/cfw/saml2/login");
	        	app.addUnsecureServlet(ServletSAML2AssertionConsumerService.class,	"/cfw/saml2/acs");
	        }
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

	@Override
	public void startTasks() {
		
		//-------------------------------
		// Create Change Listener
		ConfigChangeListener listener = new ConfigChangeListener(
				FeatureConfiguration.CONFIG_BACKUP_DB_ENABLED,
				FeatureConfiguration.CONFIG_BACKUP_DB_INTERVAL,
				FeatureConfiguration.CONFIG_BACKUP_DB_TIME
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

	@Override
	public void stopFeature() {
		// nothing to do
	}

}
