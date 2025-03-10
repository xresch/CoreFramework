package com.xresch.cfw.features.query.commands;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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
public class CFWQueryCommandOutlier extends CFWQueryCommand {
	
	private static final String COMMAND_NAME = "outlier";

	private ArrayList<QueryPartAssignment> assignmentParts = new ArrayList<>();
	
	private ArrayList<String> groupByFieldnames = new ArrayList<>();
	
	private LinkedHashMap<String, ArrayList<EnhancedJsonObject>> groupedRecords = new LinkedHashMap<>();

	private String fieldname = null;
	private String name = null;
	private BigDecimal sensitivity = null;
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandOutlier(CFWQuery parent) {
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
		return "Detects outliers int time series based on Inter-Quantile-Range(IQR).";
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
			  +"<li><b>field:&nbsp;</b>Name of the field which contains the value outliers should be detected in.</li>"
			  +"<li><b>name:&nbsp;</b>The name of the target field to store the detected outliers. (Default: name+'_outlier')</li>"
			  +"<li><b>sensitivity:&nbsp;</b>A sensitivity multiplier, higher values make the detection less sensitive. (Default: 1.5).</li>"
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
				else if	 (assignmentName.equals("sensitivity")) {	sensitivity = assignmentValue.getAsBigDecimal(); }

				else {
					throw new ParseException(COMMAND_NAME+": Unsupported parameter '"+assignmentName+"'", -1);
				}
				
			}
		}
		
		//------------------------------------------
		// Sanitize
		
		if(name == null) { name = fieldname+"_outlier";}
		if(sensitivity == null ) { sensitivity = new BigDecimal(1.5); }
		
		
		//------------------------------------------
		// Add Detected Fields
		this.fieldnameAdd(name);
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
				// Create List of Values
				ArrayList<BigDecimal> values = new ArrayList<>();
				
				for(int i = 0 ; i < group.size(); i++) {
					
					EnhancedJsonObject record = group.get(i);
					
					QueryPartValue valuePart = QueryPartValue.newFromJsonElement(record.get(fieldname));
					BigDecimal value = valuePart.getAsBigDecimal();
					
					values.add(value);
				}
				
				//------------------------------------
				// Calculate Outliers
				ArrayList<Boolean> outliersArray = CFW.Math.bigOutlier(values, sensitivity);
				
				//------------------------------------
				// Send All Records to Out Queue
				for(int k = 0 ; k < group.size(); k++) {
					EnhancedJsonObject record = group.get(k);
					record.addProperty(name, outliersArray.get(k));
					outQueue.add(record);
				}
			}
			

			this.setDone();
		}
		
		
	
	}
	

}
