package com.xresch.cfw.schedule;

import java.util.Calendar;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CFWTaskScheduler {

	 private  static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
	 
	 /********************************************************************************
	  * Schedules a periodical execution for a period of seconds
	  * @param initialDelaySeconds
	  * @param periodSeconds
	  * @param action
	  * @return ScheduledFuture<?>
	  ********************************************************************************/
	 public static ScheduledFuture<?> runPeriodically(int initialDelaySeconds, int periodSeconds, CFWScheduledTask action) {
		 return runPeriodically(initialDelaySeconds, periodSeconds, TimeUnit.SECONDS, action);
	 }
	 
	 /********************************************************************************
	  * Schedules a periodical execution for a period of seconds
	  * @param initialDelayMillis
	  * @param periodMillis
	  * @param action
	  * @return ScheduledFuture<?>
	  ********************************************************************************/
	 public static ScheduledFuture<?> runPeriodicallyMillis(int initialDelayMillis, int periodMillis, CFWScheduledTask action) {
		 return runPeriodically(initialDelayMillis, periodMillis, TimeUnit.MILLISECONDS, action);
	 }
	 
	 /********************************************************************************
	  * Schedules a periodical execution for a period of the specified time unit.
	  * @param initialDelay
	  * @param period
	  * @param unit
	  * @param action
	  * @return ScheduledFuture<?>
	  ********************************************************************************/
	 public static ScheduledFuture<?> runPeriodically(int initialDelay, int period, TimeUnit unit, CFWScheduledTask action) {
		 return scheduler.scheduleAtFixedRate(action, initialDelay, period, unit);
	 }
	 
	 /********************************************************************************
	  * Schedules a task weekly on the specified day and time.
	  * Will not execute immediately if the day is in the past, but will wait until
	  * the next day.
	  * 
	  * @param dayOfWeek use Calendar like Calendar. MONDAY
	  * @param hourOfDay
	  * @param minute
	  * @param second
	  * @param task
	  * @return
	  ********************************************************************************/
	 public static Timer scheduleWeekly(int dayOfWeek, int hourOfDay, int minute, int second, CFWScheduledTask task) {
		 return scheduleTimed((int)TimeUnit.DAYS.toSeconds(7), dayOfWeek, hourOfDay, minute, second, task);
	 }
	 
	 /********************************************************************************
	  * Schedules a task weekly on the specified day and time.
	  * Will not execute immediately if the day is in the past, but will wait until
	  * the next day.
	  * 
	  * @param intervalSec 
	  * @param dayOfWeek use Calendar like Calendar. MONDAY
	  * @param hourOfDay
	  * @param minute
	  * @param second
	  * @param task
	  * @return
	  ********************************************************************************/
	 public static Timer scheduleTimed(int intervalSec, int dayOfWeek, int hourOfDay, int minute, int second, CFWScheduledTask task) {
		
		Calendar calendar = Calendar.getInstance();
		
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);

        return scheduleTimed(calendar, intervalSec, true, task);
	 }
	 
	 /********************************************************************************
	  * Schedules a task starting from the specified date and time with the given interval
	  * in seconds.
	  * the next day. 
	  * @param startDateTime
	  * @param intervalSec
	  * @param preventImmediate do not execute immediately if startDateTime is in the past
	  * @param task
	  * @return
	  ********************************************************************************/
     public static Timer scheduleTimed(Calendar startDateTime, int intervalSec, boolean preventImmediate, CFWScheduledTask task) {
    		 
        Timer timer = new Timer(); // Instantiate Timer Object
        
        //----------------------------------
        // Increase time to future if immediate
        // is prevented
        if(preventImmediate) {
        	long startTime = startDateTime.getTimeInMillis();
        	long now = Calendar.getInstance().getTimeInMillis();
        	if(startTime < now) {
        		long diff = now - startTime;
        		long oneExecBeforeNowDeltaMs = diff - (diff % (intervalSec*1000));
        		startDateTime.add(Calendar.SECOND, ((int)oneExecBeforeNowDeltaMs/1000));
        	}
	        while(startDateTime.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
	        	startDateTime.add(Calendar.SECOND, intervalSec);
	        }
        }
        
        timer.schedule(task, startDateTime.getTime(), intervalSec*1000L);

        return timer;
	 }
}
