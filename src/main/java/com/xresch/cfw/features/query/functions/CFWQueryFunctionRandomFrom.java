package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.TreeSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
public class CFWQueryFunctionRandomFrom extends CFWQueryFunction {

	
	public CFWQueryFunctionRandomFrom(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return "randomFrom";
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
		return "randomFrom(arrayOrObject, nullPercentage)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns a random item from an array or object.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 
				"<ul>"
					+"<li><b>arrayOrObject:&nbsp;</b>An array or object with values to be picked.</li>"
					+"<li><b>nullPercentage:&nbsp;</b>Percentage of null values, define an integer between 0 and 100 (Default: 0).</li>"
				+ "</ul>"
			;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_randomfrom.html");
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
		
		if(parameters.size() == 0) { 
			return QueryPartValue.newNull();
		}
		//----------------------------------
		// Initialize
		int nullPercentage = 0;
		
		//----------------------------------
		// Min Value
		QueryPartValue listValue = null;
		if(parameters.size() >= 1) { 
			
			listValue = parameters.get(0);
			if(!listValue.isJsonArray() && !listValue.isJsonObject()) {
				return listValue;
			}
			
			//----------------------------------
			// Null Percentage
			
			if(parameters.size() >= 2) { 
				QueryPartValue percentageValue = parameters.get(1);
				if(percentageValue.isNumberOrNumberString()) {
					nullPercentage = percentageValue.getAsInteger();
				}
			}
		}
		

	
		//----------------------------------
		// Check Return Null
		if(CFW.Random.checkReturnNull(nullPercentage)) { return QueryPartValue.newNull(); };
		
		//----------------------------------
		// Check Return Null
		if(listValue != null && listValue.isJsonArray()) {
			JsonArray array = listValue.getAsJsonArray();
			
			int randomIndex = CFW.Random.randomFromZeroToInteger(array.size()-1);
			return QueryPartValue.newFromJsonElement(array.get(randomIndex));
			
		}else if(listValue.isJsonObject()) {
			JsonObject json = listValue.getAsJsonObject();
			String[] memberNames = json.keySet().toArray(new String[] {});
			
			String random = CFW.Random.randomFromArray(memberNames);
			return QueryPartValue.newString(random);
			
		}
		
		//Should not be reached
		return QueryPartValue.newNull();
		

		
	}

}
