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
import com.xresch.cfw.utils.CFWTime.CFWTimeUnit;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionTimeframeMax extends CFWQueryFunction {

	
	public static final String FUNCTION_NAME = "timeframeMax";

	public CFWQueryFunctionTimeframeMax(CFWQueryContext context) {
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
		tags.add(_CFWQueryCommon.TAG_TIME);
		return tags;
	}
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(amount, timeUnit)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Set the maximum duration allowed for the selected timeframe.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<ul>"
				  +"<li><b>unit:&nbsp;</b>(Optional) The unit used to represent the duration. One of the following(Default: 'm'):"
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
		int maxAmount = 0;
		String timeUnit = "m";
			
		//----------------------------------
		// Get Parameters
		int size = parameters.size(); 
		int index = 0;
		if(size >index) {
			
			QueryPartValue offsetAmmountValue = parameters.get(index);
			if(offsetAmmountValue.isNumberOrNumberString()) { maxAmount = offsetAmmountValue.getAsInteger(); };
			index++;
			//----------------------------------
			// offset Unit
			if(size > index) {
				QueryPartValue offsetUnitValue = parameters.get(index);
				if(offsetUnitValue.isString()) { timeUnit = offsetUnitValue.getAsString(); };
				index++;
			}
			
		}
	
		//----------------------------------
		// Create Time and Format
		long earliest = this.context.getEarliestMillis();
		long latest = this.context.getLatestMillis();
		
		long duration = latest - earliest;
		
		if(CFWTimeUnit.has(timeUnit)) {
			CFWTimeUnit unit =CFWTimeUnit.valueOf(timeUnit);
			long maxDuration = unit.toMillis(maxAmount);
			
			if(duration > maxDuration) {
				long adjustedEarliest = latest - maxDuration;
				this.context.setEarliest(adjustedEarliest);
				CFW.Messages.addWarningMessage(
						  "Timeframe limit of '"+maxAmount+timeUnit+"' has been exceeded. "
						+ "Earliest time adjusted to: "+CFW.Time.formatMillisAsTimestamp(adjustedEarliest)
						);
			}
		}else {
			CFW.Messages.addWarningMessage(FUNCTION_NAME+": Unsupported time unit: "+timeUnit);
		}
		
		return QueryPartValue.newNumber(duration);

				
	}

}
