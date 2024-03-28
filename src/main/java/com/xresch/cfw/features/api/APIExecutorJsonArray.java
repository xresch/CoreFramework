package com.xresch.cfw.features.api;

import com.google.gson.JsonArray;
import com.xresch.cfw.datahandling.CFWObject;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024 
 * @license MIT-License
 **************************************************************************************************************/
public abstract class APIExecutorJsonArray {
	
	/***********************************************************
	 * Creates and returns a JsonArray containing JsonObjects
	 * as records.
	 * 
	 * @param definition the instance of APIDefinitionSQL that
	 * calls this method. Call setStatus() on this to set a 
	 * status.
	 * @param instance of the class given to the constructor of 
	 * 		  APIDefinitionSQL, contains the values given to the 
	 * 		  API by parameters
	 * @return JsonArray , or null on error.
	 ***********************************************************/
	public abstract JsonArray execute(APIDefinitionJsonArray definition, CFWObject instance);
}