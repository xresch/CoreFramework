package com.xresch.cfw.features.query.commands;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.pipeline.PipelineActionContext;


/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandGroup extends CFWQueryCommand {
	
	private static final String COMMAND_NAME = "group";
	private static final BigDecimal MINUS_ONE = new BigDecimal(-1);
	
	private ArrayList<QueryPartAssignment> assignmentParts = new ArrayList<QueryPartAssignment>();
	
	private String type = "number";
	private String byFieldname = null;
	private String name = "Group";
	private BigDecimal step = BigDecimal.TEN;
	private BigDecimal multiplier = BigDecimal.ONE;
	private Integer maxgroups = 1000;
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandGroup(CFWQuery parent) {
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
		return "Adds a group field based on another field value.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" <fieldname>=<expression> [<fieldname>=<expression> ...]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>fieldname:&nbsp;</b>Name of the field to assign the value to.</p>"
			  +"<p><b>expression:&nbsp;</b>Expression to evaluate.</p>"
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
				
		// type=number|time|string
		// by=<fieldname>
		// target=GROUP
		// step=10
		// multiplier=1
		// timeformat=null
		// timeunit=ms
		
		//------------------------------------------
		// Get Parameters
		for(QueryPartAssignment assignment : assignmentParts) {
			
			String assignmentName = assignment.getLeftSideAsString(null);
			QueryPartValue assignmentValue = assignment.determineValue(null);
			
			if(assignmentName != null) {
				assignmentName = assignmentName.trim().toLowerCase();
				if		 (assignmentName.equals("type")) {			type = assignmentValue.getAsString(); }
				else if	 (assignmentName.equals("by")) {			byFieldname = assignmentValue.getAsString(); }
				else if	 (assignmentName.equals("name")) {			name = assignmentValue.getAsString(); }
				else if	 (assignmentName.equals("step")) {			step = assignmentValue.getAsBigDecimal(); }
				else if	 (assignmentName.equals("multiplier")) {	multiplier = assignmentValue.getAsBigDecimal(); }
				else if	 (assignmentName.equals("maxgroups")) {		maxgroups = assignmentValue.getAsInteger(); }

				else {
					throw new ParseException(COMMAND_NAME+": Unsupported argument.", -1);
				}
				
			}
		}
		
		//------------------------------------------
		// Sanitize
		if(type == null) { type = "number";}
		type = type.trim().toLowerCase();
		
		if(name == null) { name = "Group";}
		if(step == null) { step = BigDecimal.TEN;}
		if(multiplier == null) { multiplier = BigDecimal.ZERO;}
		
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
			
			if(Strings.isNullOrEmpty(byFieldname)) {
				outQueue.add(record);
			}else {
				
				QueryPartValue value = QueryPartValue.newFromJsonElement(record.get(byFieldname));
				
				if(type.equals("number") ) {
					
					String groupName = evaluateNumber(value);
					record.addProperty(name, groupName);
				}
				
				outQueue.add(record);
			}
		}
		
		this.setDoneIfPreviousDone();
	
	}

	public String evaluateNumber(QueryPartValue value) {
		
		String groupName = null;
		if(value.isNumberOrNumberString()) {
		
			
			BigDecimal number = value.getAsBigDecimal();
			boolean isPositive = number.compareTo(BigDecimal.ZERO) != -1;
			BigDecimal rangeStart = BigDecimal.ZERO;
			BigDecimal finalStep = step;
			
			if(!isPositive) {
				finalStep = finalStep.multiply(MINUS_ONE);
				rangeStart = MINUS_ONE;
			}
			
			BigDecimal rangeEnd = finalStep;
			
			for(int i = 0; i < maxgroups; i++  ) {
				
				if( 
					(isPositive && number.compareTo(rangeStart) >= 0 && number.compareTo(rangeEnd) <= 0) 
				|| (!isPositive && number.compareTo(rangeEnd) >= 0 && number.compareTo(rangeStart) <= 0) 
				) {
					break;
				}
				
				rangeStart = (isPositive) ? rangeEnd.add(BigDecimal.ONE) : rangeEnd.subtract(BigDecimal.ONE);
				if(multiplier.compareTo(BigDecimal.ONE) == 0) {
					rangeEnd = rangeEnd.add(finalStep);
				}else {
					rangeEnd = rangeEnd.multiply(multiplier);
				}
			}
			
			groupName = rangeStart + " - " + rangeEnd;
		}
		return groupName;
	}

}
