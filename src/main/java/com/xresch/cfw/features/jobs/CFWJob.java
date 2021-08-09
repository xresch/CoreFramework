package com.xresch.cfw.features.jobs;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWSchedule;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.User.UserFields;
import com.xresch.cfw.validation.LengthValidator;
import com.xresch.cfw.validation.ScheduleValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller
 **************************************************************************************************************/
public class CFWJob extends CFWObject {
	
	public static String TABLE_NAME = "CFW_JOB";
	
	public enum CFWJobFields{
		PK_ID, 
		FK_ID_USER,
		JOB_NAME, 
		DESCRIPTION,
		TASK_NAME,
		JSON_SCHEDULE,
		JSON_PROPERTIES,
		LAST_RUN,
		IS_ENABLED,
		IS_DELETABLE,
		CUSTOM_STRING,
		CUSTOM_INTEGER
	}
		
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, CFWJobFields.PK_ID)
								   .setPrimaryKeyAutoIncrement(this)
								   .setDescription("The job id.")
								   .apiFieldType(FormFieldType.NUMBER)
								   .setValue(null);
	
	private CFWField<Integer> foreignKeyOwner = CFWField.newInteger(FormFieldType.HIDDEN, CFWJobFields.FK_ID_USER)
			.setForeignKeyCascade(this, User.class, UserFields.PK_ID)
			.setDescription("The user id of the owner of the job.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
		
	private CFWField<String> jobname = CFWField.newString(FormFieldType.TEXT, CFWJobFields.JOB_NAME)
			.setColumnDefinition("VARCHAR(255)")
			.setDescription("The name of the job.")
			.addValidator(new LengthValidator(1, 255));
	
	private CFWField<String> description = CFWField.newString(FormFieldType.TEXTAREA, CFWJobFields.DESCRIPTION)
			.setColumnDefinition("CLOB")
			.setDescription("The description of the job.")
			.addValidator(new LengthValidator(-1, 100000));
	
	private CFWField<String> taskName = CFWField.newString(FormFieldType.UNMODIFIABLE_TEXT, CFWJobFields.TASK_NAME)
			.setColumnDefinition("VARCHAR(1024)")
			.setDescription("The name of the task to be executed.")
			.addValidator(new LengthValidator(3, 1024));
	
	/** Will only be used for storage. In forms will be changed to the custom fields defined by classes extending CFWJobTasks. */
	private CFWField<String> properties = CFWField.newString(FormFieldType.NONE, CFWJobFields.JSON_PROPERTIES)
			.setDescription("The properties of the job.");
	
	private CFWField<CFWSchedule> schedule = 
			CFWField.newSchedule(CFWJobFields.JSON_SCHEDULE)
			.setLabel("Schedule")
			.addValidator(new ScheduleValidator().setNullAllowed(false))
			.setValue(null);
	
	private CFWField<Timestamp> lastRun = CFWField.newTimestamp(FormFieldType.NONE, CFWJobFields.LAST_RUN)
			.setDescription("Time of the last run of the job.")
			.setValue(null);
	
	private CFWField<Boolean> isDeletable = CFWField.newBoolean(FormFieldType.NONE, CFWJobFields.IS_DELETABLE)
			.setDescription("Flag to define if the job can be deleted or not.")
			.setValue(true);
	
	private CFWField<Boolean> isEnabled = CFWField.newBoolean(FormFieldType.BOOLEAN, CFWJobFields.IS_ENABLED)
			.setDescription("Enable or disable the job.")
			.setValue(true);
	
	private CFWField<String> customString = CFWField.newString(FormFieldType.NONE, CFWJobFields.CUSTOM_STRING)
			.setDescription("Custom string value(e.g. to categorize jobs).");
	
	private CFWField<Integer> customInteger = CFWField.newInteger(FormFieldType.NONE, CFWJobFields.CUSTOM_INTEGER)
			.setDescription("Custom integer value(e.g. to add a forgein key).");
	
	public CFWJob() {
		initializeFields();
	}
	
		
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		
		//this.enableFulltextSearch(fullTextSearchColumns);
		
		this.addFields(id, 
				foreignKeyOwner,
				jobname, 
				description,
				taskName,
				schedule,
				properties,
				lastRun,
				isEnabled,
				isDeletable,
				customString,
				customInteger
				);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	public void initDB() {
	}
	
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
		
		String[] inputFields = 
				new String[] {
						CFWJobFields.PK_ID.toString(), 
						CFWJobFields.FK_ID_USER.toString(),
						CFWJobFields.JOB_NAME.toString(),
						CFWJobFields.TASK_NAME.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						CFWJobFields.PK_ID.toString(), 
						CFWJobFields.FK_ID_USER.toString(),
						CFWJobFields.JOB_NAME.toString(),
						CFWJobFields.DESCRIPTION.toString(),
						CFWJobFields.TASK_NAME.toString(),
						CFWJobFields.JSON_PROPERTIES.toString(),
						CFWJobFields.JSON_SCHEDULE.toString(),
						CFWJobFields.LAST_RUN.toString(),
						CFWJobFields.IS_ENABLED.toString(),
						CFWJobFields.IS_DELETABLE.toString(),
				};

		//----------------------------------
		// fetchJSON
		APIDefinitionFetch fetchDataAPI = 
				new APIDefinitionFetch(
						this.getClass(),
						this.getClass().getSimpleName(),
						"fetchData",
						inputFields,
						outputFields
				);
		
		apis.add(fetchDataAPI);
		
		return apis;
	}
	
	
	/**************************************************************************************
	 * Creates a Quartz Job Key for this Job using it's primary key(id).
	 **************************************************************************************/
	protected JobKey createJobKey() {
		 return new JobKey(""+this.id());
	}
	
	/**************************************************************************************
	 * Creates the Quartz Job Detail for this job.
	 **************************************************************************************/
	protected JobDetail createJobDetail() {
		//-----------------------------------
		// Create JobDetail from Task
		String taskname = this.taskName();
		Class<? extends CFWJobTask> taskClazz = CFW.Registry.Jobs.getTaskClass(taskname);
		
		return  JobBuilder
					.newJob(taskClazz)
					.withIdentity(this.createJobKey())
					.requestRecovery(true)
					.build();

	}
	
	/**************************************************************************************
	 * Creates a Quartz Job Trigger for this Job.
	 **************************************************************************************/
	protected Trigger createJobTrigger(JobDetail jobDetail) {

		//-----------------------------------
		// Create Trigger
		TriggerBuilder<Trigger> triggerBuilder = this.schedule().createQuartzTriggerBuilder();
		
		LinkedHashMap<String, String> map = this.propertiesAsMap();
				
		if(map != null) {
			for(Entry<String, String> entry : map.entrySet()){
				triggerBuilder.usingJobData(entry.getKey(), entry.getValue());
				triggerBuilder.usingJobData("test", "test");
			}
		}
		
		return triggerBuilder
				.forJob(jobDetail)
				.withIdentity("trigger(jobid:"+this.id()+")")
				.build();
		
	}


	
	public Integer id() {
		return id.getValue();
	}
	
	public CFWJob id(Integer id) {
		this.id.setValue(id);
		return this;
	}
	
	public Integer foreignKeyOwner() {
		return foreignKeyOwner.getValue();
	}
	
	public CFWJob foreignKeyOwner(Integer foreignKeyUser) {
		this.foreignKeyOwner.setValue(foreignKeyUser);
		return this;
	}
	
	public String jobname() {
		return jobname.getValue();
	}
	
	public CFWJob jobname(String value) {
		this.jobname.setValue(value);
		return this;
	}
	
	public Timestamp lastRun() {
		return lastRun.getValue();
	}
	
	public CFWJob lastRun(Timestamp value) {
		this.lastRun.setValue(value);
		return this;
	}
	
	public String executorDescription() {
		return description.getValue();
	}

	public CFWJob description(String description) {
		this.description.setValue(description);
		return this;
	}
	
	public String taskName() {
		return taskName.getValue();
	}
	
	public CFWJob taskName(String value) {
		this.taskName.setValue(value);
		return this;
	}
	
	public CFWSchedule schedule() {
		return this.schedule.getValue();
	}
	
	public CFWJob schedule(CFWSchedule value) {
		this.schedule.setValue(value);
		return this;
	}
	
	/***********************************************
	 * Returns a JSON String.
	 ***********************************************/
	public String properties() {
		return properties.getValue();
	}
	
	/***********************************************
	 * Returns a JSON Object or null.
	 ***********************************************/
	public LinkedHashMap<String, String> propertiesAsMap() {
		
		JsonObject object = CFW.JSON.stringToJsonObject(this.properties());
		return CFW.JSON.objectToMap(object);
	}
	
	
	public CFWJob properties(CFWObject propertiesObject) {
		this.properties.setValue(propertiesObject.toJSON());
		return this;
	}
	
	protected CFWJob changePropertiesDescription(String htmlDescription) {
		properties.setDescription(htmlDescription);
		return this;
	}
	
	public boolean isDeletable() {
		return isDeletable.getValue();
	}
	
	public CFWJob isDeletable(boolean value) {
		this.isDeletable.setValue(value);
		return this;
	}
	
	public boolean isEnabled() {
		return isEnabled.getValue();
	}
	
	public CFWJob isEnabled(boolean value) {
		this.isEnabled.setValue(value);
		return this;
	}
	
	public String customString() {
		return customString.getValue();
	}
	
	public CFWJob customString(String value) {
		this.customString.setValue(value);
		return this;
	}
	
	public Integer customInteger() {
		return customInteger.getValue();
	}
	
	public CFWJob customInteger(Integer value) {
		this.customInteger.setValue(value);
		return this;
	}
	
}
