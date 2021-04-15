package com.xresch.cfw.response;

import com.xresch.cfw._main.CFW;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CSVResponse extends AbstractResponse {

	public CSVResponse() {
		super();
		CFW.Context.Request.getHttpServletResponse().setContentType("application/csv");
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
