package com.xresch.cfw.features.eav;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.schedule.CFWScheduledTask;

public class TaskStoreEAVStats extends CFWScheduledTask {
	

	@Override
	public void execute() {
		
		System.out.println("--- Store EAV ---");
		int minutes = CFW.DB.Config.getConfigAsInt(FeatureEAV.CONFIG_CATEGORY_EAV, FeatureEAV.CONFIG_STATISTICS_MAX_GRANULARITY);
		CFW.DB.EAVStats.storeStatsToDB(minutes);
		
	}
	
}
