package com.xresch.cfw.features.query.parse;

import java.util.Objects;

import com.google.gson.JsonObject;
import com.xresch.cfw.features.query.CFWQueryMemoryException;
import com.xresch.cfw.features.query.EnhancedJsonObject;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public abstract class QueryPart {
	
	public static final String FIELD_PARTTYPE = "partType";
	
	private int cursorPosition = 0;
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public QueryPart() {

	}
	
	/***********************************************************************************************
	 * Evaluates and determines the value of this QueryPart.
	 * The method has to be implemented to be able to handle null as parameter value.
	 * @throws CFWQueryMemoryException 
	 ***********************************************************************************************/
	public abstract QueryPartValue determineValue(EnhancedJsonObject object);
		

	
	/***********************************************************************************************
	 * Return a debug object, the object must at least contain a property with the name
	 * QueryPart.FIELD_PARTTYPE.
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
	@Override
	public String toString() {
		
		return this.determineValue(null).getAsString();
		
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
	
	
	
	
	
}
