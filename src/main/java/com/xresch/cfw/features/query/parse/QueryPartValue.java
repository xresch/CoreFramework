package com.xresch.cfw.features.query.parse;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class QueryPartValue extends QueryPart {
	
	private QueryPartValueType type;
	private Object value = null;
	
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
	private QueryPartValue(QueryPartValueType type, Object value) {
		this.type = type;
		this.value = value;
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public static QueryPartValue newNumber(Number value){
		return new QueryPartValue(QueryPartValueType.NUMBER, value);
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public static QueryPartValue newString(String value){
		return new QueryPartValue(QueryPartValueType.STRING, value);
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public static QueryPartValue newBoolean(Boolean value){
		return new QueryPartValue(QueryPartValueType.BOOLEAN, value);
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public static QueryPartValue newJson(JsonElement value){
		return new QueryPartValue(QueryPartValueType.JSON, value);
	}

	/******************************************************************************************************
	 * From QueryPart, returns this instance if it is a value.
	 ******************************************************************************************************/
	@Override
	public QueryPartValue determineValue() {
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
	 * Check if the value is null
	 ******************************************************************************************************/
	public boolean isBoolean() {
		return this.type == QueryPartValueType.BOOLEAN;
	}
	
	/******************************************************************************************************
	 * Check if the value is null
	 ******************************************************************************************************/
	public boolean isNumber() {
		return this.type == QueryPartValueType.NUMBER;
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
