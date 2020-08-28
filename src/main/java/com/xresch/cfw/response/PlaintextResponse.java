package com.xresch.cfw.response;

import com.xresch.cfw._main.CFW;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class PlaintextResponse extends AbstractResponse {

	public PlaintextResponse() {
		super();
		CFW.Context.Request.getHttpServletResponse().setContentType("text/plain");
	}

	@Override
	public StringBuilder buildResponse() {
		return this.content;
	}
	
	@Override
	public int getEstimatedSizeChars() {
		
		int size = this.content.length();
		
		return size;
	}

}
