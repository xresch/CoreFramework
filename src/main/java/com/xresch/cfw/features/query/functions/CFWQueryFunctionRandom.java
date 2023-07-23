package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.TreeSet;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.QueryPartValue;

public class CFWQueryFunctionRandom extends CFWQueryFunction {

	
	public static final String FUNCTION_NAME = "random";

	public CFWQueryFunctionRandom(CFWQueryContext context) {
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
		tags.add(CFWQueryFunction.TAG_CODING);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(min, max, nullPercentage)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns a random integer.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 
				"<ul>"
					+"<li><b>min:&nbsp;</b>The minimum value(inclusive, default 0).</li>"
					+"<li><b>max:&nbsp;</b>The maximum value(inclusive, default 100).</li>"
					+"<li><b>nullPercentage:&nbsp;</b>Percentage of null values, define an integer between 0 and 100 (Default: 0).</li>"
				+ "</ul>"
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
		// Initialize
		int lowerInclusive = 0;
		int upperInclusive = 100;
		int nullPercentage = 0;
		
		//----------------------------------
		// Min Value
		if(parameters.size() >= 1) { 
			
			QueryPartValue lowerValue = parameters.get(0);
			if(lowerValue.isNumberOrNumberString()) {
				lowerInclusive = lowerValue.getAsInteger();
			}
			
			//----------------------------------
			// Max Value
			if(parameters.size() >= 2) { 
				QueryPartValue upperValue = parameters.get(1);
				if(upperValue.isNumberOrNumberString()) {
					upperInclusive = upperValue.getAsInteger();
				}
				
				//----------------------------------
				// Null Percentage
				
				if(parameters.size() >= 3) { 
					QueryPartValue percentageValue = parameters.get(2);
					if(percentageValue.isNumberOrNumberString()) {
						nullPercentage = percentageValue.getAsInteger();
					}
				}
			}
		}
		

	
		//----------------------------------
		// Return Number
		return QueryPartValue.newNumber(
				CFW.Random.randomIntegerInRange(lowerInclusive, upperInclusive, nullPercentage)
			);
		
	}

}
