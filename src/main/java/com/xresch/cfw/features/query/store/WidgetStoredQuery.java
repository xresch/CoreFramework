package com.xresch.cfw.features.query.store;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.dashboard.CFWJobTaskWidgetTaskExecutor;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.FeatureDashboard;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.dashboard.widgets.WidgetSettingsFactory;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject.AlertType;
import com.xresch.cfw.features.parameter.CFWParameter;
import com.xresch.cfw.features.parameter.CFWParameter.CFWParameterFields;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryExecutor;
import com.xresch.cfw.features.query.CFWQueryResultList;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.utils.CFWMonitor;
import com.xresch.cfw.utils.CFWState;
import com.xresch.cfw.utils.CFWState.CFWStateOption;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023
 * @license MIT-License
 **************************************************************************************************************/
public class WidgetStoredQuery extends WidgetDefinition {

	private static final String MESSAGE_VALUE_NOT_NUMBER = "Value was not a number: ";
	private static final String FIELDNAME_QUERY = "query";
	private static Logger logger = CFWLog.getLogger(WidgetStoredQuery.class.getName());
	
	private static final String FIELDNAME_QUERY_EXTENSION = "query_extension";
	
	private static final String FIELDNAME_DETAILFIELDS = "detailfields";
	private static final String FIELDNAME_LABELFIELDS = "labelfields";
	private static final String FIELDNAME_VALUEFIELD = "valuefield";
	private static final String FIELDNAME_URLFIELD = "urlfield";
	private static final String FIELDNAME_ALERT_THRESHOLD = "ALERT_THRESHOLD";
	
	private CFWStoredQuery storedQuery;
	/******************************************************************************
	 * 
	 ******************************************************************************/
	public WidgetStoredQuery(CFWStoredQuery storedQuery) {
		this.storedQuery = storedQuery;
	}
	
	/******************************************************************************
	 * 
	 ******************************************************************************/
	@Override
	public String getWidgetType() {return "cfw_storedquery-"+storedQuery.id(); }
	
	/******************************************************************************
	 * 
	 ******************************************************************************/
	@Override
	public WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCachePolicy.TIME_BASED;
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
	public String widgetName() { return storedQuery.name(); }
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String descriptionHTML() {
		return storedQuery.description();
	}
	
	/******************************************************************************
	 * 
	 ******************************************************************************/
	@SuppressWarnings("rawtypes")
	@Override
	public CFWObject getSettings() {
		
		ArrayList<CFWParameter> parameterList = CFW.DB.Parameters.getParametersForQuery(storedQuery.id());

		CFWObject settingsObject = new CFWObject() ;
				
		//--------------------------------------
		// Add parameter fields to form
		CFWParameter.prepareParamObjectsForForm(null, parameterList, null, true);
		
		for(CFWParameter param : parameterList) {
			
			
			CFWField valueField = param.getField(CFWParameterFields.VALUE.toString());
			valueField
				//.addAttribute("data-settingslabel", param.paramSettingsLabel())
				.setName(param.name())
				.setLabel(CFW.Utils.Text.fieldNameToLabel(param.name()))
				.setDescription(param.description())
				//.isDecoratorDisplayed(true)
				//.addCssClass(" form-control-sm cfw-widget-parameter-marker")
				;

			settingsObject.addField(valueField);
		}
		
		settingsObject.addField(
				CFWField.newString(FormFieldType.QUERY_EDITOR, FIELDNAME_QUERY_EXTENSION)
						.setDescription("This will be appended to the query that is executed in the background. Useful for psot production steps like filtering etc...")
			);
		
		return settingsObject;
		
	}
	
	/******************************************************************************
	 * 
	 ******************************************************************************/
	@Override
	public boolean canSave(HttpServletRequest request, JSONResponse response, CFWObject settings, CFWObject settingsWithParams) {

		//----------------------------
		// Get Query String
		String queryString = storedQuery.query();
		if(Strings.isNullOrEmpty(queryString)) {
			return true;
		}
				
		//----------------------------
		// Get Query String
		String queryExtension = ((CFWField<String>)settings.getField(FIELDNAME_QUERY_EXTENSION)).getValue();
		
		System.out.println("queryExtension: "+queryExtension);
		if( ! Strings.isNullOrEmpty(queryExtension) ) {
			queryString += queryExtension;
		}
		
		//----------------------------
		// Check is Parsable & Permissions
		CFWQueryContext baseQueryContext = new CFWQueryContext();
		baseQueryContext.checkPermissions(true);
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
	@SuppressWarnings("unchecked")
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings
			, CFWTimeframe timeframe) { 
		
		//---------------------------------
		// Resolve Query
//		JsonElement queryElement = jsonSettings.get(FIELDNAME_QUERY);
//		if(queryElement == null || queryElement.isJsonNull()) {
//			return;
//		}
		
		String query = storedQuery.query();
		
		//----------------------------
		// Get Query String

		String queryExtension = ((CFWField<String>)settings.getField(FIELDNAME_QUERY_EXTENSION)).getValue();
		
		if( ! Strings.isNullOrEmpty(queryExtension) ) {
			query += queryExtension;
		}
		
		//---------------------------------
		// Resolve Parameters
		JsonObject queryParams = jsonSettings;
		
		//---------------------------------
		// Fetch Data, do not check permissions
		// to allow dashboard viewers to see data
		
		CFWQueryExecutor executor = new CFWQueryExecutor().checkPermissions(false);
		CFWQueryResultList resultList = executor.parseAndExecuteAll(query, timeframe, queryParams);
				
		response.setPayload(resultList.toJson());	
	}
	
	
	/******************************************************************************
	 * 
	 ******************************************************************************/
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		
		//----------------------------------
		// Register Main Javascript
		array.add(new FileDefinition(HandlingType.JAR_RESOURCE, FeatureStoredQuery.PACKAGE_RESOURCES, "cfw_query_stored_custom_widget.js") );
		
		//----------------------------------
		// Register Widget
		
		String category = 
				CFW.Security.escapeHTMLEntities(
						MoreObjects.firstNonNull(storedQuery.widgetCategory(), "")
					);
		
		if( Strings.isNullOrEmpty(category) ) {
			category = "Custom";
		}else {
			category = "Custom | " + category;
			String javascriptRegisterCategory =  
					CFW.Files.readPackageResource( 
								FeatureStoredQuery.PACKAGE_RESOURCES
								, "cfw_query_stored_custom_widget_registeringCategory.js"
							)
							.replace("$category$", category )
						;
			
			
			array.add(new FileDefinition(javascriptRegisterCategory) );
		}
		//----------------------------------
		// Register Widget
		String queryName = 
				CFW.Security.escapeHTMLEntities(
						MoreObjects.firstNonNull(storedQuery.name(), "")
					);
		String description = 
				CFW.Security.escapeHTMLEntities(
						MoreObjects.firstNonNull(storedQuery.description(), "")
					);
		
		String javascriptRegisterWidget =  
				CFW.Files.readPackageResource( 
							FeatureStoredQuery.PACKAGE_RESOURCES
							, "cfw_query_stored_custom_widget_registeringTemplate.js"
						)
						.replace("$storedQueryID$", ""+storedQuery.id())
						.replace("$queryName$", queryName)
						.replace("$category$", category )
						.replace("$description$", description )
					;
		
		
		array.add(new FileDefinition(javascriptRegisterWidget) );

		
		return array;
	}

	/******************************************************************************
	 * 
	 ******************************************************************************/
	@Override
	public ArrayList<FileDefinition> getCSSFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		//added globally array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureQuery.PACKAGE_RESOURCES, "cfw_query.css") );
		return array;
	}

	/******************************************************************************
	 * 
	 ******************************************************************************/
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		//map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureQuery.PACKAGE_RESOURCES, "lang_en_query.properties"));
		return map;
	}
	
	/******************************************************************************
	 * 
	 ******************************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return CFW.DB.StoredQuery.hasUserAccessToStoredQuery(storedQuery.id());
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
		return "Checks if any of the selected values returned by the query exceeds the selected threshold.";
	}
	
	/************************************************************
	 * Override this method and return a CFWObject containing 
	 * fields for the task parameters. The settings will be passed 
	 * to the 
	 * Always return a new instance, do not reuse a CFWObject.
	 * @return CFWObject
	 ************************************************************/
	public CFWObject getTasksParameters() {
		
		return new CFWJobsAlertObject(false)
				.addField(CFWField.newString(FormFieldType.TEXT, FIELDNAME_VALUEFIELD)
						.setLabel("{!cfw_widget_jobtask_valuefield!}")
						.setDescription("{!cfw_widget_jobtask_valuefield_desc!}")
				)
				
				.addField(CFWField.newString(FormFieldType.TEXT, FIELDNAME_LABELFIELDS)
						.setLabel("{!cfw_widget_jobtask_labelfields!}")
						.setDescription("{!cfw_widget_jobtask_labelfields_desc!}")
				)
				
				.addField(CFWField.newString(FormFieldType.TEXT, FIELDNAME_DETAILFIELDS)
						.setLabel("{!cfw_widget_jobtask_detailfields!}")
						.setDescription("{!cfw_widget_jobtask_detailfields_desc!}")
				)

				.addField(CFWField.newString(FormFieldType.TEXT, FIELDNAME_URLFIELD)
						.setLabel("{!cfw_widget_jobtask_urlfield!}")
						.setDescription("{!cfw_widget_jobtask_urlfield_desc!}")
				)
				
				//.addField(CFWJobTaskWidgetTaskExecutor.createOffsetMinutesField())
				
				.addAllFields(CFWState.createThresholdFields())
				
				.addField(
					CFWField.newString(FormFieldType.SELECT, FIELDNAME_ALERT_THRESHOLD)
					.setDescription("Select the threshhold that should trigger the alert when reached.")
					.addValidator(new NotNullOrEmptyValidator())
					.setOptions(CFW.Conditions.STATE_OPTIONS())
					.setValue(CFW.Conditions.STATE_ORANGE.toString())
				)
				;
	}
	
	/*************************************************************************
	 * Implement the actions your task should execute.
	 * See {@link com.xresch.cfw.features.jobs.CFWJobTask#executeTask CFWJobTask.executeTask()} to get
	 * more details on how to implement this method.
	 *************************************************************************/
	public void executeTask(JobExecutionContext context
						  , CFWObject taskParams
						  , DashboardWidget widget
						  , CFWObject widgetSettings
						  , CFWMonitor monitor, CFWTimeframe offset) throws JobExecutionException {
		
		//----------------------------------------
		// Get Params
		String valueField = (String)taskParams.getField(FIELDNAME_VALUEFIELD).getValue();
		String labelFields = (String)taskParams.getField(FIELDNAME_LABELFIELDS).getValue();
		String detailFields = (String)taskParams.getField(FIELDNAME_DETAILFIELDS).getValue();
		String urlColumn = (String)taskParams.getField(FIELDNAME_URLFIELD).getValue();
		
		CFWTimeframe timeframe = CFWJobTaskWidgetTaskExecutor.getOffsetFromJobSettings(taskParams);
		
		//----------------------------------------
		// Fetch Data
		JsonArray resultArray;

		String queryString = (String)widgetSettings.getField(FIELDNAME_QUERY).getValue();
		if(Strings.isNullOrEmpty(queryString)) {
			return;
		}
		
		CFWQueryExecutor executor = new CFWQueryExecutor().checkPermissions(false);
		CFWQueryResultList resultList = executor.parseAndExecuteAll(queryString, timeframe);
				
		resultArray = resultList.toJsonRecords();	
		
		//----------------------------------------
		// Set Column default settings 
		boolean detailsEmpty = Strings.isNullOrEmpty(detailFields);
		detailFields = (detailsEmpty) ? "" : detailFields;
		
		if(resultArray != null && resultArray.size() != 0) {
			
			JsonObject object = resultArray.get(0).getAsJsonObject();
			Set<String> fields = object.keySet();
			int i=0;
			for(String fieldname : fields) {
				
				//Set first column as default for label Column
				if(i == 0
				&& Strings.isNullOrEmpty(labelFields)) {
					labelFields = fieldname;
				}
				
				// Set all columns if details are empty
				if (detailsEmpty) {
					// ignore hidden fields with underscore
					if(!fieldname.startsWith("_")) {
						detailFields += fieldname +", ";
					}
				}
				
				//Set last column as default for value
				if(i == fields.size()-1
				&& Strings.isNullOrEmpty(valueField)) {
					valueField = fieldname;
				}
				
				i++;
			}
		}
		
		// Add Value Field if missing
		if(!detailFields.contains(valueField)) {
			detailFields = valueField+", "+detailFields;
		}
		
		if(detailFields.endsWith(", ")) {
			detailFields = detailFields.substring(0, detailFields.length()-2);
		}
		
		//----------------------------------------
		// Get alertThreshhold
		String alertThreshholdString = (String)taskParams.getField(FIELDNAME_ALERT_THRESHOLD).getValue();
		
		if(alertThreshholdString == null ) {
			return;
		}

		CFWStateOption alertThreshholdCondition = CFWStateOption.valueOf(alertThreshholdString);
		
		//----------------------------------------
		// Check Condition
		boolean conditionMatched = false;
		JsonArray recordsExceedingThreshold = new JsonArray();
		
		for(JsonElement element : resultArray) {
			
			JsonObject current = element.getAsJsonObject();
			JsonElement valueFieldElement = current.get(valueField);
			BigDecimal value = CFW.Math.BIG_NEG_ONE;
			
			//-------------------------------
			// Get Value 
			if(valueFieldElement != null) {
				if(valueFieldElement.isJsonPrimitive()) {
					JsonPrimitive primitive = valueFieldElement.getAsJsonPrimitive();
					if(primitive.isNumber()) {
						value = valueFieldElement.getAsBigDecimal();
					}else if(primitive.isString()) {
						 
						String maybeNumber = primitive.getAsString();
						
						try {
							value = new BigDecimal(maybeNumber);
						}catch(NumberFormatException e) {
							CFW.Messages.addWarningMessage(MESSAGE_VALUE_NOT_NUMBER + CFW.JSON.toString(valueFieldElement) );
						}
					}else {
						CFW.Messages.addWarningMessage(MESSAGE_VALUE_NOT_NUMBER + CFW.JSON.toString(valueFieldElement) );
					}
				}else {
					CFW.Messages.addWarningMessage(MESSAGE_VALUE_NOT_NUMBER + CFW.JSON.toString(valueFieldElement) );
					continue;
				}
			}

			CFWStateOption condition = CFW.Conditions.getConditionForValue(value, taskParams);
			if(condition != null 
			&& CFW.Conditions.compareIsEqualsOrMoreDangerous(alertThreshholdCondition, condition)) {
				conditionMatched = true;
				recordsExceedingThreshold.add(current);
			}
		}
				
		//----------------------------------------
		// Handle Alerting
		CFWJobsAlertObject alertObject = new CFWJobsAlertObject(context, this.getWidgetType(), false);

		alertObject.mapJobExecutionContext(context);

		AlertType type = alertObject.checkSendAlert(conditionMatched, null);
		
		if(!type.equals(AlertType.NONE)) {

			//----------------------------------------
			// Prepare Contents
			String widgetLinkHTML = "";
			if(widget != null) {
				widgetLinkHTML = widget.createWidgetOriginMessage();
			}
			
			//----------------------------------------
			// RESOLVE
			if(type.equals(AlertType.RESOLVE)) {
				String message = CFW.Random.issueResolvedMessage();
				String messageHTML = "<p>"+message+"</p>"+widgetLinkHTML;
				
				CFW.Messages.addSuccessMessage("Issue has resolved.");
				alertObject.doSendAlert(context, MessageType.SUCCESS, "Resolved - Query record(s) below threshold", message, messageHTML);
			}
			
			//----------------------------------------
			// RAISE
			if(type.equals(AlertType.RAISE)) {
				
				//----------------------------------------
				// Create Table Header
				String tableHeader = "<tr>";
				tableHeader = "<td>Item</td>";
				for (String fieldname : detailFields.split(",")) {
					
					if( urlColumn != null && fieldname.trim().equals(urlColumn.trim())) {
						continue;
					}
					
					tableHeader += "<td>"+fieldname+"</td>";
				}
				tableHeader += "</tr>";
				//----------------------------------------
				// Create Job List 
				String metricListText = "";
				String metricTableHTML = "<table class=\"table table-sm table-striped\" border=1 cellspacing=0 cellpadding=5 >"+tableHeader;
				
				Iterator<JsonElement> iter = recordsExceedingThreshold.iterator();
				while(iter.hasNext()) {
					JsonObject current = iter.next().getAsJsonObject();
					//-----------------------------
					// Create Label String
					String labelString = "";
					
					for (String fieldname : labelFields.split(" *, *")) {
						JsonElement labelField = current.get(fieldname.trim());
						if( labelField != null && !labelField.isJsonNull()) {
							labelString += CFW.JSON.toString(labelField) + " ";
						}else {
							labelString += " ";
						}
					}
					labelString = labelString.substring(0, labelString.length()-1);
					
					metricListText +=  labelString+" / ";
					
					//---------------------------------
					// Add Label as String and Link
					metricTableHTML += "<tr>";
					if( Strings.isNullOrEmpty(urlColumn) ){
						metricTableHTML += "<td><b>"+labelString+"</b></td>";
					}else {
						String url = current.get(urlColumn.trim()).getAsString();
						if(Strings.isNullOrEmpty(url)) {
							metricTableHTML += "<td><b>"+labelString+"</b></td>";
						}else {
							metricTableHTML += "<td><b><a target=\"_blank\" href=\""+url+"\">"+labelString+"</a></td>";
						}
					}
					
					//-----------------------------
					// Create Details
					if(!Strings.isNullOrEmpty(detailFields)) {
						//String detailsString = "";
						for (String fieldname : detailFields.trim().split(",")) {
							
							if( urlColumn != null && fieldname.trim().equals(urlColumn.trim())) {
								continue;
							}
							
							if( !Strings.isNullOrEmpty(fieldname)) {
								JsonElement detailsField = current.get(fieldname.trim());
								if(detailsField != null && !detailsField.isJsonNull()) {
									String detailsValue = CFW.JSON.toString(detailsField);
									//detailsString += fieldname+"=\""+ detailsValue + "\" ";
									metricTableHTML += "<td>"
											+CFW.Security.escapeHTMLEntities(detailsValue)
									+"</td>";
								}else {
									metricTableHTML += "<td>&nbsp;</td>";
								}
							}else {
								metricTableHTML += "<td>&nbsp;</td>";
							}
						}
					}
					
					metricTableHTML += "</tr>";
				}
				
				metricListText = metricListText.substring(0, metricListText.length()-3);
				metricTableHTML+="</table>";
				
				//----------------------------------------
				// Create Message
				String baseMessage = "The following record(s) have reached the threshold "+alertThreshholdString+":";
				String messagePlaintext = baseMessage+" "+metricListText;
				String messageHTML = widgetLinkHTML;
				messageHTML += "<p>"+baseMessage+"</p>";
				messageHTML += metricTableHTML;

				CFW.Messages.addErrorMessage(messagePlaintext);
				
				
				alertObject.addTextData("data", "csv", CFW.JSON.toCSV(recordsExceedingThreshold, ";") );
				alertObject.addTextData("data", "json", CFW.JSON.toJSONPretty(recordsExceedingThreshold) );
				alertObject.doSendAlert(context, MessageType.ERROR, "Alert - Query record(s) reached threshold", messagePlaintext, messageHTML);
				
			}
			
		}
	}

}
