package com.xresch.cfw.features.dashboard.widgets.standard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.dashboard.FeatureDashboard;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.response.JSONResponse;

public class WidgetChecklist extends WidgetDefinition {

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String getWidgetType() {return "cfw_checklist";}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				.addField(
					CFWField.newString(FormFieldType.TEXTAREA, "content")
						.setDescription("Contains all the items on the checklist. Items checked begin with 'X '.")
						.setValue("Checkpoint A\r\nCheckpoint B\r\nCheckpoint C")
				)
				.addField(
					CFWField.newBoolean(FormFieldType.BOOLEAN, "strikethrough")
						.setDescription("Toggle if checked items should be striked through.")
						.setValue(true)
				)
		;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetCategory() {
		return FeatureDashboard.WIDGET_CATEGORY_STANDARD;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetName() { return "Checklist"; }
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureDashboard.PACKAGE_MANUAL, "widget_"+getWidgetType()+".html");
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCachePolicy.ALWAYS;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, CFWTimeframe timeframe) { 
		// nothing to do
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "cfw_widget_checklist.js");
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add(js);
		return array;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		return map;
	}

}
