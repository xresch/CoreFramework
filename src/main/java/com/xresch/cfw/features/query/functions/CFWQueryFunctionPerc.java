package com.xresch.cfw.features.query.functions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.QueryPartValue;

public class CFWQueryFunctionPerc extends CFWQueryFunction {

	protected ArrayList<BigDecimal> values = new ArrayList<>();
	
	protected Integer percentile = null;
	private boolean isAggregated = false;
	
	public CFWQueryFunctionPerc(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return "perc";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "perc(valueOrFieldname, percentile, includeNulls)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Aggregation function to calculate percentile values.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>valueOrFieldname:&nbsp;</b>The value or fieldname used for the count.</p>"
			 + "<p><b>percentile:&nbsp;</b>(Optional)Value from 0 to 100 to define which percentile to calculate(Default:50).</p>"
			 + "<p><b>includeNulls:&nbsp;</b>(Optional)Toggle if null values should be included in the percentile calculation(Default:false).</p>"
			;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_perc.html");
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
		
		//---------------------------------
		// Store values
		if(value.isNumberOrNumberString()) {
			values.add(value.getAsBigDecimal());
		}else if(countNulls && value.isNull()) {
			values.add(new BigDecimal(0));
		}
	}
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	private QueryPartValue calculatePercentile() {
		
		int count = values.size();
		
		if(count == 0) {
			return QueryPartValue.newNull();
		}
		
		if(percentile == null) {
			percentile = 50;
		}
		
		int percentilePosition = (int)Math.ceil( count * (percentile / 100f) );
		
		//---------------------------
		// Retrieve number
		values.sort(null);
		System.out.println("values:"+values);
		
		QueryPartValue result;
		if(percentilePosition > 0) {
			result = QueryPartValue.newNumber(values.get(percentilePosition-1));
		}else {
			result = QueryPartValue.newNumber(values.get(0));
				
		}
		System.out.println("result:"+result);
		//reset values
		values = new ArrayList<>();
		percentile = null;
		
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
		
		//---------------------------------
		// Resolve Percentile
		resolvePercentile(parameters);
		
		//---------------------------------
		// Resolve countNulls
		boolean countNulls = false;
		if(paramCount > 2) {
			countNulls = parameters.get(2).getAsBoolean();
		}
				
		addValueToAggregation(value, countNulls);
	
	}

	/***********************************************************************************************
	 * If percentile was not yet resolved, get it from parameters.
	 ***********************************************************************************************/
	private void resolvePercentile(ArrayList<QueryPartValue> parameters) {
		//---------------------------------
		// Resolve Percentile
		if(percentile == null && parameters.size() > 1) {
			if(parameters.get(1).isNumberOrNumberString()) {
				percentile = parameters.get(1).getAsInteger();
				if(percentile < 0) {
					percentile = 0;
				}else if(percentile > 100) {
					percentile = 100;
				}
			}
		}
	}
	

	/***********************************************************************************************
	 * Returns the current count and increases it by 1;
	 ***********************************************************************************************/
	@Override
	public QueryPartValue execute(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters) {
		
	
		if(isAggregated) {			
			return calculatePercentile();
		}else if(parameters.size() == 0) {
			return QueryPartValue.newNull();
		}else {
			
			QueryPartValue param = parameters.get(0);
			resolvePercentile(parameters);
			
			boolean countNulls = false;
			if(parameters.size() > 2) {
				countNulls = parameters.get(2).getAsBoolean();
			}

			if(param.isJsonArray()) {
				
				JsonArray array = param.getAsJsonArray();
				
				for(int i = 0; i < array.size(); i++) {
					
					//Be lazy, use QueryPart for conversion
					QueryPartValue value = QueryPartValue.newFromJsonElement(array.get(i));
					addValueToAggregation(value, countNulls);
				}
				return calculatePercentile();
				
			}else if(param.isJsonObject()) {
				
				for(Entry<String, JsonElement> entry : param.getAsJsonObject().entrySet()){
					QueryPartValue value = QueryPartValue.newFromJsonElement(entry.getValue());
					addValueToAggregation(value, countNulls);
				}
				return calculatePercentile();
			}else if(param.isNumberOrNumberString()) {
				
				addValueToAggregation(param, countNulls);
				return calculatePercentile();
			}
			
		}
		
		//return null in all other cases
		return QueryPartValue.newNull();
	}

}
