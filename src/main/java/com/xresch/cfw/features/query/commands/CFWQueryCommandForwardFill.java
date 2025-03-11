package com.xresch.cfw.features.query.commands;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
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
import com.xresch.cfw.features.query.parse.QueryPartArray;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineActionContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandForwardFill extends CFWQueryCommand {
	
	private static final String COMMAND_NAME = "forwardfill";

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandForwardFill.class.getName());

	
	private ArrayList<String> groupByFieldnames = new ArrayList<>();
	private LinkedHashMap<String, ArrayList<EnhancedJsonObject>> groupedRecords = new LinkedHashMap<>();
	
	private ArrayList<QueryPart> parts;
	
	private ArrayList<String> fieldnames = new ArrayList<>();
		
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandForwardFill(CFWQuery parent) {
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
	public TreeSet<String> getTags(){
		TreeSet<String> tags = new TreeSet<>();
		tags.add(_CFWQueryCommon.TAG_ANALYTICS);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Uses forward fill approach to replace null values with the last observed value.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" <fieldname> [, <fieldname>, <fieldname>...]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>fieldname:&nbsp;</b> Names of the fields that should be forward filled.</p>";
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
		// Get Fieldnames
		
		if(parts.size() == 0) {
			throw new ParseException(COMMAND_NAME+": please specify at least one fieldname.", -1);
		}
		
		this.parts = parts;
			
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
		
		for(QueryPart part : parts) {
			
			if(part instanceof QueryPartAssignment) {
				
				QueryPartAssignment assignment = (QueryPartAssignment)part;
				String assignmentName = assignment.getLeftSideAsString(null);
				QueryPartValue assignmentValue = assignment.determineValue(null);
				
				if(assignmentName != null) {
					assignmentName = assignmentName.trim().toLowerCase();

					if(assignmentName.equals("by")) {
						ArrayList<String> fieldnames = assignmentValue.getAsStringArray();
						groupByFieldnames.addAll(fieldnames);
					}else {
						throw new ParseException(COMMAND_NAME+": Unsupported parameter '"+assignmentName+"'", -1);
					}
					
				}
				
			}else if(part instanceof QueryPartArray) {
				QueryPartArray array = (QueryPartArray)part;

				for(JsonElement element : array.getAsJsonArray(null, true)) {
					
					if(!element.isJsonNull() && element.isJsonPrimitive()) {
						fieldnames.add(element.getAsString());
					}
				}
				
			}else {
				QueryPartValue value = part.determineValue(null);
				if(value.isJsonArray()) {
					fieldnames.addAll(value.getAsStringArray());
				}else if(!value.isNull()) {
					fieldnames.add(value.getAsString());
				}
			}
		}
		
		for(String fieldname : fieldnames) {
			this.fieldnameRemove(fieldname);
		}
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		//------------------------------------
		// Group All Records
		while(keepPolling()) {
			EnhancedJsonObject record = inQueue.poll();

			//----------------------------
			// Create Group String
			String groupID = record.createGroupIDString(groupByFieldnames);
			
			//----------------------------
			// Create and Get Group
			if(!groupedRecords.containsKey(groupID)) {
				groupedRecords.put(groupID, new ArrayList<>());
			}
			
			ArrayList<EnhancedJsonObject> group = groupedRecords.get(groupID);
			group.add(record);
			
		}
		
		//------------------------------------
		// Calculate Values
		if(this.isPreviousDone()) {
			
			//------------------------------------
			// Iterate the Groups
			for(List<EnhancedJsonObject> group : groupedRecords.values()) {
				
				//------------------------------------
				// Iterate
				outQueue.add(group.get(0));
				for(int i = 1 ; i < group.size(); i++) {
					EnhancedJsonObject previousRecord = group.get(i-1);
					EnhancedJsonObject record = group.get(i);
						
					for(String field : fieldnames) {
						if( CFW.JSON.isNull( record.get(field) ) ) {
							record.add(field, previousRecord.get(field) );
						}
					}
					
					outQueue.add(record);
					
					previousRecord = record;
				}
					
			}
			
			this.setDoneIfPreviousDone();
		}
	}

}
