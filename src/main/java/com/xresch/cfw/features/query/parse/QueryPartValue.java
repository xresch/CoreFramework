package com.xresch.cfw.features.query.parse;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.EnhancedJsonObject;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class QueryPartValue extends QueryPart {
	
	private QueryPartValueType type;
	private Object value = null;
	
	//Store IntegerValue after isInteger()
	private Integer integerValue;
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public enum QueryPartValueType{
		  NUMBER
		, STRING
		, BOOLEAN
		, JSON
	}
			
	/******************************************************************************************************
	 * Private Constructor to enforce correct types.
	 * 
	 ******************************************************************************************************/
	private QueryPartValue(CFWQueryContext context, QueryPartValueType type, Object value) {
		super(context);
		this.type = type;
		this.value = value;
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public static QueryPartValue newNumber(CFWQueryContext context, Number value){
		return new QueryPartValue(context, QueryPartValueType.NUMBER, value);
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public static QueryPartValue newString(CFWQueryContext context, String value){
		return new QueryPartValue(context,QueryPartValueType.STRING, value);
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public static QueryPartValue newNull(CFWQueryContext context){
		return new QueryPartValue(context,QueryPartValueType.STRING, null);
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public static QueryPartValue newBoolean(CFWQueryContext context, Boolean value){
		return new QueryPartValue(context, QueryPartValueType.BOOLEAN, value);
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public static QueryPartValue newJson(CFWQueryContext context, JsonElement value){
		return new QueryPartValue(context, QueryPartValueType.JSON, value);
	}

	/******************************************************************************************************
	 * From QueryPart, returns this instance if it is a value.
	 ******************************************************************************************************/
	@Override
	public QueryPartValue determineValue(EnhancedJsonObject object) {
		return this;
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public QueryPartValueType type() {
		return type;
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public boolean isOfType(QueryPartValueType type) {
		return this.type == type;
	}
	
	/******************************************************************************************************
	 * Check if the value is null
	 ******************************************************************************************************/
	public boolean isNull() {
		return value == null;
	}
	
	/******************************************************************************************************
	 * Check if the value is boolean
	 ******************************************************************************************************/
	public boolean isBoolean() {
		return this.type == QueryPartValueType.BOOLEAN;
	}
	
	/******************************************************************************************************
	 * Check if the value is a number
	 ******************************************************************************************************/
	public boolean isNumber() {
		return this.type == QueryPartValueType.NUMBER;
	}
	
	
	/******************************************************************************************************
	 * Check if the value is null
	 ******************************************************************************************************/
	public boolean isInteger() {
		
		boolean isItReally = false;
		
		switch(type) {
			case NUMBER:	
				Number number = this.getAsNumber();
				Double doubleValue = number.doubleValue();
				
				if(Math.floor(doubleValue) == doubleValue) {
					
					isItReally = true;
				};
				break;
		
			case STRING:	
				try{
					Integer.parseInt((String)value);
					isItReally = true;
				}catch(Exception e) {
					isItReally = false;
				}
				break;
				
			default:		isItReally = false;

		}
		return isItReally;
	}
	
	/******************************************************************************************************
	 * Check if the value is null
	 ******************************************************************************************************/
	public boolean isString() {
		return this.type == QueryPartValueType.STRING;
	}
	
	/******************************************************************************************************
	 * Check if the value is null
	 ******************************************************************************************************/
	public boolean isJson() {
		return this.type == QueryPartValueType.JSON;
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	@SuppressWarnings("unchecked")
	public Object getValue() {
		return value;
	}
	
	
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public Number getAsNumber() {
		
		switch(type) {
			case NUMBER:	return ((Number)value);
	
			case BOOLEAN: 	return ((Boolean)value)  ? 1 : 0; 
			
			case STRING:	return Float.parseFloat((String)value);
			
			case JSON:		return ((JsonElement)value).getAsBigDecimal();
				
			default:		throw new IllegalStateException("This code should not have been reached");

		}

	}
	
	/******************************************************************************************************
	 * It is recommended to use isInteger() first to make sure number value is really a Integer.
	 ******************************************************************************************************/
	public Integer getAsInteger() {
			
		switch(type) {
			case NUMBER:	return ((Number)value).intValue();
	
			case BOOLEAN: 	return ((Boolean)value)  ? 1 : 0; 
			
			case STRING:	return Integer.parseInt((String)value);
			
			case JSON:		return ((JsonElement)value).getAsInt();
				
			default:		throw new IllegalStateException("This code should not have been reached");

		}

	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public Boolean getAsBoolean() {
		
		switch(type) {
		
			case BOOLEAN: 	return ((Boolean)value); 
			
			case NUMBER:	return ( ((Number)value).floatValue() == 0) ? false : true;

			case STRING:	return Boolean.parseBoolean((String)value);
			
			case JSON:		return ((JsonElement)value).getAsBoolean();
				
			default:		throw new IllegalStateException("This code should not have been reached");

		}

	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public JsonElement getAsJson() {
		
		switch(type) {
			case JSON:		return ((JsonElement)value);
			
			case NUMBER:	return new JsonPrimitive((Number)value);

			case BOOLEAN: 	return new JsonPrimitive((Boolean)value);
			
			case STRING:	return new JsonPrimitive((String)value);
			
			default:		throw new IllegalStateException("This code should not have been reached");

		}

	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public String getAsString() {
		return value.toString();
	}

}
