package com.xresch.cfw.features.query.functions;

import java.math.BigDecimal;
import java.math.MathContext;
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
import com.xresch.cfw.features.query._CFWQueryCommon;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.utils.CFWMath;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionStdev extends CFWQueryFunction {

	public static final String FUNCTION_NAME = "stdev";
	private ArrayList<BigDecimal> values = new ArrayList<BigDecimal>(); 
	private BigDecimal sum = new BigDecimal(0).setScale(6); 
	
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
		tags.add(_CFWQueryCommon.TAG_AGGREGATION);
		return tags;
	}
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(valueOrFieldname, includeNulls, precision, usePopulation)";
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
			 + "<p><b>includeNulls:&nbsp;</b>(Optional)Toggle if null values should be included as zero in the average(Default:false).</p>"
			 + "<p><b>precision:&nbsp;</b>(Optional)Decimal precision of the result.(Default:3)</p>"
			 + "<p><b>usePopulation:&nbsp;</b>(Optional)If true use population formula, if false use sample formula.(Default: true)</p>"
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
	private BigDecimal calculateStandardDeviation(boolean usePopulation) {
		
		BigDecimal standardDeviation = CFW.Math.bigStdev(values, usePopulation, CFW.Math.GLOBAL_SCALE);
		
		//reset values when calculation is done
		values.clear();
		sum = BigDecimal.ZERO.setScale(6);
		return standardDeviation;
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
				
		addValueToAggregation(value, countNulls);
	
	}
	

	/***********************************************************************************************
	 * Returns the current count and increases it by 1;
	 ***********************************************************************************************/
	@Override
	public QueryPartValue execute(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters) {
		
		//----------------------------
		// Handle usePopulation
		boolean usePopulation = true;
		if(parameters.size() > 3
		&& parameters.get(3).isBoolOrBoolString()) {
			usePopulation = parameters.get(3).getAsBoolean();
		}
		
		
		BigDecimal result = BigDecimal.ZERO;
		if(isAggregated) {			
			result = calculateStandardDeviation(usePopulation);
		}else if(parameters.size() == 0) {
			result = null;
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
				result = calculateStandardDeviation(usePopulation);
				
			}else if(param.isJsonObject()) {
				
				for(Entry<String, JsonElement> entry : param.getAsJsonObject().entrySet()){
					QueryPartValue value = QueryPartValue.newFromJsonElement(entry.getValue());
					addValueToAggregation(value, countNulls);
				}
				result = calculateStandardDeviation(usePopulation);
			}else if(param.isNumberOrNumberString()) {
				
				addValueToAggregation(param, countNulls);
				result = calculateStandardDeviation(usePopulation);
			}
			
		}
		
		//----------------------------
		// Handle Precision
		if(parameters.size() > 2
		&& parameters.get(2).isNumberOrNumberString()) {
			precision = parameters.get(2).getAsInteger();
		}
		
		//----------------------------
		// Return Value
		if(result == null) {
			return QueryPartValue.newNull();
		}else {
			return QueryPartValue.newNumber(result.setScale(precision, RoundingMode.HALF_UP));
		}
	}
}
