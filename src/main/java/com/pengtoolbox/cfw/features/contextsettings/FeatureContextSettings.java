package com.pengtoolbox.cfw.features.contextsettings;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFWAppFeature;
import com.pengtoolbox.cfw._main.CFWApplicationExecutor;
import com.pengtoolbox.cfw.features.usermgmt.Permission;
import com.pengtoolbox.cfw.response.bootstrap.MenuItem;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class FeatureContextSettings extends CFWAppFeature {
	
	public static final String RESOURCE_PACKAGE = "com.pengtoolbox.cfw.features.contextsettings.resources";
	public static final String PERMISSION_CONTEXT_SETTINGS = "Context Settings";
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		
		//----------------------------------
		// Register Objects
		CFW.Registry.Objects.addCFWObject(ContextSettings.class);

    	//----------------------------------
    	// Register Menus
		
    	//----------------------------------
    	// Register Regular Menu
		
		CFW.Registry.Components.addAdminCFWMenuItem(
				(MenuItem)new MenuItem("Context Settings", "{!cfw_core_contextsettings!}")
					.faicon("fas fa-cogs")
					.addPermission(PERMISSION_CONTEXT_SETTINGS)
					.href("/app/contextsettings")	
				, null);
							
	}

	@Override
	public void initializeDB() {
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_CONTEXT_SETTINGS, "user")
					.description("This permission allows a user to manage context settings."),
				true,
				false);
							
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
        app.addAppServlet(ServletContextSettings.class,  "/contextsettings");
	}

	@Override
	public void startTasks() {

	}

	@Override
	public void stopFeature() {

	}

}
