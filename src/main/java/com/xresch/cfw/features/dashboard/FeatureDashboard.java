package com.xresch.cfw.features.dashboard;

import java.util.ArrayList;
import java.util.Locale;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.features.dashboard.parameters.DashboardParameter;
import com.xresch.cfw.features.dashboard.parameters.ParameterDefinitionBoolean;
import com.xresch.cfw.features.dashboard.parameters.ParameterDefinitionDashboardID;
import com.xresch.cfw.features.dashboard.parameters.ParameterDefinitionNumber;
import com.xresch.cfw.features.dashboard.parameters.ParameterDefinitionSelect;
import com.xresch.cfw.features.dashboard.parameters.ParameterDefinitionText;
import com.xresch.cfw.features.dashboard.parameters.ParameterDefinitionTextarea;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetForceRefresh;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetJavascript;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetParameter;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetPyConfig;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetPyScript;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetReplica;
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
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.response.bootstrap.DynamicItemCreator;
import com.xresch.cfw.response.bootstrap.HierarchicalHTMLItem;
import com.xresch.cfw.response.bootstrap.MenuItem;
import com.xresch.cfw.spi.CFWAppFeature;

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
	
	public static final String PACKAGE_RESOURCES = "com.xresch.cfw.features.dashboard.resources";
	public static final String PACKAGE_MANUAL = "com.xresch.cfw.features.dashboard.manual";
	
	public static final ManualPage ROOT_MANUAL_PAGE = CFW.Registry.Manual.addManualPage(null, 
					new ManualPage("Dashboard")
						.faicon("fas fa-tachometer-alt")
						.addPermission(PERMISSION_DASHBOARD_VIEWER)
						.addPermission(PERMISSION_DASHBOARD_CREATOR)
						.addPermission(PERMISSION_DASHBOARD_ADMIN)
				);
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCES);
		CFW.Files.addAllowedPackage(PACKAGE_MANUAL);
		
		//----------------------------------
		// Register Languages
		CFW.Localization.registerLocaleFile(Locale.ENGLISH, "/app/dashboard", new FileDefinition(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "lang_en_dashboard.properties"));
		CFW.Localization.registerLocaleFile(Locale.GERMAN, "/app/dashboard", new FileDefinition(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "lang_de_dashboard.properties"));
		
		CFW.Localization.registerLocaleFile(Locale.ENGLISH, URI_DASHBOARD_VIEW_PUBLIC, new FileDefinition(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "lang_en_dashboard.properties"));
		CFW.Localization.registerLocaleFile(Locale.GERMAN, URI_DASHBOARD_VIEW_PUBLIC, new FileDefinition(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "lang_de_dashboard.properties"));
		
    	//----------------------------------
    	// Register Objects
		CFW.Registry.Objects.addCFWObject(Dashboard.class);
		CFW.Registry.Objects.addCFWObject(DashboardWidget.class);
		CFW.Registry.Objects.addCFWObject(DashboardParameter.class);
		CFW.Registry.Objects.addCFWObject(DashboardFavoritesMap.class);
    	
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
		CFW.Registry.Widgets.add(new WidgetForceRefresh());
		CFW.Registry.Widgets.add(new WidgetParameter());
		CFW.Registry.Widgets.add(new WidgetReplica());
		CFW.Registry.Widgets.add(new WidgetJavascript());
		CFW.Registry.Widgets.add(new WidgetPyConfig());
		CFW.Registry.Widgets.add(new WidgetPyScript());
		
    	//----------------------------------
    	// Register Easteregg Widgets
		CFW.Registry.Widgets.add(new WidgetHelloWorld());
		CFW.Registry.Widgets.add(new WidgetEasterEggsDiscoMode());
		CFW.Registry.Widgets.add(new WidgetEasterEggsSnow());
		CFW.Registry.Widgets.add(new WidgetEasterEggsFireworks());
		CFW.Registry.Widgets.add(new WidgetEasterEggsLightSwitch());
		
    	//----------------------------------
    	// Register Parameters
		CFW.Registry.Parameters.add(new ParameterDefinitionText());
		CFW.Registry.Parameters.add(new ParameterDefinitionTextarea());
		CFW.Registry.Parameters.add(new ParameterDefinitionSelect());
		CFW.Registry.Parameters.add(new ParameterDefinitionBoolean());
		CFW.Registry.Parameters.add(new ParameterDefinitionNumber());
		CFW.Registry.Parameters.add(new ParameterDefinitionDashboardID());
		
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
		//----------------------------------
    	// Register Manual
		registerDashboardManual();
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
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
    	app.addAppServlet(ServletDashboardList.class,  URI_DASHBOARD_LIST);
    	app.addAppServlet(ServletDashboardView.class,  URI_DASHBOARD_VIEW);
    	app.addUnsecureServlet(ServletDashboardViewPublic.class,  URI_DASHBOARD_VIEW_PUBLIC);
	}

	@Override
	public void startTasks() {
		// TODO Auto-generated method stub
	}

	@Override
	public void stopFeature() {
		// TODO Auto-generated method stub
		
	}
	
	private void registerDashboardManual() {

		//----------------------------------
		//
		ROOT_MANUAL_PAGE.addChild(
				new ManualPage("Introduction")
					.faicon("fas fa-star")
					.addPermission(PERMISSION_DASHBOARD_VIEWER)
					.addPermission(PERMISSION_DASHBOARD_CREATOR)
					.addPermission(PERMISSION_DASHBOARD_ADMIN)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_00_introduction.html")
			);
		
		//----------------------------------
		//
		ROOT_MANUAL_PAGE.addChild(
				new ManualPage("Creating Dashboards")
					.faicon("fas fa-plus-circle")
					.addPermission(PERMISSION_DASHBOARD_VIEWER)
					.addPermission(PERMISSION_DASHBOARD_CREATOR)
					.addPermission(PERMISSION_DASHBOARD_ADMIN)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_creating_dashboards.html")
			);
		
		//----------------------------------
		//
		ROOT_MANUAL_PAGE.addChild(
				new ManualPage("Keyboard Shortcuts")
					.faicon("fas fa-keyboard")
					.addPermission(PERMISSION_DASHBOARD_VIEWER)
					.addPermission(PERMISSION_DASHBOARD_CREATOR)
					.addPermission(PERMISSION_DASHBOARD_ADMIN)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_shortcuts.html")
			);
		
		//----------------------------------
		//
		ManualPage widgets = 
			new ManualPage("Widgets")
				.faicon("fas fa-th")
				.addPermission(PERMISSION_DASHBOARD_VIEWER)
				.addPermission(PERMISSION_DASHBOARD_CREATOR)
				.addPermission(PERMISSION_DASHBOARD_ADMIN)
				.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_widgets_00.html");
		
		ROOT_MANUAL_PAGE.addChild(widgets );
		
		widgets.addChild(
				new ManualPage("Standard Widgets")
					.faicon("fas fa-th-large")
					.addPermission(PERMISSION_DASHBOARD_VIEWER)
					.addPermission(PERMISSION_DASHBOARD_CREATOR)
					.addPermission(PERMISSION_DASHBOARD_ADMIN)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_widgets_staticwidgets.html")
			);
		
		widgets.addChild(
				new ManualPage("Timeframe Widgets")
					.faicon("fas fa-clock")
					.addPermission(PERMISSION_DASHBOARD_VIEWER)
					.addPermission(PERMISSION_DASHBOARD_CREATOR)
					.addPermission(PERMISSION_DASHBOARD_ADMIN)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_widgets_timeframe.html")
			);
		
		//----------------------------------
		//
		ROOT_MANUAL_PAGE.addChild(
				new ManualPage("Parameters")
					.faicon("fas fa-sliders-h")
					.addPermission(PERMISSION_DASHBOARD_VIEWER)
					.addPermission(PERMISSION_DASHBOARD_CREATOR)
					.addPermission(PERMISSION_DASHBOARD_ADMIN)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_parameters.html")
			);
		
		//----------------------------------
		//
		ROOT_MANUAL_PAGE.addChild(
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
