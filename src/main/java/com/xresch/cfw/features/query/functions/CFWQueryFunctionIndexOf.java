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
public class CFWQueryFunctionIndexOf extends CFWQueryFunction {

	
	public CFWQueryFunctionIndexOf(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return "indexof";
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
		return "indexof(stringOrFieldname, searchString[, beginIndex])";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns the index of the first occurence for for the searched string.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>stringOrFieldname:&nbsp;</b>The string or or a fieldname, the value in which a string should be searched.</p>"
			  +"<p><b>searchString:&nbsp;</b>The string to search for.</p>"
			  +"<p><b>beginIndex:&nbsp;</b>(Optional)The index to start the search from.</p>"
			;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_indexof.html");
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
			if(Strings.isNullOrEmpty(initialString)) { return QueryPartValue.newNumber(-1); }
			
			//----------------------------------
			// Get string to search
			String searchThis = parameters.get(1).getAsString();
				
			//----------------------------------
			// Get Begin Index
			Integer beginIndex = null;
			if(paramCount > 2 && parameters.get(2).isNumberOrNumberString()) {
				beginIndex = parameters.get(2).getAsInteger();

				if(beginIndex >= initialString.length()) {
					beginIndex = initialString.length();
				}
			}
			
			//----------------------------------
			// Get Begin Index
			if(beginIndex == null) { 
				return QueryPartValue.newNumber(initialString.indexOf(searchThis)); 
			}else {
				return QueryPartValue.newNumber(initialString.indexOf(searchThis, beginIndex)); 
			}
		}
		
		//----------------------------------
		// Return -1 in other cases
		return QueryPartValue.newNumber(-1);
	}

}
