package com.xresch.cfw.features.jobs;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

public class RegistryJobs {

	private static Scheduler SCHEDULER;
	
	
	public static void startScheduler() {	
		SchedulerFactory factory = new StdSchedulerFactory();

		try {
			SCHEDULER = factory.getScheduler();
			
			loadJobsFromDB();
			
			SCHEDULER.start();
			
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private static void loadJobsFromDB() {
		//Load from DB
	}
}
