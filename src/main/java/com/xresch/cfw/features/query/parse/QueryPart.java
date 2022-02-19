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
	 * Evaluates and determines the value of this QueryPart.
	 * The method has to be implemented to be able to handle null as parameter value.
	 * @throws CFWQueryMemoryException 
	 ***********************************************************************************************/
	public abstract QueryPartValue determineValue(EnhancedJsonObject object);
		

	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public abstract JsonObject createDebugObject(EnhancedJsonObject object);
		

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
