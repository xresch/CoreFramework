package com.xresch.cfw.tests.schedule;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.analytics.TaskCPUSampling;
import com.xresch.cfw.features.config.FeatureConfig;
import com.xresch.cfw.schedule.CFWScheduledTask;
import com.xresch.cfw.tests._master.WebTestMaster;

@Tag("development")
public class TestSchedule extends WebTestMaster {

	public static boolean isExecuted = false;
	
	//@Test
	public void testSchedule() throws InterruptedException {
		
		ScheduledFuture<?> future = CFW.Schedule.runPeriodically(5, 1, new CFWScheduledTask() {
			int count = 1;
			@Override
			public void execute() {
				System.out.println("Task: "+count++);
				

			}
		});
		
		ScheduledFuture<?> terminator = CFW.Schedule.runPeriodically(10, 1, new CFWScheduledTask() {
			@Override
			public void execute() {
				System.out.println("Terminate");
				future.cancel(true);
			}
		});
		
		while(!future.isDone()) {
			Thread.sleep(500L);
		}
	}
	
	//@Test
	public void testScheduleWeekly() throws InterruptedException {
		
		// set the time to a past date time to check if it works
		CFW.Schedule.scheduleWeekly(Calendar.MONDAY, 19, 46, 40, new CFWScheduledTask() {

			@Override
			public void execute() {
				System.out.println("Executed on: "+new Date().toLocaleString());
				TestSchedule.isExecuted = true;
			}
			
		});
		
		while(!isExecuted) {
			Thread.sleep(500L);
		}
	}
	
	/******************************************************************
	 * Check if the time is increased above current time correctly
	 * for the next scheduled execution.
	 * @throws InterruptedException
	 ******************************************************************/
	//@Test
	public void testScheduleJumpIncrease() throws InterruptedException {
		
		// set the time to a past date time to check if it works
		CFW.Schedule.scheduleTimed((int)TimeUnit.MINUTES.toSeconds(1), Calendar.MONDAY, 19, 46, 40, new CFWScheduledTask() {

			@Override
			public void execute() {
				System.out.println("Executed on: "+new Date().toLocaleString());
				TestSchedule.isExecuted = true;
			}
			
		});
		
		while(!isExecuted) {
			Thread.sleep(500L);
		}
	}
	
	/******************************************************************
	 * Check Thread Sampling Task
	 * @throws InterruptedException
	 ******************************************************************/
	@Test
	public void testThreadSamplingTask() throws Exception {
		
		int seconds = CFW.DB.Config.getConfigAsInt(FeatureConfig.CATEGORY_PERFORMANCE, FeatureConfig.CONFIG_CPU_SAMPLING_SECONDS);
		
		ScheduledFuture<?> future = CFW.Schedule.runPeriodically(0, seconds, new TaskCPUSampling());

		ScheduledFuture<?> terminator = CFW.Schedule.runPeriodically(100, 1, new CFWScheduledTask() {
			@Override
			public void execute() {
				System.out.println("Terminate");
				future.cancel(true);
			}
		});
		
		while(!future.isDone()) {
			Thread.sleep(500L);
		}
		
	}
}
