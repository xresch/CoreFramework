package com.xresch.cfw.features.query.commands;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeSet;

import com.google.gson.JsonElement;
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
import com.xresch.cfw.utils.CFWMath.CFWMathParabolicSAR;


/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandPSAR extends CFWQueryCommand {
	
	private static final String COMMAND_NAME = "psar";
	
	private ArrayList<QueryPartAssignment> assignmentParts = new ArrayList<>();
	
	private ArrayList<String> groupByFieldnames = new ArrayList<>();
	
	// Group name and vlaues of the group
	private LinkedHashMap<String, CFWMathParabolicSAR> periodicMap = new LinkedHashMap<>();

	private String highFieldname = null;
	private String lowFieldname = null;
	private String name = null;
	private Integer precision = null;
	private Double acceleration = null;
	private Double accelerationMax = null;

	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandPSAR(CFWQuery parent) {
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
		return "Calculates parabolic SAR (stop and reverse) for the values of a field.";
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
			  +"<li><b>high:&nbsp;</b>Name of the field which contains the high value.</li>"
			  +"<li><b>low:&nbsp;</b>Name of the field which contains the low value.</li>"
			  +"<li><b>name:&nbsp;</b>The name of the target field to store the moving average value(Default: 'psar').</li>"
			  +"<li><b>acceleration:&nbsp;</b>The acceleration for the calculation (Default: 0.02).</li>"
			  +"<li><b>accelerationMax:&nbsp;</b>The maximum acceleration for the calculation (Default: 0.2).</li>"
			  +"<li><b>precision:&nbsp;</b>The decimal precision of the moving average (Default: 6, what is also the maximum).</li>"
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
				else if	 (assignmentName.equals("high")) {			highFieldname = assignmentValue.getAsString(); }
				else if	 (assignmentName.equals("low")) {			lowFieldname = assignmentValue.getAsString(); }
				else if	 (assignmentName.equals("name")) {			name = assignmentValue.getAsString(); }
				else if	 (assignmentName.equals("precision")) {		precision = assignmentValue.getAsInteger(); }
				else if	 (assignmentName.equals("acceleration")) {	acceleration = assignmentValue.getAsDouble(); }
				else if	 (assignmentName.equals("accelerationmax")) {	accelerationMax = assignmentValue.getAsDouble(); }

				else {
					throw new ParseException(COMMAND_NAME+": Unsupported parameter '"+assignmentName+"'", -1);
				}
				
			}
		}
		
		//------------------------------------------
		// Sanitize
		
		if(name == null) { name = "psar";}
		if(precision == null) { precision = 6;}
		if(acceleration == null ) { acceleration = 0.02;}
		if(accelerationMax == null ) { accelerationMax = 0.2;}
		
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
			QueryPartValue high = QueryPartValue.newFromJsonElement(record.get(highFieldname));
			QueryPartValue low = QueryPartValue.newFromJsonElement(record.get(lowFieldname));
			
			//----------------------------
			// Create Group String
			String groupID = record.createGroupIDString(groupByFieldnames);
			
			//----------------------------
			// Create and Get Group
			if(!periodicMap.containsKey(groupID)) {
				periodicMap.put(groupID, CFW.Math.createParabolicSAR(acceleration, accelerationMax, precision) );
			}
			
			CFWMathParabolicSAR mathPeriodic = periodicMap.get(groupID);
			BigDecimal bigHigh = high.getAsBigDecimal();
			BigDecimal bigLow = low.getAsBigDecimal();
			if(bigHigh == null) { bigHigh = BigDecimal.ZERO; }
			if(bigLow == null) { bigLow = BigDecimal.ZERO; }
			
			BigDecimal psar = mathPeriodic.calcPSAR(bigHigh, bigLow);

			record.addProperty(name, psar);
			
			outQueue.add(record);
			
		}
		
		this.setDoneIfPreviousDone();
	
	}

}
