package com.xresch.cfw.features.query.parse;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryMemoryException;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.parse.QueryPartValue.QueryPartValueType;
import com.xresch.cfw.logging.SysoutInterceptor;

/**************************************************************************************************************
 * Part that specifies a member access of a Json Field.
 * Pipeline will process objects like this:
 * { "_source": "random", 
 *   "key": "bla", 
 *   "value": "42", 
 *   multivalue: { "x": "22", y: "33"}
 * }
 * 
 * To access 'x' the expression would be "multivalue.x".
 * The expression can have multiple levels like "multivalue.another.level.x".
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class QueryPartJsonMemberAccess extends QueryPart {
	
	private QueryPart leftside;
	private QueryPart rightside = null;
	private CFWQueryContext context;
	
	
	/******************************************************************************************************
	 * Creates a clone of the QueryPart.
	 * 
	 ******************************************************************************************************/
	@Override
	public QueryPartJsonMemberAccess clone() {
		
		QueryPart cloneLeft = leftside.clone();
		QueryPart cloneRight = rightside.clone();
		
		QueryPartJsonMemberAccess clone = new QueryPartJsonMemberAccess(context, cloneLeft, cloneRight);
		clone.parent = this.parent;
		
		return clone;
	}
	
	/******************************************************************************************************
	 * 
	 * @param leftside The name on the left side of the assignment operation.
	 * 
	 ******************************************************************************************************/
	private QueryPartJsonMemberAccess(CFWQueryContext context, QueryPart leftside, QueryPart rightside) {
		super();
		this.context = context;
		
		if( !(leftside instanceof QueryPartJsonMemberAccess) 
		) {
			this.leftside = leftside;
			this.rightside = rightside;
		}else {
			QueryPartJsonMemberAccess memberAccess = (QueryPartJsonMemberAccess)leftside;
			this.leftside = memberAccess.leftside;
			this.rightside = new QueryPartJsonMemberAccess(context, memberAccess.rightside, rightside);
		}
		
	}
	
	/******************************************************************************************************
	 * Had to create this ugly workaround... don't ask, it made things work.
	 * 
	 ******************************************************************************************************/
	public static QueryPart createMemberAccess(CFWQueryContext context, QueryPart leftside, QueryPart rightside) {
		
		//----------------------------------------
		// Handle Left is Assignment
		if(leftside instanceof QueryPartAssignment) {
			QueryPartAssignment assignmentPart = (QueryPartAssignment)leftside;
			QueryPart assignmentLeftside = assignmentPart.getLeftSide();
			QueryPart assignmentRightside = assignmentPart.getRightSide();
			QueryPart memberAccessPart = QueryPartJsonMemberAccess.createMemberAccess(context, assignmentRightside, rightside);
			return new QueryPartAssignment(context, assignmentLeftside, memberAccessPart);
		}
		
		//----------------------------------------
		// Handle Left is Binary
		if(leftside instanceof QueryPartBinaryExpression) { 
			QueryPartBinaryExpression expression = (QueryPartBinaryExpression)leftside;
			QueryPart memberAccessPart = QueryPartJsonMemberAccess.createMemberAccess(context, expression.getRightSide(), rightside);
			return new QueryPartBinaryExpression(context, expression.getLeftSide(), expression.getOperatorType(), memberAccessPart);
		}	
		
		//----------------------------------------
		// Any other case
		return new QueryPartJsonMemberAccess(context, leftside, rightside);
		
	}
	
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	private boolean isLeftsideArrayPart() {
		return (leftside instanceof QueryPartArray);
	}
	
	/******************************************************************************************************
	 * If object is null, return the member expression as a string representation.
	 * If object is not null, determines and returns the value of the member based on the member expression.
	 * 
	 * If rightside is anything else than a QueryPartJsonMemberAccess, the value returned by 
	 * QueryPart.determineValue() will be used.
	 * If rightside is QueryPartJsonMemberAccess, the next level will be fetched recursively;
	 * @throws CFWQueryMemoryException 
	 * 
	 ******************************************************************************************************/
	@Override
	public QueryPartValue determineValue(EnhancedJsonObject object) throws CFWQueryMemoryException {	
	
		if(object != null) {
			return getValueOfMember(object, object.getWrappedObject());
		}else {
			return getValueOfMember(null, null);
		}

	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public QueryPartValue getValueOfMember(EnhancedJsonObject rootObject, JsonElement currentElement) {
		
		//----------------------------------
		// Special Case: function()[0] : Return the value of the function
		// Will not apply to: object.function()[0]
		if(leftside instanceof QueryPartFunction
		&& (rightside instanceof QueryPartArray) ){
			QueryPartValue functionResult = leftside.determineValue(rootObject);
			QueryPartValueType type = functionResult.type();

			if( ! type.equals(QueryPartValueType.JSON) ) {
				QueryPartArray array = ((QueryPartArray)rightside);
				if(array.size() == 1 ) {
					QueryPartValue indexValue = array.getAsParts().get(0).determineValue(rootObject);
					if(indexValue.isNumberOrNumberString()) {
						return functionResult;
					}
				}
			}
		}
		
		//----------------------------------
		// All Other Cases
		return QueryPartValue.newFromJsonElement( accessMemberRecursively(rootObject, currentElement) );
		
	}
	
	/******************************************************************************************************
	 * Determines and returns the member based on this member expression.
	 * 
	 * If rightside is anything else than a QueryPartJsonMemberAccess, the value returned by 
	 * QueryPart.determineValue() will be used.
	 * If rightside is QueryPartJsonMemberAccess, the next level will be fetched recursively;
	 * 
	 * To whomever dares to touch the code of this method: MAKE SURE TO THOUROUGHLY TEST THE CHANGES!
	 * 
	 ******************************************************************************************************/
	public JsonElement accessMemberRecursively(EnhancedJsonObject object, JsonElement currentElement) {
		
		//#############################################################################
		// Handle Leftside, resolve json member
		//#############################################################################
		
		JsonElement nextElement = null;	

		//--------------------------
		// Handle Function Call
		if(leftside instanceof QueryPartFunction){
			QueryPartValue functionResult = leftside.determineValue(object);
			QueryPartValueType type = functionResult.type();

			if( type.equals(QueryPartValueType.JSON) ) {
				nextElement = functionResult.getAsJsonElement();
			}else {
				
				nextElement = currentElement;
				rightside = new QueryPartJsonMemberAccess(context, functionResult.determineValue(object), rightside);
				
			}
		}
		
		//--------------------------
		// Handle JsonMemberAccess
		else if(leftside instanceof QueryPartJsonMemberAccess){
			
			QueryPartJsonMemberAccess accessExpression = (QueryPartJsonMemberAccess)leftside;
			nextElement = accessExpression.accessMemberRecursively(object, currentElement);
		}

		//--------------------------
		// Handle Null
		else if(object == null) {
			
			if(!(rightside instanceof QueryPartArray)) {
				return new JsonPrimitive(leftside+"."+rightside.determineValue(null));
			}else {
				return new JsonPrimitive(leftside+"."+rightside.determineValue(null));
			}
		}
		//--------------------------
		// Handle Not Null
		else if(currentElement != null) {

			//--------------------------
			// Handle JsonArray
			if(currentElement.isJsonArray() && (leftside instanceof QueryPartArray) ){
				
				QueryPartArray arrayExpression = (QueryPartArray)leftside;
				nextElement = arrayExpression.getElementOfJsonArray(
					object, currentElement.getAsJsonArray()
				);
			}
			
			//--------------------------
			// JsonObject: access with object.fieldname
			else if(currentElement.isJsonObject() && !(leftside instanceof QueryPartArray) ) {
				
				JsonObject jsonObject = currentElement.getAsJsonObject();
				String memberName = ((QueryPart)leftside).determineValue(object).getAsString();
				
				if(jsonObject.has(memberName)) {
	
					nextElement = jsonObject.get(memberName);
				}else {
					return JsonNull.INSTANCE;
				}
				
			}
			//--------------------------
			// JsonObject: access member with object.[fieldname]...
			else if(currentElement.isJsonObject() && (leftside instanceof QueryPartArray) ) {
				ArrayList<QueryPart> partsArray = ((QueryPartArray)leftside).getAsParts();
				nextElement = getMemberByFieldnameInArray(object, currentElement, partsArray);
			}
			
			//--------------------------
			// Use current if Leftside is null
			else if(leftside == null){
				nextElement = currentElement;
			}
			
			//--------------------------
			// Mighty Error Expression
			else {
				context.addMessage(MessageType.ERROR,"Could not access object member: "+leftside+"."+rightside);
			}
		}
		

		
		//#############################################################################
		// Handle Rightside, resolve value or next level
		//#############################################################################
		if(rightside instanceof QueryPartJsonMemberAccess) {
			return ((QueryPartJsonMemberAccess)rightside).accessMemberRecursively(object, nextElement);
		}else {
			if(nextElement == null || nextElement.isJsonNull()){
				return null;
			}else {
				if(nextElement.isJsonArray() && (rightside instanceof QueryPartArray) ){
					JsonElement valueOfMember = ((QueryPartArray)rightside).getElementOfJsonArray(
							object, nextElement.getAsJsonArray()
						);

					return valueOfMember;
				
				//--------------------------
				// JsonObject: access member with object.membername
				}else if(nextElement.isJsonObject() && !(rightside instanceof QueryPartArray) ) {
					JsonElement valueOfMember = nextElement.getAsJsonObject().get(rightside.determineValue(object).getAsString());
					return valueOfMember;
				}
				//--------------------------
				// JsonObject: access member with object.[fieldname]
				else if(nextElement.isJsonObject() && (rightside instanceof QueryPartArray) ) {
					ArrayList<QueryPart> partsArray = ((QueryPartArray)rightside).getAsParts();
					return getMemberByFieldnameInArray(object, nextElement, partsArray);
					
				}
				
			}
		}	
		
		//maybe change or add warning?
		return null;
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	private JsonElement getMemberByFieldnameInArray(EnhancedJsonObject rootObject, JsonElement theElement, ArrayList<QueryPart> partsArray) {
		JsonObject jsonObject = theElement.getAsJsonObject();
		
		if(partsArray.isEmpty()) {
			return JsonNull.INSTANCE;
		}else {
			QueryPart memberNamePart = partsArray.get(0);
			
			if(memberNamePart instanceof QueryPartValue) {
				
				QueryPartValue memberNameValue = ((QueryPartValue)memberNamePart).convertFieldnameToFieldvalue(rootObject);
				String memberName = memberNameValue.getAsString();

				if(memberName != null && jsonObject.has(memberName)) {
					theElement = jsonObject.get(memberName);
					return theElement;
				}else {
					return JsonNull.INSTANCE;
				}
			}else {
				return JsonNull.INSTANCE;
			}
			
			
		}
	}
	
	
	/******************************************************************************************************
	 * Determines the member based on this member expression and sets the specified value.
	 * 
	 * 
	 ******************************************************************************************************/
	public boolean setValueOfMember(EnhancedJsonObject object, JsonElement valueToSet) {
		return setValueRecursively(object, object.getWrappedObject(), valueToSet);
	}
	
	/******************************************************************************************************
	 * Determines the member based on this member expression and sets the specified value.
	 * 
	 * 
	 ******************************************************************************************************/
	public boolean setValueRecursively(EnhancedJsonObject object, JsonElement currentElement, JsonElement valueToSet) {
		
		//======================================================
		// Handle Leftside, resolve json member
		//======================================================
		
		JsonElement nextElement = null;
		
		//--------------------------
		// Handle Functions
		if(leftside instanceof QueryPartFunction){
			QueryPartValue value = leftside.determineValue(object);
			if(value.isJson()) {
				nextElement = value.getAsJsonElement();
			}

		}else if(leftside instanceof QueryPartJsonMemberAccess){
			//--------------------------
			// Handle JsonMemberAccess
			nextElement = ((QueryPartJsonMemberAccess)leftside).accessMemberRecursively(object, currentElement);
			

		}
		else if(currentElement.isJsonArray() && (leftside instanceof QueryPartArray) ){
			//--------------------------
			// Handle JsonArray
			QueryPartArray arrayExpression = (QueryPartArray)leftside;
			nextElement = arrayExpression.getElementOfJsonArray(
					object, currentElement.getAsJsonArray()
			);
			
			//---------------------------
			//Create if not exists
			if(nextElement == null || nextElement.isJsonNull()) {
				nextElement = createNextElementInHierarchy();
				currentElement.getAsJsonArray().add(nextElement);
			}

		}
		
		else if(currentElement.isJsonObject() && !(leftside instanceof QueryPartArray) ) {
			//--------------------------
			// Handle JsonObject
			JsonObject jsonObject = currentElement.getAsJsonObject();
			String memberName = ((QueryPartValue)leftside).getAsString();

			if(jsonObject.has(memberName)) {
				nextElement = jsonObject.get(memberName);
			}else {
				//---------------------------
				//Create if not exists
				nextElement = createNextElementInHierarchy();
				jsonObject.add(memberName, nextElement);

			}
			
		}
		
		//--------------------------
		// Use current if Leftside is null
		else if(leftside == null){
			nextElement = currentElement;
		}
		
		//--------------------------
		// Mighty Error Expression
		else {
			CFW.Messages.addWarningMessage("Could not access object member: "+leftside+"."+rightside);
		}
		
		//======================================================
		// Handle Rightside, set value or next level
		//======================================================
		if(rightside instanceof QueryPartJsonMemberAccess) {
			return ((QueryPartJsonMemberAccess)rightside).setValueRecursively(object, nextElement, valueToSet);
		}else {
			if(nextElement == null || nextElement.isJsonNull()){
				return false;
			}else {
				if(nextElement.isJsonArray() && (rightside instanceof QueryPartArray) ){
					QueryPartArray arrayPart = (QueryPartArray)rightside;
	
					if(arrayPart.isIndex(object)) {
						Integer index = arrayPart.getIndex(object);
						JsonArray array = nextElement.getAsJsonArray();
						if(array.size() > 0 && index < array.size()) {
							array.set(index, valueToSet);
						}else {
							array.add(valueToSet);
						}
						return true;
					}else {
						context.addMessage(MessageType.WARNING, "Unrecognized value for index: '"+arrayPart.determineValue(object)+"'");
						return false;
					}
					
				}else if(nextElement.isJsonObject() && !(rightside instanceof QueryPartArray) ) {
					String newMemberName = rightside.determineValue(object).getAsString();
					nextElement.getAsJsonObject()
						.add(newMemberName, valueToSet);
					return true;
				}
			}
			
		}
		
		return false;
	}

	/******************************************************************************************************
	 * Internal method for creating structure for setValueRecursively.
	 * 
	 ******************************************************************************************************/
	private JsonElement createNextElementInHierarchy() {
		JsonElement nextElement;
		if( (rightside instanceof QueryPartArray)
		|| (
				rightside instanceof QueryPartJsonMemberAccess 
				&& ((QueryPartJsonMemberAccess)rightside).isLeftsideArrayPart() 
			)
		) {
			nextElement = new JsonArray();
		}else{
			nextElement = new JsonObject();
		}
		return nextElement;
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	@Override
	public JsonObject createDebugObject(EnhancedJsonObject object) {
		
		JsonObject debugObject = new JsonObject();
		
		debugObject.addProperty(QueryPart.FIELD_PARTTYPE, "JsonMemberAccess");
		debugObject.add("leftside", leftside.createDebugObject(object));
		debugObject.add("rightside", rightside.createDebugObject(object));
		
		// use clones to avoid potential issues caused by changing states
		debugObject.add("leftEvaluated", leftside.clone().determineValue(object).getAsJsonElement());
		debugObject.add("rightEvaluated", rightside.clone().determineValue(object).getAsJsonElement());
		return debugObject;
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	@Override
	public void setParentCommand(CFWQueryCommand parent) {
		
		this.parent = parent;
		if(leftside != null) {	this.leftside.setParentCommand(parent); }
		if(rightside != null) {	this.rightside.setParentCommand(parent); }
		
	}
	

}
