package com.xresch.cfw.features.query.parse;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class QueryPartAssignment extends QueryPart {
	
	private String leftside;
	private QueryPart value = null;
		
	/******************************************************************************************************
	 * 
	 * @param leftside The name on the left side of the assignment operation.
	 * 
	 ******************************************************************************************************/
	private QueryPartAssignment(String leftside, QueryPart value) {
		this.leftside = leftside;
		this.value = value;
	}
	
	/******************************************************************************************************
	 * Returns the left side of the assignment operation.
	 * 
	 ******************************************************************************************************/
	public String getLeftSide() {
		return leftside;
	}

	/******************************************************************************************************
	 * Returns the value of the assignment.
	 * 
	 ******************************************************************************************************/
	@Override
	public QueryPartValue determineValue() {
		return value.determineValue();
	}
	
	
	

}
