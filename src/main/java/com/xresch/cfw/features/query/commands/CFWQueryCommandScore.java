package com.xresch.cfw.features.query.commands;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

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
import com.xresch.cfw.features.query.parse.QueryPartArray;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.pipeline.PipelineActionContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandScore extends CFWQueryCommand {
	
	public static final String COMMAND_NAME = "score";
	
	private ArrayList<QueryPart> parts;
	
	private ArrayList<String> groupByFieldnames = new ArrayList<>();
	
	private String scoreFieldname = "score";
	private String detailsFieldname = "scoredetails";
	private LinkedHashMap<String, QueryPartArray> scoringArrayOriginals = new LinkedHashMap<>();
	
	// contains groupID and scoringArrayMap
	private LinkedHashMap<String, LinkedHashMap<String, QueryPartArray> > groupedScoringArraysMap = new LinkedHashMap<>();
	
	private ArrayList<QueryPartAssignment> assignments = new ArrayList<>();

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandScore(CFWQuery parent) {
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
		return "Creates a score based on various evaluations.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" by=<by> scorefield=<scorefield> detailsfield=<scorefield> scoreA=[<weight>, <value>] scoreB=[<weight>, <value>] ...";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 
			"</ul>"
				+ "<li><b>by:&nbsp;</b>(Optional)The names of the fields used to group the scores.</li>" 
				+ "<li><b>scorefield:&nbsp;</b>(Optional)The name of the field where the total score should be stored. (Default: 'score')</li>" 
				+ "<li><b>detailsfield:&nbsp;</b>(Optional)The name of the field where the an object of score details should be stored.(Default: 'scoredetails')</li>" 
				+ "<li><b>[weight, value]:&nbsp;</b>A combination of score weight and the value of the score.</li>"
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
		this.parts = parts;
		
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
	public void initializeAction() throws Exception{
		
		for(int i = 0; i < parts.size(); i++) {
			
			QueryPart currentPart = parts.get(i);
			
			if(currentPart instanceof QueryPartAssignment) {
				//--------------------------------------------------
				// Resolve Fieldname=Function
				QueryPartAssignment assignment = (QueryPartAssignment)currentPart;
				String assignmentName = assignment.getLeftSideAsString(null);
				QueryPart assignmentValuePart = ((QueryPartAssignment) currentPart).getRightSide();
				
				if(assignmentName != null) {
					//--------------------------------------------------
					// By Parameter
					if(assignmentName.trim().equals("by")) {
						QueryPartValue assignmentValue = currentPart.determineValue(null);
						ArrayList<String> fieldnames = assignmentValue.getAsStringArray();
						groupByFieldnames.addAll(fieldnames);
					}
					//--------------------------------------------------
					// Parameter: scorefield
					else if(assignmentName.trim().equals("scorefield")) {
						QueryPartValue assignmentValue = currentPart.determineValue(null);
						scoreFieldname = assignmentValue.getAsString();
						
					}
					//--------------------------------------------------
					// Parameter: detailsfield
					else if(assignmentName.trim().equals("detailsfield")) {
						QueryPartValue assignmentValue = currentPart.determineValue(null);
						detailsFieldname = assignmentValue.getAsString();
					}
					//--------------------------------------------------
					// Any other parameter
					else {
						
						if(assignmentValuePart instanceof QueryPartArray) {
							QueryPartArray array = ((QueryPartArray)assignmentValuePart);
							
							if(array.size() < 2) {
								throw new ParseException(COMMAND_NAME+": Value must be an array of [<weight>, <value>].", -1);
							}
							
							scoringArrayOriginals.put(assignmentName, array);

						}else {
							throw new ParseException(COMMAND_NAME+": Value must be an array of [<weight>, <value>].", -1);
						}
					}
					
				}else {
					throw new ParseException(COMMAND_NAME+": left side of an assignment cannot be null.", -1);
				}
				
				assignments.add((QueryPartAssignment)currentPart);
			}else {
				throw new ParseException(COMMAND_NAME+": Only assignment expressions(key=value) allowed.", -1);
			}
		}
		
		this.fieldnameAdd(scoreFieldname);
		this.fieldnameAdd(detailsFieldname);

	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		//boolean printed = false;
		while(keepPolling()) {
			EnhancedJsonObject record = inQueue.poll();
			
			//----------------------------
			// Create Group String
			String groupID = "";
			
			for(String fieldname : groupByFieldnames) {
				JsonElement element = record.get(fieldname);
				if(element == null || element.isJsonNull()) {
					groupID += "-cfwNullPlaceholder";
				}else {
					groupID += record.get(fieldname).toString();
				}
			}
			
			//----------------------------
			// Create and Get Group
			if(!groupedScoringArraysMap.containsKey(groupID)) {
				JsonObject object = new JsonObject();
				for(String fieldname : groupByFieldnames) {
					
					JsonElement element = record.get(fieldname);
					object.add(fieldname, element);
				}
				
				LinkedHashMap<String, QueryPartArray> newGroup = new LinkedHashMap<>();
				
				// The QueryPartArray has to be cloned for grouping and certain functions(e.g. prev ) to work properly.
				for(Entry<String, QueryPartArray> entry : scoringArrayOriginals.entrySet()) {
					newGroup.put(entry.getKey(), entry.getValue().clone());
				}

				groupedScoringArraysMap.put(groupID, newGroup);
			}
			
			LinkedHashMap<String, QueryPartArray> currentGroup = groupedScoringArraysMap.get(groupID);
					
			//----------------------------
			// Calculate Score
			JsonObject scoreDetails = new JsonObject();
			JsonObject scoreDetailsWeights = new JsonObject();
			JsonObject scoreDetailsScores = new JsonObject();
			scoreDetails.add("scores", scoreDetailsScores);
			scoreDetails.add("weights", scoreDetailsWeights);
			
			BigDecimal sumWeightedScores = BigDecimal.ZERO.setScale(2);
			BigDecimal sumWeights = BigDecimal.ZERO.setScale(2);
			
			for(Entry<String, QueryPartArray> entry : currentGroup.entrySet()) {
				String scoreName = entry.getKey();
				ArrayList<QueryPart> scoreParts = entry.getValue().getAsParts();
				
				BigDecimal scoreWeight = scoreParts.get(0).determineValue(record).getAsBigDecimal();
				BigDecimal scoreValue = scoreParts.get(1)
												  .determineValue(record)
												  .convertFieldnameToFieldvalue(record)
												  .getAsBigDecimal();
				
				scoreDetailsScores.addProperty(scoreName, scoreValue);
				scoreDetailsWeights.addProperty(scoreName, scoreWeight);
				
				sumWeights = sumWeights.add(scoreWeight);
				sumWeightedScores = sumWeightedScores.add( scoreValue.multiply(scoreWeight) );
			}
			
			//----------------------------
			// Add Result to Record
			BigDecimal score = sumWeightedScores.divide(sumWeights, RoundingMode.HALF_UP);
			scoreDetails.addProperty("_sumWeights",sumWeights);
			scoreDetails.addProperty("_sumWeightedScores",sumWeightedScores);
			scoreDetails.addProperty("_score", score);
			
			record.addProperty(scoreFieldname, score);
			record.add(detailsFieldname, scoreDetails);
			
			outQueue.add(record);
		}
		

		this.setDoneIfPreviousDone();
		
	}

}
