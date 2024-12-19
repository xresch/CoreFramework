package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.TreeSet;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.QueryPartValue;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionSplit extends CFWQueryFunction {

	
	private static final String FUNCTION_NAME = "split";

	public CFWQueryFunctionSplit(CFWQueryContext context) {
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
		tags.add(CFWQueryFunction.TAG_STRINGS);
		return tags;
	}
		
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(stringOrFieldname, regex)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Splits the string by a regular expression.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<ul>"
				  +"<li><b>stringOrFieldname:&nbsp;</b>The string or or a fieldname that should be substringed.</li>"
				  +"<li><b>regex:&nbsp;</b>The delimiting regular expression.</li>"
			  +"</ul>"
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
		
		int paramCount = parameters.size();
		
		//----------------------------------
		// Return null if no params
		if(paramCount == 0) { 
			return QueryPartValue.newNull();
		}
		
		//----------------------------------
		// Return same if value only
		if(parameters.size() == 1 ) { 
			return parameters.get(0).convertToArray();

		}
		
		//----------------------------------
		// Get String
		if(paramCount > 1) { 
			
			//----------------------------------
			// Get String
			QueryPartValue initialValue = parameters.get(0);
			String initialString = initialValue.getAsString();

			if(Strings.isNullOrEmpty(initialString)) { return initialValue; }
			
			//----------------------------------
			// Get regexString
			String regexString;
			
			if(parameters.get(1).isNullOrEmptyString()) {
				 return initialValue.convertToArray();
			}
			
			regexString = parameters.get(1).getAsString();
				
			//----------------------------------
			// Do The Split
			String[] splitResult = initialString.split(regexString);
			
			JsonArray array = new JsonArray();
			for(String part : splitResult) {
				array.add(part);
			}
			
			return QueryPartValue.newFromJsonElement(array);
		}
		
		// return empty in other cases
		return QueryPartValue.newFromJsonElement( new JsonArray() );
	}

}
