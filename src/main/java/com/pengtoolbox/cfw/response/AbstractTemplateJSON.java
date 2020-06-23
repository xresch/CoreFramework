package com.pengtoolbox.cfw.response;

import com.pengtoolbox.cfw._main.CFW;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public abstract class AbstractTemplateJSON extends AbstractResponse {

	public AbstractTemplateJSON() {
		super();
		CFW.Context.Request.getHttpServletResponse().setContentType("application/json");
	}
	
	@Override
	public StringBuffer buildResponse() {
		return this.content;
	}
	
	@Override
	public int getEstimatedSizeChars() {
		
		int size = this.content.length();
		
		return size;
	}

}
