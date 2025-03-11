package com.xresch.cfw.features.query.functions;

import com.xresch.cfw.features.query.CFWQueryContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionP extends CFWQueryFunctionParam {

	
	private static final String FUNCTION_NAME = "p";

	public CFWQueryFunctionP(CFWQueryContext context) {
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
		return FUNCTION_NAME+"(propertyName [, fallbackForNullOrEmpty])";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Shorthand for the param()-function. Gets the value for the parameter or uses the fallback value.";
	}
	
}
