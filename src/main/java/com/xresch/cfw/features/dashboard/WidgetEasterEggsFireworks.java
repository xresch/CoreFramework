package com.xresch.cfw.features.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.response.JSONResponse;

public class WidgetEasterEggsFireworks extends WidgetDefinition {

	@Override
	public String getWidgetType() {return "cfw_easteregg_snow";}

	@Override
	public CFWObject getSettings() {
		return new CFWObject()
		
//				.addField(CFWField.newString(FormFieldType.NUMBER, "discolevel")
//						.setLabel("Discolevel")
//						.setDescription("Sets the level of disco-ness!")
//						.setValue("500")
//						.addValidator(new NumberRangeValidator(100, 999))
//				)
		;
	}

	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, long earliest, long latest) { 
		// nothing to do
	}

	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "cfw_widget_eastereggs_snow.js");
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add(js);
		return array;
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		return map;
	}

}
