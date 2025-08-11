package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
public class CFWQueryFunctionArrayDedup extends CFWQueryFunction {

	
	public static final String FUNCTION_NAME = "arrayDedup";
	

	public CFWQueryFunctionArrayDedup(CFWQueryContext context) {
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
		tags.add(_CFWQueryCommon.TAG_ARRAYS);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(arrayOrFieldname)";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Creates and returns a new array with the values being deduplicated.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<ul>"
			  +"<li><b>valueOrFieldname:&nbsp;</b>The array that should be deduplicated.</li>"
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
		
		JsonArray result = new JsonArray();
		

		//----------------------------------
		// Concat Arrays
		if(parameters.size() > 0) { 
			
			//----------------------------
			// Get First Param
			QueryPartValue firstParam = parameters.get(0);
			
			if( ! firstParam.isJsonArray()) {
				return firstParam;
			} else {
				
				
				//----------------------------
				// Reverse Array
				JsonArray original = firstParam.getAsJsonArray();

				HashSet<String> encounters = new HashSet<>();
				for(int i = 0; i < original.size(); i++ ) {
					JsonElement element = original.get(i);
					String stringified = element.toString(); // check if value was already encountered, strings will be in "", so 3 and "3" will not be the same
					
					if( ! encounters.contains(stringified) ) {
						encounters.add(stringified);
						result.add(element);
					}
										
				}
			}
			
			
			
		}
		
		//----------------------------------
		// Return result 
		return QueryPartValue.newFromJsonElement(result);
		
	}
}
