package com.xresch.cfw.features.jobs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

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
	
	public enum JobFields{
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
	}
	
//	private static ArrayList<String> fullTextSearchColumns = new ArrayList<>();
//	static {
//		fullTextSearchColumns.add(PersonFields.PK_ID.toString());
//		fullTextSearchColumns.add(PersonFields.FIRSTNAME.toString());
//		fullTextSearchColumns.add(PersonFields.LASTNAME.toString());
//		fullTextSearchColumns.add(PersonFields.LOCATION.toString());
//		fullTextSearchColumns.add(PersonFields.EMAIL.toString());
//		fullTextSearchColumns.add(PersonFields.LIKES_TIRAMISU.toString());
//		fullTextSearchColumns.add(PersonFields.CHARACTER.toString());
//	}
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, JobFields.PK_ID)
								   .setPrimaryKeyAutoIncrement(this)
								   .setDescription("The job id.")
								   .apiFieldType(FormFieldType.NUMBER)
								   .setValue(null);
	
	private CFWField<Integer> foreignKeyOwner = CFWField.newInteger(FormFieldType.HIDDEN, JobFields.FK_ID_USER)
			.setForeignKeyCascade(this, User.class, UserFields.PK_ID)
			.setDescription("The user id of the owner of the job.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
		
	private CFWField<String> jobname = CFWField.newString(FormFieldType.TEXT, JobFields.JOB_NAME)
			.setColumnDefinition("VARCHAR(255)")
			.setDescription("The name of the job.")
			.addValidator(new LengthValidator(1, 255));
	
	private CFWField<String> description = CFWField.newString(FormFieldType.TEXTAREA, JobFields.DESCRIPTION)
			.setColumnDefinition("CLOB")
			.setDescription("The description of the job.")
			.addValidator(new LengthValidator(-1, 100000));
	
	private CFWField<String> taskName = CFWField.newString(FormFieldType.UNMODIFIABLE_TEXT, JobFields.TASK_NAME)
			.setColumnDefinition("VARCHAR(1024)")
			.setDescription("The name of the task to be executed.")
			.addValidator(new LengthValidator(3, 1024));
	
	private CFWField<LinkedHashMap<String, String>> properties = CFWField.newValueLabel(JobFields.JSON_PROPERTIES)
			.setLabel("Properties")
			.setDescription("The properties of the job.");
	
	private CFWField<CFWSchedule> schedule = 
			CFWField.newSchedule("JSON_SCHEDULE")
			.setLabel("Schedule")
			.addValidator(new ScheduleValidator().setNullAllowed(false))
			.setValue(null);
	
	private CFWField<Boolean> isDeletable = CFWField.newBoolean(FormFieldType.NONE, JobFields.IS_DELETABLE)
			.setDescription("Flag to define if the job can be deleted or not.")
			.setValue(true);
	
	private CFWField<Boolean> isEnabled = CFWField.newBoolean(FormFieldType.BOOLEAN, JobFields.IS_ENABLED)
			.setDescription("Enable or disable the job.")
			.setValue(true);
	
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
				isEnabled,
				isDeletable
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
						JobFields.PK_ID.toString(), 
						JobFields.FK_ID_USER.toString(),
						JobFields.JOB_NAME.toString(),
						JobFields.TASK_NAME.toString(),

				};
		
		String[] outputFields = 
				new String[] {
						JobFields.PK_ID.toString(), 
						JobFields.FK_ID_USER.toString(),
						JobFields.JOB_NAME.toString(),
						JobFields.DESCRIPTION.toString(),
						JobFields.TASK_NAME.toString(),
						JobFields.JSON_PROPERTIES.toString(),
						JobFields.JSON_SCHEDULE.toString(),
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
					.build();

	}
	
	/**************************************************************************************
	 * Creates a Quartz Job Trigger for this Job.
	 **************************************************************************************/
	protected Trigger createJobTrigger() {

		//-----------------------------------
		// Create Trigger
		TriggerBuilder<Trigger> triggerBuilder = this.schedule().createQuartzTriggerBuilder();
						
		for(Entry<String, String> entry : this.properties().entrySet()){
			triggerBuilder.usingJobData(entry.getKey(), entry.getValue());
		}
		
		return triggerBuilder
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
	
	public LinkedHashMap<String, String> properties() {
		return properties.getValue();
	}
	
	public CFWJob properties(LinkedHashMap<String, String> value) {
		this.properties.setValue(value);
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
	
}
