package com.xresch.cfw.extensions.cli;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

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
import com.xresch.cfw.features.query._CFWQueryCommon;
import com.xresch.cfw.features.query._CFWQueryCommonStringParser;
import com.xresch.cfw.features.query._CFWQueryCommonStringParser.CFWQueryStringParserType;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.utils.CFWReadableOutputStream;
import com.xresch.cfw.utils.json.JsonTimerangeChecker;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;
	
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourceCLI extends CFWQuerySource {

	private static Logger logger = CFWLog.getLogger(WidgetCLIResults.class.getName());
	
	private static final String PARAM_AS		= "as";
	private static final String PARAM_DIR 		= "dir";
	private static final String PARAM_COMMANDS 	= "commands";
	private static final String PARAM_ENV		= "env";
	private static final String PARAM_HEAD		= "head";
	private static final String PARAM_TAIL		= "tail";
	private static final String PARAM_COUNT_SKIPPED	= "countSkipped";
	private static final String PARAM_TIMEOUT 	= "timeout";
	
	private static final String PARAM_TIMEFIELD = "timefield";
	private static final String PARAM_TIMEFORMAT = "timeformat";
	

	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceCLI(CFWQuery parent) {
		super(parent);
	}

	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String uniqueName() {
		return "cli";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Takes command line inputs and executes them on the command line of the server.";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionTime() {
		return "Use earliest() and latest() functions to add time filtering to your command line. For type=json: You can use the parameters timefield and timeformat to specify the time filtering.(Default: no filtering by time)";
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
					FeatureCLIExtensions.PACKAGE_RESOURCES
					, "source_cli.html"
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
		return CFW.Context.Request.hasPermission(FeatureCLIExtensions.PERMISSION_CLI_EXTENSIONS);
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
						CFWField.newString(FormFieldType.TEXT, PARAM_AS)
						.setDescription("(Optional)Define how the response should be parsed, default is 'lines'. Options: "
								 					+CFW.JSON.toJSON( CFWQueryStringParserType.getNames()))
						.addValidator(new NotNullOrEmptyValidator())
						.disableSanitization()
						)
				
				.addField(
						CFWField.newString(FormFieldType.TEXT, PARAM_DIR)
							.setDescription("(Optional)The working directory where the commands should be executed. (Default: \""+getDefaultFolderDescription()+"\")")
							.addValidator(new NotNullOrEmptyValidator())
							.disableSanitization()
					)

				.addField(
						CFWField.newString(FormFieldType.TEXTAREA, PARAM_COMMANDS)
								.setDescription("The body contents of the request. Setting the header 'Content-Type' might be needed(e.g. 'application/json; charset=UTF-8').")
								.disableSanitization()
						)
				
				.addField(
						CFWField.newString(FormFieldType.TEXT, PARAM_ENV)
							.setDescription("(Optional)Additional environment variables which should be passed to the command line.")
							.addValidator(new NotNullOrEmptyValidator())
							.disableSanitization()
					)
				
				.addField(
						CFWField.newInteger(FormFieldType.TEXT, PARAM_HEAD)
							.setDescription("(Optional)Number of lines that should be read from the head(start) of the output.")
							.disableSanitization()
							.setValue(0)
					)
				
				.addField(
						CFWField.newInteger(FormFieldType.TEXT, PARAM_TAIL)
							.setDescription("(Optional)Number of lines that should be read from the head(start) of the output.")
							.disableSanitization()
							.setValue(0)
					)
				
				.addField(
						CFWField.newBoolean(FormFieldType.TEXT, PARAM_COUNT_SKIPPED)
						.setDescription("(Optional)If parameter head or tail is set, this parameter decides if skipped line count should be added in the output.(Default:true)")
						.disableSanitization()
						.setValue(true)
						)
				
				.addField(
						CFWField.newInteger(FormFieldType.TEXTAREA, PARAM_TIMEOUT)
						.setDescription("(Optional)The timeout in seconds (default: 120).")
						.setValue(124)
						)
				
				.addField(
						CFWField.newString(FormFieldType.TEXT, PARAM_TIMEFIELD)
							.setDescription("(Optional)The column of the result that contains the time.")	
					)
				
				.addField(
						CFWField.newString(FormFieldType.TEXT, PARAM_TIMEFORMAT)
							.setDescription("(Optional)The format of the time in the time column. (Default: 'epoch').")	
							.setValue("epoch")
					)
			;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private String getDefaultFolderDescription() {
		
		String defaultDir = CFW.DB.Config.getConfigAsString(FeatureCLIExtensions.CONFIG_CATEGORY, FeatureCLIExtensions.CONFIG_DEFAULT_WORKDIR); 
		if(Strings.isNullOrEmpty(defaultDir)) {
			defaultDir = "App Root Directory";
		}
		
		return defaultDir;
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
		if(Strings.isNullOrEmpty(parseAs)) { parseAs = "lines"; };
		parseAs = parseAs.trim().toLowerCase();
		
		if( !CFWQueryStringParserType.has(parseAs) ){
			this.getParent().getContext().addMessageError("source cli: value as='"+parseAs+"' is not supported."
														 +" Available options: "
														 +CFW.JSON.toJSON( CFWQueryStringParserType.getNames()) );
			return;
		}
		
		CFWQueryStringParserType type = CFWQueryStringParserType.valueOf(parseAs);
		
		//------------------------------------
		// Get DIR
		String dir = (String) parameters.getField(PARAM_DIR).getValue();

		//------------------------------------
		// Get Commands
		String commands = (String) parameters.getField(PARAM_COMMANDS).getValue();
		
		//------------------------------------
		// Get timeout
		Integer timeout = (Integer) parameters.getField(PARAM_TIMEOUT).getValue();
		if(timeout == null) {
			timeout = 120;
		}
		
		//------------------------------------
		// Get Head/Tail/Skipped
		int head = (Integer) parameters.getField(PARAM_HEAD).getValue();
		int tail = (Integer) parameters.getField(PARAM_TAIL).getValue();
		boolean countSkipped = (Boolean) parameters.getField(PARAM_COUNT_SKIPPED).getValue();
		
		//------------------------------------
		// Get Environment Variables
		String envString = (String) parameters.getField(PARAM_ENV).getValue();
		
		HashMap<String, String> envMap = new HashMap<>();
		
		if(envString != null && envString.startsWith("{")) {
			JsonObject headersObject = CFW.JSON.fromJson(envString).getAsJsonObject();
			
			for(Entry<String, JsonElement> entry : headersObject.entrySet()) {
				envMap.put(entry.getKey(), entry.getValue().getAsString());
			}
		}
		
		//----------------------------------------
		// Execute Command
		CFWCLIExecutor executor = new CFWCLIExecutor(dir, commands, envMap); 
		
		//will wait until done
		executor.execute();
		executor.waitForCompletionOrTimeout(timeout);
		
		//----------------------------------------
		// Get Data
		String dataString = executor.getOutputStream().readHeadAndTail(head, tail, countSkipped);

		//------------------------------------
		// Parse Data
		try {
			ArrayList<EnhancedJsonObject> result = _CFWQueryCommonStringParser.parse(type, dataString);
			
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
			new CFWLog(logger).severe("source cli: Error while creating result: "+e, e);
			return;
		}
			
	}

}
