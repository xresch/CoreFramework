package com.xresch.cfw.features.query.parse;

import com.xresch.cfw.features.query.CFWQueryContext;

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
	public QueryPartValue determineValue() {
		return value.determineValue();
	}
	
	
	

}
