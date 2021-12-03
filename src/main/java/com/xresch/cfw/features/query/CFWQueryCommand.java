package com.xresch.cfw.features.query;

import java.util.ArrayList;

import com.xresch.cfw.features.query.parse.QueryPart;

public abstract class CFWQueryCommand {

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public abstract String uniqueName();
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public abstract String shortDescription();
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public abstract String syntax();
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public abstract boolean setAndValidateQueryParts(ArrayList<QueryPart> parts);
	
	
}
