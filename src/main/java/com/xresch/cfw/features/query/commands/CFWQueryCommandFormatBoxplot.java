package com.xresch.cfw.features.query.commands;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineActionContext;
import com.xresch.cfw.utils.json.CFWJson;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandFormatBoxplot extends CFWQueryCommand {
	
	private static final String COMMAND_NAME = "formatboxplot";

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandFormatCSS.class.getName());
	
	private ArrayList<QueryPartAssignment> assignmentParts = new ArrayList<QueryPartAssignment>();
	private QueryPartAssignment colorPart = null; 
	
	// These all represent fieldnames
	private String field = "boxplot"; 
	private String min = null; 
	private String low = null; 
	private String median = null; 
	private String high = null; 
	private String max = null; 
	
	private Boolean relative = false; 
	
	private ArrayList<EnhancedJsonObject> objectList = new ArrayList<>();
	

	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandFormatBoxplot(CFWQuery parent) {
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
		return "Formats the specified fields with the defined css style.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" field=<field> min=<min> low=<low> median=<median> high=<high> max=<max>";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return """
			  <ul>
			  	<li><b>field:&nbsp;</b>Array of the fieldnames these display settings should be applied too.</li>
			  	<li><b>style:&nbsp;</b>The CSS style that should be applied to the field.</li>
			  </ul>
			  """
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
		//------------------------------------------
		for(QueryPartAssignment zePart : assignmentParts) {
			String partName = zePart.getLeftSideAsString(null).toLowerCase();
			QueryPart rightSide = zePart.getRightSide();
			
			if(partName.equals("field")) { 			field = rightSide.determineValue(null).getAsString(); }
			else if(partName.equals("min")) {		min = rightSide.determineValue(null).getAsString(); }
			else if(partName.equals("low")) {		low = rightSide.determineValue(null).getAsString(); }
			else if(partName.equals("median")) {	median = rightSide.determineValue(null).getAsString(); }
			else if(partName.equals("high")) {		high = rightSide.determineValue(null).getAsString(); }
			else if(partName.equals("max")) {		max = rightSide.determineValue(null).getAsString(); }
			else if(partName.equals("relative")) {	relative = rightSide.determineValue(null).getAsBoolean(); }
			else if(partName.equals("color")){
				colorPart = zePart;
			}
			else {

				continue;
			}
		}
		
		//------------------------------------------
		// Sanitize
		//------------------------------------------
		if(Strings.isNullOrEmpty(field)) { 	field = "boxplot"; }
		if(relative == null) { 	relative = false; }
		
		//------------------------
		// Check min is set
		if(Strings.isNullOrEmpty(min) 
		&& Strings.isNullOrEmpty(low) ) {
			throw new IllegalArgumentException(COMMAND_NAME+": Please specify either of the parameters 'min' or 'low'.");
		}
		if(Strings.isNullOrEmpty(min)) {
			min = low;
		}
		
		//------------------------
		// Check max is set
		if(Strings.isNullOrEmpty(max) 
		&& Strings.isNullOrEmpty(high) ) {
			throw new IllegalArgumentException(COMMAND_NAME+": Please specify either of the parameters 'min' or 'low'.");
		}
		if(Strings.isNullOrEmpty(max)) {
			max = high;
		}
		
		CFWQueryCommandFormatField.addFormatterByName(this.getQueryContext(), field, "special"); 
		this.fieldnameAdd(field);
		
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		BigDecimal smallestMin = null;
		BigDecimal biggestMax = null;
		//-------------------------------------
		// Fetch All Before Processing
		while(keepPolling()) {
			
			//--------------------------
			// Get Record
			EnhancedJsonObject record = inQueue.poll();
			objectList.add(record);
			
			//--------------------------
			// Process Relative
			if(relative) {
				
				//--------------------------
				// Find the smallest Min Value
				JsonElement minElement = record.get(min);
				QueryPartValue minValue = QueryPartValue.newFromJsonElement(minElement);
				
				if(minValue.isNumberOrNumberString()){
					BigDecimal minDecimal = minValue.getAsBigDecimal();
					if(smallestMin == null
					|| smallestMin.compareTo(minDecimal) > 0) {
						smallestMin = minDecimal;
					}
				}
				
				//--------------------------
				// Find the biggest Max Value
				JsonElement maxElement = record.get(max);
				QueryPartValue maxValue = QueryPartValue.newFromJsonElement(maxElement);
				
				if(maxValue.isNumberOrNumberString()){
					BigDecimal maxDecimal = maxValue.getAsBigDecimal();
					if(biggestMax == null
					|| biggestMax.compareTo(maxDecimal) < 0) {
						biggestMax = maxDecimal;
					}
				}
			}
		}
		
		if(isPreviousDone()) {

			for(EnhancedJsonObject record : objectList) {

				//-------------------------------------
				// Get Values
				String color = "";
				if(colorPart != null) {
					color = colorPart.determineValue(record).getAsString(); 
				}
										
				//-------------------------------------
				// Create object for Special Formatter
				JsonObject specialObject = new JsonObject();
	
				specialObject.addProperty("format", "boxplot");
				specialObject.addProperty("color", color);
				
				JsonObject valuesObject = new JsonObject();
				specialObject.add("values", valuesObject);
				
				valuesObject.add("min", record.get(min));
				valuesObject.add("low", record.get(low));
				valuesObject.add("median", record.get(median));
				valuesObject.add("high", record.get(high));
				valuesObject.add("max", record.get(max));
				
				if(relative) {
					valuesObject.addProperty("start", smallestMin);
					valuesObject.addProperty("end", biggestMax);
				}
				
				//-------------------------------------
				// Replace Value 
				record.add(field, specialObject);
	
				outQueue.add(record);
				
			}
			
			this.setDoneIfPreviousDone();
		}
	}

}
