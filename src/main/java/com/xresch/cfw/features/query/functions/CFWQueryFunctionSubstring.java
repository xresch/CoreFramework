package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.TreeSet;

import com.google.common.base.Strings;
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
public class CFWQueryFunctionSubstring extends CFWQueryFunction {

	
	public CFWQueryFunctionSubstring(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return "substring";
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
		return "substring(stringOrFieldname, beingIndex, endIndex)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns a substring for the given string.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>stringOrFieldname:&nbsp;</b>The string or or a fieldname that should be substringed.</p>"
			  +"<p><b>beginIndex:&nbsp;</b>The begin index.</p>"
			  +"<p><b>endIndex:&nbsp;</b>(Optional)The end index.</p>"
			;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_substring.html");
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
		
		QueryPartValue initialValue = parameters.get(0);
		int paramCount = parameters.size();
		
		//----------------------------------
		// Get String
		if(paramCount > 1) { 
			
			//----------------------------------
			// Get String
			String initialString = initialValue.getAsString();
			if(Strings.isNullOrEmpty(initialString)) { return initialValue; }
			
			//----------------------------------
			// Get Begin Index
			Integer beginIndex;
			if(parameters.get(1).isNumberOrNumberString()) {
				beginIndex = parameters.get(1).getAsInteger();
				
				if(beginIndex < 0) {
					beginIndex = 0;
				}else if(beginIndex >= initialString.length()) {
					beginIndex = initialString.length();
				}
			}else {
				return initialValue;
			}
			
			//----------------------------------
			// Get End Index
			Integer endIndex = null;
			if(paramCount > 2 && parameters.get(2).isNumberOrNumberString()) {
				endIndex = parameters.get(2).getAsInteger();

				if(endIndex >= initialString.length()) {
					endIndex = initialString.length();
				}else if(endIndex < beginIndex) {
					endIndex = beginIndex;
				}
				
			}
			
			//----------------------------------
			// Get Begin Index
			if(endIndex == null) { 
				return QueryPartValue.newString(initialString.substring(beginIndex)); 
			}else {
				return QueryPartValue.newString(initialString.substring(beginIndex, endIndex)); 
			}
		}
		
		//----------------------------------
		// Return same if no params
		if(parameters.size() == 1) { 
			return parameters.get(0);
		}
		
		// return empty in other cases
		return QueryPartValue.newString("");
	}

}
