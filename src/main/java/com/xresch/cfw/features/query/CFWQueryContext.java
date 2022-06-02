package com.xresch.cfw.features.query;

import java.util.HashSet;
import java.util.LinkedHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.response.bootstrap.AlertMessage;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQueryContext{
	
	private long earliest = 0;
	private long latest = 0;
	private boolean checkPermissions = true;
	
	private LinkedHashMap<String, AlertMessage> alertMap = CFW.Context.Request.getAlertMap();
	
	private JsonObject metadata = new JsonObject();
	private JsonObject globals = new JsonObject();
	private JsonObject displaySettings = new JsonObject();
	
	protected CFWQueryFieldnameManager contextFieldnameManager = new CFWQueryFieldnameManager();
	
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
	 * Set the latest time for this query.
	 ***********************************************************************************************/
	public CFWQueryContext addMessage(MessageType type, String message) {

		if(alertMap != null) {
			alertMap.put(message, new AlertMessage(type, message));
		}
		return this;
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
	
	
}