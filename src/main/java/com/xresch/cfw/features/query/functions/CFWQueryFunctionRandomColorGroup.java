package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.bouncycastle.util.test.TestRandomBigInteger;

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
public class CFWQueryFunctionRandomColorGroup extends CFWQueryFunction {

	
	private static final String FUNCTION_NAME = "randomColorGroup";

	// contains group and color
	private HashMap<String, QueryPartValue> groupColorsCache = new HashMap<>();
	
	public CFWQueryFunctionRandomColorGroup(CFWQueryContext context) {
		super(context);
	}

	int lastHueValue = 0;
	
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
		tags.add(_CFWQueryCommon.TAG_FORMAT);
		return tags;
	}
		
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(group, minS, maxS, minL, maxL)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns a HSL CSS color string for a group.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 
				"<ul>"
					+"<li><b>group:&nbsp;</b>(Optional)The name of the group.</li>"
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
		String group = null;
		int minS = 50;
		int maxS = 70;
		int minL = 30;
		int maxL = 50;
			
		//----------------------------------
		// Group Value
		if(parameters.size() > 0) { 
			
			QueryPartValue groupValue = parameters.get(0);
			group = groupValue.getAsString();
			
			if(groupColorsCache.containsKey(group)) {
				return groupColorsCache.get(group);
			}
			
			//----------------------------------
			// MinS Value
			if(parameters.size() > 1) { 
				
				QueryPartValue minSValue = parameters.get(1);
				if(minSValue.isNumberOrNumberString()) {
					minS = minSValue.getAsInteger();
				}
				
				//----------------------------------
				// MaxS Value
				if(parameters.size() > 2) { 
					QueryPartValue maxSValue = parameters.get(2);
					if(maxSValue.isNumberOrNumberString()) {
						maxS = maxSValue.getAsInteger();
					}
					
					//----------------------------------
					// MinL Value
					if(parameters.size() > 3) { 
						QueryPartValue minLValue = parameters.get(3);
						if(minLValue.isNumberOrNumberString()) {
							minL = minLValue.getAsInteger();
						}
						
						//----------------------------------
						// MaxL Value
						if(parameters.size() > 4) { 
							QueryPartValue maxLValue = parameters.get(4);
							if(maxLValue.isNumberOrNumberString()) {
								maxL = maxLValue.getAsInteger();
							}
							
							
						}
					}
				}
			}
		}
		
		//----------------------------------
		// Return Number
		if(group == null) {
			 return QueryPartValue.newString(
					CFW.Random.colorHSL(minS, maxS, minL, maxL)
				);
		}
		
		//----------------------------------
		// Create Hue

		int hueValue = group.hashCode() % 360;
		
		// make sure there is enough difference
		if( Math.abs(lastHueValue - hueValue) < 50) {
			if(lastHueValue < hueValue) {
				hueValue += 50 - Math.abs(lastHueValue - hueValue) + CFW.Random.integer(-10, +10);
			}else {
				hueValue -= 50 - Math.abs(lastHueValue - hueValue) + CFW.Random.integer(-10, +10);;
			}
		}
		
		lastHueValue = hueValue;
		
		//----------------------------------
		// Create Color
		QueryPartValue color =  QueryPartValue.newString(
				CFW.Random.colorSL(hueValue, minS, maxS, minL, maxL)
			);
		
		groupColorsCache.put(group, color);
		
		return color;
	
	}

}
