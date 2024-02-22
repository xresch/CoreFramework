package com.xresch.cfw.features.query.database;

import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.schedule.CFWScheduledTask;

public class TaskQueryHistoryLimitEntries extends CFWScheduledTask {
	
	private static Logger logger = CFWLog.getLogger(TaskQueryHistoryLimitEntries.class.getName());

	public void execute() {
		
		int historyLimit = CFW.DB.Config.getConfigAsInt(FeatureQuery.CONFIG_CATEGORY, FeatureQuery.CONFIG_QUERY_HISTORY_LIMIT);
		CFWDBQueryHistory.removeOldestEntries(historyLimit);
	}
	

}
