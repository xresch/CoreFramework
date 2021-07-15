package com.xresch.cfw.datahandling;

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
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;

public class CFWSchedule {

	private static Logger logger = CFWLog.getLogger(CFWSchedule.class.getName());
	
	private JsonObject scheduleData;
	
	private JsonObject timeframe;
	private JsonObject interval;
	private JsonObject everyweek;
	
	private static final String STARTDATETIME 	= "startdatetime";
	private static final String ENDTYPE 		= "endtype";
	private static final String ENDDATETIME 	= "enddatetime";
	private static final String EXECUTIONCOUNT 	= "executioncount";
	
	private static final String INTERVALTYPE 	= "intervaltype";
	private static final String EVERYXMINUTES 	= "everyxminutes";
	private static final String EVERYXDAYS 		= "everyxdays";
	private static final String CRONEXPRESSION 	= "cronexpression";
	private static final String EVERYWEEK 		= "everyweek";
	
//	private static String jsonTemplate = 
//			"{ "
//			+"'timeframe': { "
//				+"'"+STARTDATETIME+"': null, "
//				+"'"+ENDTYPE+"': null, "
//				+"'"+ENDDATETIME+"': null, "
//				+"'"+EXECUTIONCOUNT+"': '0'"
//			+"}, 'interval': { +"
//				+"'"+INTERVALTYPE+"': null, "
//				+"'"+EVERYXMINUTES+"': '0', "
//				+"'"+EVERYXDAYS+"': '0',"
//			+"   '"+EVERYWEEK+"': { 'weekcount': '0', 'MON': false, 'TUE': false,'WED': false, 'THU': false, 'FRI': false, 'SAT': false, 'SUN': false } },"
//			+"   '"+CRONEXPRESSION+"': null"
//			+"}"
//			.replace("'", "\"");
	
	
	
	public enum EndType{
		RUN_FOREVER,
		END_DATE_TIME,
		EXECUTION_COUNT,
	}
	
	public enum IntervalType{
		EVERY_X_MINUTES,
		EVERY_X_DAYS,
		EVERY_WEEK,
		CRON_EXPRESSION,
	}
	
	public enum Weekday{
		MON,
		TUE,
		WED,
		THU,
		FRI,
		SAT,
		SUN,
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule() {
		setToDefaults();
		
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule(String jsonString) {
		
		if(Strings.isNullOrEmpty(jsonString)) {
			setToDefaults();
			return;
		}
		
		JsonElement element = CFW.JSON.jsonStringToJsonElement(jsonString);
		if(!element.isJsonNull() && element.isJsonObject()) {
			scheduleData = element.getAsJsonObject();
				timeframe 	= scheduleData.get("timeframe").getAsJsonObject();
				interval 	= scheduleData.get("interval").getAsJsonObject();
					everyweek = interval.get("everyweek").getAsJsonObject();
			
		}
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	private void setToDefaults() {
		
		scheduleData = new JsonObject();
			timeframe 	= new JsonObject(); scheduleData.add("timeframe", timeframe);
				timeframe.add(STARTDATETIME, null);
				timeframe.add(ENDTYPE, null);
				timeframe.add(ENDDATETIME, null);
				timeframe.add(EXECUTIONCOUNT, null);
			
			interval 	= new JsonObject(); scheduleData.add("interval", interval);
				interval.add(INTERVALTYPE, null);
				interval.addProperty(EVERYXMINUTES, 0);
				interval.addProperty(EVERYXDAYS, 0);
				interval.add(CRONEXPRESSION, null);
				
				everyweek = new JsonObject(); interval.add(EVERYWEEK, everyweek);
					everyweek.addProperty(Weekday.MON.toString(), false);
					everyweek.addProperty(Weekday.TUE.toString(), false);
					everyweek.addProperty(Weekday.WED.toString(), false);
					everyweek.addProperty(Weekday.THU.toString(), false);
					everyweek.addProperty(Weekday.FRI.toString(), false);
					everyweek.addProperty(Weekday.SAT.toString(), false);
					everyweek.addProperty(Weekday.SUN.toString(), false);
				
	}
	
	
	
	/***************************************************************************************
	 * Can return null
	 ***************************************************************************************/
	public Long timeframeStart() {
		if(timeframe.get(STARTDATETIME).isJsonNull()) return null;
		
		return timeframe.get(STARTDATETIME).getAsLong();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule timeframeStart(Date date) {
		timeframe.addProperty(STARTDATETIME, date.getTime());
		return this;
	}
	
	/***************************************************************************************
	 * can return null
	 ***************************************************************************************/
	public Long timeframeEndtime() {
		if(timeframe.get(ENDDATETIME).isJsonNull()) return null;
		
		return timeframe.get(ENDDATETIME).getAsLong();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule timeframeEndtime(Date date) {
		timeframe.addProperty(ENDDATETIME, date.getTime());
		return this;
	}
	
	/***************************************************************************************
	 * can return null
	 ***************************************************************************************/
	public Integer timeframeExecutionCount() {
		if(timeframe.get(EXECUTIONCOUNT).isJsonNull()) return null;
		
		return timeframe.get(EXECUTIONCOUNT).getAsInt();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule timeframeExecutionCount(int value) {
		timeframe.addProperty(EXECUTIONCOUNT, value);
		return this;
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public IntervalType intervalType() {
		if(interval.get(INTERVALTYPE).isJsonNull()) return null;

		return IntervalType.valueOf(interval.get(INTERVALTYPE).getAsString());
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule intervalType(IntervalType value) {
		interval.addProperty(INTERVALTYPE, value.toString());
		return this;
	}
	
	/***************************************************************************************
	 * Can return null
	 ***************************************************************************************/
	public Integer intervalMinutes() {
		if(interval.get(EVERYXMINUTES).isJsonNull()) return null;
		if(Strings.isNullOrEmpty(interval.get(EVERYXMINUTES).getAsString()) ) return null;
		
		return interval.get(EVERYXMINUTES).getAsInt();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule intervalMinutes(int value) {
		interval.addProperty(EVERYXMINUTES, value);
		return this;
	}
	

	/***************************************************************************************
	 * Can return null
	 ***************************************************************************************/
	public Integer intervalDays() {
		if(interval.get(EVERYXDAYS).isJsonNull()) return null;
		if(Strings.isNullOrEmpty(interval.get(EVERYXDAYS).getAsString()) ) return null;
		
		return interval.get(EVERYXDAYS).getAsInt();
	}
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule intervalDays(int value) {
		interval.addProperty(EVERYXDAYS, value);
		return this;
	}
		
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public boolean intervalWeekday(Weekday day) {
		if(everyweek.get(day.toString()).isJsonNull()) return false;
		
		return everyweek.get(day.toString()).getAsBoolean();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule intervalWeekday(Weekday day, boolean value) {
		everyweek.addProperty(day.toString(), value);
		return this;
	}
	
	/***************************************************************************************
	 * Return a set of the Weekdays for the weekly schedule
	 ***************************************************************************************/
	public Set<Integer> getWeekdays() {
		TreeSet<Integer> set = new TreeSet<>();
		
		if(intervalWeekday(Weekday.MON)) { set.add(Calendar.MONDAY); }
		if(intervalWeekday(Weekday.TUE)) { set.add(Calendar.TUESDAY); }
		if(intervalWeekday(Weekday.WED)) { set.add(Calendar.WEDNESDAY); }
		if(intervalWeekday(Weekday.THU)) { set.add(Calendar.THURSDAY); }
		if(intervalWeekday(Weekday.FRI)) { set.add(Calendar.FRIDAY); }
		if(intervalWeekday(Weekday.SAT)) { set.add(Calendar.SATURDAY); }
		if(intervalWeekday(Weekday.SUN)) { set.add(Calendar.SUNDAY); }
		
		return set;
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public String intervalCronExpression() {
		if(interval.get(CRONEXPRESSION).isJsonNull()) return null;
		
		return interval.get(CRONEXPRESSION).getAsString();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule intervalCronExpression(String value) {
		interval.addProperty(CRONEXPRESSION, value);
		return this;
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public EndType endType() {
		if(timeframe.get(ENDTYPE).isJsonNull()) return null;
		
		return EndType.valueOf(timeframe.get(ENDTYPE).getAsString());
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule endType(EndType value) {
		timeframe.addProperty(ENDTYPE, value.toString());
		return this;
	}
	
	/***************************************************************************************
	 * Convert to JSON String
	 ***************************************************************************************/
	@Override
	public String toString() {
		return CFW.JSON.toJSON(scheduleData);
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public JsonObject getAsJsonObject() {
		return scheduleData.deepCopy();
	}
	
	/***************************************************************************************
	 * Returns the number of seconds between the two next executions of this schedule.
	 * Returns -1 if it cannot be calculated(e.g. single execution left) or no more executions are planned.
	 * It is recommended to validate the CFWSchedule before using this method.
	 ***************************************************************************************/
	public int getCalculatedIntervalSeconds() {
		
		Trigger trigger = createQuartzTriggerBuilder().build();

	    Date nextDate = trigger.getFireTimeAfter(new Date());
	    if(nextDate == null) return -1;
	    
        Date nextDate2 = trigger.getFireTimeAfter(nextDate);
        if(nextDate2 == null) return -1;
        
        int seconds = (int)(nextDate2.getTime() - nextDate.getTime()) / 1000;
        return seconds;

	}
	
	
	/***************************************************************************************
	 * Returns a Quartz Trigger Builder based on this schedule.  
	 ***************************************************************************************/
	@SuppressWarnings("unchecked")
	public TriggerBuilder<Trigger> createQuartzTriggerBuilder() {
		
		TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
					//.withIdentity("myTrigger", "group1")
					.startAt(new Date(this.timeframeStart()));
		
		if(this.endType().equals(EndType.END_DATE_TIME.toString())) {
			triggerBuilder.endAt(new Date(this.timeframeEndtime()));
		}

		//----------------------------------------
		// Scheduler

		switch(this.intervalType()) {
			case EVERY_X_MINUTES: 
				SimpleScheduleBuilder simpleBuilder = SimpleScheduleBuilder
					.simpleSchedule()
					.withIntervalInMinutes(this.intervalMinutes());
				
				switch(this.endType()) {
					case RUN_FOREVER: 		simpleBuilder.repeatForever(); break;
					case EXECUTION_COUNT: 	simpleBuilder.withRepeatCount(this.timeframeExecutionCount());	break;
					default:				/*do nothing*/ break;
				}
				triggerBuilder.withSchedule(simpleBuilder);
				
			break;
				
			case EVERY_X_DAYS: 
				CalendarIntervalScheduleBuilder calendarBuilder = CalendarIntervalScheduleBuilder
					.calendarIntervalSchedule()
					.withIntervalInDays(this.intervalDays());
				
				switch(this.endType()) {
					case RUN_FOREVER: 		/*do nothing*/ break;
					case EXECUTION_COUNT: 	new CFWLog(logger).severe("Execution count is not supported for interval in days.", new Exception());	
											return null;
											
					default:				/*do nothing*/ break;
				}
				triggerBuilder.withSchedule(calendarBuilder);
				
				break;
				
			case EVERY_WEEK: 

				DailyTimeIntervalScheduleBuilder weeklyBuilder = DailyTimeIntervalScheduleBuilder
					.dailyTimeIntervalSchedule()
					.withIntervalInHours(24)
					.onDaysOfTheWeek(this.getWeekdays());
				switch(this.endType()) {
					case RUN_FOREVER: 		/*do nothing*/ break;
					case EXECUTION_COUNT: 	weeklyBuilder.withRepeatCount(this.timeframeExecutionCount());	break;
					default:				/*do nothing*/ break;
				}
				triggerBuilder.withSchedule(weeklyBuilder);
				break;
			
			case CRON_EXPRESSION: 
				CronScheduleBuilder cronBuilder = CronScheduleBuilder
					.cronSchedule(this.intervalCronExpression());
				
				triggerBuilder.withSchedule(cronBuilder);
				break;
		}

		return triggerBuilder;

	}
	

	
}
