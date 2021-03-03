package com.xresch.cfw.features.dashboard;

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
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.validation.LengthValidator;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;

public class WidgetHelloWorld extends WidgetDefinition {

	@Override
	public String getWidgetType() {return "cfw_helloworld";}

	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				.addField(CFWField.newString(FormFieldType.TEXT, "name")
								.setLabel("{!cfw_widget_helloworld_name!}")
								.setDescription("{!cfw_widget_helloworld_name_desc!}")
								.addValidator(new LengthValidator(2, 25))
								.setValue("Jane Doe")
				)
				.addField(CFWField.newTagsSelector("JSON_HOBBIES_SELECTOR")
							.setLabel("Hobbies")
							.setAutocompleteHandler(new CFWAutocompleteHandler(5) {
								
								public AutocompleteResult getAutocompleteData(HttpServletRequest request, String inputValue) {
									AutocompleteList list = new AutocompleteList();
									
									for(int i = 0; i < 25; i++ ) {
										String tag = inputValue;
										if(i > 0) {
											tag += "_"+i;
										}
										list.addItem(tag);
									}
									return new AutocompleteResult(list);
								}
							})
				)
				.addField(CFWField.newInteger(FormFieldType.NUMBER, "number")
						.addValidator(new NotNullOrEmptyValidator())
						.setValue(1)
				)	
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, "dosave")
						.setLabel("Do Save")
						.setValue(true)
				)
		;
	}

	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, JsonObject settings) { 
		//int number = settings.get("number").getAsInt();
		String number = settings.get("number").getAsString();
		response.getContent().append("\"{!cfw_widget_helloworld_serverside!} "+number+"\"");
	}

	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "cfw_widget_helloworld.js");
		array.add(js);
		return array;
	}

	@Override
	public ArrayList<FileDefinition> getCSSFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "lang_en_widget_helloworld.properties"));
		map.put(Locale.GERMAN, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "lang_de_widget_helloworld.properties"));
		return map;
	}

	@Override
	public boolean hasPermission() {
		if(CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_VIEWER)
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_CREATOR)
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
			return true;
		}
		return false;
	}

}
