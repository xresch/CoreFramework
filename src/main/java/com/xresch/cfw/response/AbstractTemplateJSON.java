package com.xresch.cfw.response;

import com.xresch.cfw._main.CFW;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public abstract class AbstractTemplateJSON extends AbstractResponse {

	public AbstractTemplateJSON() {
		super();
		CFW.Context.Request.getHttpServletResponse().setContentType("application/json");
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
