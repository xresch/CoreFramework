package com.xresch.cfw.features.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;

public class AutocompleteResult {
	
	ArrayList<AutocompleteList> listArray = new ArrayList<AutocompleteList>();
	
	private String htmlDescription = null;
	
	public AutocompleteResult() {
		
	}
	
	public AutocompleteResult(AutocompleteList list) {
		listArray.add(list);
	}
	
	public AutocompleteResult(LinkedHashMap<Object, Object> map) {
		this.addList(map);
	}
	
	
	public AutocompleteResult addList(AutocompleteList list) {
		listArray.add(list);
		return this;
	}
	
	public AutocompleteResult addList(LinkedHashMap<Object, Object> map) {
		
		AutocompleteList list = new AutocompleteList();
		for(Entry<Object, Object> entry : map.entrySet()) {
			list.addItem(entry.getKey(), entry.getValue());
		}
		
		listArray.add(list);
		return this;
	}
	
	public ArrayList<AutocompleteList> getLists() {
		return listArray;
	}
	
	public String getHTMLDescription() {
		return htmlDescription;
	}
	
	public AutocompleteResult setHTMLDescription(String htmlDescription) {
		this.htmlDescription = htmlDescription;
		return this;
	}
	
	/*************************************************************************
	 * Convert to JSON
	 *************************************************************************/
	public JsonObject toJson() {
		JsonArray array = new JsonArray();
		
		for(AutocompleteList list : listArray) {
			array.add(list.toJson());
		}
		
		JsonObject resultObject = new JsonObject();
		
		CFW.JSON.addObject(resultObject, "lists", array);
		CFW.JSON.addObject(resultObject, "description", htmlDescription);
		
		return resultObject;
	}
	
}
