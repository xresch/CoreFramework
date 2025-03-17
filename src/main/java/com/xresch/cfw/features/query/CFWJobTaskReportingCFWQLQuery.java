package com.xresch.cfw.features.query;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.CFWFieldFlag;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.config.FeatureConfig;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.features.dashboard.CFWJobTaskWidgetTaskExecutor;
import com.xresch.cfw.features.dashboard.widgets.WidgetSettingsFactory;
import com.xresch.cfw.features.jobs.CFWJobTask;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject;
import com.xresch.cfw.features.jobs.FeatureJobs;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject.AlertType;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.utils.CFWMonitor;
import com.xresch.cfw.utils.CFWState;
import com.xresch.cfw.utils.CFWState.CFWStateOption;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/	
public class CFWJobTaskReportingCFWQLQuery extends CFWJobTask {
	
	private WidgetQueryResults widget = new WidgetQueryResults();
	private static final String FIELDNAME_QUERY = "query";
	
	private static final String FIELDNAME_DETAILFIELDS = "detailfields";
	private static final String FIELDNAME_LABELFIELDS = "labelfields";
	private static final String FIELDNAME_VALUEFIELD = "valuefield";
	private static final String FIELDNAME_URLFIELD = "urlfield";
	private static final String FIELDNAME_CREATE_TABLE = "createtable";
	private static final String FIELDNAME_ATTACH_CSV = "attachcsv";
	private static final String FIELDNAME_ATTACH_HTML = "attachhtml";
	private static final String FIELDNAME_ATTACH_JSON = "attachjson";
	private static final String FIELDNAME_ATTACH_XML = "attachxml";
	
	/*****************************************************************************
	 * 
	 *****************************************************************************/
	@Override
	public String uniqueName() {
		return "Reporting: Query Results";
	}
	
	/*****************************************************************************
	 * 
	 *****************************************************************************/
	@Override
	public String taskDescription() {
		return "Executes a query and reports the data to the reporting channels of your choice.";
	}
	
	/*****************************************************************************
	 * 
	 *****************************************************************************/
	@Override
	public CFWObject getParameters() {
		return new CFWObject()
				
				.addField(CFWJobTaskWidgetTaskExecutor.createOffsetMinutesField())
				
				.addField(
						CFWField.newString(FormFieldType.QUERY_EDITOR, FIELDNAME_QUERY)
						.setLabel("{!cfw_widget_queryresults_query!}")
						.setDescription("{!cfw_widget_queryresults_query_desc!}")
						.disableSanitization() // Disable Security to not mess up Queries
						.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
						// validation is done using canSave() method in this class
						//.addValidator()
				)
				
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
				
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, FIELDNAME_CREATE_TABLE)
						.setLabel("{!cfw_widget_jobtask_create_table!}")
						.setDescription("{!cfw_widget_jobtask_create_table_desc!}")
						.setValue(true)
						)
				
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, FIELDNAME_ATTACH_CSV)
						.setLabel("{!cfw_widget_jobtask_attach_csv!}")
						.setDescription("{!cfw_widget_jobtask_attach_csv_desc!}")
						.setValue(true)
				)
				
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, FIELDNAME_ATTACH_HTML)
						.setLabel("{!cfw_widget_jobtask_attach_html!}")
						.setDescription("{!cfw_widget_jobtask_attach_html_desc!}")
						.setValue(false)
						)
				
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, FIELDNAME_ATTACH_JSON)
						.setLabel("{!cfw_widget_jobtask_attach_json!}")
						.setDescription("{!cfw_widget_jobtask_attach_json_desc!}")
						.setValue(false)
				)
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, FIELDNAME_ATTACH_XML)
						.setLabel("{!cfw_widget_jobtask_attach_xml!}")
						.setDescription("{!cfw_widget_jobtask_attach_xml_desc!}")
						.setValue(false)
				)
				.addAllFields(new CFWJobsAlertObject(true).getFields())

			;
	}
	
	/*****************************************************************************
	 * 
	 *****************************************************************************/
	@Override
	public int minIntervalSeconds() {
		return 15;
	}
	
	/*****************************************************************************
	 * 
	 *****************************************************************************/
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		
		return widget.getLocalizationFiles();
	}
	
	/*****************************************************************************
	 * 
	 *****************************************************************************/
	@Override
	public boolean hasPermission(User user) {
		
		if(
			(
				user.hasPermission(FeatureJobs.PERMISSION_JOBS_USER) 
			 && user.hasPermission(FeatureQuery.PERMISSION_QUERY_USER) 
			 )
		|| user.hasPermission(FeatureQuery.PERMISSION_QUERY_ADMIN) 
		) {
			return true;
		}
		
		return false;
	}
	
	/*****************************************************************************
	 * 
	 *****************************************************************************/
	@Override
	public void executeTask(JobExecutionContext context, CFWMonitor monitor) throws JobExecutionException {
		
		CFWObject taskParams = this.getParameters();
		taskParams.mapJobExecutionContext(context);
		
		//----------------------------------------
		// Get Params
		String valueField = (String)taskParams.getField(FIELDNAME_VALUEFIELD).getValue();
		String labelFields = (String)taskParams.getField(FIELDNAME_LABELFIELDS).getValue();
		String detailFields = (String)taskParams.getField(FIELDNAME_DETAILFIELDS).getValue();
		String urlColumn = (String)taskParams.getField(FIELDNAME_URLFIELD).getValue();
		Boolean createTable = (Boolean)taskParams.getField(FIELDNAME_CREATE_TABLE).getValue();
		Boolean attachCSV = (Boolean)taskParams.getField(FIELDNAME_ATTACH_CSV).getValue();
		Boolean attachHTML = (Boolean)taskParams.getField(FIELDNAME_ATTACH_HTML).getValue();
		Boolean attachJSON = (Boolean)taskParams.getField(FIELDNAME_ATTACH_JSON).getValue();
		Boolean attachXML = (Boolean)taskParams.getField(FIELDNAME_ATTACH_XML).getValue();
		
		CFWTimeframe timeframe = CFWJobTaskWidgetTaskExecutor.getOffsetFromJobSettings(taskParams);
		
		//----------------------------------------
		// Fetch Data
		JsonArray resultArray;

		String queryString = (String)taskParams.getField(FIELDNAME_QUERY).getValue();
		if(Strings.isNullOrEmpty(queryString)) {
			return;
		}
		
		CFWQueryExecutor executor = new CFWQueryExecutor().checkPermissions(false);
		CFWQueryResultList resultList = executor.parseAndExecuteAll(queryString, timeframe);
				
		resultArray = resultList.toJsonRecords();	
				
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
		// Prepare Contents
		String widgetLinkHTML = "";
		
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
		
		Iterator<JsonElement> iter = resultArray.iterator();
		
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
		// Prepare Message
		String baseMessage = "Your scheduled report contains "+resultArray.size()+" record(s)";
		String messagePlaintext = baseMessage+": "+metricListText;
		String messageHTML = widgetLinkHTML;
		messageHTML += "<p>"+baseMessage+".</p>";
		
		if(createTable) {
			messageHTML += metricTableHTML;
		}

		String defaultTitle =  "Job: Query Results Report";
		String menuTitle = CFW.DB.Config.getConfigAsString(FeatureConfig.CATEGORY_LOOK_AND_FEEL, FeatureConfig.CONFIG_MENU_TITLE);
		if( ! Strings.isNullOrEmpty(menuTitle) ) {
			defaultTitle = menuTitle + " - " + defaultTitle;
		}
		
		//----------------------------------------
		// Create Attachments
		CFWJobsAlertObject alertObject = new CFWJobsAlertObject(context, this.uniqueName(), true);
		alertObject.mapJobExecutionContext(context);
		
		if(attachCSV) {		alertObject.addTextData("data", "csv", CFW.JSON.toCSV(resultArray, ";") ); }
		if(attachHTML) {	alertObject.addTextData("data", "html", "<html><body>"+metricTableHTML+"</body></html>"); }
		if(attachJSON) {	alertObject.addTextData("data", "json", CFW.JSON.toJSONPretty(resultArray) ); }
		if(attachXML) {		alertObject.addTextData("data", "xml", CFW.JSON.toXML(resultArray, true) ); }
		
		//----------------------------------------
		// Send Report
		alertObject.doSendAlert(context, MessageType.INFO, "Job: Report Query Results", messagePlaintext, messageHTML);
		
	}
	
}
