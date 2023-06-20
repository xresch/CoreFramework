package com.xresch.cfw.features.query;

import java.util.HashSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQueryContext{
	
	
	private long earliest = 0;
	private long latest = 0;
	private int timezoneOffsetMinutes = 0;
	private boolean checkPermissions = true;
	private CFWQueryResultList resultArray;
	
	private JsonObject metadata = new JsonObject();
	private JsonObject globals = new JsonObject();
	private JsonObject displaySettings = new JsonObject();
	private JsonObject fieldFormats = new JsonObject();
	
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
	 ***********************************************************************************************/
	public CFWQueryContext clone() {
		
		CFWQueryContext clonedContext = new CFWQueryContext(this.resultArray);
		clonedContext.checkPermissions(this.checkPermissions());
		clonedContext.setEarliest(this.getEarliestMillis());
		clonedContext.setLatest(this.getLatestMillis());
		clonedContext.setTimezoneOffsetMinutes(this.getTimezoneOffsetMinutes());
		clonedContext.setGlobals(this.getGlobals());
		clonedContext.setFieldnames(contextFieldnameManager);
		
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
	 * Returns the object containing the metadata of the query.
	 ***********************************************************************************************/
	public JsonObject getMetadata() {
		return metadata;
	}
	
	/***********************************************************************************************
	 * Returns the object containing the metadata of the query.
	 ***********************************************************************************************/
	public void addMetadata(String propertyName, String value) {
		metadata.addProperty(propertyName, value);
	}
	
	/***********************************************************************************************
	 * Returns the object containing the globals defined by this query.
	 ***********************************************************************************************/
	public JsonObject getGlobals() {
		return globals;
	}
	
	/***********************************************************************************************
	 * Returns the object containing the globals of the query.
	 ***********************************************************************************************/
	public void addGlobal(String propertyName, String value) {
		globals.addProperty(propertyName, value);
	}
	
	/***********************************************************************************************
	 * Sets the globals object that is shared by various queries.
	 ***********************************************************************************************/
	public void setGlobals(JsonObject globalsObject) {
		globals = globalsObject;
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
	public HashSet<String> getFinalFieldnames() {
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