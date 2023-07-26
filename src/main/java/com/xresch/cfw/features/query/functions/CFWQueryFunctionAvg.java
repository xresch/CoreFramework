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
public class CFWQueryFunctionAvg extends CFWQueryFunction {

	private int count = 0; 
	private BigDecimal sum = new BigDecimal(0); 
	
	private boolean isAggregated = false;
	private int precision = 3;
	
	public CFWQueryFunctionAvg(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return "avg";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public TreeSet<String> getTags(){
		TreeSet<String> tags = new TreeSet<>();
		tags.add(CFWQueryFunction.TAG_MATH);
		tags.add(CFWQueryFunction.TAG_AGGREGATION);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "avg(valueOrFieldname, includeNulls, precision)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Aggregation function to create average.";
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
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_avg.html");
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
			count++;
			sum = sum.add(value.getAsBigDecimal());
		}else if(countNulls && value.isNull()) {
			count++;
			// sum + 0
		}
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	private QueryPartValue calculateAverage() {
		
		if(count == 0) {
			return QueryPartValue.newNumber(0);
		}
		
		BigDecimal average = sum.divide(new BigDecimal(count), precision, RoundingMode.HALF_UP);
		
		//reset values when calculation is done
		count = 0;
		sum = new BigDecimal(0);
		return QueryPartValue.newNumber(average);
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
			return calculateAverage();
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
				return calculateAverage();
				
			}else if(param.isJsonObject()) {
				
				for(Entry<String, JsonElement> entry : param.getAsJsonObject().entrySet()){
					QueryPartValue value = QueryPartValue.newFromJsonElement(entry.getValue());
					addValueToAggregation(value, countNulls);
				}
				return calculateAverage();
			}else if(param.isNumberOrNumberString()) {
				
				addValueToAggregation(param, countNulls);
				return calculateAverage();
			}
			
			
		}
		
		//return null in all other cases
		return QueryPartValue.newNull();
	}
}
