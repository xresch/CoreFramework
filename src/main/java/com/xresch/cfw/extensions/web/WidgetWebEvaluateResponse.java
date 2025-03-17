package com.xresch.cfw.extensions.web;

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
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.utils.web.CFWHttp.CFWHttpRequestBuilder;
import com.xresch.cfw.utils.web.CFWHttp.CFWHttpResponse;
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
public class WidgetWebEvaluateResponse extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(WidgetWebEvaluateResponse.class.getName());
	
	private static final String PARAM_METHOD = "METHOD";
	private static final String PARAM_URLS = "URLS";
	private static final String PARAM_LABELS = "LABELS";
	private static final String PARAM_HEADERS = "JSON_HEADERS";
	private static final String PARAM_BODY = "BODY";
	private static final String PARAM_USERNAME = "USERNAME";
	private static final String PARAM_PASSWORD = "PASSWORD";
	
	private static final String PARAM_STATUS_CODE = "STATUS_CODE";
	private static final String PARAM_CHECK_FOR = "CHECK_FOR";
	private static final String PARAM_CHECK_TYPE = "CHECK_TYPE";
	
	private static final String PARAM_DEBUG_MODE = "DEBUG_MODE";
	
	
	// Returns the unique name of the widget. Has to be the same unique name as used in the javascript part.
	@Override
	public String getWidgetType() {
		return FeatureWebExtensions.WIDGET_PREFIX+"_evaluateresponse"; 
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
		return FeatureWebExtensions.WIDGET_CATEGORY_WEB;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetName() { return "Evaluate Response"; }
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureWebExtensions.PACKAGE_RESOURCES, "widget_"+getWidgetType()+".html");
	}

	// Creates an object with fields that will be used as the settings for this particular widget.
	@Override
	public CFWObject getSettings() {
		return new CFWObject()

				.addField(CFWField.newString(CFWField.FormFieldType.SELECT, PARAM_METHOD)
						.setLabel("{!cfw_widget_webextensions_method!}")
						.setDescription("{!cfw_widget_webextensions_method_desc!}")
						.addOptions("GET", "POST")
						.setValue("GET")
						.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
					)
				
				.addField(CFWField.newString(CFWField.FormFieldType.TEXTAREA, PARAM_URLS)
						.setLabel("{!cfw_widget_webextensions_urls!}")
						.setDescription("{!cfw_widget_webextensions_urls_desc!}")
						.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
						.setValue("")
					)
				
				.addField(CFWField.newString(CFWField.FormFieldType.TEXTAREA, PARAM_LABELS)
						.setLabel("{!cfw_widget_webextensions_labels!}")
						.setDescription("{!cfw_widget_webextensions_labels_desc!}")
						.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
						.setValue("")
						)
				
				.addField(CFWField.newValueLabel(PARAM_HEADERS)
						.setLabel("{!cfw_widget_webextensions_headers!}")
						.setDescription("{!cfw_widget_webextensions_headers_desc!}")
						.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
						)
				
				.addField(CFWField.newString(FormFieldType.TEXTAREA, PARAM_BODY)
						.setLabel("{!cfw_widget_webextensions_body!}")
						.setDescription("{!cfw_widget_webextensions_body_desc!}")
						.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
						)

				.addField(CFWField.newString(CFWField.FormFieldType.TEXT, PARAM_USERNAME)
						.setLabel("{!cfw_widget_webextensions_username!}")
						.setDescription("{!cfw_widget_webextensions_username_desc!}")
						.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
						.setValue(null)
					)
				
				.addField(CFWField.newString(CFWField.FormFieldType.PASSWORD, PARAM_PASSWORD)
						.setLabel("{!cfw_widget_webextensions_password!}")
						.setDescription("{!cfw_widget_webextensions_password_desc!}")
						// DO NOT TOUCH! Changing salt will corrupt all password stored in the database
						.enableEncryption("emp_httpextensions_encryptionSalt-fFDSgasTR1")
						.disableSanitization()
						.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
						.setValue(null)
					)
				
				// Labels for the URL ?
				.addField(
						CFW.Utils.Text.getCheckTypeOptionField(
								  PARAM_CHECK_TYPE
								, "{!cfw_widget_webextensions_checktype!}"
								, "{!cfw_widget_webextensions_checktype_desc!}"
							)
							.setValue("Contains")
							.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
					)

				.addField(CFWField.newString(CFWField.FormFieldType.TEXTAREA, PARAM_CHECK_FOR)
						.setLabel("{!cfw_widget_webextensions_matchfor!}")
						.setDescription("{!cfw_widget_webextensions_matchfor_desc!}")
						.setValue("")
						.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
					)

				.addField(CFWField.newInteger(CFWField.FormFieldType.NUMBER, PARAM_STATUS_CODE)
						.setLabel("{!cfw_widget_webextensions_statuscode!}")
						.setDescription("{!cfw_widget_webextensions_statuscode_desc!}")
						.addValidator(new NumberRangeValidator(0, 999))
						.setValue(200)
						.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
					)
						
				.addField(WidgetSettingsFactory.createDefaultDisplayAsField())
				.addAllFields(WidgetSettingsFactory.createTilesSettingsFields())
				
				.addField(CFWField.newBoolean(CFWField.FormFieldType.BOOLEAN, PARAM_DEBUG_MODE)
						.setLabel("{!cfw_widget_webextensions_debugmode!}")
						.setDescription("{!cfw_widget_webextensions_debugmode_desc!}")
						.setValue(false)
					)
				;

	}

	@SuppressWarnings("unchecked")
	@Override
	public void fetchData(HttpServletRequest httpServletRequest, JSONResponse jsonResponse, CFWObject cfwObject, JsonObject jsonObject, CFWTimeframe timeframe) {

		//------------------------------------
		// Get Method
		String method = (String) cfwObject.getField(PARAM_METHOD).getValue();
		if(Strings.isNullOrEmpty(method)){
			method = "GET";
		}
		
		//------------------------------------
		// Get IRLS
		String urls = (String) cfwObject.getField(PARAM_URLS).getValue();
		if (Strings.isNullOrEmpty(urls)) {
			return;
		}
		String[] splittedURLs = urls.trim().split("\\r\\n|\\n");
		
		//------------------------------------
		// Get Labels
		String labels = (String) cfwObject.getField(PARAM_LABELS).getValue();
		String[] splittedLabels = new String[] {};
		if (!Strings.isNullOrEmpty(labels)) {
			splittedLabels = labels.trim().split("\\r\\n|\\n");
		}

		//------------------------------------
		// Get Credentials
		String username = (String) cfwObject.getField(PARAM_USERNAME).getValue();
		String password = (String) cfwObject.getField(PARAM_PASSWORD).getValue();

		//------------------------------------
		// Get Headers and Body
		LinkedHashMap<String, String> headers = (LinkedHashMap<String, String>) cfwObject.getField(PARAM_HEADERS).getValue();
		String bodyContents = (String) cfwObject.getField(PARAM_BODY).getValue();

		//------------------------------------
		// Get Checks
		String checkType = (String) cfwObject.getField(PARAM_CHECK_TYPE).getValue();
		String checkFor = (String) cfwObject.getField(PARAM_CHECK_FOR).getValue();

		Integer expectedResponseCode = (Integer) cfwObject.getField(PARAM_STATUS_CODE).getValue();

		//------------------------------------
		// Get Debug Mode
		boolean debugMode = (Boolean) cfwObject.getField(PARAM_DEBUG_MODE).getValue();
		
		//------------------------------------
		// Iterate URLs and Build Response
		JsonArray jsonArray = new JsonArray();

		int index = 0;
		for (String splittedURL : splittedURLs) {

			//----------------------------------------
			// Check URL
			if(!splittedURL.contains("://") 
			&& !splittedURL.startsWith("http") ) {
				splittedURL = "https://"+splittedURL;
			}
			//----------------------------------------
			// Build Request and Call URL
			CFWHttpRequestBuilder requestBuilder = CFW.HTTP.newRequestBuilder(splittedURL);
			
			if(method.trim().toUpperCase().equals("POST")) {
				requestBuilder.POST();
			}else {
				requestBuilder.GET();
			}
			
			if(!Strings.isNullOrEmpty(username)) {
				requestBuilder.setAuthCredentialsBasic(username, password);
			}
			
			requestBuilder.headers(headers);
			
			if(!Strings.isNullOrEmpty(bodyContents)) {
				requestBuilder.body(bodyContents);
			}

			CFWHttpResponse response = requestBuilder.send();

			if(response.errorOccured()) {
				CFW.Messages.addInfoMessage("Hint: Check if your URLs include the right protocol(http/https).");
				CFW.Messages.addInfoMessage("Another Hint: The application server might not have access to the URL. Check with the application support.");
				return;
			}
			
			
			//------------------------------------
			// Handle Debug Mode
			if(debugMode) {
				JsonObject debugObject = new JsonObject();
								
				debugObject.addProperty("URL", requestBuilder.buildURLwithParams());
				debugObject.addProperty("RESPONSE_STATUS", response.getStatus());
				debugObject.add("RESPONSE_HEADERS", response.getHeadersAsJson());
				debugObject.addProperty("RESPONSE_BODY", response.getResponseBody());
				
				
				jsonArray.add(debugObject);
				jsonResponse.setPayload(jsonArray);
				return;
			}
			

			//----------------------------------------
			// Create Data Object
			JsonObject returnObject = new JsonObject();

			if(index < splittedLabels.length 
			&& !Strings.isNullOrEmpty(splittedLabels[index])) {
				returnObject.addProperty("LABEL", splittedLabels[index]);
			}else {
				
				try {
					returnObject.addProperty("LABEL", new URL(splittedURL).getHost());
				} catch (MalformedURLException e) {
					returnObject.addProperty("LABEL", splittedURL);
				}
			}
			
			returnObject.addProperty("URL", splittedURL);
			returnObject.addProperty(PARAM_CHECK_TYPE, checkType);
			returnObject.addProperty(PARAM_CHECK_FOR, checkFor);

			//----------------------------------------
			// Check response content
			boolean result = false;
			if(Strings.isNullOrEmpty(checkFor)) {
				result = true;
			}else {
				result = CFW.Utils.Text.checkTextForContent(checkType, response.getResponseBody(), checkFor);
			}
			
			returnObject.addProperty("CHECK_RESULT", result);

			//----------------------------------------
			// Check response code
			int actualResponseStatusCode = response.getStatus();
			
			returnObject.addProperty(PARAM_STATUS_CODE, actualResponseStatusCode);
			
			if(expectedResponseCode != null) {
				boolean isValid =  (expectedResponseCode == actualResponseStatusCode);
				returnObject.addProperty("STATUS_CODE_VALID", isValid);
				if(!isValid) {
					returnObject.addProperty("STATUS_CODE_MESSAGE", "HTTP Status Code '"+expectedResponseCode
							+"' was expected but '"+actualResponseStatusCode+"' was received.");
				}
				
			}
			
			//----------------------------------------
			// Add Object to the Result Array
			jsonArray.add(returnObject);
			index++;

		}

		// Add jsonArray to the payload
		jsonResponse.setPayload(jsonArray);

	}

	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> fileDefinitions = new ArrayList<>();
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE,
				FeatureWebExtensions.PACKAGE_RESOURCES, "widget_evaluateresponse.js");
		fileDefinitions.add(js);
		return fileDefinitions;
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureWebExtensions.PACKAGE_RESOURCES, "lang_en_webextensions.properties"));
		map.put(Locale.GERMAN, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureWebExtensions.PACKAGE_RESOURCES, "lang_de_webextensions.properties"));
		return map;
	}
}
