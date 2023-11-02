package com.xresch.cfw.features.dashboard.widgets.advanced;

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
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.features.dashboard.FeatureDashboard;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.response.JSONResponse;

public class WidgetJavascript extends WidgetDefinition {

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String getWidgetType() {return "cfw_javascript";}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCachePolicy.OFF;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetCategory() {
		return FeatureDashboard.WIDGET_CATEGORY_ADVANCED;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetName() { return "Javascript"; }
	
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
	public CFWObject getSettings() {
		return new CFWObject()
				.addField(CFWField.newString(FormFieldType.TEXTAREA, "script")
						.setDescription("The javascript that should be added to the dashboard.")
						.setValue(
							"//Sample script on how to register function with interval\r\n" + 
							"//function myAction(){ console.log('executed'); }\r\n" + 
							"//myAction();\r\n" + 
							"//if(INTERVAL_xxx != undefined){ window.clearInterval(INTERVAL_xxx); }\r\n" + 
							"//var INTERVAL_xxx = window.setInterval(myAction, 3000);")
						)
		;
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
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureCore.PERMISSION_ALLOW_JAVASCRIPT);
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "cfw_widget_javascript.js");
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add(js);
		return array;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public ArrayList<FileDefinition> getCSSFiles() { 
		FileDefinition css = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "cfw_widget.css");
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add(css);
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
