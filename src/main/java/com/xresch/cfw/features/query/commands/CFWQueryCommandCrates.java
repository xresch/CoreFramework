package com.xresch.cfw.features.query.commands;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.TreeSet;

import com.google.common.base.Strings;
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
	public TreeSet<String> getTags(){
		TreeSet<String> tags = new TreeSet<>();
		tags.add(_CFWQueryCommon.TAG_ANALYTICS);
		tags.add(_CFWQueryCommon.TAG_STATS);
		return tags;
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
		return COMMAND_NAME+" <param>=<value> [<param>=<value> ...]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<ul>"
			  +"<li><b>by:&nbsp;</b>Name of the field which should be used for determining the crate.</li>"
			  +"<li><b>type:&nbsp;</b>(Optional)The type of the evaluation, one of the following(Default: number):"
				  +"<ul>"
				  +"<li><b>number:&nbsp;</b>Make crates based on number values. Create will be a number range.</li>"
				  +"<li><b>time:&nbsp;</b>Make crates based on time values. Results in epoch milliseconds, not a range.</li>"
				  +"<li><b>alpha:&nbsp;</b>Make creates based on values from A-Z. Crates will be a range in the alphabet(e.g. A-C, D-F etc...)</li>"
				  +"</ul>"
			  +"</li>"
			  +"<li><b>step:&nbsp;</b>(Optional)The steps used for the range of the crate, effect depends on type(Default: 10):"
				  +"<ul>"
					  +"<li><b>number:&nbsp;</b>Step will be the span of consecutive number ranges, e.g. 0-10 / 11-20 / 21-30 / (...).</li>"
					  +"<li><b>time:&nbsp;</b>Step will be the number of units of time which the crate should be rounded too(see also timeunit parameter).</li>"
					  +"<li><b>alpha:&nbsp;</b>Step will be the span in the alphabet, e.g step&eq;4 will result in A-C / D-F / G-I / (...).</li>"
					  +"</ul>"
				  +"</li>"
			  +"<li><b>multiplier:&nbsp;</b>(Optional) When type is number, the end of the range will be multiplied for each successive crate. E.g. step&eq;10 and multiplier&eq;2 will result in creates: 0-10 / 11-20 / 21 - 40 / 41 -80 / ... .</li>"
			  +"<li><b>timeunit:&nbsp;</b>(Optional) The time unit used for type 'time'. Defines the unit for the steps parameter, one of the following(Default: 'm'):"
			  	+ CFWTimeUnit.getOptionsHTMLList()
			  + "</li>"
			  +"<li><b>name:&nbsp;</b>The name of the target field to put the crate value(Default: 'CRATE').</li>"
			  +"<li><b>maxgroups:&nbsp;</b>The Maximum number of crates for the type 'number'(Default: 1000).</li>"
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
		
		if(name == null) { name = "CRATE";}
		if(step == null || step.compareTo(BigDecimal.ZERO) == 0 ) { step = BigDecimal.TEN; }
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
				
				if(i >= (maxgroups-2) ) {
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
