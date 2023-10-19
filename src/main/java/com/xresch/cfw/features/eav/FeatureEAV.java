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

	public final static String CONFIG_CATEGORY_EAV = "EAV: Entity Attribute Value";
	public final static String CONFIG_STATISTICS_MAX_GRANULARITY = "Statistic Max Granularity";
	
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
			new Configuration(CONFIG_CATEGORY_EAV, FeatureEAV.CONFIG_STATISTICS_MAX_GRANULARITY )
				.description("The maximum granularity in minutes, that defines how often EAV statistics should be written to the database.")
				.type(FormFieldType.SELECT)
				.options(new Integer[]{5, 10, 15, 10, 20, 30, 60})
				.value("15")
		);
		
		//-------------------------------
		// Create Change Listener
		ConfigChangeListener listener = new ConfigChangeListener(
				FeatureEAV.CONFIG_STATISTICS_MAX_GRANULARITY
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
		
		int millis = (int)(1000 * 60 * CFW.DB.Config.getConfigAsInt(CONFIG_CATEGORY_EAV, FeatureEAV.CONFIG_STATISTICS_MAX_GRANULARITY));
		millis = 60000;
		taskEavStoreToDB = CFW.Schedule.runPeriodicallyMillis(0, millis, new TaskEAVStatsStoreToDB());
		
		//----------------------------------------
		// Task: AgeOut
		if(taskEavAgeOut != null) {
			taskEavAgeOut.cancel(false);
		}
		
		int millisAgeOut = (int)(1000 * 60 * CFW.DB.Config.getConfigAsInt(CONFIG_CATEGORY_EAV, FeatureEAV.CONFIG_STATISTICS_MAX_GRANULARITY));
		millisAgeOut = 6000;
		taskEavAgeOut = CFW.Schedule.runPeriodicallyMillis(0, millisAgeOut, new TaskEAVStatsAgeOut());
		
	}

	@Override
	public void stopFeature() {
		taskEavStoreToDB.cancel(false);
		taskEavAgeOut.cancel(false);
	}

}
