package com.xresch.cfw.utils;

import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.xrutils.utils.XRTime;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWTime extends XRTime {
	

	public static final int MINUTES_OF_15 = 15;
	public static final int MINUTES_OF_HOUR = 60;
	public static final int MINUTES_OF_6HOURS = 6*MINUTES_OF_HOUR;
	public static final int MINUTES_OF_DAY = 24*MINUTES_OF_HOUR;
	public static final int MINUTES_OF_WEEK = 7*MINUTES_OF_DAY;

	public static final int[] AGE_OUT_GRANULARITIES = new int[] {
			  MINUTES_OF_15
			, MINUTES_OF_HOUR
			, MINUTES_OF_6HOURS
			, MINUTES_OF_DAY
			, MINUTES_OF_WEEK
			};
	
	/********************************************************************************************
	 * Replaces timeframe placeholders.
	 * @param clientTimezoneOffset TODO
	 ********************************************************************************************/
	public static String replaceTimeframePlaceholders(String value, CFWTimeframe timeframe) {
		return replaceTimeframePlaceholders(
				  value
				, timeframe.getEarliest()
				, timeframe.getLatest()
				, timeframe.getClientTimezoneOffset()
			);
	}
}