package com.xresch.cfw.features.query.sources;

import java.io.InputStream;
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
import com.xresch.cfw.db.CFWResultSet;
import com.xresch.cfw.extensions.cli.FeatureCLIExtensions;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.filemanager.CFWStoredFile;
import com.xresch.cfw.features.filemanager.CFWStoredFile.CFWStoredFileFields;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
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
public class CFWQuerySourceFile extends CFWQuerySource {

	private static final String SOURCE_NAME = "file";

	private static Logger logger = CFWLog.getLogger(CFWQuerySourceFile.class.getName());
	
	private static final String PARAM_AS		= "as";
	private static final String PARAM_FILE 		= SOURCE_NAME;
	private static final String PARAM_SHEET 	= "sheet";
	
	private static final String PARAM_ENV		= "env";
	private static final String PARAM_HEAD		= "head";
	private static final String PARAM_TAIL		= "tail";
	private static final String PARAM_COUNT_SKIPPED	= "countSkipped";
	
	private static final String PARAM_TIMEFIELD = "timefield";
	private static final String PARAM_TIMEFORMAT = "timeformat";
	
	private static final String PARAM_CSVSEPARATOR = "csvSeparator";
	

	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceFile(CFWQuery parent) {
		super(parent);
	}

	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String uniqueName() {
		return SOURCE_NAME;
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
		// Fetch resource and replace
		String asOptionList = CFWQueryStringParserType.getDescriptionHTMLList();
		
		return CFW.Files.readPackageResource(
					  FeatureQuery.PACKAGE_MANUAL+".sources"
					, "source_"+SOURCE_NAME+".html"
				).replace("{asOptionPlaceholder}", asOptionList)
				;
	}
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionRequiredPermission() {
		return FeatureCLIExtensions.PERMISSION_CLI_EXTENSIONS;
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
		CFW.DB.StoredFile.autocompleteFileForQuery(result, helper);
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public CFWObject getParameters() {
		return new CFWObject()
				
				.addField(
						CFWField.newString(FormFieldType.TEXT, PARAM_AS)
						.setDescription("(Optional)Define how the response should be parsed, default is 'auto'. Options: "
								 					+CFW.JSON.toJSON( CFWQueryStringParserType.getNames()))
						.addValidator(new NotNullOrEmptyValidator())
						.disableSanitization()
						)
				
				.addField(
						CFWField.newString(FormFieldType.TEXT, PARAM_FILE)
							.setDescription("The file stored in the file manager that should be used as a data source. (use Ctrl + Space to select a file)")
							.addValidator(new NotNullOrEmptyValidator())
							.disableSanitization()
					)

				.addField(
						CFWField.newString(FormFieldType.TEXTAREA, PARAM_SHEET)
								.setDescription("(Optional)The name of the sheet that should be read from an excel file(Default: First sheet).")
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
							.setDescription("(Optional)Number of lines that should be read from the tail(end) of the output.")
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
						CFWField.newString(FormFieldType.TEXT, PARAM_TIMEFIELD)
							.setDescription("(Optional)The column of the result that contains the time.")	
					)
				
				.addField(
						CFWField.newString(FormFieldType.TEXT, PARAM_TIMEFORMAT)
							.setDescription("(Optional)The format of the time in the time column. (Default: 'epoch').")	
							.setValue("epoch")
					)
				
				.addField(
						CFWField.newString(FormFieldType.TEXT, PARAM_CSVSEPARATOR)
						.setDescription("(Optional)The separator used in case the response is parsed as CSV. (Default: ',').")	
						.setValue(",")
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
		// Get File ID
		String fileString = (String) parameters.getField(PARAM_FILE).getValue();
		
		Integer fileID = null; 
		
		if(Strings.isNullOrEmpty(fileString)) {
			CFW.Messages.addWarningMessage("source "+SOURCE_NAME+": Please specify the parameter 'file' (use Ctrl+Space for list of suggestions).");
			return;
		}
		
		if(fileString.startsWith("{")) {
			JsonObject settingsObject = CFW.JSON.fromJson(fileString).getAsJsonObject();
			
			if(settingsObject.get("id") != null) {
				fileID = settingsObject.get("id").getAsInt();
				 
			}
		}else {
			QueryPartValue value = QueryPartValue.newString(fileString);
			
			if(value.isNumberOrNumberString()) {
				fileID = value.getAsInteger();
			}
		}
		
		if(fileID == null) {
			CFW.Messages.addWarningMessage("source "+SOURCE_NAME+": The file ID could not be retrieved, something seems wrong with the file-parameter.");
			return;
		}
		
		//------------------------------------
		// Get File
		
		CFWStoredFile file = CFW.DB.StoredFile.selectByID(fileID);
		
		if(file == null) {
			CFW.Messages.addWarningMessage("source "+SOURCE_NAME+": The file with ID "+fileID+" could not be found.");
			return;
		}
		
		//------------------------------------
		// Get As
		String parseAs = (String) parameters.getField(PARAM_AS).getValue();	
		if(Strings.isNullOrEmpty(parseAs)) { parseAs = "auto"; };
		
		parseAs = parseAs.trim().toLowerCase();
		
		if(parseAs.equals("auto")) {
			
			String extension = file.extension();
			
			if(Strings.isNullOrEmpty(extension)) {		parseAs = CFWQueryStringParserType.lines.toString(); }
			else if(extension.startsWith("json")) {		parseAs = CFWQueryStringParserType.json.toString(); }
			else if(extension.startsWith("csv")) {		parseAs = CFWQueryStringParserType.csv.toString(); }
			else if(extension.startsWith("xml")) {		parseAs = CFWQueryStringParserType.xml.toString(); }
			else if(extension.startsWith("html")) {		parseAs = CFWQueryStringParserType.html.toString(); }
			else if(extension.startsWith("xls")) {		parseAs = "excel"; }
			else { /*parse as lines by default*/		parseAs = CFWQueryStringParserType.lines.toString(); }
			
		}
		
		if( ! parseAs.equals("excel") 
		&&  ! CFWQueryStringParserType.has(parseAs) ){
			this.getParent()
				.getContext()
				.addMessageError("source "+SOURCE_NAME+": value as='"+parseAs+"' is not supported."
								 +" Available options: "
								 +CFW.JSON.toJSON( CFWQueryStringParserType.getNames()) );
			return;
		}
		
		
		//------------------------------------
		// Get Sheet
		String sheet = (String) parameters.getField(PARAM_SHEET).getValue();
		
		
		//------------------------------------
		// Get Separator
		String csvSeparator = (String) parameters.getField(PARAM_CSVSEPARATOR).getValue();
		
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
		// Get Data
		ArrayList<EnhancedJsonObject> result;
		if(parseAs.equals("excel")) {
			//----------------------------
			// Excel
			CFWResultSet cfwResult =CFW.DB.StoredFile.retrieveDataStreamObject(file);
			InputStream dataSream = cfwResult.getBytesStream(CFWStoredFileFields.DATA.toString());
			JsonArray array = CFW.Excel.readExcelSheetAsJsonArray(dataSream, sheet);
			cfwResult.close();
			
			result = new ArrayList<>();
			for(JsonElement element : array) {
				if(element != null && element.isJsonObject()) {
					result.add(new EnhancedJsonObject( element.getAsJsonObject() ) ); 
				}
			}
			
		}else {
			//----------------------------
			// Other Types
			CFWQueryStringParserType type = CFWQueryStringParserType.valueOf(parseAs);
			String dataString = CFW.DB.StoredFile.retrieveDataAsString(file);
			result = _CFWQueryCommonStringParser.parse(type, dataString, csvSeparator);
			
		}
		
		//------------------------------------
		// Parse Data
		try {

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
