package com.xresch.cfw.features.query.functions;

import com.xresch.cfw.features.query.CFWQueryContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionG extends CFWQueryFunctionGlobals {

	
	private static final String FUNCTION_NAME = "g";

	public CFWQueryFunctionG(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return FUNCTION_NAME;
	}
		
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(propertyName [, propertyValue])";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Shorthand for the globals()-function. Sets or gets the value for the global property set with the command globals.";
	}
	
}
