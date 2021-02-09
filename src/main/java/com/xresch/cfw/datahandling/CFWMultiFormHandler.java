package com.xresch.cfw.datahandling;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public abstract class CFWMultiFormHandler {
	
	public abstract void handleForm( HttpServletRequest request,HttpServletResponse response, CFWMultiForm form, LinkedHashMap<Integer, CFWObject> originsMap);

}
