package com.xresch.cfw.features.query.functions;

import java.time.ZonedDateTime;
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
public class CFWQueryFunctionEarliest extends CFWQueryFunction {

	
	private static final String FUNCTION_NAME = "earliest";

	public CFWQueryFunctionEarliest(CFWQueryContext context) {
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
		return FUNCTION_NAME+"(format, useClientTimezone)";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns earliest time as epoch time or in a specific format. ";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>format:&nbsp;</b>(Optional)The format the returned time should have. Default is null, what returns epoch time in milliseconds. (Example: yyyy-MM-dd'T'HH:mm:ss.SSSZ)</p>"
			 + "<p><b>useClientTimezone:&nbsp;</b>(Optional). Default is false, UTC format will be used. If set to true, the time zone of the client will be used."
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
		int size = parameters.size(); 
		
		String dateformat = null;
		boolean useClientTimezone = false;
		
		//----------------------------------
		// Get Format
		if(size > 0) {
			QueryPartValue formatValue = parameters.get(0);
			if(formatValue.isString()) { dateformat = formatValue.getAsString(); };
			
			//----------------------------------
			// Get Format
			if(size > 1) {
				QueryPartValue useClientTimezoneValue = parameters.get(1);
				if(useClientTimezoneValue.isBoolOrBoolString()) { useClientTimezone = useClientTimezoneValue.getAsBoolean(); };
			}
		}
					
		//----------------------------------
		// Create Time and Format
		long millis = this.context.getEarliestMillis();
		if(dateformat == null) {
			return QueryPartValue.newNumber(millis);
		}else {
			ZonedDateTime zonedTime = CFW.Time.zonedTimeFromEpoch(millis);
			
			int offset = 0;
			if(useClientTimezone) {
				offset = this.context.getTimezoneOffsetMinutes();
			}
			
			String dateFormatted = CFW.Time.formatDate(zonedTime, dateformat, offset);
			return QueryPartValue.newString(dateFormatted);
		}
				
	}

}
