package com.xresch.cfw.features.query.parse;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.EnhancedJsonObject;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
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
		, NULL
	}
			
	/******************************************************************************************************
	 * Private Constructor to enforce correct types.
	 * 
	 ******************************************************************************************************/
	private QueryPartValue(QueryPartValueType type, Object value) {
		super();
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
	// Overload method to improve performance of getAsBigDecimal()
	public static QueryPartValue newNumber(Integer value){
		if(value == null) { return newNull(); }
		return new QueryPartValue(QueryPartValueType.NUMBER, new BigDecimal(value));
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	// Overload method to improve performance of getAsBigDecimal()
	public static QueryPartValue newNumber(Long value){
		if(value == null) { return newNull(); }
		return new QueryPartValue(QueryPartValueType.NUMBER, new BigDecimal(value));
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	// Overload method to improve performance of getAsBigDecimal()
	public static QueryPartValue newNumber(Short value){
		if(value == null) { return newNull(); }
		return new QueryPartValue(QueryPartValueType.NUMBER, new BigDecimal(value));
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	// Overload method to improve performance of getAsBigDecimal()
	public static QueryPartValue newNumber(Float value){
		if(value == null) { return newNull(); }
		return new QueryPartValue(QueryPartValueType.NUMBER, new BigDecimal(value));
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	// Overload method to improve performance of getAsBigDecimal()
	public static QueryPartValue newNumber(Double value){
		if(value == null) { return newNull(); }
		return new QueryPartValue(QueryPartValueType.NUMBER, new BigDecimal(value));
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public static QueryPartValue newNumber(Number value){
		if(value == null) { return newNull(); }
		return new QueryPartValue(QueryPartValueType.NUMBER, value);
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public static QueryPartValue newString(String value){
		if(value == null) { return newNull(); }
		return new QueryPartValue(QueryPartValueType.STRING,value);
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public static QueryPartValue newNull(){
		return new QueryPartValue(QueryPartValueType.NULL, null);
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public static QueryPartValue newBoolean(Boolean value){
		if(value == null) { return newNull(); }
		return new QueryPartValue(QueryPartValueType.BOOLEAN, value);
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public static QueryPartValue newJson(JsonElement value){
		if(value == null) { return newNull(); }
		return new QueryPartValue(QueryPartValueType.JSON, value);
	}
	
	/******************************************************************************************************
	 * Creates a new QueryPart based on the type of the JsonElement
	 ******************************************************************************************************/
	public static QueryPartValue newFromJsonElement(JsonElement value){
		
		if(value == null || value.isJsonNull()) {
			return newNull(); 
		}
		
		if(value.isJsonPrimitive()) {
			JsonPrimitive primitive = value.getAsJsonPrimitive();
			
			if(primitive.isBoolean()) {		return newBoolean(value.getAsBoolean()); }
			if(primitive.isNumber()) {		return newNumber(value.getAsBigDecimal()); }
			if(primitive.isString()) {		return newString(value.getAsString()); }
		}
		
		return newJson(value);	
		
	}
	/******************************************************************************************************
	 * Creates a new QueryPart based on the type of the JsonElement
	 ******************************************************************************************************/
	public void addToJsonObject(String memberName, EnhancedJsonObject object){
		 addToJsonObject(memberName, object.getWrappedObject());
	}

	/******************************************************************************************************
	 * Creates a new QueryPart based on the type of the JsonElement
	 ******************************************************************************************************/
	public void addToJsonObject(String propertyName, JsonObject object){
		
		if(object == null) {
			return;
		}
		
		switch(type) {
			case STRING: 	object.addProperty(propertyName, (String)value);
							break;
							
			case NUMBER:	object.addProperty(propertyName, (Number)value);
							break;
							
			case BOOLEAN:	object.addProperty(propertyName, (Boolean)value);
							break;
							
			case JSON:		object.add(propertyName, (JsonElement)value);
							break;
							
			case NULL:		object.add(propertyName, JsonNull.INSTANCE);
							break;

		default:
			break;
	
		}
		

		
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
	 * Check if the value is null
	 ******************************************************************************************************/
	public boolean isNullOrEmptyString() {
		return 
			value == null 
			|| (this.type == QueryPartValueType.STRING 
				&& Strings.isNullOrEmpty((String)value) 
			);
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
			
			return NumberUtils.isParsable(value);

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
	 * Check if the value is an Integer
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
	 * Check if the value is of type String
	 ******************************************************************************************************/
	public boolean isString() {
		return this.type == QueryPartValueType.STRING;
	}
	
	/******************************************************************************************************
	 * Check if the value is of type JSON
	 ******************************************************************************************************/
	public boolean isJson() {
		return this.type == QueryPartValueType.JSON;
	}
	
	/******************************************************************************************************
	 * Check if the value is a JSON Array
	 ******************************************************************************************************/
	public boolean isJsonArray() {
		return this.type == QueryPartValueType.JSON && ((JsonElement)this.value).isJsonArray();
	}
	
	/******************************************************************************************************
	 * Check if the value is a JSON Object
	 ******************************************************************************************************/
	public boolean isJsonObject() {
		return this.type == QueryPartValueType.JSON && ((JsonElement)this.value).isJsonObject();
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	@SuppressWarnings("unchecked")
	public Object getValue() {
		return value;
	}
	
	
	
	/******************************************************************************************************
	 * Returns this value as a number.
	 * Booleans will be returned as 0 for false and 1 for true.
	 * Strings will be parsed as double.
	 * for JsonElements getBigDecimal will be called.
	 * This function might throw runtime exceptions for parsing or if json element is not a number.
	 * It is highly recommended to always call one of the isNumber()-methods before calling this method.
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
	 * It is recommended to use isNumber()/isNumberString() first to make 
	 * sure number value is really a Number.
	 ******************************************************************************************************/
	public Float getAsFloat() {
			
		Number number = this.getAsNumber();
		
		if(number == null) return null;
		
		return number.floatValue();

	}
	
	/******************************************************************************************************
	 * It is recommended to use isNumber()/isNumberString() first to make 
	 * sure number value is really a Number.
	 ******************************************************************************************************/
	public Long getAsLong() {
			
		Number number = this.getAsNumber();
		
		if(number == null) return null;
		
		return number.longValue();

	}
	
	/******************************************************************************************************
	 * It is recommended to use isNumber()/isNumberString() first to make 
	 * sure number value is really a Number.
	 ******************************************************************************************************/
	public Double getAsDouble() {
			
		Number number = this.getAsNumber();
		
		if(number == null) return null;
		
		return number.doubleValue();

	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public BigDecimal getAsBigDecimal() {
		
		//--------------------------------
		// Most numbers are be stored as BigDecimal
		// to improve performance of this method
		if(value instanceof BigDecimal) {
			return (BigDecimal)value;
		}
		
		//--------------------------------
		// Handle Number Strings
		if(this.isNumberString()) {
			return new BigDecimal(this.getAsString());
		}
		
		//--------------------------------
		// Any Other Value
		Number number = this.getAsNumber();
		if(number == null) return null;
		
		// this check is needed for proper conversion to string values
		if(number instanceof BigDecimal){
			return (BigDecimal) number;
		}else if(  number instanceof Integer 
				|| number instanceof Long
				|| number instanceof Short
				) {
			new BigDecimal(number.longValue());
		}
		
		return new BigDecimal(number.doubleValue());

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
			
			case JSON:
				JsonElement element = (JsonElement)value;
				if(element.isJsonPrimitive()) {
					if(element.getAsJsonPrimitive().isBoolean()) {
						return element.getAsBoolean();
					}else {
						return false;
					}
				}else {
					return false;
				}
				
			default:		return false;

		}

	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public JsonElement getAsJsonElement() {
		
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
	 * Converts the value into a JsonObject if not already a json object.
	 * If value is converted, resulting object contains two fields: type and value.
	 * Use getAsJsonElement for not having conversion.
	 ******************************************************************************************************/
	public JsonObject getAsJsonObject() {
		JsonObject object = new JsonObject();
		switch(type) {
			case JSON:		JsonElement element = ((JsonElement)value);
							if(element.isJsonObject()) {
								object = element.getAsJsonObject();
							}else if(element.isJsonArray()) {
								
								object.addProperty("type", "array");
								object.add("value", element);
							}else {
								//this code should never be reached
								object.add("type", JsonNull.INSTANCE);
								object.add("value", element);
							}
							break;
			
			case NUMBER:	object.addProperty("type", "number");
							object.addProperty("value", (Number)value);
							break; 
							
			case BOOLEAN:	object.addProperty("type", "boolean");
							object.addProperty("value", (Boolean)value);
							break; 
							
			case STRING:	object.addProperty("type", "string");
							object.addProperty("value", (String)value);
							break; 
						
			case NULL:		object.addProperty("type", "null");
							object.add("value", JsonNull.INSTANCE);
							break;
							
			default:		object.addProperty("type", "null");
							object.add("value", JsonNull.INSTANCE);
							break;
		}
		
		return object;
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
	public ArrayList<String> getAsStringArray() {
		 return CFW.JSON.jsonToStringArrayList(
				 getAsJsonArray() 
			);
	}
	
	/******************************************************************************************************
	 * Creates a clone of this QueryPartValue.
	 ******************************************************************************************************/
	public QueryPartValue getAsClone() {
		
		switch(type) {
			case NULL:		return QueryPartValue.newNull();
			
			case JSON:		String jsonString = CFW.JSON.toJSON((JsonElement)value);
							JsonElement clone = CFW.JSON.fromJson(jsonString);
							return QueryPartValue.newJson(clone);
							
			
			case NUMBER:	double myDouble = ((Number)value).doubleValue();
							return QueryPartValue.newNumber(myDouble);

			case BOOLEAN: 	boolean boolClone = (Boolean)value;
							return QueryPartValue.newBoolean(boolClone);
			
			case STRING:	String stringClone = ((String)value)+"";
							return QueryPartValue.newString(stringClone);

			default:		return QueryPartValue.newNull();

		}

	}
	
	/******************************************************************************************************
	 * Returns a string representation of this value, except if the value is null.
	 ******************************************************************************************************/
	public String getAsString() {
		if(value == null) return null;
		
		// convert numbers to string without scientific notation
		if(type == QueryPartValueType.NUMBER) {
			return this.getAsBigDecimal().stripTrailingZeros().toPlainString();
		}
		
		return value.toString();
	}
	/******************************************************************************************************
	 * Checks if the value of this part is a fieldname of the object and returns the value of the field.
	 * If false returns this QueryPart unchanged.
	 * 
	 ******************************************************************************************************/
	public QueryPartValue convertFieldnameToFieldvalue(EnhancedJsonObject object) {
		
		if(value == null || object == null) return this;

		if(this.isString()){

			String potentialFieldname = this.getAsString();
			if(object.has(potentialFieldname)) {
				return QueryPartValue.newFromJsonElement(object.get(potentialFieldname));
			}
		}
		
		return this;
	}
	
	/***********************************************************************************************
	 * Checks if the JsonElement is equals to this value.
	 * Ignores type safety as follows:
	 * <ul>
	 * 		<li>If this is of type boolean, compare to strings while ignoring case. Compare to numbers while 0 is false and everything else is true.</li>
	 * 		<li>If this is string, compare to the string representations of booleans(case insensitive) and numbers </li>
	 * 		<li>if this is a number, compare to strings by converting the number to a string. Compare to booleans with 0 as false and true everything else. </li>
	 * 		<li>If this is null, compare to strings and only return true if string is exactly "null" </li>
	 * </ul>
	 ***********************************************************************************************/
	public boolean equalsElement(JsonElement element) {
		
		switch(this.type()) {
			case BOOLEAN:
				if(element.isJsonPrimitive()) {
					JsonPrimitive primitive = element.getAsJsonPrimitive();
					if(primitive.isBoolean()) {
						if(primitive.getAsBoolean() == this.getAsBoolean()) {
							return true;
						}
					}else if(primitive.isString() || primitive.isNumber()) {
						if(primitive.getAsString().equalsIgnoreCase(this.getAsString()) ) {
							return true;
						}
					}
				}
				break;
														
			case NUMBER:
				if(element.isJsonPrimitive()) {
					JsonPrimitive primitive = element.getAsJsonPrimitive();
					if(primitive.isNumber()) {
						if(primitive.getAsNumber().doubleValue() == this.getAsNumber().doubleValue()) {
							return true;
						}
					}else if(primitive.isString() || primitive.isBoolean()) {
						if(primitive.getAsString().equals(this.getAsString()) ) {
							return true;
						}
					}
				}
				break;
				
				
			case STRING:
				if(element.isJsonPrimitive()) {
					JsonPrimitive primitive = element.getAsJsonPrimitive();
					if(primitive.isString()) {
						if(primitive.getAsString().equals(this.getAsString()) ) {
							return true;
						}
					}else if(primitive.isBoolean()) {
						if(this.getAsString().equalsIgnoreCase(primitive.getAsBoolean()+"") ) {
							return true;
						}
					}else if(primitive.isNumber()) {
						if(this.getAsString().equals(primitive.getAsNumber()+"") ) {
							return true;
						}
					}
				}else if(element.isJsonNull()) {
					return this.getAsString().equals("null");
				}
				break;
			
				
			case NULL:
				if(element.isJsonNull()
				|| ( element.isJsonPrimitive() && element.getAsString().equals("null") )  
				) {
					return true;
				}
				break;
				
				
			case JSON: // Not supported
				break;
				
			default: 
				return false;
		
		}
		
		return false;
	}
	
	/***********************************************************************************************
	 * Checks if the this value is contained in the given JsonElement.
	 * Ignores type safety as all values are converted and evaluated as strings.
	 ***********************************************************************************************/
	public boolean containedInElement(JsonElement element) {
		
		if(element.isJsonNull()) {
			if(this.isNull()) return true;
			
			return "null".contains(this.getAsString());
		}
		
		switch(this.type()) {
			case NUMBER:	
			case STRING:	
				if(element.isJsonPrimitive()) {
					
					String primitiveString = element.getAsString();
					String thisString = this.getAsString();
					
					// compare booleans case insensitive
					if(element.getAsJsonPrimitive().isBoolean()) {
						thisString = thisString.toLowerCase();
					}
					
					return primitiveString.contains(thisString);
				}
				break;
			
			case BOOLEAN:
				if(element.isJsonPrimitive()) {
					String primitiveString = element.getAsString();
					return primitiveString.toLowerCase().contains(this.getAsString().toLowerCase());
				}
				break;
				
			case NULL:
				if(element.isJsonPrimitive()) {
					String primitiveString = element.getAsString();
					return primitiveString.contains("null");
				}				 
				
			case JSON: // Not supported
				break;
				
			default: 
				return false;
		
		}
		
		return false;
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	@Override
	public JsonObject createDebugObject(EnhancedJsonObject object) {
		
		JsonObject debugObject = new JsonObject();

		debugObject.addProperty("partType", "Value");
		
		debugObject.add("value", this.getAsJsonElement());

		return debugObject;
	}

}
