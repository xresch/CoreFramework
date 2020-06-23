package com.xresch.cfw.response;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class JSONResponse extends AbstractTemplateJSON {

	private boolean success = true;
	
	public JSONResponse() {
		super();
	}

	@Override
	public StringBuffer buildResponse() {
		
		StringBuffer builder = new StringBuffer("{"); 
		
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
