package com.pengtoolbox.cfw.features.api;

import java.sql.ResultSet;

import com.pengtoolbox.cfw.datahandling.CFWObject;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
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