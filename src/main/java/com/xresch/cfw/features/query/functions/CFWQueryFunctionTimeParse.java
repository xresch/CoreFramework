package com.xresch.cfw.features.query.functions;

import java.text.ParseException;
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

public class CFWQueryFunctionTimeParse extends CFWQueryFunction {

	
	public CFWQueryFunctionTimeParse(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return "timeparse";
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
		return "timeparse(format, timeString)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Parses time from a string representation and returns it as epoch milliseconds.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 
				"<ul>"
				+"<li><b>format:&nbsp;</b>(Optional)The The format of the input string. (Example: yyyy-MM-dd'T'HH:mm:ss.SSSZ)</li>"
				+"<li><b>timeString:&nbsp;</b>(Optional). A string containing a time in the defined format.</li>"
			+ "</ul>"
			;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_timeparse.html");
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
		// Check has Params
		if(size < 2) {
			return QueryPartValue.newNull();
		}

		
		//----------------------------------
		// Get Format
		String dateformat = null;

		QueryPartValue formatValue = parameters.get(0);
		if(formatValue.isString()) { 
			dateformat = formatValue.getAsString(); 
		};
		
		
		//----------------------------------
		// Get time String
		String timeString = "";

		QueryPartValue timeStringValue = parameters.get(1);
		if(!timeStringValue.isNullOrEmptyString() && timeStringValue.isString()) { 
			timeString = timeStringValue.getAsString(); 
		}else {
			return QueryPartValue.newNull();
		}
		
						
		//----------------------------------
		// Create Time and Format
		long millis = this.context.getEarliestMillis();
		if(dateformat == null) {
			return QueryPartValue.newNumber(millis);
		}else {
			long epochMillis;
			
			try {
				epochMillis = CFW.Time.parseTime(dateformat, timeString);
				return QueryPartValue.newNumber(epochMillis);
			} catch (ParseException e) {
				return QueryPartValue.newBoolean(false);
			}
			
		}
				
	}

}
