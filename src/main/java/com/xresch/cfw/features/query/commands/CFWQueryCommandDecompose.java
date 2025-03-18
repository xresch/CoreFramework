package com.xresch.cfw.features.query.commands;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;

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


/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandDecompose extends CFWQueryCommand {
	
	private static final String COMMAND_NAME = "decompose";
	private static final BigDecimal MINUS_ONE = new BigDecimal(-1);
	
	private ArrayList<QueryPartAssignment> assignmentParts = new ArrayList<>();
	
	private ArrayList<String> groupByFieldnames = new ArrayList<>();
	
	// MUST BE LINKED! following maps contain a groupID plus Record or value
	private LinkedHashMap<String, ArrayList<EnhancedJsonObject>> recordGroupsMap = new LinkedHashMap<>();
	private LinkedHashMap<String, ArrayList<BigDecimal>> valueGroupsMap = new LinkedHashMap<>();

	private String fieldname = null;
	private String nameTrend = "trend";
	private String nameSeason = "seasonal";
	private String nameResidual = "residual";
	private Integer precision = null;
	private Integer period = null;
	private Integer minLag = null;
	private Integer maxLag = null;
	private Boolean multiplicative = true;
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandDecompose(CFWQuery parent) {
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
		return "Decomposes time series into trend, seasonality and residual components.";
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
			  +"<li><b>field:&nbsp;</b>Name of the field which contains the value changepoints should be detected in.</li>"
			  +"<li><b>name:&nbsp;</b>Prefix for the result fieldnames.</li>"
			  +"<li><b>period:&nbsp;</b>The number of datapoints used for the trend as a moving average. (Default: 10)</li>"
			  +"<li><b>precision:&nbsp;</b>The decimal precision of the moving average (Default: 6).</li>"
			  +"<li><b>minlag:&nbsp;</b>The minimum lag used for extracting the seasonality. (Default: period).</li>"
			  +"<li><b>maxlag:&nbsp;</b>The maximum lag used for extracting the seasonality. (Default: 10 * minlag).</li>"
			  +"<li><b>multiplicative:&nbsp;</b>Set to false to use additive model (Default: true).</li>"
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
		String namePrefix = null;
		for(QueryPartAssignment assignment : assignmentParts) {
			
			String assignmentName = assignment.getLeftSideAsString(null);
			QueryPartValue assignmentValue = assignment.determineValue(null);
			
			if(assignmentName != null) {
				assignmentName = assignmentName.trim().toLowerCase();

				if(assignmentName.equals("by")) {
					ArrayList<String> fieldnames = assignmentValue.getAsStringArray();
					groupByFieldnames.addAll(fieldnames);
				}
				else if	 (assignmentName.equals("field")) {			fieldname = assignmentValue.getAsString(); }
				else if	 (assignmentName.equals("name")) {			namePrefix = assignmentValue.getAsString(); }
				else if	 (assignmentName.equals("precision")) {		precision = assignmentValue.getAsInteger(); }
				else if	 (assignmentName.equals("period")) {		period = assignmentValue.getAsInteger(); }
				else if	 (assignmentName.equals("minlag")) {		minLag = assignmentValue.getAsInteger(); }
				else if	 (assignmentName.equals("maxlag")) {		maxLag = assignmentValue.getAsInteger(); }
				else if	 (assignmentName.equals("multiplicative")) {	multiplicative = assignmentValue.getAsBoolean(); }

				else {
					throw new ParseException(COMMAND_NAME+": Unsupported parameter '"+assignmentName+"'", -1);
				}
				
			}
		}
		
		//------------------------------------------
		// Sanitize
		
		if(namePrefix != null) { 
			nameTrend = namePrefix+"_trend";
			nameSeason = namePrefix+"_season";
			nameResidual = namePrefix+"_residual";
		}
		
		if(precision == null) { precision = 6;}
		
		if(period == null ) { period = 10; }
		if(period % 2 > 0 ) { period += 1; }
		if(period < 2 ) { period = 4; }
		
		if(minLag == null ) { minLag = period; }
		if(maxLag == null ) { maxLag = minLag * 10; }

		
		//------------------------------------------
		// Add Detected Fields
		this.fieldnameAdd(nameTrend);
		this.fieldnameAdd(nameSeason);
		this.fieldnameAdd(nameResidual);
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
			if(!recordGroupsMap.containsKey(groupID)) {
				recordGroupsMap.put(groupID, new ArrayList<>());
				valueGroupsMap.put(groupID, new ArrayList<>());
			}
			
			ArrayList<EnhancedJsonObject> recordGroup = recordGroupsMap.get(groupID);
			recordGroup.add(record);
			
			ArrayList<BigDecimal> valueGroup = valueGroupsMap.get(groupID);
			QueryPartValue valuePart = QueryPartValue.newFromJsonElement(record.get(fieldname));

			valueGroup.add(valuePart.getAsBigDecimal());
			
		}
		
		//------------------------------------
		// Calculate Values
		if(this.isPreviousDone()) {
			
			//------------------------------------
			// Iterate the Groups
			ArrayList<ArrayList<EnhancedJsonObject>> recordGroups = new ArrayList<>(recordGroupsMap.values() );
			ArrayList<ArrayList<BigDecimal>> valueGroups = new ArrayList<>(valueGroupsMap.values());
			for(int k = 0; k < recordGroupsMap.size(); k++) {
								
				List<EnhancedJsonObject> currentRecords = recordGroups.get(k);
				List<BigDecimal> currentValues = valueGroups.get(k);
				
				//------------------------------------
				// Skip Leading Null Values
				for(int i = 0 ; i < currentValues.size(); i++) {
										
					BigDecimal value = currentValues.get(i);
					
					//---------------------------
					// Check Nulls
					if(value == null) { 
						EnhancedJsonObject record = currentRecords.get(i);
						record.add(nameTrend, JsonNull.INSTANCE);
						record.add(nameSeason, JsonNull.INSTANCE);
						record.add(nameResidual, JsonNull.INSTANCE);
						outQueue.add(record);
						continue;
					}else {
						// skip all leading null values
						currentRecords = currentRecords.subList(i, currentRecords.size());
						currentValues = currentValues.subList(i, currentValues.size());
						break;
					}
				}
				
				//------------------------------------
				// Calculate Components

				CFW.Math.forwardFill(currentValues);
				ArrayList<BigDecimal> trend = CFW.Math.bigMovAvgArray(currentValues, period, precision, BigDecimal.ZERO);
				ArrayList<BigDecimal> seasonalityResidual = CFW.Math.decomposeSeasonalityResidual(currentValues, trend, multiplicative);
				ArrayList<BigDecimal> seasonality = CFW.Math.decomposeSeasonality(currentValues, seasonalityResidual, minLag, maxLag, multiplicative);
				ArrayList<BigDecimal> residual = CFW.Math.decomposeResidual(currentValues, trend, seasonality, multiplicative);

				//------------------------------------
				// Add to Records
				for(int r = 0 ; r < currentRecords.size(); r++) {
					
					EnhancedJsonObject record = currentRecords.get(r);
					record.addProperty(nameTrend, trend.get(r));
					record.addProperty(nameSeason, seasonality.get(r));
					record.addProperty(nameResidual, residual.get(r));
					
					outQueue.add(record);
				}
				
				
				
			}
			

			this.setDone();
		}
		
		
	
	}
	

}
