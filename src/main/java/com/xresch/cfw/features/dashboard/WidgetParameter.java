package com.xresch.cfw.features.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.CFWFieldFlag;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.dashboard.DashboardWidget.DashboardWidgetFields;
import com.xresch.cfw.features.dashboard.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.dashboard.parameters.DashboardParameter;
import com.xresch.cfw.features.dashboard.parameters.DashboardParameter.DashboardParameterFields;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.utils.CFWUtilsText;
import com.xresch.cfw.validation.CustomValidator;

public class WidgetParameter extends WidgetDefinition {

	public static final String FIELDNAME_AFFECTED_WIDGETS = "JSON_AFFECTED_WIDGETS";
	public static final String FIELDNAME_PASSWORD = "password";
	public static final String FIELDNAME_CHECKPASSWORD = "checkpassword";
	public static final String FIELDNAME_BUTTONLABEL = "buttonlabel";
	public static final String FIELDNAME_SHOWBUTTON = "showbutton";
	public static final String FIELDNAME_JSON_PARAMETERS = "JSON_PARAMETERS";
	public static final String FIELDNAME_DESCRIPTION = "description";
	
	public static final String WIDGET_TYPE = "cfw_parameter";
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCachePolicy.OFF;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String getWidgetType() {return WIDGET_TYPE;}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				
				.addField(CFWField.newString(FormFieldType.WYSIWYG, FIELDNAME_DESCRIPTION)
					.setLabel("{!cfw_widget_parameter_description!}")
					.setDescription("{!cfw_widget_parameter_description_desc!}")
					.allowHTML(true)
				)
				.addField(CFWField.newTagsSelector(FIELDNAME_JSON_PARAMETERS)
						.setLabel("{!cfw_widget_parameter_params!}")
						.setDescription("{!cfw_widget_parameter_params_desc!}")
						.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
							
							@Override
							public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
								
								String dashboardID = request.getParameter("cfw-dashboardid");								
								HashSet<String> usedParamIDs = getParamIDsAlreadyInUse(dashboardID);

								//---------------------------------------
								// Return Params not already in use
								CFWSQL sql = new CFWSQL(new DashboardParameter())
										.select()
										.where(DashboardParameterFields.FK_ID_DASHBOARD, dashboardID)
										.and().like(DashboardParameterFields.NAME, "%"+searchValue+"%");
										if(usedParamIDs.size() > 0) {
											sql.and().not().in(DashboardParameterFields.PK_ID, usedParamIDs.toArray(new Object[] {}));
										}
								return sql.getAsAutocompleteResult(DashboardParameterFields.PK_ID, DashboardParameterFields.NAME, DashboardParameterFields.WIDGET_TYPE);
							}
						})	
					)
				
			.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, FIELDNAME_SHOWBUTTON)
					.setLabel("{!cfw_widget_parameter_showbutton!}")
					.setDescription("{!cfw_widget_parameter_showbutton_desc!}")
					.setValue(true)
				)
			
			.addField(CFWField.newString(FormFieldType.TEXT, FIELDNAME_BUTTONLABEL)
					.setLabel("{!cfw_widget_parameter_buttonlabel!}")
					.setDescription("{!cfw_widget_parameter_buttonlabel_desc!}")
					.setValue("")
				)
			
			.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, FIELDNAME_CHECKPASSWORD)
					.setLabel("{!cfw_widget_parameter_checkpassword!}")
					.setDescription("{!cfw_widget_parameter_checkpassword_desc!}")
					.setValue(false)
				)
			
			.addField(CFWField.newString(FormFieldType.PASSWORD, FIELDNAME_PASSWORD)
					.setLabel("{!cfw_widget_parameter_password!}")
					.setDescription("{!cfw_widget_parameter_password_desc!}")
					.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
					.setValue("")
				)
			
			.addField(CFWField.newTagsSelector(FIELDNAME_AFFECTED_WIDGETS)
					.setLabel("{!cfw_widget_parameter_affected_widgets!}")
					.setDescription("{!cfw_widget_parameter_affected_widgets_desc!}")
					.addAttribute("maxTags", "128")
					.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
						public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
							String dashboardID = request.getParameter("cfw-dashboardid");
							return CFW.DB.DashboardWidgets.autocompleteWidget(dashboardID, searchValue, this.getMaxResults());					
						}
					})
			)
		;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, long earliest, long latest, int timezoneOffsetMinutes) {
		
		//---------------------------------
		// Get ID
		String dashboardID = request.getParameter("dashboardid");
		
		//---------------------------------
		// Resolve Parameters
		JsonElement paramsElement = jsonSettings.get(FIELDNAME_JSON_PARAMETERS);
		if(paramsElement.isJsonNull()) {
			return;
		}
		
		JsonObject paramsObject = paramsElement.getAsJsonObject();
		if(paramsObject.size() == 0) {
			return;
		}
		
		ArrayList<String> paramNames = new ArrayList<>();
		for(Entry<String, JsonElement> entry : paramsObject.entrySet()) {
			paramNames.add(entry.getValue().getAsString());
		}

		//Filter by names instead of IDs to still get parameters if they were changed.
		ArrayList<CFWObject> paramsResultArray = new CFWSQL(new DashboardParameter())
				.select()
				.whereIn(DashboardParameterFields.NAME, paramNames)
				.and(DashboardParameterFields.FK_ID_DASHBOARD, dashboardID)
				.orderby(DashboardParameterFields.WIDGET_TYPE.toString(), DashboardParameterFields.LABEL.toString())
				.getAsObjectList();
		
		DashboardParameter.prepareParamObjectsForForm(request, paramsResultArray, true);

		//---------------------------------
		// Resolve Parameters
		Boolean showButton = (Boolean)settings.getField(FIELDNAME_SHOWBUTTON).getValue();
		String buttonLabel = null;

		if(showButton != null && showButton) {
			buttonLabel = (String)settings.getField(FIELDNAME_BUTTONLABEL).getValue();
			
			if(Strings.isNullOrEmpty(buttonLabel)) {
				
				buttonLabel = "Update"; 
			}
		}
		
		//--------------------------------------
		// Create Form with Custom onclick event
		
		CFWForm paramForm = new CFWForm("cfwWidgetParameterForm"+CFW.Random.randomStringAlphaNumerical(12), buttonLabel);
		paramForm.isInlineForm(true);
		paramForm.addAttribute("onclick", "cfw_dashboard_parameters_fireParamWidgetUpdate(this);");
		
		//--------------------------------------
		// Add parameter fields to form
		
		for(CFWObject object : paramsResultArray) {
			DashboardParameter param = (DashboardParameter)object;
			
			CFWField valueField = param.getField(DashboardParameterFields.VALUE.toString());
			valueField
				.addAttribute("data-widgettype", param.widgetType())
				.addAttribute("data-settingslabel", param.paramSettingsLabel())
				.setName(param.name())
				.setLabel(CFW.Utils.Text.fieldNameToLabel(param.name()))
				.isDecoratorDisplayed(false)
				.addCssClass(" form-control-sm cfw-widget-parameter-marker");

			paramForm.addField(valueField);
		}

		//--------------------------------------
		// Add Field for password check
		Boolean checkPassword = (Boolean)settings.getField(FIELDNAME_CHECKPASSWORD).getValue();

		paramForm.addField(
			CFWField.newBoolean(FormFieldType.HIDDEN, "cfw-promptpassword") 
				.setValue( (checkPassword != null && checkPassword) )
		);
		
		//--------------------------------------
		// Add Fields containing Affected Widgets
		LinkedHashMap<String, String> affectedWidgets = (LinkedHashMap<String, String>)settings.getField(FIELDNAME_AFFECTED_WIDGETS).getValue();
		System.out.println("affectedWidgets: "+CFW.JSON.toJSON(affectedWidgets));
		if(affectedWidgets == null) { affectedWidgets = new LinkedHashMap<String, String>(); }
		
		String[] affectedIDs = affectedWidgets.keySet().toArray(new String[] {});
		System.out.println("affectedWidgets.keySet(): "+CFW.JSON.toJSON(affectedWidgets.keySet()));
		System.out.println("affectedIDs: "+CFW.JSON.toJSON(affectedIDs));
		paramForm.addField(
				CFWField.newString(FormFieldType.HIDDEN, "cfw-affectedwidgets") 
					.setValue( CFW.JSON.toJSON(affectedIDs).replaceAll("\"", "") )
			);
		
		//--------------------------------------
		// Return Form in Payload
		paramForm.appendToPayload(response);		
		
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "cfw_widget_parameter.js");
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add(js);
		return array;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public ArrayList<FileDefinition> getCSSFiles() { return null; }
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		return map;
	}
	
	
	/******************************************************************
	 *
	 ******************************************************************/
	private HashSet<String> getParamIDsAlreadyInUse(String dashboardID){
		//---------------------------------------
		// Get Params already in use
		ArrayList<CFWObject> paramWidgetsForDB = new CFWSQL(new DashboardWidget())
				.queryCache()
				.select()
				.where(DashboardWidgetFields.FK_ID_DASHBOARD, dashboardID)
				.and(DashboardWidgetFields.TYPE, WIDGET_TYPE)
				.getAsObjectList();
		
		HashSet<String> usedParamIDs = new HashSet<>();
		for(CFWObject object : paramWidgetsForDB) {
			DashboardWidget widget = (DashboardWidget)object;
			JsonObject settings = CFW.JSON.fromJson(widget.settings()).getAsJsonObject();
			
			if(!settings.isJsonNull() 
			&& settings.has(FIELDNAME_JSON_PARAMETERS)
			&& settings.get(FIELDNAME_JSON_PARAMETERS).isJsonObject()) {
				usedParamIDs.addAll(settings.get(FIELDNAME_JSON_PARAMETERS).getAsJsonObject().keySet());
			}
		}
		
		return usedParamIDs;
		
	}
	
	
	/******************************************************************
	 *
	 ******************************************************************/
	public static void checkParameterWidgetPassword(HttpServletRequest request, HttpServletResponse response, JSONResponse json) {
		
		//-----------------------------------
		// Prepare Widget Settings
		String widgetID = request.getParameter("widgetid");
		DashboardWidget widget = CFW.DB.DashboardWidgets.selectByID(widgetID);
		String widgetType = widget.type();
		String JSON_SETTINGS = widget.settings();
		
		//apply Parameters to JSONSettings
		WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(widgetType);
		CFWObject settingsObject = definition.getSettings();
		settingsObject.mapJsonFields(JSON_SETTINGS, false, true);
		
		//-----------------------------------
		// Check if Password should be checked
		Boolean checkPassword = (Boolean)settingsObject.getField(FIELDNAME_CHECKPASSWORD).getValue();
		
		if(checkPassword != null && !checkPassword) {
			json.setSuccess(true);
			return;
		}
		
		//-----------------------------------
		// Check Password

		String password = (String)settingsObject.getField(FIELDNAME_PASSWORD).getValue();
		String givenPassword = request.getParameter("credentialKey");
		
		if(givenPassword.equals(password)) {
			json.setSuccess(true);
			return;
		}
		
		CFW.Messages.addErrorMessage("Password verification failed.");
		json.setSuccess(false);
		return;
	}

}
