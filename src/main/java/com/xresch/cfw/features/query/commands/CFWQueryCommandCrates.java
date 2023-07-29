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
import com.xresch.cfw.utils.CFWTime.CFWTimeUnit;


/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandCrates extends CFWQueryCommand {
	
	private static final String COMMAND_NAME = "crates";
	private static final BigDecimal MINUS_ONE = new BigDecimal(-1);
	
	private ArrayList<QueryPartAssignment> assignmentParts = new ArrayList<QueryPartAssignment>();
	
	private String type = "number";
	private String byFieldname = null;
	private String name = "CRATE";
	private BigDecimal step = BigDecimal.TEN;
	private BigDecimal multiplier = BigDecimal.ONE;
	private String timeunit = "m";
	private Integer maxgroups = 1000;
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandCrates(CFWQuery parent) {
		super(parent);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String[] uniqueNameAndAliases() {
		return new String[] {COMMAND_NAME, "bin"};
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Adds a field to sort a record into a certain 'crate' or 'bin', which contains items with a similar values.";
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
				
		// type=number|time|alpha
		// by=<fieldname>
		// target=GROUP
		// step=10
		// multiplier=1
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
				else if	 (assignmentName.equals("timeunit")) {		timeunit = assignmentValue.getAsString(); }
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
		if(timeunit == null || !CFWTimeUnit.has(timeunit)) { timeunit = "m";}
		
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
				
				switch(type) {
					
					case "number": 		record.addProperty(name, evaluateNumber(value));	break;
					case "alpha": 		record.addProperty(name, evaluateAlpha(value));	break;
					case "time": 		record.addProperty(name,evaluateTime(value));	break;
						
				}
				
				outQueue.add(record);
			}
		}
		
		this.setDoneIfPreviousDone();
	
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public String evaluateNumber(QueryPartValue value) {
		
		String crateName = null;
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
			
			int i = 0;
			while(true) {
				
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
				
				if(i >= maxgroups) {
					crateName = ">= " + rangeStart;
					break;
				}
				
				i++;
			}
			
			if(crateName == null) {
				crateName = rangeStart + " - " + rangeEnd;
			}
		}
		
		return crateName;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public String evaluateAlpha(QueryPartValue value) {
		
		String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String crateName = null;
		if(!value.isNullOrEmptyString()) {

			String stringValue = value.getAsString().toUpperCase();
			char firstChar = stringValue.charAt(0);
			
			int rangeStart = 0;
			int rangeEnd = step.intValue()-1;
			
			while(true) {

				if(rangeStart >= alpha.length()) { crateName = "Other"; break; }
				if(rangeEnd >= alpha.length()) { break; }
				
				if( 
					Character.compare(firstChar, alpha.charAt(rangeStart)) >= 0  
				 && Character.compare(firstChar, alpha.charAt(rangeEnd)) <= 0
				) {
					break;
				}
				
				rangeStart = rangeEnd+1;
				rangeEnd = rangeEnd+step.intValue();
				
			}
			
			if(crateName == null) {
				if(rangeEnd >= alpha.length()) { rangeEnd = alpha.length()-1; }
				crateName = alpha.charAt(rangeStart) + " - " + alpha.charAt(rangeEnd);
			}
		}
		
		return crateName;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public Long evaluateTime(QueryPartValue value) {
		
		Long crateValue = null;
		if(value.isNumberOrNumberString()) {

			long timeMillis = value.getAsLong();
			
			crateValue = CFWTimeUnit.valueOf(timeunit).round(timeMillis, step.intValue());
		}
		
		return crateValue;
	}


}
