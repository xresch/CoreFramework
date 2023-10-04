package com.xresch.cfw.features.parameter;

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
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.datahandling.CFWFormCustomAutocompleteHandler;
import com.xresch.cfw.datahandling.CFWMultiForm;
import com.xresch.cfw.datahandling.CFWMultiFormHandler;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.ServletDashboardViewMethods;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetParameter;
import com.xresch.cfw.features.parameter.CFWParameter.DashboardParameterFields;
import com.xresch.cfw.features.parameter.CFWParameter.DashboardParameterMode;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWModifiableHTTPRequest;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023
 **************************************************************************************************************/
public class ServletParameter extends HttpServlet
{

	private static final Logger logger = CFWLog.getLogger(ServletParameter.class.getName());
	
	public static final String SCOPE_DASHBOARD = "dashboard";
	public static final String SCOPE_QUERY = "query";
	
	private static final long serialVersionUID = 1L;
	
	public ServletParameter() {
	
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
   protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
   {
		handleDataRequest(request, response);
   }
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		handleDataRequest(request, response);
    }
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void handleDataRequest(HttpServletRequest request, HttpServletResponse response) {
		
		String action = request.getParameter("action");
		String item = request.getParameter("item");
		
		// scope would be 'dashboard' or 'query'
		String scope = request.getParameter("scope");
		
		// the id of the dashboard or query
		String id = request.getParameter("id");
		
		//int	userID = CFW.Context.Request.getUser().id();
			
		JSONResponse jsonResponse = new JSONResponse();		

		switch(action.toLowerCase()) {
		
			case "fetch": 			
				switch(item.toLowerCase()) {
	  										
					case "availableparams": getAvailableParams(request, jsonResponse, scope, id);
											break;	
											
					case "paramform": 		fetchParameterEditForm(request, response, jsonResponse);
											break; 
	  										
					default: 				CFW.Messages.itemNotSupported(item);
											break;
				}
				break;
			
			case "create": 			
				switch(item.toLowerCase()) {		
					case "param": 				createParam(request, response, jsonResponse);
												break;
												
					default: 					CFW.Messages.itemNotSupported(item);
												break;
				}
				break;	
			
			case "delete": 			
				switch(item.toLowerCase()) {					
					case "param": 				deleteParam(request, response, jsonResponse);
												break;
																	
					default: 					CFW.Messages.itemNotSupported(item);
												break;
				}
				break;	
				
			default: 			CFW.Messages.actionNotSupported(action);
								break;
								
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private static boolean canEdit(String scope, String ID) {
		
		if( (scope.equals(SCOPE_DASHBOARD) && !CFW.DB.Dashboards.checkCanEdit(ID)) ) {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Insufficient rights to load dashboard parameters.");
			return false;
		}
		
		return true;
		
	}
	
	/*****************************************************************
	 *
	 *****************************************************************/
	@SuppressWarnings("rawtypes")
	private static void getAvailableParams(HttpServletRequest request, JSONResponse response, String scope, String ID) {

		//--------------------------------------------
		// Check can Edit
		if (!canEdit(scope, ID)) {
			return;
		}

		//--------------------------------------------
		// Add Params for Widgets on Dashboard
		JsonArray widgetParametersArray = new JsonArray();
		HashSet<String> uniqueTypeChecker = new HashSet<>();
		
		if( scope.equals(SCOPE_DASHBOARD) ) {
			ArrayList<DashboardWidget> widgetList = CFW.DB.DashboardWidgets.getWidgetsForDashboard(ID);
			
			for(DashboardWidget widget : widgetList) {
				
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
		}
		
		//--------------------------------------------
		// Add Params from Definitions
		JsonArray parameterDefArray = new JsonArray();
		
		for(ParameterDefinition def : CFW.Registry.Parameters.getParameterDefinitions().values()) {
			if(def.isAvailable(uniqueTypeChecker)) {
				JsonObject paramObject = new JsonObject();
				paramObject.add("widgetType", null);
				paramObject.add("widgetSetting", null);
				paramObject.addProperty("label", def.getParamUniqueName());
				
				parameterDefArray.add(paramObject);
			}
		}
		
		parameterDefArray.addAll(widgetParametersArray);
		response.getContent().append(parameterDefArray.toString());
		
	}
	
	/*****************************************************************
	 *
	 *****************************************************************/
	@SuppressWarnings("rawtypes")
	private static void createParam(HttpServletRequest request, HttpServletResponse response, JSONResponse json) {
		
		//----------------------------
		// Get Values
		String scope = request.getParameter("scope");
		String ID = request.getParameter("id");
		String widgetType = request.getParameter("widgetType");
		String widgetSetting = request.getParameter("widgetSetting");
		String label = request.getParameter("label");
		
		//--------------------------------------------
		// Check can Edit
		if (!canEdit(scope, ID)) {
			return;
		}
		
		//----------------------------
		// Create Param
		CFWParameter param = new CFWParameter();
		param.foreignKeyDashboard(Integer.parseInt(ID));

		if(Strings.isNullOrEmpty(widgetSetting)) {
			param.widgetType(null);
			param.paramSettingsLabel(null);
			
			//----------------------------
			// Handle Default Params
			ParameterDefinition def = CFW.Registry.Parameters.getDefinition(label);
			if(def != null) {
				CFWField paramField = def.getFieldForSettings(request, ID, null);
				param.paramType(paramField.fieldType());
				param.paramSettingsLabel(def.getParamUniqueName());
				param.name(label.toLowerCase().replace(" ", "_")+"_"+CFW.Random.randomStringAlphaNumerical(6));
				param.mode(DashboardParameterMode.MODE_SUBSTITUTE);
				param.isModeChangeAllowed(false);
				param.isDynamic(def.isDynamic());
				
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
				param.getField(DashboardParameterFields.VALUE.toString()).setValueConvert(settingsField.getValue(), true);
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

	}
	
	/*****************************************************************
	 *
	 *****************************************************************/
	private static void deleteParam(HttpServletRequest request, HttpServletResponse response, JSONResponse json) {
		
		String scope = request.getParameter("scope");
		String ID = request.getParameter("id");
	
		//--------------------------------------------
		// Check can Edit
		if (!canEdit(scope, ID)) {
			return;
		}
		
		//--------------------------------------------
		// Delete Param
		String paramID = request.getParameter("paramid");
		if(CFW.DB.DashboardParameters.checkIsParameterOfDashboard(ID, paramID)) {
			boolean success = CFW.DB.DashboardParameters.deleteByID(paramID);
			json.setSuccess(success);
			CFW.Messages.deleted();
			
			//Remove From Form to avoid errors on save
			String formID = request.getParameter("formid");
			CFWMultiForm form = (CFWMultiForm)CFW.Context.Session.getForm(formID);
			
			form.getOrigins().remove(Integer.parseInt(paramID));
		}

	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void fetchParameterEditForm(HttpServletRequest request, HttpServletResponse response, JSONResponse json) {
		
		String scope = request.getParameter("scope");
		String ID = request.getParameter("id");
		
		//--------------------------------------------
		// Check can Edit
		if (!canEdit(scope, ID)) {
			return;
		}
		
		//--------------------------------------------
		// Get Parameter List
		ArrayList<CFWObject> parameterList = null;
		switch(scope) {
			case SCOPE_DASHBOARD: 	parameterList = CFW.DB.DashboardParameters.getParametersForDashboard(ID);	break;
			case SCOPE_QUERY:  	  	/*parameterList = CFW.DB.DashboardParameters.getParametersForDashboard(ID);*/	break;
			default:				CFW.Messages.itemNotSupported(scope); return;
		}
		
		CFWTimeframe notNeeded = null;
		CFWParameter.prepareParamObjectsForForm(request, parameterList, notNeeded, false);
		if(parameterList.size() == 0) {
			return;
		}
		
		//--------------------------------------------
		// Create Form
		CFWMultiForm parameterEditForm = new CFWMultiForm("cfwParameterEditMultiForm"+CFW.Random.randomStringAlphaNumerical(12), "Save", parameterList);
		
		//--------------------------------------------
		// Create Form Handler
		parameterEditForm.setMultiFormHandler(new CFWMultiFormHandler() {
			
			@Override
			public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWMultiForm form,
					LinkedHashMap<Integer, CFWObject> originsMap) {
				
				form.mapRequestParameters(request);
				
				//revert uniques of the fields to be able to save to the database.
				form.revertFieldNames();
					for(CFWObject object : originsMap.values()) {
						CFWParameter param = (CFWParameter)object;
						
						if(!CFW.DB.DashboardParameters.checkIsParameterNameUsedOnUpdate(param)) {
							//do not update WidgetType and Setting as the values were overridden with labels.
							boolean success = new CFWSQL(param).updateWithout(
									DashboardParameterFields.WIDGET_TYPE.toString(),
									DashboardParameterFields.LABEL.toString());
							
							if(!success) {
								CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The data with the ID '"+param.getPrimaryKeyValue()+"' could not be saved to the database.");
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
		
		//--------------------------------------------
		// Set Form Autocomplete Handler
		parameterEditForm.setCustomAutocompleteHandler(new CFWFormCustomAutocompleteHandler() {
			
			@Override
			public AutocompleteResult getAutocompleteData(HttpServletRequest request, HttpServletResponse response,
					CFWForm form, CFWField field, String searchValue, int cursorPosition) {
				
				//------------------------------------
				// Create Request with additional Params
				// for the same Widget Type.
				// allows fields using other request params
				// for autocomplete to work properly
				CFWMultiForm multiform = (CFWMultiForm)form;
				
				String paramID = field.getName().split("-")[0];
				int paramIDNumber = Integer.parseInt(paramID);
				LinkedHashMap<Integer, CFWObject> origins = multiform.getOrigins();
				CFWParameter paramToAutocomplete = (CFWParameter)origins.get(paramIDNumber);
				String widgetType = paramToAutocomplete.widgetType();
				
				Map<String, String[]> extraParams = new HashMap<String, String[]>();
				if(widgetType != null) {
					//------------------------------------
					//Find all Settings from the same Widget Type
					
					for(CFWObject object : origins.values() ) {
						CFWParameter currentParam = (CFWParameter)object;
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
						CFWParameter currentParam = (CFWParameter)object;
						
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
		    		AutocompleteResult suggestions = field.getAutocompleteHandler().getAutocompleteData(modifiedRequest, searchValue, cursorPosition);
		    		return suggestions;
		    	}else {
		    		json.setSuccess(false);
		    		new CFWLog(logger)
			    		.severe("The field with name '"+field.getName()+"' doesn't have an autocomplete handler.");
		    		return null;
		    	}
			}
		});
		
		//--------------------------------------------
		// Add to Payload
		parameterEditForm.appendToPayload(json);
		json.setSuccess(true);	
		
	}

	
}