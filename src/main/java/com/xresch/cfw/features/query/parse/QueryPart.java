package com.xresch.cfw.features.query.parse;

import com.google.gson.JsonObject;
import com.xresch.cfw.features.query.CFWQueryCommand;
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
	
	// The command which this query part is part of, will be set by the parser
	protected CFWQueryCommand parent;
	
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
	 * Creates a clone of the QueryPart.
	 * 
	 ***********************************************************************************************/
	public abstract QueryPart clone();
		
	/***********************************************************************************************
	 * Implement this method so that it sets the Parent command of itself and all it's child parts.
	 ***********************************************************************************************/
	public abstract void setParentCommand(CFWQueryCommand parent);
	

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

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	protected CFWQueryCommand getParentCommand() {
		return parent;
	}

	
	
	
	
	
	
	
}
