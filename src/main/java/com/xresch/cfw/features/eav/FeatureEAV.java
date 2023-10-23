package com.xresch.cfw.features.eav;

import java.util.concurrent.ScheduledFuture;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.config.ConfigChangeListener;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.spi.CFWAppFeature;

/**************************************************************************************************************
 * Feature for Entry-Attribute-Value(EAV) data.
 *  
 * @author Reto Scheiwiller, (c) Copyright 2023
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureEAV extends CFWAppFeature {
	
	public static final String PACKAGE_RESOURCE = "com.xresch.cfw.features.eav.resources";
	public static final String PACKAGE_MANUAL = "com.xresch.cfw.features.eav.manual";

	public final static String CONFIG_CATEGORY_EAV 		= "EAV: Entity Attribute Value";
	public final static String CONFIG_MAX_GRANULARITY 	= "Statistic Max Granularity";
	public final static String CONFIG_AGE_OUT_INTERVAL	= "Age Out Interval";
	public final static String CONFIG_AGE_OUT_15MIN 	= "1st Age Out: < 15 Minutes ";
	public final static String CONFIG_AGE_OUT_1HOUR 	= "2nd Age Out: < 1 Hour";
	public final static String CONFIG_AGE_OUT_6HOURS 	= "3rd Age Out: < 6 Hours";
	public final static String CONFIG_AGE_OUT_24HOURS 	= "4th Age Out: < 24 Hours";
	public final static String CONFIG_AGE_OUT_1WEEK 	= "5th Age Out: < 1 Week";
	
	private static ScheduledFuture<?> taskEavStoreToDB;
	private static ScheduledFuture<?> taskEavAgeOut;
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCE);
		CFW.Files.addAllowedPackage(PACKAGE_MANUAL);
		
		//----------------------------------
		// Register Objects
		CFW.Registry.Objects.addCFWObject(EAVEntity.class);
		CFW.Registry.Objects.addCFWObject(EAVAttribute.class);
		CFW.Registry.Objects.addCFWObject(EAVValue.class);
		CFW.Registry.Objects.addCFWObject(EAVStats.class);
		
		//----------------------------------
		// Register Source
		CFW.Registry.Query.registerSource(new CFWQuerySourceEAVStats(null));
	}

	@Override
	public void initializeDB() {
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration(CONFIG_CATEGORY_EAV, FeatureEAV.CONFIG_MAX_GRANULARITY )
				.description("The maximum granularity in minutes, that defines how often EAV statistics should be written to the database.")
				.type(FormFieldType.SELECT)
				.options(new Integer[]{1, 5, 10, 15, 20, 30, 60})
				.value("15")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration(CONFIG_CATEGORY_EAV, FeatureEAV.CONFIG_AGE_OUT_INTERVAL )
				.description("The execution interval of the age out of data.")
				.type(FormFieldType.SELECT)
				.options(new Integer[]{60, 120, 180, 360, 720, 1440})
				.value("60")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
				new Configuration(CONFIG_CATEGORY_EAV, FeatureEAV.CONFIG_AGE_OUT_15MIN )
				.description("The number of hours data with a granularity less then 15 minutes should be kept until aggregated to 15 minutes.")
				.type(FormFieldType.NUMBER)
				.value("6")
				);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
				new Configuration(CONFIG_CATEGORY_EAV, FeatureEAV.CONFIG_AGE_OUT_1HOUR )
				.description("The number of days data with a granularity less then 1 hour should be kept until aggregated to 1 hour.")
				.type(FormFieldType.NUMBER)
				.value("1")
				);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
				new Configuration(CONFIG_CATEGORY_EAV, FeatureEAV.CONFIG_AGE_OUT_6HOURS )
				.description("The number of days data with a granularity less then 6 hours should be kept until aggregated to 6 hours.")
				.type(FormFieldType.NUMBER)
				.value("7")
				);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
				new Configuration(CONFIG_CATEGORY_EAV, FeatureEAV.CONFIG_AGE_OUT_24HOURS )
				.description("The number of days data with a granularity less then 24 hours should be kept until aggregated to 24 hours.")
				.type(FormFieldType.NUMBER)
				.value("60")
				);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
				new Configuration(CONFIG_CATEGORY_EAV, FeatureEAV.CONFIG_AGE_OUT_1WEEK )
				.description("The number of days data with a granularity less then 1 week should be kept until aggregated to 1 week.")
				.type(FormFieldType.NUMBER)
				.value("365")
				);
		
		//-------------------------------
		// Create Change Listener
		ConfigChangeListener listener = new ConfigChangeListener(
				FeatureEAV.CONFIG_MAX_GRANULARITY
			) {
			
			@Override
			public void onChange() {
				startTasks();
			}
		};
		
		CFW.DB.Config.addChangeListener(listener);
		
		
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {
		//app.addAppServlet(ServletKeyValuePairs.class,  "/configuration");
	}

	@Override
	public void startTasks() {
		
		//----------------------------------------
		// Task: Store to Database
		if(taskEavStoreToDB != null) {
			taskEavStoreToDB.cancel(false);
		}
		
		int millis = (int)(1000 * 60 * CFW.DB.Config.getConfigAsInt(CONFIG_CATEGORY_EAV, FeatureEAV.CONFIG_MAX_GRANULARITY));
		taskEavStoreToDB = CFW.Schedule.runPeriodicallyMillis(0, millis, new TaskEAVStatsStoreToDB());
		
		//----------------------------------------
		// Task: AgeOut
		if(taskEavAgeOut != null) {
			taskEavAgeOut.cancel(false);
		}
		
		int millisAgeOut = (int)(1000 * 60 * CFW.DB.Config.getConfigAsInt(CONFIG_CATEGORY_EAV, FeatureEAV.CONFIG_AGE_OUT_INTERVAL));
		taskEavAgeOut = CFW.Schedule.runPeriodicallyMillis(0, millisAgeOut, new TaskEAVStatsAgeOut());
		
	}

	@Override
	public void stopFeature() {
		taskEavStoreToDB.cancel(false);
		taskEavAgeOut.cancel(false);
	}

}
