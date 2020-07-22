package com.xresch.cfw.features.core;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;

public class AutocompleteList {
	
	// value / label pairs
	private ArrayList<AutocompleteItem> items = new  ArrayList<AutocompleteItem>();

	
	
	/*************************************************************************
	 * Add an item to the autocomplete results. The value will also be used 
	 * as the label.
	 * @param value
	 * @return
	 *************************************************************************/
	public AutocompleteList addItem(Object value) {
		items.add(new AutocompleteItem(value,value));
		return this;
	}
	
	/*************************************************************************
	 * Add an item to the autocomplete results.
	 * @param value
	 * @param label
	 * @return
	 *************************************************************************/
	public AutocompleteList addItem(Object value, Object label) {
		items.add(new AutocompleteItem(value, label));
		return this;
	}
	
	/*************************************************************************
	 * Add an item to the autocomplete result.
	 * @param value
	 * @param label
	 * @param description
	 * @return
	 *************************************************************************/
	public AutocompleteList addItem(Object value, Object label, Object description) {
		items.add(new AutocompleteItem(value, label, description));
		return this;
	}
	
	/*************************************************************************
	 * Add an item to the autocomplete result.
	 * @param value
	 * @param label
	 * @param description
	 * @return
	 *************************************************************************/
	public AutocompleteList addItem(AutocompleteItem item) {
		items.add(item);
		return this;
	}
	
	
	/*************************************************************************
	 * Convert to JSON
	 *************************************************************************/
	public JsonArray toJson() {
		JsonArray array = new JsonArray();
		
		for(AutocompleteItem item : items) {
			array.add(item.toJson());
		}
		
		return array;
	}
	
	public  ArrayList<AutocompleteItem> getItems(){
		return items;
	}
	
	public  int size(){
		return items.size();
	}
	
}
