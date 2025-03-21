package com.xresch.cfw.features.credentials;

import java.util.Locale;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.parameter.FeatureParameter;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemMenuItem;
import com.xresch.cfw.spi.CFWAppFeature;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureCredentials extends CFWAppFeature {
	
	public static final String URI_CREDENTIALS_LIST = "/app/credentials";
	
	public static final String PERMISSION_CREDENTIALS_VIEWER = "Credentials: Viewer";
	public static final String PERMISSION_CREDENTIALS_CREATOR = "Credentials: Creator";
	public static final String PERMISSION_CREDENTIALS_ADMIN = "Credentials: Admin";
	
	public static final String CONFIG_CATEGORY = "Credentials";
	public static final String CONFIG_DEFAULT_IS_SHARED = "Is Shared Default";

	
	public static final String PACKAGE_RESOURCES = "com.xresch.cfw.features.credentials.resources";

	public static final String EAV_STATS_CATEGORY = "CredentialsStats";
	public static final String EAV_STATS_PAGE_LOADS = "Page Loads";
	public static final String EAV_STATS_PAGE_LOADS_AND_REFRESHES = "Page Loads And Refreshes";
	public static final String EAV_STATS_WIDGET_LOADS_CACHED = "Widget Loads Cached";
	public static final String EAV_STATS_WIDGET_LOADS_UNCACHED = "Widget Loads Not Cached";
	
	public static final String MANUAL_NAME_CREDENTIALS = "Credentials";

	public static ManualPage MANUAL_PAGE_ROOT;
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCES);
		
		//----------------------------------
		// Register Languages
		
		FileDefinition english = new FileDefinition(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "lang_en_credentials.properties");
		registerLocale(Locale.ENGLISH, english);
		
		FileDefinition german = new FileDefinition(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "lang_de_credentials.properties");
		registerLocale(Locale.GERMAN, german);
		
		
    	//----------------------------------
    	// Register Objects
		CFW.Registry.Objects.addCFWObject(CFWCredentials.class);
		CFW.Registry.Objects.addCFWObject(CFWCredentialsSharedGroupsMap.class);
		CFW.Registry.Objects.addCFWObject(CFWCredentialsSharedUserMap.class);
		CFW.Registry.Objects.addCFWObject(CFWCredentialsEditorsMap.class);
		CFW.Registry.Objects.addCFWObject(CFWCredentialsEditorGroupsMap.class);
    	
		
		//----------------------------------
    	// Register Audit
		CFW.Registry.Audit.addUserAudit(new UserAuditExecutorCredentialsUserDirect());
		CFW.Registry.Audit.addUserAudit(new UserAuditExecutorCredentialsUserGroups());
				
		//----------------------------------
    	// Register Menu				
		CFW.Registry.Components.addToolsMenuItem(
				(CFWHTMLItemMenuItem)new CFWHTMLItemMenuItem("Credentials")
					.faicon("fas fa-key")
					.addPermission(PERMISSION_CREDENTIALS_VIEWER)
					.addPermission(PERMISSION_CREDENTIALS_CREATOR)
					.addPermission(PERMISSION_CREDENTIALS_ADMIN)
					.href(URI_CREDENTIALS_LIST)
					.addAttribute("id", "cfwMenuTools-Credentials")
				, null);
		
	
		
	}

	
	/**********************************************************************************
	 * Registers a locale file for credentials, public credentials and manual.
	 * 
	 * @param locale
	 * @param definition
	 **********************************************************************************/
	public static void registerLocale(Locale locale, FileDefinition definition) {
		
		if(locale == null || definition == null) {
			return;
		}
		
		CFW.Localization.registerLocaleFile(locale, "/app/credentials", definition);
		CFW.Localization.registerLocaleFile(locale, FeatureParameter.URI_PARAMETER, definition);
		CFW.Localization.registerLocaleFile(locale, FeatureManual.URI_MANUAL, definition);
	}
	
	@Override
	public void initializeDB() {

		//============================================================
		// PERMISSIONS
		//============================================================
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_CREDENTIALS_VIEWER, FeatureUserManagement.CATEGORY_USER)
					.description("Can view credentials that other users have shared. Cannot create credentials, but might edit when allowed by a credentials creator."),
					true,
					false
				);	
		
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_CREDENTIALS_CREATOR, FeatureUserManagement.CATEGORY_USER)
					.description("Can view and create credentials and share them with other users."),
					true,
					false
				);	
		
		
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_CREDENTIALS_ADMIN, FeatureUserManagement.CATEGORY_USER)
					.description("View, Edit and Delete all credentials of all users, regardless of the share settings of the credentials."),
					true,
					false
				);	
		
		
		//============================================================
		// CONFIGURATION
		//============================================================
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration(CONFIG_CATEGORY, CONFIG_DEFAULT_IS_SHARED)
				.description("The default value for the credentials setting 'Is Shared'.")
				.type(FormFieldType.BOOLEAN)
				.value("false")
		);
		

	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {
		
		//----------------------------------
    	// Servlets
    	app.addAppServlet(ServletCredentialsList.class,  URI_CREDENTIALS_LIST);

    	//----------------------------------
    	// Manual
		createCredentialsManual();
		
	}

	@Override
	public void startTasks() {
		// nothing to do
	}

	@Override
	public void stopFeature() {
		// nothing to do
		
	}
	
	private void createCredentialsManual() {
		
		//----------------------------------
    	// Register Manual Pages
		MANUAL_PAGE_ROOT = CFW.Registry.Manual.addManualPage(null, 
				new ManualPage(MANUAL_NAME_CREDENTIALS)
					.faicon("fas fa-key")
					.addPermission(PERMISSION_CREDENTIALS_VIEWER)
					.addPermission(PERMISSION_CREDENTIALS_CREATOR)
					.addPermission(PERMISSION_CREDENTIALS_ADMIN)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "manual_credentials.html")
			);	

		
	}
	
}
