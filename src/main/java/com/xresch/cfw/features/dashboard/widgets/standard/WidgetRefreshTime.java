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

public class WidgetRefreshTime extends WidgetDefinition {

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String getWidgetType() {return "cfw_refreshtime";}

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
		return FeatureDashboard.WIDGET_CATEGORY_STANDARD;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetName() { return "Refresh Time"; }
	
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
				.addField(CFWField.newString(FormFieldType.SELECT, "direction")
						.setLabel("{!cfw_widget_cfwlabel_direction!}")
						.setDescription("{!cfw_widget_cfwlabel_direction_desc!}")
						.setOptions(new String[] {"Left to Right", "Bottom to Top", "Top to Bottom", "Upside Down"})
						.setValue("Left to Right")
						)
				.addField(CFWField.newString(FormFieldType.SELECT, "sizefactor")
						.setLabel("{!cfw_dashboard_sizefactor!}")
						.setDescription("{!cfw_dashboard_sizefactor_desc!}")
						.setOptions(new String[]{"0.5", "1", "1.25", "1.5", "1.75", "2.0", "2.5", "3.0", "4.0"})
						.setValue("1")
				).addField(CFWField.newString(FormFieldType.LIST, "format")
						.setLabel("{!cfw_widget_cfwrefreshtime_format!}")
						.setDescription("{!cfw_widget_cfwrefreshtime_format_desc!}")
						.setOptions(new String[]{"HH:mm", "HH:mm:ss", "HH:mm:ss.SSS", "DD.MM.YYYY HH:mm", "MM/DD/YYYY HH:mm", "YYYY-MM-DDTHH:mm:ss.SSS", "DD.MM.YY" })
						.setValue("HH:mm")
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
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "cfw_widget_refreshtime.js");
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add(js);
		return array;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public ArrayList<FileDefinition> getCSSFiles() { return null; }

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		return map;
	}

}
