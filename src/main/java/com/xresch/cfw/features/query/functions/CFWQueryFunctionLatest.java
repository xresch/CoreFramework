package com.xresch.cfw.features.query.functions;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.TreeSet;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.QueryPartValue;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionLatest extends CFWQueryFunction {

	
	public CFWQueryFunctionLatest(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return "latest";
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
		return "latest(format)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns latest time as epoch time or in a specific format. ";
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
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_latest.html");
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
		
		//----------------------------------
		// Get Format
		String dateformat = null;
		
		if(size > 0) {
			QueryPartValue formatValue = parameters.get(0);
			if(formatValue.isString()) { dateformat = formatValue.getAsString(); };
		}
		
		//----------------------------------
		// Get useClientTimezone
		boolean useClientTimezone = false;
		if(size > 1) {
			QueryPartValue useClientTimezoneValue = parameters.get(1);
			if(useClientTimezoneValue.isBoolOrBoolString()) { useClientTimezone = useClientTimezoneValue.getAsBoolean(); };
		}
						
		//----------------------------------
		// Create Time and Format
		long millis = this.context.getLatestMillis();
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
