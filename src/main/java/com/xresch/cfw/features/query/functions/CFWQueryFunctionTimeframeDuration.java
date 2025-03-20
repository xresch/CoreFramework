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
public class CFWQueryFunctionTimeframeDuration extends CFWQueryFunction {

	
	public static final String FUNCTION_NAME = "timeframeDuration";

	public CFWQueryFunctionTimeframeDuration(CFWQueryContext context) {
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
		return FUNCTION_NAME+"(timeUnit)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns the duration of the selected timeframe with the specified time unit.";
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

		String offsetUnit = "m";
			
		//----------------------------------
		// Get Parameters
		int size = parameters.size(); 

		if(size > 0) {
			QueryPartValue offsetUnitValue = parameters.get(0);
			if(offsetUnitValue.isString()) { offsetUnit = offsetUnitValue.getAsString(); };
		}
	
		//----------------------------------
		// Create Time and Format
		long earliest = this.context.getEarliestMillis();
		long latest = this.context.getLatestMillis();
		
		long duration = latest - earliest;
		
		if(CFWTimeUnit.has(offsetUnit)) {
			CFWTimeUnit unit =CFWTimeUnit.valueOf(offsetUnit);
			duration = unit.convert(duration);
		}else {
			CFW.Messages.addWarningMessage(FUNCTION_NAME+": Unsupported time unit: "+offsetUnit);
		}
		
		if(duration == 0) {
			duration = 1;
		}

		return QueryPartValue.newNumber(duration);

				
	}

}
