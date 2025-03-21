package com.xresch.cfw.extensions.cli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.utils.CFWMonitor;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller 
 * 
 * (c) Copyright 2024
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
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	@Override
	public String widgetCategory() {
		return FeatureCLIExtensions.WIDGET_CATEGORY_CLI;
	}

	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	@Override
	public String widgetName() { return "CLI Results"; }
	
	/***************************************************************************************
	 * Check if the current user has the required permission to create and
	 * edit the widget. Returns true by default.
	 * return true if has permission, false otherwise
	 * @param user TODO
	 ***************************************************************************************/
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureCLIExtensions.PERMISSION_CLI_EXTENSIONS);
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureCLIExtensions.PACKAGE_RESOURCES, "widget_"+getWidgetType()+".html");
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
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

	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	@SuppressWarnings("unchecked")
	@Override
	public void fetchData(HttpServletRequest httpServletRequest, JSONResponse jsonResponse, CFWObject cfwObject, JsonObject jsonObject, CFWTimeframe timeframe) {

		//------------------------------------
		// Get Commands
		String commands = (String) cfwObject.getField(CFWCLIExtensionsCommon.PARAM_COMMANDS).getValue();
		if (Strings.isNullOrEmpty(commands)) {
			return;
		}

		try {
			//----------------------------------------
			// Get Data
			String dataString = executeCommandsAndGetOutput(cfwObject, null, timeframe, null);

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
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	@SuppressWarnings("unchecked")
	private String executeCommandsAndGetOutput(CFWObject widgetSettings, String dashboardParams, CFWTimeframe timeframe, CFWMonitor monitor) throws Exception {
		//------------------------------------
		// Get Working Dir
		String dir = (String) widgetSettings.getField(CFWCLIExtensionsCommon.PARAM_DIR).getValue();

		//------------------------------------
		// Get Commands
		String commands = (String) widgetSettings.getField(CFWCLIExtensionsCommon.PARAM_COMMANDS).getValue();
		if (Strings.isNullOrEmpty(commands)) {
			return "";
		}
		
		commands = commands.replace("$earliest$", ""+timeframe.getEarliest())
						  .replace("$latest$", ""+timeframe.getLatest())
						  ;

		//------------------------------------
		// Get Env Variables
		LinkedHashMap<String,String> envVariables = (LinkedHashMap<String,String>) widgetSettings.getField(CFWCLIExtensionsCommon.PARAM_ENV).getValue();
		
		if(dashboardParams != null) {
			envVariables.put("CFW_DASHBOARD_PARAMS", CFW.JSON.toJSON(dashboardParams) );
		}
		
		//------------------------------------
		// Get Others
		Integer head = (Integer) widgetSettings.getField(CFWCLIExtensionsCommon.PARAM_HEAD).getValue();
		Integer tail = (Integer) widgetSettings.getField(CFWCLIExtensionsCommon.PARAM_TAIL).getValue();
		Integer timeout = (Integer) widgetSettings.getField(CFWCLIExtensionsCommon.PARAM_TIMEOUT).getValue();

		//------------------------------------
		// Get Checks
		Boolean countSkipped = (Boolean) widgetSettings.getField(CFWCLIExtensionsCommon.PARAM_COUNT_SKIPPED).getValue();

		//----------------------------------------
		// Execute Command
		CFWCLIExecutor executor = new CFWCLIExecutor(dir, commands, envVariables); 
		executor.setMonitor(monitor)
				.execute();

		//----------------------------------------
		// Get Data
		String dataString = executor.readOutputOrTimeout(timeout, head, tail, countSkipped);

		return dataString;
			
		
	}

	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> fileDefinitions = new ArrayList<>();
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE,
				FeatureCLIExtensions.PACKAGE_RESOURCES, "widget_"+getWidgetType()+".js");
		fileDefinitions.add(js);
		return fileDefinitions;
	}

	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		return map;
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public boolean supportsTask() {
		return true;
	}
	
	/************************************************************
	 * Override this method to return a description of what the
	 * task of this widget does.
	 ************************************************************/
	public String getTaskDescription() {
		return "Periodically executes the commands on the command line and reports the output to the specified users or groups.";
	}
	
	/************************************************************
	 * Override this method and return a CFWObject containing 
	 * fields for the task parameters. The settings will be passed 
	 * to the 
	 * Always return a new instance, do not reuse a CFWObject.
	 * @return CFWObject
	 ************************************************************/
	public CFWObject getTasksParameters() {
		
		return new CFWJobsAlertObject(true);
	}
	
	/*************************************************************************
	 * Implement the actions your task should execute.
	 * See {@link com.xresch.cfw.features.jobs.CFWJobTask#executeTask CFWJobTask.executeTask()} to get
	 * more details on how to implement this method.
	 *************************************************************************/
	public void executeTask(JobExecutionContext context
						  , CFWObject taskParams
						  , DashboardWidget widget
						  , CFWObject widgetSettings
						  , CFWMonitor monitor
						  , CFWTimeframe offset) throws JobExecutionException {
				
		
		//----------------------------------------
		// Get Custom Data from JobMap
		JobDataMap data = context.getMergedJobDataMap();
		
		Object customData = data.get(CFW.Registry.Jobs.FIELD_CUSTOM);
		
		String dashboardParams = null;
		if(customData instanceof String) {
			dashboardParams = (String)customData;
		}
		
		//----------------------------------------
		// Get CLI Output
		String output;
		try {
			output = executeCommandsAndGetOutput(widgetSettings,dashboardParams, offset, monitor);
		} catch (Exception e) {
			new CFWLog(logger).severe("Task - error while getting CLI output:"+e.getMessage(), e);
			return;
		}	
		
		//----------------------------------------
		// Handle Alerting
		CFWJobsAlertObject alertObject = new CFWJobsAlertObject(context, this.getWidgetType(), true);

		alertObject.mapJobExecutionContext(context);

		//----------------------------------------
		// Prepare Contents
		String widgetLinkHTML = "";
		if(widget != null) {
			widgetLinkHTML = widget.createWidgetOriginMessage();
		}
		
		//----------------------------------------
		// REPORT
		String introText = "Following output has been produced by the executed commands: ";
		String messageHTML = widgetLinkHTML+"<p>"+introText+"</p><pre><code>"+output.replaceAll("<", "&lt;")+"</code></pre>";
		String message = introText+"\n"+output;
		
		alertObject.doSendAlert(context, MessageType.INFO, "Report: Command Line Output", message, messageHTML);
	}
}
