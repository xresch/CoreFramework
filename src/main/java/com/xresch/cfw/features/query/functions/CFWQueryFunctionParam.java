package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.TreeSet;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query._CFWQueryCommon;
import com.xresch.cfw.features.query.parse.QueryPartValue;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionParam extends CFWQueryFunction {

	
	public static final String FUNCTION_NAME = "param";

	public CFWQueryFunctionParam(CFWQueryContext context) {
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
	public TreeSet<String> getTags(){
		TreeSet<String> tags = new TreeSet<>();
		tags.add(_CFWQueryCommon.TAG_CODING);
		return tags;
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
		return "Gets the value for the parameter..";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 
				"<p><b>propertyName:&nbsp;</b>The name of the property to get or set.</p>"
			  + "<p><b>fallbackForNullOrEmpty:&nbsp;</b>(Optional)if given, this value will be used if the parameter value is null or empty string.</p>"
			;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_"+FUNCTION_NAME+".html");
	}


	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public boolean supportsAggregation() {
		return false;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void aggregate(EnhancedJsonObject object,ArrayList<QueryPartValue> parameters) {
		// not supported
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public QueryPartValue execute(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters) {
		
		//----------------------------------
		// Return empty string if no params
		if(parameters.size() == 0) { return QueryPartValue.newString(""); }
		
		//----------------------------------
		// Get / Set Global Value
		String propertyName = parameters.get(0).getAsString();
		
		if(propertyName != null) { 

			//----------------------------------
			// Se Global Value
			
			JsonObject paramsObject = this.getContext().getParameters();
			JsonElement param = paramsObject.get(propertyName);
			
			if(parameters.size() > 1) {
				if(param == null 
				|| param.isJsonNull() 
				|| (param.isJsonPrimitive() && param.getAsString().trim().length() == 0)
				) {
					return parameters.get(1); 
				}
			}
			
			//----------------------------------
			// Get Param Value
			return QueryPartValue.newFromJsonElement(param); 
		}
		
		return QueryPartValue.newString(""); 
		
		
	}

}
