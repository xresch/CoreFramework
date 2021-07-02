package com.xresch.cfw.features.jobs;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.features.usermgmt.Permission;

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
		//CFW.Registry.Objects.addCFWObject(CPUSampleSignature.class);
    	
    	//----------------------------------
    	// Register Admin Menu

//		CFW.Registry.Components.addAdminCFWMenuItem(
//				(MenuItem)new MenuItem("DB Analytics")
//					.faicon("fas fa-database")
//					.addPermission(FeatureCore.PERMISSION_APP_ANALYTICS)
//					.href("/app/dbanalytics")	
//				, null);
		
	}

	@Override
	public void initializeDB() {
		
		//----------------------------------
    	// Permissions
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_JOBS_USER, "user")
					.description("User can view and edit his own jobs."),
					true,
					false
			);
		
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_JOBS_ADMIN, "user")
					.description("User can view and edit all jobs in the system."),
					true,
					false
			);
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	

		  
	}

	@Override
	public void startTasks() {

	}

	@Override
	public void stopFeature() {
		// TODO Auto-generated method stub
		
	}

}
