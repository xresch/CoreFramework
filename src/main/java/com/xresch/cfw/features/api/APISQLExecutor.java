package com.xresch.cfw.features.api;

import java.sql.ResultSet;

import com.xresch.cfw.datahandling.CFWObject;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public abstract class APISQLExecutor {
	/***********************************************************
	 * Execute an SQL statement
	 * @param definition
	 * @param instance
	 * @return the ResultSet of the SQL
	 ***********************************************************/
	public abstract ResultSet execute(APIDefinitionSQL definition, CFWObject instance);
}