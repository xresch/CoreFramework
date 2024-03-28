package com.xresch.cfw.features.api;

import java.sql.ResultSet;

import com.xresch.cfw.datahandling.CFWObject;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public abstract class APIExecutorSQL {
	/***********************************************************
	 * Execute an SQL statement
	 * @param definition the instance of APIDefinitionSQL that
	 * calls this method. Call setStatus() on this to set a 
	 * status.
	 * @param instance of the class given to the constructor of 
	 * 		  APIDefinitionSQL, contains the values given to the 
	 * 		  API by parameters
	 * @return the ResultSet of the SQL, or null on error.
	 ***********************************************************/
	public abstract ResultSet execute(APIDefinitionSQL definition, CFWObject instance);
}