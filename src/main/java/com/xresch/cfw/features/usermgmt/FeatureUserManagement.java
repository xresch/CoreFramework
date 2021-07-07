package com.xresch.cfw.features.usermgmt;

import java.util.Locale;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.response.bootstrap.MenuItem;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureUserManagement extends CFWAppFeature {

	public static final String PERMISSION_CPU_SAMPLING = "CPU Sampling";
	public static final String RESOURCE_PACKAGE = "com.xresch.cfw.features.usermgmt.resources";
	
	public static final String CONFIG_SESSIONTIMEOUT_USERS = "Session Timout Users";
	public static final String CONFIG_SESSIONTIMEOUT_VISITORS = "Session Timout Visitors";
	public static final String CONFIG_SESSIONTIMEOUT_API = "Session Timout API";
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		
		//----------------------------------
		// Register Languages
		CFW.Localization.registerLocaleFile(Locale.ENGLISH, "/app/usermanagement", new FileDefinition(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "lang_en.properties"));
		CFW.Localization.registerLocaleFile(Locale.GERMAN, "/app/usermanagement", new FileDefinition(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "lang_de.properties"));
		
		//----------------------------------
		// Register Objects
		CFW.Registry.Objects.addCFWObject(User.class);
		CFW.Registry.Objects.addCFWObject(Role.class);
		CFW.Registry.Objects.addCFWObject(Permission.class);
		CFW.Registry.Objects.addCFWObject(UserRoleMap.class);
		CFW.Registry.Objects.addCFWObject(RolePermissionMap.class);
    	
		//----------------------------------
    	// Register Audit
		CFW.Registry.Audit.addUserAudit(new UserAuditExecutorPermissions());
		CFW.Registry.Audit.addUserAudit(new UserAuditExecutorGroups());
		
    	//----------------------------------
    	// Register Regular Menu
		CFW.Registry.Components.addAdminCFWMenuItem(
				(MenuItem)new MenuItem("Manage Users", "{!cfw_core_manage_users!}") 
					.faicon("fas fa-users")
					.addPermission(Permission.CFW_USER_MANAGEMENT)
					.href("/app/usermanagement")	
				, null);
		
	}

	@Override
	public void initializeDB() {
    	//----------------------------------
    	// Register Configurations
		CFW.DB.Config.oneTimeCreate(
				new Configuration("Timeouts", CONFIG_SESSIONTIMEOUT_USERS)
					.description("The session timeout in seconds for logged in users. Changes will be applied to active sessions on the next request.")
					.type(FormFieldType.NUMBER)
					.value("36000")
			);
		
		CFW.DB.Config.oneTimeCreate(
				new Configuration("Timeouts", CONFIG_SESSIONTIMEOUT_VISITORS)
					.description("The session timeout in seconds for users that are not logged in. Changes will be applied to active sessions on the next request.")
					.type(FormFieldType.NUMBER)
					.value("600")
			);
		
		CFW.DB.Config.oneTimeCreate(
				new Configuration("Timeouts", CONFIG_SESSIONTIMEOUT_API)
					.description("The session timeout in seconds for API related calls without login. ")
					.type(FormFieldType.NUMBER)
					.value("10")
			);
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		app.addAppServlet(ServletUserManagement.class,  "/usermanagement");
		app.addAppServlet(ServletPermissions.class,  "/usermanagement/permissions");
		app.addAppServlet(SevletUserManagementAPI.class, "/usermanagement/data"); 
	}

	@Override
	public void startTasks() { /* nothing to do */ }

	@Override
	public void stopFeature() { /* nothing to do */ }

}
