package com.xresch.cfw.validation;

import java.text.ParseException;

import org.quartz.CronExpression;

import com.xresch.cfw.datahandling.CFWSchedule;
import com.xresch.cfw.datahandling.CFWSchedule.EndType;
import com.xresch.cfw.datahandling.CFWSchedule.IntervalType;
import com.xresch.cfw.datahandling.CFWSchedule.Weekday;


/**************************************************************************************************************
 * Used to validate CFWSchedule.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021
 * @license MIT-License
 **************************************************************************************************************/
public class ScheduleValidator extends AbstractValidator {
	
	public ScheduleValidator(IValidatable<?> validatable){
		super(validatable);
	}
	
	public ScheduleValidator(){}
	
	@Override
	public boolean validate(Object value) {
		
		//------------------------------------
		// Check Null values
		if( this.isNullAllowed() ) {
			if(value == null
			|| value.toString().isEmpty()
			|| value.toString().equals("{}")) {
				return true;
			}
		}else {
			if(value == null
			|| value.toString().isEmpty()
			|| value.toString().equals("{}")) {
				this.setInvalidMessage("The value for '"+validateable.getLabel()+"' cannot be null.");
				return false;
			}
		}
		
		//------------------------------------
		// Start Time
		CFWSchedule schedule = new CFWSchedule(value.toString());
		
		if(schedule.timeframeStart() == null) {
			this.setInvalidMessage("The start time for '"+validateable.getLabel()+"' is not valid.");
			return false;
		}
		
		//------------------------------------
		// End Type
		if(schedule.endType() == null) {
			this.setInvalidMessage("No end type is set for '"+validateable.getLabel()+"'.");
			return false;
		}
		
		//------------------------------------
		// End Time
		if(schedule.endType().equals(EndType.END_DATE_TIME)) {
			
			if(schedule.timeframeEndtime() == null) {
				this.setInvalidMessage("The end time for '"+validateable.getLabel()+"' is not valid.");
				return false;
			}
			
			if(schedule.timeframeEndtime() <= schedule.timeframeStart()) {
				this.setInvalidMessage("The end time for '"+validateable.getLabel()+"' has to be after the start time.");
				return false;
			}
			
		}
		
		//------------------------------------
		// Execution Count
		if( schedule.endType().equals(EndType.EXECUTION_COUNT) ) {
		
			if(schedule.timeframeExecutionCount() == null) {
				
				this.setInvalidMessage("The execution count for '"+validateable.getLabel()+"' is not set.");
				return false;
			}
		}

		//------------------------------------
		// Interval Type
		if(schedule.intervalType() == null) {
			this.setInvalidMessage("No interval type is set for '"+validateable.getLabel()+"'.");
			return false;
		}
		
		//------------------------------------
		// Interval Minutes
		if( schedule.intervalType().equals(IntervalType.EVERY_X_MINUTES) ) {
		
			if(schedule.intervalMinutes() == null || schedule.intervalMinutes() <= 0) {
				
				this.setInvalidMessage("The interval in minutes for '"+validateable.getLabel()+"' is not set.");
				return false;
			}
		}
		
		//------------------------------------
		// Interval Days
		if( schedule.intervalType().equals(IntervalType.EVERY_X_DAYS) ) {
		
			if(schedule.intervalDays() == null || schedule.intervalDays() <= 0 ) {
				
				this.setInvalidMessage("The interval in days for '"+validateable.getLabel()+"' is not set.");
				return false;
			}
		}
		
		//------------------------------------
		// Interval Weeks
		if( schedule.intervalType().equals(IntervalType.EVERY_WEEK) ) {
		
			boolean noDaysSelected = true;
			if( schedule.intervalWeekday(Weekday.MON) ) { noDaysSelected = false; }
			else if( schedule.intervalWeekday(Weekday.TUE) ) { noDaysSelected = false; }
			else if( schedule.intervalWeekday(Weekday.WED) ) { noDaysSelected = false; }
			else if( schedule.intervalWeekday(Weekday.THU) ) { noDaysSelected = false; }
			else if( schedule.intervalWeekday(Weekday.FRI) ) { noDaysSelected = false; }
			else if( schedule.intervalWeekday(Weekday.SAT) ) { noDaysSelected = false; }
			else if( schedule.intervalWeekday(Weekday.SUN) ) { noDaysSelected = false; }
			
				
			if( noDaysSelected ) {
				this.setInvalidMessage("No week day selected for '"+validateable.getLabel()+"'.");
				return false;
			}
		}
		
		//------------------------------------
		// Interval Cron
		if( schedule.intervalType().equals(IntervalType.CRON_EXPRESSION) ) {
			try {
				CronExpression cron = new CronExpression(schedule.intervalCronExpression());
			} catch (ParseException e) {
				this.setInvalidMessage("The expression '"+schedule.intervalCronExpression()+"' set for '"+validateable.getLabel()+"' is not a valid cron expresison.");
				return false;
			}
		}
		return true;
	}


}
