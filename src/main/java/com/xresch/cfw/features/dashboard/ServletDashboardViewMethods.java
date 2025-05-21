package com.xresch.cfw.features.dashboard;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.datahandling.CFWFormHandler;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWSchedule;
import com.xresch.cfw.datahandling.CFWSchedule.EndType;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.dashboard.DashboardWidget.DashboardWidgetFields;
import com.xresch.cfw.features.dashboard.widgets.ManualPageWidget;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetParameter;
import com.xresch.cfw.features.jobs.CFWDBJob;
import com.xresch.cfw.features.jobs.CFWJob;
import com.xresch.cfw.features.jobs.CFWJob.CFWJobFields;
import com.xresch.cfw.features.jobs.CFWJobTask;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.parameter.CFWParameter;
import com.xresch.cfw.features.parameter.CFWParameter.CFWParameterScope;
import com.xresch.cfw.features.parameter.FeatureParameter;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.utils.CFWRandom;
import com.xresch.cfw.utils.undoredo.UndoRedoHistory;
import com.xresch.cfw.utils.undoredo.UndoRedoManager;
import com.xresch.cfw.utils.undoredo.UndoRedoOperation;
import com.xresch.cfw.validation.ScheduleValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class ServletDashboardViewMethods
{
	private static final Logger logger = CFWLog.getLogger(ServletDashboardViewMethods.class.getName());
	
	private static final UndoRedoManager<ArrayList<DashboardWidget>> undoredoManager = 
									new UndoRedoManager<ArrayList<DashboardWidget>>("Dashboard", 30);

	private static Method methodResetToState = null;
	
	static {
		try {
			methodResetToState = ServletDashboardViewMethods.class.getMethod("undoredo_executeResetToState", new ArrayList<DashboardWidget>().getClass());
		} catch (Exception e) {
			new CFWLog(logger).severe("Error during reflection of method: "+e.getMessage(), e);
		}
	}
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
			
			// null if not logged in
			User currentUser = CFW.Context.Request.getUser();
			

			if(action == null) {
				HTMLResponse html = new HTMLResponse("Dashboard");
				
				StringBuilder content = html.getContent();
				
				//---------------------------
				// Check Access
				if(!CFW.DB.Dashboards.hasUserAccessToDashboard(dashboardID, isPublicServlet)) {
					CFW.Messages.addErrorMessage(CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
					return;
				}
				
				//---------------------------
				// Count PageLoad Statistics
				CFWDBDashboard.pushEAVStats(FeatureDashboard.EAV_STATS_PAGE_LOADS, dashboardID, 1);

				//---------------------------
				// Clear existing History
				undoredo_clearHistory(CFW.Context.Request.getUserID(), dashboardID);
				
				//---------------------------
				// Add CSS and JS
				html.addCSSFile(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "gridstack-5.1.0.min.css");
				html.addCSSFile(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "cfw_dashboard.css");
				
				//html.addJSFileBottomSingle(new FileDefinition(HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE+".js", "cfw_usermgmt.js"));
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "gridstack-h5-5.1.0.js");
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureParameter.PACKAGE_RESOURCES, "cfw_parameter.js");
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "cfw_dashboard_common.js");
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "cfw_dashboard_view.js");
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureManual.PACKAGE_RESOURCES, "cfw_manual_common.js"); // needed to make links work
				
				//---------------------------
				// Add HTML
				String htmlTemplate = CFW.Files.readPackageResource(FeatureDashboard.PACKAGE_RESOURCES, "cfw_dashboard.html");
				
				if(isPublicServlet || !CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_FAST_RELOAD)) {
					htmlTemplate = htmlTemplate.replace("$$fastreload$$", "");
				}else {
					htmlTemplate = htmlTemplate.replace(
							"$$fastreload$$"
							, "<option value=\"60000\">1 {!cfw_core_minute!}</option>"
							+ "<option value=\"120000\">2 {!cfw_core_minutes!}</option>"
						);
				}
				
				content.append(htmlTemplate);
				
				
				//--------------------------------------
				// Add widget CSS and JS files based on
				// user permissions
				CFW.Registry.Widgets.addFilesToResponse(html);

				//--------------------------------------
				// Check exists
				Dashboard dashboard = CFW.DB.Dashboards.selectByID(dashboardID);
				if(dashboard == null) {
					CFW.Messages.addWarningMessage("The dashboard with the id '"+dashboardID+"' does not exist.");
					content.setLength(0);
					return;
				}
				
				//--------------------------------------
				// Add Version/Archive Warning
				if(dashboard.version() != 0) {
					
					Integer currentID = CFW.DB.Dashboards.getCurrentVersionForDashboard(dashboard);
					String url = FeatureDashboard.createURLForDashboard(currentID);
					CFW.Messages.addWarningMessage("Your are currently viewing version <b>"+dashboard.version()+"</b> of this dashboard, "
								+"not the <a href=\""+url+"\">current</a> version.");
					
				}
				
				if(dashboard.isArchived()) {
					CFW.Messages.addWarningMessage("This dashboard is in the archive and might get deleted in the future.");
				}
				
				//--------------------------------------
				// Add Page Data
				html.setPageTitle(dashboard.name());
				html.addJavascriptData("dashboardName",  dashboard.name());
				
				if(currentUser != null) {
					html.addJavascriptData("dashboardIsFaved",  CFW.DB.DashboardFavorites.checkIsDashboardFavedByUser(dashboard, currentUser));
				}
				
				html.addJavascriptData("startFullscreen", dashboard.startFullscreen() );
				html.addJavascriptData("isOwner", dashboard.foreignKeyOwner() == CFW.Context.Request.getUserID() );
				html.addJavascriptData("canEdit", CFW.DB.Dashboards.checkCanEdit(request.getParameter("id")) );
				html.addJavascriptData("canEditSettings",  dashboard.alloweEditSettings());
				html.addJavascriptCode("cfw_dashboard_initialDraw();");
				
		        response.setContentType("text/html");
		        response.setStatus(HttpServletResponse.SC_OK);
			}else {
				handleActionRequest(request, response, isPublicServlet);
			}
		}else {
			CFW.Messages.addErrorMessage(CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
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
			handleActionRequest(request, response, isPublicServlet);
		}else {
			CFW.Messages.addErrorMessage(CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
		}
        
    }
    
    
    /*****************************************************************
	 *
	 *****************************************************************/
	private static void handleActionRequest(HttpServletRequest request, HttpServletResponse response, boolean isPublicServlet) {
		
		String action = request.getParameter("action");
		String item = request.getParameter("item");
		String dashboardID = request.getParameter("dashboardid");
		Integer userID = CFW.Context.Request.getUserID();
		
		JSONResponse jsonResponse = new JSONResponse();

		switch(action.toLowerCase()) {
		
			case "fetch": 			
				switch(item.toLowerCase()) {
					case "widgetsandparams": 	fetchWidgetsAndParams(jsonResponse, dashboardID, isPublicServlet);
	  											break;	
	  				
					case "widgetcopy": 			getWidgetForCopy(request, response, jsonResponse);
												break;	
					
					case "widgetdata": 			fetchWidgetData(request, response, jsonResponse);
												break;	
												
					case "manualpage": 			getManualPage(request, jsonResponse);
												break;	
												
					case "settingsformwidget":  getSettingsFormWidget(request, response, jsonResponse);
												break;	
												
					case "settingsformdefault": getSettingsFormDefault(request, response, jsonResponse);
												break;	
												
					case "taskparamform": 		getTaskParamForm(request, response, jsonResponse);
												break;	
												
												
					case "paramwidgetpwcheck": 	WidgetParameter.checkParameterWidgetPassword(request, response, jsonResponse);
												break;
												
					default: 					CFW.Messages.addErrorMessage("The value of item '"+item+"' is not supported.");
												break;
				}
				break;
				
			case "create": 			
				switch(item.toLowerCase()) {
					case "widget": 				createWidget(request, response, jsonResponse);
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
			
			case "undoredo": 			
				switch(item.toLowerCase()) {
					case "startbundle": 		undoredo_operationBundleStart(userID, dashboardID);
	  											break;
	  											
					case "endbundle": 			undoredo_operationBundleEnd(userID, dashboardID);
												break;
					
					case "triggerundo": 		undoredo_triggerUndoRedo(request, jsonResponse, true, isPublicServlet);
												break;
												
					case "triggerredo": 		undoredo_triggerUndoRedo(request, jsonResponse, false, isPublicServlet);
												break;
					
					default: 					CFW.Messages.itemNotSupported(item);
												break;
				}
				break;	
			
			case "update": 			
				switch(item.toLowerCase()) {
					case "widgetfull": 				updateWidget(request, response, jsonResponse, false);
	  												break;
	  												
					case "widgetdefaultsettings": 	updateWidget(request, response, jsonResponse, true);
													break;
	  														
					default: 					CFW.Messages.itemNotSupported(item);
												break;
				}
				break;
				
			case "delete": 			
				switch(item.toLowerCase()) {
					case "widget": 				deleteWidget(request, response, jsonResponse);
	  											break;
																	
					default: 					CFW.Messages.itemNotSupported(item);
												break;
				}
				break;	
				
			default: 			CFW.Messages.addErrorMessage("The action '"+action+"' is not supported.");
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
		// Count PageLoad Statistics
		CFWDBDashboard.pushEAVStats(FeatureDashboard.EAV_STATS_PAGE_LOADS_AND_REFRESHES, dashboardID, 1);
		
		//---------------------------
		// Create Response
		
		if(isPublicServlet
		||  dashboard.isShared() 
		|| CFW.DB.Dashboards.checkCanEdit(dashboard) ) {
				
			StringBuilder jsonString = new StringBuilder();
			
			jsonString
				.append("{ \"widgets\": ")
				// filters out any field flagged with SERVER_SIDE_ONLY
				.append(CFW.DB.DashboardWidgets.getWidgetsForDashboardAsJSON(dashboardID))
				.append(", \"params\": ")
				.append(CFW.DB.Parameters.getParametersForDashboardAsJSON(dashboardID))
				.append("}");
			
			response.getContent().append(jsonString);
		}else{
			CFW.Messages.addErrorMessage("Insufficient rights to view this dashboard.");
		}
	}
	
	/*****************************************************************
	 *
	 *****************************************************************/
	private static void createWidget(HttpServletRequest request, HttpServletResponse response, JSONResponse json) {

		String dashboardID = request.getParameter("dashboardid");
		
		if(CFW.DB.Dashboards.checkCanEdit(dashboardID)) {
			
			String type = request.getParameter("type");
			boolean withData = Boolean.parseBoolean(request.getParameter("withdata"));
			String data = request.getParameter("data");
			
			//----------------------------
			// Create Widget
			WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(type);
			User currentUser = CFW.Context.Request.getUser();
			if(definition.hasPermission(currentUser) || CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
				DashboardWidget newWidget = new DashboardWidget();
	
				//----------------------------
				// Prepare Data
				if(!withData) {
					newWidget.type(type);
					if(definition != null) {
						newWidget.settings(definition.getSettings().toJSON());
					}
				}else {
					newWidget.mapJsonFields(data, false, true);
				}
				
				//overwrite parent dashboard, make sure id is null, e.g. from Copy & paste
				newWidget.foreignKeyDashboard(Integer.parseInt(dashboardID));
				newWidget.id(null);
				
				int id = CFW.DB.DashboardWidgets.createGetPrimaryKey(newWidget);
				newWidget.id(id);
				
				json.getContent().append(CFW.JSON.toJSON(newWidget));
			}else {
				CFW.Messages.noPermission();
			}
		}else{
			CFW.Messages.addErrorMessage("Insufficient rights to execute action.");
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
				duplicateThis.id(id); // needed because of caching
				duplicateThis.x(null);
				duplicateThis.y(null);
				response.getContent().append(CFW.JSON.toJSON(duplicateThis));
			}else {
				CFW.Messages.noPermission();
			}
		}else{
			CFW.Messages.addErrorMessage("Insufficient rights to execute action.");
		}

	}
		
	/*****************************************************************
	 *
	 *****************************************************************/
	private static void updateWidget(HttpServletRequest request, HttpServletResponse response, JSONResponse json, boolean defaultSettingsOnly) {
		
		
		String dashboardID = request.getParameter("dashboardid");

		//Prevent saving when DashboardID is null (e.g. needed for Replica Widget)
		if(Strings.isNullOrEmpty(dashboardID)) {
			return;
		}
		
		if(CFW.DB.Dashboards.checkCanEdit(dashboardID)) {
			
			//----------------------------
			// Get Values
			String widgetDataString = request.getParameter("widget");
			JsonObject widgetDataJson = CFW.JSON.fromJson(widgetDataString).getAsJsonObject();
			String widgetType = widgetDataJson.get(DashboardWidgetFields.TYPE.toString()).getAsString();
			String dashboardParams = request.getParameter(FeatureParameter.CFW_PARAMS);
			
			WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(widgetType);
			
			User currentUser = CFW.Context.Request.getUser();	
			if(definition.hasPermission(currentUser) 
			|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
				
				//----------------------------
				// Map and Validate Data from Client
				DashboardWidget tempWidget = new DashboardWidget();
				boolean isValid = tempWidget.mapJsonFields(widgetDataString, true, true);
				if(!isValid) {
					CFW.Messages.addErrorMessage("Validation of Widget Data failed.");
					return;
				}
				
				CFWObject settings = definition.getSettings();
				isValid = settings.mapJsonFields(tempWidget.settings(), true, true);
				if(!isValid) {
					CFW.Messages.addErrorMessage("Validation of Widget Settings failed.");
					return;
				}
				
				//----------------------------
				// Validate canSave()
				
				CFWObject settingsWithParams = definition.getSettings();
				JsonElement settingsWithParamsElement = CFWParameter.replaceParamsInSettings(tempWidget.settings(), dashboardParams, widgetType);
				settingsWithParams.mapJsonFields(settingsWithParamsElement, false, false);
				
				if(!definition.canSave(request, json, settings, settingsWithParams)) {
					CFW.Messages.addErrorMessage("Widget cannot be saved.");
					return;
				}

				//----------------------------
				// Save to Database
				int widgetID = tempWidget.id();
				DashboardWidget widgetFromDB = CFW.DB.DashboardWidgets.selectByID(widgetID);

				// check if default settings are valid
				if(widgetFromDB.mapJsonFields(widgetDataJson, true, true)) {
					//Use sanitized values
					widgetFromDB.settings(settings.toJSONEncrypted());
					if(!defaultSettingsOnly) {
						CFW.DB.DashboardWidgets.updateWithout(widgetFromDB
												, DashboardWidgetFields.JSON_TASK_PARAMETERS.toString());
					}else {
						// needed to not override these settings
						// needed also for security reasons (user cannot override task params when not having permissions)
						CFW.DB.DashboardWidgets.updateWithout(
										widgetFromDB
										, DashboardWidgetFields.JSON_SETTINGS.toString()
										, DashboardWidgetFields.JSON_TASK_PARAMETERS.toString()
									);
					}
					
				}
			}else {
				CFW.Messages.noPermissionToEdit();
			}
			
		}else{
			CFW.Messages.addErrorMessage("Insufficient rights to execute action.");
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
				CFW.DB.Dashboards.updateLastUpdated(dashboardID);
				
				json.setSuccess(success);
			}else {
				CFW.Messages.noPermissionToEdit();
				json.setSuccess(false);
			}
			
		}else{
			CFW.Messages.addErrorMessage("Insufficient rights to execute action.");
		}

	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private static void getManualPage(HttpServletRequest request, JSONResponse jsonResponse) {
		String widgetType = request.getParameter("type");
		
		WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(widgetType);
		ManualPageWidget page = new ManualPageWidget(definition, false);
		jsonResponse.setPayload(page.content().readContents());

	}
	/*****************************************************************
	 *
	 *****************************************************************/
	private static void getSettingsFormWidget(HttpServletRequest request, HttpServletResponse response, JSONResponse json) {
		
		String dashboardID = request.getParameter("FK_ID_DASHBOARD");

		if(CFW.DB.Dashboards.checkCanEdit(dashboardID)) {
						
			//-----------------------------------
			// Prepare Widget Settings

			String widgetID = request.getParameter("PK_ID");
			DashboardWidget widget = CFW.DB.DashboardWidgets.selectByID(widgetID);
			String widgetType = widget.type();
			String JSON_SETTINGS = widget.settings();
			WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(widgetType);

			User currentUser = CFW.Context.Request.getUser();
			
						
			if(definition.hasPermission(currentUser) || CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
				//----------------------------
				// Create Form
				
				CFWObject settings = definition.getSettings();
				CFWParameter.addParameterHandlingToField(CFWParameterScope.dashboard, settings, dashboardID, widgetType);
				
				settings.mapJsonFields(JSON_SETTINGS, false, true);
				
				CFWForm form = settings.toForm("cfwWidgetFormSettings"+CFWRandom.stringAlphaNumSpecial(6), "n/a-willBeReplacedByJavascript");
				
				form.appendToPayload(json);
			}else {
				CFW.Messages.noPermissionToEdit();
				
			}
			
		}else{
			CFW.Messages.addErrorMessage("Insufficient rights to execute action.");
		}

	}
	
	/*****************************************************************
	 *
	 *****************************************************************/
	private static void getSettingsFormDefault(HttpServletRequest request, HttpServletResponse response, JSONResponse json) {
		
		String dashboardID = request.getParameter("FK_ID_DASHBOARD");

		if(CFW.DB.Dashboards.checkCanEdit(dashboardID)) {
						
			//-----------------------------------
			// Prepare Widget Settings

			String widgetID = request.getParameter("PK_ID");
			DashboardWidget widget = CFW.DB.DashboardWidgets.selectByID(widgetID);
			String widgetType = widget.type();
			WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(widgetType);

			User currentUser = CFW.Context.Request.getUser();
			
			widget.createCloneForDefaultSettingsForm();
			if(definition.hasPermission(currentUser) || CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
				//----------------------------
				// Create Form
				
				//DashboardParameter.addParameterHandlingToField(settings, dashboardID, widgetType);
				
				DashboardWidget clone = widget.createCloneForDefaultSettingsForm();
				CFWForm form = clone.toForm("formEditDefaultSettings"+widgetID, CFW.L("cfw_core_save", "Save") );
				form.addAttribute("onclick", "cfw_dashboard_widget_save_defaultSettings('"+widgetID+"')");
				
				form.appendToPayload(json);
			}else {
				CFW.Messages.noPermissionToEdit();
			}
			
		}else{
			CFW.Messages.addErrorMessage("Insufficient rights to execute action.");
		}

	}
	
	
	/*****************************************************************
	 *
	 *****************************************************************/
	private static void getTaskParamForm(HttpServletRequest request, HttpServletResponse response,JSONResponse json) {
		
		String dashboardID = request.getParameter("dashboardid");
		
		if(!CFW.DB.Dashboards.checkCanEdit(dashboardID)) {
			CFW.Messages.noPermission();
		}else {
			
			//----------------------------
			// Get Values
			String widgetID = request.getParameter("widgetid");
			JsonObject payload = new JsonObject();
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

				CFWObject taskParams = new CFWObject();
				taskParams.addField(CFWJobTaskWidgetTaskExecutor.createOffsetMinutesField());
				taskParams.addAllFields(definition.getTasksParameters().getFields());

				taskParams.mapJsonFields(widget.taskParameters(), true, true);
				formObject.addAllFields(taskParams.getFields());
				
				CFWForm taskParamForm = formObject.toForm("cfwWidgetTaskParamForm"+CFW.Random.stringAlphaNum(16), "Save");
				
				taskParamForm.setFormHandler(new CFWFormHandler() {
					
					@SuppressWarnings("unchecked")
					@Override
					public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
						
						//-------------------------------------
						// Validate is current version
						Dashboard board = CFW.DB.Dashboards.selectByID(dashboardID);
						if(board.version() != 0) {
							CFW.Messages.addWarningMessage("Sorry, tasks can only be configured for the current version of a dashboard.");
							return;
						}
						
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
							
							//--------------------------------------
							// Update Dashboard and Widget Name

							LinkedHashMap<String, String> taskExecutorParams = jobToSave.propertiesAsMap();
							if(board != null) {
								taskExecutorParams.put(CFWJobTaskWidgetTaskExecutor.PARAM_DASHBOARD_NAME, board.name());
							}
							
							if(widget != null) {
								taskExecutorParams.put(CFWJobTaskWidgetTaskExecutor.PARAM_WIDGET_NAME, widget.title());
							}
							
							CFWTimeframe offset = (CFWTimeframe)formObject.getField(CFWJobTaskWidgetTaskExecutor.PARAM_TIMEFRAME_OFFSET).getValue();
							if(offset != null) {
								taskExecutorParams.put(CFWJobTaskWidgetTaskExecutor.PARAM_TIMEFRAME_OFFSET, ""+offset.toString());
							}
							
							jobToSave.properties(taskExecutorParams);
							
							//--------------------------------------
							// Save Job
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
		}

	}
	
	/*****************************************************************
	 *
	 *****************************************************************/
	private static void getWidgetForCopy(HttpServletRequest request, HttpServletResponse response, JSONResponse jsonResponse) {
		
		//----------------------------
		// Get Values
		String widgetID = request.getParameter("widgetid");

		//-----------------------------------
		// Prepare Widget Settings
		DashboardWidget widget = CFW.DB.DashboardWidgets.selectByID(widgetID);
		
		// remove task to avoid spamming the system with jobs
		widget.taskParameters("");
		
		jsonResponse.setPayload(widget.toJSON());
	}
	
	/*****************************************************************
	 * Returns the data to show as the widget content.
	 * 
	 *****************************************************************/
	private static void fetchWidgetData(HttpServletRequest request, HttpServletResponse response, JSONResponse jsonResponse) {
		
		//----------------------------
		// Get Values
		String widgetID = request.getParameter("widgetid");
		String dashboardParams = request.getParameter(FeatureParameter.CFW_PARAMS);
		String forceRefresh = request.getParameter("forcerefresh");
		
		String timeframeString = request.getParameter("timeframe");
		CFWTimeframe timeframe = new CFWTimeframe(timeframeString);

		//-----------------------------------
		// Prepare Widget Settings
		DashboardWidget widget = CFW.DB.DashboardWidgets.selectByID(widgetID);
		
		//-----------------------------------
		// Prepare Widget Settings
		String widgetType = widget.type();
		String JSON_SETTINGS = widget.settings();
		
		//apply Parameters to JSONSettings
		JSON_SETTINGS = CFW.Time.replaceTimeframePlaceholders(JSON_SETTINGS, timeframe);
		JsonElement jsonSettings = CFWParameter.replaceParamsInSettings(JSON_SETTINGS, dashboardParams, widgetType);
		WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(widgetType);
		CFWObject settingsObject = definition.getSettings();
		settingsObject.mapJsonFields(jsonSettings, false, true);
		
		//----------------------------
		// Create Response
		try {
			if(jsonSettings.isJsonObject()) {
				
				//----------------------------
				// Do that Cache Thingy
				
				WidgetDataCachePolicy cachePolicy = definition.getCachePolicy();
				if( cachePolicy == WidgetDataCachePolicy.OFF
				|| (
					cachePolicy == WidgetDataCachePolicy.TIME_BASED 
					&& !timeframe.isOffsetDefined() 
					) 
				) {
					//----------------------------
					// Do Not Cache
					CFWDBDashboard.pushEAVStats(FeatureDashboard.EAV_STATS_WIDGET_LOADS_UNCACHED, ""+widget.foreignKeyDashboard(), 1);
					definition.fetchData(request, jsonResponse, settingsObject, jsonSettings.getAsJsonObject(), timeframe);
					
				}else {
					//----------------------------
					// Create Cache ID
					String cacheID = widgetID;
					if(cachePolicy == WidgetDataCachePolicy.TIME_BASED) {
						cacheID += "_"+timeframe.getOffsetString();
					}
					
					cacheID += "_"+dashboardParams.hashCode()+"_"+jsonSettings.hashCode();
					
					//----------------------------
					// Force Cache Refresh
					if(forceRefresh.trim().equalsIgnoreCase("true")) {
						WidgetDataCache.CACHE.invalidate(cacheID);
					}
					 
					//----------------------------
					// Do Cached
					if(WidgetDataCache.CACHE.getIfPresent(cacheID) != null) {
						CFWDBDashboard.pushEAVStats(FeatureDashboard.EAV_STATS_WIDGET_LOADS_CACHED, ""+widget.foreignKeyDashboard(), 1);
					}
					
					JSONResponse responseFromCache = WidgetDataCache.CACHE.get(cacheID,
							new Callable<JSONResponse>() {

								@Override
								public JSONResponse call() throws Exception {
									
									try {
										//Hack: update and return existing jsonResponse to not overwrite it by creating a new instance.
										CFWDBDashboard.pushEAVStats(FeatureDashboard.EAV_STATS_WIDGET_LOADS_UNCACHED, ""+widget.foreignKeyDashboard(), 1);
										definition.fetchData(request, jsonResponse, settingsObject, jsonSettings.getAsJsonObject(), timeframe);
									}catch(Throwable e){
										new CFWLog(logger).severe("Unexpected Error Occured: "+e.getMessage(), e);
									}
									return jsonResponse;
								}
							});
					
					
					jsonResponse.copyFrom(responseFromCache);
				}
			}else {
				new CFWLog(logger).warn("Widget Data was not of the correct type.", new IllegalArgumentException());
			}
		}catch(Throwable e) {
			new CFWLog(logger).severe(e.getMessage(), e);
		}
					
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private static UndoRedoHistory<ArrayList<DashboardWidget>> undoredo_getHistory(int userID, String dashboardID) {
		try {
			return undoredoManager.getHistory(userID+"-"+dashboardID);
		} catch (ExecutionException e) {
			new CFWLog(logger).severe("Error occured while trying to get undo/redo history.", e);
		}
		
		return null;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private static UndoRedoHistory<ArrayList<DashboardWidget>> undoredo_clearHistory(Integer userID, String dashboardID) {
		if(userID == null) {
			return null;
		}
		undoredo_getHistory(userID, dashboardID).clear();
		
		return null;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private static void undoredo_operationBundleStart(int userID, String dashboardID) {
		
		if(CFW.DB.Dashboards.checkCanEdit(dashboardID)) {
			UndoRedoHistory<ArrayList<DashboardWidget>> history = undoredo_getHistory(userID, dashboardID);
			
			if(history != null) {
				history.operationBundleStart();
				undoredo_addResetStateOperation(history, dashboardID);
			}
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private static void undoredo_operationBundleEnd(int userID, String dashboardID) {
		
		if(CFW.DB.Dashboards.checkCanEdit(dashboardID)) {
			UndoRedoHistory<ArrayList<DashboardWidget>> history = undoredo_getHistory(userID, dashboardID);
			
			if(history != null) {
				undoredo_addResetStateOperation(history, dashboardID);
				history.operationBundleEnd();
			}
		}

	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private static void undoredo_addResetStateOperation(UndoRedoHistory<ArrayList<DashboardWidget>> history, String dashboardID) {
		
		if(CFW.DB.Dashboards.checkCanEdit(dashboardID)) {
			ArrayList<DashboardWidget> state = CFW.DB.DashboardWidgets.getWidgetsForDashboard(dashboardID);
			if(history != null) {
				history.addOperation(
						new UndoRedoOperation<ArrayList<DashboardWidget>>(
								history
								, methodResetToState
								, methodResetToState
								, state
								, state)
				);
			}
		}

	}
	
	/******************************************************************
	 * Resets the state to the given snapshot
	 ******************************************************************/
	@SuppressWarnings("unused")
	public static void undoredo_executeResetToState(ArrayList<DashboardWidget> state) {
		
		if(state.size() > 0) {
			//----------------------------
			// Start Transaction
			CFW.DB.transactionStart();
				//----------------------------
				// Reset Widgets 
				boolean success = true;
				int dashboardID = state.get(0).foreignKeyDashboard();
				success &= CFW.DB.DashboardWidgets.deleteWidgetsForDashboard(""+dashboardID);
				for(DashboardWidget widget : state) {
					success &= CFW.DB.DashboardWidgets.create(widget);
				}
			
			//----------------------------
			// Commit or Rollback
			if(success) {
				CFW.DB.transactionCommit();
			}else {
				CFW.DB.transactionRollback();
			}
		}

	}
	
	
	/******************************************************************
	 *
	 ******************************************************************/
	private static void undoredo_triggerUndoRedo(HttpServletRequest request, JSONResponse json, boolean doUndo, boolean isPublicServlet) {
		
		Integer userID = CFW.Context.Request.getUser().id(); 
		String dashboardID = request.getParameter("dashboardid");
		
		UndoRedoHistory<ArrayList<DashboardWidget>> history = undoredo_getHistory(userID, dashboardID);
		
		if(CFW.DB.Dashboards.checkCanEdit(dashboardID)) {
			try {
				
				//-----------------------------
				// Execute Undo/Redo
				if(doUndo) {
						history.executeUndoDirect();
				}else {
					history.executeRedoDirect();
				}
				
				//-----------------------------
				// Set Last Updated
				CFW.DB.Dashboards.updateLastUpdated(dashboardID);
				
				//-----------------------------
				// Read Current State and send
				// to Browser
				fetchWidgetsAndParams(json, dashboardID, isPublicServlet);
				
				
				
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				new CFWLog(logger).severe("Exception while executing undo/redo:"+e, e);
				json.setSuccess(false);
			}
		}
		
		
		
	}
	

}