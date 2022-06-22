package com.xresch.cfw.features.query.parse;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.parse.CFWQueryToken.CFWQueryTokenType;

/**************************************************************************************************************
 * QueryPart that will hold the following expressions:
 *  - A List of Binary expressions. AND is implicitly added between expressions if multiple expressions
 *  	are given: 
 *    (someValue != anotherValue [implicit AND] ( myfield == "value"  myNumber < 22 ...) [implicit AND] a < b )
 *    
 *  - An array of various parts, will result in an array when evaluated
 *  	- 
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class QueryPartGroup extends QueryPart {
	
	private CFWQueryContext context;
	private ArrayList<QueryPart> partsGroup;
	private JsonArray jsonArray = null;
	private ArrayList<String> stringArray = null;
	
	//holds index if this array is a index access expression(e.g. [1])
	private Integer arrayIndex = null;
	
	
	/******************************************************************************************************
	 *  
	 ******************************************************************************************************/
	public QueryPartGroup(CFWQueryContext context) {
		this.context = context;
		this.partsGroup = new ArrayList<>();
	}
	
	
	/******************************************************************************************************
	 *  
	 ******************************************************************************************************/
	public QueryPartGroup(CFWQueryContext context, QueryPart initialPart) {
		this(context);
		partsGroup.add(initialPart);
	}
	
	
	/******************************************************************************************************
	 *  
	 ******************************************************************************************************/
	public QueryPartGroup(CFWQueryContext context, QueryPart... parts) {
		super();
		for(QueryPart part : parts) {
			this.add(part);
		}
	}
	
	/******************************************************************************************************
	 *  Creates an index expression
	 ******************************************************************************************************/
	public QueryPartGroup(CFWQueryContext context, int index) {
		this(context);
		this.add(QueryPartValue.newNumber(index));
	}
	
	/******************************************************************************************************
	 * Returns the number of elements in the group.
	 * 
	 ******************************************************************************************************/
	public int size() {
		return partsGroup.size(); 
				
	}
	
	/******************************************************************************************************
	 * Returns true if the QueryPart evaluates to boolean.
	 * 
	 ******************************************************************************************************/
	public static boolean partEvaluatesToBoolean(QueryPart part) {
		return (
				part instanceof QueryPartBinaryExpression
				||  part instanceof QueryPartGroup
				||  (
						part instanceof QueryPartValue
						&& ((QueryPartValue) part).isBoolOrBoolString()
					)
				);
	}
	/******************************************************************************************************
	 * Adds a query part. If the query part is a QueryPartArray, the parts in that array are merged into
	 * this array.
	 * 
	 ******************************************************************************************************/
	public QueryPartGroup add(QueryPart part) {
		
		if(partEvaluatesToBoolean(part)) {
			
			if(partsGroup.size() == 1) { 
				//---------------------------------------
				// Merge together if all are Binary Expressions
				QueryPart existingPart = partsGroup.get(0);
				if(partEvaluatesToBoolean(existingPart)) {
					partsGroup.clear();
					partsGroup.add(
						new QueryPartBinaryExpression(
							this.context
						  , existingPart
						  , CFWQueryTokenType.OPERATOR_AND
						  , part)
					);
					return this;
				}
			}
		}else {
			if(part instanceof QueryPartArray) {
				QueryPartArray array = (QueryPartArray)part;
				if(!array.isEmbracedArray()) {
					//unwrap arrays
					partsGroup.addAll(array.getQueryPartsArray());
					return this;
				}
			}
		}
		
		// Just add if nothing of above has matched
		partsGroup.add(part);
		
		return this;
	}

	/******************************************************************************************************
	 * Returns the values as QueryPartValue of type JSON containing a JsonArray
	 * 
	 ******************************************************************************************************/
	@Override
	public QueryPartValue determineValue(EnhancedJsonObject object) {

		if(partsGroup.size() == 1) {
			//---------------------------------------
			// Evaluate as Binary
			QueryPart singlePart = partsGroup.get(0);
			if(partEvaluatesToBoolean(singlePart)) {
				return singlePart.determineValue(object);
			}
				
		}
		
		//Return evaluated array by default
		JsonArray array = getAsJsonArray(object, true);
		return QueryPartValue.newJson(array);
		
	}
	

	/******************************************************************************************************
	 * Returns the values as JsonArray.
	 * @param getFromCache TODO
	 * 
	 ******************************************************************************************************/
	public JsonArray getAsJsonArray(EnhancedJsonObject object, boolean getFromCache) {
		
		//cache instance
		if(!getFromCache || jsonArray == null) {
			jsonArray = new JsonArray();
			
			for(QueryPart part : partsGroup) {
				if(part != null) {
					jsonArray.add(part.determineValue(object).getAsJson());
				}
			}
		}
		return jsonArray;
	}
	
	/******************************************************************************************************
	 * Returns the values as JsonArray.
	 * 
	 ******************************************************************************************************/
	public ArrayList<String> getAsStringArray(EnhancedJsonObject object, boolean getFromCache) {
		
		//cache instance
		if(!getFromCache || stringArray == null) {
			stringArray = new ArrayList<>();
			
			for(QueryPart part : partsGroup) {
				if(part != null) {
					stringArray.add(part.determineValue(object).getAsString());
				}
			}
		}
		return stringArray;
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	protected ArrayList<QueryPart> getQueryPartsArray() {
		return partsGroup;
	}
	
	/******************************************************************************************************
	 * Returns the values as QueryPartValue of type JSON containing a JsonArray
	 * 
	 ******************************************************************************************************/
	public boolean isIndex() {
		
		if(partsGroup.size() == 1) {
			QueryPartValue value = partsGroup.get(0).determineValue(null);
			if(value.isNumber() && value.isInteger()) {
				arrayIndex = value.getAsInteger();
				return true;
			}
		}
		
		return false;
	}
	
	/******************************************************************************************************
	 * isIndex() has to be called first before this method will return a correct result.
	 * 
	 ******************************************************************************************************/
	public Integer getIndex() {
		return arrayIndex;
	}
	
	/******************************************************************************************************
	 * Returns the element in the array represented by the index of this QueryPartArray.
	 * Returns a JsonNull object if not resolvable.
	 * 
	 ******************************************************************************************************/
	public JsonElement getElementOfJsonArray(JsonArray array) {
		
		if(this.isIndex()) {
			int index = this.getIndex();
			
			if(index < array.size()) {
				return array.get(index);
			}else {
				CFW.Messages.addWarningMessage("Array index out of bounds.");
			}
		}else {
			CFW.Messages.addWarningMessage("Array Expression is not an index.");
		}
		
		return JsonNull.INSTANCE;
	}
	
	
	@Override
	public JsonObject createDebugObject(EnhancedJsonObject object) {
		
		JsonObject debugObject = new JsonObject();
		
		debugObject.addProperty("partType", "Group");
		
		int i = 0;
		for(QueryPart part : partsGroup) {
			if(part != null) {
				debugObject.add("Element["+i+"]", part.createDebugObject(object));
			}else {
				debugObject.add("Element["+i+"]", null);
			}
			i++;
		}
		
		return debugObject;
	}

	
	
	
	
	

}
