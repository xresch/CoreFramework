package com.xresch.cfw.extensions.cli;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.cli.CFWCLIExecutor;
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
	private static final String PARAM_TIMEOUT 	= "timeout";
	
	private static final String PARAM_TIMEFIELD = "timefield";
	private static final String PARAM_TIMEFORMAT = "timeformat";
	
	
	private QueryPartValue listFormatter = null;

	
	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceCLI(CFWQuery parent) {
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
						CFWField.newString(FormFieldType.TEXT, PARAM_DIR)
							.setDescription("The working directory where the commands should be executed.")
							.addValidator(new NotNullOrEmptyValidator())
							.disableSanitization()
					)

				.addField(
						CFWField.newString(FormFieldType.TEXTAREA, PARAM_COMMANDS)
								.setDescription("(Optional)The body contents of the request. Setting the header 'Content-Type' might be needed(e.g. 'application/json; charset=UTF-8').")
								.disableSanitization()
						)
				
				.addField(
						CFWField.newInteger(FormFieldType.TEXTAREA, PARAM_TIMEOUT)
						.setDescription("(Optional)The timeout in seconds (default: 120).")
						.setValue(124)
						)
				
							
				.addField(
						CFWField.newString(FormFieldType.TEXT, PARAM_AS)
						.setDescription("(Optional)Define how the response should be parsed, default is 'lines'. Options: "
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
		
		//----------------------------------------
		// Execute Command
		ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
		
		CFWCLIExecutor executor = new CFWCLIExecutor(dir, commands, resultStream); 
		
		//will wait until done
		executor.execute();
		executor.waitForCompletionOrTimeout(timeout);
		
		//----------------------------------------
		// Get Data
		String dataString = new String(resultStream.toByteArray());

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
