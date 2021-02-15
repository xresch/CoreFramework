package com.xresch.cfw.features.dashboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
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
import com.xresch.cfw.datahandling.CFWMultiForm;
import com.xresch.cfw.datahandling.CFWMultiFormHandlerDefault;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.dashboard.DashboardParameter.DashboardParameterFields;
import com.xresch.cfw.features.dashboard.DashboardParameter.DashboardParameterMode;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWModifiableHTTPRequest;
import com.xresch.cfw.utils.CFWRandom;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletDashboardView extends HttpServlet
{
	private static final Logger logger = CFWLog.getLogger(ServletDashboardView.class.getName());
	private static final long serialVersionUID = 1L;
	
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {

		if(CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_VIEWER)
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_CREATOR)
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
			
			String dashboardID = request.getParameter("id");
			String action = request.getParameter("action");
			
			if(action == null) {
				HTMLResponse html = new HTMLResponse("Dashboard");
				StringBuilder content = html.getContent();
				
				//---------------------------
				// Check Access
				if(!CFW.DB.Dashboards.hasUserAccessToDashboard(dashboardID)) {
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
				handleDataRequest(request, response);
			}
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
		}
        
    }
	
	/*****************************************************************
	 *
	 *****************************************************************/
	@Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {

		if(CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_VIEWER)
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_CREATOR)
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
			handleDataRequest(request, response);
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
		}
        
    }
	
	/*****************************************************************
	 *
	 *****************************************************************/
	private void handleDataRequest(HttpServletRequest request, HttpServletResponse response) {
		
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
					case "widgets": 			fetchWidgets(jsonResponse, dashboardID);
	  											break;	
	  											
					case "widgetdata": 			getWidgetData(request, response, jsonResponse);
												break;	
												
					case "availableparams": 	getAvailableParams(jsonResponse, dashboardID);
												break;	
												
					case "settingsform": 		getSettingsForm(request, response, jsonResponse);
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
												
					default: 					CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The value of item '"+item+"' is not supported.");
												break;
				}
				break;	
				
			case "update": 			
				switch(item.toLowerCase()) {
					case "widget": 				updateWidget(request, response, jsonResponse);
	  											break;
	  																
					default: 					CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The value of item '"+item+"' is not supported.");
												break;
				}
				break;
				
			case "delete": 			
				switch(item.toLowerCase()) {
					case "widget": 				deleteWidget(request, response, jsonResponse);
	  											break;
					
					case "param": 				deleteParam(request, response, jsonResponse);
												break;
																	
					default: 					CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The value of item '"+item+"' is not supported.");
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
	private void fetchWidgets(JSONResponse response, String dashboardID) {
		
		Dashboard dashboard = CFW.DB.Dashboards.selectByID(dashboardID);
		
		if(dashboard.isShared() || CFW.DB.Dashboards.checkCanEdit(dashboardID)) {
			
			response.getContent().append(CFW.DB.DashboardWidgets.getWidgetsForDashboardAsJSON(dashboardID));
			
		}else{
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Insufficient rights to view this dashboard.");
		}
	}
	
	/*****************************************************************
	 *
	 *****************************************************************/
	private void createWidget(JSONResponse response, String type, String dashboardID) {

		if(CFW.DB.Dashboards.checkCanEdit(dashboardID)) {
			
			//----------------------------
			// Create Widget
			DashboardWidget newWidget = new DashboardWidget();
			newWidget.type(type);
			newWidget.foreignKeyDashboard(Integer.parseInt(dashboardID));
			
			WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(type);
			if(definition != null) {
				newWidget.settings(definition.getSettings().toJSON());
			}
			
			int id = CFW.DB.DashboardWidgets.createGetPrimaryKey(newWidget);
			newWidget.id(id);
			
			response.getContent().append(CFW.JSON.toJSON(newWidget));
			
		}else{
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Insufficient rights to execute action.");
		}

	}
		
	/*****************************************************************
	 *
	 *****************************************************************/
	private void updateWidget(HttpServletRequest request, HttpServletResponse response, JSONResponse json) {
		
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
			
			//----------------------------
			// Validate
			CFWObject settings = definition.getSettings();
			
			boolean isValid = settings.mapJsonFields(jsonElement);
			
			if(isValid) {
				DashboardWidget widgetToUpdate = new DashboardWidget();
				
				// check if default settings are valid
				if(widgetToUpdate.mapRequestParameters(request)) {
					//Use sanitized values
					widgetToUpdate.settings(settings.toJSON());
					CFW.DB.DashboardWidgets.update(widgetToUpdate);
				}
			}
			
		}else{
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Insufficient rights to execute action.");
		}

	}
	
	/*****************************************************************
	 *
	 *****************************************************************/
	private void deleteWidget(HttpServletRequest request, HttpServletResponse response, JSONResponse json) {
		
		String dashboardID = request.getParameter("dashboardid");
		
		if(CFW.DB.Dashboards.checkCanEdit(dashboardID)) {
			
			String widgetID = request.getParameter("widgetid");
			
			boolean success = CFW.DB.DashboardWidgets.deleteByID(widgetID);
			
			json.setSuccess(success);
		}else{
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Insufficient rights to execute action.");
		}

	}
	
	/*****************************************************************
	 *
	 *****************************************************************/
	private void getSettingsForm(HttpServletRequest request, HttpServletResponse response, JSONResponse json) {
		
		String dashboardID = request.getParameter("FK_ID_DASHBOARD");

		if(CFW.DB.Dashboards.checkCanEdit(dashboardID)) {
			
			//----------------------------
			// Get Values
			String widgetType = request.getParameter("TYPE");
			String JSON_SETTINGS = request.getParameter("JSON_SETTINGS");
			JsonElement jsonElement = CFW.JSON.fromJson(JSON_SETTINGS);
			WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(widgetType);
			
			//----------------------------
			// Create Form
			CFWObject settings = definition.getSettings();
			settings.mapJsonFields(jsonElement);
			
			CFWForm form = settings.toForm("cfwWidgetFormSettings"+CFWRandom.randomStringAlphaNumSpecial(6), "n/a-willBeRemoved");
			
			form.appendToPayload(json);
			
		}else{
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Insufficient rights to execute action.");
		}

	}
	
	/*****************************************************************
	 *
	 *****************************************************************/
	private void getWidgetData(HttpServletRequest request, HttpServletResponse response, JSONResponse jsonResponse) {
		
		//----------------------------
		// Get Values
		String widgetType = request.getParameter("TYPE");
		String JSON_SETTINGS = request.getParameter("JSON_SETTINGS");
		JsonElement jsonSettingsObject = CFW.JSON.fromJson(JSON_SETTINGS);
		WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(widgetType);
		
		//----------------------------
		// Create Response
		if(jsonSettingsObject.isJsonObject()) {
		definition.fetchData(null, jsonResponse, jsonSettingsObject.getAsJsonObject());
		}else {
			new CFWLog(logger).warn("Widget Data was not of the correct type.", new IllegalArgumentException());
		}
					
	}
	
	/*****************************************************************
	 *
	 *****************************************************************/
	@SuppressWarnings("rawtypes")
	private void getAvailableParams(JSONResponse response, String dashboardID) {
		

		if(CFW.DB.Dashboards.checkCanEdit(dashboardID)) {
			
			JsonArray parametersArray = new JsonArray();
			//--------------------------------------------
			// Add Default Params
			JsonObject textParamObject = new JsonObject();
			textParamObject.add("widgetType", null);
			textParamObject.add("widgetSetting", null);
			textParamObject.addProperty("label", "Text");
			
			parametersArray.add(textParamObject);
			
			//--------------------------------------------
			// Add Params for Widgets on Dashboard
			ArrayList<CFWObject> widgetList = CFW.DB.DashboardWidgets.getWidgetsForDashboard(dashboardID);
			HashSet<String> uniqueTypeChecker = new HashSet<>();
			
			for(CFWObject object : widgetList) {
				
				DashboardWidget widget = (DashboardWidget)object;
				String widgetType = widget.type();
				
				if(uniqueTypeChecker.contains(widgetType)) {
					continue;
				}else {
					WidgetDefinition definition =  CFW.Registry.Widgets.getDefinition(widgetType);
					for(Entry<String, CFWField> entry : definition.getSettings().getFields().entrySet()) {
						CFWField field = entry.getValue();
						JsonObject paramObject = new JsonObject();
						paramObject.addProperty("widgetType", definition.getWidgetType());
						paramObject.addProperty("widgetSetting", field.getName());
						paramObject.addProperty("label", field.getLabel());
						
						parametersArray.add(paramObject);
						
					}
				}	
			}
			
			response.getContent().append(parametersArray.toString());
		}else{
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Insufficient rights to load dashboard parameters.");
		}
	}
	
	/*****************************************************************
	 *
	 *****************************************************************/
	private void createParam(HttpServletRequest request, HttpServletResponse response, JSONResponse json) {
		
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
			param.name("param_name_"+CFW.Random.randomStringAlphaNumerical(6));

			if(Strings.isNullOrEmpty(widgetType) && Strings.isNullOrEmpty(widgetSetting)) {
				param.widgetType(null);
				param.widgetSetting(null);
				
				//----------------------------
				// Handle Default Params
				switch(label) {
					case "Text": 	param.paramType(FormFieldType.TEXT);
									param.mode(DashboardParameterMode.MODE_SUBSTITUTE);
									param.isModeChangeAllowed(false);
									break;
									
					case "Select": 	param.paramType(FormFieldType.SELECT);
									param.mode(DashboardParameterMode.MODE_SUBSTITUTE);
									param.isModeChangeAllowed(false);
									break;	
									
					case "Boolean":	param.paramType(FormFieldType.BOOLEAN);
									param.mode(DashboardParameterMode.MODE_SUBSTITUTE);
									param.isModeChangeAllowed(false);
									break;	
									
					default: /*Unknown type*/ return;
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
					param.widgetSetting(widgetSetting);
					param.paramType(settingsField.fieldType()); // used to fetch similar field types
					param.mode(DashboardParameterMode.MODE_SUBSTITUTE);
					param.isModeChangeAllowed(true);
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
	private void deleteParam(HttpServletRequest request, HttpServletResponse response, JSONResponse json) {
		
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
	private void createParameterEditForm(HttpServletRequest request, HttpServletResponse response, JSONResponse json) {

		String dashboardID = request.getParameter("dashboardid");
		ArrayList<CFWObject> parameterList = CFW.DB.DashboardParameters.getParametersForDashboard(dashboardID);
		
		//===========================================
		// Replace Value Field
		//===========================================
		for(CFWObject object : parameterList) {
			DashboardParameter param = (DashboardParameter)object;
			CFWField currentValueField = param.getField(DashboardParameterFields.VALUE.toString());
			CFWField newValueField;
			if(param.widgetType() != null) {
				//---------------------------------------
				// Replace Value field with field from WidgetSettings
				WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(param.widgetType());
				CFWObject settings = definition.getSettings();
				newValueField = settings.getField(param.widgetSetting());
				newValueField.setName(DashboardParameterFields.VALUE.toString());
				newValueField.setLabel("Value");
				newValueField.setDescription("The value of the parameter.");
			}else {
				//----------------------------
				// Add Field 
				switch(param.paramType()) {
					case TEXT: 	
						newValueField = CFWField.newString(FormFieldType.TEXT, DashboardParameterFields.VALUE);
									break;
									
					case SELECT: 	
						newValueField = CFWField.newString(FormFieldType.SELECT, DashboardParameterFields.VALUE);
									break;
									
					case BOOLEAN: 	
						newValueField = CFWField.newString(FormFieldType.BOOLEAN, DashboardParameterFields.VALUE);
									break;	
									
					default: /*Unknown type*/ return;
				}
			}
			//currentValue field is always a String field
			newValueField.setValueConvert(currentValueField.getValue());
			param.getFields().remove(DashboardParameterFields.VALUE.toString());
			param.addField(newValueField);
		}
		
		//===========================================
		// Replace Value Field
		//===========================================
		if(parameterList.size() != 0) {
			
			CFWMultiForm parameterEditForm = new CFWMultiForm("cfwParameterEditMultiForm"+CFW.Random.randomStringAlphaNumerical(12), "Save", parameterList);
			
			parameterEditForm.setMultiFormHandler(new CFWMultiFormHandlerDefault());
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
					
					//------------------------------------
					//Find all Settings from the same Widget Type
					Map<String, String[]> extraParams = new HashMap<String, String[]>();
					for(CFWObject object : origins.values() ) {
						DashboardParameter currentParam = (DashboardParameter)object;
						System.out.println("===== currentParam.widgetType(): "+currentParam.widgetType());
						System.out.println("widgetType: "+widgetType);
						if(currentParam.widgetType() != null && currentParam.widgetType().equals(widgetType)) {
							String paramName = currentParam.widgetSetting();
							String valueFieldName = currentParam.id()+"-"+DashboardParameterFields.VALUE;
							String paramValue = request.getParameter(valueFieldName);
							System.out.println("paramName: "+paramName);
							System.out.println("paramValue: "+paramValue);
					        extraParams.put(paramName, new String[] { paramValue });
						}
					}
					
					CFWModifiableHTTPRequest modifiedRequest = new CFWModifiableHTTPRequest(request, extraParams);
					System.out.println("ModifiedParams:"+CFW.JSON.toJSON(modifiedRequest.getParameterMap()));
					
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