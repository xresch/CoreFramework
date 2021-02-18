package com.xresch.cfw.features.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

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
import com.xresch.cfw.features.dashboard.DashboardParameter.DashboardParameterFields;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.utils.TextUtils;

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
								
								String dashboardID = "2081";

								return CFW.DB.DashboardParameters.autocompleteParamsForDashboard(dashboardID);
							}
						})			
					)
			
		;
	}

	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, JsonObject settings) {
		// fetch Parameter objects
		// Create html Form Fields
		// remove submit Button
		
		//---------------------------------
		// Resolve Parameters
		JsonElement paramsElement = settings.get("JSON_PARAMETERS");
		if(paramsElement.isJsonNull()) {
			return;
		}
		
		JsonObject paramsObject = paramsElement.getAsJsonObject();
		if(paramsObject.size() == 0) {
			return;
		}
		
		ArrayList<String> paramIDs = new ArrayList<>();
		for(Entry<String, JsonElement> entry : paramsObject.entrySet()) {
			paramIDs.add(entry.getKey());
		}
		
		ArrayList<CFWObject> paramsResultArray = new CFWSQL(new DashboardParameter())
				.select()
				.whereIn(DashboardParameterFields.PK_ID, paramIDs)
				.orderby(DashboardParameterFields.WIDGET_TYPE.toString(), DashboardParameterFields.WIDGET_SETTING.toString())
				.getAsObjectList();
		
		DashboardParameter.prepareParamObjectsForForm(paramsResultArray);
		
		//--------------------------------------
		// Add on change event for triggering updates
		CFWForm paramForm = new CFWForm("cfwWidgetParameterForm"+CFW.Random.randomStringAlphaNumerical(12), null);
		paramForm.isInlineForm(true);
		for(CFWObject object : paramsResultArray) {
			DashboardParameter param = (DashboardParameter)object;
			
			CFWField valueField = param.getField(DashboardParameterFields.VALUE.toString());
			valueField
				.addAttribute("onenter", "cfw_widget_paramater_fireParamUpdate(this);")
				.addAttribute("onblur", "cfw_widget_paramater_fireParamUpdate(this);")
				.setName(param.name())
				.setLabel(TextUtils.fieldNameToLabel(param.name()))
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

}
