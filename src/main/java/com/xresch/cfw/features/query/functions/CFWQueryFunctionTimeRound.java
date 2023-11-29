package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.TreeSet;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.utils.CFWTime.CFWTimeUnit;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionTimeRound extends CFWQueryFunction {

	
	public static final String FUNCTION_NAME = "timeround";

	public CFWQueryFunctionTimeRound(CFWQueryContext context) {
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
		tags.add(CFWQueryFunction.TAG_TIME);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(timeInMillis, amount, unit)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Takes epoch milliseconds and rounds it by the specified amount and unit. ";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<ul>"
				  +"<li><b>timeInMillis:&nbsp;</b>(Optional) The time in epoch milliseconds. If null, current earliest time is used.</li>"
				  +"<li><b>amount:&nbsp;</b>(Optional) The amount to offset from present time.(Default: 1)</li>"
				  +"<li><b>unit:&nbsp;</b>(Optional) The unit used to offset the time. One of the following(Default: 'm'):"
				  + CFWTimeUnit.getOptionsHTMLList()
				  + "</li>"
			  + "</ul>"
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
	public QueryPartValue execute(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters) {
		
		//----------------------------------
		// Default Params
		Long epochMillis = null;
		int amount = 1;
		String unit = "m";
	
		//----------------------------------
		// Get Parameters
		int size = parameters.size(); 
		int index = 0;
		if(size >index) {
			
			QueryPartValue millisValue = parameters.get(index);
			if(millisValue.isNumberOrNumberString()) { epochMillis = millisValue.getAsLong(); };
			index++;

			//----------------------------------
			// Amount
			if(size > index) {
				QueryPartValue offsetAmmountValue = parameters.get(index);
				if(offsetAmmountValue.isNumberOrNumberString()) { amount = offsetAmmountValue.getAsInteger(); };
				index++;
				//----------------------------------
				// Unit
				if(size > index) {
					QueryPartValue offsetUnitValue = parameters.get(index);
					if(offsetUnitValue.isString()) { unit = offsetUnitValue.getAsString(); };
					index++;
				}
			}
		}
			
		//----------------------------------
		// Create Time and Format
		if(epochMillis == null) {
			epochMillis = this.context.getEarliestMillis();
		}

		//----------------------------------
		// Offset an set Earliest
		if(amount != 0 && CFWTimeUnit.has(unit)) {
			epochMillis = CFWTimeUnit
							.valueOf(unit)
							.round(epochMillis, amount);
		}
		
		return QueryPartValue.newNumber(epochMillis);

				
	}

}
