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
public class CFWQueryFunctionArrayStringify extends CFWQueryFunction {

	
	public static final String FUNCTION_NAME = "arrayStringify";
	

	public CFWQueryFunctionArrayStringify(CFWQueryContext context) {
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
		tags.add(_CFWQueryCommon.TAG_CODING);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(arrayOrFieldname, separator, quoteCharacter)";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Converts an array into a string by joining the string values together with a separator.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<ul>"
			  +"<li><b>valueOrFieldname:&nbsp;</b>The array, value or fieldname that should be concatenated.</li>"
			  +"<li><b>separator:&nbsp;</b>(Optional)The separator, default comma.</li>"
			  +"<li><b>quoteCharacter:&nbsp;</b>(Optional)The character used to quote the string, default double quotes.</li>"
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
		
		StringBuilder result = new StringBuilder();
		
		String separator = ",";
		String quoteCharacter = "\"";
		
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
				// Get Separator
				if(parameters.size() > 1){
					QueryPartValue separatorParam = parameters.get(1);
					separator = separatorParam.getAsString();

					//----------------------------
					// Get Quote Character
					if(parameters.size() > 2){
						QueryPartValue quoteParam = parameters.get(2);
						quoteCharacter = quoteParam.getAsString();
					}
					
				}
				
				//----------------------------
				// Chunkify
				JsonArray array = firstParam.getAsJsonArray();
				
				for(int i = 0; i < array.size(); i++) {
					
					// Using this to get string values without the quotes you would get from JsonElement.toString()
					QueryPartValue value = QueryPartValue.newFromJsonElement(array.get(i));
					result
						.append(quoteCharacter)
						.append(value.getAsString())
						.append(quoteCharacter)
						.append(separator)
						;
				}
				
				// remove last comma
				result.deleteCharAt(result.length()-1);
			}
			
			
			
		}
		
		//----------------------------------
		// Return result 
		return QueryPartValue.newString(result.toString());
		
	}
}
