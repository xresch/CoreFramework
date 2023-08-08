package com.xresch.cfw.features.query;

import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class CFWQueryResult {
	
	public static final String RESULTFIELDS_RESULTS 			= "results";
	public static final String RESULTFIELDS_DISPLAY_SETTINGS 	= "displaySettings";
	public static final String RESULTFIELDS_METADATA 			= "metadata";
	public static final String RESULTFIELDS_DETECTED_FIELDS 	= "detectedFields";
	public static final String RESULTFIELDS_GLOBALS 			= "globals";
	public static final String RESULTFIELDS_EXEC_TIME_MILLIS 	= "execTimeMillis";
	public static final String RESULTFIELDS_RESULT_COUNT 		= "resultCount";
	
	private  CFWQueryContext context;
	private JsonObject object = new JsonObject();
	
	/****************************************************
	 * 
	 ****************************************************/
	public CFWQueryResult(CFWQueryContext context) {
		
		//------------------------
		// Set Values from Context
		this.context = context;
		this.setGlobals(context.getGlobals());
		this.setMetadata(context.getMetadata());
		this.setDisplaySettings(context.getDisplaySettings());
		this.setDetectedFields(context.getFieldnamesAsJsonArray());
		
		//------------------------
		// Set Default Values
		this.setExecTimeMillis(-1);
		this.setResults(new JsonArray());
		
	}
	
	/****************************************************
	 * 
	 ****************************************************/
	public JsonObject toJson() {
		this.updateResultCount();
		return object;
	}
	
	/****************************************************
	 * 
	 ****************************************************/
	private CFWQueryResult updateResultCount() {
		object.addProperty(RESULTFIELDS_RESULT_COUNT, this.getResults().size());
		return this;
	}
	
	/****************************************************
	 * 
	 ****************************************************/
	public int getResultCount() {
		return this.getResults().size();
	}
	
	/****************************************************
	 * 
	 ****************************************************/
	public CFWQueryContext getQueryContext() {
		return context;
	}
	
	/****************************************************
	 * 
	 ****************************************************/
	public CFWQueryResult setExecTimeMillis(long value) {
		object.addProperty(RESULTFIELDS_EXEC_TIME_MILLIS, value);
		return this;
	}
	
	/****************************************************
	 * 
	 ****************************************************/
	public long getExecTimeMillis() {
		return object.get(RESULTFIELDS_EXEC_TIME_MILLIS).getAsLong();
	}
	
	
	/****************************************************
	 * 
	 ****************************************************/
	public CFWQueryResult setGlobals(JsonObject value) {
		object.add(RESULTFIELDS_GLOBALS, value);
		return this;
	}
	
	/****************************************************
	 * 
	 ****************************************************/
	public JsonObject getGlobals() {
		return object.get(RESULTFIELDS_GLOBALS).getAsJsonObject();
	}
	
	/****************************************************
	 * 
	 ****************************************************/
	public CFWQueryResult setMetadata(JsonObject value) {
		object.add(RESULTFIELDS_METADATA, value);
		return this;
	}
	
	/****************************************************
	 * Returns the metadata object
	 ****************************************************/
	public JsonObject getMetadata() {
		return object.get(RESULTFIELDS_METADATA).getAsJsonObject();
	}
	
	/****************************************************
	 * Returns the metadata property by the given name.
	 ****************************************************/
	public JsonElement getMetadata(String memberName) {
		return object.get(RESULTFIELDS_METADATA).getAsJsonObject().get(memberName);
	}
	
	/****************************************************
	 * Returns the metadata property by the given name.
	 ****************************************************/
	public boolean isMetadataValueTrue(String memberName) {
		JsonObject metadata = object.get(RESULTFIELDS_METADATA).getAsJsonObject();
		JsonPrimitive member = metadata.getAsJsonPrimitive(memberName);
		if(member != null
		&& member.isBoolean()
		&& member.getAsBoolean()) {
			return true;
		}
		
		return false;
	}
	
	/****************************************************
	 * 
	 ****************************************************/
	public CFWQueryResult setDisplaySettings(JsonObject value) {
		object.add(RESULTFIELDS_DISPLAY_SETTINGS, value);
		return this;
	}
	
	/****************************************************
	 * 
	 ****************************************************/
	public JsonObject getDisplaySettings() {
		return object.get(RESULTFIELDS_DISPLAY_SETTINGS).getAsJsonObject();
	}
	
	/****************************************************
	 * 
	 ****************************************************/
	public CFWQueryResult setDetectedFields(JsonArray value) {
		object.add(RESULTFIELDS_DETECTED_FIELDS, value);
		return this;
	}
	
	/****************************************************
	 * 
	 ****************************************************/
	public CFWQueryResult setDetectedFields(Set<String> set) {
		JsonArray detectedFieldsArray = new JsonArray();
		for(String entry : set) {
			detectedFieldsArray.add(entry);
		}
		object.add(RESULTFIELDS_DETECTED_FIELDS, detectedFieldsArray);
		return this;
	}
	
	/****************************************************
	 * 
	 ****************************************************/
	public JsonArray getDetectedFields() {
		return object.get(RESULTFIELDS_DETECTED_FIELDS).getAsJsonArray();
	}
	
	/****************************************************
	 * 
	 ****************************************************/
	public CFWQueryResult setResults(JsonArray value) {
		if(value == null) {
			return this;
		}
		
		object.add(RESULTFIELDS_RESULTS, value);
		this.updateResultCount();
		return this;
	}
	
	/****************************************************
	 * 
	 ****************************************************/
	public JsonArray getResults() {
		return object.get(RESULTFIELDS_RESULTS).getAsJsonArray();
	}
	/****************************************************
	 * 
	 ****************************************************/
	public JsonObject getResult(int index) {
		return object.get(RESULTFIELDS_RESULTS).getAsJsonArray().get(index).getAsJsonObject();
	}


}
