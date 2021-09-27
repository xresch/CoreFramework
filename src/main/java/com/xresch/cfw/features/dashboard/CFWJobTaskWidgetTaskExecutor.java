package com.xresch.cfw.features.dashboard;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.jobs.CFWJobTask;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;

public class CFWJobTaskWidgetTaskExecutor extends CFWJobTask {
	
	public static final String UNIQUE_NAME = "Widget Task Executor";
	
	public static final String PARAM_WIDGET_ID = "WIDGET_ID";
	public static final String PARAM_DASHBOARD_ID = "DASHBOARD_ID";
	
	private static final Logger logger = CFWLog.getLogger(CFWJobTaskWidgetTaskExecutor.class.getName());
	
	@Override
	public String uniqueName() {
		return UNIQUE_NAME;
	}

	@Override
	public String taskDescription() {
		return "Executes tasks of dashboard widgets.";
	}

	@Override
	public CFWObject getParameters() {
		return new CFWObject()
			.addField(
				CFWField.newInteger(FormFieldType.UNMODIFIABLE_TEXT, PARAM_DASHBOARD_ID)
						.setLabel("Dashboard ID")
						.setDescription("The ID of the dashboard containing the widget.")
						.addValidator(new NotNullOrEmptyValidator())
			)
			.addField(
				CFWField.newInteger(FormFieldType.UNMODIFIABLE_TEXT, PARAM_WIDGET_ID)
						.setLabel("Widget ID")
						.setDescription("The ID of the widget whose task should be executed.")
						.addValidator(new NotNullOrEmptyValidator())
			)
			;
		

	}

	@Override
	public int minIntervalSeconds() {
		return 5;
	}
	
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		return null;
	}

	@Override
	public boolean hasPermission(User user) {	
		return true;
	}
	
	@Override
	public boolean createableFromUI() {
		//Do not allow people to create this task from the UI.	
		return false;
	}

	@Override
	public void executeTask(JobExecutionContext context) throws JobExecutionException {
		
		JobDataMap data = context.getMergedJobDataMap();
		String widgetID = data.getString("WIDGET_ID");
		
		DashboardWidget widget = CFW.DB.DashboardWidgets.selectByID(widgetID);
		if(widget == null) {
			String jobID = context.getJobDetail().getKey().getName();
			new CFWLog(logger).warn("There is a job(ID:"+jobID+") defined for a not existing widget(ID:"+widgetID+").");
			return;
		}
		WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(widget.type());

		//------------------------------
		// Widget Settings
		CFWObject widgetSettings = definition.getSettings();
		widgetSettings.mapJsonFields(widget.settings());
		
		//------------------------------
		// Task Parameters
		CFWObject taskParams = definition.getTasksParameters();
		taskParams.mapJsonFields(widget.taskParameters());
		
		//Add to job data, needed for mapping context to CFWJobAlertObject
		for(Entry<String, CFWField> entry : taskParams.getFields().entrySet()) {
			Object fieldValue = entry.getValue().getValue();
			if(fieldValue instanceof String) { 
				data.put(entry.getKey(), fieldValue); 
			}
			else {
				data.put(entry.getKey(), CFW.JSON.toJSON(fieldValue));
			}
		}
			
		//------------------------------
		// Call widget Task
		definition.executeTask(context, taskParams, widget, widgetSettings);
	}
	
}
