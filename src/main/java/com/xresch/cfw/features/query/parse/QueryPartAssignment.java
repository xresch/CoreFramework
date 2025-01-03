package com.xresch.cfw.features.query.parse;

import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.xresch.cfw.features.query.CFWQueryCommand;
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
	
	private CFWQueryContext context;
	
	/******************************************************************************************************
	 * Creates a clone of the QueryPart.
	 * 
	 ******************************************************************************************************/
	@Override
	public QueryPartAssignment clone() {
		
		QueryPart cloneLeft = leftside.clone();
		QueryPart cloneRight = rightside.clone();
		
		QueryPartAssignment clone = new QueryPartAssignment(context, cloneLeft, cloneRight);
		clone.parent = this.parent;
		
		return clone;
	}
	
	/******************************************************************************************************
	 * 
	 * @param leftside The name on the left side of the assignment operation.
	 * 
	 ******************************************************************************************************/
	public QueryPartAssignment(CFWQueryContext context, QueryPart leftside, QueryPart rightside) {
		super();
		this.context = context;
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
		return rightside.determineValue(object);
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
		
		//-------------------------------------
		// Determine Value
		QueryPartValue valueToAssign;
		
		if(rightside instanceof QueryPartArray
		|| (rightside instanceof QueryPartValue && ((QueryPartValue)rightside).isJson() )) {
			//make a clone to not assign the same object 
			QueryPartValue tempPart = rightside.determineValue(object);
			valueToAssign = tempPart.getAsClone();
		}else {
			valueToAssign = rightside.determineValue(object).convertFieldnameToFieldvalue(object);
		}
		
		//-------------------------------------
		// Assign Value
		if (leftside instanceof QueryPartJsonMemberAccess) {
			QueryPartJsonMemberAccess memberAccess = (QueryPartJsonMemberAccess)leftside;
			
			memberAccess.setValueOfMember(object, valueToAssign.getAsJsonElement());
		}else {
			String memberName = this.getLeftSideAsString(object);

			object.addProperty(memberName, valueToAssign);
			
		}
	
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	@Override
	public JsonObject createDebugObject(EnhancedJsonObject object) {
		
		JsonObject debugObject = new JsonObject();
		
		debugObject.addProperty(QueryPart.FIELD_PARTTYPE, "Assignment");
		debugObject.add("leftside", leftside.createDebugObject(object));
		debugObject.add("rightside", rightside.createDebugObject(object));
		
		return debugObject;
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	@Override
	public void setParentCommand(CFWQueryCommand parent) {
		
		this.parent = parent;
		this.leftside.setParentCommand(parent);
		this.rightside.setParentCommand(parent);
		
	}

}
