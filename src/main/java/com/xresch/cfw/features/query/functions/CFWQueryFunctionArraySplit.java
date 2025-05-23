package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.TreeSet;

import com.google.gson.JsonArray;
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
public class CFWQueryFunctionArraySplit extends CFWQueryFunction {

	
	public static final String FUNCTION_NAME = "arraySplit";
	

	public CFWQueryFunctionArraySplit(CFWQueryContext context) {
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
		return FUNCTION_NAME+"(arrayOrFieldname, chunkSize)";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Splits an array into chunks and returns a new array containing the chunked arrays.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<ul>"
			  +"<li><b>valueOrFieldname:&nbsp;</b>The array, value or fieldname that should be concatenated.</li>"
			  +"<li><b>chunkSize:&nbsp;</b>(Optional)The size of the chunk, default 10.</li>"
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
				// Get Chunk Size
				int chunkSize = 10;
				if(parameters.size() > 1){
					QueryPartValue chunkParam = parameters.get(1);
					
					if(chunkParam.isNumberOrNumberString()) {
						chunkSize = chunkParam.getAsInteger();
					}
				}
				
				//----------------------------
				// Chunkify
				JsonArray original = firstParam.getAsJsonArray();
				
				for(int i = 0; i < original.size(); ) {
					JsonArray chunk = new JsonArray();
					
					int chunkEndIndex = Math.min(original.size(), i + chunkSize);
					int k = i;
					for(; k < chunkEndIndex; k++) {
						chunk.add(original.get(k));
					}
					i = k;
					
					result.add(chunk);
				}
			}
			
			
			
		}
		
		//----------------------------------
		// Return result 
		return QueryPartValue.newFromJsonElement(result);
		
	}
}
