package com.xresch.cfw.features.query.functions;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
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
public class CFWQueryFunctionNow extends CFWQueryFunction {

	
	public static final String FUNCTION_NAME = "now";

	public CFWQueryFunctionNow(CFWQueryContext context) {
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
		return FUNCTION_NAME+"(format, offsetAmount, offsetUnit)";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns the current time with an optional offset. ";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<ul>"
				  +"<li><b>format:&nbsp;</b>(Optional)The format the returned time should have. Default is null, what returns epoch time in milliseconds. (Example: yyyy-MM-dd'T'HH:mm:ss.SSSZ)</li>"
				  +"<li><b>offsetAmount:&nbsp;</b>(Optional)The amount to offset from present time.(Default: 0)</li>"
				  +"<li><b>offsetUnit:&nbsp;</b>(Optional)The unit used to offset the time. One of the following(Default: 'm'):"
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
		String dateformat = null;
		int offsetAmount = 0;
		String offsetUnit = "m";
		
		//----------------------------------
		// Get Parameters
		int size = parameters.size(); 
		int index = 0;
		if(size >index) {
			QueryPartValue formatValue = parameters.get(index);
			if(formatValue.isString()) { dateformat = formatValue.getAsString(); };
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
		long offsetTime = new Date().getTime();
		
		if(offsetAmount != 0 && CFWTimeUnit.has(offsetUnit)) {
			offsetTime = CFWTimeUnit
							.valueOf(offsetUnit)
							.offset(offsetTime, offsetAmount);
		}
		
		if(dateformat == null) {
			return QueryPartValue.newNumber(offsetTime);
		}else {
			ZonedDateTime zonedTime = CFW.Time.zonedTimeFromEpoch(offsetTime);
			String dateFormatted = CFW.Time.formatDate(zonedTime, dateformat, this.context.getTimezoneOffsetMinutes());
			return QueryPartValue.newString(dateFormatted);
		}
					
	}

}
