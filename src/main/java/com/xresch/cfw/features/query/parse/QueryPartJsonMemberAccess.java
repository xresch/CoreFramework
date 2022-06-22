package com.xresch.cfw.features.query.parse;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryMemoryException;
import com.xresch.cfw.features.query.EnhancedJsonObject;

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
		
	/******************************************************************************************************
	 * 
	 * @param leftside The name on the left side of the assignment operation.
	 * 
	 ******************************************************************************************************/
	public QueryPartJsonMemberAccess(CFWQueryContext context, QueryPart leftside, QueryPart rightside) {
		super();
		this.leftside = leftside;
		this.rightside = rightside;
	}
	
	/******************************************************************************************************
	 * 
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
		if(object == null) {
			return QueryPartValue.newString(leftside+"."+rightside.determineValue(null));
		}else {				
			return getValueOfMember(object, object.getWrappedObject());
		}
	}
	
	public QueryPartValue getValueOfMember(EnhancedJsonObject rootObject, JsonElement currentElement) {
		return QueryPartValue.newFromJsonElement(accessMemberRecursively(rootObject, currentElement)
		);
		
	}
	
	/******************************************************************************************************
	 * Determines and returns the member based on this member expression.
	 * 
	 * If rightside is anything else than a QueryPartJsonMemberAccess, the value returned by 
	 * QueryPart.determineValue() will be used.
	 * If rightside is QueryPartJsonMemberAccess, the next level will be fetched recursively;
	 * 
	 ******************************************************************************************************/
	public JsonElement accessMemberRecursively(EnhancedJsonObject rootObject, JsonElement currentElement) {
		
		//======================================================
		// Handle Leftside, resolve json member
		//======================================================
		
		//--------------------------
		// Handle JsonArray
		JsonElement nextElement = null;
		
		if(currentElement.isJsonArray() && (leftside instanceof QueryPartArray) ){
			
			QueryPartArray arrayExpression = (QueryPartArray)leftside;
			nextElement = arrayExpression.getElementOfJsonArray(
				currentElement.getAsJsonArray()
			);

		}
		
		//--------------------------
		// Handle JsonObject
		else if(currentElement.isJsonObject() && !(leftside instanceof QueryPartArray) ) {
			JsonObject jsonObject = currentElement.getAsJsonObject();
			String memberName = ((QueryPart)leftside).determineValue(rootObject).getAsString();
			System.out.println("1"+memberName);
			if(jsonObject.has(memberName)) {
				System.out.println("2");
				nextElement = jsonObject.get(memberName);
			}else {
				CFW.Messages.addWarningMessage("Member not found: "+leftside+"."+rightside);
				return JsonNull.INSTANCE;
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
		// Handle Rightside, resolve value or next level
		//======================================================
		if(rightside instanceof QueryPartJsonMemberAccess) {
			return ((QueryPartJsonMemberAccess)rightside).accessMemberRecursively(rootObject, nextElement);
		}else {
			System.out.println("a"+nextElement+"/"+rightside);
			if(nextElement == null || nextElement.isJsonNull()){
				
				return null;
			}else {
				if(nextElement.isJsonArray() && (rightside instanceof QueryPartArray) ){
					System.out.println("b");
					JsonElement valueOfMember = ((QueryPartArray)rightside).getElementOfJsonArray(
							nextElement.getAsJsonArray()
						);

					return valueOfMember;
					
				}else if(nextElement.isJsonObject() && !(rightside instanceof QueryPartArray) ) {
					System.out.println("d");
					JsonElement valueOfMember = nextElement.getAsJsonObject().get(rightside.determineValue(rootObject).getAsString());
					return valueOfMember;
				}
			}
		}
		
		//maybe change or add warning?
		return null;
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
	public boolean setValueRecursively(EnhancedJsonObject rootObject, JsonElement currentElement, JsonElement valueToSet) {
		
		//======================================================
		// Handle Leftside, resolve json member
		//======================================================
		
		//--------------------------
		// Handle JsonArray
		JsonElement nextElement = null;
		
		if(currentElement.isJsonArray() && (leftside instanceof QueryPartArray) ){
			
			QueryPartArray arrayExpression = (QueryPartArray)leftside;
			nextElement = arrayExpression.getElementOfJsonArray(
				currentElement.getAsJsonArray()
			);
			
			//---------------------------
			//Create if not exists
			if(nextElement == null || nextElement.isJsonNull()) {
				nextElement = createNextElementInHierarchy();
				currentElement.getAsJsonArray().add(nextElement);
			}

		}
		
		//--------------------------
		// Handle JsonObject
		else if(currentElement.isJsonObject() && !(leftside instanceof QueryPartArray) ) {
			JsonObject jsonObject = currentElement.getAsJsonObject();
			String memberName = ((QueryPartValue)leftside).getAsString();
			System.out.println("1"+memberName);
			if(jsonObject.has(memberName)) {
				System.out.println("2");
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
			return ((QueryPartJsonMemberAccess)rightside).setValueRecursively(rootObject, nextElement, valueToSet);
		}else {
			System.out.println("a"+nextElement+"/"+rightside);

			if(nextElement.isJsonArray() && (rightside instanceof QueryPartArray) ){
				System.out.println("b");
				QueryPartArray arrayPart = (QueryPartArray)rightside;

				if(arrayPart.isIndex()) {
					nextElement.getAsJsonArray().set(arrayPart.getIndex(), valueToSet);
					return true;
				}else {
					System.out.println("Unrecognized value for index: '"+arrayPart.determineValue(rootObject)+"'");
					return false;
				}
				
			}else if(nextElement.isJsonObject() && !(rightside instanceof QueryPartArray) ) {
				String newMemberName = rightside.determineValue(rootObject).getAsString();
				nextElement.getAsJsonObject()
					.add(newMemberName, valueToSet);
				return true;
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
		
		debugObject.addProperty("partType", "JsonMemberAccess");
		debugObject.add("leftside", leftside.createDebugObject(object));
		debugObject.add("rightside", leftside.createDebugObject(object));
		debugObject.add("leftEvaluated", leftside.determineValue(object).getAsJsonElement());
		debugObject.add("rightEvaluated", leftside.determineValue(object).getAsJsonElement());
		return debugObject;
	}
	

}
