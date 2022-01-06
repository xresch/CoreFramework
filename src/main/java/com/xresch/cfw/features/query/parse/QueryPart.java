package com.xresch.cfw.features.query.parse;

import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.EnhancedJsonObject;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public abstract class QueryPart {
	
	private int cursorPosition = 0;
	private CFWQueryContext context;
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public QueryPart(CFWQueryContext context) {
		this.context = context;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public abstract QueryPartValue determineValue(EnhancedJsonObject object);
		

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public void position(int cursorPosition) {
		this.cursorPosition = cursorPosition;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public int position() {
		return cursorPosition;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryContext context() {
		return context;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public void context(CFWQueryContext context) {
		this.context = context;
	}
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String toString() {
		return this.determineValue(null).getAsString();
		
	}
	
	
	
}