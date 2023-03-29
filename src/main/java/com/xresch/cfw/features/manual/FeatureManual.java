package com.xresch.cfw.features.manual;

import java.util.TimeZone;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.response.bootstrap.MenuItem;
import com.xresch.cfw.spi.CFWAppFeature;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureManual extends CFWAppFeature {

	private Object object;
	public static final String PERMISSION_MANUAL = "Manual";
	public static final String PERMISSION_ADMIN_MANUAL = "Manual for Admins";

	public static final String RESOURCE_PACKAGE = "com.xresch.cfw.features.manual.resources";
	
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
	
	public static final ManualPage TOP_PAGE_GENERAL = CFW.Registry.Manual.addManualPage(null, 
			new ManualPage("General")
				.faicon("fa fa-star")
			);
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);

    	//----------------------------------
    	// Register Button Menu
		CFW.Registry.Components.addButtonsMenuItem(
		(MenuItem)new MenuItem("Manual", "{!cfw_core_manual!}") 
				.faicon("fas fa-book")
				.addPermission(FeatureManual.PERMISSION_MANUAL)
				.href("/app/manual")
				.addAttribute("id", "cfwMenuButtons-Manual")
				, null
		);

		//---------------------------
		// Register Manuals
		registerAdminManual();
		registerDeveloperManual();
		registerGeneral();
	}

	@Override
	public void initializeDB() {
		
		//-----------------------------------
		// 
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_MANUAL, FeatureUserManagement.CATEGORY_USER)
					.description("Can access the manual pages. Adds the manual menu item to the menu bar."),
					true,
					true
				);
			
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_ADMIN_MANUAL, FeatureUserManagement.CATEGORY_USER)
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
		// nothing to do
	}

	@Override
	public void stopFeature() {
		// nothing to do
		
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	private void registerAdminManual() {
		
		TOP_PAGE_ADMIN.addChild(new ManualPage("Setup: Initial Installation")
				.faicon("fas fa-star")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".admin", "manual_admin_setup.html")
			);
				
		TOP_PAGE_ADMIN.addChild(new ManualPage("Setup: Authentication")
				.faicon("fas fa-user")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".admin", "manual_admin_setupauthentication.html")
				);
		
		TOP_PAGE_ADMIN.addChild(new ManualPage("Configuration")
				.faicon("fas fa-cog")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".admin", "manual_admin_configuration.html")
		);
		
		TOP_PAGE_ADMIN.addChild(new ManualPage("User Management")
				.faicon("fas fa-users")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".admin", "manual_admin_usermanagement.html")
		);

		TOP_PAGE_ADMIN.addChild(new ManualPage("Context Settings")
				.faicon("fas fa-cogs")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".admin", "manual_admin_contextsettings.html")
		);
		
		TOP_PAGE_ADMIN.addChild(new ManualPage("System Analytics")
				.faicon("fas fa-traffic-light")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".admin", "manual_admin_analytics.html")
		);
		
		TOP_PAGE_ADMIN.addChild(new ManualPage("Keyboard Shortcuts")
				.faicon("fas fa-keyboard")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".admin", "manual_admin_shortcuts.html")
		);
		
		TOP_PAGE_ADMIN.addChild(new ManualPage("What do you do?")
				.faicon("fas fa-question")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".admin", "manual_admin_whatdoyoudo.html")
				);
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	private void registerDeveloperManual() {
		
		ManualPage philosophy = new ManualPage("Framework Philosophy")
				.faicon("fas fa-seedling")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev", "manual_dev_cfw_philosophy.html");
		TOP_PAGE_DEV.addChild(philosophy);
		
//		ManualPage quickstart = new ManualPage("Quickstart").faicon("fas fa-fighter-jet").addPermission(PERMISSION_ADMIN_MANUAL);
//		TOP_PAGE_DEV.addChild(quickstart);
		
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
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev", "manual_dev_quick_setup_run_export.html")
			);
		
		quickstart.addChild(new ManualPage("Overview")
				.faicon("fas fa-eye")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev", "manual_dev_quick_overview.html")
			);
		
		quickstart.addChild(new ManualPage("Create an Application")
				.faicon("fas fa-server")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev", "manual_dev_quick_create_application.html")
			);
		
		quickstart.addChild(new ManualPage("Create a Feature")
				.faicon("fas fa-plug")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev", "manual_dev_quick_create_feature.html")
			);
		quickstart.addChild(new ManualPage("Write Extensions")
				.faicon("fas fa-plug")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev", "manual_dev_quick_write_extensions.html")
			);
		
		quickstart.addChild(new ManualPage("Create a Servlet")
				.faicon("fas fa-server")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev", "manual_dev_quick_create_servlet.html")
			);
		
		quickstart.addChild(
				new ManualPage("Add Menu Items")
					.faicon("fa fa-book")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev", "manual_dev_quick_menuitems.html")
				);
		
		//-----------------------------
		// Data Handling
		ManualPage dataHandling = 
				new ManualPage("Data Handling")
					.faicon("fas fa-database")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					//.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "manual_dev_quick_cfwobject.html")
					;
		
		quickstart.addChild(dataHandling);
		
		dataHandling.addChild(
				new ManualPage("Working with CFWObjects")
					.faicon("fas fa-th-large")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev.datahandling", "manual_dev_quick_cfwobject.html")
				);
		
		dataHandling.addChild(
				new ManualPage("Creating CFWFields")
					.faicon("far fa-square")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev.datahandling", "manual_dev_quick_cfwfields.html")
				);
		
		dataHandling.addChild(
				new ManualPage("Autocomplete Fields")
					.faicon("fas fa-magic")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev.datahandling", "manual_dev_quick_autocomplete.html")
				);
		
		dataHandling.addChild(
				new ManualPage("Working with Forms")
					.faicon("fas fa-file-upload")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev.datahandling", "manual_dev_quick_forms.html")
				);
		
		dataHandling.addChild(
				new ManualPage("Accessing Databases")
					.faicon("fas fa-database")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev.datahandling", "manual_dev_quick_database.html")
				);
		
		dataHandling.addChild(
				new ManualPage("Executing SQL")
					.faicon("fas fa-bolt")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev.datahandling", "manual_dev_quick_sql.html")
				);
		

		//-----------------------------
		// Other
		
		quickstart.addChild(new ManualPage("Write Logs")
				.faicon("fa fa-pen")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev", "manual_dev_quick_logging.html")
			);
		
		quickstart.addChild(new ManualPage("Add Configuration Items")
					.faicon("fa fa-cog")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev", "manual_dev_quick_configuration.html")
				);
		
		quickstart.addChild(new ManualPage("Add Context Settings")
				.faicon("fas fa-city")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev", "manual_dev_quick_contextsettings.html")
			);
		
		quickstart.addChild(
				new ManualPage("Create Permissions")
					.faicon("fa fa-lock")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev", "manual_dev_quick_permissions.html")
				);
		
		quickstart.addChild(
				new ManualPage("Notify Users")
					.faicon("fa fa-bell")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev", "manual_dev_quick_notifications.html")
				);
		
		quickstart.addChild(
				new ManualPage("Create Manual Pages")
					.faicon("fa fa-book")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev", "manual_dev_quick_manualpages.html")
				);
				
		quickstart.addChild(
				new ManualPage("Localization")
					.faicon("fas fa-globe-americas")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev", "manual_dev_quick_localization.html")
				);
		
		quickstart.addChild(
				new ManualPage("Add an API")
					.faicon("fas fa-paperclip")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev", "manual_dev_quick_api.html")
				);
		
		quickstart.addChild(
				new ManualPage("Add User Audit")
					.faicon("fas fa-check")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev", "manual_dev_quick_useraudit.html")
				);
		
		quickstart.addChild(
				new ManualPage("Add Job Tasks")
					.faicon("fas fa-cogs")
					.addPermission(PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev", "manual_dev_quick_jobtasks.html")
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
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev.js", "manual_dev_cfwjs_overview.html")
			);
		
		javascript.addChild(new ManualPage("Working with Renderers")
				.faicon("fas fa-vector-square")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev.js", "manual_dev_cfwjs_renderer.html")
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
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev.dashboard", "manual_dev_dashboard_wigdets.html")
			);
		
		dashboard.addChild(new ManualPage("Creating Dashboard Parameters")
				.faicon("fas fa-sliders-h")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE+".dev.dashboard", "manual_dev_dashboard_parameters.html")
			);
		
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	private void registerGeneral() {
		
		//----------------------------------
		// Available Time Zones
		String htmlString = 
				  "<p>Functionalities like dashboard widgets and queries support the use of time parameters. "
				+ "Following the list of available placeholders that are inserted into your strings like '$earliest$':</p>"
				+ CFW.Files.readPackageResource(RESOURCE_PACKAGE+".general", "manual_general_params_time.html");
		
		TOP_PAGE_GENERAL.addChild(new ManualPage("Time Parameters")
				.faicon("fas fa-clock")
				.addPermission(PERMISSION_ADMIN_MANUAL)
				.content(htmlString)
			);
		
		//----------------------------------
		// Available Time Zones
		htmlString = "<p>Some of the query sources might provide the possibility to specify a time zone to manage time offsets."
				+ "The following is a list of available time zones.</p>";
				
		for(String zone : TimeZone.getAvailableIDs()) {
			htmlString += "<li>"+zone+"</li>";
		}
		htmlString += "</ul>";
		
		ManualPage timezonePage = new ManualPage("Available Time Zones")
				.faicon("fas fa-clock")
				.content(htmlString);
		
		TOP_PAGE_GENERAL.addChild(timezonePage);
		
	}

}
