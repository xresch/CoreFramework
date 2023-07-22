package com.xresch.cfw.extensions.web;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.db.CFWSQL;
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

public class FeatureWebExtensions extends CFWAppFeature {

	private static Logger logger = CFWLog.getLogger(WidgetWebEvaluateResponse.class.getName());
	
	// Fields
	public static final String PACKAGE_RESOURCES = "com.xresch.cfw.extensions.web.resources";

	public static final String FEATURE_NAME = "Web Extensions";
	public static final String PERMISSION_WEB_EXTENSIONS = FEATURE_NAME;
	public static final String WIDGET_PREFIX = "cfw_webextensions";
	
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
		return "Extensions to work with Web Requests.(Widgets, Source ...)";
	};
	
	/************************************************************************************
	 * Return if the feature is active by default or if the admin has to enable it.
	 ************************************************************************************/
	public boolean activeByDefault() {
		return true;
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
		CFW.Registry.Widgets.add(new WidgetWebEvaluateResponse());
		
		//----------------------------------
		// Register Sources
		CFW.Registry.Query.registerSource(new CFWQuerySourceWeb(null));
				
		//----------------------------------
		// Register Manual Page
		CFW.Registry.Manual.addManualPage(null,
				new ManualPage(FEATURE_NAME)
					.faicon("fas fa-code")
					.addPermission(FeatureWebExtensions.PERMISSION_WEB_EXTENSIONS)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "manual_webextensions.html")
			);
	}

	/************************************************************************************
	 *
	 ************************************************************************************/
	@Override
	public void initializeDB() {
		
		//----------------------------------
		// Migration
		new CFWLog(logger).off("Migration: Rename HTTP Extensions to "+FEATURE_NAME);
		String oldName = "HTTP Extensions";
		if(CFW.DB.Permissions.checkExistsByName(oldName)) {
			Permission permission = CFW.DB.Permissions.selectByName(oldName);
			permission.name(PERMISSION_WEB_EXTENSIONS);
			permission.update(PermissionFields.NAME);
		}
		
		if(CFW.DB.KeyValuePairs.checkKeyExists(CFWAppFeature.KEY_VALUE_PREFIX+oldName)) {
			KeyValuePair httpPair = CFW.DB.KeyValuePairs.selectByKey(CFWAppFeature.KEY_VALUE_PREFIX+oldName);
			String httpValue = httpPair.value();
			
			KeyValuePair webPair = CFW.DB.KeyValuePairs.selectByKey(CFWAppFeature.KEY_VALUE_PREFIX+FEATURE_NAME);
			webPair.value(httpValue);
			webPair.update(KeyValuePairFields.KEY);
			
			CFW.DB.KeyValuePairs.deleteByID(httpPair.id());
		}
		
		ArrayList<DashboardWidget> widgetsToUpdate = new CFWSQL(new DashboardWidget())
				.select()
				.where(DashboardWidgetFields.TYPE, "emp_httpextensions_evaluateresponse")
				.getAsObjectListConvert(DashboardWidget.class);
		
		for(DashboardWidget widget : widgetsToUpdate) {
			widget.type(WIDGET_PREFIX+"_evaluateresponse")
				  .update(DashboardWidgetFields.TYPE);
		}
				
		
		//----------------------------------
		// Permissions
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_WEB_EXTENSIONS, FeatureUserManagement.CATEGORY_USER)
					.description("Allows to use the Web Extensions(Widgets, Sources etc...)."),
				true,
				false);
		
		
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
