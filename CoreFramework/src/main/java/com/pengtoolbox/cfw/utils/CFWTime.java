package com.pengtoolbox.cfw.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.pengtoolbox.cfw._main.CFW;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWTime {
	
	public static final String TIME_FORMAT = "YYYY-MM-dd'T'HH:mm:ss.SSS";
	public static final SimpleDateFormat dateFormatter = new SimpleDateFormat(CFWTime.TIME_FORMAT);
	
	/********************************************************************************************
	 * Get a timestamp string of the current time in the format  "YYYY-MM-dd'T'HH:mm:ss.SSS".
	 * 
	 ********************************************************************************************/
	public static String currentTimestamp(){
		
		return CFWTime.formatDate(new Date());
	}
	
	/********************************************************************************************
	 * Get a string representation of the date in the format  "YYYY-MM-dd'T'HH:mm:ss.SSS".
	 ********************************************************************************************/
	public static String formatDate(Date date){
		return dateFormatter.format(date);
	}
	
	/********************************************************************************************
	 * Return a new instance of Date with the given offset from the current time.
	 * Use positive values to go back to the future, use negative values to go to the past.
	 * @return date
	 ********************************************************************************************/
	public static Date getCurrentDateWithOffset(int years, int days, int hours, int minutes) {
		
		Calendar calendar = Calendar.getInstance();
		
		calendar.add(Calendar.YEAR, years);
		calendar.add(Calendar.DAY_OF_YEAR, days);
		calendar.add(Calendar.HOUR_OF_DAY, hours);
		calendar.add(Calendar.MINUTE, minutes);
		
		return calendar.getTime();
	}
	
	/********************************************************************************************
	 * Return a new instance of Timestamp with the given offset from the current time.
	 * Use positive values to go back to the future, use negative values to go to the past.
	 * @return timestamp
	 ********************************************************************************************/
	public static Timestamp getCurrentTimestampWithOffset(int years, int days, int hours, int minutes) {
		return offsetTimestamp(new Timestamp(System.currentTimeMillis()), years, days, hours, minutes);
	}
	
	/********************************************************************************************
	 * Return a new instance of Timestamp with an offset starting from the given timestamp.
	 * Use positive values to go back to the future, use negative values to go to the past.
	 * @return timestamp
	 ********************************************************************************************/
	public static Timestamp offsetTimestamp(Timestamp timestamp, int years, int days, int hours, int minutes) {
		
		Calendar calendar = Calendar.getInstance();
		
		calendar.setTimeInMillis(timestamp.getTime());
		calendar.add(Calendar.YEAR, years);
		calendar.add(Calendar.DAY_OF_YEAR, days);
		calendar.add(Calendar.HOUR_OF_DAY, hours);
		calendar.add(Calendar.MINUTE, minutes);
		
		return new Timestamp(calendar.getTimeInMillis());
	}
	
	/********************************************************************************************
	 * Get the default age out time of the application.
	 * @return timestamp
	 ********************************************************************************************/
	public static Timestamp getDefaultAgeOutTime(int granularityMinutes) {
		Timestamp ageOutOffset = null;
		
		if		(granularityMinutes <= 3) 		{ ageOutOffset = CFW.Time.getCurrentTimestampWithOffset(0, 0, 0, -30); }
		else if (granularityMinutes <= 15) 		{ ageOutOffset = CFW.Time.getCurrentTimestampWithOffset(0, 0, -1, 0); }
		else if (granularityMinutes <= 60) 		{ ageOutOffset = CFW.Time.getCurrentTimestampWithOffset(0, -1, 0, 0); }
		else if (granularityMinutes <= 240) 	{ ageOutOffset = CFW.Time.getCurrentTimestampWithOffset(0, -7, 0, 0); }
		else if (granularityMinutes <= 720) 	{ ageOutOffset = CFW.Time.getCurrentTimestampWithOffset(0, -14, 0, 0); }
		else if (granularityMinutes <= 1440) 	{ ageOutOffset = CFW.Time.getCurrentTimestampWithOffset(0, -30, 0, 0); }
		else  									{ ageOutOffset = CFW.Time.getCurrentTimestampWithOffset(0, -90, 0, 0); }

		return ageOutOffset;
	}
	


}
