package com.xresch.cfw.response;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public abstract class AbstractResponse {

	protected StringBuilder content = new StringBuilder();
	protected HttpServletRequest request;
	protected boolean useGlobaleLocale = false;
	
	public AbstractResponse(){
		this.request = CFW.Context.Request.getRequest();
		CFW.Context.Request.setResponse(this);
		
	}
	
	//##############################################################################
	// Class Methods
	//##############################################################################
	public abstract StringBuilder buildResponse();
	public abstract int getEstimatedSizeChars();
	
	//##############################################################################
	// Getters
	//##############################################################################
	public StringBuilder getContent() { return content;}

	//##############################################################################
	// Setters
	//##############################################################################
	public void setContentType(String contentType) {
		CFW.Context.Request.getHttpServletResponse().setContentType(contentType);
		
	}
	
	public void setContent(StringBuilder content) {this.content = content;}
	
	public boolean useGlobaleLocale() {
		return this.useGlobaleLocale;
	}
	
	public void useGlobalLocale(boolean useGlobalLocale) {
		this.useGlobaleLocale = useGlobalLocale;
	}
	
	public StringBuilder append(String string) {
		content.append(string);
		return content;
	}
	
	
}
