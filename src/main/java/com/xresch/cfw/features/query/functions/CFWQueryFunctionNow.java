package com.xresch.cfw.features.query.functions;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.QueryPartValue;

public class CFWQueryFunctionNow extends CFWQueryFunction {

	
	public CFWQueryFunctionNow(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return "now";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "now(format, offsetY, offsetM, offsetD, offsetH, offsetMin, offsetS, offsetMS)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns time based on present time and an optional offset. ";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>format:&nbsp;</b>(Optional)The format the returned time should have. Default is null, what returns epoch time in milliseconds. (Example: YYYY-MM-dd'T'HH:mm:ss.SSSZ)</p>"
			  +"<p><b>offsetY:&nbsp;</b>(Optional)Offset in years from present time.</p>"
			  +"<p><b>offsetM:&nbsp;</b>(Optional)Offset in months from present time.</p>"
			  +"<p><b>offsetD:&nbsp;</b>(Optional)Offset in days from present time.</p>"
			  +"<p><b>offsetH:&nbsp;</b>(Optional)Offset in hours from present time.</p>"
			  +"<p><b>offsetMin:&nbsp;</b>(Optional)Offset in minutes from present time.</p>"
			  +"<p><b>offsetS:&nbsp;</b>(Optional)Offset in seconds from present time.</p>"
			  +"<p><b>offsetMS:&nbsp;</b>(Optional)Offset in milliseconds from present time.</p>"
			;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_now.html");
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
		int offsetY = 0;
		int offsetM = 0;
		int offsetD = 0;
		int offsetH = 0;
		int offsetMin = 0;
		int offsetS = 0;
		int offsetMS = 0;
	
		//----------------------------------
		// Get Parameters
		int size = parameters.size(); 
		int index = 0;
		if(size > index) {
			QueryPartValue formatValue = parameters.get(index);
			if(formatValue.isString()) { dateformat = formatValue.getAsString(); };
			index++;

			//----------------------------------
			// offsetY
			if(size > index) {
				QueryPartValue offsetYValue = parameters.get(index);
				if(offsetYValue.isNumberOrNumberString()) { offsetY = offsetYValue.getAsInteger(); };
				index++;
				//----------------------------------
				// offsetM
				if(size > index) {
					QueryPartValue offsetMValue = parameters.get(index);
					if(offsetMValue.isNumberOrNumberString()) { offsetM = offsetMValue.getAsInteger(); };
					index++;
					//----------------------------------
					// offsetD
					if(size > index) {
						QueryPartValue offsetDValue = parameters.get(index);
						if(offsetDValue.isNumberOrNumberString()) { offsetD = offsetDValue.getAsInteger(); };
						index++;
						//----------------------------------
						// offsetH
						if(size > index) {
							QueryPartValue offsetHValue = parameters.get(index);
							if(offsetHValue.isNumberOrNumberString()) { offsetH = offsetHValue.getAsInteger(); };
							index++;
							//----------------------------------
							// offsetMin
							if(size > index) {
								QueryPartValue offsetMinValue = parameters.get(index);
								if(offsetMinValue.isNumberOrNumberString()) { offsetMin = offsetMinValue.getAsInteger(); };
								index++;
								//----------------------------------
								// offsetS
								if(size > index) {
									QueryPartValue offsetSValue = parameters.get(index);
									if(offsetSValue.isNumberOrNumberString()) { offsetS = offsetSValue.getAsInteger(); };
									index++;
									//----------------------------------
									// offsetMS
									if(size > index) {
										QueryPartValue offsetMSValue = parameters.get(index);
										if(offsetMSValue.isNumberOrNumberString()) { offsetMS = offsetMSValue.getAsInteger(); };
										index++;
									}
								}
							}
						}
					}
				}
			}
		}
						
		//----------------------------------
		// Create Time and Format
		if(dateformat == null) {
			long timeOffset = CFW.Time.offsetTime(new Date().getTime()
					, offsetY, offsetM, offsetD, offsetH, offsetMin, offsetS, offsetMS);
			return QueryPartValue.newNumber(timeOffset);
		}else {
			long timeOffset = CFW.Time.offsetTime(new Date().getTime()
					, offsetY, offsetM, offsetD, offsetH, offsetMin, offsetS, offsetMS);
			
			ZonedDateTime zonedTime = CFW.Time.zonedTimeFromEpoch(timeOffset);
			String dateFormatted = CFW.Time.formatDate(zonedTime, dateformat, this.context.getTimezoneOffsetMinutes());
			return QueryPartValue.newString(dateFormatted);
		}
				
	}

}
