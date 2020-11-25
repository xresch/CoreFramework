package com.xresch.cfw.features.spaces;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.features.core.HierarchicalSortConfig;
import com.xresch.cfw.features.core.ServletSortHierarchy;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.spaces.Space.SpaceFields;
import com.xresch.cfw.response.bootstrap.MenuItem;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureSpaces extends CFWAppFeature {
	
	public static final String PERMISSION_SPACE_VIEWER = "Space User";
	public static final String PERMISSION_SPACE_ADMIN = "Space Admin";

	
	public static final String PACKAGE_RESOURCES = "com.xresch.cfw.features.spaces.resources";
	public static final String PACKAGE_MANUAL = "com.xresch.cfw.features.spaces.manual";
	
	public static final ManualPage ROOT_MANUAL_PAGE = CFW.Registry.Manual.addManualPage(null, new ManualPage("Dashboard").faicon("fas fa-tachometer-alt"));
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCES);
		CFW.Files.addAllowedPackage(PACKAGE_MANUAL);
		
		//----------------------------------
		// Register Languages
//		CFW.Localization.registerLocaleFile(Locale.ENGLISH, "/app/dashboard", new FileDefinition(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "lang_en_dashboard.properties"));
//		CFW.Localization.registerLocaleFile(Locale.GERMAN, "/app/dashboard", new FileDefinition(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "lang_de_dashboard.properties"));
		
    	//----------------------------------
    	// Register Objects
		CFW.Registry.Objects.addCFWObject(Space.class);
		CFW.Registry.Objects.addCFWObject(SpaceGroup.class);
		
    	//----------------------------------
    	// Register Objects
		ServletSortHierarchy.addConfig(new HierarchicalSortConfig("spaces", Space.class, SpaceFields.NAME) {
			
			@Override
			public boolean canSort(String sortedElementID, String targetParentID) {
				return true;
			}
			
			@Override
			public boolean canAccess(String rootElementID) {
				return true;
			}
		});
		
		//----------------------------------
    	// Register Menu
		CFW.Registry.Components.addRegularMenuItem(
				(MenuItem)new MenuItem("Spaces")
					.faicon("fas fa-tachometer-alt")
					//.addPermission(PERMISSION_DASHBOARD_VIEWER)
					//.addPermission(PERMISSION_DASHBOARD_CREATOR)
				, null);
				
		CFW.Registry.Components.addRegularMenuItem(
				(MenuItem)new MenuItem("Sort Hierarchy")
					.faicon("fas fa-images")
					//.addPermission(PERMISSION_DASHBOARD_VIEWER)
					//.addPermission(PERMISSION_DASHBOARD_CREATOR)
					.href("/app/sorthierarchy?type=spaces")
				, "Spaces");
		
		//----------------------------------
    	// Register Manual
		registerSpacesManual();
	}

	@Override
	public void initializeDB() {
		//-----------------------------------
		// 
//		CFW.DB.Permissions.oneTimeCreate(
//				new Permission(PERMISSION_DASHBOARD_VIEWER, "user")
//					.description("Can view dashboards that other users have shared. Cannot create dashboards, but might edit when allowed by a dashboard creator."),
//					true,
//					false
//				);	
		
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
    	//app.addAppServlet(ServletDashboardList.class,  "/dashboard/list");
	}

	@Override
	public void startTasks() {
		// nothing todo
	}

	@Override
	public void stopFeature() {
		// do nothing
		
	}
	
	private void registerSpacesManual() {
				
		//----------------------------------
		//
//		ROOT_MANUAL_PAGE.addChild(
//				new ManualPage("Introduction")
//					.faicon("fas fa-star")
//					.addPermission(PERMISSION_DASHBOARD_VIEWER)
//					.addPermission(PERMISSION_DASHBOARD_CREATOR)
//					.addPermission(PERMISSION_DASHBOARD_ADMIN)
//					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_00_introduction.html")
//			);
		
			
	}

}
