package com.xresch.cfw.features.query.parse;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class QueryPartValue<T> extends QueryPart {
	
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
	public static QueryPartValue<Number> newNumber(Number value){
		return new QueryPartValue<Number>(QueryPartValueType.NUMBER, value);
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public static QueryPartValue<String> newString(String value){
		return new QueryPartValue<String>(QueryPartValueType.STRING, value);
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public static QueryPartValue<Boolean> newBoolean(Boolean value){
		return new QueryPartValue<Boolean>(QueryPartValueType.BOOLEAN, value);
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public static QueryPartValue<JsonElement> newJSON(JsonElement value){
		return new QueryPartValue<JsonElement>(QueryPartValueType.JSON, value);
	}

	/******************************************************************************************************
	 * From QueryPart, returns this instance if it is a value.
	 ******************************************************************************************************/
	@Override
	public QueryPartValue<T> determineValue() {
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
	public T getValue() {
		return (T)value;
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
