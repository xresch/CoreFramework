package com.xresch.cfw.features.query;

import java.util.ArrayList;

import com.xresch.cfw.features.query.commands.CFWQueryCommandSource;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public abstract class CFWQueryFunction{

	protected CFWQueryContext context;
	
	public CFWQueryFunction(CFWQueryContext context) {
		this.context = context;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public abstract String uniqueName();
	
	/***********************************************************************************************
	 * Return a short description that can be shown in content assist and will be used as intro text
	 * in the manual. Do not use newlines in this description.
	 ***********************************************************************************************/
	public abstract String descriptionShort();
	
	/***********************************************************************************************
	 * Return the syntax as a single line. This will be shown in the manual and in content assist.
	 * Will be added in the manual under the header " <h2>Syntax</h2>" as a "<pre><code>" element. 
	 ***********************************************************************************************/
	public abstract String descriptionSyntax();
	
	/***********************************************************************************************
	 * Describe the parameters of this function.
	 * If you add headers to your description it is recommended to use <h3> or lower headers.
	 ***********************************************************************************************/
	public abstract String descriptionSyntaxDetailsHTML();	
	
	/***********************************************************************************************
	 * Return the description for the manual page.
	 * This description will be shown on the manual under the header " <h2>Usage</h2>".
	 * If you add headers to your description it is recommended to use <h3> or lower headers.
	 ***********************************************************************************************/
	public abstract String descriptionHTML();
	
	/*************************************************************************
	 * Return true if this function supports aggregation.
	 *************************************************************************/
	public abstract boolean supportsAggregation();
	
	/*************************************************************************
	 * Implement the aggregation.
	 * Return the result with the method execute.
	 * 
	 *************************************************************************/
	public abstract void aggregate(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters);
	
	/***********************************************************************************************
	 * Execute the function and return the result as a QueryPartValue.
	 * 
	 ***********************************************************************************************/
	public abstract QueryPartValue execute(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters);
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryContext getContext() {
		return context;
	}

}
