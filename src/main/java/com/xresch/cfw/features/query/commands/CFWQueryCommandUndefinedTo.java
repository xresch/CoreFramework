package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query._CFWQueryCommon;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineActionContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandUndefinedTo extends CFWQueryCommand {
	
	public static final String COMMAND_NAME = "undefinedto";

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandUndefinedTo.class.getName());
	
	private ArrayList<QueryPartAssignment> assignmentParts = new ArrayList<QueryPartAssignment>();

	private String replacement = " ";
	private ArrayList<String> fieldnames = new ArrayList<>();
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandUndefinedTo(CFWQuery parent) {
		super(parent);
	}

	/***********************************************************************************************
	 * Return the command name and aliases.
	 * The first entry in the array will be used as the main name, under which the documentation can
	 * be found in the manual. All other will be used as aliases.
	 ***********************************************************************************************/
	@Override
	public String[] uniqueNameAndAliases() {
		return new String[] {COMMAND_NAME, "undefto"};
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public TreeSet<String> getTags(){
		TreeSet<String> tags = new TreeSet<>();
		tags.add(_CFWQueryCommon.TAG_GENERAL);
		tags.add(_CFWQueryCommon.TAG_CODING);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Handles undefined values(missing fields) by specifying the value.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" value=<value> fields=<fieldnames>";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 
			 "<ul>"
			+"<li><b>value:&nbsp;</b> The value that should be used instead of undefined.</li>"
			+"<li><b>fieldnames:&nbsp;</b> Names of the fields that should be affected by the command. Affects all if not specified.</li>"
			+"</ul>"
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
				if		 (assignmentName.equals("value")) {		replacement = assignmentValue.getAsString(); }
				else if	 (assignmentName.equals("fields")) {	fieldnames =  assignmentValue.getAsStringArray(); }
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
		// Wait until previous is done
		// to have detect all possible fieldnames
		if(fieldnames.isEmpty() && !this.isPreviousDone()) {
			return;
		}
		
		//------------------------------
		// Do Da Thing
		while(keepPolling()) {
			
			EnhancedJsonObject record = inQueue.poll();
				
			if(fieldnames.isEmpty()) {
				//-----------------------
				// Check all fields
				LinkedHashSet<String> detectedNames = this.getQueryContext().getFinalFieldnames();
				for(String fieldname : detectedNames) {
					if(!record.has(fieldname)) {
						record.addProperty(fieldname, replacement);
					}
				}
			}else {
				//---------------------------
				// Replace in selected fields
				for(String fieldname : fieldnames) {
					if(!record.has(fieldname)) {
						record.addProperty(fieldname, replacement);
					}
				}
			}
			
			outQueue.add(record);
			
		}
		
		this.setDoneIfPreviousDone();
		
	}

}
