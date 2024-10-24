package com.xresch.cfw.features.query.parse;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.EnhancedJsonObject;

/**************************************************************************************************************
 * QueryPart that will hold the following expressions:
 *  - Comma Separated List of QueryParts, e.g: itemA, 42, function(bla)
 *  - Array of QueryParts, e.g: [itemX, 8008, random(2,3)]
 *  - Array Access Index e.g: [1]
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class QueryPartArray extends QueryPart {
	
	private ArrayList<QueryPart> partsArray;
	private JsonArray jsonArray = null;
	private ArrayList<String> stringArray = null;
	
	//holds index if this array is a index access expression(e.g. [1])
	private Integer arrayIndex = null;
	
	// true if the array was embraced with square braces
	// Used in add()-method to determine whether the array should be unwrapped when added
	private boolean embracedArray = false;
	
	/******************************************************************************************************
	 *  
	 ******************************************************************************************************/
	public QueryPartArray(CFWQueryContext context, ArrayList<QueryPart> parts) {
		super();
		this.partsArray = parts;
	}
	
	/******************************************************************************************************
	 *  
	 ******************************************************************************************************/
	public QueryPartArray(CFWQueryContext context) {
		this(context,  new ArrayList<>());
	}
	
	/******************************************************************************************************
	 *  
	 ******************************************************************************************************/
	public QueryPartArray(CFWQueryContext context, QueryPart... parts) {
		this(context,  new ArrayList<>());
		for(QueryPart part : parts) {
			this.add(part);
		}
	}
	
	/******************************************************************************************************
	 *  Creates an index expression
	 ******************************************************************************************************/
	public QueryPartArray(CFWQueryContext context, int index) {
		this(context);
		this.add(QueryPartValue.newNumber(index));
	}
	
	/******************************************************************************************************
	 * Adds a query part. If the query part is a QueryPartArray, the parts in that array are merged into
	 * this array.
	 * 
	 ******************************************************************************************************/
	public QueryPartArray add(QueryPart part) {
		
		if( !(part instanceof QueryPartArray)) {
			partsArray.add(part);
		}else {
			QueryPartArray array = (QueryPartArray)part;
			if(array.isEmbracedArray()) {
				partsArray.add(part);
			}else {
				//unwrap arrays
				partsArray.addAll(array.getQueryPartsArray());
			}
		}
		return this;
	}

	/******************************************************************************************************
	 * Returns the values as QueryPartValue of type JSON containing a JsonArray
	 * 
	 ******************************************************************************************************/
	@Override
	public QueryPartValue determineValue(EnhancedJsonObject object) {
		
		JsonArray array = getAsJsonArray(object, true);
		
		return QueryPartValue.newJson(array);
	}
	

	/******************************************************************************************************
	 * Returns a copy of the parts as added to the Array, nothing will be evaluated.
	 * 
	 ******************************************************************************************************/
	public ArrayList<QueryPart> getAsParts() {
		
		ArrayList<QueryPart> partsArrayClone = new ArrayList<>();
		partsArrayClone.addAll(partsArray);

		return partsArrayClone;
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
					jsonArray.add(part.determineValue(object).getAsJsonElement());
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
			if((value.isNumber() && value.isInteger()) ) {
				arrayIndex = value.getAsInteger();
				return true;
			}
			
			// Assume is an index if the part is probably evaluating to a number
			QueryPart part = partsArray.get(0);
			if(part instanceof QueryPartBinaryExpression 
			|| part instanceof QueryPartFunction
			|| part instanceof QueryPartGroup
			) {
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
	public QueryPartArray isEmbracedArray(boolean enclosedArray) {
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
		
		debugObject.addProperty(QueryPart.FIELD_PARTTYPE, "Array");
		
		int i = 0;
		for(QueryPart part : partsArray) {
			debugObject.add("Element["+i+"]", part.createDebugObject(object));
			i++;
		}
		
		return debugObject;
	}

	
	
	
	
	

}
