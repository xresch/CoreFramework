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
public class CFWQueryFunctionDistinct extends CFWQueryFunction {

	public static final String FUNCTION_NAME = "distinct";

	private LinkedHashSet<QueryPartValue> distinctValues = new LinkedHashSet<>(); 
	private boolean isAggregated = false;
	
	public CFWQueryFunctionDistinct(CFWQueryContext context) {
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
		return "Aggregation function to create an array of distinct values.";
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
			+"<li><b>doSort:&nbsp;</b>(Optional) Specify if the resulting array should be sorted.(Default: true)</li>"
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
			distinctValues.add(value);
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
			boolean includeNulls = false;
			if(parameters.size() > 1) {
				includeNulls = parameters.get(1).getAsBoolean();
			}
			
			if(param.isJsonArray()) {
				
				JsonArray array = param.getAsJsonArray();
				
				for(int i = 0; i < array.size(); i++) {
					JsonElement current = array.get(i);
					if(!current.isJsonNull() || includeNulls) {
						distinctValues.add( QueryPartValue.newFromJsonElement(current) );
					}
				}
				
			}else if(param.isJsonObject()) {
				for(Entry<String, JsonElement> entry : param.getAsJsonObject().entrySet()){
					distinctValues.add( 
						QueryPartValue.newFromJsonElement( entry.getValue() )
					);
				}

			}else {
				// just return the value
				return param;
			}
			
		}
		
		//-------------------------------
		// Do Sort
		Set<QueryPartValue> finalSet = distinctValues;
		boolean doSort = true;
		if(parameters.size() >= 3 ) {
			 doSort = parameters.get(2).getAsBoolean();
		}
		
		if(doSort) {
			finalSet = new TreeSet<>();
			finalSet.addAll(distinctValues);
		}
		
		//-------------------------------
		// Create Array and Return
		JsonArray array = new JsonArray();
		
		for(QueryPartValue value : finalSet) {
			array.add(value.getAsJsonElement());
		}
		distinctValues = new LinkedHashSet<>(); // reset values
		return QueryPartValue.newFromJsonElement(array);

	}

}
