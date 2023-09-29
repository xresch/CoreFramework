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
import com.xresch.cfw.validation.NumberRangeValidator;

public class WidgetLabel extends WidgetDefinition {

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String getWidgetType() {return "cfw_label";}

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
	public String widgetName() { return "Label"; }
	
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
				.addField(CFWField.newString(FormFieldType.TEXT, "label")
						.setLabel("{!cfw_widget_cfwlabel_label!}")
						.setDescription("{!cfw_widget_cfwlabel_label_desc!}")
						.setValue("Label")
						)
				.addField(CFWField.newString(FormFieldType.SELECT, "direction")
						.setLabel("{!cfw_widget_cfwlabel_direction!}")
						.setDescription("{!cfw_widget_cfwlabel_direction_desc!}")
						.setOptions(new String[] {"Left to Right", "Bottom to Top", "Top to Bottom", "Upside Down"})
						.setValue("Left to Right")
						)
				.addField(CFWField.newString(FormFieldType.TEXT, "link")
						.setLabel("{!cfw_widget_cfwlabel_link!}")
						.setDescription("{!cfw_widget_cfwlabel_link_desc!}")
						.setValue("")
						)
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, "newwindow")
						.setLabel("{!cfw_widget_cfwlabel_newwindow!}")
						.setDescription("{!cfw_widget_cfwlabel_newwindow_desc!}")
						.setValue(true)
						)
				.addField(CFWField.newString(FormFieldType.NUMBER, "sizefactor")
						.setLabel("{!cfw_dashboard_sizefactor!}")
						.setDescription("{!cfw_dashboard_sizefactor_desc!}")
						.addValidator(new NumberRangeValidator(0.1f, 10.0f))
						//.setOptions(new String[]{"0.5", "1", "1.25", "1.5", "1.75", "2.0", "2.5", "3.0", "4.0"})
						.setValue("1")
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
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "cfw_widget_label.js");
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
