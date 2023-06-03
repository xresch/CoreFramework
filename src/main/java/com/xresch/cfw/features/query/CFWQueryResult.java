package com.xresch.cfw.features.query;

import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class CFWQueryResult {
	

	
	public static final String RESULTFIELDS_RESULTS 			= "results";
	public static final String RESULTFIELDS_DISPLAY_SETTINGS 	= "displaySettings";
	public static final String RESULTFIELDS_METADATA 			= "metadata";
	public static final String RESULTFIELDS_DETECTED_FIELDS 	= "detectedFields";
	public static final String RESULTFIELDS_GLOBALS 			= "globals";
	public static final String RESULTFIELDS_EXEC_TIME_MILLIS 	= "execTimeMillis";
	public static final String RESULTFIELDS_RESULT_COUNT 		= "resultCount";
	
	
	private JsonObject object = new JsonObject();
	
	
	/****************************************************
	 * 
	 ****************************************************/
	public CFWQueryResult() {
		
		//------------------------
		// Set Default Values
		this.setExecTimeMillis(-1);
		this.setGlobals(new JsonObject());
		this.setMetadata(new JsonObject());
		this.setDisplaySettings(new JsonObject());
		this.setDetectedFields(new JsonArray());
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
	 * 
	 ****************************************************/
	public JsonObject getMetadata() {
		return object.get(RESULTFIELDS_METADATA).getAsJsonObject();
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


}
