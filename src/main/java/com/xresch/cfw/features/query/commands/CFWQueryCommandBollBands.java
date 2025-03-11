package com.xresch.cfw.features.query.commands;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandBollBands extends CFWQueryCommand {
	
	private static final String COMMAND_NAME = "bollbands";

	private ArrayList<QueryPartAssignment> assignmentParts = new ArrayList<>();
	
	private ArrayList<String> groupByFieldnames = new ArrayList<>();
	
	// Group name and vlaues of the group
	private LinkedHashMap<String, ArrayList<BigDecimal>> valuesMap = new LinkedHashMap<>();

	private String fieldname = null;
	private String prefix = null;
	private Integer precision = null;
	private Integer period = null;
	private float stdevMultiplier = 2;
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandBollBands(CFWQuery parent) {
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
		tags.add(_CFWQueryCommon.TAG_ANALYTICS);
		return tags;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Calculates the bollinger bands for the values of a field.";
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
			  +"<li><b>prefix:&nbsp;</b>The prefix of the target fields to store the bollinger band values(Default: 'BOLL_').</li>"
			  +"<li><b>period:&nbsp;</b>(Optional)The number of datapoints used for creating the bollinger bands(Default: 20).</li>"
			  +"<li><b>precision:&nbsp;</b>(Optional)The decimal precision of the moving average (Default: 3, Max: 6).</li>"
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
			
			String assignmentName = assignment.getLeftSideAsString(null);
			QueryPartValue assignmentValue = assignment.determineValue(null);
			
			if(assignmentName != null) {
				assignmentName = assignmentName.trim().toLowerCase();

				if(assignmentName.trim().equals("by")) {
					ArrayList<String> fieldnames = assignmentValue.getAsStringArray();
					groupByFieldnames.addAll(fieldnames);
				}
				else if	 (assignmentName.equals("field")) {			fieldname = assignmentValue.getAsString(); }
				else if	 (assignmentName.equals("prefix")) {		prefix = assignmentValue.getAsString(); }
				else if	 (assignmentName.equals("precision")) {		precision = assignmentValue.getAsInteger(); }
				else if	 (assignmentName.equals("period")) {	period = assignmentValue.getAsInteger(); }

				else {
					throw new ParseException(COMMAND_NAME+": Unsupported parameter '"+assignmentName+"'", -1);
				}
				
			}
		}
		
		// 10 = 1.5
		// 20 = 2
		// 50 = 2.5
		// etc...
		if(period <= 20) {
			stdevMultiplier = 1.0f + (period / 20.0f);
		}else {
			stdevMultiplier = (period / 20.0f);
		}

		//------------------------------------------
		// Sanitize
		
		if(prefix == null) { prefix = "boll-";}
		if(precision == null) { precision = 6;}
		if(period == null ) { period = 20;}
		
		//------------------------------------------
		// Add Detected Fields
		this.fieldnameAdd(prefix+"upper");
		this.fieldnameAdd(prefix+"movavg");
		this.fieldnameAdd(prefix+"lower");
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		//boolean printed = false;
		while(keepPolling()) {
			EnhancedJsonObject record = inQueue.poll();
			QueryPartValue value = QueryPartValue.newFromJsonElement(record.get(fieldname));
			
			//----------------------------
			// Create Group String
			String groupID = record.createGroupIDString(groupByFieldnames);
			
			//----------------------------
			// Create and Get Group
			if(!valuesMap.containsKey(groupID)) {
				valuesMap.put(groupID, new ArrayList<>());
			}
			
			ArrayList<BigDecimal> groupedValues = valuesMap.get(groupID);
			groupedValues.add(value.getAsBigDecimal());
			
			BigDecimal movavg = CFW.Math.bigMovAvg(groupedValues, period, precision);
			BigDecimal movstdev = CFW.Math.bigMovStdev(groupedValues, period, false, precision);
			
			BigDecimal bollUpper = null;
			BigDecimal bollLower = null;
			
			if(movstdev != null) {
				BigDecimal offset = movstdev.multiply(new BigDecimal(stdevMultiplier));
				bollUpper = movavg.add(offset);
				bollLower = movavg.subtract(offset);
			}

			record.addProperty(prefix+"upper", bollUpper);
			record.addProperty(prefix+"movavg", movavg);
			record.addProperty(prefix+"lower", bollLower);
			
			outQueue.add(record);
			
		}
		
		this.setDoneIfPreviousDone();
	
	}

}
