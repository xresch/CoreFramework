package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.TreeSet;

import com.google.common.base.Strings;
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
public class CFWQueryFunctionReplace extends CFWQueryFunction {

	
	private static final String FUNCTION_NAME = "replace";

	public CFWQueryFunctionReplace(CFWQueryContext context) {
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
		tags.add(_CFWQueryCommon.TAG_STRINGS);
		return tags;
	}
		
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(stringOrFieldname, searchString, replacement)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns a string with every occurence of the search string replaced with the replacement.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>stringOrFieldname:&nbsp;</b>The input value for the replacement.</p>"
			  +"<p><b>searchString:&nbsp;</b>The string to search for.</p>"
			  +"<p><b>replacement:&nbsp;</b>(Optional)The replacement for the findings.</p>"
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
			return parameters.get(0);
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
			// Get searchString
			String searchString;
			
			if(parameters.get(1).isNullOrEmptyString()) {
				 return initialValue;
			}
			
			searchString = parameters.get(1).getAsString();
				
			//----------------------------------
			// Get Replacement
			String replacement = "";
			if(paramCount > 2 && !parameters.get(2).isNullOrEmptyString()) {
				replacement = parameters.get(2).getAsString();
			}
			
			//----------------------------------
			// Do Replacement
			String result = initialString.replace(searchString, replacement);
			return QueryPartValue.newString(result);
		}
		

		// return empty in other cases
		return QueryPartValue.newString("");
	}

}
