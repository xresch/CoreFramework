package com.xresch.cfw.features.jobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.CFWFieldFlag;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.dashboard.FeatureDashboard;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023
 * @license MIT-License
 **************************************************************************************************************/
public class WidgetTriggerJobs extends WidgetDefinition {

	private static final String FIELDNAME_JOBS = "JSON_JOBS";
	private static Logger logger = CFWLog.getLogger(WidgetTriggerJobs.class.getName());
	

	/******************************************************************************
	 * 
	 ******************************************************************************/
	@Override
	public String getWidgetType() {return "cfw_triggerjobs";}
	
	/******************************************************************************
	 * 
	 ******************************************************************************/
	@Override
	public WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCachePolicy.OFF;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetCategory() {
		return FeatureDashboard.WIDGET_CATEGORY_ADVANCED;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetName() { return "Trigger Jobs"; }
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureJobs.PACKAGE_RESOURCES, "widget_"+getWidgetType()+".html");
	}
	
	/******************************************************************************
	 * 
	 ******************************************************************************/
	@SuppressWarnings("rawtypes")
	@Override
	public CFWObject getSettings() {
		return new CFWObject()
								
				.addField(
						(CFWField)CFWField.newTagsSelector(FIELDNAME_JOBS)
						.setLabel("Jobs")
						.setDescription("The jobs the user can trigger")
						.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
						.setAutocompleteHandler(new CFWAutocompleteHandler() {
							
							@Override
							public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
								
								return CFW.DB.Jobs.autocompleteJob(searchValue, 20);
							}
						})

				)
		;
	}
	

	/******************************************************************************
	 * 
	 ******************************************************************************/
	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings
			, CFWTimeframe timeframe) { 
			
		
		LinkedHashMap<String,String> jobs = (LinkedHashMap<String,String>) settings.getField(FIELDNAME_JOBS).getValue();
		
		JsonArray jobsArray = CFW.DB.Jobs.getJobListAsJsonArray(jobs.keySet().toArray());
		response.setPayload(jobsArray);	
	}
	
	
	/******************************************************************************
	 * 
	 ******************************************************************************/
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add(  new FileDefinition(HandlingType.JAR_RESOURCE, FeatureJobs.PACKAGE_RESOURCES, "widget_"+getWidgetType()+".js") );
		return array;
	}

	/******************************************************************************
	 * 
	 ******************************************************************************/
	@Override
	public ArrayList<FileDefinition> getCSSFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		//added globally array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureJobs.PACKAGE_RESOURCES, "cfw_query.css") );
		return array;
	}

	/******************************************************************************
	 * 
	 ******************************************************************************/
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		//map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureJobs.PACKAGE_RESOURCES, "lang_en_query.properties"));
		return map;
	}
	
	/******************************************************************************
	 * 
	 ******************************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return 
			user.hasPermission(FeatureJobs.PERMISSION_JOBS_USER) 
		||  user.hasPermission(FeatureJobs.PERMISSION_JOBS_ADMIN);
	}
	
}
