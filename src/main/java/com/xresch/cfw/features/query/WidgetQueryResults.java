package com.xresch.cfw.features.query;

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

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
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
import com.xresch.cfw.features.dashboard.CFWJobTaskWidgetTaskExecutor;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.FeatureDashboard;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.dashboard.widgets.WidgetSettingsFactory;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject.AlertType;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWState;
import com.xresch.cfw.utils.CFWState.CFWStateOption;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023
 * @license MIT-License
 **************************************************************************************************************/
public class WidgetQueryResults extends WidgetDefinition {

	private static final String FIELDNAME_QUERY = "query";
	private static Logger logger = CFWLog.getLogger(WidgetQueryResults.class.getName());
	
	private static final String FIELDNAME_DETAILFIELDS = "detailfields";
	private static final String FIELDNAME_LABELFIELDS = "labelfields";
	private static final String FIELDNAME_VALUEFIELD = "valuefield";
	private static final String FIELDNAME_URLFIELD = "urlfield";
	private static final String FIELDNAME_ALERT_THRESHOLD = "ALERT_THRESHOLD";
	
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
	public String widgetName() { return "Display Query Results"; }
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL, "widget_"+getWidgetType()+".html");
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
						(CFWField)CFWField.newString(FormFieldType.QUERY_EDITOR, FIELDNAME_QUERY)
						.setLabel("{!cfw_widget_queryresults_query!}")
						.setDescription("{!cfw_widget_queryresults_query_desc!}")
						.disableSanitization()
						.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
						//.addCssClass("textarea-nowrap")	
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

		
		//----------------------------
		// Get Query String
		String queryString = (String)settingsWithParams.getField(FIELDNAME_QUERY).getValue();
		if(Strings.isNullOrEmpty(queryString)) {
			return true;
		}
				
		CFWQueryContext baseQueryContext = new CFWQueryContext();
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
			response.setPayLoad(createSampleData());
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
		// Resolve Parameters
		String dashboardParamsString = request.getParameter("params");
		
		JsonObject queryParams = new JsonObject();
		if(!Strings.isNullOrEmpty(dashboardParamsString)) {
			JsonArray boardParams = new JsonArray();
			boardParams = CFW.JSON.stringToJsonElement(dashboardParamsString).getAsJsonArray();
			
			boardParams.forEach(new Consumer<JsonElement>() {

				@Override
				public void accept(JsonElement element) {
					JsonObject object = element.getAsJsonObject();
					queryParams.add(object.get("NAME").getAsString(), object.get("VALUE"));
				}
			});
		}
		
		//---------------------------------
		// Fetch Data, do not check permissions
		// to allow dashboard viewers to see data
		
		CFWQueryExecutor executor = new CFWQueryExecutor().checkPermissions(false);
		CFWQueryResultList resultList = executor.parseAndExecuteAll(query, timeframe, queryParams);
				
		response.setPayLoad(resultList.toJson());	
	}
	
	/******************************************************************************
	 * 
	 ******************************************************************************/
	public JsonArray createSampleData() { 

		return CFW.JSON.stringToJsonElement(
			CFW.Files.readPackageResource(FeatureQuery.PACKAGE_RESOURCES, "cfw_widget_queryresults_sample.json")
		).getAsJsonArray();
		
	}
	
	/******************************************************************************
	 * 
	 ******************************************************************************/
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add(  new FileDefinition(HandlingType.JAR_RESOURCE, FeatureQuery.PACKAGE_RESOURCES, "cfw_query_editor.js") );
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
		return array;
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
		return "Checks if any of the selected values returned by the SQL query exceeds the selected threshold.";
	}
	
	/************************************************************
	 * Override this method and return a CFWObject containing 
	 * fields for the task parameters. The settings will be passed 
	 * to the 
	 * Always return a new instance, do not reuse a CFWObject.
	 * @return CFWObject
	 ************************************************************/
	public CFWObject getTasksParameters() {
		
		return new CFWJobsAlertObject()
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
						  , CFWTimeframe offset) throws JobExecutionException {
		
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
		Boolean isSampleData = (Boolean)widgetSettings.getField(WidgetSettingsFactory.FIELDNAME_SAMPLEDATA).getValue();
		if(isSampleData != null && isSampleData) {
			resultArray = createSampleData();
		}else {
			String queryString = (String)widgetSettings.getField(FIELDNAME_QUERY).getValue();
			if(Strings.isNullOrEmpty(queryString)) {
				return;
			}
			
			CFWQueryExecutor executor = new CFWQueryExecutor().checkPermissions(false);
			CFWQueryResultList resultList = executor.parseAndExecuteAll(queryString, timeframe);
					
			resultArray = resultList.toJsonRecords();	
		}
		
		if(resultArray == null || resultArray.size() == 0) {
			return;
		}
		
		//----------------------------------------
		// Set Column default settings 
		JsonObject object = resultArray.get(0).getAsJsonObject();
		Set<String> fields = object.keySet();
		boolean detailsEmpty = Strings.isNullOrEmpty(detailFields);
		detailFields = (detailsEmpty) ? "" : detailFields;
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
		JsonArray instantExceedingThreshold = new JsonArray();
		
		for(JsonElement element : resultArray) {
			
			JsonObject current = element.getAsJsonObject();
			Float value = current.get(valueField).getAsFloat();

			CFWStateOption condition = CFW.Conditions.getConditionForValue(value, taskParams);
			if(condition != null 
			&& CFW.Conditions.compareIsEqualsOrMoreDangerous(alertThreshholdCondition, condition)) {
				conditionMatched = true;
				instantExceedingThreshold.add(current);
			}
		}
				
		//----------------------------------------
		// Handle Alerting
		CFWJobsAlertObject alertObject = new CFWJobsAlertObject(context, this.getWidgetType());

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
				String message = CFW.Random.randomIssueResolvedMessage();
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
					tableHeader += "<td>"+fieldname+"</td>";
				}
				tableHeader += "</tr>";
				//----------------------------------------
				// Create Job List 
				String metricListText = "";
				String metricTableHTML = "<table class=\"table table-sm table-striped\" border=1 cellspacing=0 cellpadding=5 >"+tableHeader;
				
				Iterator<JsonElement> iter = instantExceedingThreshold.iterator();
				while(iter.hasNext()) {
					JsonObject current = iter.next().getAsJsonObject();
					//-----------------------------
					// Create Label String
					String labelString = "";
					
					for (String fieldname : labelFields.split(" *, *")) {
						JsonElement labelField = current.get(fieldname.trim());
						if(labelField != null && !labelField.isJsonNull()) {
							labelString += current.get(fieldname.trim()).getAsString() + " ";
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
						String detailsString = "";
						for (String fieldname : detailFields.split(",")) {
							if(fieldname != null && (urlColumn == null || !fieldname.trim().equals(urlColumn.trim())) ) {
								JsonElement detailsField = current.get(fieldname.trim());
								if(detailsField != null && !detailsField.isJsonNull()) {
									detailsString += fieldname+"=\""+current.get(fieldname.trim()).getAsString() + "\" ";
									metricTableHTML += "<td>"
											+CFW.Security.escapeHTMLEntities(detailsField.getAsString())
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
				
				alertObject.addTextData("data", "csv", CFW.JSON.formatJsonArrayToCSV(instantExceedingThreshold, ";") );
				alertObject.addTextData("data", "json", CFW.JSON.toJSONPretty(instantExceedingThreshold) );
				alertObject.doSendAlert(context, MessageType.ERROR, "Alert - Query record(s) reached threshold", messagePlaintext, messageHTML);
				
			}
			
		}
	}

}
