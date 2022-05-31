package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.QueryPartValue;

public class CFWQueryFunctionIf extends CFWQueryFunction {

	
	public CFWQueryFunctionIf(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return "if";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "if(condition, trueValue, falseValue)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Evaluates the condition, returns the respective value for true or false.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>condition:&nbsp;</b>The condition to evaluate for the if-statement.</p>"
			  +"<p><b>trueValue:&nbsp;</b>The value to return if the condition is true.</p>"
			  +"<p><b>falseValue:&nbsp;</b>The value to return if the condition is false.</p>"
			;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_if.html");
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
		// Return same value if not second param
		if(parameters.size() >= 2) { 
			
			QueryPartValue condition = parameters.get(0); 
			QueryPartValue trueValue = parameters.get(1); 
			QueryPartValue falseValue = (parameters.size() >= 3) ? parameters.get(2) : QueryPartValue.newString(""); 
			
			if(condition.getAsBoolean()) {
				return trueValue;
			}else {
				return falseValue;
			}
		}
		
		//----------------------------------
		// Return empty string if not enough params
		return QueryPartValue.newNull();
		
	}

}
