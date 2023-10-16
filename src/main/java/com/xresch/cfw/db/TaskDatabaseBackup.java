package com.xresch.cfw.db;

import java.util.Calendar;
import java.util.Timer;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.config.FeatureConfig;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.schedule.CFWScheduledTask;

public class TaskDatabaseBackup extends CFWScheduledTask {
	
	private static Timer databaseBackupTimer = null;
	
	public static void setupTask() {
		//---------------------------
		// Take down running taks
		if(databaseBackupTimer != null) {
			databaseBackupTimer.cancel();
			databaseBackupTimer = null;
		}
		
		//---------------------------
		// Check is Backup Enabled
		
		if(!CFW.DB.Config.getConfigAsBoolean(FeatureConfig.CATEGORY_DATABASE, FeatureConfig.CONFIG_DB_BACKUP_ENABLED)) {
			return;
		}
		
		//---------------------------
		// Create new Task
		long startTimeMillis = CFW.DB.Config.getConfigAsLong(FeatureConfig.CATEGORY_DATABASE, FeatureConfig.CONFIG_DB_BACKUP_TIME);
		int intervalDays = CFW.DB.Config.getConfigAsInt(FeatureConfig.CATEGORY_DATABASE, FeatureConfig.CONFIG_DB_BACKUP_INTERVAL);
		if(intervalDays <= 0) { intervalDays = 7; }

		Calendar startTime = Calendar.getInstance();
		startTime.setTimeInMillis(startTimeMillis);

		databaseBackupTimer = CFW.Schedule.scheduleTimed(
				startTime, 
				intervalDays * 24 * 60 * 60, 
				true, 
				new TaskDatabaseBackup());
	}
	
	@Override
	public void execute() {
		String folderPath = CFW.DB.Config.getConfigAsString(FeatureConfig.CATEGORY_DATABASE, FeatureConfig.CONFIG_DB_BACKUP_FOLDER);
		CFW.DB.backupDatabaseFile(folderPath, "h2_database_backup");
	}

}
