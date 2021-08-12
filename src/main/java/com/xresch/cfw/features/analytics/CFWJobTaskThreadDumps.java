package com.xresch.cfw.features.analytics;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.jobs.CFWJobTask;
import com.xresch.cfw.utils.CFWUtilsAnalysis;
import com.xresch.cfw.validation.LengthValidator;

public class CFWJobTaskThreadDumps extends CFWJobTask {
	

	@Override
	public String uniqueName() {
		return "Thread Dumps";
	}

	@Override
	public String taskDescription() {
		return "Writes Thread dumps to the disk for analytical purposes.";
	}

	@Override
	public CFWObject getParameters() {
		return new CFWObject()
			.addField(
				CFWField.newString(FormFieldType.TEXT, "folder")
						.setDescription("the path of the folder were the thread dumps should be written on the server.")
						.setValue("./threaddumps")
						.addValidator(new LengthValidator(3, 4096))
			);
		

	}

	@Override
	public int minIntervalSeconds() {
		return 15;
	}

	@Override
	public boolean hasPermission() {
		
		if( CFW.Context.Request.hasPermission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS) ) {
			return true;
		}
		
		return false;
	}

	@Override
	public void executeTask(JobExecutionContext context) throws JobExecutionException {
		
		JobDataMap data = context.getMergedJobDataMap();
		String folderpath = data.getString("folder");
		
        String filepath = "threaddump_"+CFW.Utils.Time.currentTimestamp().replace(":", "")+".txt";
        CFWUtilsAnalysis.threadDumpToDisk(folderpath, filepath);
		
	}
	
}
