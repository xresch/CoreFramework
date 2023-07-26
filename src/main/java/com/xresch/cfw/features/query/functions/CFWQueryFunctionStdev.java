package com.xresch.cfw.features.query.functions;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class CFWQueryFunctionStdev extends CFWQueryFunction {

	public static final String FUNCTION_NAME = "stdev";
	private ArrayList<BigDecimal> values = new ArrayList<BigDecimal>(); 
	private BigDecimal sum = new BigDecimal(0); 
	
	private boolean isAggregated = false;
	private int precision = 3;
	
	public CFWQueryFunctionStdev(CFWQueryContext context) {
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
		return FUNCTION_NAME+"(valueOrFieldname, includeNulls, precision)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Aggregation function to calculate standard deviation.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>valueOrFieldname:&nbsp;</b>The value or fieldname used for the average.</p>"
			 + "<p><b>includeNulls:&nbsp;</b>(Optional)Toggle if null values should be included in the average(Default:false).</p>"
			 + "<p><b>precision:&nbsp;</b>(Optional)Decimal precision of the result.(Default:3)</p>"
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
	private void addValueToAggregation(QueryPartValue value, boolean countNulls) {
		
		if(value.isNumberOrNumberString()) {
			values.add(value.getAsBigDecimal());
			sum = sum.add(value.getAsBigDecimal());
		}else if(countNulls && value.isNull()) {
			values.add(BigDecimal.ZERO);
		}
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	private QueryPartValue calculateStandardDeviation() {
		
		if(values.size() == 0) {
			return QueryPartValue.newNumber(0);
		}
	
//		How to calculate standard deviation:
//		Step 1: Find the mean/average.
//		Step 2: For each data point, find the square of its distance to the mean.
//		Step 3: Sum the values from Step 2.
//		Step 4: Divide by the number of data points.
//		Step 5: Take the square root.
		
		//-----------------------------------------
		// STEP 1: Find Average
		BigDecimal count = new BigDecimal(values.size());
		BigDecimal average = sum.divide(count, RoundingMode.HALF_UP);
		
		BigDecimal sumDistanceSquared = BigDecimal.ZERO;
		for(BigDecimal value : values) {
			//-----------------------------------------
			// STEP 2: For each data point, find the 
			// square of its distance to the mean.
			BigDecimal distance = value.subtract(average).abs();
			//-----------------------------------------
			// STEP 3: Sum the values from Step 2.
			sumDistanceSquared = sumDistanceSquared.add(distance.pow(2));
		}
		
		//-----------------------------------------
		// STEP 4 & 5: Divide and take square root
		BigDecimal divided = sumDistanceSquared.divide(count, RoundingMode.HALF_UP);
		
		// TODO JDK8 Migration: should work with JDK 9
		// MathContext mc = new MathContext(6, RoundingMode.HALF_UP);
		// BigDecimal standardDeviation = divided.sqrt(mc);
		BigDecimal standardDeviation = 
				new BigDecimal(Math.sqrt(divided.doubleValue()))
				.setScale(precision, RoundingMode.HALF_UP);
		
		//reset values when calculation is done
		values.clear();
		sum = new BigDecimal(0);
		return QueryPartValue.newNumber(standardDeviation);
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void aggregate(EnhancedJsonObject object,ArrayList<QueryPartValue> parameters) {
		
		isAggregated = true;
		
		QueryPartValue value = parameters.get(0);
		boolean countNulls = false;
		if(parameters.size() > 1) {
			countNulls = parameters.get(1).getAsBoolean();
		}
		
		if(parameters.size() > 2
		&& parameters.get(2).isNumberOrNumberString()) {
			precision = parameters.get(2).getAsInteger();
		}
		
		addValueToAggregation(value, countNulls);
	
	}
	

	/***********************************************************************************************
	 * Returns the current count and increases it by 1;
	 ***********************************************************************************************/
	@Override
	public QueryPartValue execute(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters) {
		
		if(isAggregated) {			
			return calculateStandardDeviation();
		}else if(parameters.size() == 0) {
			return QueryPartValue.newNull();
		}else {
			
			QueryPartValue param = parameters.get(0);
			boolean countNulls = false;
			if(parameters.size() > 1) {
				countNulls = parameters.get(1).getAsBoolean();
			}

			if(param.isJsonArray()) {
				
				JsonArray array = param.getAsJsonArray();
				
				for(int i = 0; i < array.size(); i++) {
					
					//Be lazy, use QueryPart for conversion
					QueryPartValue value = QueryPartValue.newFromJsonElement(array.get(i));
					addValueToAggregation(value, countNulls);
				}
				return calculateStandardDeviation();
				
			}else if(param.isJsonObject()) {
				
				for(Entry<String, JsonElement> entry : param.getAsJsonObject().entrySet()){
					QueryPartValue value = QueryPartValue.newFromJsonElement(entry.getValue());
					addValueToAggregation(value, countNulls);
				}
				return calculateStandardDeviation();
			}else if(param.isNumberOrNumberString()) {
				
				addValueToAggregation(param, countNulls);
				return calculateStandardDeviation();
			}
			
		}
		
		//return null in all other cases
		return QueryPartValue.newNull();
	}
}
