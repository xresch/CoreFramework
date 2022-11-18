package com.xresch.cfw.features.query;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartValue;

/**************************************************************************************************************
 * Wrapper class to enhance GSON JsonObject with additional methods.
 * also uses SoftReference to avoid Memory Leaks caused by too big queries.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021
 **************************************************************************************************************/
public class EnhancedJsonObject {

	private SoftReference<JsonObject> internal;
	
	public EnhancedJsonObject() {
		this.internal = new SoftReference<>(new JsonObject());
	}
	
	public EnhancedJsonObject(JsonObject object) {
		this.internal = new SoftReference<>(object);
	}
	
	/*************************************************************************************************
	 * Returns the wrapped GSON JsonObject.
	 * @throws CFWQueryMemoryException 
	 * 
	 *************************************************************************************************/
	public JsonObject getWrappedObject() {
		JsonObject object = internal.get();
		if(object == null) { throw new CFWQueryMemoryException(); } 
		return object;
	}
	
	/*************************************************************************************************
	 * Change the object wrapped by this Enhanced object.
	 * Will do nothing if input is null
	 * 
	 *************************************************************************************************/
	public void setWrappedObject(JsonObject newObject) {
		if(newObject == null) { return; } 
		internal = new SoftReference<>(newObject);
	}
	
	
	/*************************************************************************************************
	 * 
	 *************************************************************************************************/
	public void removeMultiple(String... fieldnames) {
		JsonObject object = getWrappedObject();
		
		for(String fieldname : fieldnames) {
			object.remove(fieldname);
		}
	}
	
	/*************************************************************************************************
	 * Convenience method to add all fields of another object to this object.
	 * will override existing values with the same fieldname.
	 *
	 *************************************************************************************************/
	public void addAll(EnhancedJsonObject object) {
		this.addAll(object.getWrappedObject());
	}
	
	/*************************************************************************************************
	 * Convenience method to add all fields of another object to this object.
	 * will override existing values with the same fieldname.
	 *
	 *************************************************************************************************/
	public void addAll(JsonObject object) {
		for(Entry<String, JsonElement> entry : object.entrySet()) {
			this.add(entry.getKey(), entry.getValue());
		}
	}
	/*************************************************************************************************
	 * Convenience method to add a value of a QueryPart
	 *
	 * @param property name of the member.
	 * @param value the string value associated with the member.
	 *************************************************************************************************/
	public void addProperty(String property, QueryPart part) {
		this.addProperty(property, part.determineValue(null));
	}
	
	/*************************************************************************************************
	 * Convenience method to add a value of a QueryPart
	 *
	 * @param property name of the member.
	 * @param value the string value associated with the member.
	 *************************************************************************************************/
	public void addProperty(String property, QueryPartValue value) {
		value.addToJsonObject(property, this.getWrappedObject());
	}
	
	
	/*************************************************************************************************
	 * Returns the value of the specified member converted to a string.
	 *
	 * @param memberName name of the member that is being requested.
	 * @return a string or empty string, never null
	 *************************************************************************************************/
	public String convertToString(String memberName) {
		
		JsonElement member = getWrappedObject().get(memberName);
		
		if(member == null || member.isJsonNull()) return "";
		
		if(member.isJsonPrimitive()) {
			JsonPrimitive primitive = member.getAsJsonPrimitive();
			
			if(primitive.isString()) 	return primitive.getAsString();
			if(primitive.isBoolean()) 	return ""+primitive.getAsBoolean();
			if(primitive.isNumber()) 	return ""+primitive.getAsBigDecimal();
			
		}

		return member.toString();
	}
	
	
	
	//#####################################################################################################################
	//#####################################################################################################################
	// WRAPPED METHODS
	//#####################################################################################################################
	//#####################################################################################################################
	
	/*************************************************************************************************
	 * Creates a deep copy of this element and all its children
	 *************************************************************************************************/
	public JsonObject deepCopy() {
		return getWrappedObject().deepCopy();
	}
	
	/*************************************************************************************************
	 * Adds a member, which is a name-value pair, to self. The name must be a String, but the value
	 * can be an arbitrary JsonElement, thereby allowing you to build a full tree of JsonElements
	 * rooted at this node.
	 *
	 * @param property name of the member.
	 * @param value the member object.
	 *************************************************************************************************/
	public void add(String property, JsonElement value){
		getWrappedObject().add(property, value);
	}
	
	/*************************************************************************************************
	 * Removes the {@code property} from this {@link JsonObject}.
	 *
	 * @param property name of the member that should be removed.
	 * @return the {@link JsonElement} object that is being removed.
	 * @since 1.3
	 *************************************************************************************************/
	public JsonElement remove(String property) {
	  return getWrappedObject().remove(property);
	}
	
	/*************************************************************************************************
	 * Convenience method to add a primitive member. The specified value is converted to a
	 * JsonPrimitive of String.
	 *
	 * @param property name of the member.
	 * @param value the string value associated with the member.
	 *************************************************************************************************/
	public void addProperty(String property, String value) {
		getWrappedObject().addProperty(property, value);
	}
	
	/*************************************************************************************************
	 * Convenience method to add a primitive member. The specified value is converted to a
	 * JsonPrimitive of Number.
	 *
	 * @param property name of the member.
	 * @param value the number value associated with the member.
	 *************************************************************************************************/
	public void addProperty(String property, Number value) {
		getWrappedObject().addProperty(property, value);
	}
	
	/*************************************************************************************************
	 * Convenience method to add a boolean member. The specified value is converted to a
	 * JsonPrimitive of Boolean.
	 *
	 * @param property name of the member.
	 * @param value the number value associated with the member.
	 *************************************************************************************************/
	public void addProperty(String property, Boolean value) {
		getWrappedObject().addProperty(property, value);
	}
	
	/*************************************************************************************************
	 * Convenience method to add a char member. The specified value is converted to a
	 * JsonPrimitive of Character.
	 *
	 * @param property name of the member.
	 * @param value the number value associated with the member.
	 *************************************************************************************************/
	public void addProperty(String property, Character value) {
		getWrappedObject().addProperty(property, value);
	}
	
	/*************************************************************************************************
	 * Returns a set of members of this object. The set is ordered, and the order is in which the
	 * elements were added.
	 *
	 * @return a set of members of this object.
	 *************************************************************************************************/
	public Set<Map.Entry<String, JsonElement>> entrySet() {
	  return getWrappedObject().entrySet();
	}
	
	/*************************************************************************************************
	 * Returns a set of members key values.
	 *
	 * @return a set of member keys as Strings
	 * @since 2.8.1
	 *************************************************************************************************/
	public Set<String> keySet() {
	  return getWrappedObject().keySet();
	}
	
	/*************************************************************************************************
	 * Returns the number of key/value pairs in the object.
	 *
	 * @return the number of key/value pairs in the object.
	 *************************************************************************************************/
	public int size() {
	  return getWrappedObject().size();
	}
	
	/*************************************************************************************************
	 * Convenience method to check if a member with the specified name is present in this object.
	 *
	 * @param memberName name of the member that is being checked for presence.
	 * @return true if there is a member with the specified name, false otherwise.
	 *************************************************************************************************/
	public boolean has(String memberName) {
	  return getWrappedObject().has(memberName);
	}
	
	/*************************************************************************************************
	 * Returns the member with the specified name.
	 *
	 * @param memberName name of the member that is being requested.
	 * @return the member matching the name. Null if no such member exists.
	 *************************************************************************************************/
	public JsonElement get(String memberName) {
	  return getWrappedObject().get(memberName);
	}
	
	/*************************************************************************************************
	 * Convenience method to get the specified member as a JsonPrimitive element.
	 *
	 * @param memberName name of the member being requested.
	 * @return the JsonPrimitive corresponding to the specified member.
	 *************************************************************************************************/
	public JsonPrimitive getAsJsonPrimitive(String memberName) {
	  return getWrappedObject().getAsJsonPrimitive(memberName);
	}
	
	/*************************************************************************************************
	 * Convenience method to get the specified member as a JsonArray.
	 *
	 * @param memberName name of the member being requested.
	 * @return the JsonArray corresponding to the specified member.
	 *************************************************************************************************/
	public JsonArray getAsJsonArray(String memberName) {
	  return getWrappedObject().getAsJsonArray(memberName);
	}
	
	/*************************************************************************************************
	 * Convenience method to get the specified member as a JsonObject.
	 *
	 * @param memberName name of the member being requested.
	 * @return the JsonObject corresponding to the specified member.
	 *************************************************************************************************/
	public JsonObject getAsJsonObject(String memberName) {
		return getWrappedObject().getAsJsonObject(memberName);
	}
	

	/*************************************************************************************************
	 * 
	 *************************************************************************************************/
	@Override
	public boolean equals(Object o) {
	  return getWrappedObject().equals(o);
	}
	
	/*************************************************************************************************
	 * 
	 *************************************************************************************************/
	@Override
	public int hashCode() {
	  return getWrappedObject().hashCode();
	}
}
