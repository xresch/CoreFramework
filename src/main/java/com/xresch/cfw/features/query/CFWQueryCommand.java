package com.xresch.cfw.features.query;

import java.text.ParseException;
import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.pipeline.PipelineAction;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public abstract class CFWQueryCommand extends PipelineAction<EnhancedJsonObject, EnhancedJsonObject> {

	protected CFWQuery parent;
	
	public CFWQueryCommand(CFWQuery parent) {
		this.parent = parent;
	}
	
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
	public abstract void setAndValidateQueryParts(CFWQueryParser parser, ArrayList<QueryPart> parts)  throws ParseException;
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQuery getParent() {
		return parent;
	}
		
}
