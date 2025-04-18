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
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionTan extends CFWQueryFunction {

	public static final String FUNCTION_NAME = "tan";

	public CFWQueryFunctionTan(CFWQueryContext context) {
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
		tags.add(_CFWQueryCommon.TAG_MATH);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(number, useDegrees)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns the tangent value of a radians or degree value.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 
			 "<ul>"
			+"<li><b>number:&nbsp;</b>The value you want the tangent value for.</li>"
			+"<li><b>useDegrees:&nbsp;</b>(Optional)Set to true to use degrees instead of radians for first parameter.</li>"
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
		
		//-----------------------------
		// Handle empty params
		int paramCount = parameters.size();
		if(paramCount == 0) {
			return QueryPartValue.newNumber(0);
		}

		//-----------------------------
		// Get Value to Calculate
		QueryPartValue initialValue = parameters.get(0);
		
		
		//-----------------------------
		// Calculate
		if(initialValue.isNumberOrNumberString()) {
			
			//-----------------------------
			// Get useDegrees
			boolean useDegrees = false;
			if(paramCount >= 2) {
				QueryPartValue booleanValue = parameters.get(1);
				if(booleanValue.isBoolOrBoolString()) {
					useDegrees = booleanValue.getAsBoolean();
				}
			}
			
			//-----------------------------
			// Calculate Value
			double doubleValue = initialValue.getAsDouble();
			
			if(useDegrees) {
				doubleValue = Math.toRadians(doubleValue);
			}
			
			return QueryPartValue.newNumber(
					Math.tan( doubleValue )
				);
			
			
		}
		
		// return empty in other cases
		return QueryPartValue.newNumber(0);
	}

}
