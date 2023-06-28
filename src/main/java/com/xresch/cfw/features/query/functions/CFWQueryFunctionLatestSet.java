package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.QueryPartValue;

public class CFWQueryFunctionLatestSet extends CFWQueryFunction {

	
	public CFWQueryFunctionLatestSet(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return "latestSet";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "latestSet(timeInMillis, offsetY, offsetM, offsetD, offsetH, offsetMin, offsetS, offsetMS)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Takes epoch milliseconds and offsets it by the specified amount, then sets it as the latest time. ";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>timeInMillis:&nbsp;</b>(Optional)The time in epoch milliseconds. If null, current latest time is used.</p>"
			  +"<p><b>offsetY:&nbsp;</b>(Optional)Offset in years.</p>"
			  +"<p><b>offsetM:&nbsp;</b>(Optional)Offset in months.</p>"
			  +"<p><b>offsetD:&nbsp;</b>(Optional)Offset in days.</p>"
			  +"<p><b>offsetH:&nbsp;</b>(Optional)Offset in hours.</p>"
			  +"<p><b>offsetMin:&nbsp;</b>(Optional)Offset in minutes.</p>"
			  +"<p><b>offsetS:&nbsp;</b>(Optional)Offset in seconds.</p>"
			  +"<p><b>offsetMS:&nbsp;</b>(Optional)Offset in milliseconds.</p>"
			;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_latestset.html");
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
			QueryPartValue millisValue = parameters.get(index);
			if(millisValue.isNumberOrNumberString()) { epochMillis = millisValue.getAsLong(); };
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

		if(epochMillis == null) {
			epochMillis = this.context.getLatestMillis();
		}

		long newLatest = CFW.Time.offsetTime(epochMillis
				, offsetY, offsetM, offsetD, offsetH, offsetMin, offsetS, offsetMS);
		
		this.context.setLatest(newLatest);
		return QueryPartValue.newNumber(newLatest);

				
	}

}