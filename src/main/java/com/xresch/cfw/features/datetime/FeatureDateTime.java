package com.xresch.cfw.features.datetime;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.spi.CFWAppFeature;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023
 * @license Org Manager License
 **************************************************************************************************************/
public class FeatureDateTime extends CFWAppFeature {
	
	public static final String PACKAGE_RESOURCE = "com.xresch.cfw.features.datetime.resources";
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCE);

		//-------------------------------------
    	// Register Objects
		CFW.Registry.Objects.addCFWObject(CFWDate.class);			
	}

	@Override
	public void initializeDB() {
		
		CFW.DB.Date.reloadCache();
		
		//-------------------------------------
		// Initialize DB with +/-1 year 
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
		calendar.add(Calendar.DATE,-365);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		ZonedDateTime date = CFW.Time.zonedTimeFromEpochUTC(System.currentTimeMillis());
		date = date.withZoneSameLocal(ZoneId.of("UTC"))
			.minusYears(1)
			.withHour(0)
			.withMinute(0)
			.withSecond(0)
			.withNano(0);
		
		for(int i = 0; i < (365*2); i++) {
			// creates entry in DB if not exists
			CFWDate.newDate(date);
			date = date
					.plusDays(1)
					.withHour(0)
					.withMinute(0)
					.withSecond(0)
					.withNano(0);
		}
						
	}

	@Override
	public void addFeature(CFWApplicationExecutor executor) {
		
		//executor.addAppServlet(ServletOrgUnits.class, "/datapoints");

	}

	@Override
	public void startTasks() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopFeature() {
		// TODO Auto-generated method stub

	}

}
