package com.xresch.cfw.features.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.gson.JsonObject;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.response.JSONResponse;

public abstract class WidgetDefinition {

	/************************************************************
	 * Return the unique name of the widget.
	 * @return String name
	 ************************************************************/
	public abstract String getWidgetType();
	
	/************************************************************
	 * Return a CFWObject containing fields with default values.
	 * Always return a new instance, do not reuse a CFWObject.
	 * @return CFWObject
	 ************************************************************/
	public abstract CFWObject getSettings();
	
	/************************************************************
	 * Create a json response containing the data you need for 
	 * your widget.
	 * @param request TODO
	 * @param settings TODO
	 * @return JSON string
	 ************************************************************/
	public abstract void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings);

	/************************************************************
	 * Return the file definitions of the javascript part of the 
	 * widget.
	 * @return file definition
	 ************************************************************/
	public abstract ArrayList<FileDefinition> getJavascriptFiles();

	/************************************************************
	 * Return the file definitions of the client side part of the 
	 * script.
	 * @return file definition
	 ************************************************************/
	public abstract HashMap<Locale, FileDefinition> getLocalizationFiles();
	
	/************************************************************
	 * Return the file definitions of the javascript part of the 
	 * widget.
	 * @return file definition
	 ************************************************************/
	public ArrayList<FileDefinition> getCSSFiles() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/************************************************************
	 * Check if the user has the required permission to create and
	 * edit the widget. Returns true by default.
	 * return true if has permission, false otherwise
	 ************************************************************/
	public boolean hasPermission() {
		return true;
	}
	
	/************************************************************
	 * Override this method and return true to define that this 
	 * widget definition supports tasks.
	 ************************************************************/
	public boolean supportsTask() {
		return false;
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
	public void executeTask(JobExecutionContext context, CFWObject taskParams, DashboardWidget widget, CFWObject widgetSettings) throws JobExecutionException {
		/* do nothing by default */
	}
	
	
}
