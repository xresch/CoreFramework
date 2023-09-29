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
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.dashboard.FeatureDashboard;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.validation.LengthValidator;

public class WidgetThresholdLegend extends WidgetDefinition {

	@Override
	public String getWidgetType() {return "emp_customthresholdlegend";} // keep "emp" for compatibility


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
	public String widgetName() { return "Threshold Legend"; }
	
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
		LengthValidator validator = new LengthValidator(0, 1024);
		return new CFWObject()
				.addField(CFWField.newString(FormFieldType.TEXT, "labelexcellent")
						.setLabel("{!cfw_widget_thresholdlegendlabelexcellent!}")
						.setDescription("{!cfw_widget_thresholdlegendlabelexcellent_desc!}")
						.addValidator(validator)
						.setValue("Excellent")
				)
				.addField(CFWField.newString(FormFieldType.TEXT, "labelgood")
						.setLabel("{!cfw_widget_thresholdlegendlabelgood!}")
						.setDescription("{!cfw_widget_thresholdlegendlabelgood_desc!}")
						.addValidator(validator)
						.setValue("Good")
				)
				.addField(CFWField.newString(FormFieldType.TEXT, "labelwarning")
						.setLabel("{!cfw_widget_thresholdlegendlabelwarning!}")
						.setDescription("{!cfw_widget_thresholdlegendlabelwarning_desc!}")
						.addValidator(validator)
						.setValue("Warning")
				)
				.addField(CFWField.newString(FormFieldType.TEXT, "labelemergency")
						.setLabel("{!cfw_widget_thresholdlegendlabelemergency!}")
						.setDescription("{!cfw_widget_thresholdlegendlabelemergency_desc!}")
						.addValidator(validator)
						.setValue("Emergency")
				)
				.addField(CFWField.newString(FormFieldType.TEXT, "labeldanger")
						.setLabel("{!cfw_widget_thresholdlegendlabeldanger!}")
						.setDescription("{!cfw_widget_thresholdlegendlabeldanger_desc!}")
						.addValidator(validator)
						.setValue("Danger")
				)
				.addField(CFWField.newString(FormFieldType.TEXT, "labelgray")
						.setLabel("{!cfw_widget_thresholdlegendlabelgray!}")
						.setDescription("{!cfw_widget_thresholdlegendlabelgray_desc!}")
						.addValidator(validator)
						.setValue("Unknown/No Data")
				)
				.addField(CFWField.newString(FormFieldType.TEXT, "labeldarkgray")
						.setLabel("{!cfw_widget_thresholdlegendlabeldarkgray!}")
						.setDescription("{!cfw_widget_thresholdlegendlabeldarkgray_desc!}")
						.addValidator(validator)
						.setValue("Disabled")
				)
				;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, CFWTimeframe timeframe) { /* do nothing */ }

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "cfw_widget_customthresholdlegend.js");
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add(js);
		return array;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public ArrayList<FileDefinition> getCSSFiles() { 
		
		FileDefinition css = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "cfw_widgets.css");
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
