package com.xresch.cfw.response.bootstrap;

import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw._main.CFWMessages.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021
 * @license MIT-License
 **************************************************************************************************************/
public class CFWHTMLItemAlertMessage {
	
	private MessageType type;
	private String message;
	
	public CFWHTMLItemAlertMessage(MessageType type, String message){
		this.type = type;
		this.message = message;
	}
	
	public String createHTML() {
		//		<div class=\"alert alert-success\" role=\"alert\">...</div>
		
		String clazz = "";
		switch(type){
			
			case SUCCESS: 	clazz = "alert-success"; break;
			case INFO: 		clazz = "alert-info"; break;
			case WARNING: 	clazz = "alert-warning"; break;
			case ERROR: 	clazz = "alert-danger"; break;
			default:	 	clazz = "alert-info"; break;
			
		}
		
		StringBuilder html = new StringBuilder();
		html.append("<div class=\"alert alert-dismissible ").append(clazz).append("\" role=\"alert\">");
		html.append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" aria-label=\"Close\"><span aria-hidden=\"true\">&times;</span></button>");
		html.append(message);
		html.append("</div>\n");
		
		return html.toString();
	}

	public MessageType getType() {
		return type;
	}

	public CFWHTMLItemAlertMessage setType(MessageType type) {
		this.type = type;
		return this;
	}

	public String getMessage() {
		return message;
	}

	public CFWHTMLItemAlertMessage setMessage(String message) {
		this.message = message;
		return this;
	}
	
	
	
}