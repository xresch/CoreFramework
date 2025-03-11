package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeSet;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query._CFWQueryCommon;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartFunction;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.pipeline.PipelineActionContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandStats extends CFWQueryCommand {
	
	public static final String COMMAND_NAME = "stats";
	
	private ArrayList<QueryPart> parts;
	
	private ArrayList<String> groupByFieldnames = new ArrayList<>();
	private LinkedHashMap<String, QueryPartFunction> functionMap = new LinkedHashMap<>();
	
	// contains groupID and aggregationGroup
	private LinkedHashMap<String, AggregationGroup> groupMap = new LinkedHashMap<>();
	
	private ArrayList<String> detectedFieldnames = new ArrayList<>();
	private ArrayList<QueryPartAssignment> assignments = new ArrayList<>();

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandStats(CFWQuery parent) {
		super(parent);
	}

	/***********************************************************************************************
	 * 
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
		tags.add(_CFWQueryCommon.TAG_STATS);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Creates statistics based on field values.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" by=<arrayOfFieldnames> <targetFieldname>=function(params) [<targetFieldname>=function(params) ...]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>arrayOfFieldnames:&nbsp;</b>The names of the fields used to group the statistics.</p>" 
			  +"<p><b>targetFieldname:&nbsp;</b>Name of the field to assign the resulting aggregation value.</p>"
			  +"<p><b>function(params):&nbsp;</b>Call of an aggregation function.</p>"
			  +"</p>"
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
	public void initializeAction() throws Exception{
		
		for(int i = 0; i < parts.size(); i++) {
			
			QueryPart currentPart = parts.get(i);
			
			if(currentPart instanceof QueryPartAssignment) {
				//--------------------------------------------------
				// Resolve Fieldname=Function
				QueryPartAssignment assignment = (QueryPartAssignment)currentPart;
				String assignmentName = assignment.getLeftSideAsString(null);
				QueryPart assignmentValuePart = ((QueryPartAssignment) currentPart).getRightSide();
				
				if(assignmentName != null) {
					//--------------------------------------------------
					// By Parameter
					if(assignmentName.trim().equals("by")) {
						QueryPartValue assignmentValue = currentPart.determineValue(null);
						ArrayList<String> fieldnames = assignmentValue.getAsStringArray();
						groupByFieldnames.addAll(fieldnames);
						detectedFieldnames.addAll(fieldnames);
					}
					//--------------------------------------------------
					// Any other parameter
					else {
						
						detectedFieldnames.add(assignmentName);
						
						if(assignmentValuePart instanceof QueryPartFunction) {
							QueryPartFunction function = ((QueryPartFunction)assignmentValuePart);
							CFWQueryFunction functionInstance = function.getFunctionInstance();
							if(functionInstance.supportsAggregation()) {
								functionMap.put(assignmentName, function);
							}else {
								throw new ParseException(COMMAND_NAME+": Function '"+functionInstance.uniqueName()+"' does not support aggregations.", -1);
							}
						}else {
							throw new ParseException(COMMAND_NAME+": Value must be an aggregation function.", -1);
						}
					}
					
				}else {
					throw new ParseException(COMMAND_NAME+": left side of an assignment cannot be null.", -1);
				}
				
				assignments.add((QueryPartAssignment)currentPart);
			}else if(currentPart instanceof QueryPartFunction) {
				//--------------------------------------------------
				// Resolve Function only
				QueryPartFunction function = ((QueryPartFunction)currentPart);
				CFWQueryFunction functionInstance = function.getFunctionInstance();
				String fieldname=function.getDefaultLabel();
				
				detectedFieldnames.add(fieldname);
				
				if(functionInstance.supportsAggregation()) {
					functionMap.put(fieldname, function);
				}else {
					throw new ParseException(COMMAND_NAME+": Function '"+functionInstance.uniqueName()+"' does not support aggregations.", -1);
				}

			}else {
				throw new ParseException(COMMAND_NAME+": Only assignment expressions(key=value) allowed.", -1);
			}
		}
		
		
		this.fieldnameKeep(detectedFieldnames.toArray(new String[]{}));
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		//boolean printed = false;
		while(keepPolling()) {
			EnhancedJsonObject record = inQueue.poll();
			
			//----------------------------
			// Create Group String
			String groupID = record.createGroupIDString(groupByFieldnames);
			
			//----------------------------
			// Create and Get Group
			if(!groupMap.containsKey(groupID)) {
				JsonObject object = new JsonObject();
				for(String fieldname : groupByFieldnames) {
					
					JsonElement element = record.get(fieldname);
					object.add(fieldname, element);
				}
				
				AggregationGroup newGroup = new AggregationGroup(object, null);

				newGroup.addFunctions(functionMap);
				groupMap.put(groupID, newGroup);
			}
			
			AggregationGroup currentGroup = groupMap.get(groupID);
					
			//----------------------------
			// CreateGrouping
			currentGroup.doAggregation(record);
		
		}
		
		//----------------------------
		// Add records to group
		if(isPreviousDone()) {
			for(AggregationGroup group : groupMap.values()) {
				outQueue.add(group.toRecord());
			}
			this.setDone();
		}
		
	}

}
