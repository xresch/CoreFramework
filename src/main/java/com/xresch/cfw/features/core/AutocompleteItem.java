package com.xresch.cfw.features.core;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;

public class AutocompleteItem {

	private Object value;
	private Object label;
	private Object description;
	
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
	
	/*************************************************************************
	 * Convert to JSON
	 *************************************************************************/
	public JsonObject toJson() {
		JsonObject jsonItem = new JsonObject();
		
		CFW.JSON.addObject(jsonItem, "value", value);
		CFW.JSON.addObject(jsonItem, "label", label);
		CFW.JSON.addObject(jsonItem, "description", description);
		
		return jsonItem;
	}

}
