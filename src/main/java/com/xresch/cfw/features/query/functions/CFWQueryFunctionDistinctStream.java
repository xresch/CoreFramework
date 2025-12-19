package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
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
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionDistinctStream extends CFWQueryFunction {

	public static final String FUNCTION_NAME = "distinctStream";

	private ArrayList<QueryPartValue> distinctValues = new ArrayList<>(); 
	private boolean isAggregated = false;
	
	public CFWQueryFunctionDistinctStream(CFWQueryContext context) {
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
		tags.add(_CFWQueryCommon.TAG_AGGREGATION);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(valueOrFieldname, includeNulls, doSort)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Aggregation function to create an array of values, where successively equal values are reduced to a single entry.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 
			 "<ul>"
			+"<li><b>valueOrFieldname:&nbsp;</b>The value or fieldname.</li>"
			+"<li><b>includeNulls:&nbsp;</b>(Optional) Specify if null values should be included.(Default: true)</li>"
			+"<li><b>doSort:&nbsp;</b>(Optional) Specify if the resulting array should be sorted.(Default: false)</li>"
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
			
			if(!distinctValues.isEmpty()) { 
				
				QueryPartValue lastValue = distinctValues.get(distinctValues.size()-1);
				if( ! lastValue.equals(value) ) {
					distinctValues.add(value);
				}
				
			}else {
				distinctValues.add(value);
			}
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
		boolean includeNulls = true;
		if(parameters.size() > 1) {
			includeNulls = parameters.get(1).getAsBoolean();
		}
		
		
		addValueToAggregation(value, includeNulls);

		
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public QueryPartValue execute(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters) {
				
		//-------------------------------
		// Do Not Aggregated
		if(!isAggregated) {
	
			if(parameters.size() == 0) {
				return QueryPartValue.newNull();
			}
			
			QueryPartValue param = parameters.get(0);
			boolean includeNulls = true;
			if(parameters.size() > 1) {
				includeNulls = parameters.get(1).getAsBoolean();
			}
			
			if(param.isJsonArray()) {
				
				JsonArray array = param.getAsJsonArray();
				
				for(int i = 0; i < array.size(); i++) {
					JsonElement current = array.get(i);
					addValueToAggregation(QueryPartValue.newFromJsonElement(current), includeNulls);
				}
				
			}else if(param.isJsonObject()) {
				for(Entry<String, JsonElement> entry : param.getAsJsonObject().entrySet()){
					JsonElement current = entry.getValue();
					addValueToAggregation(QueryPartValue.newFromJsonElement(current), includeNulls);
				}

			}else {
				// just return the value
				return param;
			}
			
		}
		
		//-------------------------------
		// Do Sort
		ArrayList<QueryPartValue> finalSet = distinctValues;
		boolean doSort = false;
		if(parameters.size() >= 3 ) {
			 doSort = parameters.get(2).getAsBoolean();
		}
		
		if(doSort) {
			distinctValues.sort(null);
		}
		
		//-------------------------------
		// Create Array and Return
		JsonArray array = new JsonArray();
		
		for(QueryPartValue value : finalSet) {
			array.add(value.getAsJsonElement());
		}
		distinctValues = new ArrayList<>(); // reset values
		return QueryPartValue.newFromJsonElement(array);

	}

}
