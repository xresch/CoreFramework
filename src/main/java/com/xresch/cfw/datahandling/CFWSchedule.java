package com.xresch.cfw.datahandling;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;

public class CFWSchedule {

	private JsonObject scheduleData;
	
	private JsonObject timeframe;
	private JsonObject interval;
	private JsonObject everyxweeks;
	
	private static String jsonTemplate = 
			"{ "
			+"'timeframe': { 'startdatetime': null, 'endtype': null, 'enddatetime': null, 'executioncount': '0' },"
			+"'interval': { 'intervaltype': null, 'everyxdays': '0', "
			+"   'everyxweeks': { 'weekcount': '0', 'mon': false, 'tue': false,'wed': false, 'thu': false, 'fri': false, 'sat': false, 'sun': false } },"
			+"   'cronexpression': null"
			+"}"
			.replace("'", "\"");
	
	
	
	public enum EndType{
		RUN_FOREVER,
		END_DATE_TIME,
		EXECUTION_COUNT,
	}
	
	public enum IntervalType{
		EVERY_X_DAYS,
		EVERY_X_WEEKS,
		CRON_EXPRESSION,
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule() {
		
		scheduleData = CFW.JSON.jsonStringToJsonElement(jsonTemplate).getAsJsonObject();
			timeframe 	= scheduleData.get("timeframe").getAsJsonObject();
			interval 	= scheduleData.get("interval").getAsJsonObject();
				everyxweeks = interval.get("everyxweeks").getAsJsonObject();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule(String jsonString) {
		JsonElement element = CFW.JSON.jsonStringToJsonElement(jsonString);
		if(!element.isJsonNull() && element.isJsonObject()) {
			scheduleData = element.getAsJsonObject();
				timeframe 	= scheduleData.get("timeframe").getAsJsonObject();
				interval 	= scheduleData.get("interval").getAsJsonObject();
					everyxweeks = interval.get("everyxweeks").getAsJsonObject();
			
		}
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule intervalType(IntervalType value) {
		interval.addProperty("intervaltype", value.toString());
		return this;
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule endType(EndType value) {
		interval.addProperty("endtype", value.toString());
		return this;
	}
	
	
	
	
}
