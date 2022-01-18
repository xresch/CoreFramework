package com.xresch.cfw.datahandling;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.quartz.CalendarIntervalScheduleBuilder;
import org.quartz.CronScheduleBuilder;
import org.quartz.DailyTimeIntervalScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.validation.AbstractValidatable;
import com.xresch.cfw.validation.ScheduleValidator;

public class CFWTimeframe {

	private static final String MEMBER_LATEST = "latest";
	private static final String MEMBER_EARLIEST = "earliest";
	private static final String MEMBER_PRESET = "preset";


	private JsonObject timeframeData;
			
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWTimeframe() {
		setToDefaults();
		
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWTimeframe(String jsonString) {
		
		if(Strings.isNullOrEmpty(jsonString)) {
			setToDefaults();
			return;
		}
		
		JsonElement element = CFW.JSON.stringToJsonElement(jsonString);
		if(!element.isJsonNull() && element.isJsonObject()) {
			timeframeData = element.getAsJsonObject();
		}
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	private void setToDefaults() {
		timeframeData = new JsonObject();
		timeframeData.addProperty(MEMBER_PRESET, "30-m");
		timeframeData.add(MEMBER_EARLIEST, JsonNull.INSTANCE);
		timeframeData.add(MEMBER_LATEST, JsonNull.INSTANCE);
				
	}
	
	/***************************************************************************************
	 * Set the earliest time of the timeframe as epoch millis.
	 ***************************************************************************************/
	public CFWTimeframe setEarliest(long earliestMillis) {
		timeframeData.add(MEMBER_PRESET, JsonNull.INSTANCE);
		timeframeData.addProperty(MEMBER_EARLIEST, earliestMillis);
		return this;
	}
	
	/***************************************************************************************
	 * Set the earliest time of the timeframe.
	 ***************************************************************************************/
	public CFWTimeframe setEarliest(Timestamp earliest) {
		this.setEarliest(earliest.getTime());
		return this;
	}
	
	/***************************************************************************************
	 * Returns the earliest time of the timeframe as epoch millis.
	 ***************************************************************************************/
	public long getEarliest() {
		
		JsonElement preset = timeframeData.get(MEMBER_PRESET);
		if( preset != null && !preset.isJsonNull() && preset.isJsonPrimitive() ) {
			String[] splitted = preset.getAsString().trim().split("-");
			
			int offsetCount = -1 * Integer.parseInt(splitted[0]);
			String unit = splitted[1];
			
			Timestamp offset;
			switch(unit) {
				// Minutes
				case "m":  	offset = CFW.Utils.Time.getCurrentTimestampWithOffset(0, 0, 0, 0, offsetCount);
							break;
				// Hours
				case "h":  	offset = CFW.Utils.Time.getCurrentTimestampWithOffset(0, 0, 0, offsetCount, 0);
							break;
							
				//Days
				case "d":  	offset = CFW.Utils.Time.getCurrentTimestampWithOffset(0, 0, offsetCount, 0, 0);
							break;
							
				//Months
				case "M":  	offset = CFW.Utils.Time.getCurrentTimestampWithOffset(0, offsetCount, 0, 0, 0);
							break;
				
				//Unknown, fallback to 30 minutes and warn
				default:    offset = CFW.Utils.Time.getCurrentTimestampWithOffset(0, 0, 0, 0, -30);
							CFW.Messages.addWarningMessage("Unrecognized timeframe preset '"+preset.getAsString()+"', use last 30 minutes.");
							break;
			}
			
			return offset.getTime();
			
		}else if( timeframeData.get(MEMBER_EARLIEST) != null) {
			return timeframeData.get(MEMBER_EARLIEST).getAsLong();
		}else {
			CFW.Messages.addWarningMessage("Unrecognized timeframe settings, use last 30 minutes.");
			return CFW.Utils.Time.getCurrentTimestampWithOffset(0, 0, 0, 0, -30).getTime();
		}
		
	}
	
	/***************************************************************************************
	 * Set the earliest time of the timeframe as epoch millis.
	 ***************************************************************************************/
	public CFWTimeframe setLatest(long latestMillis) {
		timeframeData.add(MEMBER_PRESET, JsonNull.INSTANCE);
		timeframeData.addProperty(MEMBER_LATEST, latestMillis);
		return this;
	}
	
	/***************************************************************************************
	 * Set the earliest time of the timeframe.
	 ***************************************************************************************/
	public CFWTimeframe setLatest(Timestamp latest) {
		this.setLatest(latest.getTime());
		return this;
	}
	
	/***************************************************************************************
	 * Returns the latest time of the timeframe as epoch millis.
	 ***************************************************************************************/
	public long getLatest() {
		
		JsonElement preset = timeframeData.get(MEMBER_PRESET);
		if( preset != null && !preset.isJsonNull() && preset.isJsonPrimitive() ) {
				
			
			return System.currentTimeMillis();
			
		}else if( timeframeData.get(MEMBER_LATEST) != null) {
			return timeframeData.get(MEMBER_LATEST).getAsLong();
		}else {
			CFW.Messages.addWarningMessage("Unrecognized latest timeframe settings, use last 30 minutes.");
			return System.currentTimeMillis();
		}
		
	}
	
	/***************************************************************************************
	 * Convert to JSON String
	 ***************************************************************************************/
	@Override
	public String toString() {
		return CFW.JSON.toJSON(timeframeData);
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public JsonObject getAsJsonObject() {
		return timeframeData.deepCopy();
	}

}
