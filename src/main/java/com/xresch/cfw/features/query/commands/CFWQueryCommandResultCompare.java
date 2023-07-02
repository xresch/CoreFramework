package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryResult;
import com.xresch.cfw.features.query.CFWQueryResultList;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineActionContext;

public class CFWQueryCommandResultCompare extends CFWQueryCommand {
	
	public static final String COMMAND_NAME = "resultcompare";

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandResultCompare.class.getName());
	
	private ArrayList<QueryPartAssignment> assignmentParts = new ArrayList<QueryPartAssignment>();

	private ArrayList<String> resultNames = new ArrayList<>();
	private ArrayList<String> groupByFieldnames = new ArrayList<>();
	
	private QueryPartValue percentColumnsFormatter = QueryPartValue.newString("percent");
	private String labelOld = "_A";
	private String labelYoung = "_B";
	private String labelDiff = "_Diff";
	private String labelDiffPercent = "_%";
	private boolean compareAbsolutes = true;
	private boolean comparePercent = true;
	private boolean compareStrings = true;
	private boolean compareArrays = true;
	private boolean compareObjects = true;
	private boolean compareBooleans = true;
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandResultCompare(CFWQuery parent) {
		super(parent);
	}

	/***********************************************************************************************
	 * Return the command name and aliases.
	 * The first entry in the array will be used as the main name, under which the documentation can
	 * be found in the manual. All other will be used as aliases.
	 ***********************************************************************************************/
	@Override
	public String[] uniqueNameAndAliases() {
		return new String[] {COMMAND_NAME};
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Compares completed results of previous queries.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" by=<identifierFields> results=<resultnamesArray> [otherParam=<value> ...]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 
			  "<ul>"
			+ "<li><b>byFieldnames:&nbsp;</b>(Optional) Names of the fields which are used to identify a record. Records with same identifier will be compared with each other. (Default: First fieldname of first record)</li>"
			+ "<li><b>resultnamesArray:&nbsp;</b>(Optional) Names of the results that should be compared. (Default: 2 preceeding query results, or if current query has records use those plus preceeding result)</li>"
			+ "<li><b>labelOld:&nbsp;</b>(Optional) A string used as a postfix to label columns of the older/first result in the comparison.(Default: '"+labelOld+"')</li>"
			+ "<li><b>labelYoung:&nbsp;</b>(Optional) A string used as a postfix to label columns of the younger/second result in the comparison.(Default: '"+labelYoung+"')</li>"
			+ "<li><b>labelDiff:&nbsp;</b>(Optional) A string used as a postfix to label columns containing the result of comparison.(Default: '"+labelDiff+"')</li>"
			+ "<li><b>labelDiffPercent:&nbsp;</b>(Optional) A string used as a postfix to label columns containing the result of percentage comparison.(Default: '"+labelDiffPercent+"')</li>"
			+ "<li><b>percentFormat:&nbsp;</b>(Optional) A name of a formatter or an array of formatters, see command 'formatfield' to find a list of formatters.(Default: 'percent')</li>"
			+ "<li><b>absolute:&nbsp;</b>(Optional) Toogle if comparison should include absolute difference for number values.(Default: '"+compareAbsolutes+"')</li>"
			+ "<li><b>percent:&nbsp;</b>(Optional) Toogle if comparison should include percentage difference for number values. This will be added in a separate field.(Default: '"+comparePercent+"')</li>"
			+ "<li><b>booleans:&nbsp;</b>(Optional) Toogle if booleans should be compared.(Default: '"+compareBooleans+"')</li>"
			+ "<li><b>strings:&nbsp;</b>(Optional) Toogle if strings should be compared.(Default: '"+compareStrings+"')</li>"
			+ "<li><b>arrays:&nbsp;</b>(Optional) Toogle if arrays should be compared.(Default: '"+compareArrays+"')</li>"
			+ "<li><b>objects:&nbsp;</b>(Optional) Toogle if objects should be compared.(Default: '"+compareObjects+"')</li>"
			+ "</ul>"
			;
	}
	

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".commands", "command_"+COMMAND_NAME+".html");
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void setAndValidateQueryParts(CFWQueryParser parser, ArrayList<QueryPart> parts) throws ParseException {
		//------------------------------------------
		// Get Parameters
		for(int i = 0; i < parts.size(); i++) {
			
			QueryPart currentPart = parts.get(i);
			
			if(currentPart instanceof QueryPartAssignment) {
				assignmentParts.add((QueryPartAssignment)currentPart);

			}else {
				parser.throwParseException(COMMAND_NAME+": Only parameters(key=value) are allowed.", currentPart);
			}
		}
				
			
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		// keep default
	}

	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void initializeAction() throws Exception {
		//------------------------------------------
		// Get Parameters
		for(QueryPartAssignment assignment : assignmentParts) {
			//--------------------------------------------------
			// Resolve Fieldname=Function
			String assignmentName = assignment.getLeftSideAsString(null);
			QueryPartValue assignmentValue = assignment.determineValue(null);
			
			if(assignmentName != null) {
				assignmentName = assignmentName.trim().toLowerCase();
				if		 (assignmentName.equals("results")) {			resultNames.addAll( assignmentValue.getAsStringArray() ); }
				else if	 (assignmentName.equals("by")) {				groupByFieldnames.addAll( assignmentValue.getAsStringArray() ); }
				else if	 (assignmentName.equals("labelold")) {			labelOld =  assignmentValue.getAsString(); }
				else if	 (assignmentName.equals("labelyoung")) {		labelYoung =  assignmentValue.getAsString(); }
				else if	 (assignmentName.equals("labeldiff")) {			labelDiff =  assignmentValue.getAsString(); }
				else if	 (assignmentName.equals("labeldiffpercent")) {	labelDiffPercent =  assignmentValue.getAsString(); }
				else if	 (assignmentName.startsWith("percentformat")) { percentColumnsFormatter =  assignmentValue; }
				else if	 (assignmentName.startsWith("absolute")) { 		compareAbsolutes =  assignmentValue.getAsBoolean(); }
				else if	 (assignmentName.startsWith("percent")) { 		comparePercent =  assignmentValue.getAsBoolean(); }
				else if	 (assignmentName.startsWith("strings")) { 		compareStrings =  assignmentValue.getAsBoolean(); }
				else if	 (assignmentName.startsWith("booleans")) { 		compareBooleans =  assignmentValue.getAsBoolean(); }
				else if	 (assignmentName.startsWith("arrays")) { 		compareArrays =  assignmentValue.getAsBoolean(); }
				else if	 (assignmentName.startsWith("objects")) { 		compareObjects =  assignmentValue.getAsBoolean(); }
				else {
					throw new ParseException(COMMAND_NAME+": Unsupported parameter: "+assignmentName, -1);
				}
			}
		}
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		//------------------------------
		// Read Records of current Query
//		while(keepPolling()) {
//			outQueue.add(inQueue.poll());
//		}
		
		//------------------------------
		// Read Records of current Query
		if(isPreviousDone()) {
			
			//------------------------------
			// Find Results to Compare
			CFWQueryContext queryContext = this.parent.getContext();
			CFWQueryResultList previousResults = queryContext.getResultList();
			
			CFWQueryResult youngerResult = null;
			CFWQueryResult olderResult = null;
			if(resultNames.isEmpty() 
			&& inQueue.isEmpty()
			&& previousResults.size() >= 2) {
				olderResult = previousResults.get(previousResults.size()-2);
				youngerResult = previousResults.get(previousResults.size()-1);
			}else if(resultNames.size() >= 2
				  && previousResults.size() >= 2) {
				olderResult = queryContext.getResultByName(resultNames.get(0));
				youngerResult = queryContext.getResultByName(resultNames.get(1));
			}else if(!inQueue.isEmpty()
				&& previousResults.size() >= 1) {
				
				if(resultNames.size() >= 1) {	
					olderResult = queryContext.getResultByName(resultNames.get(0));
				}else {
					olderResult = previousResults.get(previousResults.size()-1);
				}
				
				JsonArray queueResultArray = new JsonArray();
				for (EnhancedJsonObject object : inQueue) {
					queueResultArray.add(object.getWrappedObject());
				}
				
				youngerResult = new CFWQueryResult(queryContext);
				youngerResult.setResults(queueResultArray);
			}
			
			//------------------------------
			// Check if any is null
			if(youngerResult == null || olderResult == null) {
				throw new IllegalArgumentException(COMMAND_NAME+": Please provide at least 2 results to compare.");
			}
			
			//------------------------------
			// Remove from ResultList
			previousResults.removeResult(olderResult);
			previousResults.removeResult(youngerResult);
			
			//------------------------------
			// Compare
			CFWQueryResult compared = 
					new CFWQueryCommandResultCompareMethods()
						.identifierFields(groupByFieldnames)
						.labelOld(labelOld)
						.labelYoung(labelYoung)
						.labelDiff(labelDiff)
						.labelDiffPercent(labelDiffPercent)
						.doCompareNumbersAbsolute(compareAbsolutes)
						.doCompareNumbersDiffPercent(comparePercent)
						.doCompareBooleans(compareBooleans)
						.doCompareStrings(compareStrings)
						.doCompareArrays(compareArrays)
						.doCompareObjects(compareObjects)
						.compareQueryResults(olderResult, youngerResult);
						;
			
			//----------------------------
			// Set Detected Fields
			this.fieldnameClearAll();
			this.fieldnameAddAll(compared.getDetectedFields());
			
			//----------------------------
			// Set Field Formats
			for(JsonElement element : compared.getDetectedFields()) {
				String fieldname = element.getAsString();
				if(fieldname.endsWith(labelDiffPercent)) {
					CFWQueryCommandFormatField.addFormatter(this.parent.getContext(), fieldname, percentColumnsFormatter);
				}
			}
			
			//----------------------------
			// Add to Queue
			for(JsonElement record : compared.getResults()) {
				outQueue.add(new EnhancedJsonObject(record.getAsJsonObject()));
			}
			
			this.setDone();
		}
		
	}

}
