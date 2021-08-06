package com.xresch.cfw.features.dashboard;

import java.util.Locale;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.features.dashboard.parameters.DashboardParameter;
import com.xresch.cfw.features.dashboard.parameters.ParameterDefinitionBoolean;
import com.xresch.cfw.features.dashboard.parameters.ParameterDefinitionSelect;
import com.xresch.cfw.features.dashboard.parameters.ParameterDefinitionText;
import com.xresch.cfw.features.dashboard.parameters.ParameterDefinitionTextarea;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.response.bootstrap.MenuItem;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureDashboard extends CFWAppFeature {
	
	public static final String PERMISSION_DASHBOARD_VIEWER = "Dashboard Viewer";
	public static final String PERMISSION_DASHBOARD_CREATOR = "Dashboard Creator";
	public static final String PERMISSION_DASHBOARD_ADMIN = "Dashboard Admin";

	
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
		
    	//----------------------------------
    	// Register Objects
		CFW.Registry.Objects.addCFWObject(Dashboard.class);
		CFW.Registry.Objects.addCFWObject(DashboardWidget.class);
		CFW.Registry.Objects.addCFWObject(DashboardParameter.class);
		
    	//----------------------------------
    	// Register Widgets
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
		CFW.Registry.Widgets.add(new WidgetParameter());
		CFW.Registry.Widgets.add(new WidgetReplica());
		
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
		
		//----------------------------------
    	// Register Audit
		CFW.Registry.Audit.addUserAudit(new UserAuditExecutorDashboardUserDirect());
		CFW.Registry.Audit.addUserAudit(new UserAuditExecutorDashboardUserGroups());
		
    	//----------------------------------
    	// Register Job Tasks
		CFW.Registry.Jobs.registerTask(new CFWJobTaskWidget());
		
		//----------------------------------
    	// Register Menu				
		CFW.Registry.Components.addRegularMenuItem(
				(MenuItem)new MenuItem("Dashboards")
					.faicon("fas fa-tachometer-alt")
					.addPermission(PERMISSION_DASHBOARD_VIEWER)
					.addPermission(PERMISSION_DASHBOARD_CREATOR)
					.addPermission(PERMISSION_DASHBOARD_ADMIN)
					.href("/app/dashboard/list")
				, FeatureCore.MENU_TOOLS);
		
		//----------------------------------
    	// Register Manual
		registerDashboardManual();
	}

	@Override
	public void initializeDB() {
		//-----------------------------------
		// 
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
				new Permission(PERMISSION_DASHBOARD_ADMIN, FeatureUserManagement.CATEGORY_USER)
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

}
