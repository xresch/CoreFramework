package com.xresch.cfw.features.dashboard;

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
import com.xresch.cfw.features.dashboard.widgets.ManualPageWidget;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetDefaultRefresh;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetForceRefresh;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetJavascript;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetParameter;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetPyConfig;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetPyScript;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetReplica;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetThresholdLegend;
import com.xresch.cfw.features.dashboard.widgets.eastereggs.WidgetEasterEggsDiscoMode;
import com.xresch.cfw.features.dashboard.widgets.eastereggs.WidgetEasterEggsFireworks;
import com.xresch.cfw.features.dashboard.widgets.eastereggs.WidgetEasterEggsLightSwitch;
import com.xresch.cfw.features.dashboard.widgets.eastereggs.WidgetEasterEggsSnow;
import com.xresch.cfw.features.dashboard.widgets.eastereggs.WidgetHelloWorld;
import com.xresch.cfw.features.dashboard.widgets.standard.WidgetChecklist;
import com.xresch.cfw.features.dashboard.widgets.standard.WidgetHTMLEditor;
import com.xresch.cfw.features.dashboard.widgets.standard.WidgetImage;
import com.xresch.cfw.features.dashboard.widgets.standard.WidgetLabel;
import com.xresch.cfw.features.dashboard.widgets.standard.WidgetList;
import com.xresch.cfw.features.dashboard.widgets.standard.WidgetRefreshTime;
import com.xresch.cfw.features.dashboard.widgets.standard.WidgetTable;
import com.xresch.cfw.features.dashboard.widgets.standard.WidgetTags;
import com.xresch.cfw.features.dashboard.widgets.standard.WidgetText;
import com.xresch.cfw.features.dashboard.widgets.standard.WidgetWebsite;
import com.xresch.cfw.features.dashboard.widgets.standard.WidgetYoutubeVideo;
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
public class FeatureDashboard extends CFWAppFeature {
	
	public static final String URI_DASHBOARD_LIST = "/app/dashboard/list";
	public static final String URI_DASHBOARD_VIEW = "/app/dashboard/view";
	public static final String URI_DASHBOARD_VIEW_PUBLIC = "/public/dashboard/view";
	
	public static final String PERMISSION_DASHBOARD_VIEWER = "Dashboard Viewer";
	public static final String PERMISSION_DASHBOARD_CREATOR = "Dashboard Creator";
	public static final String PERMISSION_DASHBOARD_CREATOR_PUBLIC = "Dashboard Creator Public";
	public static final String PERMISSION_DASHBOARD_ADMIN = "Dashboard Admin";
	public static final String PERMISSION_DASHBOARD_TASKS = "Dashboard Tasks";
	public static final String PERMISSION_DASHBOARD_FAST_RELOAD = "Dashboard Fast Reload";
	
	public static final String CONFIG_CATEGORY = "Dashboard";
	public static final String CONFIG_DEFAULT_IS_SHARED = "Default Is Shared";
	public static final String CONFIG_AUTO_VERSIONS = "Auto Versions";
	public static final String CONFIG_AUTO_VERSIONS_AGE = "Auto Versions Age";
	
	public static final String PACKAGE_RESOURCES = "com.xresch.cfw.features.dashboard.resources";
	public static final String PACKAGE_MANUAL = "com.xresch.cfw.features.dashboard.manual";
	
	public static final String EAV_STATS_CATEGORY = "DashboardStats";
	public static final String EAV_STATS_PAGE_LOADS = "Page Loads";
	public static final String EAV_STATS_PAGE_LOADS_AND_REFRESHES = "Page Loads And Refreshes";
	public static final String EAV_STATS_WIDGET_LOADS_CACHED = "Widget Loads Cached";
	public static final String EAV_STATS_WIDGET_LOADS_UNCACHED = "Widget Loads Not Cached";
	
	public static final String WIDGET_CATEGORY_ADVANCED = "Advanced";
	public static final String WIDGET_CATEGORY_EASTEREGGS = "Eastereggs";
	public static final String WIDGET_CATEGORY_STANDARD = "Standard";
	
	public static final String MANUAL_NAME_DASHBOARD = "Dashboard";
	public static final String MANUAL_NAME_WIDGETS = "Widgets";
	public static final String MANUAL_PATH_WIDGETS = MANUAL_NAME_DASHBOARD+"|"+MANUAL_NAME_WIDGETS;
	
	private static ScheduledFuture<?> taskCreateVersions;
	
	public static final ManualPage MANUAL_PAGE_ROOT = CFW.Registry.Manual.addManualPage(null, 
					new ManualPage(MANUAL_NAME_DASHBOARD)
						.faicon("fas fa-tachometer-alt")
						.addPermission(PERMISSION_DASHBOARD_VIEWER)
						.addPermission(PERMISSION_DASHBOARD_CREATOR)
						.addPermission(PERMISSION_DASHBOARD_ADMIN)
				);
	
	
	
	public static final ManualPage MANUAL_PAGE_WIDGETS = MANUAL_PAGE_ROOT.addChild( 
			new ManualPage(MANUAL_NAME_WIDGETS)
				.faicon("fas fa-th")
				.addPermission(PERMISSION_DASHBOARD_VIEWER)
				.addPermission(PERMISSION_DASHBOARD_CREATOR)
				.addPermission(PERMISSION_DASHBOARD_ADMIN)
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
		
		FileDefinition english = new FileDefinition(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "lang_en_dashboard.properties");
		registerLocale(Locale.ENGLISH, english);
		
		FileDefinition german = new FileDefinition(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "lang_de_dashboard.properties");
		registerLocale(Locale.GERMAN, german);
		
		
    	//----------------------------------
    	// Register Objects
		CFW.Registry.Objects.addCFWObject(Dashboard.class);
		CFW.Registry.Objects.addCFWObject(DashboardWidget.class);
		CFW.Registry.Objects.addCFWObject(DashboardFavoritesMap.class);
		CFW.Registry.Objects.addCFWObject(DashboardSharedGroupsMap.class);
		CFW.Registry.Objects.addCFWObject(DashboardSharedUserMap.class);
		CFW.Registry.Objects.addCFWObject(DashboardEditorsMap.class);
		CFW.Registry.Objects.addCFWObject(DashboardEditorGroupsMap.class);
    	
		//----------------------------------
    	// Initialize Cache
		WidgetDataCache.initialize();
		
    	//----------------------------------
    	// Register Default Widgets
		CFW.Registry.Widgets.add(new WidgetText());
		CFW.Registry.Widgets.add(new WidgetLabel());
		CFW.Registry.Widgets.add(new WidgetList());
		CFW.Registry.Widgets.add(new WidgetChecklist());
		CFW.Registry.Widgets.add(new WidgetTable());
		CFW.Registry.Widgets.add(new WidgetTags());
		CFW.Registry.Widgets.add(new WidgetImage());
		CFW.Registry.Widgets.add(new WidgetHTMLEditor());
		CFW.Registry.Widgets.add(new WidgetWebsite());
		CFW.Registry.Widgets.add(new WidgetYoutubeVideo());
		CFW.Registry.Widgets.add(new WidgetRefreshTime());

		
    	//----------------------------------
    	// Register Advanced Widgets
		CFW.Registry.Widgets.add(new WidgetDefaultRefresh());
		CFW.Registry.Widgets.add(new WidgetForceRefresh());
		CFW.Registry.Widgets.add(new WidgetParameter());
		CFW.Registry.Widgets.add(new WidgetReplica());
		CFW.Registry.Widgets.add(new WidgetJavascript());
		CFW.Registry.Widgets.add(new WidgetPyConfig());
		CFW.Registry.Widgets.add(new WidgetPyScript());
		CFW.Registry.Widgets.add(new WidgetThresholdLegend());
		
		
    	//----------------------------------
    	// Register Easteregg Widgets
		CFW.Registry.Widgets.add(new WidgetHelloWorld());
		CFW.Registry.Widgets.add(new WidgetEasterEggsDiscoMode());
		CFW.Registry.Widgets.add(new WidgetEasterEggsFireworks());
		CFW.Registry.Widgets.add(new WidgetEasterEggsSnow());
		CFW.Registry.Widgets.add(new WidgetEasterEggsLightSwitch());
		
		//----------------------------------
    	// Register Audit
		CFW.Registry.Audit.addUserAudit(new UserAuditExecutorDashboardUserDirect());
		CFW.Registry.Audit.addUserAudit(new UserAuditExecutorDashboardUserGroups());
		CFW.Registry.Audit.addUserAudit(new UserAuditExecutorWidgetPermissions());
		
    	//----------------------------------
    	// Register Job Tasks
		CFW.Registry.Jobs.registerTask(new CFWJobTaskWidgetTaskExecutor());
		
		//----------------------------------
    	// Register Menu				
		CFW.Registry.Components.addToolsMenuItem(
				(MenuItem)new MenuItem("Dashboards")
					.faicon("fas fa-tachometer-alt")
					.addPermission(PERMISSION_DASHBOARD_VIEWER)
					.addPermission(PERMISSION_DASHBOARD_CREATOR)
					.addPermission(PERMISSION_DASHBOARD_ADMIN)
					.href("/app/dashboard/list")
					.addAttribute("id", "cfwMenuTools-Dashboards")
				, null);
		
			//----------------------------------
	    	// Register Menus
			MenuItem favoritesMenu = (MenuItem)new MenuItem("Favorites")
				.addPermission(PERMISSION_DASHBOARD_VIEWER)
				.addPermission(PERMISSION_DASHBOARD_CREATOR)
				.addPermission(PERMISSION_DASHBOARD_ADMIN)
				.addAttribute("id", "cfwMenuButtons-Favorites")
				.setDynamicCreator(new DynamicItemCreator() {		
		
					@Override
					public ArrayList<HierarchicalHTMLItem> createDynamicItems() {
						
						ArrayList<HierarchicalHTMLItem> childitems = new ArrayList<HierarchicalHTMLItem>();
						ArrayList<Dashboard> dashboardList = CFW.DB.Dashboards.getFavedDashboardList();
						
						//-------------------------
						// Handle no Faves
						if(dashboardList.isEmpty()) {
							childitems.add(
									new MenuItem("No Favorites")
								);
							return childitems;
						}
						
						for(Dashboard current : dashboardList) {

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
	 * Registers a locale file for dashboard, public dashboard and manual.
	 * 
	 * @param locale
	 * @param definition
	 **********************************************************************************/
	public static void registerLocale(Locale locale, FileDefinition definition) {
		
		if(locale == null || definition == null) {
			return;
		}
		
		CFW.Localization.registerLocaleFile(locale, "/app/dashboard", definition);
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
				new Permission(PERMISSION_DASHBOARD_VIEWER, FeatureUserManagement.CATEGORY_USER)
					.description("Can view dashboards that other users have shared. Cannot create dashboards, but might edit when allowed by a dashboard creator."),
					true,
					false
				);	
		
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_DASHBOARD_CREATOR, FeatureUserManagement.CATEGORY_USER)
					.description("Can view and create dashboards and share them with other users."),
					true,
					false
				);	
		
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_DASHBOARD_CREATOR_PUBLIC, FeatureUserManagement.CATEGORY_USER)
					.description("Additional permission for dashboard creators to allow making public links for dashboards."),
					true,
					false
				);	
		
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_DASHBOARD_ADMIN, FeatureUserManagement.CATEGORY_USER)
					.description("View, Edit and Delete all dashboards of all users, regardless of the share settings of the dashboards."),
					true,
					false
				);	
		
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_DASHBOARD_TASKS, FeatureUserManagement.CATEGORY_USER)
				.description("Add and edit tasks of widgets."),
				true,
				false
				);	
		
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_DASHBOARD_FAST_RELOAD, FeatureUserManagement.CATEGORY_USER)
				.description("Gives the user the option to reload the dashboard every minute."),
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
				.description("The default value for the dashboard setting 'Is Shared'.")
				.type(FormFieldType.BOOLEAN)
				.value("false")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration(CONFIG_CATEGORY, CONFIG_AUTO_VERSIONS)
				.description("Enable or disable the automatic creation of dashboard versions.")
				.type(FormFieldType.BOOLEAN)
				.value("true")
				);
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration(CONFIG_CATEGORY, CONFIG_AUTO_VERSIONS_AGE)
				.description("The age of a dashboard change in hours that will cause a dashboard to get a new version.")
				.type(FormFieldType.NUMBER)
				.value("24")
				);
		
		//============================================================
		// EAV Entities
		//============================================================
		CFW.DB.EAVEntity.oneTimeCreate(
				  FeatureDashboard.EAV_STATS_CATEGORY
				, FeatureDashboard.EAV_STATS_PAGE_LOADS
				, "Number of times the dashboard was loaded as a page in the the browser"
				);
		
		CFW.DB.EAVEntity.oneTimeCreate(
				  FeatureDashboard.EAV_STATS_CATEGORY
				, FeatureDashboard.EAV_STATS_PAGE_LOADS_AND_REFRESHES
				, "Number of times the dashboard was loaded as a page in the the browser plus the number of refreshes."
				);
		
		CFW.DB.EAVEntity.oneTimeCreate(
				FeatureDashboard.EAV_STATS_CATEGORY
				, FeatureDashboard.EAV_STATS_WIDGET_LOADS_CACHED
				, "Number of total widget data loads which have been loaded from the cache."
				);
		
		CFW.DB.EAVEntity.oneTimeCreate(
				FeatureDashboard.EAV_STATS_CATEGORY
				, FeatureDashboard.EAV_STATS_WIDGET_LOADS_UNCACHED
				, "Number of total widget data loads which have not been loaded from the cache."
				);
		
		//-------------------------------
		// Create Change Listener
		ConfigChangeListener listener = new ConfigChangeListener(CONFIG_AUTO_VERSIONS_AGE) {
			
			@Override
			public void onChange() {
				startTasks();
			}
		};
		CFW.DB.Config.addChangeListener(listener);
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {
		
		//----------------------------------
    	// Servlets
    	app.addAppServlet(ServletDashboardList.class,  URI_DASHBOARD_LIST);
    	app.addAppServlet(ServletDashboardView.class,  URI_DASHBOARD_VIEW);
    	app.addUnsecureServlet(ServletDashboardViewPublic.class,  URI_DASHBOARD_VIEW_PUBLIC);
    	
    	//----------------------------------
    	// Manual
		createDashboardManual();
		
	}

	@Override
	public void startTasks() {

		//----------------------------------------
		// Task: Create
		if(taskCreateVersions != null) {
			taskCreateVersions.cancel(false);
		}
		boolean doAutoVersions = CFW.DB.Config.getConfigAsBoolean(FeatureDashboard.CONFIG_CATEGORY, FeatureDashboard.CONFIG_AUTO_VERSIONS);
		
		if(doAutoVersions) {
			int millis = (int) CFWTimeUnit.m.toMillis(67); // take uneven minutes 
			// millis = 5000;
			taskCreateVersions = CFW.Schedule.runPeriodicallyMillis(millis, millis, new TaskDashboardCreateVersions());
		}
	}

	@Override
	public void stopFeature() {
		// TODO Auto-generated method stub
		
	}
	
	private void createDashboardManual() {

		//----------------------------------
		//
		MANUAL_PAGE_ROOT.addChild(
				new ManualPage("Introduction")
					.faicon("fas fa-star")
					.addPermission(PERMISSION_DASHBOARD_VIEWER)
					.addPermission(PERMISSION_DASHBOARD_CREATOR)
					.addPermission(PERMISSION_DASHBOARD_ADMIN)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_00_introduction.html")
			);
		
		//----------------------------------
		//
		MANUAL_PAGE_ROOT.addChild(
				new ManualPage("Creating Dashboards")
					.faicon("fas fa-plus-circle")
					.addPermission(PERMISSION_DASHBOARD_VIEWER)
					.addPermission(PERMISSION_DASHBOARD_CREATOR)
					.addPermission(PERMISSION_DASHBOARD_ADMIN)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_creating_dashboards.html")
			);
		
		//----------------------------------
		//
		MANUAL_PAGE_ROOT.addChild(
				new ManualPage("Keyboard Shortcuts")
					.faicon("fas fa-keyboard")
					.addPermission(PERMISSION_DASHBOARD_VIEWER)
					.addPermission(PERMISSION_DASHBOARD_CREATOR)
					.addPermission(PERMISSION_DASHBOARD_ADMIN)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_shortcuts.html")
			);
		
		//----------------------------------
		// Pages for each Widget
		LinkedHashMap<String, WidgetDefinition> widgetList = CFW.Registry.Widgets.getWidgetDefinitions();
		
		
		for(WidgetDefinition current : widgetList.values()) {
			
			ManualPageWidget widgetPage = new ManualPageWidget(current);
			String path = MANUAL_PATH_WIDGETS;
			String category = current.widgetCategory();
			
			if(category == FeatureDashboard.WIDGET_CATEGORY_EASTEREGGS) {
				// skip these bastards
				continue;
			}
			
			if(category != null) {
				path += "|" + category;
			}
			
			CFW.Registry.Manual.addManualPage(path, widgetPage);
			
		}
		
		//----------------------------------
		//
//		ManualPage widgets = 
//			new ManualPage("Widgets")
//				.faicon("fas fa-th")
//				.addPermission(PERMISSION_DASHBOARD_VIEWER)
//				.addPermission(PERMISSION_DASHBOARD_CREATOR)
//				.addPermission(PERMISSION_DASHBOARD_ADMIN)
//				.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_widgets_00.html");
//		
//		MANUAL_PAGE_ROOT.addChild(widgets );
//		
//		widgets.addChild(
//				new ManualPage("Standard Widgets")
//					.faicon("fas fa-th-large")
//					.addPermission(PERMISSION_DASHBOARD_VIEWER)
//					.addPermission(PERMISSION_DASHBOARD_CREATOR)
//					.addPermission(PERMISSION_DASHBOARD_ADMIN)
//					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_widgets_staticwidgets.html")
//			);
//		
//		widgets.addChild(
//				new ManualPage("Timeframe Widgets")
//					.faicon("fas fa-clock")
//					.addPermission(PERMISSION_DASHBOARD_VIEWER)
//					.addPermission(PERMISSION_DASHBOARD_CREATOR)
//					.addPermission(PERMISSION_DASHBOARD_ADMIN)
//					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_widgets_timeframe.html")
//			);
//		
		
		//----------------------------------
		//
		MANUAL_PAGE_ROOT.addChild(
				new ManualPage("Parameters")
					.faicon("fas fa-sliders-h")
					.addPermission(PERMISSION_DASHBOARD_VIEWER)
					.addPermission(PERMISSION_DASHBOARD_CREATOR)
					.addPermission(PERMISSION_DASHBOARD_ADMIN)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_parameters.html")
			);
		
		//----------------------------------
		//
		MANUAL_PAGE_ROOT.addChild(
				new ManualPage("Tips and Tricks")
					.faicon("fas fa-asterisk")
					.addPermission(PERMISSION_DASHBOARD_VIEWER)
					.addPermission(PERMISSION_DASHBOARD_CREATOR)
					.addPermission(PERMISSION_DASHBOARD_ADMIN)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_tips_tricks.html")
			);
		
	}
	
	/*******************************************************************
	 * Returns the URL for the given Dashboard ID or null if the
	 * servername was not defined in the cfw.properties
	 * @param dashboardID
	 *******************************************************************/
	public static String createURLForDashboard(int dashboardID) {
		return createURLForDashboard(""+dashboardID);
	}
	
	/*******************************************************************
	 * Returns the URL for the given Dashboard ID or null if the
	 * servername was not defined in the cfw.properties
	 * @param dashboardID
	 *******************************************************************/
	public static String createURLForDashboard(String dashboardID) {
		
		if(CFW.Properties.APPLICATION_URL == null) {
			return null;
		}
		
		return CFW.Properties.APPLICATION_URL + URI_DASHBOARD_VIEW + "?id="+dashboardID;
	}
	
}
