package com.xresch.cfw.extensions.cli;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.CFWFieldFlag;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.dashboard.FeatureDashboard;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.dashboard.widgets.WidgetSettingsFactory;
import com.xresch.cfw.features.query._CFWQueryCommonStringParser.CFWQueryStringParserType;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.utils.CFWHttp.CFWHttpRequestBuilder;
import com.xresch.cfw.utils.CFWHttp.CFWHttpResponse;
import com.xresch.cfw.utils.CFWUtilsText;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;
import com.xresch.cfw.validation.NumberRangeValidator;

/**************************************************************************************************************
 * 
 * @author Joel Laeubin (Base implementation)
 * @author Reto Scheiwiller (integration into EMP, ehancements etc...)
 * 
 * (c) Copyright 2022 
 * 
 * @license MIT-License
 **************************************************************************************************************/
public class WidgetCLIResults extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(WidgetCLIResults.class.getName());
	
	private static final String PARAM_DIR 		= "WORKING_DIRECTORY";
	private static final String PARAM_COMMANDS 	= "COMMANDS";
	private static final String PARAM_ENV		= "JSON_ENV_VARIABLES";
	private static final String PARAM_HEAD		= "HEAD";
	private static final String PARAM_TAIL		= "TAIL";
	private static final String PARAM_COUNT_SKIPPED	= "COUNT_SKIPPED";
	private static final String PARAM_TIMEOUT 	= "TIMEOUT";
	
	// Returns the unique name of the widget. Has to be the same unique name as used in the javascript part.
	@Override
	public String getWidgetType() {
		return FeatureCLIExtensions.WIDGET_PREFIX+"_results"; 
	}
	
	@Override
	public WidgetDataCache.WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCache.WidgetDataCachePolicy.ALWAYS;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetCategory() {
		return FeatureCLIExtensions.WIDGET_CATEGORY_CLI;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetName() { return "CLI Results"; }
	
	/************************************************************
	 * Check if the current user has the required permission to create and
	 * edit the widget. Returns true by default.
	 * return true if has permission, false otherwise
	 * @param user TODO
	 ************************************************************/
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureCLIExtensions.PERMISSION_CLI_EXTENSIONS);
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureCLIExtensions.PACKAGE_RESOURCES, "widget_"+getWidgetType()+".html");
	}

	// Creates an object with fields that will be used as the settings for this particular widget.
	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				
				.addField(
						CFWField.newString(FormFieldType.TEXT, PARAM_DIR)
							.setDescription("(Optional)The working directory where the commands should be executed. (Default: \""+FeatureCLIExtensions.getDefaultFolderDescription()+"\")")
							.disableSanitization()
					)

				.addField(
						CFWField.newString(FormFieldType.TEXTAREA, PARAM_COMMANDS)
								.setDescription("The body contents of the request. Setting the header 'Content-Type' might be needed(e.g. 'application/json; charset=UTF-8').")
								.disableSanitization()
						)
				
				.addField(
						CFWField.newValueLabel(PARAM_ENV)
							.setLabel("Environment Variables")
							.setDescription("(Optional)Additional environment variables which should be passed to the command line.")
							.addValidator(new NotNullOrEmptyValidator())
							.disableSanitization()
					)
				
				.addField(
						CFWField.newInteger(FormFieldType.NUMBER, PARAM_HEAD)
							.setDescription("(Optional)Number of lines that should be read from the head(start) of the output.")
							.disableSanitization()
							.addValidator(new NumberRangeValidator(0, 10000))
							.setValue(100)
					)
				
				.addField(
						CFWField.newInteger(FormFieldType.NUMBER, PARAM_TAIL)
							.setDescription("Number of lines that should be read from the tail(end) of the output.")
							.disableSanitization()
							.addValidator(new NumberRangeValidator(0, 10000))
							.setValue(100)
					)
				
				.addField(
						CFWField.newBoolean(FormFieldType.BOOLEAN, PARAM_COUNT_SKIPPED)
						.setDescription("If parameter head or tail is set, this parameter decides if skipped line count should be added in the output.(Default:true)")
						.disableSanitization()
						.setValue(true)
						)
				
				.addField(
						CFWField.newInteger(FormFieldType.NUMBER, PARAM_TIMEOUT)
						.setDescription("The timeout in seconds.")
						.setValue(120)
						)
				
			;
				
				

	}

	@SuppressWarnings("unchecked")
	@Override
	public void fetchData(HttpServletRequest httpServletRequest, JSONResponse jsonResponse, CFWObject cfwObject, JsonObject jsonObject, CFWTimeframe timeframe) {

		//------------------------------------
		// Get Working Dir
		String dir = (String) cfwObject.getField(PARAM_DIR).getValue();

		//------------------------------------
		// Get Commands
		String commands = (String) cfwObject.getField(PARAM_COMMANDS).getValue();
		if (Strings.isNullOrEmpty(commands)) {
			return;
		}

		
		//------------------------------------
		// Get Env Variables
		LinkedHashMap<String,String> envVariables = (LinkedHashMap<String,String>) cfwObject.getField(PARAM_ENV).getValue();

		//------------------------------------
		// Get Others
		Integer head = (Integer) cfwObject.getField(PARAM_HEAD).getValue();
		Integer tail = (Integer) cfwObject.getField(PARAM_TAIL).getValue();
		Integer timeout = (Integer) cfwObject.getField(PARAM_TIMEOUT).getValue();


		//------------------------------------
		// Get Checks
		Boolean countSkipped = (Boolean) cfwObject.getField(PARAM_COUNT_SKIPPED).getValue();
		

		try {
			//----------------------------------------
			// Execute Command
			CFWCLIExecutor executor = new CFWCLIExecutor(dir, commands, envVariables); 
			executor.execute();

			//----------------------------------------
			// Get Data
			String dataString;
			dataString = executor.readOutputOrTimeout(timeout, head, tail, countSkipped);

			//----------------------------------------
			// Create Result
			JsonObject result = new JsonObject();
			result.addProperty("output", dataString);
			jsonResponse.setPayload(result);
			
		} catch (Exception e) {
			new CFWLog(logger).severe(widgetName()+": Error while getting data:"+e.getMessage(), e);
		}
		
		return;

	}

	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> fileDefinitions = new ArrayList<>();
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE,
				FeatureCLIExtensions.PACKAGE_RESOURCES, "widget_"+getWidgetType()+".js");
		fileDefinitions.add(js);
		return fileDefinitions;
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		return map;
	}
}
