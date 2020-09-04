package com.xresch.cfw.features.core;

import java.util.Locale;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw._main.CFWProperties;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.db.TaskDatabaseBackup;
import com.xresch.cfw.features.config.ConfigChangeListener;
import com.xresch.cfw.features.config.FeatureConfiguration;
import com.xresch.cfw.features.core.auth.ServletChangePassword;
import com.xresch.cfw.features.core.auth.ServletLogin;
import com.xresch.cfw.features.core.auth.ServletLogout;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.response.bootstrap.MenuItem;

import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.BufferPoolsExports;
import io.prometheus.client.hotspot.ClassLoadingExports;
import io.prometheus.client.hotspot.GarbageCollectorExports;
import io.prometheus.client.hotspot.MemoryAllocationExports;
import io.prometheus.client.hotspot.MemoryPoolsExports;
import io.prometheus.client.hotspot.StandardExports;
import io.prometheus.client.hotspot.ThreadExports;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureCore extends CFWAppFeature {

	public static final String RESOURCE_PACKAGE = "com.xresch.cfw.features.core.resources";
	public static final String PERMISSION_APP_ANALYTICS = "System Analytics";
	public static final String PERMISSION_ALLOW_HTML = "Allow HTML";
	public static final String PERMISSION_ALLOW_JAVASCRIPT = "Allow Javascript";
	
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
				(MenuItem)new MenuItem("System Analytics")
					.faicon("fas fa-traffic-light")
					.addPermission(FeatureCore.PERMISSION_APP_ANALYTICS)	
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
		new Permission(PERMISSION_APP_ANALYTICS, "user")
			.description("Analyze the application status with tools like cpu sampling."),
			true,
			false
		);	

		//-----------------------------------
		// 
		CFW.DB.Permissions.oneTimeCreate(
		new Permission(PERMISSION_ALLOW_HTML, "user")
			.description("Allow the user to enter HTML code in any of the text fields. As this is a potential security issue, handle this permission with care."),
			false,
			false
		);	
		//-----------------------------------
		// 
		CFW.DB.Permissions.oneTimeCreate(
		new Permission(PERMISSION_ALLOW_JAVASCRIPT, "user")
			.description("Allow the user to enter javascript code in any of the text fields. As this is a potential security issue, handle this permission with care. This permission requires 'Allow HTML' to work."),
			false,
			false
		);	
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		
		//-----------------------------------------
		// Authentication Servlets
	    if(CFWProperties.AUTHENTICATION_ENABLED) {
	    	app.addAppServlet(ServletLogin.class, "/login");
	        app.addAppServlet(ServletLogout.class,  "/logout");
	    }
	  
		//-----------------------------------------
		// User Profile Servlets
	    app.addAppServlet(ServletChangePassword.class,  "/changepassword");
	    
		
	    //-----------------------------------------
		// Other Servlets
		app.addUnsecureServlet(ServletLocalization.class,  	"/cfw/locale");
		app.addUnsecureServlet(ServletFormHandler.class,	"/cfw/formhandler");
		app.addUnsecureServlet(ServletAutocomplete.class,  	"/cfw/autocomplete");
		app.addUnsecureServlet(ServletAssembly.class, 		"/cfw/assembly"); 
		app.addUnsecureServlet(ServletJARResource.class, 	"/cfw/jarresource");
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
