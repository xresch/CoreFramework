package com.xresch.cfw.features.query.parse;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
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
		super(context);
		this.leftside = leftside;
		this.rightside = rightside;
	}
	
	/******************************************************************************************************
	 * If object is null, return the member expression as a string representation.
	 * If object is not null, determines and returns the value of the member based on the member expression.
	 * 
	 * If rightside is anything else than a QueryPartJsonMemberAccess, the value returned by 
	 * QueryPart.determineValue() will be used.
	 * If rightside is QueryPartJsonMemberAccess, the next level will be fetched recursively;
	 * 
	 ******************************************************************************************************/
	@Override
	public QueryPartValue determineValue(EnhancedJsonObject object) {	
		if(object == null) {
			return QueryPartValue.newString(this.context(), leftside+"."+rightside.determineValue(null));
		}else {				
			return getValueOfMember(object, object.getWrappedObject());
		}
	}
	
	
	/******************************************************************************************************
	 * Determines and returns the member based on this member expression.
	 * 
	 * If rightside is anything else than a QueryPartJsonMemberAccess, the value returned by 
	 * QueryPart.determineValue() will be used.
	 * If rightside is QueryPartJsonMemberAccess, the next level will be fetched recursively;
	 * 
	 ******************************************************************************************************/
	public QueryPartValue getValueOfMember(EnhancedJsonObject rootObject, JsonElement currentElement) {
		
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
			String memberName = ((QueryPartValue)leftside).getAsString();
			System.out.println("1"+memberName);
			if(jsonObject.has(memberName)) {
				System.out.println("2");
				nextElement = jsonObject.get(memberName);
			}else {
				CFW.Messages.addErrorMessage("Member not found: "+leftside+"."+rightside);
				return QueryPartValue.newNull(this.context());
			}
			
		}
		
		//--------------------------
		// Mighty Error Expression
		else {
			CFW.Messages.addErrorMessage("Could not access object member: "+leftside+"."+rightside);
		}
		
		
		//======================================================
		// Handle Rightside, resolve value or next level
		//======================================================
		if(rightside instanceof QueryPartJsonMemberAccess) {
			return ((QueryPartJsonMemberAccess)rightside).getValueOfMember(rootObject, nextElement);
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
					System.out.println("c");
					return QueryPartValue.newFromJsonElement(this.context(), valueOfMember);
					
				}else if(nextElement.isJsonObject() && !(rightside instanceof QueryPartArray) ) {
					System.out.println("d");
					JsonElement valueOfMember = nextElement.getAsJsonObject().get(rightside.determineValue(rootObject).getAsString());
					return QueryPartValue.newFromJsonElement(this.context(), valueOfMember);
				}
			}
		}
		
		//maybe change or add warning?
		return null;
	}
	
	
	/******************************************************************************************************
	 * Determines and returns the member based on this member expression.
	 * 
	 * If rightside is anything else than a QueryPartJsonMemberAccess, the value returned by 
	 * QueryPart.determineValue() will be used.
	 * If rightside is QueryPartJsonMemberAccess, the next level will be fetched recursively;
	 * 
	 ******************************************************************************************************/
	public void setValueOfMember(JsonElement parent, boolean createIfNotExists) {

		//TODO
	}
	
	
	

}
