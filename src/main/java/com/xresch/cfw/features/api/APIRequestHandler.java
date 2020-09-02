package com.xresch.cfw.features.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public abstract class APIRequestHandler {
	

	public abstract void handleRequest(HttpServletRequest request, HttpServletResponse response, APIDefinition definition);

}
