package com.xresch.cfw.features.query.parse;

import com.google.gson.JsonObject;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.EnhancedJsonObject;

/**************************************************************************************************************
 * 
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class QueryPartGroup extends QueryPart {
	
	private QueryPart leftside;
	private QueryPart value = null;
		
	/******************************************************************************************************
	 * 
	 * @param leftside The name on the left side of the assignment operation.
	 * 
	 ******************************************************************************************************/
	private QueryPartGroup(CFWQueryContext context, QueryPart leftside, QueryPart value) {
		super(context);
		this.leftside = leftside;
		this.value = value;
	}
	
	/******************************************************************************************************
	 * Returns the left side of the assignment operation.
	 * 
	 ******************************************************************************************************/
	public QueryPart getLeftSide() {
		return leftside;
	}

	/******************************************************************************************************
	 * Determines and returns the right side of the value of the assignment.
	 * 
	 ******************************************************************************************************/
	@Override
	public QueryPartValue determineValue(EnhancedJsonObject object) {
		return value.determineValue(null);
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	@Override
	public JsonObject createDebugObject(EnhancedJsonObject object) {
		
		JsonObject debugObject = new JsonObject();

		debugObject.addProperty("partType", "Group");
		
		//TODO
//		debugObject.add("leftside", leftside.createDebugObject(object));
//		debugObject.add("rightside", leftside.createDebugObject(object));

		return debugObject;
	}
	

}
