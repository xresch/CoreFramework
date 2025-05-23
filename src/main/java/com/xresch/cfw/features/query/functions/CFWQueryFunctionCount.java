package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.TreeSet;

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
public class CFWQueryFunctionCount extends CFWQueryFunction {

	public static final String FUNCTION_NAME = "count";
	
	private int count = 0; 
	private boolean isAggregated = false;
	
	public CFWQueryFunctionCount(CFWQueryContext context) {
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
		tags.add(_CFWQueryCommon.TAG_MATH);
		tags.add(_CFWQueryCommon.TAG_AGGREGATION);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(valueOrFieldname, countNulls)";
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
			 + "<p><b>countNulls:&nbsp;</b>(Optional)Toggle if null values and empty strings should be counted or not(Default:false).</p>"
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
	@Override
	public void aggregate(EnhancedJsonObject object,ArrayList<QueryPartValue> parameters) {
		
		isAggregated = true;
		
		int paramCount = parameters.size();
		if(paramCount == 0) {
			count++;
			return;
		}

		QueryPartValue value = parameters.get(0);
		boolean countNulls = false;
		if(paramCount > 1) {
			countNulls = parameters.get(1).getAsBoolean();
		}
		
		if(!value.isNullOrEmptyString()) {
			count++;
		}else if(countNulls) {
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
				result = QueryPartValue.newNumber(param.getAsJsonArray().size());
			}else if(param.isJsonObject()) {
				result = QueryPartValue.newNumber(param.getAsJsonObject().entrySet().size());
			}else if(param.isNull()) {
				result = QueryPartValue.newNull();
			}else {
				result = QueryPartValue.newNumber(1);
			}
		}

		count++;
		
		return result;
	}

}
