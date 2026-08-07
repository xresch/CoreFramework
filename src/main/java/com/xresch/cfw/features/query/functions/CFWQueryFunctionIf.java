package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.TreeSet;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query._CFWQueryCommon;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartValue;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionIf extends CFWQueryFunction {

	
	public static final String FUNCTION_NAME = "if";

	public CFWQueryFunctionIf(CFWQueryContext context) {
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
		tags.add(_CFWQueryCommon.TAG_CODING);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(condition, trueValue, falseValue)";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Evaluates the condition, returns the respective value for true or false.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<ul>"
			  +"<li><b>condition:&nbsp;</b>The condition to evaluate for the if-statement.</li>"
			  +"<li><b>trueValue:&nbsp;</b>The value to return if the condition is true.</li>"
			  +"<li><b>falseValue:&nbsp;</b>The value to return if the condition is false.</li>"
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
		return false;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void aggregate(EnhancedJsonObject object,ArrayList<QueryPartValue> parameters) {
		// not supported
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public boolean doPreEvaluate() {
		return false;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public QueryPartValue execute(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters, ArrayList<QueryPart> unevalParams) {

		//----------------------------------
		// Return same value if not second param
		if(unevalParams.size() >= 2) { 
			
			//----------------------------
			// Condition
			QueryPart condition = unevalParams.get(0); 

			if(condition instanceof QueryPartValue) {
				condition = ((QueryPartValue)condition).convertFieldnameToFieldvalue(object);
			}

			//----------------------------
			// True False Parts
			QueryPart trueValue = unevalParams.get(1); 
			QueryPart falseValue = (unevalParams.size() >= 3) ? unevalParams.get(2) : QueryPartValue.newString(""); 
			
			if(condition.determineValue(object).getAsBoolean()) {
				
				//---------------------
				// Return True Value
				if(trueValue instanceof QueryPartValue) {
					trueValue = trueValue.convertFieldnameToFieldvalue(object);
				}
				return trueValue.determineValue(object);
			}else {
				//---------------------
				// Return False Value
				if(falseValue instanceof QueryPartValue) {
					falseValue = falseValue.convertFieldnameToFieldvalue(object);
				}
				return falseValue.determineValue(object);
			}
		}
		
		//----------------------------------
		// Return empty string if not enough params
		return QueryPartValue.newNull();
		
	}

}
