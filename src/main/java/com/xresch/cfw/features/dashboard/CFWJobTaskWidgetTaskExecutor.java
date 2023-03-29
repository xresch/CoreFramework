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
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.jobs.CFWJobTask;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;

public class CFWJobTaskWidgetTaskExecutor extends CFWJobTask {
	
	public static final String UNIQUE_NAME = "Widget Task Executor";
	
	public static final String PARAM_TIMEFRAME_OFFSET = "JSON_TIMEFRAME_OFFSET";
	public static final String PARAM_WIDGET_ID = "WIDGET_ID";
	public static final String PARAM_WIDGET_NAME = "WIDGET_NAME";
	public static final String PARAM_DASHBOARD_ID = "DASHBOARD_ID";
	public static final String PARAM_DASHBOARD_NAME = "DASHBOARD_NAME";
	
	private static final Logger logger = CFWLog.getLogger(CFWJobTaskWidgetTaskExecutor.class.getName());
	
	@Override
	public String uniqueName() {
		return UNIQUE_NAME;
	}

	@Override
	public String taskDescription() {
		return "Executes tasks of dashboard widgets.";
	}

	@SuppressWarnings("rawtypes")
	public static CFWField createOffsetMinutesField() {
		return CFWField.newTimeframe(PARAM_TIMEFRAME_OFFSET)
						.setLabel("Timeframe Offset")
						.setDescription("The offset in minutes from present time used in case the placeholders $earliest$ / $latest$ are used in your widget.")
						.setValue(new CFWTimeframe());
			
	}
	
	@Override
	public CFWObject getParameters() {
		
		return new CFWObject()
			.addField(createOffsetMinutesField())
			.addField(
				CFWField.newInteger(FormFieldType.UNMODIFIABLE_TEXT, PARAM_DASHBOARD_ID)
						.setLabel("Dashboard ID")
						.setDescription("The ID of the dashboard containing the widget.")
						.addValidator(new NotNullOrEmptyValidator())
			)
			.addField(
					CFWField.newString(FormFieldType.UNMODIFIABLE_TEXT, PARAM_DASHBOARD_NAME)
							.setLabel("Dashboard Name")
							.setDescription("The name of the dashboard containing the widget.")
				)
			.addField(
				CFWField.newInteger(FormFieldType.UNMODIFIABLE_TEXT, PARAM_WIDGET_ID)
						.setLabel("Widget ID")
						.setDescription("The ID of the widget whose task should be executed.")
						.addValidator(new NotNullOrEmptyValidator())
			)
			.addField(
					CFWField.newString(FormFieldType.UNMODIFIABLE_TEXT, PARAM_WIDGET_NAME)
							.setLabel("Widget Name")
							.setDescription("The name of the widget whose task should be executed")
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
	
	/**************************************************************************************
	 * Adds the earliest and latest parameter to the job data map.
	 * @param data
	 * @param jobsettings
	 * @return
	 **************************************************************************************/
	public static CFWTimeframe getOffsetFromJobSettings(CFWObject jobsettings) {
		CFWTimeframe offset = (CFWTimeframe)jobsettings.getField(CFWJobTaskWidgetTaskExecutor.PARAM_TIMEFRAME_OFFSET).getValue();
		return offset;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void executeTask(JobExecutionContext context) throws JobExecutionException {
		
		JobDataMap data = context.getMergedJobDataMap();
		String widgetID = data.getString("WIDGET_ID");
		
		//------------------------------
		// Job Settings 
		CFWObject jobsettings = this.getParameters();
		jobsettings.mapJobExecutionContext(context);
		
		CFWTimeframe offset = getOffsetFromJobSettings(jobsettings); 
		
		//------------------------------
		// Fetch Widget 
		DashboardWidget widget = CFW.DB.DashboardWidgets.selectByID(widgetID);
		if(widget == null) {
			String jobID = context.getJobDetail().getKey().getName();
			new CFWLog(logger).warn("There is a job(ID:"+jobID+") defined for a not existing widget(ID:"+widgetID+").");
			return;
		}
		WidgetDefinition widgetDef = CFW.Registry.Widgets.getDefinition(widget.type());

		//------------------------------
		// Prepare Widget Settings
		CFWObject widgetSettings = widgetDef.getSettings();
		
		String placeholdersReplaced = 
				CFW.Utils.Time.replaceTimeframePlaceholders(
						widget.settings(), 
						offset.getEarliest(), 
						offset.getLatest(), 0);
		
		widgetSettings.mapJsonFields(placeholdersReplaced, true, true);
		
		//------------------------------
		// Task Parameters
		CFWObject taskParams = widgetDef.getTasksParameters();
		taskParams.addField(CFWJobTaskWidgetTaskExecutor.createOffsetMinutesField());
		taskParams.mapJsonFields(widget.taskParameters(), true, true);

		//-------------------------------
		// Map to Job Data
		// needed for mapping context to CFWJobAlertObject
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
		widgetDef.executeTask(context, taskParams, widget, widgetSettings, offset);
	}
	
}
