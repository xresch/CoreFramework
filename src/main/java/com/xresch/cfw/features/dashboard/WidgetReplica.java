package com.xresch.cfw.features.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.response.JSONResponse;

public class WidgetReplica extends WidgetDefinition {

	@Override
	public String getWidgetType() {return "cfw_replica";}

	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				.addField(CFWField.newTagsSelector("JSON_DASHBOARD")
						.setLabel("{!cfw_widget_replica_dashboard!}")
						.setDescription("{!cfw_widget_replica_dashboard_desc!}")
						.addAttribute("maxTags", "1")
						.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
							public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
								return CFW.DB.Dashboards.autocompleteDashboard(searchValue, this.getMaxResults());					
							}
						})
				)
				.addField(CFWField.newTagsSelector("JSON_WIDGET")
						.setLabel("{!cfw_widget_replica_widget!}")
						.setDescription("{!cfw_widget_replica_widget_desc!}")
						.addAttribute("maxTags", "1")
						.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
							public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
								String JSON_DASHBOARD = request.getParameter("JSON_DASHBOARD");
								LinkedHashMap<String, String> selectedDashboard = CFW.JSON.fromJsonLinkedHashMap(JSON_DASHBOARD);
								String dashboardID = null;
								if(selectedDashboard.size() > 0) {
									dashboardID = selectedDashboard.keySet().iterator().next();
								}
								return CFW.DB.DashboardWidgets.autocompleteWidget(dashboardID, searchValue, this.getMaxResults());					
							}
						})
				)
		;
	}

	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, long earliest, long latest) { 

		//--------------------------------
		// Retrieve Widget ID
		JsonElement widgetElement = jsonSettings.get("JSON_WIDGET");
		if(widgetElement.isJsonNull()) {
			return;
		}
		
		JsonObject widgetObject = widgetElement.getAsJsonObject();
		if(widgetObject.size() == 0) {
			return;
		}
		
		String widgetID = widgetObject.keySet().iterator().next();

		//--------------------------------
		// Retrieve Widget Data
		
		response.getContent().append(CFW.DB.DashboardWidgets.getWidgetAsJSON(widgetID));
	}

	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "cfw_widget_replica.js");
		array.add(js);
		return array;
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		return null;
	}

}
