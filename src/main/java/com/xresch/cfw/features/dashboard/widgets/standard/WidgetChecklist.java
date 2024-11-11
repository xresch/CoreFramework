package com.xresch.cfw.features.dashboard.widgets.standard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.FeatureDashboard;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.utils.CFWMonitor;

public class WidgetChecklist extends WidgetDefinition {

	private static final String FIELDNAME_CONTENT = "content";
	private static final String FIELDNAME_STRIKETHROUGH = "strikethrough";
	private static final String FIELDNAME_DO_SORT = "doSort";

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String getWidgetType() {return "cfw_checklist";}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				.addField(
					CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_CONTENT)
						.setDescription("Contains all the items on the checklist. Items checked begin with 'X '.")
						.setValue("Checkpoint A\r\nCheckpoint B\r\nCheckpoint C")
				)
				.addField(
					CFWField.newBoolean(FormFieldType.BOOLEAN, FIELDNAME_STRIKETHROUGH)
						.setDescription("Toggle if checked items should be striked through.")
						.setValue(true)
				)
				.addField(
						CFWField.newBoolean(FormFieldType.BOOLEAN, FIELDNAME_DO_SORT)
						.setLabel("Sort")
						.setDescription("Toggle if checked/unchecked items should be sorted.")
						.setValue(false)
						)
		;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetCategory() {
		return FeatureDashboard.WIDGET_CATEGORY_STANDARD;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetName() { return "Checklist"; }
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureDashboard.PACKAGE_MANUAL, "widget_"+getWidgetType()+".html");
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCachePolicy.ALWAYS;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, CFWTimeframe timeframe) { 
		// nothing to do
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "cfw_widget_checklist.js");
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add(js);
		return array;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		return map;
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public boolean supportsTask() {
		return true;
	}
	
	/************************************************************
	 * Override this method to return a description of what the
	 * task of this widget does.
	 ************************************************************/
	public String getTaskDescription() {
		return "Resets all checkboxes at the defined schedule.";
	}
	
	/************************************************************
	 * Override this method and return a CFWObject containing 
	 * fields for the task parameters. The settings will be passed 
	 * to the 
	 * Always return a new instance, do not reuse a CFWObject.
	 * @return CFWObject
	 ************************************************************/
	public CFWObject getTasksParameters() {
		
		return new CFWObject();
	}
	
	/*************************************************************************
	 * Implement the actions your task should execute.
	 * See {@link com.xresch.cfw.features.jobs.CFWJobTask#executeTask CFWJobTask.executeTask()} to get
	 * more details on how to implement this method.
	 *************************************************************************/
	@SuppressWarnings("unchecked")
	public void executeTask(JobExecutionContext context
						  , CFWObject taskParams
						  , DashboardWidget widget
						  , CFWObject widgetSettings
						  , CFWMonitor monitor, CFWTimeframe offset) throws JobExecutionException {
		
		
		//----------------------------------------
		// Fetch Data
		CFWField<String> contentField = (CFWField<String>)widgetSettings.getField(FIELDNAME_CONTENT);
		String currentContent = contentField.getValue();

		
		//----------------------------------------
		// Reset Checkboxes
		String resetContent = currentContent
								.replace("\nX", "\n")
								.replace("\nx", "\n")
								.replace("\r\nX", "\r\n")
								.replace("\r\nx", "\r\n")
								;
		
		//----------------------------------------
		// Save
		contentField.setValue(resetContent);
		
		widget.settings(CFW.JSON.toJSON(widgetSettings));
		
		widget.update();
		
		
	}

}
