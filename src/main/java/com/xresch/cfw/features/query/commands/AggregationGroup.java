package com.xresch.cfw.features.query.commands;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.google.gson.JsonObject;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.parse.QueryPartFunction;
import com.xresch.cfw.features.query.parse.QueryPartValue;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class AggregationGroup {
	
	private JsonObject initialResultObject;
	private JsonObject groupValues;
	private ArrayList<String> targetFieldnames = new ArrayList<>();
	private LinkedHashMap<String, QueryPartFunction> functionMap = new LinkedHashMap<>();
	
	public AggregationGroup(JsonObject initialResultObject, JsonObject groupValues) {
		this.initialResultObject = initialResultObject;
		this.groupValues = groupValues;
	}
	
	public JsonObject getGroupValues() {
		return this.groupValues;
	}
	public void addFunctions(LinkedHashMap<String, QueryPartFunction> functions) {
		
		for(Entry<String, QueryPartFunction> entry : functions.entrySet()) {
			this.addFunction(entry.getKey(), entry.getValue());
		}
		
	}

	public void addFunction(String targetFieldname, QueryPartFunction functionPart) {
		
		targetFieldnames.add(targetFieldname);
		String instanceID = functionPart.createManagedInstance();
		functionMap.put(instanceID, functionPart);
	}
	
	public void doAggregation(EnhancedJsonObject object) {
		
		for(Entry<String, QueryPartFunction> entry : functionMap.entrySet()) {
			entry.getValue().aggregateFunctionInstance(entry.getKey(), object);
		}
	}
	
	public EnhancedJsonObject toRecord() {
		
		int index = 0;
		for(Entry<String, QueryPartFunction> entry : functionMap.entrySet()) {
			String propertyName = targetFieldnames.get(index);

			String instanceID = entry.getKey();
			QueryPartFunction functionPart = entry.getValue();
			QueryPartValue aggregationValue = functionPart.executeFunctionInstance(instanceID, null);
			
			aggregationValue.addToJsonObject(propertyName, initialResultObject);
			index++;
		}
		
		return new EnhancedJsonObject(initialResultObject);
	}
	
}