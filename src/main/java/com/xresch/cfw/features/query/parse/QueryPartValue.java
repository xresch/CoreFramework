package com.xresch.cfw.features.query.parse;

import java.math.BigDecimal;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
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
		, NULL
	}
			
	/******************************************************************************************************
	 * Private Constructor to enforce correct types.
	 * 
	 ******************************************************************************************************/
	private QueryPartValue(CFWQueryContext context, QueryPartValueType type, Object value) {
		super(context);
		this.value = value;
		if(value != null) {
			this.type = type;
		}else {
			this.type = QueryPartValueType.NULL;
		}
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
		return new QueryPartValue(context, QueryPartValueType.NULL, null);
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
	 * Creates a new QueryPart based on the type of the JsonElement
	 ******************************************************************************************************/
	public static QueryPartValue newFromJsonElement(CFWQueryContext context, JsonElement value){
		
		if(value.isJsonNull()) {
			return newNull(context); 
		}
		
		if(value.isJsonPrimitive()) {
			JsonPrimitive primitive = value.getAsJsonPrimitive();
			
			if(primitive.isBoolean()) {		return newBoolean(context, value.getAsBoolean()); }
			if(primitive.isNumber()) {		return newNumber(context, value.getAsBigDecimal()); }
			if(primitive.isString()) {		return newString(context, value.getAsString()); }
		}
		
		return newJson(context, value);
		
			
		
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
	 * If the value is null, change it to a number of value zero.
	 ******************************************************************************************************/
	public void nullToZero() {
		if(value == null) {
			this.value = 0;
			this.type = QueryPartValueType.NUMBER;
		}
	}
	/******************************************************************************************************
	 * Check if the value is null
	 ******************************************************************************************************/
	public boolean isNull() {
		return value == null;
	}
	
	/******************************************************************************************************
	 * Check if the value is of type boolean
	 ******************************************************************************************************/
	public boolean isBoolean() {
		return this.type == QueryPartValueType.BOOLEAN;
	}
	
	/******************************************************************************************************
	 * Check if the value is string representation of a boolean. 
	 ******************************************************************************************************/
	public boolean isBooleanString() {
		
		if(this.type == QueryPartValueType.STRING) {
			String value = this.getAsString().trim().toLowerCase();
			return  value.equals("true") || value.equals("false")   ;
		}
		
		return false;
	}
	
	/******************************************************************************************************
	 * Check if the value is string representation of a boolean. 
	 ******************************************************************************************************/
	public boolean isBoolOrBoolString() {
				
		return this.isBoolean() || this.isBooleanString();
	}
	
	/******************************************************************************************************
	 * Check if the value is a number
	 ******************************************************************************************************/
	public boolean isNumber() {
		return this.type == QueryPartValueType.NUMBER;
	}
	
	
	/******************************************************************************************************
	 * Check if the value is a number
	 ******************************************************************************************************/
	public boolean isNumberString() {
		if(this.type == QueryPartValueType.STRING) {
			String value = this.getAsString().trim();
			
			try{
				Double.parseDouble(value);
				return true;
			}catch(Exception e){
				return false;
			}

		}
		
		return false;

	}
	
	/******************************************************************************************************
	 * Check if the value is a number or a number string
	 ******************************************************************************************************/
	public boolean isNumberOrNumberString() {
				
		return this.isNumber() || this.isNumberString();
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
	 * Check if the value is null
	 ******************************************************************************************************/
	public boolean isJsonArray() {
		return this.type == QueryPartValueType.JSON && ((JsonElement)this.value).isJsonArray();
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
			
			case STRING:	return Double.parseDouble((String)value);
			
			case JSON:		return ((JsonElement)value).getAsBigDecimal();
				
			default:		return null;

		}

	}
	
	/******************************************************************************************************
	 * It is recommended to use isInteger() first to make sure number value is really a Integer.
	 ******************************************************************************************************/
	public Integer getAsInteger() {
			
		Number number = this.getAsNumber();
		
		if(number == null) return null;
		
		return number.intValue();

	}
	
	
	/******************************************************************************************************
	 * It is recommended to use isInteger() first to make sure number value is really a Integer.
	 ******************************************************************************************************/
	public Float getAsFloat() {
			
		Number number = this.getAsNumber();
		
		if(number == null) return null;
		
		return number.floatValue();

	}
	
	/******************************************************************************************************
	 * It is recommended to use isInteger() first to make sure number value is really a Integer.
	 ******************************************************************************************************/
	public Double getAsDouble() {
			
		Number number = this.getAsNumber();
		
		if(number == null) return null;
		
		return number.doubleValue();

	}
	
	/******************************************************************************************************
	 * It is recommended to use isInteger() first to make sure number value is really a Integer.
	 ******************************************************************************************************/
	public BigDecimal getAsBigDecimal() {
			
		Number number = this.getAsNumber();
		
		if(number == null) return null;
		
		return BigDecimal.valueOf(number.doubleValue());

	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public Boolean getAsBoolean() {
		
		switch(type) {
		
			case BOOLEAN: 	return ((Boolean)value); 
			
			case NUMBER:	return ( ((Number)value).floatValue() == 0) ? false : true;

			case STRING:	return Boolean.parseBoolean((String)value);
			
			case NULL:		return false;
			
			case JSON:		return ((JsonElement)value).getAsBoolean();
				
			default:		return null;

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
			
			case NULL:	return JsonNull.INSTANCE;
			
			default:	return JsonNull.INSTANCE;

		}

	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public JsonArray getAsJsonArray() {
		JsonArray array;
		
		switch(type) {
			case JSON:		return ((JsonElement)value).getAsJsonArray();
			
			case NUMBER:	array = new JsonArray();
			 				array.add((Number)value);
							return array;

			case BOOLEAN: 	array = new JsonArray();
							array.add((Boolean)value);
							return array;
			
			case STRING:	array = new JsonArray();
							array.add((String)value);
							return array;

			default:		return new JsonArray();

		}

	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public String getAsString() {
		if(value == null) return null;
		
		return value.toString();
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	@Override
	public JsonObject createDebugObject(EnhancedJsonObject object) {
		
		JsonObject debugObject = new JsonObject();

		debugObject.addProperty("partType", "Value");
		
		debugObject.add("value", this.getAsJson());

		return debugObject;
	}

}
