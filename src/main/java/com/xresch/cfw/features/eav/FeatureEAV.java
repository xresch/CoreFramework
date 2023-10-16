package com.xresch.cfw.features.eav;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.concurrent.ScheduledFuture;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.analytics.TaskCPUSampling;
import com.xresch.cfw.features.analytics.TaskCPUSamplingAgeOut;
import com.xresch.cfw.features.config.ConfigChangeListener;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.features.config.FeatureConfig;
import com.xresch.cfw.spi.CFWAppFeature;

/**************************************************************************************************************
 * Feature for Entry-Attribute-Value(EAV) data.
 *  
 * @author Reto Scheiwiller, (c) Copyright 2023
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureEAV extends CFWAppFeature {
	
	public static final String RESOURCE_PACKAGE = "com.xresch.cfw.features.eav.resources";

	public final static String CONFIG_CATEGORY_EAV = "EAV: Entity Attribute Value";
	public final static String CONFIG_STATISTICS_MAX_GRANULARITY = "Statistic Max Granularity";
	
	private static ScheduledFuture<?> taskStoreEav;
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		//----------------------------------
		// Register Objects
		CFW.Registry.Objects.addCFWObject(EAVEntity.class);
		CFW.Registry.Objects.addCFWObject(EAVAttribute.class);
		CFW.Registry.Objects.addCFWObject(EAVValue.class);
		CFW.Registry.Objects.addCFWObject(EAVStats.class);
		

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
		
		if(taskStoreEav != null) {
			taskStoreEav.cancel(false);
		}
		
		int millis = (int)(1000 * 60 * CFW.DB.Config.getConfigAsInt(CONFIG_CATEGORY_EAV, FeatureEAV.CONFIG_STATISTICS_MAX_GRANULARITY));
		millis = 60000;
		taskStoreEav = CFW.Schedule.runPeriodicallyMillis(0, millis, new TaskStoreEAVStats());
		
	}

	@Override
	public void stopFeature() {
		taskStoreEav.cancel(false);
	}

}
