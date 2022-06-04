package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.QueryPartValue;

public class CFWQueryFunctionLastIndexOf extends CFWQueryFunction {

	
	public CFWQueryFunctionLastIndexOf(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return "lastindexof";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "lastindexof(stringOrFieldname, searchString[, beginIndex])";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns the index of the last occurence for the searched string.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>stringOrFieldname:&nbsp;</b>The string or or a fieldname, the value in which a string should be searched.</p>"
			  +"<p><b>searchString:&nbsp;</b>The string to search for.</p>"
			  +"<p><b>fromIndex:&nbsp;</b>(Optional)The index to start the search from. Search will be executed backwards starting from this index.</p>"
			;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_lastindexof.html");
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
			Integer fromIndex = null;
			if(paramCount > 2 && parameters.get(2).isNumberOrNumberString()) {
				fromIndex = parameters.get(2).getAsInteger();

				if(fromIndex >= initialString.length()) {
					fromIndex = initialString.length();
				}
			}
			
			//----------------------------------
			// Get Begin Index
			if(fromIndex == null) { 
				return QueryPartValue.newNumber(initialString.lastIndexOf(searchThis)); 
			}else {
				return QueryPartValue.newNumber(initialString.lastIndexOf(searchThis, fromIndex)); 
			}
		}
		
		//----------------------------------
		// Return -1 in other cases
		return QueryPartValue.newNumber(-1);
	}

}
