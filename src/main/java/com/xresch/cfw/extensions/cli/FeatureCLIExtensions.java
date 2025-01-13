package com.xresch.cfw.extensions.cli;

import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFW.DB;
import com.xresch.cfw._main.CFW.DB.Config;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.query.CFWJobTaskAlertingCFWQLQuery;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.spi.CFWAppFeature;

public class FeatureCLIExtensions extends CFWAppFeature {

	private static Logger logger = CFWLog.getLogger(WidgetCLIResults.class.getName());
	
	// Fields
	public static final String PACKAGE_RESOURCES = "com.xresch.cfw.extensions.cli.resources";
	
	public static final String FEATURE_NAME = "CLI Extensions";
	
	public static final String PERMISSION_CLI_EXTENSIONS = FEATURE_NAME;
	
	public static final String CONFIG_CATEGORY = FEATURE_NAME;
	public static final String CONFIG_DEFAULT_WORKDIR = "Default Working Directory";
	
	public static final String WIDGET_PREFIX = "cfw_cliextensions";
	public static final String WIDGET_CATEGORY_CLI = "Command Line";
	
	/************************************************************************************
	 * Override to make it managed and return something else then null.
	 ************************************************************************************/
	@Override
	public String getNameForFeatureManagement() {
		return FEATURE_NAME;
	};
	
	/************************************************************************************
	 * Register a description for the feature management.
	 ************************************************************************************/
	@Override
	public String getDescriptionForFeatureManagement() {
		return "Extensions to execute commands on the CLI of the application server.(Widgets, Source ...)";
	};
	
	/************************************************************************************
	 * Return if the feature is active by default or if the admin has to enable it.
	 ************************************************************************************/
	public boolean activeByDefault() {
		return false;
	};
	
	/************************************************************************************
	 *
	 ************************************************************************************/
	@Override
	public void register() {

		//----------------------------------
		// Register packages
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCES);

		//----------------------------------
		// Register Widget
		CFW.Registry.Widgets.add(new WidgetCLIResults());
		
		//----------------------------------
		// Register Sources
		CFW.Registry.Query.registerSource(new CFWQuerySourceCLI(null));
		
		//----------------------------------
		// Register Job Tasks
		CFW.Registry.Jobs.registerTask(new CFWJobTaskExecuteCommandLine());
		
		//----------------------------------
		// Register ContextSettings
		CFW.Registry.ContextSettings.register(
						  CFWJobsChannelCLISettings.SETTINGS_TYPE
						, CFWJobsChannelCLISettings.class
					);

		//----------------------------------
		// Register Manual Page
		CFW.Registry.Manual.addManualPage(null,
				new ManualPage(FEATURE_NAME)
					.faicon("fas fa-code")
					.addPermission(FeatureCLIExtensions.PERMISSION_CLI_EXTENSIONS)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "manual_cli_extensions.html")
			);
	}

	/************************************************************************************
	 *
	 ************************************************************************************/
	@Override
	public void initializeDB() {			
		
		//============================================================
		// Permissions
		//============================================================
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_CLI_EXTENSIONS, FeatureUserManagement.CATEGORY_USER)
					.description("Allows to use the CLI Extensions(Widgets, Sources etc...)."),
				true,
				false);
		
		//============================================================
		// CONFIGURATION
		//============================================================
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration(CONFIG_CATEGORY, CONFIG_DEFAULT_WORKDIR)
				.description("The default working directory. If this is not set, the default will be the working directory of the application process.")
				.type(FormFieldType.TEXT)
		);
		
	}
	
	/************************************************************************************
	 *
	 ************************************************************************************/
	@Override
	public void addFeature(CFWApplicationExecutor cfwApplicationExecutor) {
		//-----------------------------------------
		// Add Context Settings
		CFWJobsChannelCLISettingsManagement.initialize();
				
		
	}

	/************************************************************************************
	 *
	 ************************************************************************************/
	@Override
	public void startTasks() {

	}

	/************************************************************************************
	 *
	 ************************************************************************************/
	@Override
	public void stopFeature() {

	}

	/******************************************************************
	 *
	 ******************************************************************/
	public static String getDefaultFolderDescription() {
		
		String defaultDir = CFW.DB.Config.getConfigAsString(CONFIG_CATEGORY, CONFIG_DEFAULT_WORKDIR); 
		if(Strings.isNullOrEmpty(defaultDir)) {
			defaultDir = "App Root Directory";
		}
		
		return defaultDir;
	}
}
