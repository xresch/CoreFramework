package com.xresch.cfw.features.query.parse;

import java.util.ArrayList;

import com.google.gson.JsonArray;
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
	
	//holds index if this array is a index access expression(e.g. [1])
	private Integer arrayIndex = null;
	
	/******************************************************************************************************
	 *  
	 ******************************************************************************************************/
	public QueryPartArray(CFWQueryContext context) {
		this(context,  new ArrayList<>());
	}
	
	/******************************************************************************************************
	 *  
	 ******************************************************************************************************/
	public QueryPartArray(CFWQueryContext context, ArrayList<QueryPart> parts) {
		super(context);
		this.partsArray = parts;
	}
	
	/******************************************************************************************************
	 * Returns the left side of the assignment operation.
	 * 
	 ******************************************************************************************************/
	public QueryPartArray add(QueryPart part) {
		partsArray.add(part);
		return this;
	}

	/******************************************************************************************************
	 * Returns the values as QueryPartValue of type JSON containing a JsonArray
	 * 
	 ******************************************************************************************************/
	@Override
	public QueryPartValue determineValue(EnhancedJsonObject object) {
		
		JsonArray array = getAsJsonArray(object);
		
		return QueryPartValue.newJson(this.context(), array);
	}
	

	/******************************************************************************************************
	 * Returns the values as JsonArray.
	 * 
	 ******************************************************************************************************/
	public JsonArray getAsJsonArray(EnhancedJsonObject object) {
		
		if(jsonArray == null) {
			jsonArray = new JsonArray();
			
			for(QueryPart part : partsArray) {
				jsonArray.add(part.determineValue(object).getAsJson());
			}
		}
		return jsonArray;
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

	
	
	
	
	

}
