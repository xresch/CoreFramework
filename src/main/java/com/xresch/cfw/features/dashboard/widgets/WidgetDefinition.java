package com.xresch.cfw.features.dashboard.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.gson.JsonObject;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.FeatureDashboard;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.usermgmt.User;
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
	
	/***********************************************************************************************
	 * Return the category of the widget.
	 * This is used for the manual pages.
	 * If this returns FeatureDashboard.WIDGET_CATEGORY_EASTEREGGS, no manual page will be added.
	 ***********************************************************************************************/
	public abstract String widgetCategory();
	
	/***********************************************************************************************
	 * Return the name of the widget.
	 * This is used for the manual pages.
	 ***********************************************************************************************/
	public abstract String widgetName();
	
	/***********************************************************************************************
	 * Return the description for the manual page.
	 * This description will be shown on the manual under the header " <h2>Usage</h2>".
	 * If you add headers to your description it is recommended to use <h3> or lower headers.
	 * If this returns null, no entry will be added to the manual.
	 ***********************************************************************************************/
	public abstract String descriptionHTML();
	
	/************************************************************
	 * Return the cache policy of this widget for the default
	 * 1 minute widget data cache.
	 * @return String name
	 ************************************************************/
	public abstract WidgetDataCachePolicy getCachePolicy();
	
	
	/************************************************************
	 * This method returns true by default.Override this to 
	 * verify if the user can save the specified settings.
	 * This can be used to check permissions of the user and 
	 * deny him to save in certain cases.
	 * It can also be used to validate that the settings are 
	 * valid before saving them.
	 * 
	 * This method is also responsible to create error messages
	 * using CFW.Messages, in case saving is not allowed.
	 * 
	 * This is for example needed in case of the query feature.
	 * In most cases you will just return true.
	 * 
	 * @param request 
	 * @param response add your response data here.
	 * @param settings the settings of the widget, without
	 *        parameter values applied
	 * @param settingsWithParams the settings of the widget, 
	 * 		  with parameter values applied
	 * 	
	 ************************************************************/
	public boolean canSave(HttpServletRequest request, JSONResponse response, CFWObject settings, CFWObject settingsWithParams) {
		return true;
	}

	
	/************************************************************
	 * Create a json response containing the data you need for 
	 * your widget.
	 * @param request 
	 * @param response add your response data here.
	 * @param settings the settings of your widget
	 * @param jsonSettings the settings of your widget as Json
	 * @param timeframe TODO
	 * 	
	 ************************************************************/
	public abstract void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, CFWTimeframe timeframe);

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
	 * Check if the current user has the required permission to create and
	 * edit the widget. Returns true by default.
	 * return true if has permission, false otherwise
	 * @param user TODO
	 ************************************************************/
	public boolean hasPermission(User user) {
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
	 * Override this method to return a description of what the
	 * task of this widget does.
	 ************************************************************/
	public String getTaskDescription() {
		return null;
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
	 * @param offset TODO
	 *************************************************************************/
	public void executeTask(JobExecutionContext context, CFWObject taskParams, DashboardWidget widget, CFWObject widgetSettings, CFWTimeframe offset) throws JobExecutionException {
		/* do nothing by default */
	}
	
	
}
