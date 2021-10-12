package com.xresch.cfw.features.jobs;

import java.util.Locale;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.dashboard.FeatureDashboard;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.response.bootstrap.MenuItem;
import com.xresch.cfw.spi.CFWAppFeature;

public class FeatureJobs extends CFWAppFeature {
	
	private static final String URI_JOBS = "/app/jobs";
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
		// Register Languages for Dashboard
		// Needed for default fields created 
		// by WidgetSettingsFactory. 
		CFW.Localization.registerLocaleFile(Locale.ENGLISH, getJobsURI(), new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "lang_en_dashboard.properties"));
		CFW.Localization.registerLocaleFile(Locale.GERMAN, getJobsURI(), new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "lang_de_dashboard.properties"));
		
		//----------------------------------
    	// Register Alerting Channel
		CFW.Registry.JobsAlerting.registerChannel(new CFWJobsAlertingChannelEMail());
		CFW.Registry.JobsAlerting.registerChannel(new CFWJobsAlertingChannelAppLog());
		
		//----------------------------------
    	// Register Audit
		CFW.Registry.Audit.addUserAudit(new UserAuditExecutorJobTask());
		CFW.Registry.Audit.addUserAudit(new UserAuditExecutorAlertChannel());
		
		//----------------------------------
    	// Register Menu				
		CFW.Registry.Components.addToolsMenuItem(
				(MenuItem)new MenuItem("Jobs")
					.faicon("fas fa-play-circle")
					.addPermission(PERMISSION_JOBS_USER)
					.addPermission(PERMISSION_JOBS_ADMIN)
					.href("/app/jobs")
				, null);
		
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
		app.addAppServlet(ServletJobs.class,  URI_JOBS);
		
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
	
	public static String getJobsURI() {
		return URI_JOBS;
	}

}
