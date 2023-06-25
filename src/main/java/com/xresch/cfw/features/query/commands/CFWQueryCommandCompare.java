package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQueryResult;
import com.xresch.cfw.features.query.CFWQueryResultList;
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineActionContext;

public class CFWQueryCommandCompare extends CFWQueryCommand {
	
	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandCompare.class.getName());
	
	CFWQuerySource source = null;
	ArrayList<String> resultnames = new ArrayList<>();
		
	HashSet<String> encounters = new HashSet<>();
	
	ArrayList<String> groupByFieldnames = new ArrayList<>();
	ArrayList<String> detectedFieldnames = new ArrayList<>();
	
	QueryPartValue percentColumnsFormatter = QueryPartValue.newString("percent");
	String labelOld = "_A";
	String labelYoung = "_B";
	String labelDiff = "_Diff";
	String labelDiffPercent = "_%";
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandCompare(CFWQuery parent) {
		super(parent);
	}

	/***********************************************************************************************
	 * Return the command name and aliases.
	 * The first entry in the array will be used as the main name, under which the documentation can
	 * be found in the manual. All other will be used as aliases.
	 ***********************************************************************************************/
	@Override
	public String[] uniqueNameAndAliases() {
		return new String[] {"compare"};
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
		return "compare [results=resultnamesArray]";
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
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".commands", "command_compare.html");
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
				//--------------------------------------------------
				// Resolve Fieldname=Function
				QueryPartAssignment assignment = (QueryPartAssignment)currentPart;
				String assignmentName = assignment.getLeftSideAsString(null);
				QueryPartValue assignmentValue = ((QueryPartAssignment) currentPart).determineValue(null);
				
				if(assignmentName != null) {
					assignmentName = assignmentName.trim().toLowerCase();
					if		 (assignmentName.equals("by")) {				groupByFieldnames.addAll( assignmentValue.getAsStringArray() ); }
					else if	 (assignmentName.equals("labelold")) {			labelOld =  assignmentValue.getAsString(); }
					else if	 (assignmentName.equals("labelyoung")) {		labelYoung =  assignmentValue.getAsString(); }
					else if	 (assignmentName.equals("labeldiff")) {			labelDiff =  assignmentValue.getAsString(); }
					else if	 (assignmentName.equals("labeldiffpercent")) {	labelDiffPercent =  assignmentValue.getAsString(); }
					else if	 (assignmentName.startsWith("percentformat")) { percentColumnsFormatter =  assignmentValue; }
					else {
						parser.throwParseException("compare: Unsupported argument.", currentPart);
					}
					
				}
				
			}else {
				parser.throwParseException("compare: Only assignment expressions(key=value) allowed.", currentPart);
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
	public void initializeAction() {
		// nothing todo
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
					new CFWQueryCommandCompareMethods()
						.identifierFields(groupByFieldnames)
						.labelOld(labelOld)
						.labelYoung(labelYoung)
						.labelDiff(labelDiff)
						.labelDiffPercent(labelDiffPercent)
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
