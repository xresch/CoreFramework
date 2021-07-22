package com.xresch.cfw.features.jobs;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.response.bootstrap.MenuItem;

public class FeatureJobs extends CFWAppFeature {
	
	public static final String RESOURCE_PACKAGE = "com.xresch.cfw.features.jobs.resources";
	public static final String PERMISSION_JOBS_USER = "Jobs: User";
	public static final String PERMISSION_JOBS_ADMIN = "Jobs: Admin";
	
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		
		//----------------------------------
		// Register Objects
		CFW.Registry.Objects.addCFWObject(CFWJob.class);
    	
		//----------------------------------
    	// Register Menu				
		CFW.Registry.Components.addRegularMenuItem(
				(MenuItem)new MenuItem("Jobs")
					.faicon("fas fa-play-circle")
					.addPermission(PERMISSION_JOBS_USER)
					.addPermission(PERMISSION_JOBS_ADMIN)
					.href("/app/jobs")
				, FeatureCore.MENU_TOOLS);
		
	}

	@Override
	public void initializeDB() {
		
		//----------------------------------
    	// Permissions
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_JOBS_USER, FeatureUserManagement.CATEGORY_USER)
					.description("User can view and edit his own jobs."),
					true,
					false
			);
		
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_JOBS_ADMIN, FeatureUserManagement.CATEGORY_USER)
					.description("User can view and edit all jobs in the system."),
					true,
					false
			);
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		app.addAppServlet(ServletJobs.class,  "/jobs");
		
	}

	@Override
	public void startTasks() {
		//----------------------------------------
		// Load Jobs after all features loaded
		for(CFWObject object : CFW.DB.Jobs.getEnabledJobs()) {
			CFW.Registry.Jobs.addJob((CFWJob)object);
		}
	}

	@Override
	public void stopFeature() {
		// TODO Auto-generated method stub
		
	}

}
