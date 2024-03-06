package com.xresch.cfw.features.dashboard;

import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.schedule.CFWScheduledTask;
import com.xresch.cfw.utils.CFWTime.CFWTimeUnit;

public class TaskDashboardCreateVersions extends CFWScheduledTask {
	
	private static Logger logger = CFWLog.getLogger(TaskDashboardCreateVersions.class.getName());


	@Override
	public void execute() {
		
		boolean doAutoVersions = CFW.DB.Config.getConfigAsBoolean(FeatureDashboard.CONFIG_CATEGORY, FeatureDashboard.CONFIG_AUTO_VERSIONS);
		
		if(doAutoVersions) {
			
			new CFWLog(logger).info("Dashboard: Create Automatic Versions");
			int hours = CFW.DB.Config.getConfigAsInt(FeatureDashboard.CONFIG_CATEGORY, FeatureDashboard.CONFIG_AUTO_VERSIONS_AGE);
			long millis = CFWTimeUnit.h.offset(null, -1 * hours);
			
			CFW.DB.Dashboards.createAutomaticVersions(millis);
			
		}
	}
	
}
