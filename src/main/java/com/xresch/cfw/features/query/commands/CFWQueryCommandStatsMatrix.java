package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.google.gson.JsonArray;
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
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartFunction;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.pipeline.PipelineActionContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandStatsMatrix extends CFWQueryCommand {
	
	public static final String COMMAND_NAME = "statsmatrix";
	
	private ArrayList<QueryPart> parts;
	
	private ArrayList<String> rowFieldnames = new ArrayList<>();
	private LinkedHashMap<String, QueryPartFunction> functionMap = new LinkedHashMap<>();
	
	// contains row plus columnMaps
	private TreeMap<String, TreeMap<String, AggregationGroup>> rowMap = new TreeMap<>();
	
	private String columnFieldname = null;
	
	private LinkedHashSet<String> detectedFieldnames = new LinkedHashSet<>();
	private ArrayList<QueryPartAssignment> assignments = new ArrayList<>();

	private QueryPartValue listFormatter = null;
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandStatsMatrix(CFWQuery parent) {
		super(parent);
		JsonArray listFormatterParams = new JsonArray();
		listFormatterParams.add("list");
		listFormatterParams.add("none");
		listFormatterParams.add("0px");
		listFormatterParams.add(true);
		listFormatter = QueryPartValue.newJson(listFormatterParams);
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
	public String descriptionShort() {
		return "Creates statistics based on two fields used for columns, rows and an aggregation of values.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" column=<columnFieldname> row=<rowFieldnames> <targetFieldname>=<function(params)> [<targetFieldname>=<function(params)> ...]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 
			   "<p><b>columnFieldname:&nbsp;</b>The name of the field used as the identifier for the columns.</p>" 
			  +"<p><b>rowFieldname:&nbsp;</b>The name of the field(s) used as the identifier for the rows as a string or array of strings.</p>" 
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
					assignmentName = assignmentName.trim();
					//--------------------------------------------------
					// By Parameter
					if(assignmentName.toLowerCase().equals("column")) {
						QueryPartValue assignmentValue = currentPart.determineValue(null);
						columnFieldname = assignmentValue.getAsString();
					} else if(assignmentName.toLowerCase().equals("row")) {
						QueryPartValue assignmentValue = currentPart.determineValue(null);
						rowFieldnames = assignmentValue.getAsStringArray();
						detectedFieldnames.addAll(rowFieldnames);
						
					//--------------------------------------------------
					// Any other parameter
					} else {
						
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
			}else {
				throw new ParseException(COMMAND_NAME+": Only assignment expressions(key=value) allowed.", -1);
			}
		}
		
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
			String columnValue = 
					QueryPartValue.newFromJsonElement(
							record.get(columnFieldname)
						).getAsString();
			columnValue = (columnValue == null) ? "null" : columnValue;
			detectedFieldnames.add(columnValue);
			
			//----------------------------
			// Create Row Group String
			String rowID = "";
			
			for(String fieldname : rowFieldnames) {
				JsonElement element = record.get(fieldname);
				if(element == null || element.isJsonNull()) {
					rowID += "-cfwNullPlaceholder";
				}else {
					rowID += record.get(fieldname).toString();
				}
			}
			
			
			//----------------------------
			// Create and Get Group
			if(!rowMap.containsKey(rowID)) {
				TreeMap<String, AggregationGroup> newColumnMap = new TreeMap<>();
				rowMap.put(rowID, newColumnMap);
			}
			
			TreeMap<String, AggregationGroup> columnMap = rowMap.get(rowID);
			
			if(!columnMap.containsKey(columnValue)) {
				
				JsonObject objectWithValues = new JsonObject();
				for(String fieldname : rowFieldnames) {
					
					JsonElement element = record.get(fieldname);
					objectWithValues.add(fieldname, element);
				}
				AggregationGroup newGroup = new AggregationGroup(new JsonObject(), objectWithValues);
				newGroup.addFunctions(functionMap);
				columnMap.put(columnValue, newGroup);
			}
			
			AggregationGroup currentGroup = columnMap.get(columnValue);
					
			//----------------------------
			// CreateGrouping
			currentGroup.doAggregation(record);
		
		}
		
		//----------------------------
		// Create Result Records
		if(isPreviousDone() && getInQueue().isEmpty()) {
			
			for(Entry<String, TreeMap<String, AggregationGroup>> rowEntry : rowMap.entrySet()) {
				EnhancedJsonObject newRecord = new EnhancedJsonObject();
				boolean isGroupAdded = false;
				for(Entry<String, AggregationGroup> columnsEntry : rowEntry.getValue().entrySet()) {
					
					if(!isGroupAdded) {
						newRecord.addAll(columnsEntry.getValue().getGroupValues());
						isGroupAdded = true;
					}
					
					EnhancedJsonObject value = columnsEntry.getValue().toRecord();
					if(value.size() == 1) {
						JsonElement unboxedValue = null;
						for(String onlyKey : value.keySet()) { unboxedValue = value.get(onlyKey); }
						newRecord.add(columnsEntry.getKey(), unboxedValue);
					}else {
						newRecord.add(columnsEntry.getKey(), value.getWrappedObject());
					}
					
				}
				outQueue.add(newRecord);
			}
			
			//----------------------------
			// Handle Fields
			for(String fieldname : detectedFieldnames) {
				CFWQueryCommandFormatField.addFormatter(this.parent.getContext(), fieldname, listFormatter);
			}
			this.fieldnameKeep(detectedFieldnames.toArray(new String[]{}));

			this.setDone();
		}
		
	}

}
