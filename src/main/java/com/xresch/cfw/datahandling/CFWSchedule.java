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
import com.xresch.cfw.utils.CFWTime.CFWTimeUnit;
import com.xresch.cfw.validation.AbstractValidatable;
import com.xresch.cfw.validation.ScheduleValidator;

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
	private static final String TIMEZONEOFFSET	= "timezoneOffset";
	

	private TriggerBuilder<Trigger> triggerBuilder = null;
	
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
		
		JsonElement element = CFW.JSON.stringToJsonElement(jsonString);
		if(!element.isJsonNull() && element.isJsonObject()) {
			scheduleData = element.getAsJsonObject();
				timeframe 	= scheduleData.get("timeframe").getAsJsonObject();
				interval 	= scheduleData.get("interval").getAsJsonObject();
				everyweek = interval.get("everyweek").getAsJsonObject();
			
		}
		reset();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	private void setToDefaults() {
		reset();
		scheduleData = new JsonObject();
		

		int offsetMinutes = CFW.Time.getMachineTimeZoneOffSetMinutes();

		scheduleData.addProperty(TIMEZONEOFFSET,  offsetMinutes);
		
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
	public Integer timezoneOffset() {
		if(scheduleData == null || scheduleData.get(TIMEZONEOFFSET).isJsonNull()) return null;
		
		return scheduleData.get(TIMEZONEOFFSET).getAsInt();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule timezoneOffset(int timezoneOffset) {
		scheduleData.addProperty(TIMEZONEOFFSET, timezoneOffset);
		reset();
		return this;
	}
	
	/***************************************************************************************
	 * Can return null
	 ***************************************************************************************/
	public Long timeframeStart() {
		if(timeframe == null || timeframe.get(STARTDATETIME).isJsonNull()) return null;
		
		return timeframe.get(STARTDATETIME).getAsLong();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule timeframeStart(Date date) {
		timeframe.addProperty(STARTDATETIME, date.getTime());
		reset();
		return this;
	}
	
	/***************************************************************************************
	 * can return null
	 ***************************************************************************************/
	public Long timeframeEndtime() {
		if(timeframe == null || timeframe.get(ENDDATETIME).isJsonNull()) return null;
		
		return timeframe.get(ENDDATETIME).getAsLong();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule timeframeEndtime(Date date) {
		timeframe.addProperty(ENDDATETIME, date.getTime());
		reset();
		return this;
	}
	
	/***************************************************************************************
	 * can return null
	 ***************************************************************************************/
	public Integer timeframeExecutionCount() {
		if(timeframe == null || timeframe.get(EXECUTIONCOUNT).isJsonNull()) return null;
		
		return timeframe.get(EXECUTIONCOUNT).getAsInt();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule timeframeExecutionCount(int value) {
		timeframe.addProperty(EXECUTIONCOUNT, value);
		reset();
		return this;
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public IntervalType intervalType() {
		if(interval == null || interval.get(INTERVALTYPE).isJsonNull()) return null;

		return IntervalType.valueOf(interval.get(INTERVALTYPE).getAsString());
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule intervalType(IntervalType value) {
		interval.addProperty(INTERVALTYPE, value.toString());
		reset();
		return this;
	}
		
	/***************************************************************************************
	 * Can return null
	 ***************************************************************************************/
	public Integer intervalMinutes() {
		
		if(interval == null || interval.get(EVERYXMINUTES).isJsonNull()) return null;
		if(Strings.isNullOrEmpty(interval.get(EVERYXMINUTES).getAsString()) ) return null;
		
		return interval.get(EVERYXMINUTES).getAsInt();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule intervalMinutes(int value) {
		interval.addProperty(EVERYXMINUTES, value);
		reset();
		return this;
	}
	

	/***************************************************************************************
	 * Can return null
	 ***************************************************************************************/
	public Integer intervalDays() {
		if(interval == null ||  interval.get(EVERYXDAYS).isJsonNull()) return null;
		if(Strings.isNullOrEmpty(interval.get(EVERYXDAYS).getAsString()) ) return null;
		
		return interval.get(EVERYXDAYS).getAsInt();
	}
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule intervalDays(int value) {
		interval.addProperty(EVERYXDAYS, value);
		reset();
		return this;
	}
		
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public boolean intervalWeekday(Weekday day) {
		if(everyweek == null || everyweek.get(day.toString()).isJsonNull()) return false;
		
		return everyweek.get(day.toString()).getAsBoolean();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule intervalWeekday(Weekday day, boolean value) {
		everyweek.addProperty(day.toString(), value);
		reset();
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
		if(interval == null || interval.get(CRONEXPRESSION).isJsonNull()) return null;
		
		return interval.get(CRONEXPRESSION).getAsString();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule intervalCronExpression(String value) {
		interval.addProperty(CRONEXPRESSION, value);
		reset();
		return this;
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public EndType endType() {
		if(timeframe == null || timeframe.get(ENDTYPE).isJsonNull()) return null;
		
		return EndType.valueOf(timeframe.get(ENDTYPE).getAsString());
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWSchedule endType(EndType value) {
		timeframe.addProperty(ENDTYPE, value.toString());
		reset();
		return this;
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public String toStringScheduleEnd() {
		if(timeframe == null || timeframe.get(ENDTYPE).isJsonNull()) return null;
		
		if ( endType().equals(EndType.RUN_FOREVER) ){ return "Run Forever"; }
		else if (endType().equals(EndType.EXECUTION_COUNT)){ return "After "+this.timeframeExecutionCount()+" execution(s)"; }
		else if(this.timeframeEndtime() != null ) { return ""+this.timeframeEndtime(); }
		
		return null;

	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public String toStringScheduleInterval() {
		if(this.intervalType() == null) return null;
		
		//values: EVERY_X_MINUTES, EVERY_X_DAYS, EVERY_WEEK, CRON_EXPRESSION
		if (this.intervalType().equals(IntervalType.EVERY_X_MINUTES)){ return "Every "+this.intervalMinutes()+" minute(s)"; }
		else if (this.intervalType().equals(IntervalType.EVERY_X_DAYS)){ return "Every "+this.intervalDays()+" day(s)"; }
		else if (this.intervalType().equals(IntervalType.CRON_EXPRESSION)){ return "CRON: "+this.intervalCronExpression(); }
		else if (this.intervalType().equals(IntervalType.EVERY_WEEK)){ 
			
			String days = "";
			for(Integer day : this.getWeekdays()) {
				
				switch(day) {
					case Calendar.MONDAY: 		days += "MON/"; break;
					case Calendar.TUESDAY: 		days += "TUE/"; break;
					case Calendar.WEDNESDAY: 	days += "WED/"; break;
					case Calendar.THURSDAY: 	days += "THU/"; break;
					case Calendar.FRIDAY: 		days += "FRI/"; break;
					case Calendar.SATURDAY: 	days += "SAT/"; break;
					case Calendar.SUNDAY: 		days += "SON/"; break;
					
					default: break;
				}
			}
			
			days = days.substring(0, days.length()-1);
			return "Every week on "+days;
		}
		
		return null;

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
	 * Returns true if schedule is valid, false otherwise
	 ***************************************************************************************/
	public boolean validate() {
		ScheduleValidator validator = new ScheduleValidator(
				new AbstractValidatable<String>() {}.setLabel("schedule")
			);
		
		if( !validator.validate(this.toString()) ){
			new CFWLog(logger).severe(validator.getInvalidMessage(), new Exception());
			return false;
		}
		
		return true;
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
	 * Reset triggerBuilder on changes.
	 ***************************************************************************************/
	private void reset() {
		triggerBuilder = null;
	}
	
	
	/***************************************************************************************
	 * Returns a Quartz Trigger Builder based on this schedule.  
	 ***************************************************************************************/
	@SuppressWarnings("unchecked")
	public TriggerBuilder<Trigger> createQuartzTriggerBuilder() {
		
		if(triggerBuilder != null) {
			return triggerBuilder;
		}
		
		triggerBuilder = TriggerBuilder.newTrigger()
				//.withIdentity("myTrigger", "group1")
				.startAt(new Date(this.timeframeStart()));
		
		if(this.endType().equals(EndType.END_DATE_TIME)) {
			triggerBuilder.endAt(new Date(this.timeframeEndtime()));
		}

		//----------------------------------------
		// Scheduler

		switch(this.intervalType()) {
			case EVERY_X_MINUTES: 
				SimpleScheduleBuilder simpleBuilder = SimpleScheduleBuilder
					.simpleSchedule()
					.withIntervalInMinutes(this.intervalMinutes())
					.repeatForever() // getCalculatedIntervalSeconds() will not properly work if not set. Will be overridden by triggerBuilder.endAt()
					.withMisfireHandlingInstructionNextWithRemainingCount();
				
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
					.withIntervalInDays(this.intervalDays())
					.inTimeZone(
						CFW.Time.timezoneFromTimeZoneOffset( this.timezoneOffset() ) 
					)
					;
				
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
					.cronSchedule(this.intervalCronExpression())
					.inTimeZone( 
							CFW.Time.timezoneFromTimeZoneOffset( this.timezoneOffset() ) 
						)
					;
				
				switch(this.endType()) {
				case RUN_FOREVER: 		/*do nothing*/ break;
				case EXECUTION_COUNT: 	new CFWLog(logger).severe("Execution count is not supported for cron expressions.", new Exception());	
										return null;
										
				default:				/*do nothing*/ break;
			}
				triggerBuilder.withSchedule(cronBuilder);
				break;
		}

		return triggerBuilder;

	}
	

	
}
