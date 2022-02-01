package com.xresch.cfw.utils.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**************************************************************************************************************
 * Auxiliary class that is used to check if one or more JsonObjects are in a specified time range.
 *  
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class JsonTimerangeChecker {
	
	private String fieldname;
	private String timeformat;
	private long   earliestMillis;
	private long   latestMillis;
	
	private String epochFieldName;
	
	private static SimpleDateFormat simpleDateFormat;
	
	/*****************************************************************************************
	 * 
	 * @param fieldname the name of the field containing time
	 * @param timeformat the format of the time. use "epoch" for eopch time in millis, any other
	 *        string will be used as a date format string by SimpleDateFormat
	 * @param earliestMillis earliest time in epoch milliseconds
	 * @param latestMillis latest time in epoch milliseconds
	 *****************************************************************************************/
	public JsonTimerangeChecker(String fieldname, String timeformat, long earliestMillis, long latestMillis) {
		
		this.fieldname       = fieldname;         
		this.timeformat      = timeformat;        
		this.earliestMillis  = earliestMillis;   
		this.latestMillis    = latestMillis;     
		
		if(!timeformat.equals("epoch")) {
			simpleDateFormat = new SimpleDateFormat(timeformat);
		}
	}
	
	/************************************************************
	 * Set a fieldname that should be added to the object and 
	 * will contain the epoch time that was extracted from a 
	 * date string.
	 ************************************************************/
	public JsonTimerangeChecker epochAsNewField(String newFieldname) {
		epochFieldName = newFieldname;
		return this;
	}
	/************************************************************
	 * Check if the specified JsonObject is in the timerange.
	 * Returns false on parsing error.
	 * @param object to check if it is in the time range
	 * @param returnTrueOnNull if null values should be considered in range  
	 * @return
	 ************************************************************/
	public boolean isInTimerange(JsonObject object, boolean returnTrueOnNull) throws ParseException {
		
		JsonElement element = object.get(fieldname);
		
		if(element == null || element.isJsonNull()) {
			return (returnTrueOnNull) ? true : false;
		}
		
		long time;
		if(timeformat.equals("epoch")) {
			time = element.getAsLong();
		}else {
			String timeString = element.getAsString();
			time = simpleDateFormat.parse(timeString).getTime();
			if(epochFieldName != null) {
				object.addProperty(epochFieldName, time);
			}
		}

		return (time >= earliestMillis && time <= latestMillis ) ? true : false;
	}
	
	
	

}
