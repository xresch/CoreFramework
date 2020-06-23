package com.pengtoolbox.cfw.features.manual;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFWAppFeature;
import com.pengtoolbox.cfw._main.CFWApplicationExecutor;
import com.pengtoolbox.cfw.caching.FileDefinition.HandlingType;
import com.pengtoolbox.cfw.features.usermgmt.Permission;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class FeatureManual extends CFWAppFeature {

	public static final String PERMISSION_MANUAL = "Manual";
	public static final String PERMISSION_ADMIN_MANUAL = "Manual for Admins";

	public static final String RESOURCE_PACKAGE = "com.pengtoolbox.cfw.features.manual.resources";
	
	public static final ManualPage TOP_PAGE_ADMIN = CFW.Registry.Manual.addManualPage(null, 
			new ManualPage("Administration")
				.faicon("fa fa-cog")
				.addPermission(PERMISSION_ADMIN_MANUAL)
			);
	
	public static final ManualPage TOP_PAGE_DEV = CFW.Registry.Manual.addManualPage(null, 
			new ManualPage("Development")
				.faicon("fa fa-code")
				.addPermission(PERMISSION_ADMIN_MANUAL)
			);
	
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);

    	//----------------------------------
    	// Register Regular Menu
		// Manual Menu is handled by CFW.Registry.Components.createMenuInstance()
		
		//---------------------------
		// Admin Manuals

		//---------------------------
		// Developer Manual
		registerAdminManual();
		registerDeveloperManual();
	}

	@Override
	public void initializeDB() {
		
		//-----------------------------------
		// 
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_MANUAL, "user")
					.description("Can access the manual pages. Adds the manual menu item to the menu bar."),
					true,
					true
				);
			
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_ADMIN_MANUAL, "user")
					.description("Can access the manual pages for admins and developers."),
					true,
					false
				);
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
    	app.addAppServlet(ServletManual.class,  "/manual");
	}

	@Override
	public void startTasks() {

	}

	@Override
	public void stopFeature() {
		// TODO Auto-generated method stub
		
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	private void registerAdminManual() {
		
		TOP_PAGE_ADMIN.addChild(new ManualPage("Introduction")
				.faicon("fas fa-star")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_admin_00_intro.html")
			);
		
		TOP_PAGE_ADMIN.addChild(new ManualPage("Configuration")
				.faicon("fas fa-cog")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_admin_configuration.html")
		);
		
		
		TOP_PAGE_ADMIN.addChild(new ManualPage("User Management")
				.faicon("fas fa-users")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_admin_usermanagement.html")
		);

		TOP_PAGE_ADMIN.addChild(new ManualPage("Context Settings")
				.faicon("fas fa-cogs")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_admin_contextsettings.html")
		);
		
		TOP_PAGE_ADMIN.addChild(new ManualPage("System Analytics")
				.faicon("fas fa-traffic-light")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_admin_analytics.html")
		);
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	private void registerDeveloperManual() {
		
		ManualPage philosophy = new ManualPage("Framework Philosophy")
				.faicon("fas fa-seedling")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_dev_cfw_philosophy.html");
		TOP_PAGE_DEV.addChild(philosophy);
		
		ManualPage quickstart = new ManualPage("Quickstart").faicon("fas fa-fighter-jet").addPermission(PERMISSION_ADMIN_MANUAL);
		TOP_PAGE_DEV.addChild(quickstart);
		
		registerDeveloperQuickstart(TOP_PAGE_DEV);
		registerDeveloperCFWJS(TOP_PAGE_DEV);
		registerDeveloperDashboard(TOP_PAGE_DEV);
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	private void registerDeveloperQuickstart(ManualPage parent) {
		
		ManualPage quickstart = new ManualPage("Quickstart").faicon("fas fa-fighter-jet").addPermission(PERMISSION_ADMIN_MANUAL);
		parent.addChild(quickstart);

		quickstart.addChild(new ManualPage("Setup, Run and Export")
				.faicon("fas fa-star")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_dev_quick_setup_run_export.html")
			);
		
		quickstart.addChild(new ManualPage("Overview")
				.faicon("fas fa-eye")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_dev_quick_overview.html")
			);
		
		quickstart.addChild(new ManualPage("Create an Application")
				.faicon("fas fa-server")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_dev_quick_create_application.html")
			);
		
		quickstart.addChild(new ManualPage("Create a Feature")
				.faicon("fas fa-plug")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_dev_quick_create_feature.html")
			);
		
		quickstart.addChild(new ManualPage("Create a Servlet")
				.faicon("fas fa-server")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_dev_quick_create_servlet.html")
			);
		
		quickstart.addChild(
				new ManualPage("Working with CFWObjects")
					.faicon("fas fa-th-large")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_dev_quick_cfwobject.html")
				);
		
		quickstart.addChild(
				new ManualPage("Creating CFWFields")
					.faicon("far fa-square")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_dev_quick_cfwfields.html")
				);
		
		quickstart.addChild(
				new ManualPage("Accessing Databases")
					.faicon("fas fa-database")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_dev_quick_database.html")
				);
		
		quickstart.addChild(
				new ManualPage("Executing SQL")
					.faicon("fas fa-bolt")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_dev_quick_sql.html")
				);
		
		quickstart.addChild(new ManualPage("Add Configuration Items")
					.faicon("fa fa-cog")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_dev_quick_configuration.html")
				);
		
		quickstart.addChild(new ManualPage("Add Context Settings")
				.faicon("fas fa-city")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_dev_quick_contextsettings.html")
			);
		
		quickstart.addChild(
				new ManualPage("Create Permissions")
					.faicon("fa fa-lock")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_dev_quick_permissions.html")
				);
		
		quickstart.addChild(
				new ManualPage("Create Manual Pages")
					.faicon("fa fa-book")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_dev_quick_manualpages.html")
				);
				
		quickstart.addChild(
				new ManualPage("Localization")
					.faicon("fas fa-globe-americas")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_dev_quick_localization.html")
				);
		
		quickstart.addChild(
				new ManualPage("Add an API")
					.faicon("fas fa-paperclip")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_dev_quick_api.html")
				);
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	private void registerDeveloperCFWJS(ManualPage parent) {
		
		ManualPage javascript = new ManualPage("Javascript").faicon("fab fa-js-square").addPermission(PERMISSION_ADMIN_MANUAL);
		parent.addChild(javascript);

		javascript.addChild(new ManualPage("Overview")
				.faicon("fas fa-eye")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_dev_cfwjs_overview.html")
			);
		
		javascript.addChild(new ManualPage("Working with Renderers")
				.faicon("fas fa-vector-square")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_dev_cfwjs_renderer.html")
			);
		
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	private void registerDeveloperDashboard(ManualPage parent) {
		
		ManualPage dashboard = new ManualPage("Dashboard").faicon("fas fa-tachometer-alt").addPermission(PERMISSION_ADMIN_MANUAL);
		parent.addChild(dashboard);
		
		dashboard.addChild(new ManualPage("Creating Dashboard Widgets")
				.faicon("fas fa-th")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_dev_dashboard_wigdets.html")
			);
		
	}

}
