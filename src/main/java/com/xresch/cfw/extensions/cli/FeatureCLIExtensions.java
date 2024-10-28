package com.xresch.cfw.extensions.cli;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.DashboardWidget.DashboardWidgetFields;
import com.xresch.cfw.features.keyvaluepairs.KeyValuePair;
import com.xresch.cfw.features.keyvaluepairs.KeyValuePair.KeyValuePairFields;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.query.sources.CFWQuerySourceApplog;
import com.xresch.cfw.features.query.sources.CFWQuerySourceAuditlog;
import com.xresch.cfw.features.query.sources.CFWQuerySourceEmpty;
import com.xresch.cfw.features.query.sources.CFWQuerySourceJson;
import com.xresch.cfw.features.query.sources.CFWQuerySourceRandom;
import com.xresch.cfw.features.query.sources.CFWQuerySourceText;
import com.xresch.cfw.features.query.sources.CFWQuerySourceThreaddump;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.features.usermgmt.Permission.PermissionFields;
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
	public static final String WIDGET_CATEGORY_CLI = "CLI";
	
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
		return "Extensions to execute command on the CLI of the application server.(Widgets, Source ...)";
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
		//CFW.Registry.Widgets.add(new WidgetCLIResults());
		
		//----------------------------------
		// Register Sources
		CFW.Registry.Query.registerSource(new CFWQuerySourceCLI(null));
				
		//----------------------------------
		// Register Manual Page
		CFW.Registry.Manual.addManualPage(null,
				new ManualPage(FEATURE_NAME)
					.faicon("fas fa-code")
					.addPermission(FeatureCLIExtensions.PERMISSION_CLI_EXTENSIONS)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "widget_cfw_cli.html")
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
}
