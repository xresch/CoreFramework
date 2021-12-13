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
	private QueryPartJsonMemberAccess(CFWQueryContext context, QueryPart leftside, QueryPart rightside) {
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
							
			
		}
		return rightside.determineValue(null);
	}
	
	
	/******************************************************************************************************
	 * Determines and returns the member based on this member expression.
	 * 
	 * If rightside is anything else than a QueryPartJsonMemberAccess, the value returned by 
	 * QueryPart.determineValue() will be used.
	 * If rightside is QueryPartJsonMemberAccess, the next level will be fetched recursively;
	 * 
	 ******************************************************************************************************/
	@Override
	public QueryPartValue getValueOfMember(JsonElement currentElement) {
		
		//======================================================
		// Handle Leftside, resolve json member
		//======================================================
		
		//--------------------------
		// Handle JsonArray
		JsonElement nextElement = null;
		
		if(currentElement.isJsonArray() && (leftside instanceof QueryPartArray) ){
			
			QueryPartArray arrayExpression = (QueryPartArray)leftside;
			
			if(arrayExpression.isIndex()) {
				JsonArray array = currentElement.getAsJsonArray();
				int index = arrayExpression.getIndex();
				
				if(index < array.size()) {
					nextElement = array.get(index);
				}else {
					CFW.Messages.addErrorMessage("Array index out of bounds: "+index);
				}
			}else {
				CFW.Messages.addErrorMessage("Array Expression is not an index: "+leftside);
			}
			
		}
		//--------------------------
		// Handle JsonObject
		else if(currentElement.isJsonObject() && leftside instanceof QueryPartValue ) {
			JsonObject jsonObject = currentElement.getAsJsonObject();
			String memberName = ((QueryPartValue)leftside).getAsString();
			
			if(jsonObject.has(memberName)) {
				nextElement = jsonObject.get(memberName);
			}else {
				CFW.Messages.addErrorMessage("Object member not found: "+leftside+"."+rightside);
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
			return ((QueryPartJsonMemberAccess)rightside).getValueOfMember(nextElement);
		}else {
			
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
	@Override
	public void setValueOfMember(JsonElement parent, boolean createIfNotExists) {

		return rightside.determineValue(null);
	}
	
	
	

}
