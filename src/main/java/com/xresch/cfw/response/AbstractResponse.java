package com.xresch.cfw.response;

import javax.servlet.http.HttpServletRequest;

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
	public void setContent(StringBuilder content) {this.content = content;}

	public boolean useGlobaleLocale() {
		return this.useGlobaleLocale;
	}
	
	public void useGlobaleLocale(boolean useGlobaleLocale) {
		this.useGlobaleLocale = useGlobaleLocale;
	}
	
	
	
}
