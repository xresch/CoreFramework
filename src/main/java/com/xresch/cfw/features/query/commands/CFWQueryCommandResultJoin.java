package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQueryResult;
import com.xresch.cfw.features.query.CFWQueryResultList;
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.LeftRightEvaluatable;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartArray;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineActionContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandResultJoin extends CFWQueryCommand {
	
	private static final String METADATA_ISMATCHED = "ResultJoin-matched";

	public static final String COMMAND_NAME = "resultjoin";

	private ArrayList<QueryPartAssignment> assignmentParts = new ArrayList<QueryPartAssignment>();

	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandResultJoin.class.getName());
	
	CFWQuerySource source = null;
	
	private LeftRightEvaluatable onConditionSlow = null;
	private String onConditionFastLeft = null;
	private String onConditionFastRight = null;
	private boolean remove = true;
	
	
	private CFWQueryCommandResultJoinType joinType = CFWQueryCommandResultJoinType.left;
	private String leftName = null;
	private String rightName = null;
	
	ArrayList<String> resultnames = new ArrayList<>();
	HashSet<String> encounters = new HashSet<>();
	
	public enum CFWQueryCommandResultJoinType {

		  inner("Returns records that have matching values in both results.")
		, left("Returns all records from the left result, and the matched records from the right result.")
		, right("Returns all records from the right result, and the matched records from the left result.")
		//, full("Returns all records when there is a match in either left or right table")
		;
		
		//==============================
		// Caches
		private static TreeSet<String> enumNames = null;		
		
		//==============================
		// Fields
		private String shortDescription;

		private CFWQueryCommandResultJoinType(String shortDescription) {
			this.shortDescription = shortDescription;
		}
				
		public String shortDescription() { return this.shortDescription; }
		
		/********************************************************************************************
		 * Returns a set with all names
		 ********************************************************************************************/
		public static TreeSet<String> getNames() {
			if(enumNames == null) {
				enumNames = new TreeSet<>();
				
				for(CFWQueryCommandResultJoinType unit : CFWQueryCommandResultJoinType.values()) {
					enumNames.add(unit.name());
				}
			}
			return enumNames;
		}
		
		/********************************************************************************************
		 * 
		 ********************************************************************************************/
		public static boolean has(String enumName) {
			return getNames().contains(enumName);
		}

	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandResultJoin(CFWQuery parent) {
		super(parent);
	}

	/***********************************************************************************************
	 * Return the command name and aliases.
	 * The first entry in the array will be used as the main name, under which the documentation can
	 * be found in the manual. All other will be used as aliases.
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
		return "Joins two results based on field values.";
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return COMMAND_NAME+" on=<onCondition> [join=<joinType>] [remove=<doRemove>] [left=<resultNameLeft>] [right=<resultNameRight>]";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return  "<ul>"
				+"<li><b>onCondition:&nbsp;</b>The condition, if evaluates to true, the records are joined together.</li>"
				+"<li><b>joinType:&nbsp;</b>(Optional) The type of the join, either, 'inner', 'left' or 'right'. </li>"
				+"<li><b>remove:&nbsp;</b>(Optional) Toggle if the the original results should be removed. (Default: true) </li>"
				+"<li><b>resultNameLeft:&nbsp;</b>(Optional) The name of the left result. If this is omitted, the second last result will be used.</li>"
				+"<li><b>resultNameRight:&nbsp;</b>(Optional) The name of the right result. If this is omitted, the last result will be used.</li>"
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
	public void initializeAction() throws Exception{
		
		for(int i = 0; i < assignmentParts.size(); i++) {
			
			QueryPart currentPart = assignmentParts.get(i);
			
			if(currentPart instanceof QueryPartAssignment) {
				//--------------------------------------------------
				// Resolve Fieldname=Function
				QueryPartAssignment assignment = (QueryPartAssignment)currentPart;
				String assignmentName = assignment.getLeftSideAsString(null);
				QueryPart assignmentValuePart = ((QueryPartAssignment) currentPart).getRightSide();
				
				if(assignmentName != null) {
					
					assignmentName = assignmentName.trim().toLowerCase();
					switch(assignmentName) {
					
						//-------------------------------------
						// 
						//-------------------------------------
						case "on":
							if(assignmentValuePart instanceof QueryPartValue ) {
								onConditionFastLeft =  assignmentValuePart.determineValue(null).getAsString();
								onConditionFastRight = onConditionFastLeft;
							}else if(assignmentValuePart instanceof QueryPartArray ) {
								QueryPartArray arrayPart = (QueryPartArray)assignmentValuePart;
								ArrayList<QueryPart> array = arrayPart.getAsParts();
								if(array.size() > 0) {  onConditionFastLeft =  array.get(0).determineValue(null).getAsString(); }
								if(array.size() > 1) {  onConditionFastRight =  array.get(1).determineValue(null).getAsString(); }
							}else if(assignmentValuePart instanceof LeftRightEvaluatable) {
								onConditionSlow = (LeftRightEvaluatable)assignmentValuePart; 
							}else {
								throw new ParseException(COMMAND_NAME+": value for on-parameter must be an array or expression.", -1);
							}
							break;
						
						//-------------------------------------
						// 
						//-------------------------------------
						case "join":
							String join = currentPart.determineValue(null).getAsString(); 
							if(join == null) { join = "left"; }
							join = join.trim().toLowerCase();
							if(CFWQueryCommandResultJoinType.has(join)) {
								joinType = CFWQueryCommandResultJoinType.valueOf(join);
							}else {
								throw new ParseException(
										COMMAND_NAME
										+": The value '"+joinType+"' for parameter 'join' is not supported: "
										+ CFW.JSON.toJSON( CFWQueryCommandResultJoinType.getNames() )
										, -1);
							}
							break;
						
						//-------------------------------------
						// 
						//-------------------------------------
						case "left":
							leftName = currentPart.determineValue(null).getAsString(); 
							break;
						
						//-------------------------------------
						// 
						//-------------------------------------	
						case "right":
							rightName = currentPart.determineValue(null).getAsString(); 
							break;
							
							//-------------------------------------
							// 
							//-------------------------------------	
						case "remove":
							remove = currentPart.determineValue(null).getAsBoolean(); 
							break;
						
						default:
							throw new ParseException(COMMAND_NAME+": Unknown parameter: '"+assignmentName+".", -1);
					}

				}else {
					throw new ParseException(COMMAND_NAME+": left side of an assignment cannot be null.", -1);
				}
				
			}else {
				throw new ParseException(COMMAND_NAME+": Only assignment expressions(key=value) allowed.", -1);
			}
		}
		
		//-------------------------------------
		// 
		//-------------------------------------
		if(onConditionSlow == null
		&& onConditionFastLeft == null) {
			throw new ParseException(COMMAND_NAME+": Please provide a valid value for the on-parameter.", -1);
		}
	}
		
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void execute(PipelineActionContext context) throws Exception {
		
		//------------------------------
		// Read Records of current Query
		while(keepPolling()) {
			outQueue.add(inQueue.poll());
		}
		
		//------------------------------
		// Read Records of current Query
		if(isPreviousDone() && inQueue.isEmpty()) {
			CFWQueryResult left = null; 
			CFWQueryResult right = null; 
			
			CFWQueryResultList results = this.getQueryContext().getResultList();
			if(results.size() < 2) {
				throw new IllegalStateException(COMMAND_NAME+": There must be at least two results to be able to join");
			}
			
			//------------------------------
			// Get Left Result
			if(leftName != null) {
				left = this.getQueryContext().getResultByName(leftName);
			}else {
				left = results.get(results.size()-2);
			}
			
			if(left  == null) {
				throw new IllegalStateException(COMMAND_NAME+": Left result could not be found.");
			}
			
			//------------------------------
			// Get Right Result
			if(rightName != null) {
				right = this.getQueryContext().getResultByName(rightName);
			}else {
				right = results.get(results.size()-1);
			}
			
			if(right  == null) {
				throw new IllegalStateException(COMMAND_NAME+": Right result could not be found.");
			}
			
			//----------------------------
			// Join Results
			this.fieldnameAddAll(left.getDetectedFields());	
			this.fieldnameAddAll(right.getDetectedFields());	
			
			if(onConditionSlow == null) {
				doJoinFast(left, right);
			}else {
				doJoinSlow(left, right);
			}
			
			//----------------------------
			// Remove Merged Results
			
			if(remove) {
				results.removeResult(right);
				results.removeResult(left);
			}
		
			this.setDone();
		}
		
	}
	
	/***********************************************************************************************
	 * Joins records by evaluating the condition, using a HashMap to find respective values.
	 ***********************************************************************************************/
	private void doJoinFast(CFWQueryResult left, CFWQueryResult right) throws Exception {
		
		//-------------------------------------------
		// Make sure we have both left and right Condition
		if(onConditionFastRight == null) {
			onConditionFastRight = onConditionFastLeft;
		}
		
		//-------------------------------------------
		// Base And Matching
		ArrayList<EnhancedJsonObject> baseRecords = left.getRecords();
		ArrayList<EnhancedJsonObject> matchingRecords = right.getRecords();
		String keyNameBase = onConditionFastLeft;
		String KeyNameMatching = onConditionFastRight;
		
		if(CFWQueryCommandResultJoinType.right.equals(joinType) ) {
			baseRecords = right.getRecords();
			matchingRecords = left.getRecords();
			keyNameBase = onConditionFastRight;
			KeyNameMatching = onConditionFastLeft;
		}

		//----------------------------
		// Create Base Map
		LinkedHashMap<String, ArrayList<EnhancedJsonObject> > baseMap = new LinkedHashMap<>();
		for(int i = 0; i < baseRecords.size() ; i++) {
			
			EnhancedJsonObject baseObject = baseRecords.get(i);		
			String key = CFW.JSON.toString( baseObject.get(keyNameBase) );
			
			if(!baseMap.containsKey(key)) {
				baseMap.put(key, new ArrayList<EnhancedJsonObject>() );
			}
			
			baseMap.get(key).add(baseObject);
		}
		
		//----------------------------
		// Iterate Matching
		for(int k = 0; k < matchingRecords.size() ; k++) {
			
			EnhancedJsonObject matchObject = matchingRecords.get(k);
			String keyMatch = CFW.JSON.toString( matchObject.get(KeyNameMatching) );
			
			if(baseMap.containsKey(keyMatch)) {
				for(EnhancedJsonObject current : baseMap.get(keyMatch)) {
					
					current.addMetadata(METADATA_ISMATCHED, true);
					
					EnhancedJsonObject clone = current.clone();
					clone.addAll(matchObject); 
					
					outQueue.add(clone);
				}
			}
		}
		
		//----------------------------
		// Handle non Matching Records
		if( !CFWQueryCommandResultJoinType.inner.equals(joinType) ) {
			
			for(ArrayList<EnhancedJsonObject> currentArray : baseMap.values()) {
				
				for(EnhancedJsonObject currentObject : currentArray) {
					
					Object hasMatched = currentObject.getMetadata(METADATA_ISMATCHED);

					if(hasMatched == null) {
						outQueue.add(currentObject);
					}
				}
			}
		}
			
		
	}

	/***********************************************************************************************
	 * Joins records by evaluating the condition, each record against any other record.
	 ***********************************************************************************************/
	private void doJoinSlow(CFWQueryResult left, CFWQueryResult right) throws Exception {
		
		ArrayList<EnhancedJsonObject> baseRecords = left.getRecords();
		ArrayList<EnhancedJsonObject> matchingRecords = right.getRecords();
		if(CFWQueryCommandResultJoinType.right.equals(joinType) ) {
			baseRecords = right.getRecords();
			matchingRecords = left.getRecords();
		}
		
		//----------------------------
		// Iterate Base
		for(int i = 0; i < baseRecords.size() ; i++) {
			
			EnhancedJsonObject baseObject = baseRecords.get(i);
			
			//----------------------------
			// Iterate Matching
			boolean hasMatched = false;
			
			for(int k = 0; k < matchingRecords.size() ; k++) {
				
				EnhancedJsonObject matchObject = matchingRecords.get(k);
				boolean evalResult = false;
				switch(joinType) {
					case inner:
					case left:	evalResult = onConditionSlow.evaluateLeftRightValues(baseObject, matchObject).getAsBoolean();
								break;
					
					case right: evalResult = onConditionSlow.evaluateLeftRightValues(matchObject, baseObject).getAsBoolean();
								break;
					
					default:	break;
				}

				if(evalResult) {
					
					hasMatched = true;
					
					EnhancedJsonObject clone = baseObject.clone();
					clone.addAll(matchObject); 
					outQueue.add(clone);

				}

			}
			
			if( !hasMatched
			&& !CFWQueryCommandResultJoinType.inner.equals(joinType)) {
				outQueue.add(baseObject);
			}
			
		}
	}

}
