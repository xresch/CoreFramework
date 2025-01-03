package com.xresch.cfw.extensions.web;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query._CFWQueryCommon;
import com.xresch.cfw.features.query._CFWQueryCommonStringParser;
import com.xresch.cfw.features.query._CFWQueryCommonStringParser.CFWQueryStringParserType;
import com.xresch.cfw.features.query.commands.CFWQueryCommandFormatField;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.utils.CFWHttp.CFWHttpAuthMethod;
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
	private static final String PARAM_AUTH 		= "auth";
	private static final String PARAM_USERNAME 	= "username";
	private static final String PARAM_PASSWORD 	= "password";
	private static final String PARAM_AS	= "as";
	
	private static final String PARAM_TIMEFIELD = "timefield";
	private static final String PARAM_TIMEFORMAT = "timeformat";
	
	private QueryPartValue lastHTTPResponse = null;
	

	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceWeb(CFWQuery parent) {
		super(parent);
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
		return "Use earliest() and latest() functions to add time filtering to your HTTP-requests. For type=json: You can use the parameters timefield and timeformat to specify the time filtering.(Default: no filtering by time)";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionHTML() {
		
		//------------------------------------
		// Create As-Option List
		StringBuilder asOptionList = new StringBuilder("<ul>");
		
		for(String type : CFWQueryStringParserType.getNames()){
			CFWQueryStringParserType current = CFWQueryStringParserType.valueOf(type);
			asOptionList.append("<li><b>"+type+":&nbsp;</b>"+current.shortDescription()+"</li>");
		}
		asOptionList.append("</ul>");
		
		
		//------------------------------------
		// Fetch resource and replace
		return CFW.Files.readPackageResource(
					FeatureWebExtensions.PACKAGE_RESOURCES
					, "source_web.html"
				).replace("{asOptionPlaceholder}", asOptionList)
				;
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
						CFWField.newString(FormFieldType.TEXT, PARAM_AUTH)
								.setDescription("(Optional)The authentication method used, either of 'basic' | 'digest' | 'ntlm' (Default: basic)")
								.addValidator(new NotNullOrEmptyValidator())
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
						.setDescription("(Optional)Define how the response should be parsed, options: "
								 					+CFW.JSON.toJSON( CFWQueryStringParserType.getNames()))
						.addValidator(new NotNullOrEmptyValidator())
						.disableSanitization()
						)
				
				.addField(
						CFWField.newString(FormFieldType.TEXT, PARAM_TIMEFIELD)
							.setDescription("(Optional)The field of the response that contains the time when using as=json.")	
					)
				
				.addField(
						CFWField.newString(FormFieldType.TEXT, PARAM_TIMEFORMAT)
							.setDescription("(Optional)The format of the time in the time field when using as=json. (Default: 'epoch').")	
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
	protected QueryPartValue getLastHTTPResponse() {
		return lastHTTPResponse;
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
		
		if( !CFWQueryStringParserType.has(parseAs) ){
			this.getParent().getContext().addMessageError("source web: value as='"+parseAs+"' is not supported."
														 +" Available options: "
														 +CFW.JSON.toJSON( CFWQueryStringParserType.getNames()) );
			return;
		}
		
		CFWQueryStringParserType type = CFWQueryStringParserType.valueOf(parseAs);
		

		
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
		
		String auth = (String) parameters.getField(PARAM_AUTH).getValue();
		CFWHttpAuthMethod authMethod = null;
		if(auth != null) {
			authMethod = CFWHttpAuthMethod.valueOf(auth.trim().toUpperCase());
		}
		
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
			requestBuilder.setAuthCredentials(authMethod,username, password);
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
		// Set Last Response
		ArrayList<EnhancedJsonObject> lastResponseArray = _CFWQueryCommonStringParser.parseAsHTTP(response);
		JsonObject lastResponseObject = lastResponseArray.get(0).getWrappedObject();
		lastHTTPResponse = QueryPartValue.newJson(lastResponseObject);

		//------------------------------------
		// Parse Data
		try {
			ArrayList<EnhancedJsonObject> result = _CFWQueryCommonStringParser.parse(type, response);
			
			//------------------------------------
			// Json Timeframe Checker
			String timefield = (String)parameters.getField(PARAM_TIMEFIELD).getValue();
			String timeformat = (String)parameters.getField(PARAM_TIMEFORMAT).getValue();	
			JsonTimerangeChecker timerangeChecker = 
					new JsonTimerangeChecker(timefield, timeformat, earliestMillis, latestMillis);

			//------------------------------------
			// Filter by Time Range
			if(result != null && !result.isEmpty()) {

				for(EnhancedJsonObject current : result) {

					if(timerangeChecker.isInTimerange(current.getWrappedObject(), false)) {
						outQueue.add(current);
					}
				}
			}
			
		}catch(Exception e) {
			_CFWQueryCommon.createHTTPResponseExceptionResult(this.parent.getContext(), outQueue, response, e);
			return;
		}
			
	}


}
