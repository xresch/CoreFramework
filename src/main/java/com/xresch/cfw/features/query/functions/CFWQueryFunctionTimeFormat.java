package com.xresch.cfw.features.query.functions;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeSet;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.QueryPartValue;

public class CFWQueryFunctionTimeFormat extends CFWQueryFunction {

	public static final String FUNCTION_NAME = "timeformat";
	
	LocalDate now = LocalDate.now();
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	String formattedDate = now.format(formatter);
	LocalDate parsedDate = LocalDate.parse(formattedDate, formatter);
	
	public CFWQueryFunctionTimeFormat(CFWQueryContext context) {
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
		return FUNCTION_NAME+"(format, epochMillis, timezoneOffset)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns time based on present time and an optional offset.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>format:&nbsp;</b>(Optional)The format the returned time should have.</p>"
			  +"<p><b>epochMillis:&nbsp;</b>(Optional)The time to be formatted in epoch milliseconds.</p>"
			  +"<p><b>useClientTimezone:&nbsp;</b>(Optional). Default is false, UTC format will be used. If set to true, the time zone of the client will be used."
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
		// Initialize
		int paramCount = parameters.size(); 
		if(paramCount < 2) {
			return QueryPartValue.newNull();
		}
		
		String dateformat = null;
		long epochMillis = -1;
		boolean useClientTimezone = false;

		//----------------------------------
		// Get dateformat
		QueryPartValue formatValue = parameters.get(0);
		if(formatValue.isString()) { 
			dateformat = formatValue.getAsString(); 
		} else {
			return QueryPartValue.newNull();
		}
		

		//----------------------------------
		// Get epochMillis
		
		if(paramCount >= 2) {
			QueryPartValue epochValue = parameters.get(1);
			if(epochValue.isNumberOrNumberString()) { 
				epochMillis = epochValue.getAsLong(); 
			} else {
				return QueryPartValue.newNull();
			}

			//----------------------------------
			// Get useClientTimezone
			if(paramCount >= 3) {
				QueryPartValue timezoneValue = parameters.get(2);
				if(timezoneValue.isBoolOrBoolString()) { 
					useClientTimezone = timezoneValue.getAsBoolean(); 
				}
			}
		}
			
		//----------------------------------
		// Create Time and Format
		ZonedDateTime zonedTime = CFW.Time.zonedTimeFromEpoch(epochMillis);
		
		int offset = 0;
		if(useClientTimezone) {
			offset = this.context.getTimezoneOffsetMinutes();
		}
		
		String dateFormatted = CFW.Time.formatDate(zonedTime, dateformat, offset);
		return QueryPartValue.newString(dateFormatted);
				
	}

}
