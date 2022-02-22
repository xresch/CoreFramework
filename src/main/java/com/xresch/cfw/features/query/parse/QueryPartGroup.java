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
	
	private ArrayList<QueryPart> partsArray;
	private JsonArray jsonArray = null;
	private ArrayList<String> stringArray = null;
	
	//holds index if this array is a index access expression(e.g. [1])
	private Integer arrayIndex = null;
	
	// true if the array was embraced with square braces
	// Used in add()-method to determine wether the array should be unwrapped when added
	private boolean embracedArray = false;
	
	
	/******************************************************************************************************
	 *  Creates an index expression
	 ******************************************************************************************************/
	public QueryPartGroup(CFWQueryContext context, int index) {
		super(context);
		this.add(QueryPartValue.newNumber(context, index));
	}
	
	/******************************************************************************************************
	 * Adds a query part. If the query part is a QueryPartArray, the parts in that array are merged into
	 * this array.
	 * 
	 ******************************************************************************************************/
	public QueryPartGroup add(QueryPart part) {
		
		if( part instanceof QueryPartBinaryExpression) {
			
			if(partsArray.size() == 1) { 
				//---------------------------------------
				// Merge together if all are Binary Expressions
				QueryPart singlePart = partsArray.get(0);
				if( singlePart instanceof QueryPartBinaryExpression) {
					partsArray.clear();
					partsArray.add(
						new QueryPartBinaryExpression(
							this.context()
						  , singlePart
						  , CFWQueryTokenType.OPERATOR_AND
						  , part)
					);
					return this;
				}
			}
		}else {
			if(part instanceof QueryPartArray) {
				QueryPartGroup array = (QueryPartGroup)part;
				if(!array.isEmbracedArray()) {
					//unwrap arrays
					partsArray.addAll(array.getQueryPartsArray());
					return this;
				}
			}
		}
		
		// Just add if nothing of above has matched
		partsArray.add(part);
		
		return this;
	}

	/******************************************************************************************************
	 * Returns the values as QueryPartValue of type JSON containing a JsonArray
	 * 
	 ******************************************************************************************************/
	@Override
	public QueryPartValue determineValue(EnhancedJsonObject object) {
		
		if(partsArray.size() == 1) {
			//---------------------------------------
			// Evaluate as Binary
			QueryPart singlePart = partsArray.get(0);
			if( singlePart instanceof QueryPartBinaryExpression) {
				return singlePart.determineValue(object);
			}
				
		}
		
		//Return evaluated array by default
		JsonArray array = getAsJsonArray(object, true);
		return QueryPartValue.newJson(this.context(), array);
		
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
			
			for(QueryPart part : partsArray) {
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
			
			for(QueryPart part : partsArray) {
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
		return partsArray;
	}
	
	/******************************************************************************************************
	 * Returns the values as QueryPartValue of type JSON containing a JsonArray
	 * 
	 ******************************************************************************************************/
	public boolean isIndex() {
		
		if(partsArray.size() == 1) {
			QueryPartValue value = partsArray.get(0).determineValue(null);
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
	 * Return True if the array was embraced with square braces.
	 * 
	 ******************************************************************************************************/
	public boolean isEmbracedArray() {
		return embracedArray;
	}
	
	/******************************************************************************************************
	 * Set to true if the array was embraced with square braces.
	 * 
	 ******************************************************************************************************/
	public QueryPartGroup isEmbracedArray(boolean enclosedArray) {
		this.embracedArray = enclosedArray;
		return this;
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
		
		debugObject.addProperty("partType", "Array");
		
		int i = 0;
		for(QueryPart part : partsArray) {
			debugObject.add("Element["+i+"]", part.createDebugObject(object));
			i++;
		}
		
		return debugObject;
	}

	
	
	
	
	

}
