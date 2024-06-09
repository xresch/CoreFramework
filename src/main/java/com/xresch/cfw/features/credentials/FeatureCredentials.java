package com.xresch.cfw.features.credentials;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.concurrent.ScheduledFuture;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.config.ConfigChangeListener;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.parameter.FeatureParameter;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.response.bootstrap.DynamicItemCreator;
import com.xresch.cfw.response.bootstrap.HierarchicalHTMLItem;
import com.xresch.cfw.response.bootstrap.MenuItem;
import com.xresch.cfw.spi.CFWAppFeature;
import com.xresch.cfw.utils.CFWTime.CFWTimeUnit;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureCredentials extends CFWAppFeature {
	
	public static final String URI_DASHBOARD_LIST = "/app/credentials/list";
	public static final String URI_DASHBOARD_VIEW = "/app/credentials/view";
	public static final String URI_DASHBOARD_VIEW_PUBLIC = "/public/credentials/view";
	
	public static final String PERMISSION_CREDENTIALS_VIEWER = "Credentials Viewer";
	public static final String PERMISSION_CREDENTIALS_CREATOR = "Credentials Creator";
	public static final String PERMISSION_CREDENTIALS_CREATOR_PUBLIC = "Credentials Creator Public";
	public static final String PERMISSION_CREDENTIALS_ADMIN = "Credentials Admin";
	public static final String PERMISSION_CREDENTIALS_TASKS = "Credentials Tasks";
	public static final String PERMISSION_CREDENTIALS_FAST_RELOAD = "Credentials Fast Reload";
	
	public static final String CONFIG_CATEGORY = "Credentials";
	public static final String CONFIG_DEFAULT_IS_SHARED = "Default Is Shared";

	
	public static final String PACKAGE_RESOURCES = "com.xresch.cfw.features.credentials.resources";
	public static final String PACKAGE_MANUAL = "com.xresch.cfw.features.credentials.manual";
	
	public static final String EAV_STATS_CATEGORY = "CredentialsStats";
	public static final String EAV_STATS_PAGE_LOADS = "Page Loads";
	public static final String EAV_STATS_PAGE_LOADS_AND_REFRESHES = "Page Loads And Refreshes";
	public static final String EAV_STATS_WIDGET_LOADS_CACHED = "Widget Loads Cached";
	public static final String EAV_STATS_WIDGET_LOADS_UNCACHED = "Widget Loads Not Cached";
	
	public static final String WIDGET_CATEGORY_ADVANCED = "Advanced";
	public static final String WIDGET_CATEGORY_EASTEREGGS = "Eastereggs";
	public static final String WIDGET_CATEGORY_STANDARD = "Standard";
	
	public static final String MANUAL_NAME_DASHBOARD = "Credentials";
	public static final String MANUAL_NAME_WIDGETS = "Widgets";
	public static final String MANUAL_PATH_WIDGETS = MANUAL_NAME_DASHBOARD+"|"+MANUAL_NAME_WIDGETS;
	
	private static ScheduledFuture<?> taskCreateVersions;
	
	public static final ManualPage MANUAL_PAGE_ROOT = CFW.Registry.Manual.addManualPage(null, 
					new ManualPage(MANUAL_NAME_DASHBOARD)
						.faicon("fas fa-tachometer-alt")
						.addPermission(PERMISSION_CREDENTIALS_VIEWER)
						.addPermission(PERMISSION_CREDENTIALS_CREATOR)
						.addPermission(PERMISSION_CREDENTIALS_ADMIN)
				);
	
	
	
	public static final ManualPage MANUAL_PAGE_WIDGETS = MANUAL_PAGE_ROOT.addChild( 
			new ManualPage(MANUAL_NAME_WIDGETS)
				.faicon("fas fa-th")
				.addPermission(PERMISSION_CREDENTIALS_VIEWER)
				.addPermission(PERMISSION_CREDENTIALS_CREATOR)
				.addPermission(PERMISSION_CREDENTIALS_ADMIN)
				.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "&nbsp;"))
			;
	
	
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCES);
		CFW.Files.addAllowedPackage(PACKAGE_MANUAL);
		
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
				(MenuItem)new MenuItem("Credentialss")
					.faicon("fas fa-tachometer-alt")
					.addPermission(PERMISSION_CREDENTIALS_VIEWER)
					.addPermission(PERMISSION_CREDENTIALS_CREATOR)
					.addPermission(PERMISSION_CREDENTIALS_ADMIN)
					.href("/app/credentials/list")
					.addAttribute("id", "cfwMenuTools-Credentialss")
				, null);
		
			//----------------------------------
	    	// Register Menus
			MenuItem favoritesMenu = (MenuItem)new MenuItem("Favorites")
				.addPermission(PERMISSION_CREDENTIALS_VIEWER)
				.addPermission(PERMISSION_CREDENTIALS_CREATOR)
				.addPermission(PERMISSION_CREDENTIALS_ADMIN)
				.addAttribute("id", "cfwMenuButtons-Favorites")
				.setDynamicCreator(new DynamicItemCreator() {		
		
					@Override
					public ArrayList<HierarchicalHTMLItem> createDynamicItems() {
						
						ArrayList<HierarchicalHTMLItem> childitems = new ArrayList<HierarchicalHTMLItem>();
						ArrayList<CFWCredentials> credentialsList = CFW.DB.Credentials.getFavedCredentialsList();
						
						//-------------------------
						// Handle no Faves
						if(credentialsList.isEmpty()) {
							childitems.add(
									new MenuItem("No Favorites")
								);
							return childitems;
						}
						
						for(CFWCredentials current : credentialsList) {

							childitems.add(
								(MenuItem)new MenuItem(current.name())
									.noIconSpace(true)
									.href(URI_DASHBOARD_VIEW+ "?id="+current.id()) 	
							);
						}
						return childitems;
					}
				});
			
			favoritesMenu.faicon("fas fa-star");
		
		CFW.Registry.Components.addButtonsMenuItem(favoritesMenu, null);
		
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
		CFW.Localization.registerLocaleFile(locale, URI_DASHBOARD_VIEW_PUBLIC, definition);
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
				new Permission(PERMISSION_CREDENTIALS_CREATOR_PUBLIC, FeatureUserManagement.CATEGORY_USER)
					.description("Additional permission for credentials creators to allow making public links for credentials."),
					true,
					false
				);	
		
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_CREDENTIALS_ADMIN, FeatureUserManagement.CATEGORY_USER)
					.description("View, Edit and Delete all credentials of all users, regardless of the share settings of the credentials."),
					true,
					false
				);	
		
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_CREDENTIALS_TASKS, FeatureUserManagement.CATEGORY_USER)
				.description("Add and edit tasks of widgets."),
				true,
				false
				);	
		
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_CREDENTIALS_FAST_RELOAD, FeatureUserManagement.CATEGORY_USER)
				.description("Gives the user the option to reload the credentials every minute."),
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
    	app.addAppServlet(ServletCredentialsList.class,  URI_DASHBOARD_LIST);

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
		//
		MANUAL_PAGE_ROOT.addChild(
				new ManualPage("Introduction")
					.faicon("fas fa-star")
					.addPermission(PERMISSION_CREDENTIALS_VIEWER)
					.addPermission(PERMISSION_CREDENTIALS_CREATOR)
					.addPermission(PERMISSION_CREDENTIALS_ADMIN)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_00_introduction.html")
			);
		
	}
	
	/*******************************************************************
	 * Returns the URL for the given Credentials ID or null if the
	 * servername was not defined in the cfw.properties
	 * @param credentialsID
	 *******************************************************************/
	public static String createURLForCredentials(int credentialsID) {
		return createURLForCredentials(""+credentialsID);
	}
	
	/*******************************************************************
	 * Returns the URL for the given Credentials ID or null if the
	 * servername was not defined in the cfw.properties
	 * @param credentialsID
	 *******************************************************************/
	public static String createURLForCredentials(String credentialsID) {
		
		if(CFW.Properties.APPLICATION_URL == null) {
			return null;
		}
		
		String appURL = CFW.Properties.APPLICATION_URL;
		if(appURL.endsWith("/")) {
			appURL = appURL.substring(0, appURL.length()-1);
		}
		
		return appURL + URI_DASHBOARD_VIEW + "?id="+credentialsID;
	}
	
}
