package com.xresch.cfw.features.query.commands;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryResult;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;


/************************************************************************************************************
 * This class provides the methods which are used to compare two CSV-Files.
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryCommandResultCompareMethods {
	
	private static final Logger logger = CFWLog.getLogger(CFWQueryCommandResultCompareMethods.class.getName());
	
	private ArrayList<String> identifierFields = null;
	private String toplevelField = null;
	private String labelOld = "_A";
	private String labelYoung = "_B";
	private String labelDiff = "_Diff";
	private String labelDiffPercent = "_%";
	
	private LinkedHashSet<String> detectedFields = new LinkedHashSet<>();
	private HashSet<String> fieldHasPercentage = new HashSet<>();
	private boolean makeIdentifierUnique = true;
	
	boolean compareNumbersAbsolute = true;
	boolean compareNumbersDiffPercent = true;
	boolean compareBooleans = true;
	boolean compareStrings = true;
	boolean compareArrays = true;
	boolean compareObjects = true;
	boolean doSort = false;
	
	public CFWQueryCommandResultCompareMethods() {
		
	}
	
	/***********************************************************************************************
	 * Set the fields used for identifying a record in the older record.
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandResultCompareMethods identifierFields(ArrayList<String> identifierFields) {
		this.identifierFields = identifierFields;
		return this;
	}
	
	/***********************************************************************************************
	 * Set the fields used for identifying a record.
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandResultCompareMethods makeIdentifierUnique(boolean makeIdentifierUnique) {
		this.makeIdentifierUnique = makeIdentifierUnique;
		return this;
	}
	
	/***********************************************************************************************
	 * Set the label used as a postfix for the columns of the older data.
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandResultCompareMethods labelOld(String labelOld) {
		this.labelOld = labelOld;
		return this;
	}
	
	/***********************************************************************************************
	 * Set the label used as a postfix for the columns of the younger data.
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandResultCompareMethods labelYoung(String labelYoung) {
		this.labelYoung = labelYoung;
		return this;
	}
	
	/***********************************************************************************************
	 * Set the label used as a postfix for the columns of difference.
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandResultCompareMethods labelDiff(String labelDiff) {
		this.labelDiff = labelDiff;
		return this;
	}
	/***********************************************************************************************
	 * Set the label used as a postfix for the columns of percent difference.
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandResultCompareMethods labelDiffPercent(String labelDiffPercent) {
		this.labelDiffPercent = labelDiffPercent;
		return this;
	}
	
	/***********************************************************************************************
	 * Set if numbers values should have absolute comparison.
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandResultCompareMethods doCompareNumbersAbsolute(boolean value) {
		this.compareNumbersAbsolute = value;
		return this;
	}
	
	/***********************************************************************************************
	 * Set if numbers values should have percent difference comparison.
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandResultCompareMethods doCompareNumbersDiffPercent(boolean value) {
		this.compareNumbersDiffPercent = value;
		return this;
	}
	
	/***********************************************************************************************
	 * Set if strings / objects / array values should be compared.
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandResultCompareMethods doCompareBooleans(boolean value) {
		this.compareBooleans = value;
		return this;
	}
	
	/***********************************************************************************************
	 * Set if strings / objects / array values should be compared.
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandResultCompareMethods doCompareStrings(boolean value) {
		this.compareStrings = value;
		return this;
	}
	
	/***********************************************************************************************
	 * Set if strings / objects / array values should be compared.
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandResultCompareMethods doCompareArrays(boolean value) {
		this.compareArrays = value;
		return this;
	}
	
	/***********************************************************************************************
	 * Set if strings / objects / array values should be compared.
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandResultCompareMethods doCompareObjects(boolean value) {
		this.compareObjects = value;
		return this;
	}
	
	/***********************************************************************************************
	 * Set the fields used for identifying a record.
	 * 
	 ***********************************************************************************************/
	public CFWQueryCommandResultCompareMethods toplevelFieldField(String toplevelField) {
		this.toplevelField = toplevelField;
		return this;
	}
	
	/***********************************************************************************************
	 * This method will initialize the columns which are used as identifiers in the comparison.
	 * Then it will decide based on the given command line arguments how the files will be compared.
	 * 
	 * @param olderResult the object with the CFWQueryResult which should be considered as older in the 
	 *                       comparison
	 * @param youngerResult the object with the CFWQueryResult which should be considered as younger in 
	 *                       the comparison
	 ***********************************************************************************************/
	public CFWQueryResult compareQueryResults(CFWQueryResult olderResult, CFWQueryResult youngerResult){
		JsonArray olderArray = olderResult.getRecordsAsJsonArray();
		JsonArray youngerArray = youngerResult.getRecordsAsJsonArray();
		JsonArray comparison = compareJsonObjectArrays(olderArray, youngerArray);
		CFWQueryResult comparedResult = 
				new CFWQueryResult(youngerResult.getQueryContext())
					.setRecords(comparison)
					.setDetectedFields(detectedFields)
					;
		
		return comparedResult;
	}
	
	/***********************************************************************************************
	 * This method will initialize the columns which are used as identifiers in the comparison.
	 * Then it will decide based on the given command line arguments how the files will be compared.
	 * 
	 * @param olderArray the object with the CFWQueryResult which should be considered as older in the 
	 *                       comparison
	 * @param youngerArray the object with the CFWQueryResult which should be considered as younger in 
	 *                       the comparison
	 ***********************************************************************************************/
	public JsonArray compareJsonObjectArrays(JsonArray olderArray, JsonArray youngerArray){

		JsonArray compareResults = new JsonArray();
		//---------------------------------------------------
		// Initialize identifier Fields	
		//---------------------------------------------------
		if(identifierFields == null) {
			identifierFields = new ArrayList<>();
		}
		
		addFirstMemberNameFromArray(identifierFields, youngerArray);
		
		//---------------------------------------------------
		// Validation
		//---------------------------------------------------
		
		//compare only if the identifier Column could be found in both files
		if(identifierFields.isEmpty() ) {
			JsonObject errorMessage = new JsonObject();
			errorMessage.addProperty("message", "Could not compare as no field identifier could be found in one dataset.");
		}
		
		//---------------------------------------------------
		// Decide how to Compare
		//---------------------------------------------------
		
		compareResults = this.doCompareDefault(olderArray, youngerArray);
		
//		if(toplevelField == null){
//		
//			if(!arguments.hasColumnDefinitions()){
//				compareResults = this.doDefaultComparision(olderArray, youngerArray, identifierColumnOlder, identifierColumnYounger);
//			}else{
//				compareResults = this.doComparisionByManualDefinition(olderArray, youngerArray, identifierColumnOlder, identifierColumnYounger);
//			}
//		}else{
//			compareResults = this.doComparisionWithTopLevel(olderArray, youngerArray, identifierColumnOlder, identifierColumnYounger);
//		}
		
		return compareResults;

	}
	
	/***********************************************************************************************
	 * If the fieldnameArray is empty, finds the first member name from the given JsonArray and adds it to the fieldnameArray.
	 * 
	 * @param fieldnameArray Array containing field names
	 * @param objectArray array containing JsonObjects
	 ***********************************************************************************************/
	public void addFirstMemberNameFromArray(ArrayList<String> fieldnameArray, JsonArray objectArray) {
		// Extract ID fieldname from first encountered JsonObject in array
		if(fieldnameArray.isEmpty() && !objectArray.isEmpty()){
			JsonElement firstElement = null;
			int i = -1;
			
			// find JsonObject in first 100 entries
			while( i++ <= 100 
			    && i < objectArray.size()
			    && (firstElement == null || !firstElement.isJsonObject())
			) {
				firstElement = objectArray.get(i);
			}
			
			// get first Fieldname
			if(firstElement != null) {
				for (Entry<String, JsonElement> entry : firstElement.getAsJsonObject().entrySet()) {
					fieldnameArray.add(entry.getKey());
					break;
				}
			}
		}
	}
	
	

	/***********************************************************************************************
	 * This method is used when the argument "-column.toplevel" is provided.
	 * It will split the provided CFWQueryResult in parts, with the toplevelField as a "delimiter".
	 * The parts are compared if in both files a toplevelField with the same identifier is found.
	 * The result of all part comparisons is merged in one result file.
	 * 
	 * @param olderResult the object with the CFWQueryResult which should be considered as older in the 
	 *        comparison
	 * @param youngerResult the object with the CFWQueryResult which should be considered as younger in 
	 *        the comparison
	 * @param identifierColumnOlder the column of the older CFWQueryResult which is used to identify 
	 *        the record   
	 * @param identifierColumnYounger the column of the older CFWQueryResult which is used to identify 
	 *        the record
	 ***********************************************************************************************/
//	private CFWQueryResult doComparisionWithTopLevel(CompareArguments arguments, CFWQueryResult olderResult, CFWQueryResult youngerResult, Column identifierColumnOlder, Column identifierColumnYounger){
//		
//		//###################################################################################################
//		// Initialize Variables
//		//###################################################################################################
//		
//		//Initialize result variable
//		CFWQueryResult compareResults = null;;
//		
//		String toplevelField = arguments.getValue(CompareArguments.COLUMN_TOPLEVEL);
//		String[] splittedTopLevel = toplevelField.split(",");
//		String olderTopLevelName = splittedTopLevel[0];
//		String youngerTopLevelName = splittedTopLevel[1];
//		String toplevelFieldContent = splittedTopLevel[2];
//		
//		// [DEBUG]
//		new CFWLog(logger).debug("olderTopLevelName: "+olderTopLevelName+
//					", youngerTopLevelName: "+youngerTopLevelName+
//					", toplevelFieldContent: "+toplevelFieldContent);
//		
//		//###################################################################################################
//		// Initialize older TopLevels
//		//###################################################################################################
//		Column olderTopLevelColumn = olderResult.getColumn(olderTopLevelName);
//		
//		Map<String, CFWQueryResult> olderTopLevelParts = new  TreeMap<String, CFWQueryResult>();
//		String olderLabel = arguments.getValue(CompareArguments.OLDER_LABEL);
//		
//		Row olderHeaders = olderResult.getHeadersAsRow();
//		
//		for(int i = 0; i < olderTopLevelColumn.size(); i++){
//			if(olderTopLevelColumn.get(i).toString().contains(toplevelFieldContent)){
//				
//				//save identifier for the Map
//				String identifier = identifierColumnOlder.get(i).toString();
//				
//				//add the first row with contains the TopLevel
//				CFWQueryResult toplevelFieldPart = new CFWQueryResult(olderLabel);
//				toplevelFieldPart.initializeColumnsByHeaders(olderHeaders);
//				toplevelFieldPart.addRow(olderResult.getRow(i));
//				
//				//jump to next row and add all rows till the next toplevelField
//				i++;
//				while(   i < olderTopLevelColumn.size() 
//						&& !olderTopLevelColumn.get(i).toString().contains(toplevelFieldContent) ){
//					
//					toplevelFieldPart.addRow(olderResult.getRow(i));
//					i++;
//				}
//				//revert to one row before
//				i--;
//				
//				olderTopLevelParts.put(identifier, toplevelFieldPart);
//			}
//		}
//		
//		new CFWLog(logger).debug("top levels found in older: " + olderTopLevelParts.size());
//		
//		//###################################################################################################
//		// Initialize younger TopLevels
//		//###################################################################################################
//		Column youngerTopLevelColumn = youngerResult.getColumn(youngerTopLevelName);
//		
//		Map<String, CFWQueryResult> youngerTopLevelParts = new  TreeMap<String, CFWQueryResult>();
//		String youngerLabel = arguments.getValue(CompareArguments.YOUNGER_LABEL);
//		
//		Row youngerHeaders = youngerResult.getHeadersAsRow();
//		
//		for(int i = 0; i < youngerTopLevelColumn.size(); i++){
//			if(youngerTopLevelColumn.get(i).toString().contains(toplevelFieldContent)){
//				
//				//save identifier for the Map
//				String identifier = identifierColumnYounger.get(i).toString();
//				
//				//add the first row with contains the TopLevel
//				CFWQueryResult toplevelFieldPart = new CFWQueryResult(youngerLabel);
//				toplevelFieldPart.initializeColumnsByHeaders(youngerHeaders);
//				toplevelFieldPart.addRow(youngerResult.getRow(i));
//				
//				//jump to next row and add all rows till the next toplevelField
//				i++;
//				while(   i < youngerTopLevelColumn.size() 
//						&& !youngerTopLevelColumn.get(i).toString().contains(toplevelFieldContent) ){
//					
//					toplevelFieldPart.addRow(youngerResult.getRow(i));
//					i++;
//				}
//				//revert to one row before
//				i--;
//				
//				youngerTopLevelParts.put(identifier, toplevelFieldPart);
//			}
//		}
//		
//		
//		new CFWLog(logger).debug("toplevelFields found in younger: " + youngerTopLevelParts.size());
//		
//		//###################################################################################################
//		// Compare TopLevelParts
//		//###################################################################################################
//		
//		// remove toplevelField-Argument from ArgumentList and compare TopLevelParts
//		arguments.getLoadedArguments().remove(CompareArguments.COLUMN_TOPLEVEL);
//		
//		// On the first Comparison, use the result to initialize the variable "comparedResult"
//		boolean isFirstComparison = true;
//		
//		// Save not Comparable TopLevels and append them on the end of the result
//		ArrayList<CFWQueryResult>  youngerPartsNotComparable = new ArrayList<CFWQueryResult>();
//		ArrayList<CFWQueryResult>  olderPartsNotComparable = new ArrayList<CFWQueryResult>();
//		
//		// -----------------------------------------------------
//		// Iterate over youngerParts and compare if possible
//		for(String identifier : youngerTopLevelParts.keySet()){
//			
//			CFWQueryResult olderResultPart = olderTopLevelParts.get(identifier);
//			CFWQueryResult youngerResultPart = youngerTopLevelParts.get(identifier);
//			
//			if(olderTopLevelParts.containsKey(identifier)){
//					
//				CFWQueryResult partResult = CFWQueryCommandCompareMethods.compare2CSVFiles(arguments, olderResultPart, youngerResultPart);
//				
//				if(isFirstComparison){
//					isFirstComparison = false;
//					compareResults = partResult;
//				}else{
//					compareResults.addRows(partResult.getAllRows());
//				}
//			}else{
//				youngerPartsNotComparable.add(youngerResultPart);
//			}
//		}
//		
//		// -----------------------------------------------------
//		// Iterate over olderParts and find not comparable Parts
//		for(String identifier : olderTopLevelParts.keySet()){
//			if(!youngerTopLevelParts.containsKey(identifier)){
//				olderPartsNotComparable.add(olderTopLevelParts.get(identifier));
//			}
//		}
//		
//		new CFWLog(logger).info("younger top levels not comparable: " + youngerPartsNotComparable.size());
//		new CFWLog(logger).info("older top levels not comparable: " + olderPartsNotComparable.size());
//		
//		//###################################################################################################
//		// Pseudo Compare for not comparable parts
//		// this will create a result which fits in the 
//		// other results.
//		//###################################################################################################
//		
//		//initialize pseudo data
//		CFWQueryResult olderPseudoCFWQueryResult = new CFWQueryResult("pseudo");
//		olderPseudoCFWQueryResult.initializeColumnsByHeaders(youngerResult.getHeadersAsRow());
//		CFWQueryResult youngerPseudoCFWQueryResult = new CFWQueryResult("pseudo");
//		youngerPseudoCFWQueryResult.initializeColumnsByHeaders(youngerResult.getHeadersAsRow());
//		
//		// -----------------------------------------------------
//		// younger pseudo compare
//		for(CFWQueryResult youngerPart : youngerPartsNotComparable){
//			CFWQueryResult partResult = CFWQueryCommandCompareMethods.compare2CSVFiles(arguments, olderPseudoCFWQueryResult, youngerPart);
//			
//			if(isFirstComparison){
//				isFirstComparison = false;
//				compareResults = partResult;
//			}else{
//				compareResults.addRows(partResult.getAllRows());
//			}
//		}
//		
//		// -----------------------------------------------------
//		// older pseudo compare
//		for(CFWQueryResult olderPart : olderPartsNotComparable){
//			CFWQueryResult partResult = CFWQueryCommandCompareMethods.compare2CSVFiles(arguments, olderPart, youngerPseudoCFWQueryResult);
//			
//			if(isFirstComparison){
//				isFirstComparison = false;
//				compareResults = partResult;
//			}else{
//				compareResults.addRows(partResult.getAllRows());
//			}
//		}
//				
//		
//		return compareResults;
//	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/	
	private class ComparableData{
		// the objects containing the results
		// array used to support multiple records with same ID
		private ArrayList<JsonObject> olderArray = new ArrayList<>();
		// array used to support multiple records with same ID
		private ArrayList<JsonObject> youngerArray = new ArrayList<>();
		
		/**************************************************
		 * 
		 **************************************************/
		public void addOlder(JsonObject older) {  
			olderArray.add(older);
		}
		
		/**************************************************
		 * 
		 **************************************************/
		public void addYounger(JsonObject younger) {  
			youngerArray.add(younger);
		}
		
		/**************************************************
		 * 
		 **************************************************/
		public JsonObject getOlder(int i) { 
			if(i < olderArray.size()) {
				return olderArray.get(i); 
			}
			return null;
		}
		
		/**************************************************
		 * 
		 **************************************************/
		public JsonObject getYounger(int i) { 
			if(i < youngerArray.size()) {
				return youngerArray.get(i); 
			}
			return null;
		}
		
		/**************************************************
		 * 
		 **************************************************/
		public int size() {
			return (olderArray.size() >= youngerArray.size()) ? olderArray.size() : youngerArray.size();
		}
		
		/**************************************************
		 * 
		 **************************************************/
		public void compareAddToResult(JsonArray resultArray) {
			
			int size = this.size(); // slight performance improvement
			for(int i = 0; i < size ; i++) {
				
				//---------------------------------
				// Younger Data
				JsonObject resultObject = new JsonObject();
				
				JsonObject youngerObject = this.getYounger(i);
				JsonObject olderObject = this.getOlder(i);
				JsonObject iteratorObject = (youngerObject != null) ? youngerObject : olderObject;
			
				//---------------------------------
				// Iterate Object Members
				for(Entry<String, JsonElement> entry : iteratorObject.entrySet()) {
					String fieldname = entry.getKey();
					
					JsonElement youngValue = (youngerObject != null) ? youngerObject.get(fieldname) : JsonNull.INSTANCE;
					JsonElement oldValue = (olderObject != null) ? olderObject.get(fieldname) : JsonNull.INSTANCE;
					
					//---------------------------------
					// If Identifier just add without compare
					if(identifierFields.contains(fieldname)) {
						
						// Make sure ID field uses a non null value if one is not null
						JsonElement finalValue = youngValue;
						if(finalValue == null || finalValue.isJsonNull()) {
							finalValue = oldValue;
						}
						
						// add count or no count
						if(i == 0) {
							resultObject.add(fieldname, finalValue);
						}else {
							String indexed = (!finalValue.isJsonNull()) ? finalValue.getAsString() : "null";
							indexed += "["+(i+1)+"]";
							resultObject.addProperty(fieldname, indexed);
						}
						
						continue;
					}
					
					//---------------------------------
					// Add Original Values
					resultObject.add(fieldname+labelOld, oldValue);
					resultObject.add(fieldname+labelYoung, youngValue);
					
					//---------------------------------
					// Ensure Column Order
					if(compareNumbersAbsolute) { resultObject.add(fieldname+labelDiff, JsonNull.INSTANCE); }
					if(compareNumbersDiffPercent) { resultObject.add(fieldname+labelDiffPercent, JsonNull.INSTANCE); }
					
					//---------------------------------
					// Check Nulls
					String nullLabel = (fieldHasPercentage.contains(fieldname))	? labelDiffPercent : labelDiff;
					if( (youngValue == null || youngValue.isJsonNull())
					&&  (oldValue == null || oldValue.isJsonNull())
					) {
						resultObject.addProperty(fieldname+nullLabel, true);
						continue;
					}
					
					if(youngValue == null
					|| youngValue.isJsonNull()	
					|| oldValue == null 
					|| oldValue.isJsonNull()
					) {
						resultObject.addProperty(fieldname+nullLabel, false);
						continue;
					}
										
					//---------------------------------
					// Check Primitives
					if(youngValue.isJsonPrimitive() && oldValue.isJsonPrimitive()) {
						QueryPartValue oldPart = QueryPartValue.newFromJsonElement(oldValue);
						QueryPartValue youngPart = QueryPartValue.newFromJsonElement(youngValue);
						
						//--------------------
						// Numbers
						if(oldPart.isNumberOrNumberString()
						&& youngPart.isNumberOrNumberString()) {
							if(compareNumbersAbsolute) {
								BigDecimal diff = youngPart.getAsBigDecimal().subtract(oldPart.getAsBigDecimal());
								resultObject.addProperty(fieldname+labelDiff, diff);
							}
							if(compareNumbersDiffPercent) {
								fieldHasPercentage.add(fieldname);
								
								if(oldPart.getAsFloat() == 0f) {
									float yFloat = youngPart.getAsFloat();
									if(yFloat == 0f) {
										resultObject.addProperty(fieldname+labelDiffPercent, 0);
									}else if(yFloat > 0) {
										resultObject.addProperty(fieldname+labelDiffPercent, 9999999);
									}else {
										resultObject.addProperty(fieldname+labelDiffPercent, -9999999);
									}
								}else {
									BigDecimal diff = youngPart.getAsBigDecimal().subtract(oldPart.getAsBigDecimal());
									BigDecimal diffPerc = diff.divide(oldPart.getAsBigDecimal(), 6, RoundingMode.HALF_UP);
									resultObject.addProperty(fieldname+labelDiffPercent, diffPerc.multiply(BigDecimal.valueOf(100)));
								}
							}
							
							if(!compareNumbersAbsolute && !compareNumbersDiffPercent) {
								resultObject.add(fieldname+labelDiff, JsonNull.INSTANCE);
							}
							continue;
						}
						
						//--------------------
						// Booleans
						if(oldPart.isBoolOrBoolString()
						&& youngPart.isBoolOrBoolString()) {
							if(compareBooleans) {
								boolean oldBool = oldPart.getAsBoolean();
								boolean youngBool = youngPart.getAsBoolean();
								resultObject.addProperty(fieldname+labelDiff, oldBool == youngBool);
							}else {
								resultObject.add(fieldname+labelDiff, JsonNull.INSTANCE);
							}
							continue;
							
						}
						
						//--------------------
						// Compare As Strings
						if(oldPart.isString()
						&& youngPart.isString()) {
							if(compareStrings) {
								String old = oldPart.getAsString();
								String young = youngPart.getAsString();
								resultObject.addProperty(fieldname+labelDiff, old.equals(young));
							}else {
								resultObject.add(fieldname+labelDiff, JsonNull.INSTANCE);
							}
							continue;
						}
						
						//--------------------
						// Anything Else uncomparable
						resultObject.add(fieldname+labelDiff, JsonNull.INSTANCE);
						continue;
					}
					
					//---------------------------------
					// Check Arrays
					
					if(youngValue.isJsonObject() 
					|| oldValue.isJsonObject()
					) {
						if(compareObjects) {	
							boolean equals = youngValue.toString().equals(oldValue.toString());
							resultObject.addProperty(fieldname+labelDiff, equals);
						}else {
							resultObject.add(fieldname+labelDiff, JsonNull.INSTANCE);
						}
						continue;
					}
					
					//---------------------------------
					// Check Arrays
					if(youngValue.isJsonArray()
					|| oldValue.isJsonArray()
					) {
						if(compareArrays) {	
							boolean equals = youngValue.toString().equals(oldValue.toString());
							resultObject.addProperty(fieldname+labelDiff, equals);
						}else {
							resultObject.add(fieldname+labelDiff, JsonNull.INSTANCE);
						}
						continue;
					}
					
					//---------------------------------
					// Fallback: Anything Else not comparable
					// Probably unreachable with GSON version at implementation time
					resultObject.add(fieldname+labelDiff, JsonNull.INSTANCE);
					
				}
			
				resultArray.add(resultObject);
				detectedFields.addAll(resultObject.keySet());
			}
		}

	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	private JsonArray doCompareDefault(JsonArray olderArray, JsonArray youngerArray){
		
		//###################################################################################################
		// Initialize Variables
		//###################################################################################################
		
		//Initialize result variable
		JsonArray compareResult = new JsonArray();
				
		// Map with recordID and object
		TreeMap<String, ComparableData> comparableDataMap = new TreeMap<>();
		
		//---------------------------------
		// Add All Older Results
		for(JsonElement element : olderArray){
			
			if(element != null && element.isJsonObject()) {
				JsonObject olderObject = element.getAsJsonObject();
				String recordID = getRecordID(identifierFields, olderObject);
				
				if(!comparableDataMap.containsKey(recordID)){
					comparableDataMap.put(recordID, new ComparableData());
				}
				
				comparableDataMap.get(recordID).addOlder(olderObject);
				
			}
		}
		
		//---------------------------------
		// Initialize All Younger Results
		for(JsonElement element : youngerArray){
			
			if(element != null && element.isJsonObject()) {
				JsonObject youngerObject = element.getAsJsonObject();
				String recordID = getRecordID(identifierFields, youngerObject);
				
				if(!comparableDataMap.containsKey(recordID)){
					comparableDataMap.put(recordID, new ComparableData());
				}
				
				comparableDataMap.get(recordID).addYounger(youngerObject);
				
			}
		}
		
		//---------------------------------------------------
		// Iterate over all Columns of the youngerData, try to find a 
		// matching column in the olderData and compare if there is 
		// a column with the same name.
		//---------------------------------------------------
				
		for(ComparableData comparableData : comparableDataMap.values()){
		
			comparableData.compareAddToResult(compareResult);
		}
		
		return compareResult;
	}
	
	/***********************************************************************************************
	 * Creates a recordID for the jsonObjet and the given fieldnames.
	 ***********************************************************************************************/
	private String getRecordID(ArrayList<String> idFieldnames, JsonObject object) {
		String recordID = "";
		for(String idfield : idFieldnames) {
			if(object.has(idfield)) {
				recordID += CFW.JSON.toJSON(object.get(idfield))+"_";
			}
		}
		
		return recordID;
	}
	
	/***********************************************************************************************
	 * This method is used when "-column.compareDef" arguments are provided.
	 * It will compare all columns which has the same header in both files.
	 * 
	 * @param olderResult the object with the CFWQueryResult which should be considered as older in the 
	 *        comparison
	 * @param youngerResult the object with the CFWQueryResult which should be considered as younger in 
	 *        the comparison
	 * @param identifierColumnOlder the column of the older CFWQueryResult which is used to identify 
	 *        the record   
	 * @param identifierColumnYounger the column of the older CFWQueryResult which is used to identify 
	 *        the record
	 ***********************************************************************************************/
//	private CFWQueryResult doComparisionByManualDefinition(CompareArguments arguments, CFWQueryResult olderResult, CFWQueryResult youngerResult, Column identifierColumnOlder, Column identifierColumnYounger){
//		
//		//###################################################################################################
//		// Initialize Variables
//		//###################################################################################################
//		
//		//Initialize result variable
//		CFWQueryResult compareResults = new CFWQueryResult("result");
//		
//		//used to check if identifierColumn is printed
//		boolean isIdentifierPrinted = false;
//		
//		// Load sort argument
//		String doSortString = arguments.getValue(CompareArguments.RESULT_SORT).toLowerCase();
//		boolean doSort = doSortString.matches("true") ? true : false ;
//		
//		//Load Arguments
//		ArrayList<CompareDefinition> compareDefinitions = arguments.getCompareDefinitions();
//
//		//merge identifier Columns
//		Column mergedIdentifierColumns = CSVAPIUtils.merge2ColumnsNoDuplicates(identifierColumnOlder, identifierColumnYounger, doSort);
//		mergedIdentifierColumns.setHeader(identifierColumnOlder.getHeader());
//		
//		//###################################################################################################
//		// Iterate over all compare definitions, and do the actions
//		// which are defined in it
//		//###################################################################################################
//		for(CompareDefinition definition : compareDefinitions){
//			
//			//###################################################################################################
//			//Evaluate Definition
//			//###################################################################################################
//		
//			// getColumns for Comparison
//			Column valueColumnOlder = olderResult.getColumn(definition.getOlderColumnName());
//			Column valueColumnYounger = youngerResult.getColumn(definition.getYoungerColumnName());
//			
//			new CFWLog(logger).debug("current columns for compare definition - "
//						+ "olderColumn:"+valueColumnOlder.getHeader()
//						+", youngerColumn:"+valueColumnYounger.getHeader());
//			
//
//			//###################################################################################################
//			// create result Columns
//			//###################################################################################################
//			Column resultColumnOlder = CFWQueryCommandCompareMethods.initializeResultColumn(mergedIdentifierColumns, identifierColumnOlder, valueColumnOlder);
//			resultColumnOlder.setHeader(valueColumnOlder.getHeader()+"("+olderResult.getLabel()+")");
//			
//			Column resultColumnYounger = CFWQueryCommandCompareMethods.initializeResultColumn(mergedIdentifierColumns, identifierColumnYounger, valueColumnYounger);
//			resultColumnYounger.setHeader(valueColumnYounger.getHeader()+"("+youngerResult.getLabel()+")");	
//			
//			//###################################################################################################
//			// Iterate over all methods in the definition
//			//###################################################################################################
//			
//			for(Entry<String,String> methodEntry : definition.getMethods()){
//				
//				if(valueColumnYounger != null && valueColumnOlder != null ){
//					
//					String methodName =  		methodEntry.getKey();
//					String methodArguments = 	methodEntry.getValue();
//					
//					new CFWLog(logger).debug("manual compare - "
//								+ " executeMethod: " + methodName
//								+ " with Arguments: " + methodArguments);
//					
//					//###################################################################################################
//					//Resolving Block for comparing methods
//					//###################################################################################################
//					if(   methodName.equals("compareDifference") 
//					   || methodName.equals("compareDifference%")
//					   || methodName.equals("compareAsStrings")){
//						
//						Method compareMethod = CFWQueryCommandCompareMethods.getMethod(methodName);
//						if(compareMethod != null ){
//							Column comparedColumn = CFWQueryCommandCompareMethods.compare2Columns(resultColumnOlder, resultColumnYounger, compareMethod);
//							
//							if(comparedColumn != null){
//								
//								//add result Columns
//								
//								comparedColumn.setHeader(valueColumnOlder.getHeader()+" "+arguments.getHeaderForArgument(methodName));
//								compareResults.addColumn(comparedColumn);
//							}
//						}
//					}
//					
//					//###################################################################################################
//					//Resolving Block for calculation methods
//					//###################################################################################################
//					if(   methodName.equals("multiplyYoungerBy") 
//					   || methodName.equals("multiplyOlderBy")
//					   || methodName.equals("divideYoungerBy")
//					   || methodName.equals("divideOlderBy")){
//						
//							try {
//							
//								if(methodName.equals("divideYoungerBy"))
//									CSVAPIUtils.divideColumnBy(resultColumnYounger, methodArguments);
//								
//								if(methodName.equals("multiplyYoungerBy"))
//									CSVAPIUtils.multiplyColumnBy(resultColumnYounger, methodArguments);
//								
//								if(methodName.equals("divideOlderBy"))
//									CSVAPIUtils.divideColumnBy(resultColumnOlder, methodArguments);
//								
//								if(methodName.equals("multiplyOlderBy"))
//									CSVAPIUtils.multiplyColumnBy(resultColumnOlder, methodArguments);
//								
//							} catch (Exception e) {
//								new CFWLog(logger).error("Error occured during manual compare: "+e.getMessage());
//							}
//					}
//				
//					//###################################################################################################
//					//Resolving Blocks for printing methods
//					//###################################################################################################
//					
//					if(methodName.equals("printOlder")){
//						compareResults.addColumn(resultColumnOlder);
//					}
//					
//					if(methodName.equals("printYounger")){
//						compareResults.addColumn(resultColumnYounger);
//					}
//					
//					if(methodName.equals("printMerged")){
//						
//						Column merged = CSVAPIUtils.mergeValuesOf2ResultColumns(resultColumnOlder, resultColumnYounger);
//						
//						if(valueColumnYounger.equals(identifierColumnYounger)){
//							isIdentifierPrinted=true;
//						}
//						
//						compareResults.addColumn(merged);
//					}
//					if(methodName.equals("printSeparator")){
//						Column separatorColumn = new Column(" ");
//						for(int j=0; j < mergedIdentifierColumns.size(); j++){
//							separatorColumn.add(" ");
//						}
//						compareResults.addColumn(separatorColumn);
//					}
//					
//					
//				}else{
//					new CFWLog(logger).error("Could not find both columns - " +
//								"youngerColumn: "+valueColumnYounger.getHeader()+
//								", olderColumn: "+valueColumnOlder.getHeader());
//				}
//			}
//		}
//		
//		//###################################################################################################
//		// if the identifier column was not printed till know
//		// add it as first column
//		//###################################################################################################
//		if(!isIdentifierPrinted){
//			ArrayList<Column> columnsArray = compareResults.getColumns();
//			compareResults = new CFWQueryResult("Results");
//			compareResults.addColumn(mergedIdentifierColumns);
//			compareResults.addColumns(columnsArray);
//		}
//		
//		return compareResults;
//	}
		
	/***********************************************************************************************
	 * 
	 *  !!!!!!!!!!!!!! UNDER CONSTRUCTION !!!!!!!!!!!!!!!!!!!!<p>
	 * 
	 * This method is used when the arguments "-hierarchy" and "-hierarchyColumn" is provided.
	 * It will split the provided CFWQueryResult in parts, with the toplevelField as a "delimiter". 
	 * This method considers also hierarchical relations between the rows in the data, and creates 
	 * subsets of CFWQueryResult in the parts.
	 * The parts are compared if in both files a toplevelField with the same identifier is found.
	 * The result of all part comparisons is merged in one result file.
	 * 
	 * @param olderResult the object with the CFWQueryResult which should be considered as older in the 
	 *        comparison
	 * @param youngerResult the object with the CFWQueryResult which should be considered as younger in 
	 *        the comparison
	 * @param identifierColumnOlder the column of the older CFWQueryResult which is used to identify 
	 *        the record   
	 * @param identifierColumnYounger the column of the older CFWQueryResult which is used to identify 
	 *        the record                                                  
	 ***********************************************************************************************/
//	private JsonArray doComparisionWithHierarchy(CompareArguments arguments, CFWQueryResult olderResult, CFWQueryResult youngerResult, Column identifierColumnOlder, Column identifierColumnYounger){
//		
//		//###################################################################################################
//		// Initialize Variables
//		//###################################################################################################
//		
//		//Initialize result variable
//		CFWQueryResult compareResults = null;;
//		
//		//Initialize toplevelField
//		String hierarchyColumns = arguments.getValue("-hierarchyColumn");
//		String[] splittedTopLevel = hierarchyColumns.split(",");
//		String olderHierarchyName = splittedTopLevel[0];
//		String youngerHierarchyName = splittedTopLevel[1];
//		
//		//Initialize hierarchy
//		String hierarchy = arguments.getValue("-hierarchy");
//		ArrayList<String> hierarchyArray = new ArrayList<String>();
//		hierarchyArray.addAll(Arrays.asList(hierarchy.split(",")));
//		
//		String toplevelFieldContent = hierarchyArray.get(0);
//			
//		//###################################################################################################
//		// DEBUG
//		//###################################################################################################
//		new CFWLog(logger).debug(	"olderTopLevelName: "+olderHierarchyName+
//						", youngerTopLevelName: "+youngerHierarchyName+
//						", toplevelFieldContent: "+toplevelFieldContent);
//		
//		for (int i=0 ; i < hierarchyArray.size(); i++){
//			new CFWLog(logger).debug("Hierarchy: Level-"+i+" is "+hierarchyArray.get(i));
//		}
//	
//		
//		//###################################################################################################
//		// Initialize older TopLevels
//		//###################################################################################################
//		Column olderTopLevelColumn = olderResult.getColumn(olderHierarchyName);
//		
//		Map<String, CFWQueryResult> olderTopLevelParts = new TreeMap<String, CFWQueryResult>();
//		
//		HierarchicalCFWQueryResult olderAllLevels = new HierarchicalCFWQueryResult("olderHCVS");
//		
//		String olderLabel = arguments.getValue(CompareArguments.OLDER_LABEL);
//		
//		Row olderHeaders = olderResult.getHeadersAsRow();
//		
//		for(int i = 0; i < olderTopLevelColumn.size(); i++){
//			if(olderTopLevelColumn.get(i).toString().contains(toplevelFieldContent)){
//				
//				//save identifier for the Map
//				String identifier = identifierColumnOlder.get(i).toString();
//				
//				//add the first row with contains the TopLevel
//				HierarchicalCFWQueryResult toplevelFieldMap = new HierarchicalCFWQueryResult(identifier, olderAllLevels);
//				toplevelFieldMap.initializeColumnsByHeaders(olderHeaders);
//				toplevelFieldMap.addRow(youngerResult.getRow(i));
//				
//				// Save current Values
//				HierarchicalCFWQueryResult currentCFWQueryResult = toplevelFieldMap; 
//				int currentLevel = hierarchyArray.indexOf(olderTopLevelColumn.get(i).toString());
//				
//				//jump to next row and add all rows till the next toplevelField
//				i++;
//				int nextLevel = hierarchyArray.indexOf(olderTopLevelColumn.get(i).toString());
//				
//				while(   i < olderTopLevelColumn.size()
//					  && !olderTopLevelColumn.get(i).toString().contains(toplevelFieldContent) ){
//					
//					
//					if(currentLevel == nextLevel){
//						Row row = currentCFWQueryResult.getRow(i);						
//						toplevelFieldMap.addRow(row);
//					}else if(currentLevel > nextLevel){
//						//add new hierarchy
//					}else{
//						//go one hierarchy back up
//					}
//					i++;
//				}
//				
//				//revert to one row before, so we are back on the toplevelField on the next loop
//				i--;
//				
//				olderAllLevels.addCFWQueryResultChild(toplevelFieldMap);
//			}
//		}
//		
//		
//		new CFWLog(logger).debug("toplevelFields found in older: " + olderTopLevelParts.size());
//		
//		//###################################################################################################
//		// Initialize younger TopLevels
//		//###################################################################################################
//		Column youngerTopLevelColumn = youngerResult.getColumn(youngerHierarchyName);
//		
//		Map<String, CFWQueryResult> youngerTopLevelParts = new  TreeMap<String, CFWQueryResult>();
//		String youngerLabel = arguments.getValue(CompareArguments.YOUNGER_LABEL);
//		
//		Row youngerHeaders = youngerResult.getHeadersAsRow();
//		
//		for(int i = 0; i < youngerTopLevelColumn.size(); i++){
//			if(youngerTopLevelColumn.get(i).toString().contains(toplevelFieldContent)){
//				
//				//save identifier for the Map
//				String identifier = identifierColumnYounger.get(i).toString();
//								
//				//add the first row with contains the TopLevel
//				CFWQueryResult toplevelFieldPart = new CFWQueryResult(youngerLabel);
//				toplevelFieldPart.initializeColumnsByHeaders(youngerHeaders);
//				toplevelFieldPart.addRow(youngerResult.getRow(i));
//				
//				//jump to next row and add all rows till the next toplevelField
//				i++;
//				while(   i < youngerTopLevelColumn.size() 
//					  && !youngerTopLevelColumn.get(i).toString().contains(toplevelFieldContent) ){
//					
//					toplevelFieldPart.addRow(youngerResult.getRow(i));
//					i++;
//				}
//				//revert to one row before
//				i--;
//				
//				youngerTopLevelParts.put(identifier, toplevelFieldPart);
//			}
//		}
//		
//		
//		new CFWLog(logger).debug("toplevelFields found in younger: " + youngerTopLevelParts.size());
//		
//		//###################################################################################################
//		// Compare TopLevelParts
//		//###################################################################################################
//		
//		boolean isFirstComparision = true;
//		for(String identifier : youngerTopLevelParts.keySet()){
//			if(olderTopLevelParts.containsKey(identifier)){
//				
//				CFWQueryResult olderResultPart = olderTopLevelParts.get(identifier);
//				CFWQueryResult youngerResultPart = youngerTopLevelParts.get(identifier);
//				
//				// Removing toplevelField-Argument from ArgumentList
//				// to not end in an endless loop
//				arguments.getLoadedArguments().remove(CompareArguments.COLUMN_TOPLEVEL);
//				
//				CFWQueryResult partResult = CFWQueryCommandCompareMethods.compare2CSVFiles(arguments, olderResultPart, youngerResultPart);
//				
//				if(isFirstComparision){
//					isFirstComparision = false;
//					compareResults = partResult;
//				}else{
//					compareResults.addRows(partResult.getAllRows());
//				}
//			}
//				
//		}
//			
//		return compareResults;
//	}

	/************************************************************
	 * Calculate the difference between two values as double.
	 * 
	 ************************************************************/
//	private double calcDiffAsDouble(Object olderValue, Object youngerValue) throws Exception{
//		
//		String valString1 = olderValue.toString();
//		String valString2 = youngerValue.toString();
//		Double d1;
//		Double d2;
//		Double compared;
//		
//		try{
//			d1 = Double.parseDouble(valString1);
//			d2 = Double.parseDouble(valString2);
//			
//			compared = d2 - d1; 
//		}catch (NumberFormatException e) {
//			throw new Exception("Could not parse as double: "+valString1+","+valString2);
//		}
//	
//		return compared;
//	}
	
	/************************************************************
	 * Calculate the difference in percentage between two values 
	 * as double.
	 * 
	 * @param  olderValue the older value
	 * @param youngerValue the younger value
	 * 
	 ************************************************************/
//	private double calcDiffPercentageAsDouble(Object olderValue, Object youngerValue) throws Exception{
//		
//		String valStringOlder = olderValue.toString();
//		String valStringYounger = youngerValue.toString();
//		Double doubleOlder;
//		Double doubleYounger;
//		Double compared;
//		
//		try{
//			doubleOlder = Double.parseDouble(valStringOlder);
//			doubleYounger = Double.parseDouble(valStringYounger);
//			
//			compared = doubleYounger/doubleOlder-1; 
//		}catch (NumberFormatException e) {
//			throw new Exception("Could not parse as double: "+valStringOlder+","+valStringYounger);
//		}
//	
//		return compared;
//	}
	
	/************************************************************
	 * Compare two Strings.<br>
	 * 
	 * @param  olderValue the older value
	 * @param youngerValue the younger value
	 * @return if(equals) --> "EQ"<br>
	 *         if(!equals) --> "NOTEQ"
	 ************************************************************/
//	private String compareAsString(Object olderValue, Object youngerValue) throws Exception{
//		
//		String valString1 = olderValue.toString();
//		String valString2 = youngerValue.toString();
//		String result = "NOTEQ";
//	
//		if(valString1.equals(valString2)){
//			result="EQ";
//		}
//		
//		return result;
//	}

}
