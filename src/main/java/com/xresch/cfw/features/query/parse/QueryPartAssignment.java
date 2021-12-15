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
	private QueryPartAssignment(CFWQueryContext context, QueryPart leftside, QueryPart value) {
		super(context);
		this.leftside = leftside;
		this.rightside = value;
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
		return rightside.determineValue(null);
	}
	
	/******************************************************************************************************
	 * Determines and returns the right side of the value of the assignment.
	 * 
	 ******************************************************************************************************/
	public void assignToJsonObject(EnhancedJsonObject object) {
		
		if (leftside instanceof QueryPartValue) {
			QueryPartValue value = (QueryPartValue)leftside;
			if(value.isString()) {
				object.addProperty(value.getAsString(), rightside);
			}
		}else if(leftside instanceof QueryPartJsonMemberAccess) {
			//TODO
		}
		

	}
	
	
	

}
