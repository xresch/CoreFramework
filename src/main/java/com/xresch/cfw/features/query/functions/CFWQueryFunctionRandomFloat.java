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
public class CFWQueryFunctionRandomFloat extends CFWQueryFunction {

	
	public CFWQueryFunctionRandomFloat(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return "randomfloat";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public TreeSet<String> getTags(){
		TreeSet<String> tags = new TreeSet<>();
		tags.add(_CFWQueryCommon.TAG_CODING);
		return tags;
	}
		
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "randomfloat(min, max, nullPercentage)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns a random float.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 
				"<ul>"
					+"<li><b>min:&nbsp;</b>The minimum value(inclusive, default 0).</li>"
					+"<li><b>max:&nbsp;</b>The maximum value(inclusive, default 1).</li>"
					+"<li><b>nullPercentage:&nbsp;</b>Percentage of null values, define an integer between 0 and 100 (Default: 0).</li>"
				+ "</ul>"
			;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_randomfloat.html");
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
		float lowerInclusive = 0;
		float upperInclusive = 1;
		int nullPercentage = 0;
		
		//----------------------------------
		// Min Value
		if(parameters.size() >= 1) { 
			
			QueryPartValue lowerValue = parameters.get(0);
			if(lowerValue.isNumberOrNumberString()) {
				lowerInclusive = lowerValue.getAsFloat();
			}
			
			//----------------------------------
			// Max Value
			if(parameters.size() >= 2) { 
				QueryPartValue upperValue = parameters.get(1);
				if(upperValue.isNumberOrNumberString()) {
					upperInclusive = upperValue.getAsFloat();
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
				CFW.Random.floatInRange(lowerInclusive, upperInclusive, nullPercentage)
			);
		
	}

}
