package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.TreeSet;

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
public class CFWQueryFunctionRandomColorHSL extends CFWQueryFunction {

	
	private static final String FUNCTION_NAME = "randomColorHSL";

	public CFWQueryFunctionRandomColorHSL(CFWQueryContext context) {
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
		tags.add(CFWQueryFunction.TAG_FORMAT);
		return tags;
	}
		
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(minS, maxS, minL, maxL)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns a HSL CSS color string.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 
				"<ul>"
					+"<li><b>minS:&nbsp;</b>(Optional)The minimum saturation in percent 0-100.</li>"
					+"<li><b>maxS:&nbsp;</b>(Optional)The maximum saturation in percent 0-100.</li>"
					+"<li><b>minL:&nbsp;</b>(Optional)The minimum lightness in percent 0-100.</li>"
					+"<li><b>maxL:&nbsp;</b>(Optional)The maximum lightness in percent 0-100.</li>"
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
		int minS = 50;
		int maxS = 70;
		int minL = 20;
		int maxL = 50;
				
		//----------------------------------
		// MinS Value
		if(parameters.size() > 0) { 
			
			QueryPartValue minSValue = parameters.get(0);
			if(minSValue.isNumberOrNumberString()) {
				minS = minSValue.getAsInteger();
			}
			
			//----------------------------------
			// MaxS Value
			if(parameters.size() > 1) { 
				QueryPartValue maxSValue = parameters.get(1);
				if(maxSValue.isNumberOrNumberString()) {
					maxS = maxSValue.getAsInteger();
				}
				
				//----------------------------------
				// MinL Value
				if(parameters.size() > 2) { 
					QueryPartValue minLValue = parameters.get(2);
					if(minLValue.isNumberOrNumberString()) {
						minL = minLValue.getAsInteger();
					}
					
					//----------------------------------
					// MaxL Value
					if(parameters.size() > 3) { 
						QueryPartValue maxLValue = parameters.get(3);
						if(maxLValue.isNumberOrNumberString()) {
							maxL = maxLValue.getAsInteger();
						}
						
						
					}
				}
			}
		}
		

	
		//----------------------------------
		// Return Number
		return QueryPartValue.newString(
				CFW.Random.randomColorHSL(minS, maxS, minL, maxL)
			);
		
	}

}
