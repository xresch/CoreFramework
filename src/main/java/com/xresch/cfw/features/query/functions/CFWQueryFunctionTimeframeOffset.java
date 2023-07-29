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
public class CFWQueryFunctionTimeframeOffset extends CFWQueryFunction {

	
	public static final String FUNCTION_NAME = "timeframeOffset";

	public CFWQueryFunctionTimeframeOffset(CFWQueryContext context) {
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
		return FUNCTION_NAME+"(shiftMultiplier, offsetAmount, offsetUnit)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Offsets the earliest and latest time by the specified amount.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<ul>"
				  +"<li><b>shiftMultiplier:&nbsp;</b>(Optional)Shifts the timeframe N-times by this multiplier(Positive Values: into future, Negative Values: into past).</li>"
				  +"<li><b>offsetAmount:&nbsp;</b>(Optional) The amount to offset from present time.(Default: 0)</li>"
				  +"<li><b>offsetUnit:&nbsp;</b>(Optional) The unit used to offset the time. One of the following(Default: 'm'):"
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
		int shiftMultiplier = 0;
		int offsetAmount = 0;
		String offsetUnit = "m";
			
		//----------------------------------
		// Get Parameters
		int size = parameters.size(); 
		int index = 0;
		if(size >index) {
			
			QueryPartValue shiftMultiplierValue = parameters.get(index);
			if(shiftMultiplierValue.isNumberOrNumberString()) {
				shiftMultiplier = shiftMultiplierValue.getAsInteger(); 
			};
			index++;

			//----------------------------------
			// Offset Amount
			if(size > index) {
				QueryPartValue offsetAmmountValue = parameters.get(index);
				if(offsetAmmountValue.isNumberOrNumberString()) { offsetAmount = offsetAmmountValue.getAsInteger(); };
				index++;
				//----------------------------------
				// offset Unit
				if(size > index) {
					QueryPartValue offsetUnitValue = parameters.get(index);
					if(offsetUnitValue.isString()) { offsetUnit = offsetUnitValue.getAsString(); };
					index++;
				}
			}
		}
		
		//----------------------------------
		// Create Time and Format
		long currentEarliest = this.context.getEarliestMillis();
		long currentLatest = this.context.getLatestMillis();
		long shiftByMillis = shiftMultiplier*(currentLatest-currentEarliest);
		
		long newEarliest = (currentEarliest+shiftByMillis);
		long newLatest = (currentLatest+shiftByMillis);
		
		if(offsetAmount != 0 && CFWTimeUnit.has(offsetUnit)) {
			CFWTimeUnit unit =CFWTimeUnit.valueOf(offsetUnit);
			newEarliest = unit.offset(newEarliest, offsetAmount);
			newLatest = unit.offset(newLatest, offsetAmount);
		}
		
		this.context.setEarliest(newEarliest);
		this.context.setLatest(newLatest);
		return QueryPartValue.newNumber(newLatest);

				
	}

}
