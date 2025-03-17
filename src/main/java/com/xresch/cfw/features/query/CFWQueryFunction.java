package com.xresch.cfw.features.query;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.TreeSet;

import com.xresch.cfw.features.query.commands.CFWQueryCommandSource;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemAlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public abstract class CFWQueryFunction{

	protected CFWQueryContext context;
	protected CFWQueryCommand parent;
	
	public CFWQueryFunction(CFWQueryContext context) {
		this.context = context;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public abstract String uniqueName();
	
	/***********************************************************************************************
	 * Return an arrayList with Tags
	 ***********************************************************************************************/
	public abstract TreeSet<String> getTags();
	
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
	 * Return the result with the method execute().
	 * 
	 *************************************************************************/
	public abstract void aggregate(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters);
	
	
	/*************************************************************************
	 * Override this method to validate if only QueryParts of a certain type
	 * are passed to this method.
	 * For example only allowing literal string values.
	 * This method is responsible to throw a ParseException in case something
	 * is not right.
	 * 
	 * @param partsArray the queryParts passed to this function
	 * @param doCheckPermissions toggle if permissions should be checked if
	 * the user has the required permissions to execute the function with the 
	 * given parameters.
	 *************************************************************************/
	public boolean validateQueryParts(ArrayList<QueryPart> partsArray, boolean doCheckPermissions) throws ParseException {
		return true;
	}
	
	/*************************************************************************
	 * Override this method and return true to not replace strings matching
	 * field names with the values of the field.
	 * This is needed in case you want to not receive the field value, but 
	 * the field names.
	 *************************************************************************/
	public boolean receiveStringParamsLiteral() {
		return false;
	}
	/***********************************************************************************************
	 * Execute the function and return the result as a QueryPartValue.
	 * If the implementation of this class stores any internal values(e.g. for aggregation), the call
	 * to this function has to reset all internally stored values. Else the resulting values might
	 * be incorrect.
	 * 
	 ***********************************************************************************************/
	public abstract QueryPartValue execute(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters);
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQueryContext getContext() {
		return context;
	}

	/***********************************************************************************************
	 * Returns the parent command of this function.
	 ***********************************************************************************************/
	public CFWQueryCommand getParentCommand() {
		return parent;
	}

	/***********************************************************************************************
	 * INTERNAL USE
	 * Set the command which is the parent of the function.
	 ***********************************************************************************************/
	public void setParentCommand(CFWQueryCommand parent) {
		this.parent = parent;
	}

	
}
