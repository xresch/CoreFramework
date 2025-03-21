package com.xresch.cfw.features.query;

import java.util.LinkedHashSet;
import java.util.Properties;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.features.query.parse.QueryPartValue;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQueryContext{
	// used for history, fully query string, all queries
	private String fullQueryString = "";
	
	// used for mimic command, part of a full query separated with ;
	private String originalQueryString = "";
	
	private long earliest = 0;
	private long latest = 0;
	private int timezoneOffsetMinutes = 0;
	private boolean checkPermissions = true;
	private CFWQueryResultList resultArray;
	
	// set with query command 'metadata', get with the query function 'metadata()'
	private JsonObject metadata = new JsonObject();
	// set with query command 'paramdefaults', get with the query function 'param()'
	private JsonObject parameters = new JsonObject();
	// set with query command 'globals', get with the query function 'globals()'
	private JsonObject globals = new JsonObject();
	// set with the query command 'display'
	private JsonObject displaySettings = new JsonObject();
	// set with the query command 'formatfield'
	private JsonObject fieldFormats = new JsonObject();
	
	// used internally by query sources, commands and functions
	private Properties metaObjects = new Properties();
	
	protected CFWQueryFieldnameManager contextFieldnameManager = new CFWQueryFieldnameManager();
	
	/***********************************************************************************************
	 * Constructs a Context with a new CFWQueryResultList.
	 * This is mainly used for testing.
	 ***********************************************************************************************/
	public CFWQueryContext() {
		this(new CFWQueryResultList());
	}
	
	/***********************************************************************************************
	 * Constructs a Context with a shared CFWQueryResultList.
	 * This is needed for commands which operate on existing results.
	 ***********************************************************************************************/
	public CFWQueryContext(CFWQueryResultList sharedResults) {
		resultArray = sharedResults;
		displaySettings.add("fieldFormats", fieldFormats);
	}
	
	/***********************************************************************************************
	 * Clones the context to make a new base context that can be shared between multiple queries.
	 * (queries split up by ';')
	 * @param isSharedContext if set to false gets it's own result list. (used for subquery function)
	 ***********************************************************************************************/
	public CFWQueryContext createClone(boolean isSharedContext) {
		
		//-----------------------------
		// ResultList to user
		CFWQueryResultList resultList = this.resultArray;
		if(!isSharedContext) {
			resultList = new CFWQueryResultList();
		}
		
		//-----------------------------
		// Clone
		CFWQueryContext clonedContext = new CFWQueryContext(resultList);
		clonedContext.setFullQueryString(this.getFullQueryString());
		clonedContext.checkPermissions(this.checkPermissions());
		clonedContext.setEarliest(this.getEarliestMillis());
		clonedContext.setLatest(this.getLatestMillis());
		clonedContext.setTimezoneOffsetMinutes(this.getTimezoneOffsetMinutes());
		clonedContext.setParameters(this.getParameters());
		clonedContext.setGlobals(this.getGlobals());
		//clonedContext.setFieldnames(contextFieldnameManager);
		
		return clonedContext;
	}
	
	/***********************************************************************************************
	 * Get the earliest time for this query.
	 ***********************************************************************************************/
	public long getEarliestMillis() {
		return earliest;
	}
	
	/***********************************************************************************************
	 * Set the earliest time for this query.
	 ***********************************************************************************************/
	public CFWQueryContext setEarliest(long earliest) {
		this.earliest = earliest;
		return this;
	}
	
	/***********************************************************************************************
	 * Get the full query string containing all query parts.
	 ***********************************************************************************************/
	public String getFullQueryString() {
		return fullQueryString;
	}
	
	/***********************************************************************************************
	 * Set the original query string if not already set.
	 ***********************************************************************************************/
	public CFWQueryContext setFullQueryString(String fullQueryString) {
		if(Strings.isNullOrEmpty(this.fullQueryString)) {
			this.fullQueryString = fullQueryString;
		}
		return this;
	}
	
	/***********************************************************************************************
	 * Get the query part(separated with ';') of the full query string that caused the current query.
	 ***********************************************************************************************/
	public String getOriginalQueryString() {
		return originalQueryString;
	}
	
	/***********************************************************************************************
	 * Set the original query string if not already set.
	 ***********************************************************************************************/
	public CFWQueryContext setOriginalQueryString(String originalQueryString) {
		
		// !!!!!!!!!!!! IMPORTANT !!!!!!!!!!!!!!!
		// Only set the original query string if not already set.
		// If query string is overridden, CFWQueryCommandMimic will not work when calling itself.
		if(Strings.isNullOrEmpty(this.originalQueryString)) {
			this.originalQueryString = originalQueryString;
		}
		return this;
	}

	/***********************************************************************************************
	 * Get the latest time for this query.
	 ***********************************************************************************************/
	public long getLatestMillis() {
		return latest;
	}

	/***********************************************************************************************
	 * Set the latest time for this query.
	 ***********************************************************************************************/
	public CFWQueryContext setLatest(long latest) {
		this.latest = latest;
		return this;
	}
	/***********************************************************************************************
	 * Get the timezoneoffsetMinutes time for this query.
	 ***********************************************************************************************/
	public int getTimezoneOffsetMinutes() {
		return timezoneOffsetMinutes;
	}
	
	/***********************************************************************************************
	 * Set the timezoneoffsetMinutes time for this query.
	 ***********************************************************************************************/
	public CFWQueryContext  setTimezoneOffsetMinutes(int timezoneOffsetMinutes) {
		this.timezoneOffsetMinutes = timezoneOffsetMinutes;
		return this;
	}
	
	/****************************************************************
	 * Enable or disable if the users permissions should be
	 * verified or not.
	 ****************************************************************/
	public CFWQueryContext checkPermissions(boolean checkPermissions) {
		this.checkPermissions = checkPermissions;
		return this;
	}
	
	/****************************************************************
	 * Return if the permissions should be checked.
	 ****************************************************************/
	public boolean checkPermissions() {
		return checkPermissions;
	}
	
	/***********************************************************************************************
	 * Returns the array containing all the results of already completed queries(splitted by ';').
	 ***********************************************************************************************/
	public CFWQueryResultList getResultList() {
		return resultArray;
	}
	
	/***********************************************************************************************
	 * Returns the result with the given name, or null.
	 ***********************************************************************************************/
	public CFWQueryResult getResultByName(String resultName) {
		
		if(resultName == null) {
			return null;
		}
				
		for(int i = 0; i < resultArray.size(); i++) {
			
			CFWQueryResult current = resultArray.get(i);
			JsonElement name = current.getMetadata().get("name");
			String nameString = (name != null && !name.isJsonNull()) ? name.getAsString() : null;
			
			if(nameString != null && resultName.equals(nameString)
			) {
				return current;
			}
		}
		
		return null;
	}
	
	/***********************************************************************************************
	 * Returns the object stored in the meta objects.
	 ***********************************************************************************************/
	public Object getMetaObject(Object key) {
		return metaObjects.get(key);
	}
	
	/***********************************************************************************************
	 * Add the object to the meta objects.
	 ***********************************************************************************************/
	public void addMetaObject(Object key, Object value) {
		metaObjects.put(key, value);
	}
	
	/***********************************************************************************************
	 * Returns the object containing the metadata of the query.
	 ***********************************************************************************************/
	public JsonObject getMetadata() {
		return metadata;
	}
	
	/***********************************************************************************************
	 * Add the value to the metadata.
	 ***********************************************************************************************/
	public void addMetadata(String propertyName, String value) {
		metadata.addProperty(propertyName, value);
	}
	
	/***********************************************************************************************
	 * Add the value to the metadata.
	 ***********************************************************************************************/
	public void addMetadata(String propertyName, Integer value) {
		metadata.addProperty(propertyName, value);
	}
	
	/***********************************************************************************************
	 * Add the value to the metadata.
	 ***********************************************************************************************/
	public void addMetadata(String propertyName, Boolean value) {
		metadata.addProperty(propertyName, value);
	}
	
	/***********************************************************************************************
	 * Add the value to the metadata.
	 ***********************************************************************************************/
	public void addMetadata(String propertyName, JsonElement value) {
		metadata.add(propertyName, value);
	}
	
	/***********************************************************************************************
	 * Add the query parts value to the metadata.
	 ***********************************************************************************************/
	public void addMetadata(String propertyName, QueryPartValue value) {
		if(value != null) {
			value.addToJsonObject(propertyName, metadata);
		}
	}
	
	/***********************************************************************************************
	 * Checks if the metadata exists for the given key.
	 ***********************************************************************************************/
	public boolean hasMetadata(String propertyName) {
		return metadata.has(propertyName);
	}
	
	/***********************************************************************************************
	 * Returns the metadata for the given key.
	 ***********************************************************************************************/
	public JsonElement getMetadata(String propertyName) {
		return metadata.get(propertyName);
	}
	
	/***********************************************************************************************
	 * Returns the object containing the globals defined by this query.
	 ***********************************************************************************************/
	public JsonObject getGlobals() {
		return globals;
	}
	
	/***********************************************************************************************
	 * Adds a global value.
	 ***********************************************************************************************/
	public void addGlobal(String propertyName, String value) {
		globals.addProperty(propertyName, value);
	}
	
	/***********************************************************************************************
	 * Returns the object containing the metadata of the query.
	 ***********************************************************************************************/
	public void addGlobal(String propertyName, QueryPartValue value) {
		if(value != null) {
			value.addToJsonObject(propertyName, globals);
		}
	}
	
	/***********************************************************************************************
	 * Sets the globals object that is shared by various queries.
	 ***********************************************************************************************/
	public void setGlobals(JsonObject globalsObject) {
		globals = globalsObject;
	}
	
	/***********************************************************************************************
	 * Returns the object containing the parameters defined by this query.
	 ***********************************************************************************************/
	public JsonObject getParameters() {
		return parameters;
	}
	
	/***********************************************************************************************
	 * Sets a parameter default value.
	 * If a parameter is already present, this method does nothing.
	 ***********************************************************************************************/
	public void setParameterDefaultValue(String paramName, String value) {
		if(!parameters.has(paramName)) {
			parameters.addProperty(paramName, value);
		}
	}
	
	/***********************************************************************************************
	 * Sets the parameters object that is shared by various queries.
	 * Must have the format 
	 * 	{
	 * 		"paramName1": "paramValue2", 
	 * 		"paramName2": "paramValue2", 
	 * 		... 
	 *  }
	 ***********************************************************************************************/
	public void setParameters(JsonObject parametersObject) {
		if(parametersObject == null) { return; }
		
		parameters = parametersObject;
	}
	
	
	/***********************************************************************************************
	 * Returns the object containing the metadata of the query.
	 ***********************************************************************************************/
	public JsonObject getDisplaySettings() {
		return displaySettings;
	}
	
	
	/***********************************************************************************************
	 * Returns the object containing the metadata of the query.
	 ***********************************************************************************************/
	public void addDisplaySetting(String propertyName, String value) {
		displaySettings.addProperty(propertyName, value);
	}
	
	/***********************************************************************************************
	 * Returns the object containing the metadata of the query.
	 ***********************************************************************************************/
	public JsonObject getFieldFormats() {
		return fieldFormats;
	}
	
	
	/***********************************************************************************************
	 * Returns the detected Fieldnames
	 ***********************************************************************************************/
	public LinkedHashSet<String> getFinalFieldnames() {
		return contextFieldnameManager.getFinalFieldList();
	}
	
	/***********************************************************************************************
	 * Returns the detected Fieldnames
	 ***********************************************************************************************/
	public JsonArray getFieldnamesAsJsonArray() {
		return contextFieldnameManager.getFinalFieldListAsJsonArray();
	}
	
	/***********************************************************************************************
	 * Override the current fieldnames.
	 ***********************************************************************************************/
	public void setFieldnames(CFWQueryFieldnameManager fieldnameManager) {
		
		contextFieldnameManager = fieldnameManager;
	}
	
	/***********************************************************************************************
	 * Add a message for the user.
	 ***********************************************************************************************/
	public CFWQueryContext addMessage(MessageType type, String message) {

		CFW.Messages.addMessage(type, message);
//		if(alertMap != null) {
//			alertMap.put(message, new AlertMessage(type, message));
//		}
		return this;
	}
	
	/***********************************************************************************************
	 * Add a info message for the user.
	 ***********************************************************************************************/
	public CFWQueryContext addMessageInfo(String message) {
		return this.addMessage(MessageType.INFO, message);
	}
	
	/***********************************************************************************************
	 * Add a success message for the user.
	 ***********************************************************************************************/
	public CFWQueryContext addMessageSuccess(String message) {
		return this.addMessage(MessageType.SUCCESS, message);
	}
	
	/***********************************************************************************************
	 * Add a warning message for the user.
	 ***********************************************************************************************/
	public CFWQueryContext addMessageWarning(String message) {
		return this.addMessage(MessageType.WARNING, message);
	}
	
	/***********************************************************************************************
	 * Add a error message for the user.
	 ***********************************************************************************************/
	public CFWQueryContext addMessageError(String message) {
		return this.addMessage(MessageType.ERROR, message);
	}
	
	
}