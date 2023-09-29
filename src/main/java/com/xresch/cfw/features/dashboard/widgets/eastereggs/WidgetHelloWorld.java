package com.xresch.cfw.features.dashboard.widgets.eastereggs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.FeatureDashboard;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.validation.LengthValidator;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;

public class WidgetHelloWorld extends WidgetDefinition {

	private static final String NUMBER = "NUMBER";
	private static final String LIKES_TIRAMISU = "LIKES_TIRAMISU";
	private static final String MESSAGE = "MESSAGE";
	private static final Logger logger = CFWLog.getLogger(WidgetHelloWorld.class.getName());

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String getWidgetType() {return "cfw_helloworld";}

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
	public String widgetCategory() {
		return FeatureDashboard.WIDGET_CATEGORY_EASTEREGGS;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetName() { return "Hello World"; }
	
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
				.addField(CFWField.newString(FormFieldType.TEXT, "name")
								.setLabel("{!cfw_widget_helloworld_name!}")
								.setDescription("{!cfw_widget_helloworld_name_desc!}")
								.addValidator(new LengthValidator(2, 25))
								.setValue("Jane Doe")
				)
				.addField(CFWField.newTagsSelector("JSON_HOBBIES_SELECTOR")
							.setLabel("Hobbies")
							.setAutocompleteHandler(new CFWAutocompleteHandler(5) {
								
								public AutocompleteResult getAutocompleteData(HttpServletRequest request, String inputValue, int cursorPosition) {
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
				.addField(CFWField.newString(FormFieldType.TEXT, "favorite_food")
						.setDescription("Enter your favorite food")
						.addValidator(new LengthValidator(-1, 50))
						.setValue("Tiramisu")
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

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, CFWTimeframe timeframe) { 
		//int number = settings.get("number").getAsInt();
		String number = jsonSettings.get("number").getAsString();
		String favoriteFood = jsonSettings.get("favorite_food").getAsString();
		response.getContent().append("\"{!cfw_widget_helloworld_serverside!} "+number+". Your favorite food is: "+favoriteFood+"\"");
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "cfw_widget_helloworld.js");
		array.add(js);
		return array;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public ArrayList<FileDefinition> getCSSFiles() {
		// no CCS files for this widget
		return null;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "lang_en_widget_helloworld.properties"));
		map.put(Locale.GERMAN, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "lang_de_widget_helloworld.properties"));
		return map;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public boolean hasPermission(User user) {
		// just an example, replace with your own permission checks
		if(user.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_CREATOR)
		|| user.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
			return true;
		}
		return false;
	}
	

	/************************************************************
	 * 
	 ************************************************************/
	public boolean supportsTask() {
		return true;
	}
	

	/************************************************************
	 * 
	 ************************************************************/
	public CFWObject getTasksParameters() {
		return new CFWObject()
				.addField(
					CFWField.newString(FormFieldType.TEXT, MESSAGE)
							.setDescription("Message to write to the log file.")
							.addValidator(new LengthValidator(5,500))
				)
				.addField(
						CFWField.newBoolean(FormFieldType.BOOLEAN, LIKES_TIRAMISU)
								.setDescription("Affinity of tiramisu to write to the log file")
				)
				.addField(
						CFWField.newInteger(FormFieldType.NUMBER, NUMBER)
								.setDescription("Number to write to the log")
				)
				;
	}
	

	/************************************************************
	 * 
	 ************************************************************/
	public String getTaskDescription() {
		return "The task of this widget writes a message to the log file.";
	}

	/************************************************************
	 * 
	 ************************************************************/
	public void executeTask(JobExecutionContext context, CFWObject taskParams, DashboardWidget widget, CFWObject widgetSettings, CFWTimeframe offset) throws JobExecutionException {
		
		new CFWLog(logger)
			.custom("likesTiramisu", taskParams.getField(LIKES_TIRAMISU).getValue())
			.custom("chosenNumber", taskParams.getField(NUMBER).getValue())
			.info(taskParams.getField(MESSAGE).getValue().toString());
		
		//-----------------------------
		// Random Message for Testing
		MessageType[] types = MessageType.values();
		int randomIndex = CFW.Random.randomFromZeroToInteger(3);
		CFW.Messages.addMessage(types[randomIndex], "Hello World Task wrote a log message.");
		
	}

}
