package com.xresch.cfw.features.analytics;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.features.config.FeatureConfig;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.schedule.CFWScheduledTask;

public class TaskStatusMonitorUpdate extends CFWScheduledTask {
	

	@Override
	public void execute() {
		
		CFWStatusMonitorRegistry.loadStatusListAndCache();
		
	}
	


}
