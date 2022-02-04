package com.xresch.cfw.features.query;

import java.util.LinkedHashMap;

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
	
	private LinkedHashMap<String, AlertMessage> alertMap = CFW.Context.Request.getAlertMap();
	
	private JsonObject metadata = new JsonObject();
	
	
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
	
	
}