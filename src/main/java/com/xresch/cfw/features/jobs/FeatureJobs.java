package com.xresch.cfw.features.jobs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.dashboard.FeatureDashboard;
import com.xresch.cfw.features.jobs.channels.CFWJobsChannel;
import com.xresch.cfw.features.jobs.channels.CFWJobsChannelAppLog;
import com.xresch.cfw.features.jobs.channels.CFWJobsChannelEMail;
import com.xresch.cfw.features.jobs.channels.CFWJobsChannelFilesystem;
import com.xresch.cfw.features.jobs.channels.CFWJobsChannelFilesystemSettings;
import com.xresch.cfw.features.jobs.channels.CFWJobsChannelFilesystemSettingsManagement;
import com.xresch.cfw.features.jobs.channels.CFWManualPageChannel;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.parameter.CFWQueryManualPageParameter;
import com.xresch.cfw.features.parameter.ParameterDefinition;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemMenuItem;
import com.xresch.cfw.spi.CFWAppFeature;

public class FeatureJobs extends CFWAppFeature {
	
	private static final String URI_JOBS = "/app/jobs";
	public static final String PACKAGE_RESOURCES = "com.xresch.cfw.features.jobs.resources";
	public static final String PACKAGE_MANUAL = "com.xresch.cfw.features.jobs.manual";
	public static final String PERMISSION_JOBS_USER = "Jobs: User";
	public static final String PERMISSION_JOBS_ADMIN = "Jobs: Admin";
	
	public static final ManualPage MANUAL_ROOT = CFW.Registry.Manual.addManualPage(null, 
			new ManualPage("Jobs")
				.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "jobs_mainpage.html"))
			;
	
	public static final ManualPage MANUAL_CHANNELS = CFW.Registry.Manual.addManualPage("Jobs", 
			new ManualPage("Channels")
				.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "jobs_channels.html"))
			;
	/******************************************************************
	 *
	 ******************************************************************/	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCES);
		CFW.Files.addAllowedPackage(PACKAGE_MANUAL);
		
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
		// Register Job Tasks
		CFW.Registry.Jobs.registerTask(new CFWJobTaskSendMail());
		
		//----------------------------------
		// Register Context Settings
		CFW.Registry.ContextSettings.register(
				  CFWJobsChannelFilesystemSettings.SETTINGS_TYPE
				, CFWJobsChannelFilesystemSettings.class
			);

		//----------------------------------
    	// Register Alerting Channel
		CFW.Registry.JobsReporting.registerChannel(CFWJobsChannelEMail.UNIQUE_NAME, new CFWJobsChannelEMail());
		CFW.Registry.JobsReporting.registerChannel(CFWJobsChannelAppLog.UNIQUE_NAME, new CFWJobsChannelAppLog());

		//----------------------------------
    	// Register Audit
		CFW.Registry.Audit.addUserAudit(new UserAuditExecutorJobTask());
		CFW.Registry.Audit.addUserAudit(new UserAuditExecutorAlertChannel());
		
		//----------------------------------
		// Register Widgets
		CFW.Registry.Widgets.add(new WidgetTriggerJobs());
		
		//----------------------------------
    	// Register Menu				
		CFW.Registry.Components.addToolsMenuItem(
				(CFWHTMLItemMenuItem)new CFWHTMLItemMenuItem("Jobs")
					.faicon("fas fa-play-circle")
					.addPermission(PERMISSION_JOBS_USER)
					.addPermission(PERMISSION_JOBS_ADMIN)
					.href("/app/jobs")
					.addAttribute("id", "cfwMenuTools-Jobs")
				, null);
		
		//----------------------------------
    	// Register Manual
		registerManual();
		
	}

	/******************************************************************
	 *
	 ******************************************************************/
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

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		
		//----------------------------------
		// Add Servlets
		app.addAppServlet(ServletJobs.class,  URI_JOBS);
		
		//----------------------------------
    	// Create Environments
		CFWJobsChannelFilesystemSettingsManagement.initialize();
		
		
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public void startTasks() {
		//----------------------------------------
		// Load Jobs after all features loaded
		for(CFWObject object : CFW.DB.Jobs.getEnabledJobs()) {
			CFW.Registry.Jobs.addJob((CFWJob)object);
		}
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public void stopFeature() {
		// TODO Auto-generated method stub
		
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	public static String getJobsURI() {
		return URI_JOBS;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void registerManual() {
		
		FeatureJobs.registerManualPageChannel(new CFWJobsChannelAppLog());
		FeatureJobs.registerManualPageChannel(new CFWJobsChannelEMail());
		FeatureJobs.registerManualPageChannel(new CFWJobsChannelFilesystem());
		
		
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	public static void registerManualPageChannel(CFWJobsChannel channel) {

		new CFWManualPageChannel(MANUAL_CHANNELS, channel);

	}
	
	

}
