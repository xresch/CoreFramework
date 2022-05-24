package com.xresch.cfw.response;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class JSONResponse extends AbstractTemplateJSON {

	protected boolean success = true;
	protected LinkedHashMap<String, JsonElement> jsonElements = new LinkedHashMap<>();
	
	public JSONResponse() {
		super();
	}

	@Override
	public StringBuilder buildResponse() {
		
		StringBuilder builder = new StringBuilder("{"); 
		
		//----------------------------
		// Success
		builder.append("\"success\": ").append(success).append(",");
		
		//----------------------------
		// Messages
		builder.append("\"messages\": ")
			   .append(CFW.Context.Request.getAlertsAsJSONArray())
			   .append(",");
		
		//----------------------------
		// Custom Elements
		for(Entry<String, JsonElement> entry : jsonElements.entrySet()) {
			builder.append("\""+entry.getKey()+"\": ")
				   .append(entry.getValue().toString())
				   .append(",");
		}
		//----------------------------
		// Messages
		if (content.length() == 0) {
			content.append("null");
		}
		builder.append("\"payload\": ")
			   .append(content);
		//----------------------------
		// Close and Return
		builder.append("}"); 
		return builder;
	}
	
	public void addCustomAttribute(String fieldname, Boolean bool) {
		jsonElements.put(fieldname, new JsonPrimitive(bool));
	}
	
	public void addCustomAttribute(String fieldname, Integer number) {
		jsonElements.put(fieldname, new JsonPrimitive(number));
	}
	
	public void addCustomAttribute(String fieldname, String string) {
		jsonElements.put(fieldname, new JsonPrimitive(string));
	}
	
	public void addCustomAttribute(String fieldname, JsonElement element) {
		jsonElements.put(fieldname, element);
	}
	
	public void setPayLoad(String text) {
		this.getContent()
		.append("\"")
		.append(CFW.JSON.escapeString(text))
		.append("\"");
	}
	
	
	public void setPayLoad(JsonElement element) {
		if(element == null) {
			this.getContent().append("null");
		}else {
			this.getContent().append(element.toString());
		}
	}
	
	public void copyFrom(JSONResponse toCopy) {
		if(toCopy != null) {
			this.content = toCopy.content;
			this.success = toCopy.success;
			this.jsonElements = toCopy.jsonElements;
		}
	}
	
	@Override
	public int getEstimatedSizeChars() {
		
		int size = this.content.length();
		
		return size;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public void addAlert(MessageType type, String message) {
		CFW.Context.Request.addAlertMessage(type, message);
	}

}
