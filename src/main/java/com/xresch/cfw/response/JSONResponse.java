package com.xresch.cfw.response;

import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class JSONResponse extends AbstractTemplateJSON {

	private boolean success = true;
	
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
	
	public void setPayLoad(String text) {
		this.getContent()
		.append("\"")
		.append(CFW.JSON.escapeString(text))
		.append("\"");
	}
	public void setPayLoad(JsonElement element) {
		this.getContent().append(element.toString());
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
