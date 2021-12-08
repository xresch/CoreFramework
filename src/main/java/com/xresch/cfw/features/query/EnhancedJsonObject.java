package com.xresch.cfw.features.query;

import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**************************************************************************************************************
 * Wrapper class to enhance GSON JsonObject with additional methods.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021
 **************************************************************************************************************/
public class EnhancedJsonObject {

	private JsonObject internal;
	
	public EnhancedJsonObject() {
		this.internal = new JsonObject();
	}
	
	public EnhancedJsonObject(JsonObject object) {
		this.internal = object;
	}
	
	/*************************************************************************************************
	 * Creates a deep copy of this element and all its children
	 *************************************************************************************************/
	public JsonObject deepCopy() {
		return this.internal.deepCopy();
	}
	
	/*************************************************************************************************
	 * Adds a member, which is a name-value pair, to self. The name must be a String, but the value
	 * can be an arbitrary JsonElement, thereby allowing you to build a full tree of JsonElements
	 * rooted at this node.
	 *
	 * @param property name of the member.
	 * @param value the member object.
	 *************************************************************************************************/
	public void add(String property, JsonElement value) {
		this.internal.add(property, value);
	}
	
	/*************************************************************************************************
	 * Removes the {@code property} from this {@link JsonObject}.
	 *
	 * @param property name of the member that should be removed.
	 * @return the {@link JsonElement} object that is being removed.
	 * @since 1.3
	 *************************************************************************************************/
	public JsonElement remove(String property) {
	  return this.internal.remove(property);
	}
	
	/*************************************************************************************************
	 * Convenience method to add a primitive member. The specified value is converted to a
	 * JsonPrimitive of String.
	 *
	 * @param property name of the member.
	 * @param value the string value associated with the member.
	 *************************************************************************************************/
	public void addProperty(String property, String value) {
		this.internal.addProperty(property, value);
	}
	
	/*************************************************************************************************
	 * Convenience method to add a primitive member. The specified value is converted to a
	 * JsonPrimitive of Number.
	 *
	 * @param property name of the member.
	 * @param value the number value associated with the member.
	 *************************************************************************************************/
	public void addProperty(String property, Number value) {
		this.internal.addProperty(property, value);
	}
	
	/*************************************************************************************************
	 * Convenience method to add a boolean member. The specified value is converted to a
	 * JsonPrimitive of Boolean.
	 *
	 * @param property name of the member.
	 * @param value the number value associated with the member.
	 *************************************************************************************************/
	public void addProperty(String property, Boolean value) {
		this.internal.addProperty(property, value);
	}
	
	/*************************************************************************************************
	 * Convenience method to add a char member. The specified value is converted to a
	 * JsonPrimitive of Character.
	 *
	 * @param property name of the member.
	 * @param value the number value associated with the member.
	 *************************************************************************************************/
	public void addProperty(String property, Character value) {
		this.internal.addProperty(property, value);
	}
	
	/*************************************************************************************************
	 * Returns a set of members of this object. The set is ordered, and the order is in which the
	 * elements were added.
	 *
	 * @return a set of members of this object.
	 *************************************************************************************************/
	public Set<Map.Entry<String, JsonElement>> entrySet() {
	  return this.internal.entrySet();
	}
	
	/*************************************************************************************************
	 * Returns a set of members key values.
	 *
	 * @return a set of member keys as Strings
	 * @since 2.8.1
	 *************************************************************************************************/
	public Set<String> keySet() {
	  return this.internal.keySet();
	}
	
	/*************************************************************************************************
	 * Returns the number of key/value pairs in the object.
	 *
	 * @return the number of key/value pairs in the object.
	 *************************************************************************************************/
	public int size() {
	  return this.internal.size();
	}
	
	/*************************************************************************************************
	 * Convenience method to check if a member with the specified name is present in this object.
	 *
	 * @param memberName name of the member that is being checked for presence.
	 * @return true if there is a member with the specified name, false otherwise.
	 *************************************************************************************************/
	public boolean has(String memberName) {
	  return this.internal.has(memberName);
	}
	
	/*************************************************************************************************
	 * Returns the member with the specified name.
	 *
	 * @param memberName name of the member that is being requested.
	 * @return the member matching the name. Null if no such member exists.
	 *************************************************************************************************/
	public JsonElement get(String memberName) {
	  return this.internal.get(memberName);
	}
	
	/*************************************************************************************************
	 * Convenience method to get the specified member as a JsonPrimitive element.
	 *
	 * @param memberName name of the member being requested.
	 * @return the JsonPrimitive corresponding to the specified member.
	 *************************************************************************************************/
	public JsonPrimitive getAsJsonPrimitive(String memberName) {
	  return this.internal.getAsJsonPrimitive(memberName);
	}
	
	/*************************************************************************************************
	 * Convenience method to get the specified member as a JsonArray.
	 *
	 * @param memberName name of the member being requested.
	 * @return the JsonArray corresponding to the specified member.
	 *************************************************************************************************/
	public JsonArray getAsJsonArray(String memberName) {
	  return this.internal.getAsJsonArray(memberName);
	}
	
	/*************************************************************************************************
	 * Convenience method to get the specified member as a JsonObject.
	 *
	 * @param memberName name of the member being requested.
	 * @return the JsonObject corresponding to the specified member.
	 *************************************************************************************************/
	public JsonObject getAsJsonObject(String memberName) {
		return this.internal.getAsJsonObject(memberName);
	}
	
	/*************************************************************************************************
	 * 
	 *************************************************************************************************/
	@Override
	public boolean equals(Object o) {
	  return this.internal.equals(o);
	}
	
	/*************************************************************************************************
	 * 
	 *************************************************************************************************/
	@Override
	public int hashCode() {
	  return this.internal.hashCode();
	}
}
