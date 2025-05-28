package com.xresch.cfw.features.query.store;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.spi.CFWAppFeature;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureStoredQuery extends CFWAppFeature {
	
	public static final String URI_STOREDQUERY_LIST = "/app/storedquery";
	
	public static final String PERMISSION_STOREDQUERY_VIEWER = "Query Store: Viewer";
	public static final String PERMISSION_STOREDQUERY_CREATOR = "Query Store: Creator";
	public static final String PERMISSION_STOREDQUERY_ADMIN = "Query Store: Admin";
	
	public static final String CONFIG_CATEGORY = "Query";
	public static final String CONFIG_DEFAULT_IS_SHARED = "Store Is Shared Default";

	
	public static final String PACKAGE_RESOURCES = "com.xresch.cfw.features.query.store.resources";

	public static final String EAV_STATS_CATEGORY = "StoredQueryStats";
	public static final String EAV_STATS_PAGE_EXECUTION_COUNT = "Execution_count";
	public static final String EAV_STATS_PAGE_EXECUTION_TIME = "execution_time";


	public static final String MANUAL_NAME_STOREDQUERY = "Query Store";

	public static ManualPage MANUAL_PAGE_ROOT;
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCES);
		
		//----------------------------------
		// Register Languages
		
//		FileDefinition english = new FileDefinition(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "lang_en_storedQuery.properties");
//		registerLocale(Locale.ENGLISH, english);
//		
//		FileDefinition german = new FileDefinition(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "lang_de_storedQuery.properties");
//		registerLocale(Locale.GERMAN, german);
		
		
    	//----------------------------------
    	// Register Objects
		CFW.Registry.Objects.addCFWObject(CFWStoredQuery.class);
		CFW.Registry.Objects.addCFWObject(CFWStoredQuerySharedGroupsMap.class);
		CFW.Registry.Objects.addCFWObject(CFWStoredQuerySharedUserMap.class);
		CFW.Registry.Objects.addCFWObject(CFWStoredQueryEditorsMap.class);
		CFW.Registry.Objects.addCFWObject(CFWStoredQueryEditorGroupsMap.class);
    	
		
		//----------------------------------
    	// Register Audit
		CFW.Registry.Audit.addUserAudit(new UserAuditExecutorStoredQueryUserDirect());
		CFW.Registry.Audit.addUserAudit(new UserAuditExecutorStoredQueryUserGroups());
						
		//----------------------------------
    	// Manual
		createStoredQueryManual();

	}

	
	
	@Override
	public void initializeDB() {

		//============================================================
		// PERMISSIONS
		//============================================================
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_STOREDQUERY_VIEWER, FeatureUserManagement.CATEGORY_USER)
					.description("Can view stored queries that other users have shared. Cannot create stored queries, but might edit when allowed by a stored query creator."),
					true,
					false
				);	
		
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_STOREDQUERY_CREATOR, FeatureUserManagement.CATEGORY_USER)
					.description("Can view and create stored queries and share them with other users."),
					true,
					false
				);	
		
		
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_STOREDQUERY_ADMIN, FeatureUserManagement.CATEGORY_USER)
					.description("View, edit and delete all stored queries of all users, regardless of the share settings of the stored queries."),
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
				.description("The default value for the stored queries setting 'Is Shared'.")
				.type(FormFieldType.BOOLEAN)
				.value("false")
		);
		

	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {
		
		//----------------------------------
    	// Servlets
    	app.addAppServlet(ServletStoredQueryList.class,  URI_STOREDQUERY_LIST);
		
		
		//----------------------------------
    	// Register Custom Widgets
    	CFW.DB.StoredQuery.fetchAndCacheWidgets();
		
	}

	@Override
	public void startTasks() {
		// nothing to do
	}

	@Override
	public void stopFeature() {
		// nothing to do
		
	}
	
	private void createStoredQueryManual() {
		
		//----------------------------------
    	// Register Manual Pages
		MANUAL_PAGE_ROOT = CFW.Registry.Manual.addManualPage(null, 
				new ManualPage(MANUAL_NAME_STOREDQUERY)
					.faicon("fas fa-key")
					.addPermission(PERMISSION_STOREDQUERY_VIEWER)
					.addPermission(PERMISSION_STOREDQUERY_CREATOR)
					.addPermission(PERMISSION_STOREDQUERY_ADMIN)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "manual_storedQuery.html")
			);	

		
	}
	
}
