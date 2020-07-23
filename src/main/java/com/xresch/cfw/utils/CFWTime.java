package com.xresch.cfw.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

import com.xresch.cfw._main.CFW;

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
	 * Get a string representation of the date in the given format
	 ********************************************************************************************/
	public static String formatDate(Date date, String timeFormat){
		SimpleDateFormat formatter = new SimpleDateFormat(timeFormat);
		return formatter.format(date);
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
	
	/********************************************************************************************
	 * Returns the number of seconds between two epoch 
	 * @return string
	 ********************************************************************************************/
	public static long calculateSecondsBetween(long earliestMillis, long latestMillis) {
		Duration d = Duration.between(Instant.ofEpochMilli(earliestMillis), Instant.ofEpochMilli(latestMillis));
		
		long seconds = d.toMillis() / 1000;
		
		return seconds;
	}
	
	/********************************************************************************************
	 * Returns an interval string like "15s", "5m" or "1h". 
	 * @return string
	 ********************************************************************************************/
	public static String calculateDatapointInterval(long earliestMillis, long latestMillis, int maxPoints) {
		
		long seconds = calculateSecondsBetween(earliestMillis, latestMillis);

		if((seconds / 15) < maxPoints) { return "15s"; }
		else if((seconds / 30) < maxPoints) { return "30s"; }
		else if((seconds / 60) < maxPoints) { return "1m"; }
		else if((seconds / 120) < maxPoints) { return "2m"; }
		else if((seconds / 300) < maxPoints) { return "5m"; }
		else if((seconds / 600) < maxPoints) { return "10m"; }
		else if((seconds / 900) < maxPoints) { return "15m"; }
		else if((seconds / 1800) < maxPoints) { return "30m"; }
		else if((seconds / 3600) < maxPoints) { return "1h"; }
		else if((seconds / (3600*2) ) < maxPoints) { return "2h"; }
		else if((seconds / (3600*4) ) < maxPoints) { return "4h"; }
		else if((seconds / (3600*6) ) < maxPoints) { return "6h"; }
		else if((seconds / (3600*12) ) < maxPoints) { return "12h"; }
		else if((seconds / (3600*24) ) < maxPoints) { return "24h"; }
		
		return "7d";
	}
	


}
