package com.xresch.cfw.features.query.parse;

import com.google.gson.JsonObject;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryMemoryException;
import com.xresch.cfw.features.query.EnhancedJsonObject;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class QueryPartAssignment extends QueryPart {
	
	private QueryPart leftside;
	private QueryPart rightside = null;
		
	/******************************************************************************************************
	 * 
	 * @param leftside The name on the left side of the assignment operation.
	 * 
	 ******************************************************************************************************/
	public QueryPartAssignment(CFWQueryContext context, QueryPart leftside, QueryPart rightside) {
		super();
		this.leftside = leftside;
		this.rightside = rightside;
	}
		
	/******************************************************************************************************
	 * Returns the left side of the assignment operation.
	 * 
	 ******************************************************************************************************/
	public QueryPart getLeftSide() {
		return leftside;
	}
	
	/******************************************************************************************************
	 * Returns the left side of the assignment operation as a string.
	 * @return a string or null
	 ******************************************************************************************************/
	public String getLeftSideAsString(EnhancedJsonObject object) {
		return leftside.determineValue(object).getAsString();
	}
	
	/******************************************************************************************************
	 * Returns the right side of the assignment operation.
	 * 
	 ******************************************************************************************************/
	public QueryPart getRightSide() {
		return rightside;
	}

	/******************************************************************************************************
	 * Determines and returns the value of the assignment.
	 * 
	 ******************************************************************************************************/
	@Override
	public QueryPartValue determineValue(EnhancedJsonObject object) {
		return rightside.determineValue(null);
	}
	
	/******************************************************************************************************
	 * Determines the value and assigns it to the JsonObject.
	 * @throws CFWQueryMemoryException 
	 * 
	 ******************************************************************************************************/
	public void assignToJsonObject(JsonObject object) throws CFWQueryMemoryException {
		
		this.assignToJsonObject(new EnhancedJsonObject(object));
	
	}
	/******************************************************************************************************
	 * Determines the value and assigns it to the JsonObject.
	 * @throws CFWQueryMemoryException 
	 * 
	 ******************************************************************************************************/
	public void assignToJsonObject(EnhancedJsonObject object) throws CFWQueryMemoryException {
		
		if (leftside instanceof QueryPartJsonMemberAccess) {
			QueryPartJsonMemberAccess memberAccess = (QueryPartJsonMemberAccess)leftside;
			memberAccess.setValueOfMember(object, rightside.determineValue(object).getAsJson());
		}else {
			String memberName = this.getLeftSideAsString(object);
			object.addProperty(memberName, rightside.determineValue(object));
			
		}
	
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	@Override
	public JsonObject createDebugObject(EnhancedJsonObject object) {
		
		JsonObject debugObject = new JsonObject();
		
		debugObject.addProperty("partType", "Assignment");
		debugObject.add("leftside", leftside.createDebugObject(object));
		debugObject.add("rightside", rightside.createDebugObject(object));
		
		return debugObject;
	}

}
