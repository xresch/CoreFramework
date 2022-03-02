package com.xresch.cfw.features.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.dashboard.WidgetDefinition;
import com.xresch.cfw.features.dashboard.WidgetSettingsFactory;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.validation.CustomValidator;

public class WidgetQueryResults extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(WidgetQueryResults.class.getName());
	@Override
	public String getWidgetType() {return "cfw_widget_queryresults";}
		
	@SuppressWarnings("rawtypes")
	@Override
	public CFWObject getSettings() {
		return new CFWObject()
								
				// Disable Security to not mess up Queries
				.addField(
						(CFWField)CFWField.newString(FormFieldType.TEXTAREA, "query")
						.setLabel("{!cfw_widget_queryresults_query!}")
						.setDescription("{!cfw_widget_queryresults_query_desc!}")
						.disableSanitization()
						.addValidator(new CustomValidator() {
							
							@Override
							public boolean validate(Object value) {
								// does not work when using parameters
//								String stringValue = (String)value;
//								
//								if(Strings.isNullOrEmpty(stringValue)) {
//									return true;
//									
//								}
//								try {
//									//--------------------------
//									// Make sure query is parsable
//									// and User has the appropriate rights.
//									CFWQueryParser parser = new CFWQueryParser((String)value, true);
//									parser.parse();
//									
//								}catch (Error | Exception e) {
//									CFW.Messages.addErrorMessage(e.getMessage());
//									return false;
//								}
								return true;
							}
						})
						.addCssClass("textarea-nowrap")	
						
				)
				.addField(WidgetSettingsFactory.createSampleDataField())
		;
	}

	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, long earliest, long latest) { 
		
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
		JsonElement queryElement = jsonSettings.get("query");
		if(queryElement == null || queryElement.isJsonNull()) {
			return;
		}
		
		String query = queryElement.getAsString();
		
		
		//---------------------------------
		// Fetch Data, do not check permissions
		// to allow dashboard viewers to see data
		
		CFWQueryExecutor executor = new CFWQueryExecutor().checkPermissions(false);
		
		JsonArray resultArray = executor.parseAndExecuteAll(query, earliest, latest);
		
		response.setPayLoad(resultArray);	
	}
	
	public void createSampleData(JSONResponse response) { 

		response.getContent().append(CFW.Files.readPackageResource(FeatureQuery.PACKAGE_RESOURCES, "cfw_widget_queryresults_sample.json") );
		
	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureQuery.PACKAGE_RESOURCES, "cfw_query_rendering.js") );
		array.add(  new FileDefinition(HandlingType.JAR_RESOURCE, FeatureQuery.PACKAGE_RESOURCES, "cfw_widget_queryresults.js") );
		return array;
	}

	@Override
	public ArrayList<FileDefinition> getCSSFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureQuery.PACKAGE_RESOURCES, "cfw_query.css") );
		return null;
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureQuery.PACKAGE_RESOURCES, "lang_en_query.properties"));
		return map;
	}
	
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureQuery.PERMISSION_QUERY_USER) ||  user.hasPermission(FeatureQuery.PERMISSION_QUERY_ADMIN);
	}

}
