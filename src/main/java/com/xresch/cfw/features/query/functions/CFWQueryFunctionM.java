package com.xresch.cfw.features.query.functions;

import com.xresch.cfw.features.query.CFWQueryContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionM extends CFWQueryFunctionMeta {

	
	private static final String FUNCTION_NAME = "m";

	public CFWQueryFunctionM(CFWQueryContext context) {
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
		return "Shorthand for the meta()-function. Sets or gets the value for the meta property set with the command meta.";
	}
	
}
