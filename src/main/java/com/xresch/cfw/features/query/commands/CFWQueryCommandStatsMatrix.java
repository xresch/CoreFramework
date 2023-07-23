package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.TreeMap;

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

public class CFWQueryCommandStatsMatrix extends CFWQueryCommand {
	
	public static final String COMMAND_NAME = "statsmatrix";
	
	private ArrayList<QueryPart> parts;
	
	private ArrayList<String> groupByFieldnames = new ArrayList<>();
	private LinkedHashMap<String, QueryPartFunction> functionMap = new LinkedHashMap<>();
	
	// contains row plus columnMaps
	private TreeMap<String, TreeMap<String, AggregationGroup>> rowMap = new TreeMap<>();
	
	private String columnFieldname = null;
	private String rowFieldname = null;
	
	private LinkedHashSet<String> detectedFieldnames = new LinkedHashSet<>();
	private ArrayList<QueryPartAssignment> assignments = new ArrayList<>();

	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandStatsMatrix(CFWQuery parent) {
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
	public String descriptionShort() {
		return "Creates statistics based on two fields used for columns, rows and an aggregation of values.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" column=<columnFieldname> row=<rowFieldname> values=<function(params)>";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 
			   "<p><b>columnFieldname:&nbsp;</b>The name of the field used as the identifier for the columns.</p>" 
			  +"<p><b>rowFieldname:&nbsp;</b>The name of the field used as the identifier for the rows.</p>" 
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
						rowFieldname = assignmentValue.getAsString();
						detectedFieldnames.add(rowFieldname);
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
			String groupID = "";
			String columnValue = 
					QueryPartValue.newFromJsonElement(
							record.get(columnFieldname)
						).getAsString();
			columnValue = (columnValue == null) ? "null" : columnValue;
			detectedFieldnames.add(columnValue);
			
			String rowValue = 
					QueryPartValue.newFromJsonElement(
							record.get(rowFieldname)
							).getAsString();
			columnValue = (columnValue == null) ? "null" : columnValue;
			
			//----------------------------
			// Create and Get Group
			if(!rowMap.containsKey(rowValue)) {
				TreeMap<String, AggregationGroup> newColumnMap = new TreeMap<>();
				rowMap.put(rowValue, newColumnMap);
			}
			
			TreeMap<String, AggregationGroup> columnMap = rowMap.get(rowValue);
			
			if(!columnMap.containsKey(columnValue)) {
				AggregationGroup newGroup = new AggregationGroup();

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
				newRecord.addProperty(rowFieldname, rowEntry.getKey());
				for(Entry<String, AggregationGroup> columnsEntry : rowEntry.getValue().entrySet()) {
					
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
			
			this.fieldnameKeep(detectedFieldnames.toArray(new String[]{}));

			this.setDone();
		}
		
	
	}
	
	public class AggregationGroup {
	
		private ArrayList<String> targetFieldnames = new ArrayList<>();
		private LinkedHashMap<String, QueryPartFunction> functionMap = new LinkedHashMap<>();
		
		public AggregationGroup() {
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
			
			JsonObject groupValues = new JsonObject();
			
			int index = 0;
			for(Entry<String, QueryPartFunction> entry : functionMap.entrySet()) {
				String propertyName = targetFieldnames.get(index);

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
