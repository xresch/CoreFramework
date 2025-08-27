package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.TreeSet;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query._CFWQueryCommon;
import com.xresch.cfw.features.query.parse.QueryPartValue;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2025 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionSeparators extends CFWQueryFunction {

	
	private static final String FUNCTION_NAME = "separators";

	public CFWQueryFunctionSeparators(CFWQueryContext context) {
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
		return FUNCTION_NAME+"(numberOrFieldname, separator, eachDigit)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Adds separators to numbers.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<ul>"
				  +"<li><b>numberOrFieldname:&nbsp;</b>The number or a fieldname that should get separators.</li>"
				  +"<li><b>separator:&nbsp;</b>(Optional)The separator to add, default is &quot;'&quot;.</li>"
				  +"<li><b>eachDigit:&nbsp;</b>(Optional)Number of digits that should be separated, default is 3.</li>"
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
		
		String separator = "'";
		int eachDigit = 3;
		//----------------------------------
		// Return null if no params
		if(paramCount == 0) { 
			return QueryPartValue.newNull();
		}
				
		//----------------------------------
		// Get Value
		QueryPartValue valuePart = parameters.get(0);

		if (valuePart == null) return valuePart;
        if (valuePart.isBoolOrBoolString()) return valuePart;
		
        String stringValue = valuePart.getAsString();
        
		//----------------------------------
		// Get Separator
		if(paramCount > 1 ) { 
			separator = parameters.get(1).getAsString();
		}
		
		//----------------------------------
		// Get eachDigit
		if(paramCount > 2 ) { 
			eachDigit = parameters.get(2).getAsInteger();
		}
		


        int startingPos = stringValue.lastIndexOf('.') - 1;
        String resultString = "";
        // If no decimal point found
        if (startingPos == -2) {
            startingPos = stringValue.length() - 1;
            resultString = "";
        }else {
        	resultString = stringValue.substring(startingPos + 1);
        }

        int position = 0;
        for (int i = startingPos; i >= 0; i--) {
            position++;
            char ch = stringValue.charAt(i);

            // Handle minus in negative number
            if (ch == '-' && resultString.startsWith(separator)) {
                resultString = ch + resultString.substring(1);
                break;
            }

            resultString = ch + resultString;

            if (position % eachDigit == 0 && i > 0) {
                resultString = separator + resultString;
            }
        }

		return QueryPartValue.newString( resultString);
	}

}
