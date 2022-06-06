package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

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
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartArray;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartFunction;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.pipeline.PipelineActionContext;

public class CFWQueryCommandStats extends CFWQueryCommand {
	
	ArrayList<String> groupByFieldnames = new ArrayList<>();
	LinkedHashMap<String, QueryPartFunction> functionMap = new LinkedHashMap<>();
	
	// contains groupID and aggregationGroup
	LinkedHashMap<String, AggregationGroup> groupMap = new LinkedHashMap<>();
	
	
	ArrayList<String> detectedFieldnames = new ArrayList<>();
	ArrayList<QueryPartAssignment> assignments = new ArrayList<>();

	
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
		return new String[] {"stats", "aggregate"};
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
		return "stats by=<arrayOfFieldnames> <targetFieldname>=function(params) [<targetFieldname>=function(params) ...]";
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
		
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".commands", "command_stats.html");
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
				QueryPartAssignment assignment = (QueryPartAssignment)currentPart;
				String assignmentName = assignment.getLeftSideAsString(null);
				QueryPart assignmentValue = ((QueryPartAssignment) currentPart).getRightSide();
				
				if(assignmentName != null) {
					//--------------------------------------------------
					// By Parameter
					if(assignmentName.trim().equals("by")) {
						
						if(assignmentValue instanceof QueryPartArray) {
							ArrayList<String> fieldnames = ((QueryPartArray)assignmentValue).getAsStringArray(null, false);
							groupByFieldnames.addAll(fieldnames);
							detectedFieldnames.addAll(fieldnames);
						}else if(assignmentValue instanceof QueryPartValue) {
							String fieldname = ((QueryPartValue)assignmentValue).getAsString();
							
							if(fieldname != null) {
								groupByFieldnames.add(fieldname);
								detectedFieldnames.add(fieldname);
							}else {
								parser.throwParseException("stats: value for by-parameter cannot be null.", currentPart);
							}
							
						}else {
							parser.throwParseException("stats: value for by-parameter must be a string or array.", currentPart);
						}
					}
					//--------------------------------------------------
					// Any other parameter
					else {
						
						detectedFieldnames.add(assignmentName);
						
						if(assignmentValue instanceof QueryPartFunction) {
							QueryPartFunction function = ((QueryPartFunction)assignmentValue);
							CFWQueryFunction functionInstance = function.getFunctionInstance();
							if(functionInstance.supportsAggregation()) {
								functionMap.put(assignmentName, function);
							}else {
								parser.throwParseException("stats: Function '"+functionInstance.uniqueName()+"' does not support aggregations.", currentPart);
							}
						}else {
							parser.throwParseException("stats: Value must be an aggregation function.", currentPart);
						}
					}
					
				}else {
					parser.throwParseException("stats: left side of an assignment cannot be null.", currentPart);
				}
				
				assignments.add((QueryPartAssignment)currentPart);
			}else {
				parser.throwParseException("stats: Only assignment expressions(key=value) allowed.", currentPart);
			}
		}
		
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		result.setHTMLDescription(
				"<b>Hint:&nbsp;</b>Use aggregate functions to create statistics.<br>"
				+"<b>Syntax:&nbsp;</b>"+CFW.Security.escapeHTMLEntities(this.descriptionSyntax())
			);
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void initializeAction() {
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
			String groupID = "";
			
			for(String fieldname : groupByFieldnames) {
				JsonElement element = record.get(fieldname);
				if(element == null || element.isJsonNull()) {
					groupID += "-cfwNullPlaceholder";
				}else {
					groupID += record.get(fieldname).toString();
				}
			}
			
			//----------------------------
			// Create and Get Group
			
			if(!groupMap.containsKey(groupID)) {
				JsonObject object = new JsonObject();
				for(String fieldname : groupByFieldnames) {
					JsonElement element = record.get(fieldname);
					object.add(fieldname, element);
				}
				
				AggregationGroup newGroup = new AggregationGroup(object);

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
	
	public class AggregationGroup {
	
		private JsonObject groupValues;
		private ArrayList<String> targetFieldnames = new ArrayList<>();
		private LinkedHashMap<String, QueryPartFunction> functionMap = new LinkedHashMap<>();
		
		public AggregationGroup(JsonObject groupValues) {
			this.groupValues = groupValues;
		}
		
		public void addFunctions(LinkedHashMap<String, QueryPartFunction> functions) {
			
			for(Entry<String, QueryPartFunction> entry : functions.entrySet()) {
				this.addFunction(entry.getKey(), entry.getValue());
			}
			
		}

		public void addFunction(String targetFieldname, QueryPartFunction functionPart) {
			
			targetFieldnames.add(targetFieldname);
			String instanceID = functionPart.createManagedInstance();
			functionMap.put(instanceID, functionPart);
		}
		
		public void doAggregation(EnhancedJsonObject object) {
			
			for(Entry<String, QueryPartFunction> entry : functionMap.entrySet()) {
				entry.getValue().aggregateFunctionInstance(entry.getKey(), object);
			}
		}
		
		public EnhancedJsonObject toRecord() {
			
			int index = 0;
			for(Entry<String, QueryPartFunction> entry : functionMap.entrySet()) {
				String propertyName = targetFieldnames.get(index);
				System.out.println("propertyName"+propertyName);
				String instanceID = entry.getKey();
				QueryPartFunction functionPart = entry.getValue();
				QueryPartValue aggregationValue = functionPart.executeFunctionInstance(instanceID, null);
				
				aggregationValue.addToJsonObject(propertyName, groupValues);
				index++;
			}
			
			return new EnhancedJsonObject(groupValues);
		}
		
	}

}
