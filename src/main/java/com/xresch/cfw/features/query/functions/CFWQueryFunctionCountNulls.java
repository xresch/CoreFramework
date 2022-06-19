package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.QueryPartValue;

public class CFWQueryFunctionCountNulls extends CFWQueryFunction {
	
	private boolean isAggregated = false;
	
	private int count = 0; 
	
	public CFWQueryFunctionCountNulls(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return "countnulls";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "countnulls(valueOrFieldname)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Aggregation function to create counts.";
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
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_countnulls.html");
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
	@Override
	public void aggregate(EnhancedJsonObject object,ArrayList<QueryPartValue> parameters) {
		
		isAggregated = true;
		
		int paramCount = parameters.size();
		if(paramCount == 0) {
			count++;
			return;
		}

		QueryPartValue value = parameters.get(0);		
		if(value.isNull()) {
			count++;
		}
	
	}

	/***********************************************************************************************
	 * Returns the current count and increases it by 1;
	 ***********************************************************************************************/
	@Override
	public QueryPartValue execute(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters) {
		
		QueryPartValue result = QueryPartValue.newNull();
		
		if(isAggregated || parameters.size() == 0) {
			result = QueryPartValue.newNumber(count);
		}else if(parameters.size() > 0) {
			
			QueryPartValue param = parameters.get(0);
			
			if(param.isJsonArray()) {
				
				JsonArray array = param.getAsJsonArray();
				
				int nullCount = 0;
				for(int i = 0; i < array.size(); i++) {
					if(array.get(i).isJsonNull()) {
						nullCount++;
					}
				}
				result = QueryPartValue.newNumber(nullCount);
				
			}else {
				result = QueryPartValue.newNull();
			}
		}
		
		return result;
	}

}
