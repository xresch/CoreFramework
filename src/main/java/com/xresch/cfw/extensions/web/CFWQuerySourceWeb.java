package com.xresch.cfw.extensions.web;

import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.commands.CFWQueryCommandFormatField;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.utils.CFWRandom;
import com.xresch.cfw.utils.CFWHttp.CFWHttpRequestBuilder;
import com.xresch.cfw.utils.CFWHttp.CFWHttpResponse;
import com.xresch.cfw.utils.json.JsonTimerangeChecker;
import com.xresch.cfw.validation.CustomValidator;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;
	
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourceWeb extends CFWQuerySource {

	private static final String PARAM_METHOD 	= "method";
	private static final String PARAM_URL 		= "url";
	private static final String PARAM_HEADERS 	= "headers";
	private static final String PARAM_BODY 		= "body";
	private static final String PARAM_USERNAME 	= "username";
	private static final String PARAM_PASSWORD 	= "password";
	private static final String PARAM_AS	= "as";
	
	private static final String PARAM_TIMEFIELD = "timefield";
	private static final String PARAM_TIMEFORMAT = "timeformat";
	
	private QueryPartValue listFormatter = null;

	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceWeb(CFWQuery parent) {
		super(parent);
		
		JsonArray listFormatterParams = new JsonArray();
		listFormatterParams.add("list");
		listFormatterParams.add("none");
		listFormatterParams.add("0px");
		listFormatterParams.add(true);
		listFormatter = QueryPartValue.newJson(listFormatterParams);
	}

	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String uniqueName() {
		return "web";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Takes http parameters as input and loads data from a Web API.";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionTime() {
		return "For JSON: Use the parameters timefield and timeformat to specify the time filtering.(Default: no filtering by time)";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureWebExtensions.PACKAGE_RESOURCES, "source_web.html");
	}
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionRequiredPermission() {
		return "None";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return CFW.Context.Request.hasPermission(FeatureWebExtensions.PERMISSION_WEB_EXTENSIONS);
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		// do nothing
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public CFWObject getParameters() {
		return new CFWObject()
				
				.addField(
						CFWField.newString(FormFieldType.TEXT, PARAM_URL)
							.setDescription("The JSON string that should be parsed. Either an array of JSON Objects or a single JSON Object.")
							.addValidator(new NotNullOrEmptyValidator())
							.disableSanitization()
					)
				
				.addField(
						CFWField.newString(FormFieldType.TEXT, PARAM_METHOD)
							.setDescription("(Optional)The HTTP method used for the request. Either GET(default) or POST.")
							.setValue("GET")
							.addValidator(new CustomValidator() {
								
								@Override
								public boolean validate(Object value) {
									
									if (value == null) { 
										setInvalidMessage("Method cannot be null.");
										return false;
									} 
									
									String valueString = value.toString().trim().toUpperCase();
									if(Strings.isNullOrEmpty(valueString)) {
										setInvalidMessage("Method cannot be empty.");
										return false;
									}
									
									
									if( !valueString.equals("GET") && !valueString.equals("POST") ) {
										setInvalidMessage("Method must be either GET or POST.");
										return false;
									}
									
									return true;
								}
								
							})
					)
				
				.addField(
						CFWField.newString(FormFieldType.TEXT, PARAM_HEADERS)
							.setDescription("(Optional)The HTTP headers for the request.")
							.addValidator(new NotNullOrEmptyValidator())
							.disableSanitization()
					)
				.addField(
						CFWField.newString(FormFieldType.TEXTAREA, PARAM_BODY)
								.setDescription("(Optional)The body contents of the request. Setting the header 'Content-Type' might be needed(e.g. 'application/json; charset=UTF-8').")
								.disableSanitization()
						)
				
				.addField(
						CFWField.newString(FormFieldType.TEXT, PARAM_USERNAME)
								.setDescription("(Optional)The username for Basic Authentication.")
								.addValidator(new NotNullOrEmptyValidator())
								.disableSanitization()
						)
				
				.addField(
						CFWField.newString(FormFieldType.TEXT, PARAM_PASSWORD)
								.setDescription("(Optional)The password for Basic Authentication.")
								.addValidator(new NotNullOrEmptyValidator())
								.disableSanitization()
						)
				
				.addField(
						CFWField.newString(FormFieldType.TEXT, PARAM_AS)
						.setDescription("(Optional)Define how the response should be parsed, either 'json' or 'plain'. (Default: json)")
						.addValidator(new NotNullOrEmptyValidator())
						.disableSanitization()
						)
				
				.addField(
						CFWField.newString(FormFieldType.TEXT, PARAM_TIMEFIELD)
							.setDescription("The field of the response that contains the time.")	
					)
				
				.addField(
						CFWField.newString(FormFieldType.TEXT, PARAM_TIMEFORMAT)
							.setDescription("The format of the time in the time field. (Default: 'epoch').")	
							.setValue("epoch")
					)
			;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public void parametersPermissionCheck(CFWObject parameters) throws ParseException {
		//do nothing
	}
	
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public void execute(CFWObject parameters, LinkedBlockingQueue<EnhancedJsonObject> outQueue, long earliestMillis, long latestMillis, int limit) throws Exception {
		
		//------------------------------------
		// Get As
		String parseAs = (String) parameters.getField(PARAM_AS).getValue();	
		if(Strings.isNullOrEmpty(parseAs)) { parseAs = "json"; };
		
		parseAs = parseAs.trim().toLowerCase();
		
		//------------------------------------
		// Get Method
		String method = (String) parameters.getField(PARAM_METHOD).getValue();	
		
		//------------------------------------
		// Get URL
		String url = (String) parameters.getField(PARAM_URL).getValue();
		if(!url.contains("://")) {
			url = "https://"+url;
		}

		//------------------------------------
		// Get Credentials
		String username = (String) parameters.getField(PARAM_USERNAME).getValue();
		String password = (String) parameters.getField(PARAM_PASSWORD).getValue();
		
		//------------------------------------
		// Get Headers
		String headersString = (String) parameters.getField(PARAM_HEADERS).getValue();
		
		HashMap<String, String> headersMap = new HashMap<>();
		
		if(headersString != null && headersString.startsWith("{")) {
			JsonObject headersObject = CFW.JSON.fromJson(headersString).getAsJsonObject();
			
			for(Entry<String, JsonElement> entry : headersObject.entrySet()) {
				headersMap.put(entry.getKey(), entry.getValue().getAsString());
			}
		}
		
		//------------------------------------
		// Get Body
		String bodyString = (String) parameters.getField(PARAM_BODY).getValue();
		
		//----------------------------------------
		// Build Request
		CFWHttpRequestBuilder requestBuilder = CFW.HTTP.newRequestBuilder(url);
		
		if(method.trim().toUpperCase().equals("GET")) {
			requestBuilder.GET();
		}else {
			requestBuilder.POST();
		}
		
		if(!Strings.isNullOrEmpty(username)) {
			requestBuilder.authenticationBasic(username, password);
		}
		
		requestBuilder.headers(headersMap);

		if(!Strings.isNullOrEmpty(bodyString)) {
			requestBuilder.body(bodyString);
		}
		
		//----------------------------------------
		// Send Request and Fetch Data
		CFWHttpResponse response = requestBuilder.send();

		if(response.errorOccured()) {
			CFW.Messages.addInfoMessage("Hint: Check if your URL includes the right protocol(http, https..).");
			CFW.Messages.addInfoMessage("Another Hint: The application server might not have access to the URL. Check with the application support.");
			return;
		}
		
		//------------------------------------
		// Setup Timerange Filter
		String timefield = (String)parameters.getField(PARAM_TIMEFIELD).getValue();
		String timeformat = (String)parameters.getField(PARAM_TIMEFORMAT).getValue();
		
		JsonTimerangeChecker timerangeChecker = 
				new JsonTimerangeChecker(timefield, timeformat, earliestMillis, latestMillis)
					.epochAsNewField("_epoch");
		
		//------------------------------------
		// Parse Data
		
		switch(parseAs) {
			
			case "json":	parseAsJson(outQueue, limit, response, timefield, timerangeChecker); 	break;
			case "plain":	parseAsPlain(outQueue, limit, response, timefield, timerangeChecker); 	break;
			case "http":	parseAsHTTP(outQueue, limit, response, timefield, timerangeChecker); 	break;
			case "lines":	parseAsLines(outQueue, limit, response, timefield, timerangeChecker); 	break;
			default:  		parseAsJson(outQueue, limit, response, timefield, timerangeChecker); 	break;

		}
		
	
	}

	/******************************************************************
	 *
	 ******************************************************************/
	private void parseAsJson(
				LinkedBlockingQueue<EnhancedJsonObject> outQueue
				, int limit
				, CFWHttpResponse response
				, String timefield
				, JsonTimerangeChecker timerangeChecker
				) throws ParseException {
		
		
		String data = response.getResponseBody();
		
		//------------------------------------
		// Parse Data
		JsonElement element;
		
		try {
			element = CFW.JSON.fromJson(data);
		}catch(Exception e) {
			
			//------------------------------------
			// Create Error Response
			createExceptionResponse(outQueue, response, data, e);
			return;
		}
		
		//------------------------------------
		// Iterate Data
		if(element.isJsonObject()) {
			
			if(timefield != null && !timerangeChecker.isInTimerange(element.getAsJsonObject(), false)) {
				return; 
			}
			
			outQueue.add( new EnhancedJsonObject(element.getAsJsonObject()) );
			return;
		}
		
		if(element.isJsonArray()) {
			int recordCounter = 0;
			for(JsonElement current : element.getAsJsonArray() ) {
				if(current.isJsonObject()) {
					
					if( this.isLimitReached(limit, recordCounter)) { break; }
					
					if(timefield != null && !timerangeChecker.isInTimerange(current.getAsJsonObject(), false)) {
						continue; 
					}
					
					outQueue.add( new EnhancedJsonObject(current.getAsJsonObject()) );
					recordCounter++;
				}
				
			}
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void parseAsPlain(
				LinkedBlockingQueue<EnhancedJsonObject> outQueue
				, int limit
				, CFWHttpResponse response
				, String timefield
				, JsonTimerangeChecker timerangeChecker
				) throws ParseException {
		
		String data = response.getResponseBody();
		
		//------------------------------------
		// Parse Data
		
		try {
			
			EnhancedJsonObject object = new EnhancedJsonObject();
			object.addProperty("response", data);
			outQueue.add( object );
			
		}catch(Exception e) {
			
			//------------------------------------
			// Create Error Response
			createExceptionResponse(outQueue, response, data, e);
			return;
		}
		
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void parseAsLines(
				LinkedBlockingQueue<EnhancedJsonObject> outQueue
				, int limit
				, CFWHttpResponse response
				, String timefield
				, JsonTimerangeChecker timerangeChecker
				) throws ParseException {
		
		String data = response.getResponseBody();
		
		//------------------------------------
		// Parse Data
		
		try {
			
			if(!Strings.isNullOrEmpty(data)) {
				
				for( String line : data.split("\n\r|\n") ){
					EnhancedJsonObject object = new EnhancedJsonObject();
					object.addProperty("line", line);
					outQueue.add( object );
				}
			}
			
			
		}catch(Exception e) {
			
			//------------------------------------
			// Create Error Response
			createExceptionResponse(outQueue, response, data, e);
			return;
		}
		
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void parseAsHTTP(
				LinkedBlockingQueue<EnhancedJsonObject> outQueue
				, int limit
				, CFWHttpResponse response
				, String timefield
				, JsonTimerangeChecker timerangeChecker
				) throws ParseException {
		
		//------------------------------------
		// Parse Data
		String data = response.getResponseBody();
		try {
			
			EnhancedJsonObject object = new EnhancedJsonObject();
			object.addProperty("status", response.getStatus());
			object.add("headers", response.getHeadersAsJson());
			object.addProperty("body", data);
			outQueue.add( object );
			
		}catch(Exception e) {
			
			//------------------------------------
			// Create Error Response
			createExceptionResponse(outQueue, response, data, e);
			return;
		}
		
	}

	/******************************************************************
	 *
	 ******************************************************************/
	private void createExceptionResponse(LinkedBlockingQueue<EnhancedJsonObject> outQueue, CFWHttpResponse response,
			String data, Exception e) throws ParseException {
		
		EnhancedJsonObject exceptionObject = new EnhancedJsonObject();
		exceptionObject.addProperty("Key", "Exception" );
		exceptionObject.addProperty("Value", CFW.Utils.Text.stacktraceToString(e) );
		outQueue.add( exceptionObject );
		
		exceptionObject = new EnhancedJsonObject();
		exceptionObject.addProperty("Key", "HTTPStatus" );
		exceptionObject.addProperty("Value", response.getStatus() );
		outQueue.add( exceptionObject );
		exceptionObject = new EnhancedJsonObject();
		exceptionObject.addProperty("Key", "HTTPHeaders" );
		exceptionObject.add("Value", CFW.JSON.objectToJsonElement(response.getHeaders()) );
		outQueue.add( exceptionObject );
		
		exceptionObject = new EnhancedJsonObject();
		exceptionObject.addProperty("Key", "ResponseBody" );
		exceptionObject.addProperty("Value", data );
		outQueue.add( exceptionObject );
					
		CFWQueryCommandFormatField.addFormatter(this.parent.getContext(), "Value", listFormatter);
	}

}
