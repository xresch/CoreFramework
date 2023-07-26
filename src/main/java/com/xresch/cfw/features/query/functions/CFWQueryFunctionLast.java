package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
public class CFWQueryFunctionLast extends CFWQueryFunction {

	public static final String FUNCTION_NAME = "last";
	private QueryPartValue lastValue = QueryPartValue.newNull(); 
	private boolean isAggregated = false;
	
	public CFWQueryFunctionLast(CFWQueryContext context) {
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
		tags.add(CFWQueryFunction.TAG_AGGREGATION);
		return tags;
	}
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(valueOrFieldname, includeNulls)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Aggregation function to get the last value.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 
			 "<ul>"
			+"<li><b>valueOrFieldname:&nbsp;</b>The value or fieldname.</li>"
			+"<li><b>includeNulls:&nbsp;</b>(Optional) Specify if null values should be included.(Default: false)</li>"
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
		return true;
	}
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	private void addValueToAggregation(QueryPartValue value, boolean includeNulls) {
		if(!value.isNull() || includeNulls) {
			lastValue = value;
		}
	}
	

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void aggregate(EnhancedJsonObject object,ArrayList<QueryPartValue> parameters) {
		
		isAggregated = true;
		
		int paramCount = parameters.size();
		if(paramCount == 0) {
			return;
		}

		QueryPartValue value = parameters.get(0);
		boolean includeNulls = false;
		if(parameters.size() > 1) {
			includeNulls = parameters.get(1).getAsBoolean();
		}
		
		
		addValueToAggregation(value, includeNulls);

		
	}

	/***********************************************************************************************
	 * Returns the current count and increases it by 1;
	 ***********************************************************************************************/
	@Override
	public QueryPartValue execute(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters) {
		
		if(isAggregated) {			
			return lastValue;
		}else if(parameters.size() == 0) {
			return QueryPartValue.newNull();
		}else {
			
			QueryPartValue param = parameters.get(0);
			boolean includeNulls = false;
			if(parameters.size() > 1) {
				includeNulls = parameters.get(1).getAsBoolean();
			}
			
			if(param.isJsonArray()) {
				
				JsonArray array = param.getAsJsonArray();
				
				for(int i = array.size()-1; i >= 0; i--) {
					
					if(!array.get(i).isJsonNull() || includeNulls) {
						return QueryPartValue.newFromJsonElement(array.get(i));
					}
					
				}
				return QueryPartValue.newNull();
				
			}else if(param.isJsonObject()) {
				String lastMemberName = "";
				for(Entry<String, JsonElement> entry : param.getAsJsonObject().entrySet()){
					lastMemberName = entry.getKey();
				}
				return QueryPartValue.newString(lastMemberName);

			}else {
				// just return the value
				return param;
			}
			
		}
		
		//reset and return null in all other cases
		//return QueryPartValue.newNull();
	}

}
