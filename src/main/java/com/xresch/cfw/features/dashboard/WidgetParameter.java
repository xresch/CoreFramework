package com.xresch.cfw.features.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.dashboard.DashboardWidget.DashboardWidgetFields;
import com.xresch.cfw.features.dashboard.parameters.DashboardParameter;
import com.xresch.cfw.features.dashboard.parameters.DashboardParameter.DashboardParameterFields;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.utils.CFWUtilsText;
import com.xresch.cfw.validation.CustomValidator;

public class WidgetParameter extends WidgetDefinition {

	public static final String WIDGET_TYPE = "cfw_parameter";
	
	
	@Override
	public String getWidgetType() {return WIDGET_TYPE;}

	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				
				.addField(CFWField.newString(FormFieldType.WYSIWYG, "description")
					.setLabel("{!cfw_widget_parameter_description!}")
					.setDescription("{!cfw_widget_parameter_description_desc!}")
					.allowHTML(true)
				)
				.addField(CFWField.newTagsSelector("JSON_PARAMETERS")
						.setLabel("{!cfw_widget_parameter_params!}")
						.setDescription("{!cfw_widget_parameter_params_desc!}")
						.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
							
							@Override
							public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
								
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
				
			.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, "showbutton")
					.setLabel("{!cfw_widget_parameter_showbutton!}")
					.setDescription("{!cfw_widget_parameter_showbutton_desc!}")
					.setValue(true)
				)
		;
	}

	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, long earliest, long latest) {
		// fetch Parameter objects
		// Create html Form Fields
		// remove submit Button
		
		//---------------------------------
		// Get ID
		String dashboardID = request.getParameter("dashboardid");
		
		//---------------------------------
		// Resolve Parameters
		JsonElement paramsElement = jsonSettings.get("JSON_PARAMETERS");
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
		JsonElement showbuttonElement = jsonSettings.get("showbutton");
		String buttonLabel = "Update";
		if(showbuttonElement !=null && !showbuttonElement.isJsonNull()) {
			boolean showButton = showbuttonElement.getAsBoolean();
			if(!showButton) {
				buttonLabel = null;
			}
		}
		//--------------------------------------
		// Add on change event for triggering updates
		
		CFWForm paramForm = new CFWForm("cfwWidgetParameterForm"+CFW.Random.randomStringAlphaNumerical(12), buttonLabel);
		paramForm.isInlineForm(true);
		paramForm.addAttribute("onclick", "cfw_dashboard_parameters_fireParamWidgetUpdate(this, true);");
		
		for(CFWObject object : paramsResultArray) {
			DashboardParameter param = (DashboardParameter)object;
			
			CFWField valueField = param.getField(DashboardParameterFields.VALUE.toString());
			valueField
				//.addAttribute("onchange", "cfw_dashboard_parameters_fireParamWidgetUpdate(this, true);")
				//.addAttribute("onblur", "cfw_dashboard_parameters_fireParamWidgetUpdate(this, true);")
				.addAttribute("data-widgettype", param.widgetType())
				.addAttribute("data-settingslabel", param.paramSettingsLabel())
				.setName(param.name())
				.setLabel(CFW.Utils.Text.fieldNameToLabel(param.name()))
				.isDecoratorDisplayed(false)
				.addCssClass(" form-control-sm cfw-widget-parameter-marker");
			
			paramForm.addField(valueField);
		}
		
		paramForm.appendToPayload(response);		
		
	}

	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "cfw_widget_parameter.js");
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add(js);
		return array;
	}

	@Override
	public ArrayList<FileDefinition> getCSSFiles() { return null; }

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		return map;
	}
	
	
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
			&& settings.has("JSON_PARAMETERS")
			&& settings.get("JSON_PARAMETERS").isJsonObject()) {
				usedParamIDs.addAll(settings.get("JSON_PARAMETERS").getAsJsonObject().keySet());
			}
		}
		
		return usedParamIDs;
		
	}

}
