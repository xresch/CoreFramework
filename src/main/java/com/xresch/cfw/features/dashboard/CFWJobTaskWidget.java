package com.xresch.cfw.features.dashboard;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.jobs.CFWJobTask;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;

public class CFWJobTaskWidget extends CFWJobTask {
	
	private static final String WIDGET_ID = "WIDGET_ID";
	private static final String DASHBOARD_ID = "DASHBOARD_ID";

	@Override
	public String uniqueName() {
		return "Dashboard Widget";
	}

	@Override
	public String taskDescription() {
		return "Executes tasks of dashboard widgets.";
	}

	@Override
	public CFWObject getParameters() {
		return new CFWObject()
			.addField(
				CFWField.newInteger(FormFieldType.UNMODIFIABLE_TEXT, DASHBOARD_ID)
						.setDescription("The ID of the dashboard containing the widget.")
						.addValidator(new NotNullOrEmptyValidator())
			)
			.addField(
				CFWField.newInteger(FormFieldType.UNMODIFIABLE_TEXT, WIDGET_ID)
						.setDescription("The ID of the widget whose task should be executed.")
						.addValidator(new NotNullOrEmptyValidator())
			)
			;
		

	}

	@Override
	public int minIntervalSeconds() {
		return 60*5;
	}

	@Override
	public boolean hasPermission() {
		//Do not allow people to create this task from the UI.		
		return false;
	}

	@Override
	public void executeTask(JobExecutionContext context) throws JobExecutionException {
		
		JobDataMap data = context.getTrigger().getJobDataMap();
		String widgetID = data.getString("WIDGET_ID");
		
		DashboardWidget widget = CFW.DB.DashboardWidgets.selectByID(widgetID);
		WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(widget.type());

		CFWObject widgetSettings = definition.getSettings();
		widgetSettings.mapJsonFields(widget.settings());
		
		CFWObject taskParams = definition.getTasksParameters();
		taskParams.mapJsonFields(widget.taskParameters());
		
		definition.executeTask(context, taskParams, widget, widgetSettings);
	}
	
}
