package com.xresch.cfw.features.query.parse;

import com.xresch.cfw.features.query.CFWQueryContext;
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
		super(context);
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
	 * Returns the right side of the assignment operation.
	 * 
	 ******************************************************************************************************/
	public QueryPart getRightSide() {
		return leftside;
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
	 * 
	 ******************************************************************************************************/
	public void assignToJsonObject(EnhancedJsonObject object) {
		
		if (leftside instanceof QueryPartJsonMemberAccess) {
			QueryPartJsonMemberAccess memberAccess = (QueryPartJsonMemberAccess)leftside;
			memberAccess.setValueOfMember(object, rightside.determineValue(object).getAsJson());
		}else {
			String memberName = leftside.determineValue(object).getAsString();
			object.addProperty(memberName, rightside.determineValue(object));
			
		}
	
	}
	
	
	

}
