package com.xresch.cfw.response;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw._main.CFW;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public abstract class AbstractResponse {

	protected StringBuffer content = new StringBuffer();
	protected HttpServletRequest request;
	protected boolean useGlobaleLocale = false;
	
	public AbstractResponse(){
		this.request = CFW.Context.Request.getRequest();
		CFW.Context.Request.setResponse(this);
		
	}
	
	//##############################################################################
	// Class Methods
	//##############################################################################
	public abstract StringBuffer buildResponse();
	public abstract int getEstimatedSizeChars();
	
	//##############################################################################
	// Getters
	//##############################################################################
	public StringBuffer getContent() { return content;}

	//##############################################################################
	// Setters
	//##############################################################################
	public void setContent(StringBuffer content) {this.content = content;}

	public boolean useGlobaleLocale() {
		return this.useGlobaleLocale;
	}
	
	public void useGlobaleLocale(boolean useGlobaleLocale) {
		this.useGlobaleLocale = useGlobaleLocale;
	}
	
	
	
}
