package com.xresch.cfw.extensions.cli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;

/**************************************************************************************************************
 * 
 * @author Joel Laeubin (Base implementation)
 * @author Reto Scheiwiller (integration into EMP, ehancements etc...)
 * 
 * (c) Copyright 2022 
 * 
 * @license MIT-License
 **************************************************************************************************************/
public class WidgetCLIResults extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(WidgetCLIResults.class.getName());
	

	
	// Returns the unique name of the widget. Has to be the same unique name as used in the javascript part.
	@Override
	public String getWidgetType() {
		return FeatureCLIExtensions.WIDGET_PREFIX+"_results"; 
	}
	
	@Override
	public WidgetDataCache.WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCache.WidgetDataCachePolicy.ALWAYS;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetCategory() {
		return FeatureCLIExtensions.WIDGET_CATEGORY_CLI;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetName() { return "CLI Results"; }
	
	/************************************************************
	 * Check if the current user has the required permission to create and
	 * edit the widget. Returns true by default.
	 * return true if has permission, false otherwise
	 * @param user TODO
	 ************************************************************/
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureCLIExtensions.PERMISSION_CLI_EXTENSIONS);
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureCLIExtensions.PACKAGE_RESOURCES, "widget_"+getWidgetType()+".html");
	}

	// Creates an object with fields that will be used as the settings for this particular widget.
	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				
				.addField(CFWCLIExtensionsCommon.createSettingsFieldWorkingDir())
				.addField(CFWCLIExtensionsCommon.createSettingsFieldCommands())
				.addField(CFWCLIExtensionsCommon.createSettingsFieldEnvVariables())
				.addField(CFWCLIExtensionsCommon.createSettingsFieldHead())
				.addField(CFWCLIExtensionsCommon.createSettingsFieldTail())
				.addField(CFWCLIExtensionsCommon.createSettingsFieldCountSkipped())
				.addField(CFWCLIExtensionsCommon.createSettingsFieldTimeout())				
			;
				
				

	}

	@SuppressWarnings("unchecked")
	@Override
	public void fetchData(HttpServletRequest httpServletRequest, JSONResponse jsonResponse, CFWObject cfwObject, JsonObject jsonObject, CFWTimeframe timeframe) {

		//------------------------------------
		// Get Working Dir
		String dir = (String) cfwObject.getField(CFWCLIExtensionsCommon.PARAM_DIR).getValue();

		//------------------------------------
		// Get Commands
		String commands = (String) cfwObject.getField(CFWCLIExtensionsCommon.PARAM_COMMANDS).getValue();
		if (Strings.isNullOrEmpty(commands)) {
			return;
		}

		
		//------------------------------------
		// Get Env Variables
		LinkedHashMap<String,String> envVariables = (LinkedHashMap<String,String>) cfwObject.getField(CFWCLIExtensionsCommon.PARAM_ENV).getValue();

		//------------------------------------
		// Get Others
		Integer head = (Integer) cfwObject.getField(CFWCLIExtensionsCommon.PARAM_HEAD).getValue();
		Integer tail = (Integer) cfwObject.getField(CFWCLIExtensionsCommon.PARAM_TAIL).getValue();
		Integer timeout = (Integer) cfwObject.getField(CFWCLIExtensionsCommon.PARAM_TIMEOUT).getValue();


		//------------------------------------
		// Get Checks
		Boolean countSkipped = (Boolean) cfwObject.getField(CFWCLIExtensionsCommon.PARAM_COUNT_SKIPPED).getValue();
		

		try {
			//----------------------------------------
			// Execute Command
			CFWCLIExecutor executor = new CFWCLIExecutor(dir, commands, envVariables); 
			executor.execute();

			//----------------------------------------
			// Get Data
			String dataString;
			dataString = executor.readOutputOrTimeout(timeout, head, tail, countSkipped);

			//----------------------------------------
			// Create Result
			JsonObject result = new JsonObject();
			result.addProperty("output", dataString);
			jsonResponse.setPayload(result);
			
		} catch (Exception e) {
			new CFWLog(logger).severe(widgetName()+": Error while getting data:"+e.getMessage(), e);
		}
		
		return;

	}

	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> fileDefinitions = new ArrayList<>();
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE,
				FeatureCLIExtensions.PACKAGE_RESOURCES, "widget_"+getWidgetType()+".js");
		fileDefinitions.add(js);
		return fileDefinitions;
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		return map;
	}
}
