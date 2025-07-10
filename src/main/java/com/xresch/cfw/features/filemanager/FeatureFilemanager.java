package com.xresch.cfw.features.filemanager;

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
public class FeatureFilemanager extends CFWAppFeature {
	
	public static final String URI_STOREDFILE_LIST = "/app/storedfile";
	
	public static final String PERMISSION_STOREDFILE_VIEWER = "StoredFile: Viewer";
	public static final String PERMISSION_STOREDFILE_CREATOR = "StoredFile: Creator";
	public static final String PERMISSION_STOREDFILE_ADMIN = "StoredFile: Admin";
	
	public static final String CONFIG_CATEGORY = "StoredFile";
	public static final String CONFIG_DEFAULT_IS_SHARED = "Is Shared Default";

	
	public static final String PACKAGE_RESOURCES = "com.xresch.cfw.features.filemanager.resources";

	public static final String EAV_STATS_CATEGORY = "StoredFileStats";
	public static final String EAV_STATS_PAGE_LOADS = "Page Loads";
	public static final String EAV_STATS_PAGE_LOADS_AND_REFRESHES = "Page Loads And Refreshes";
	public static final String EAV_STATS_WIDGET_LOADS_CACHED = "Widget Loads Cached";
	public static final String EAV_STATS_WIDGET_LOADS_UNCACHED = "Widget Loads Not Cached";
	
	public static final String MANUAL_NAME_STOREDFILE = "StoredFile";

	public static ManualPage MANUAL_PAGE_ROOT;
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCES);
		
		//----------------------------------
		// Register Languages
		
		FileDefinition english = new FileDefinition(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "lang_en_storedfile.properties");
		registerLocale(Locale.ENGLISH, english);
		
		FileDefinition german = new FileDefinition(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "lang_de_storedfile.properties");
		registerLocale(Locale.GERMAN, german);
		
		
    	//----------------------------------
    	// Register Objects
		CFW.Registry.Objects.addCFWObject(CFWStoredFile.class);
		CFW.Registry.Objects.addCFWObject(CFWStoredFileSharedGroupsMap.class);
		CFW.Registry.Objects.addCFWObject(CFWStoredFileSharedUserMap.class);
		CFW.Registry.Objects.addCFWObject(CFWStoredFileEditorsMap.class);
		CFW.Registry.Objects.addCFWObject(CFWStoredFileEditorGroupsMap.class);
    	
		
		//----------------------------------
    	// Register Audit
		CFW.Registry.Audit.addUserAudit(new UserAuditExecutorStoredFileUserDirect());
		CFW.Registry.Audit.addUserAudit(new UserAuditExecutorStoredFileUserGroups());
				
		//----------------------------------
    	// Register Menu				
		CFW.Registry.Components.addToolsMenuItem(
				(CFWHTMLItemMenuItem)new CFWHTMLItemMenuItem("StoredFile")
					.faicon("fas fa-key")
					.addPermission(PERMISSION_STOREDFILE_VIEWER)
					.addPermission(PERMISSION_STOREDFILE_CREATOR)
					.addPermission(PERMISSION_STOREDFILE_ADMIN)
					.href(URI_STOREDFILE_LIST)
					.addAttribute("id", "cfwMenuTools-StoredFile")
				, null);
		
		//----------------------------------
    	// Manual
		createStoredFileManual();
		
	}

	
	/**********************************************************************************
	 * Registers a locale file for storedfile, public storedfile and manual.
	 * 
	 * @param locale
	 * @param definition
	 **********************************************************************************/
	public static void registerLocale(Locale locale, FileDefinition definition) {
		
		if(locale == null || definition == null) {
			return;
		}
		
		CFW.Localization.registerLocaleFile(locale, "/app/filemanager", definition);
		CFW.Localization.registerLocaleFile(locale, FeatureParameter.URI_PARAMETER, definition);
		CFW.Localization.registerLocaleFile(locale, FeatureManual.URI_MANUAL, definition);
	}
	
	@Override
	public void initializeDB() {

		//============================================================
		// PERMISSIONS
		//============================================================
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_STOREDFILE_VIEWER, FeatureUserManagement.CATEGORY_USER)
					.description("Can view Stored File that other users have shared. Cannot create Stored File, but might edit when allowed by a Stored File creator."),
					true,
					false
				);	
		
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_STOREDFILE_CREATOR, FeatureUserManagement.CATEGORY_USER)
					.description("Can view and create Stored File and share them with other users."),
					true,
					false
				);	
		
		
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_STOREDFILE_ADMIN, FeatureUserManagement.CATEGORY_USER)
					.description("View, Edit and Delete all Stored File of all users, regardless of the share settings of the Stored File."),
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
				.description("The default value for the Stored File setting 'Is Shared'.")
				.type(FormFieldType.BOOLEAN)
				.value("false")
		);
		

	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {
		
		//----------------------------------
    	// Servlets
    	app.addAppServlet(ServletFilemanager.class,  URI_STOREDFILE_LIST);
		
	}

	@Override
	public void startTasks() {
		// nothing to do
	}

	@Override
	public void stopFeature() {
		// nothing to do
		
	}
	
	private void createStoredFileManual() {
		
		//----------------------------------
    	// Register Manual Pages
		MANUAL_PAGE_ROOT = CFW.Registry.Manual.addManualPage(null, 
				new ManualPage(MANUAL_NAME_STOREDFILE)
					.faicon("fas fa-key")
					.addPermission(PERMISSION_STOREDFILE_VIEWER)
					.addPermission(PERMISSION_STOREDFILE_CREATOR)
					.addPermission(PERMISSION_STOREDFILE_ADMIN)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "manual_storedfile.html")
			);	

		
	}
	
}
