package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
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

	private ArrayList<String> groupByFieldnames = new ArrayList<>();
	
	private QueryPartValue percentColumnsFormatter = QueryPartValue.newString("percent");
	private String labelOld = "_A";
	private String labelYoung = "_B";
	private String labelDiff = "_Diff";
	private String labelDiffPercent = "_%";
	private boolean compareAbsolutes = true;
	private boolean comparePercent = true;
	private boolean compareStrings = true;
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
		return COMMAND_NAME+" [results=resultnamesArray]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>resultnamesArray:&nbsp;</b>(Optional) Names of the results that should be compared.</p>";
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
				if		 (assignmentName.equals("by")) {				groupByFieldnames.addAll( assignmentValue.getAsStringArray() ); }
				else if	 (assignmentName.equals("labelold")) {			labelOld =  assignmentValue.getAsString(); }
				else if	 (assignmentName.equals("labelyoung")) {		labelYoung =  assignmentValue.getAsString(); }
				else if	 (assignmentName.equals("labeldiff")) {			labelDiff =  assignmentValue.getAsString(); }
				else if	 (assignmentName.equals("labeldiffpercent")) {	labelDiffPercent =  assignmentValue.getAsString(); }
				else if	 (assignmentName.startsWith("percentformat")) { percentColumnsFormatter =  assignmentValue; }
				else if	 (assignmentName.startsWith("absolute")) { 			compareAbsolutes =  assignmentValue.getAsBoolean(); }
				else if	 (assignmentName.startsWith("percent")) { 			comparePercent =  assignmentValue.getAsBoolean(); }
				else if	 (assignmentName.startsWith("strings")) { 			compareStrings =  assignmentValue.getAsBoolean(); }
				else if	 (assignmentName.startsWith("booleans")) { 			compareBooleans =  assignmentValue.getAsBoolean(); }
				else {
					throw new ParseException(COMMAND_NAME+": Unsupported argument.", -1);
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
		while(keepPolling()) {
			outQueue.add(inQueue.poll());
		}
		
		//------------------------------
		// Read Records of current Query
		if(isPreviousDone() && inQueue.isEmpty()) {
			
			CFWQueryResultList previousResults = this.parent.getContext().getResultList();
			
			CFWQueryResult last = previousResults.get(previousResults.size()-1);
			CFWQueryResult secondLast = previousResults.get(previousResults.size()-2);
			
			previousResults.removeResult(last);
			previousResults.removeResult(secondLast);
			
			CFWQueryResult compared = 
					new CFWQueryCommandResultCompareMethods()
						.identifierFields(groupByFieldnames)
						.labelOld(labelOld)
						.labelYoung(labelYoung)
						.labelDiff(labelDiff)
						.labelDiffPercent(labelDiffPercent)
						.doCompareNumbersAbsolute(compareAbsolutes)
						.doCompareNumbersDiffPercent(comparePercent)
						.doCompareStrings(compareStrings)
						.doCompareBooleans(compareBooleans)
						.compareQueryResults(secondLast, last);
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
