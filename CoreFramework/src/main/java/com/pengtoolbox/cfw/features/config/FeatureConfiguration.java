package com.pengtoolbox.cfw.features.config;

import java.util.concurrent.ScheduledFuture;

import org.eclipse.jetty.servlet.ServletContextHandler;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFWAppFeature;
import com.pengtoolbox.cfw._main.CFWApplicationExecutor;
import com.pengtoolbox.cfw.features.config.Configuration;
import com.pengtoolbox.cfw.features.usermgmt.Permission;
import com.pengtoolbox.cfw.features.usermgmt.Role;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.bootstrap.MenuItem;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class FeatureConfiguration extends CFWAppFeature {

	public static final String PERMISSION_CONFIGURATION = "Configuration Management";
	
	public static final String RESOURCE_PACKAGE = "com.pengtoolbox.cfw.features.config.resources";
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		//----------------------------------
		// Register Objects
		CFW.Registry.Objects.addCFWObject(Configuration.class);
    	
    	//----------------------------------
    	// Register Regular Menu
		
		CFW.Registry.Components.addAdminCFWMenuItem(
				(MenuItem)new MenuItem("Configuration", "{!cfw_core_configuration!}")
					.faicon("fas fa-cog")
					.addPermission(PERMISSION_CONFIGURATION)
					.href("/app/configuration")	
				, null);
		
	}

	@Override
	public void initializeDB() {
		
		Role adminRole = CFW.DB.Roles.selectFirstByName(CFW.DB.Roles.CFW_ROLE_ADMIN);
		//Role userRole = CFW.DB.Roles.selectFirstByName(CFW.DB.Roles.CFW_ROLE_USER);
		
		//-----------------------------------------
		// Config Management
		//-----------------------------------------
		if(!CFW.DB.Permissions.checkExistsByName(PERMISSION_CONFIGURATION)) {
			
			CFW.DB.Permissions.create(new Permission(PERMISSION_CONFIGURATION, "user")
				.description("Gives the user the ability to view and update the configurations in the database.")
			);
			
			Permission permission = CFW.DB.Permissions.selectByName(PERMISSION_CONFIGURATION);
			CFW.DB.RolePermissionMap.addPermissionToRole(permission, adminRole, true);

		}
		
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		app.addAppServlet(ServletConfiguration.class,  "/configuration");
	}

	@Override
	public void startTasks() {
	}

	@Override
	public void stopFeature() {
	}

}
