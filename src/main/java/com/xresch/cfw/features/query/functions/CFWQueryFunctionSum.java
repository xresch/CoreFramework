package com.xresch.cfw.features.query.functions;

import java.math.BigDecimal;
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

public class CFWQueryFunctionSum extends CFWQueryFunction {

	private BigDecimal sum = new BigDecimal(0); 
	
	private boolean isAggregated = false;
	
	
	public CFWQueryFunctionSum(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return "sum";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "sum(valueOrFieldname)";
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
		return "<p><b>valueOrFieldname:&nbsp;</b>(Optional)The value or fieldname used for the count.</p>"
			;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_sum.html");
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
		
		addValueToAggregation(value);
		
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
		}else {
			
			QueryPartValue param = parameters.get(0);

			if(param.isJsonArray()) {
				
				JsonArray array = param.getAsJsonArray();
				
				for(int i = 0; i < array.size(); i++) {
					
					//Be lazy, use QueryPart for conversion
					QueryPartValue value = QueryPartValue.newFromJsonElement(array.get(i));
					addValueToAggregation(value);
				}
				return getSum();
				
			}else if(param.isJsonObject()) {
				
				for(Entry<String, JsonElement> entry : param.getAsJsonObject().entrySet()){
					QueryPartValue value = QueryPartValue.newFromJsonElement(entry.getValue());
					addValueToAggregation(value);
				}
				return getSum();
			}
			
			
		}
		
		//reset and return null in all other cases
		sum = new BigDecimal(0);
		return QueryPartValue.newNull();
	}

}
