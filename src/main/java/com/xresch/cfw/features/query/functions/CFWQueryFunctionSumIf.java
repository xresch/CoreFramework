package com.xresch.cfw.features.query.functions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Map.Entry;

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
public class CFWQueryFunctionSumIf extends CFWQueryFunction {

	public static final String FUNCTION_NAME = "sumif";

	private BigDecimal sum = new BigDecimal(0); 
	
	private boolean isAggregated = false;
	
	
	public CFWQueryFunctionSumIf(CFWQueryContext context) {
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
		return FUNCTION_NAME+"(valueOrFieldname, condition)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Aggregation function to create sum based on conditions.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<ul>"
				+"<li><b>valueOrFieldname:&nbsp;</b>The value or fieldname used to add to the sum.</li>"
				+"<li><b>condition:&nbsp;</b>(Optional)An expression, if evaluates to true, add the value to the sum. (default: true)</li>"
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
	private void addValueToAggregation(QueryPartValue value) {
		
		if(value.isNumberOrNumberString()) {
			sum = sum.add(value.getAsBigDecimal());
		}
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	private QueryPartValue getSum() {
		
		QueryPartValue result = QueryPartValue.newNumber(sum);
		
		//reset aggregation
		sum = new BigDecimal(0);
		
		return result;
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
		if(paramCount == 1) {
			addValueToAggregation(value);
			return;
		}
		
		boolean doAddToSum = parameters.get(1).getAsBoolean();
		if(doAddToSum) {
			addValueToAggregation(value);
		}
	}

	/***********************************************************************************************
	 * Returns the current count and increases it by 1;
	 ***********************************************************************************************/
	@Override
	public QueryPartValue execute(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters) {
		
		if(isAggregated) {			
			return getSum();
		}else if(parameters.size() == 0) {
			return QueryPartValue.newNull();
		}
		
		//reset and return null in all other cases
		sum = new BigDecimal(0);
		return QueryPartValue.newNull();
	}

}
