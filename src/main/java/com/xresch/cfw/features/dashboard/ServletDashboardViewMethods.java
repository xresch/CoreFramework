package com.xresch.cfw.features.dashboard;

import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.datahandling.CFWFormCustomAutocompleteHandler;
import com.xresch.cfw.datahandling.CFWFormHandler;
import com.xresch.cfw.datahandling.CFWMultiForm;
import com.xresch.cfw.datahandling.CFWMultiFormHandler;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWSchedule;
import com.xresch.cfw.datahandling.CFWSchedule.EndType;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.dashboard.parameters.DashboardParameter;
import com.xresch.cfw.features.dashboard.parameters.DashboardParameter.DashboardParameterFields;
import com.xresch.cfw.features.dashboard.parameters.DashboardParameter.DashboardParameterMode;
import com.xresch.cfw.features.dashboard.parameters.ParameterDefinition;
import com.xresch.cfw.features.jobs.CFWDBJob;
import com.xresch.cfw.features.jobs.CFWJob;
import com.xresch.cfw.features.jobs.CFWJob.CFWJobFields;
import com.xresch.cfw.features.jobs.CFWJobTask;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWModifiableHTTPRequest;
import com.xresch.cfw.utils.CFWRandom;
import com.xresch.cfw.validation.ScheduleValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletDashboardViewMethods
{
	private static final Logger logger = CFWLog.getLogger(ServletDashboardViewMethods.class.getName());
	
	
	/*****************************************************************
	 *
	 ******************************************************************/
    protected static void doGet( HttpServletRequest request, HttpServletResponse response, boolean isPublicServlet) throws ServletException, IOException
    {

		if( isPublicServlet
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_VIEWER)
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_CREATOR)
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
			
			String dashboardID = request.getParameter("id");
			String action = request.getParameter("action");
			
			if(action == null) {
				HTMLResponse html = new HTMLResponse("Dashboard");
				StringBuilder content = html.getContent();
				
				//---------------------------
				// Check Access
				if(!CFW.DB.Dashboards.hasUserAccessToDashboard(dashboardID, isPublicServlet)) {
					CFW.Context.Request.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
					return;
				}
				
				//---------------------------
				// Build Response
				html.addCSSFile(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "gridstack.min.css");
				html.addCSSFile(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "cfw_dashboard.css");
				
				//html.addJSFileBottomSingle(new FileDefinition(HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE+".js", "cfw_usermgmt.js"));
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "gridstack.all.js");
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "cfw_dashboard.js");
				
				content.append(CFW.Files.readPackageResource(FeatureDashboard.PACKAGE_RESOURCES, "cfw_dashboard.html"));
				
				//--------------------------------------
				// Add widget CSS and JS files based on
				// user permissions
				CFW.Registry.Widgets.addFilesToResponse(html);

				Dashboard dashboard = CFW.DB.Dashboards.selectByID(dashboardID);
				html.setPageTitle(dashboard.name());
				html.addJavascriptData("dashboardName",  dashboard.name());
				html.addJavascriptData("canEdit", CFW.DB.Dashboards.checkCanEdit(request.getParameter("id")) );
				html.addJavascriptCode("cfw_dashboard_initialDraw();");
				
		        response.setContentType("text/html");
		        response.setStatus(HttpServletResponse.SC_OK);
			}else {
				handleDataRequest(request, response, isPublicServlet);
			}
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
		}
        
    }
	
	/*****************************************************************
	 *
	 *****************************************************************/
    protected static void doPost( HttpServletRequest request, HttpServletResponse response, boolean isPublicServlet) throws ServletException, IOException
    {

		if(isPublicServlet
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_VIEWER)
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_CREATOR)
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
			handleDataRequest(request, response, isPublicServlet);
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
		}
        
    }
	
	/*****************************************************************
	 *
	 *****************************************************************/
	private static void handleDataRequest(HttpServletRequest request, HttpServletResponse response, boolean isPublicServlet) {
		
		String action = request.getParameter("action");
		String item = request.getParameter("item");
		String type = request.getParameter("type");
		String dashboardID = request.getParameter("dashboardid");
		//String ID = request.getParameter("id");
		//String IDs = request.getParameter("ids");
		//int	userID = CFW.Context.Request.getUser().id();
		
		JSONResponse jsonResponse = new JSONResponse();

		switch(action.toLowerCase()) {
		
			case "fetch": 			
				switch(item.toLowerCase()) {
					case "widgetsandparams": 	fetchWidgetsAndParams(jsonResponse, dashboardID, isPublicServlet);
	  											break;	
	  											
					case "widgetdata": 			getWidgetData(request, response, jsonResponse);
												break;	
												
					case "availableparams": 	getAvailableParams(request, jsonResponse, dashboardID);
												break;	
												
					case "settingsform": 		getSettingsForm(request, response, jsonResponse);
												break;	
												
					case "taskparamform": 		getTaskParamForm(request, response, jsonResponse);
												break;	
												
					case "paramform": 			createParameterEditForm(request, response, jsonResponse);
												break;								
					default: 					CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The value of item '"+item+"' is not supported.");
												break;
				}
				break;
				
			case "create": 			
				switch(item.toLowerCase()) {
					case "widget": 				createWidget(jsonResponse, type, dashboardID);
	  											break;
	  											
					case "param": 				createParam(request, response, jsonResponse);
												break;
												
					default: 					CFW.Messages.itemNotSupported(item);
												break;
				}
				break;	
			
			case "duplicate": 			
				switch(item.toLowerCase()) {
					case "widget": 				duplicateWidget(jsonResponse, request.getParameter("widgetid"));
	  											break;
	  											
					default: 					CFW.Messages.itemNotSupported(item);
												break;
				}
				break;	
			case "update": 			
				switch(item.toLowerCase()) {
					case "widget": 				updateWidget(request, response, jsonResponse);
	  											break;
	  																
					default: 					CFW.Messages.itemNotSupported(item);
												break;
				}
				break;
				
			case "delete": 			
				switch(item.toLowerCase()) {
					case "widget": 				deleteWidget(request, response, jsonResponse);
	  											break;
					
					case "param": 				deleteParam(request, response, jsonResponse);
												break;
																	
					default: 					CFW.Messages.itemNotSupported(item);
												break;
				}
				break;	
				
			default: 			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The action '"+action+"' is not supported.");
								break;
								
		}
	}
	
	/*****************************************************************
	 *
	 *****************************************************************/
	private static void fetchWidgetsAndParams(JSONResponse response, String dashboardID, boolean isPublicServlet) {
		
		Dashboard dashboard = CFW.DB.Dashboards.selectByID(dashboardID);
		
		//---------------------------
		// Public Dashboard Check
		if(isPublicServlet && !dashboard.isPublic()){ CFW.Messages.accessDenied(); return; }
		
		//---------------------------
		// Create Response
		
		if(isPublicServlet
		||  dashboard.isShared() 
		|| CFW.DB.Dashboards.checkCanEdit(dashboard) ) {
			
			StringBuilder jsonString = new StringBuilder();
			
			jsonString
				.append("{ \"widgets\": ")
				.append(CFW.DB.DashboardWidgets.getWidgetsForDashboardAsJSON(dashboardID))
				.append(", \"params\": ")
				.append(CFW.DB.DashboardParameters.getParametersForDashboardAsJSON(dashboardID))
				.append("}");
			
			response.getContent().append(jsonString);
		}else{
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Insufficient rights to view this dashboard.");
		}
	}
	
	/*****************************************************************
	 *
	 *****************************************************************/
	private static void createWidget(JSONResponse response, String type, String dashboardID) {

		if(CFW.DB.Dashboards.checkCanEdit(dashboardID)) {
			
			//----------------------------
			// Create Widget
			WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(type);
			User currentUser = CFW.Context.Request.getUser();
			if(definition.hasPermission(currentUser) || CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
				DashboardWidget newWidget = new DashboardWidget();
	
				newWidget.type(type);
				newWidget.foreignKeyDashboard(Integer.parseInt(dashboardID));
				
				if(definition != null) {
					newWidget.settings(definition.getSettings().toJSON());
				}
				
				int id = CFW.DB.DashboardWidgets.createGetPrimaryKey(newWidget);
				newWidget.id(id);
				
				response.getContent().append(CFW.JSON.toJSON(newWidget));
			}else {
				CFW.Messages.noPermission();
			}
		}else{
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Insufficient rights to execute action.");
		}

	}
	
	/*****************************************************************
	 *
	 *****************************************************************/
	private static void duplicateWidget(JSONResponse response, String widgetID) {
		
		DashboardWidget duplicateThis = CFW.DB.DashboardWidgets.selectByID(widgetID);
		duplicateThis.id(null)
		.taskParameters(null);
		
		int dashboardID = duplicateThis.foreignKeyDashboard();
		
		if(CFW.DB.Dashboards.checkCanEdit(dashboardID)) {
			
			//----------------------------
			// Create Widget
			WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(duplicateThis.type());
			User currentUser = CFW.Context.Request.getUser();
			if(definition.hasPermission(currentUser) || CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {

				
				int id = CFW.DB.DashboardWidgets.createGetPrimaryKey(duplicateThis);
				duplicateThis.id(id);
				
				response.getContent().append(CFW.JSON.toJSON(duplicateThis));
			}else {
				CFW.Messages.noPermission();
			}
		}else{
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Insufficient rights to execute action.");
		}

	}
		
	/*****************************************************************
	 *
	 *****************************************************************/
	private static void updateWidget(HttpServletRequest request, HttpServletResponse response, JSONResponse json) {
		
		String dashboardID = request.getParameter("FK_ID_DASHBOARD");

		//Prevent saving when DashboardID is null (e.g. needed for Replica Widget)
		if(Strings.isNullOrEmpty(dashboardID)) {
			return;
		}
		if(CFW.DB.Dashboards.checkCanEdit(dashboardID)) {
			//----------------------------
			// Get Values
			String widgetType = request.getParameter("TYPE");
			String JSON_SETTINGS = request.getParameter("JSON_SETTINGS");
			JsonElement jsonElement = CFW.JSON.fromJson(JSON_SETTINGS);
			WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(widgetType);
			
			User currentUser = CFW.Context.Request.getUser();	
			if(definition.hasPermission(currentUser) || CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
				//----------------------------
				// Validate
				CFWObject settings = definition.getSettings();
				
				boolean isValid = settings.mapJsonFields(jsonElement);
				
				if(isValid) {
					String widgetID = request.getParameter("PK_ID");
					DashboardWidget widgetToUpdate = CFW.DB.DashboardWidgets.selectByID(widgetID);
					
					// check if default settings are valid
					if(widgetToUpdate.mapRequestParameters(request)) {
						//Use sanitized values
						widgetToUpdate.settings(settings.toJSON());
						CFW.DB.DashboardWidgets.update(widgetToUpdate);
					}
				}
			}else {
				CFW.Messages.noPermissionToEdit();
			}
			
		}else{
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Insufficient rights to execute action.");
		}

	}
	
	/*****************************************************************
	 *
	 *****************************************************************/
	private static void deleteWidget(HttpServletRequest request, HttpServletResponse response, JSONResponse json) {
		
		String dashboardID = request.getParameter("dashboardid");
		String widgetID = request.getParameter("widgetid");
		if(CFW.DB.Dashboards.checkCanEdit(dashboardID)) {
			
			DashboardWidget widget = CFW.DB.DashboardWidgets.selectByID(widgetID);
			WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(widget.type());
			User currentUser = CFW.Context.Request.getUser();
			if(definition.hasPermission(currentUser) || CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
				
				boolean success = CFW.DB.DashboardWidgets.deleteByID(widgetID);
				json.setSuccess(success);
			}else {
				CFW.Messages.noPermissionToEdit();
				json.setSuccess(false);
			}
			
		}else{
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Insufficient rights to execute action.");
		}

	}
	
	/*****************************************************************
	 *
	 *****************************************************************/
	private static void getSettingsForm(HttpServletRequest request, HttpServletResponse response, JSONResponse json) {
		
		String dashboardID = request.getParameter("FK_ID_DASHBOARD");

		if(CFW.DB.Dashboards.checkCanEdit(dashboardID)) {
			
			//----------------------------
			// Get Values
			String widgetType = request.getParameter("TYPE");
			String JSON_SETTINGS = request.getParameter("JSON_SETTINGS");
			JsonElement jsonElement = CFW.JSON.fromJson(JSON_SETTINGS);
			WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(widgetType);
			User currentUser = CFW.Context.Request.getUser();
			if(definition.hasPermission(currentUser) || CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
				//----------------------------
				// Create Form
				CFWObject settings = definition.getSettings();
				DashboardParameter.addParameterHandlingToField(settings, dashboardID, widgetType);
				settings.mapJsonFields(jsonElement);
				
				CFWForm form = settings.toForm("cfwWidgetFormSettings"+CFWRandom.randomStringAlphaNumSpecial(6), "n/a-willBeReplacedByJavascript");
				
				form.appendToPayload(json);
			}else {
				CFW.Messages.noPermissionToEdit();
				
			}
			
		}else{
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Insufficient rights to execute action.");
		}

	}
	
	
	/*****************************************************************
	 *
	 *****************************************************************/
	private static void getTaskParamForm(HttpServletRequest request, HttpServletResponse response,JSONResponse json) {
		
		String dashboardID = request.getParameter("dashboardid");
		
		if(CFW.DB.Dashboards.checkCanEdit(dashboardID)) {
			
			String widgetID = request.getParameter("widgetid");
			JsonObject payload = new JsonObject();
			
			//----------------------------
			// Get Values
			DashboardWidget widget = CFW.DB.DashboardWidgets.selectByID(widgetID);
			WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(widget.type());
			User currentUser = CFW.Context.Request.getUser();		
			if( 
				(
					currentUser.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_TASKS)
					|| currentUser.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)
				)
				&& definition.hasPermission(currentUser)
			) {
				
				//----------------------------
				// Check supports Tasks
				if(!definition.supportsTask()) {
					payload.addProperty("supportsTask", false);
					json.getContent().append(payload.toString());
					return;
				}else {
					payload.addProperty("supportsTask", true);
				}
				
				payload.addProperty("taskDescription", definition.getTaskDescription());
				
				//----------------------------
				// Check has Active Job
				boolean hasJob = 0 < CFW.DB.Jobs.getCountByCustomInteger(widget.id());
				payload.addProperty("hasJob", hasJob);
				
				//----------------------------
				// Create Enabled Field
				CFWField<Boolean> isEnabled = CFWField.newBoolean(FormFieldType.BOOLEAN, CFWJobFields.IS_ENABLED)
						.setDescription("Enable or disable the task for this widget.")
						.setValue(true);
				
				//----------------------------
				// Create Schedule Field
				CFWField<CFWSchedule> scheduleField = CFWField.newSchedule(CFWJobFields.JSON_SCHEDULE)
				.setLabel("Schedule")
				.addValidator(new ScheduleValidator().setNullAllowed(false))
				.setValue(
					new CFWSchedule()
						.timeframeStart(Date.from(Instant.now()))
						.endType(EndType.RUN_FOREVER)
				);
				
				if(hasJob) {
					CFWJob job = CFW.DB.Jobs.selectFirstByCustomInteger(widget.id());
					isEnabled.setValue(job.isEnabled());
					scheduleField.setValue(job.schedule());
				}
				
				//----------------------------
				// Create Form
				CFWObject formObject = new CFWObject();
				formObject.addField(isEnabled);
				formObject.addField(scheduleField);
				
				CFWObject taskParams = definition.getTasksParameters();
				taskParams.mapJsonFields(widget.taskParameters());
				formObject.addAllFields(taskParams.getFields());
				
				CFWForm taskParamForm = formObject.toForm("cfwWidgetTaskParamForm"+CFW.Random.randomStringAlphaNumerical(16), "Save");
				
				taskParamForm.setFormHandler(new CFWFormHandler() {
					
					@SuppressWarnings("unchecked")
					@Override
					public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
						
						
						//-------------------------------------
						// Validate and save Task Params to Widget
						if(formObject.mapRequestParameters(request)) {
							widget.taskParameters(taskParams.toJSON());
							if( !CFW.DB.DashboardWidgets.update(widget) ) {
								return;
							}
						}else {
							return;
						}
						
						//-------------------------------------
						// Get Task
						CFWJobTask widgetTaskExecutor = CFW.Registry.Jobs.createTaskInstance(CFWJobTaskWidgetTaskExecutor.UNIQUE_NAME);
						
						//-------------------------------------
						// Load Job from DB if exists, else 
						// create a new Job
						CFWJob jobToSave;
						boolean jobExists = 0 < CFW.DB.Jobs.getCountByCustomInteger(widget.id()) ;
						if(jobExists) {
							jobToSave = CFW.DB.Jobs.selectFirstByCustomInteger(widget.id());
						}else {
							
							CFWObject taskExecutorParams = widgetTaskExecutor.getParameters();
							taskExecutorParams.getField(CFWJobTaskWidgetTaskExecutor.PARAM_DASHBOARD_ID).setValue(dashboardID);
							taskExecutorParams.getField(CFWJobTaskWidgetTaskExecutor.PARAM_WIDGET_ID).setValue(widget.id());
							
							jobToSave = new CFWJob()
									.foreignKeyOwner(CFW.Context.Request.getUser().id())
									.jobname("WidgetID-"+widget.id())
									.description("Auto generated job for widget task.")
									.taskName(widgetTaskExecutor.uniqueName())
									.customInteger(widget.id())
									.properties(taskExecutorParams);
						}
						
						//-------------------------------------
						// Map Params(Schedule/isEnabled) and create/save Job
						if(jobToSave.mapRequestParameters(request)) {

							int scheduleIntervalSec = jobToSave.schedule().getCalculatedIntervalSeconds();
							
							if( !widgetTaskExecutor.isMinimumIntervalValid(scheduleIntervalSec) ) {
								return;
							}
							
							if(jobExists) {
								if(CFWDBJob.update(jobToSave)) {
									CFW.Messages.addSuccessMessage("Update Successful!");
								}
								
							}else {
								if(CFWDBJob.create(jobToSave)) {
									CFW.Messages.addSuccessMessage("Task created Successfully!");
								}
							}
															
						}
					}
				});
		    	
		    	payload.addProperty("html", taskParamForm.getHTML());
			}else {
				CFW.Messages.noPermissionToEdit();
			}
			
			json.getContent().append(payload.toString());
			
		}else{
			CFW.Messages.noPermission();
		}

	}
	
	
	/*****************************************************************
	 *
	 *****************************************************************/
	private static void getWidgetData(HttpServletRequest request, HttpServletResponse response, JSONResponse jsonResponse) {
		
		//----------------------------
		// Get Values
		String widgetID = request.getParameter("widgetid");
		String dashboardParams = request.getParameter("params");
		
		String earliestString = request.getParameter("timeframe_earliest");
		String latestString = request.getParameter("timeframe_latest");
		
		//-----------------------------------
		// Prepare Widget Settings
		long earliest = -1;
		long latest = -1;
		if(!Strings.isNullOrEmpty(earliestString)) { earliest = Long.parseLong(earliestString); }
		if(!Strings.isNullOrEmpty(latestString)) { latest = Long.parseLong(latestString); }
		
		DashboardWidget widget = CFW.DB.DashboardWidgets.selectByID(widgetID);
		
		//-----------------------------------
		// Prepare Widget Settings
		String widgetType = widget.type();
		String JSON_SETTINGS = widget.settings();
		
		//apply Parameters to JSONSettings
		JsonElement jsonSettings = replaceParamsInSettings(JSON_SETTINGS, dashboardParams, widgetType);

		WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(widgetType);
		CFWObject settingsObject = definition.getSettings();
		settingsObject.mapJsonFields(jsonSettings);
		
		//----------------------------
		// Create Response
		if(jsonSettings.isJsonObject()) {
		definition.fetchData(request, jsonResponse, settingsObject, jsonSettings.getAsJsonObject(), earliest, latest);
		}else {
			new CFWLog(logger).warn("Widget Data was not of the correct type.", new IllegalArgumentException());
		}
					
	}
	
	/*****************************************************************
	 * Returns the settings with applied parameters
	 *****************************************************************/
	private static JsonElement replaceParamsInSettings(String jsonSettings, String jsonParams, String widgetType) {
		
		//###############################################################################
		//############################ IMPORTANT ########################################
		//###############################################################################
		// When changing this method you have to apply the same changes in the javascript 
		// method:
		// cfw_dashboard.js >> cfw_dashboard_parameters_applyToWidgetSettings()
		//
		//###############################################################################

		
		// Parameter Sample
		//{"PK_ID":1092,"FK_ID_DASHBOARD":2081,"WIDGET_TYPE":null,"LABEL":"Boolean","PARAM_TYPE":false,"NAME":"boolean","VALUE":"FALSE","MODE":"MODE_SUBSTITUTE","IS_MODE_CHANGE_ALLOWED":false},
		JsonElement dashboardParams = CFW.JSON.fromJson(jsonParams);
		
		//=============================================
		// Handle SUBSTITUTE PARAMS
		//=============================================
		
		// paramSettingsLabel and paramObject
		HashMap<String, JsonObject> globalOverrideParams = new HashMap<>();
		
		if(dashboardParams != null 
		&& !dashboardParams.isJsonNull()
		&& dashboardParams.isJsonArray()
		) {
			JsonArray paramsArray = dashboardParams.getAsJsonArray();
			
			for(JsonElement current : paramsArray) {
				
				//--------------------------------------
				// Handle Timeframe Params.
				// Would throw validation issue if mapped
				// to object.
				String paramName = current.getAsJsonObject().get("NAME").getAsString();
				if(paramName.equals("earliest") || paramName.equals("latest") ) {
					String paramValue = current.getAsJsonObject().get("VALUE").getAsString();
					jsonSettings = jsonSettings.replaceAll("\\$"+paramName+"\\$", paramValue);
					continue;
				}
				
				//--------------------------------------
				// Check is Global Override Parameter
				DashboardParameter paramObject = new DashboardParameter();
				paramObject.mapJsonFields(current);
				
				if(paramObject.mode().equals(DashboardParameterMode.MODE_GLOBAL_OVERRIDE.toString())
				&& ( paramObject.widgetType() == null || paramObject.widgetType().equals(widgetType)) ) {
					globalOverrideParams.put(paramObject.paramSettingsLabel(), current.getAsJsonObject());
					continue;
				}
					
				//--------------------------------------
				// Do Substitute
				// Double escape because Java regex is a bitch.
				String doubleEscaped = CFW.JSON.escapeString(
											CFW.JSON.escapeString(paramObject.value())
										);
				
				if(doubleEscaped == null) {
					doubleEscaped = "";
				}
				
				jsonSettings = jsonSettings.replaceAll("\\$"+paramObject.name()+"\\$", doubleEscaped);
				
				
			}
		}
		
		//=============================================
		// Handle GLOBAL OVERRIDE PARAMS
		//=============================================
		JsonElement settingsElement = CFW.JSON.fromJson(jsonSettings);
		
		if(settingsElement != null 
		&& !settingsElement.isJsonNull()
		&& settingsElement.isJsonObject()
		) {
			JsonObject settingsObject = settingsElement.getAsJsonObject();
			
			for(String paramName : globalOverrideParams.keySet()) {
				
				if (settingsObject.has(paramName)) {
					JsonElement value = globalOverrideParams.get(paramName).get("VALUE");
					settingsObject.add(paramName, value);
				}
			}
				
		}
		
		return settingsElement;
		
	}
		
	
	
	
	/*****************************************************************
	 *
	 *****************************************************************/
	@SuppressWarnings("rawtypes")
	private static void getAvailableParams(HttpServletRequest request, JSONResponse response, String dashboardID) {
		
		if(CFW.DB.Dashboards.checkCanEdit(dashboardID)) {
			
			JsonArray widgetParametersArray = new JsonArray();
			
			//--------------------------------------------
			// Add Params for Widgets on Dashboard
			ArrayList<CFWObject> widgetList = CFW.DB.DashboardWidgets.getWidgetsForDashboard(dashboardID);
			HashSet<String> uniqueTypeChecker = new HashSet<>();
			
			for(CFWObject object : widgetList) {
				
				DashboardWidget widget = (DashboardWidget)object;
				String widgetType = widget.type();
				
				if(widgetType.equals(WidgetParameter.WIDGET_TYPE) 
				|| uniqueTypeChecker.contains(widgetType)) {
					//skip Parameters Widget and type already processed once
					continue;
				}else {
					uniqueTypeChecker.add(widgetType);
					WidgetDefinition definition =  CFW.Registry.Widgets.getDefinition(widgetType);
					if(definition != null
					&& definition.getSettings() != null
					&& definition.getSettings().getFields() != null
					&& definition.getSettings().getFields().entrySet() != null) {
						for(Entry<String, CFWField> entry : definition.getSettings().getFields().entrySet()) {
							CFWField field = entry.getValue();
							JsonObject paramObject = new JsonObject();
							paramObject.addProperty("widgetType", definition.getWidgetType());
							paramObject.addProperty("widgetSetting", field.getName());
							paramObject.addProperty("label", field.getLabel());
							
							widgetParametersArray.add(paramObject);
						}
					}
				}	
			}
			
			//--------------------------------------------
			// Add Params from Definitions
			JsonArray parameterDefArray = new JsonArray();
			
			for(ParameterDefinition def : CFW.Registry.Parameters.getParameterDefinitions().values()) {
				if(def.isAvailable(uniqueTypeChecker)) {
					JsonObject paramObject = new JsonObject();
					paramObject.add("widgetType", null);
					paramObject.add("widgetSetting", null);
					paramObject.addProperty("label", def.getParamLabel());
					
					parameterDefArray.add(paramObject);
				}
			}
			
			parameterDefArray.addAll(widgetParametersArray);
			response.getContent().append(parameterDefArray.toString());
		}else{
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Insufficient rights to load dashboard parameters.");
		}
	}
	
	/*****************************************************************
	 *
	 *****************************************************************/
	@SuppressWarnings("rawtypes")
	private static void createParam(HttpServletRequest request, HttpServletResponse response, JSONResponse json) {
		
		String dashboardID = request.getParameter("dashboardid");

		//Prevent change when DashboardID is null 
		if(Strings.isNullOrEmpty(dashboardID)) {
			return;
		}
		
		if(CFW.DB.Dashboards.checkCanEdit(dashboardID)) {
			
			//----------------------------
			// Get and check Values
			String widgetType = request.getParameter("widgetType");
			String widgetSetting = request.getParameter("widgetSetting");
			String label = request.getParameter("label");
			
			//----------------------------
			// Create Param
			DashboardParameter param = new DashboardParameter();
			param.foreignKeyDashboard(Integer.parseInt(dashboardID));

			if(Strings.isNullOrEmpty(widgetSetting)) {
				param.widgetType(null);
				param.paramSettingsLabel(null);
				
				//----------------------------
				// Handle Default Params
				
				ParameterDefinition def = CFW.Registry.Parameters.getDefinition(label);
				if(def != null) {
					CFWField paramField = def.getFieldForSettings(request, dashboardID, null);
					param.paramType(paramField.fieldType());
					param.paramSettingsLabel(def.getParamLabel());
					param.name(label.toLowerCase().replace(" ", "_")+"_"+CFW.Random.randomStringAlphaNumerical(6));
					param.mode(DashboardParameterMode.MODE_SUBSTITUTE);
					param.isModeChangeAllowed(false);
					
//					if(paramField.fieldType().equals(FormFieldType.SELECT)) {
//						param.isModeChangeAllowed(true);
//					}
				}else {
					CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Parameter definition could not be found for: "+label);
				}


			}else {
				//-------------------------------
				// Check does Widget Exist
				WidgetDefinition definition =  CFW.Registry.Widgets.getDefinition(widgetType);
				if(widgetType != null && definition == null) {
					CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The selected widget type does not exist.");
					return;
				}
				
				//-------------------------------
				// Handle Widget Settings Params
				CFWField settingsField = definition.getSettings().getField(widgetSetting);
				if(settingsField == null) {
					CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The selected field does not does not exist for this widget type.");
					return;
				}else {
					param.widgetType(widgetType);
					param.paramSettingsLabel(widgetSetting);
					param.name(widgetSetting.replace(" ", "_")+"_"+CFW.Random.randomStringAlphaNumerical(6));
					param.paramType(settingsField.fieldType()); // used to fetch similar field types
					param.getField(DashboardParameterFields.VALUE.toString()).setValueConvert(settingsField.getValue());
					param.mode(DashboardParameterMode.MODE_GLOBAL_OVERRIDE);
					
					if(settingsField.fieldType() == FormFieldType.BOOLEAN
					|| settingsField.fieldType() == FormFieldType.NUMBER
					|| settingsField.fieldType() == FormFieldType.DATEPICKER
					|| settingsField.fieldType() == FormFieldType.DATETIMEPICKER
					|| settingsField.fieldType() == FormFieldType.TAGS
					|| settingsField.fieldType() == FormFieldType.TAGS_SELECTOR
					) {
						param.isModeChangeAllowed(false);
					}
				}
			}
			
			//----------------------------
			// Create Parameter in DB
			if(CFW.DB.DashboardParameters.create(param)) {
				
				CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Parameter added!");
			}
			
		}else{
			CFW.Messages.noPermission();
		}

	}
	
	/*****************************************************************
	 *
	 *****************************************************************/
	private static void deleteParam(HttpServletRequest request, HttpServletResponse response, JSONResponse json) {
		
		String dashboardID = request.getParameter("dashboardid");
		
		if(CFW.DB.Dashboards.checkCanEdit(dashboardID)) {
			
			String paramID = request.getParameter("paramid");
			if(CFW.DB.DashboardParameters.checkIsParameterOfDashboard(dashboardID, paramID)) {
				boolean success = CFW.DB.DashboardParameters.deleteByID(paramID);
				json.setSuccess(success);
				CFW.Messages.deleted();
				
				//Remove From Form to avoid errors on save
				String formID = request.getParameter("formid");
				CFWMultiForm form = (CFWMultiForm)CFW.Context.Session.getForm(formID);
				
				form.getOrigins().remove(Integer.parseInt(paramID));
			}
			
			
		}else{
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Insufficient rights to execute action.");
		}

	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void createParameterEditForm(HttpServletRequest request, HttpServletResponse response, JSONResponse json) {

		String dashboardID = request.getParameter("dashboardid");
		ArrayList<CFWObject> parameterList = CFW.DB.DashboardParameters.getParametersForDashboard(dashboardID);
		
		DashboardParameter.prepareParamObjectsForForm(request, parameterList, false);
		
		//===========================================
		// Create Form
		//===========================================
		if(parameterList.size() != 0) {
			
			CFWMultiForm parameterEditForm = new CFWMultiForm("cfwParameterEditMultiForm"+CFW.Random.randomStringAlphaNumerical(12), "Save", parameterList);
			
			parameterEditForm.setMultiFormHandler(new CFWMultiFormHandler() {
				
				@Override
				public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWMultiForm form,
						LinkedHashMap<Integer, CFWObject> originsMap) {
					
					form.mapRequestParameters(request);
					
					//revert uniques of the fields to be able to save to the database.
					form.revertFieldNames();
						for(CFWObject object : originsMap.values()) {
							DashboardParameter param = (DashboardParameter)object;
							
							if(!CFW.DB.DashboardParameters.checkIsParameterNameUsedOnUpdate(param)) {
								//do not update WidgetType and Setting as the values were overridden with labels.
								boolean success = new CFWSQL(param).updateWithout(
										DashboardParameterFields.WIDGET_TYPE.toString(),
										DashboardParameterFields.LABEL.toString());
								
								if(!success) {
									CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The data with the ID '"+param.getPrimaryKey()+"' could not be saved to the database.");
								};
							}else {
								CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The parameter name is already in use: '"+param.name());
							}
						}
						
					//make fieldnames Unique again to be able to save again.
					form.makeFieldNamesUnique();
					CFW.Messages.saved();
				}
				
			});
			parameterEditForm.setCustomAutocompleteHandler(new CFWFormCustomAutocompleteHandler() {
				
				@Override
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, HttpServletResponse response,
						CFWForm form, CFWField field, String searchValue) {
					
					//------------------------------------
					// Create Request with additional Params
					// for the same Widget Type.
					// allows fields using other request params
					// for autocomplete to work properly
					CFWMultiForm multiform = (CFWMultiForm)form;
					
					String paramID = field.getName().split("-")[0];
					int paramIDNumber = Integer.parseInt(paramID);
					LinkedHashMap<Integer, CFWObject> origins = multiform.getOrigins();
					DashboardParameter paramToAutocomplete = (DashboardParameter)origins.get(paramIDNumber);
					String widgetType = paramToAutocomplete.widgetType();
					
					Map<String, String[]> extraParams = new HashMap<String, String[]>();
					if(widgetType != null) {
						//------------------------------------
						//Find all Settings from the same Widget Type
						
						for(CFWObject object : origins.values() ) {
							DashboardParameter currentParam = (DashboardParameter)object;
							if(currentParam.widgetType() != null && currentParam.widgetType().equals(widgetType)) {
								String paramName = currentParam.paramSettingsLabel();
								String valueFieldName = currentParam.id()+"-"+DashboardParameterFields.VALUE;
								String paramValue = request.getParameter(valueFieldName);
						        extraParams.put(paramName, new String[] { paramValue });
							}
						}
					}else {
						String label = paramToAutocomplete.paramSettingsLabel();
						ParameterDefinition def = CFW.Registry.Parameters.getDefinition(label);
						for(CFWObject object : origins.values() ) {
							DashboardParameter currentParam = (DashboardParameter)object;
							
							if(currentParam.widgetType() != null ) {
								HashSet<String> widgetTypesArray = new HashSet<>();
								widgetTypesArray.add(currentParam.widgetType());
								
								if(def.isAvailable(widgetTypesArray)) {
									String currentName = currentParam.paramSettingsLabel();
									String valueFieldName = currentParam.id()+"-"+DashboardParameterFields.VALUE;
									String currentParamValue = request.getParameter(valueFieldName);
							        extraParams.put(currentName, new String[] { currentParamValue });
								}
								
							}
						}
					}
					
					CFWModifiableHTTPRequest modifiedRequest = new CFWModifiableHTTPRequest(request, extraParams);

					//------------------------------------
					// Get Autocomplete Results
			    	if(field.getAutocompleteHandler() != null) {
			    		AutocompleteResult suggestions = field.getAutocompleteHandler().getAutocompleteData(modifiedRequest, searchValue);
			    		return suggestions;
			    	}else {
			    		json.setSuccess(false);
			    		new CFWLog(logger)
				    		.severe("The field with name '"+field.getName()+"' doesn't have an autocomplete handler.");
			    		return null;
			    	}
				}
			});
			
			parameterEditForm.appendToPayload(json);
			json.setSuccess(true);	
		}
	}
	
	

}