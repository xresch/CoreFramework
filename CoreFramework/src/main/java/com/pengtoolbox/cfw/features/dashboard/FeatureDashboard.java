package com.pengtoolbox.cfw.features.dashboard;

import java.util.Locale;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFWAppFeature;
import com.pengtoolbox.cfw._main.CFWApplicationExecutor;
import com.pengtoolbox.cfw.caching.FileDefinition;
import com.pengtoolbox.cfw.caching.FileDefinition.HandlingType;
import com.pengtoolbox.cfw.features.manual.ManualPage;
import com.pengtoolbox.cfw.features.usermgmt.Permission;
import com.pengtoolbox.cfw.response.bootstrap.MenuItem;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class FeatureDashboard extends CFWAppFeature {
	
	public static final String PERMISSION_DASHBOARD_VIEWER = "Dashboard Viewer";
	public static final String PERMISSION_DASHBOARD_CREATOR = "Dashboard Creator";
	public static final String PERMISSION_DASHBOARD_ADMIN = "Dashboard Admin";

	
	public static final String PACKAGE_RESOURCES = "com.pengtoolbox.cfw.features.dashboard.resources";
	public static final String PACKAGE_MANUAL = "com.pengtoolbox.cfw.features.dashboard.manual";
	
	public static final ManualPage ROOT_MANUAL_PAGE = CFW.Registry.Manual.addManualPage(null, new ManualPage("Dashboard").faicon("fas fa-tachometer-alt"));
	
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
		
    	//----------------------------------
    	// Register Objects
		CFW.Registry.Objects.addCFWObject(Dashboard.class);
		CFW.Registry.Objects.addCFWObject(DashboardWidget.class);
		
    	//----------------------------------
    	// Register Widgets
		CFW.Registry.Widgets.add(new WidgetHelloWorld());
		CFW.Registry.Widgets.add(new WidgetText());
		CFW.Registry.Widgets.add(new WidgetLabel());
		CFW.Registry.Widgets.add(new WidgetList());
		CFW.Registry.Widgets.add(new WidgetChecklist());
		CFW.Registry.Widgets.add(new WidgetTable());
		CFW.Registry.Widgets.add(new WidgetImage());
		CFW.Registry.Widgets.add(new WidgetHTMLEditor());
		CFW.Registry.Widgets.add(new WidgetWebsite());
		CFW.Registry.Widgets.add(new WidgetYoutubeVideo());
		CFW.Registry.Widgets.add(new WidgetRefreshTime());
		CFW.Registry.Widgets.add(new WidgetEasterEggsDiscoMode());
    	
		//----------------------------------
    	// Register Menu
		CFW.Registry.Components.addRegularMenuItem(
				(MenuItem)new MenuItem("Dashboards")
					.faicon("fas fa-tachometer-alt")
					.addPermission(PERMISSION_DASHBOARD_VIEWER)
					.addPermission(PERMISSION_DASHBOARD_CREATOR)
				, null);
				
		CFW.Registry.Components.addRegularMenuItem(
				(MenuItem)new MenuItem("Dashboard List")
					.faicon("fas fa-images")
					.addPermission(PERMISSION_DASHBOARD_VIEWER)
					.addPermission(PERMISSION_DASHBOARD_CREATOR)
					.href("/app/dashboard/list")
				, "Dashboards");
		
		//----------------------------------
    	// Register Manual
		registerDashboardManual();
	}

	@Override
	public void initializeDB() {
		//-----------------------------------
		// 
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_DASHBOARD_VIEWER, "user")
					.description("Can view dashboards that other users have shared. Cannot create dashboards, but might edit when allowed by a dashboard creator."),
					true,
					false
				);	
		
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_DASHBOARD_CREATOR, "user")
					.description("Can view and create dashboards and share them with other users."),
					true,
					false
				);	
		
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_DASHBOARD_ADMIN, "user")
					.description("View, Edit and Delete all dashboards of all users, regardless of the share settings of the dashboards."),
					true,
					false
				);	
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
    	app.addAppServlet(ServletDashboardList.class,  "/dashboard/list");
    	app.addAppServlet(ServletDashboardView.class,  "/dashboard/view");
	}

	@Override
	public void startTasks() {

	}

	@Override
	public void stopFeature() {
		// TODO Auto-generated method stub
		
	}
	
	private void registerDashboardManual() {
		
		// Creating Dashboards
		//   The Dashboard List
		//   Editing a Dashboard
		//   Adding Widgets
		
		// Widgets
		//   Default Widgets
		
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
				new ManualPage("Static Widgets")
					.faicon("fas fa-th-large")
					.addPermission(PERMISSION_DASHBOARD_VIEWER)
					.addPermission(PERMISSION_DASHBOARD_CREATOR)
					.addPermission(PERMISSION_DASHBOARD_ADMIN)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_widgets_staticwidgets.html")
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

}
