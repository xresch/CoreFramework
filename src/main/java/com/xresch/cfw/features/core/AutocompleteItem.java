package com.xresch.cfw.features.core;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;

public class AutocompleteItem {

	private Object value;
	private Object label;
	private Object description;
	
	//Methods: 
	//exchange - fully replaces the current value of the input element (default)
	//replace - replaces the last occurence of the search string with the value of the item
	private String method = "exchange";
	
	public  AutocompleteItem() {}
	
	public  AutocompleteItem(Object value) {
		this(value, value);
	}
	
	public  AutocompleteItem(Object value, Object label) {
		this.value = value;
		this.label = label;
	}
	
	public AutocompleteItem(Object value, Object label, Object description) {
		this.value = value;
		this.label = label;
		this.description = description;
	}
	
	public Object value() {
		return value;
	}
	
	public AutocompleteItem value(Object value) {
		this.value = value;
		return this;
	}
		
	public Object label() {
		return label;
	}
	
	public AutocompleteItem label(Object key) {
		this.label = key;
		return this;
	}
	
	public Object description() {
		return description;
	}
	
	public AutocompleteItem description(Object description) {
		this.description = description;
		return this;
	}
	
	public AutocompleteItem setMethodReplace(String replaceThis) {
		this.method = "replace:"+replaceThis;
		return this;
	}
	public AutocompleteItem setMethodAppend() {
		this.method = "append";
		return this;
	}
	/*************************************************************************
	 * Convert to JSON
	 *************************************************************************/
	public JsonObject toJson() {
		JsonObject jsonItem = new JsonObject();
		
		CFW.JSON.addObject(jsonItem, "value", value);
		CFW.JSON.addObject(jsonItem, "label", label);
		CFW.JSON.addObject(jsonItem, "description", description);
		CFW.JSON.addObject(jsonItem, "method", method);
		return jsonItem;
	}

}
