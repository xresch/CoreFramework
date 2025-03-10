package com.xresch.cfw.features.query.commands;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

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
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.pipeline.PipelineActionContext;
import com.xresch.cfw.utils.CFWMath.CFWMathPeriodic;


/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandIndex extends CFWQueryCommand {
	
	private static final String COMMAND_NAME = "index";
	
	private ArrayList<QueryPartAssignment> assignmentParts = new ArrayList<>();
	
	private ArrayList<String> groupByFieldnames = new ArrayList<>();
	
	// Group name and the first non null value of the group
	private LinkedHashMap<String, BigDecimal> groupsMap = new LinkedHashMap<>();

	private String fieldname = null;
	private String name = null;
	private Integer precision = null;
	private BigDecimal base = null;

	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandIndex(CFWQuery parent) {
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
		return "Calculates an index for the values of a field.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" <param>=<value> [<param>=<value> ...]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<ul>"
			  +"<li><b>by:&nbsp;</b>Array of the fieldnames which should be used for grouping.</li>"
			  +"<li><b>field:&nbsp;</b>Name of the field which contains the value.</li>"
			  +"<li><b>name:&nbsp;</b>The name of the target field to store the moving average value(Default: name+'_index').</li>"
			  +"<li><b>base:&nbsp;</b>The base of the index, e.g 100. By default, percentage will be returned. (Default: null).</li>"
			  +"<li><b>precision:&nbsp;</b>The decimal precision of the index (Default: 6, what is also the maximum).</li>"
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
				
		// by=<fieldname>
		// target=GROUP
		// step=10
		// precision=1
		// period=10
		
		//------------------------------------------
		// Get Parameters
		for(QueryPartAssignment assignment : assignmentParts) {
			
			String assignmentName = assignment.getLeftSideAsString(null);
			QueryPartValue assignmentValue = assignment.determineValue(null);
			
			if(assignmentName != null) {
				assignmentName = assignmentName.trim().toLowerCase();

				if(assignmentName.trim().equals("by")) {
					ArrayList<String> fieldnames = assignmentValue.getAsStringArray();
					groupByFieldnames.addAll(fieldnames);
				}
				else if	 (assignmentName.equals("field")) {			fieldname = assignmentValue.getAsString(); }
				else if	 (assignmentName.equals("name")) {			name = assignmentValue.getAsString(); }
				else if	 (assignmentName.equals("precision")) {		precision = assignmentValue.getAsInteger(); }
				else if	 (assignmentName.equals("base")) {	base = assignmentValue.getAsBigDecimal(); }

				else {
					throw new ParseException(COMMAND_NAME+": Unsupported parameter '"+assignmentName+"'", -1);
				}
				
			}
		}
		
		//------------------------------------------
		// Sanitize
		
		if(name == null) { name = fieldname+"_index";}
		if(precision == null) { precision = 6;}
		
		
		
		
		//------------------------------------------
		// Add Detected Fields
		this.fieldnameAdd(name);
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		//boolean printed = false;
		while(keepPolling()) {
			EnhancedJsonObject record = inQueue.poll();
			QueryPartValue valuePart = QueryPartValue.newFromJsonElement(record.get(fieldname));
			BigDecimal currentValue = valuePart.getAsBigDecimal();
			
			//----------------------------
			// Create Group String
			String groupID = record.createGroupIDString(groupByFieldnames);
			
			//----------------------------
			// Create Group and Get First Value
			if(!groupsMap.containsKey(groupID)) {
				groupsMap.put(groupID, currentValue);
			}
			
			BigDecimal firstValue = groupsMap.get(groupID);
			
			if(firstValue == null) {
				groupsMap.put(groupID, currentValue);
				firstValue = currentValue;
			}
			
			//----------------------------
			// Calculate Index
			
			if(currentValue == null) {
				record.add(name, JsonNull.INSTANCE);
			
			}else {
				
				if(base == null) {
					// Return Percentages
					BigDecimal percentageIndex = 
							currentValue.divide(firstValue, precision, CFW.Math.ROUND_UP)
									  .subtract(CFW.Math.ONE)
									  .multiply(CFW.Math.BIG_100);
					record.addProperty(name, percentageIndex);
				}else {
					// Return index
					BigDecimal percentageIndex = 
							currentValue.divide(firstValue, precision, CFW.Math.ROUND_UP)
									  .multiply(base);
					record.addProperty(name, percentageIndex);
				}
			}

			outQueue.add(record);
			
		}
		
		this.setDoneIfPreviousDone();
	
	}

}
