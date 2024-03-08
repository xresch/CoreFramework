package com.xresch.cfw.features.usermgmt;

import java.util.Locale;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.features.config.FeatureConfig;
import com.xresch.cfw.response.bootstrap.MenuItem;
import com.xresch.cfw.spi.CFWAppFeature;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureUserManagement extends CFWAppFeature {

	
	public static final String PERMISSION_CPU_SAMPLING = "CPU Sampling";
	public static final String RESOURCE_PACKAGE = "com.xresch.cfw.features.usermgmt.resources";
	
	public static final String CONFIG_CATEGORY_PW_POLICY = "Password Policy";
	
	public static final String CONFIG_PWPOLICY_ISENABLED = "Enable Password Policy";
	public static final String CONFIG_PWPOLICY_MINLENGTH = "Minimum Length";
	public static final String CONFIG_PWPOLICY_UPPERCASE = "Uppercase Check";
	public static final String CONFIG_PWPOLICY_LOWERCASE = "Lowercase Check";
	public static final String CONFIG_PWPOLICY_NUMBER = "Number Check";
	public static final String CONFIG_PWPOLICY_SPECIAL = " Special Character";
	public static final String CONFIG_PWPOLICY_SPECIALORNUM = " Special Or Number";
	
	public static final String CATEGORY_USER = "user";
	
	public static final String PERMISSION_USER_MANAGEMENT = "User Management";
	
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
		CFW.Registry.Objects.addCFWObject(RoleEditorsMap.class);
    	
		//----------------------------------
    	// Register Audit
		CFW.Registry.Audit.addUserAudit(new UserAuditExecutorPermissions());
		CFW.Registry.Audit.addUserAudit(new UserAuditExecutorGroups());
		
    	//----------------------------------
    	// Register Regular Menu
		CFW.Registry.Components.addAdminCFWMenuItem(
				(MenuItem)new MenuItem("Manage Users", "{!cfw_core_manage_users!}") 
					.faicon("fas fa-users")
					.addPermission(FeatureUserManagement.PERMISSION_USER_MANAGEMENT)
					.href("/app/usermanagement")
					.addAttribute("id", "cfwMenuAdmin-UserMgmt")
				, null);
		
	}

	@Override
	public void initializeDB() {
    	//----------------------------------
    	// Register Timeout Configurations
		CFW.DB.Config.oneTimeCreate(
				new Configuration(FeatureConfig.CATEGORY_TIMEOUTS, FeatureConfig.CONFIG_SESSIONTIMEOUT_USERS)
					.description("The session timeout in seconds for logged in users. Changes will be applied to active sessions on the next request.")
					.type(FormFieldType.NUMBER)
					.value("36000")
			);
		
		CFW.DB.Config.oneTimeCreate(
				new Configuration(FeatureConfig.CATEGORY_TIMEOUTS, FeatureConfig.CONFIG_SESSIONTIMEOUT_VISITORS)
					.description("The session timeout in seconds for users that are not logged in. Changes will be applied to active sessions on the next request.")
					.type(FormFieldType.NUMBER)
					.value("600")
			);
		
		CFW.DB.Config.oneTimeCreate(
				new Configuration(FeatureConfig.CATEGORY_TIMEOUTS, FeatureConfig.CONFIG_SESSIONTIMEOUT_API)
					.description("The session timeout in seconds for API related calls without login. ")
					.type(FormFieldType.NUMBER)
					.value("10")
			);
		
    	//----------------------------------
    	// Register Password Policy Configurations
		CFW.DB.Config.oneTimeCreate(
				new Configuration(CONFIG_CATEGORY_PW_POLICY, CONFIG_PWPOLICY_ISENABLED)
				.description("Enable or disable checks of passwords. If disabled only checks if the password is not empty.")
				.type(FormFieldType.BOOLEAN)
				.value("true")
			);
		
		CFW.DB.Config.oneTimeCreate(
				new Configuration(CONFIG_CATEGORY_PW_POLICY, CONFIG_PWPOLICY_MINLENGTH)
				.description("Choose the minimum length of the password.")
				.type(FormFieldType.NUMBER)
				.value("4")
			);
		
		CFW.DB.Config.oneTimeCreate(
				new Configuration(CONFIG_CATEGORY_PW_POLICY, CONFIG_PWPOLICY_LOWERCASE)
				.description("Toggle if the password must include a lowercase letter.")
				.type(FormFieldType.BOOLEAN)
				.value("false")
			);
		
		CFW.DB.Config.oneTimeCreate(
				new Configuration(CONFIG_CATEGORY_PW_POLICY, CONFIG_PWPOLICY_UPPERCASE)
				.description("Toggle if the password must include an uppercase letter.")
				.type(FormFieldType.BOOLEAN)
				.value("false")
			);
		
		CFW.DB.Config.oneTimeCreate(
				new Configuration(CONFIG_CATEGORY_PW_POLICY, CONFIG_PWPOLICY_NUMBER)
				.description("Toggle if the password must include a number.")
				.type(FormFieldType.BOOLEAN)
				.value("false")
			);
		
		CFW.DB.Config.oneTimeCreate(
				new Configuration(CONFIG_CATEGORY_PW_POLICY, CONFIG_PWPOLICY_SPECIAL)
				.description("Toggle if the password must include a special character.")
				.type(FormFieldType.BOOLEAN)
				.value("false")
			);
		
		CFW.DB.Config.oneTimeCreate(
				new Configuration(CONFIG_CATEGORY_PW_POLICY, CONFIG_PWPOLICY_SPECIALORNUM)
				.description("Toggle if the password must include either a special character or number. If this is true, ignores the other two settings.")
				.type(FormFieldType.BOOLEAN)
				.value("false")
				);
		
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		
		app.addAppServlet(ServletUserManagement.class,  "/usermanagement");
		app.addAppServlet(ServletPermissions.class,  "/usermanagement/permissions");
		app.addAppServlet(ServletUserManagementAPI.class, "/usermanagement/data"); 
		
		//----------------------------------
    	// Cleanup Expired Sessions to 
		// prevent warning logs
		CFW.DB.getDBInterface().preparedExecute(
			"DELETE "
		  + "FROM CFW_JETTY_SESSIONS "
		  + "WHERE DATEADD(SECOND, EXPIRY_TIME / 1000, DATE '1970-01-01') < CURRENT_TIMESTAMP()" 
		);
	}

	@Override
	public void startTasks() { /* nothing to do */ }

	@Override
	public void stopFeature() { /* nothing to do */ }

}
