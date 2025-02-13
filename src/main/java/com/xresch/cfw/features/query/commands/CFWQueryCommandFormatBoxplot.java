package com.xresch.cfw.features.query.commands;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
import com.xresch.cfw.features.query._CFWQueryCommon;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineActionContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandFormatBoxplot extends CFWQueryCommand {
	
	private static final String GROUP_ID = "groupID";

	private static final String COMMAND_NAME = "formatboxplot";

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandFormatCSS.class.getName());
	
	private ArrayList<QueryPartAssignment> assignmentParts = new ArrayList<QueryPartAssignment>();
	private QueryPartAssignment colorPart = null; 
	
	private ArrayList<String> groupByFieldnames = new ArrayList<>();
	
	// Maps with GroupID and value
	private LinkedHashMap<String, BigDecimal> smallestMinMap = new LinkedHashMap<>();
	private LinkedHashMap<String, BigDecimal> biggestMaxMap = new LinkedHashMap<>();

	// These all represent fieldnames
	private String field = "Boxplot"; 
	private String min = null; 
	private String low = null; 
	private String median = null; 
	private String high = null; 
	private String max = null; 
	
	private String width = "100%"; 
	private String height = "20px"; 
	private Boolean relative = true; 
	private Boolean epoch = false; 
	
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
		return COMMAND_NAME+" field=<field> min=<min> low=<low> median=<median> high=<high> max=<max> ...";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return """
			  <ul>
			  	<li><b>by:&nbsp;</b>The name of fields that should be used to group. Groups will have the same 'start' and 'end' values of the box plot.</li>
			  	<li><b>field:&nbsp;</b>The name of the new field that will contain the boxplot.</li>
			  	<li><b>color:&nbsp;</b>(Optional)The CSS color that should be applied to the boxplot.</li>
			  	<li><b>relative:&nbsp;</b>(Optional)Toggle if the boxplots should be positioned relative to the overall min and max values. (Default: true)</li>
			  	<li><b>epoch:&nbsp;</b>(Optional)Set to true if the values are milliseconds in epoch time. Will format values to timestamp in popovers. (Default: false)</li>
			  	<li><b>min:&nbsp;</b>The name of the field that contains the min value.</li>
			  	<li><b>low:&nbsp;</b>The name of the field that contains the low value.</li>
			  	<li><b>median:&nbsp;</b>(Optional)The name of the field that contains the median value.</li>
			  	<li><b>high:&nbsp;</b>The name of the field that contains the high value.</li>
			  	<li><b>max:&nbsp;</b>The name of the field that contains the max value.</li>
				<li><b>width:&nbsp;</b>(Optional) CSS width attribute to control the size of the display. (Default: 100%)</li>
			  	<li><b>height:&nbsp;</b>(Optional) CSS height attribute to control the size of the display. (Default: 20px)</li>
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
			QueryPartValue rightSide = zePart.getRightSide().determineValue(null);
			
			switch(partName) {
				case "by":
					ArrayList<String> fieldnames = rightSide.getAsStringArray();
					groupByFieldnames.addAll(fieldnames);
				break;
				case "field":		field = rightSide.getAsString(); break;
				case "relative":	relative = rightSide.getAsBoolean(); break;
				case "epoch":		epoch = rightSide.getAsBoolean(); break;
				case "width":		width = rightSide.getAsString(); break;
				case "height":		height = rightSide.getAsString(); break;
				case "color":		colorPart = zePart; break;
				case "min":			min = rightSide.getAsString(); break;
				case "low":			low = rightSide.getAsString(); break;
				case "median":		median = rightSide.getAsString(); break;
				case "high":		high = rightSide.getAsString(); break;
				case "max":			max = rightSide.getAsString(); break;
				default:
					throw new IllegalArgumentException(COMMAND_NAME+": unknown parameter '"+partName+"'.");
					
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
			throw new IllegalArgumentException(COMMAND_NAME+": Please specify either of the parameters 'max' or 'high'.");
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
				
				//----------------------------
				// Create Group String
				String groupID = record.createGroupIDString(groupByFieldnames);
				record.addMetadata(GROUP_ID, groupID); // store it to not have to generate it twice
				
				//----------------------------
				// Create and Get Group
				if(!smallestMinMap.containsKey(groupID)) {
					smallestMinMap.put(groupID, null);
					biggestMaxMap.put(groupID, null);
				}
				
				BigDecimal smallestMin = smallestMinMap.get(groupID);
				BigDecimal biggestMax = biggestMaxMap.get(groupID);
				
				//--------------------------
				// Find the smallest Min Value
				JsonElement minElement = record.get(min);
				QueryPartValue minValue = QueryPartValue.newFromJsonElement(minElement);
				
				if(minValue.isNumberOrNumberString()){
					BigDecimal minDecimal = minValue.getAsBigDecimal();
					if(smallestMin == null
					|| smallestMin.compareTo(minDecimal) > 0) {
						smallestMinMap.put(groupID, minDecimal);
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
						biggestMaxMap.put(groupID, maxDecimal);
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
				specialObject.addProperty("epoch", epoch);
				specialObject.addProperty("width", width);
				specialObject.addProperty("height", height);
				
				JsonObject valuesObject = new JsonObject();
				specialObject.add("values", valuesObject);
				
				if(relative) {
					BigDecimal smallestMin = smallestMinMap.get(record.getMetadata(GROUP_ID));
					valuesObject.addProperty("start", smallestMin);
				}
				
				valuesObject.add("min", record.get(min));
				valuesObject.add("low", record.get(low));
				valuesObject.add("median", record.get(median));
				valuesObject.add("high", record.get(high));
				valuesObject.add("max", record.get(max));
				
				if(relative) {
					BigDecimal biggestMax = biggestMaxMap.get(record.getMetadata(GROUP_ID));
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
