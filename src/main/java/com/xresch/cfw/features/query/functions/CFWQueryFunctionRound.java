package com.xresch.cfw.features.query.functions;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.QueryPartValue;

public class CFWQueryFunctionRound extends CFWQueryFunction {

	
	public CFWQueryFunctionRound(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return "round";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "round(number, precision)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns the rounded representation for a number.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>number:&nbsp;</b>The value that should be rounded.</p>"
			 + "<p><b>precision:&nbsp;</b>Number of decimal places.</p>"
			;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_round.html");
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
		if(paramCount == 0) {
			return QueryPartValue.newNumber(0);
		}

		QueryPartValue initialValue = parameters.get(0);
		
		
		if(initialValue.isNumberOrNumberString()) {
			int precision = 0;
			if(paramCount > 1 && parameters.get(0).isNumberOrNumberString()) {
				precision = parameters.get(1).getAsInteger();
			}
			
			return QueryPartValue.newNumber(
					initialValue.getAsBigDecimal().setScale(precision, RoundingMode.HALF_UP)
				);
		}
		
		// return empty in other cases
		return QueryPartValue.newNumber(0);
	}

}
