package com.xresch.cfw.features.query;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.CFWFieldFlag;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.dashboard.widgets.WidgetSettingsFactory;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023
 * @license MIT-License
 **************************************************************************************************************/
public class WidgetQueryResults extends WidgetDefinition {

	private static final String FIELDNAME_QUERY = "query";
	private static Logger logger = CFWLog.getLogger(WidgetQueryResults.class.getName());
	
	
	/******************************************************************************
	 * 
	 ******************************************************************************/
	@Override
	public String getWidgetType() {return "cfw_widget_queryresults";}
	
	/******************************************************************************
	 * 
	 ******************************************************************************/
	@Override
	public WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCachePolicy.TIME_BASED;
	}
	/******************************************************************************
	 * 
	 ******************************************************************************/
	@SuppressWarnings("rawtypes")
	@Override
	public CFWObject getSettings() {
		return new CFWObject()
								
				// Disable Security to not mess up Queries
				.addField(
						(CFWField)CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_QUERY)
						.setLabel("{!cfw_widget_queryresults_query!}")
						.setDescription("{!cfw_widget_queryresults_query_desc!}")
						.disableSanitization()
						.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
						.addCssClass("textarea-nowrap")	
						// validation is done using canSave() method in this class
						//.addValidator()
				)
				.addField(WidgetSettingsFactory.createSampleDataField())
		;
	}
	
	/******************************************************************************
	 * 
	 ******************************************************************************/
	@Override
	public boolean canSave(HttpServletRequest request, JSONResponse response, CFWObject settings, CFWObject settingsWithParams) {

		String queryString = (String)settingsWithParams.getField(FIELDNAME_QUERY).getValue();
		
		//----------------------------
		// Check isEmpty
		if(Strings.isNullOrEmpty(queryString)) {
			return true;
		}
		
		CFWQueryContext baseQueryContext = new CFWQueryContext();
		//baseQueryContext.setEarliest(earliest);
		//baseQueryContext.setLatest(latest);
		//baseQueryContext.setTimezoneOffsetMinutes(timezoneOffsetMinutes);
		baseQueryContext.checkPermissions(true);
		
		//----------------------------
		// Check is Parsable & Permissions
		CFWQueryParser parser = new CFWQueryParser(queryString, true, baseQueryContext, true);
		boolean canSave = true;
		try {
			parser.parse();
		}catch (NumberFormatException e) {
			new CFWLog(logger).severe("Error Parsing a number:"+e.getMessage(), e);
			canSave = false;
		} catch (ParseException e) {
			CFW.Messages.addErrorMessage(e.getMessage());
			canSave = false;
		}  catch (OutOfMemoryError e) {
			new CFWLog(logger).severe("Out of memory while parsing query. Please check your syntax.", e);
			canSave = false;
		} catch (IndexOutOfBoundsException e) {
			new CFWLog(logger).severe("Query Parsing: "+e.getMessage(), e);
			canSave = false;
		}catch (Exception e) {
			new CFWLog(logger).severe("Error when parsing the query: "+e.getMessage(), e);
			canSave = false;
		}
		
		return canSave;
	}

	/******************************************************************************
	 * 
	 ******************************************************************************/
	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings
			, CFWTimeframe timeframe) { 
				
		//---------------------------------
		// Example Data
		JsonElement sampleDataElement = jsonSettings.get("sampledata");
		
		if(sampleDataElement != null 
		&& !sampleDataElement.isJsonNull() 
		&& sampleDataElement.getAsBoolean()) {
			createSampleData(response);
			return;
		}
		
		//---------------------------------
		// Resolve Query
		JsonElement queryElement = jsonSettings.get(FIELDNAME_QUERY);
		if(queryElement == null || queryElement.isJsonNull()) {
			return;
		}
		
		String query = queryElement.getAsString();
		
		
		//---------------------------------
		// Fetch Data, do not check permissions
		// to allow dashboard viewers to see data
		
		CFWQueryExecutor executor = new CFWQueryExecutor().checkPermissions(false);
		CFWQueryResultList resultList = executor.parseAndExecuteAll(query, timeframe);
				
		response.setPayLoad(resultList.toJson());	
	}
	
	/******************************************************************************
	 * 
	 ******************************************************************************/
	public void createSampleData(JSONResponse response) { 

		response.getContent().append(CFW.Files.readPackageResource(FeatureQuery.PACKAGE_RESOURCES, "cfw_widget_queryresults_sample.json") );
		
	}
	
	/******************************************************************************
	 * 
	 ******************************************************************************/
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureQuery.PACKAGE_RESOURCES, "cfw_query_rendering.js") );
		array.add(  new FileDefinition(HandlingType.JAR_RESOURCE, FeatureQuery.PACKAGE_RESOURCES, "cfw_widget_queryresults.js") );
		return array;
	}

	/******************************************************************************
	 * 
	 ******************************************************************************/
	@Override
	public ArrayList<FileDefinition> getCSSFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureQuery.PACKAGE_RESOURCES, "cfw_query.css") );
		return null;
	}

	/******************************************************************************
	 * 
	 ******************************************************************************/
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureQuery.PACKAGE_RESOURCES, "lang_en_query.properties"));
		return map;
	}
	
	/******************************************************************************
	 * 
	 ******************************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return 
			user.hasPermission(FeatureQuery.PERMISSION_QUERY_USER) 
		||  user.hasPermission(FeatureQuery.PERMISSION_QUERY_ADMIN);
	}

}
