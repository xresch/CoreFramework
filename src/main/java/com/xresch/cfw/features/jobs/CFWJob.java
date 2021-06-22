package com.xresch.cfw.features.jobs;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWSchedule;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.User.UserFields;
import com.xresch.cfw.validation.LengthValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller
 **************************************************************************************************************/
public class CFWJob extends CFWObject {
	
	public static String TABLE_NAME = "CFW_JOB";
	
	public enum JobFields{
		PK_ID, 
		FK_ID_USER,
		NAME, 
		DESCRIPTION,
		JOB_EXECUTOR_CLASS,
//		START_DATE,
//		END_DATE,
		JSON_SCHEDULE,
		JSON_PROPERTIES,
		IS_ACTIVE,
		IS_DELETEABLE,
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
	
	private CFWField<String> jobExecutorClassname = CFWField.newString(FormFieldType.NONE, JobFields.JOB_EXECUTOR_CLASS)
			.setColumnDefinition("VARCHAR(1024)")
			.setDescription("The class that will be executed.")
			.addValidator(new LengthValidator(3, 1024));
	
	private CFWField<String> name = CFWField.newString(FormFieldType.TEXT, JobFields.NAME)
			.setColumnDefinition("VARCHAR(255)")
			.setDescription("The name of the job.")
			.addValidator(new LengthValidator(1, 255));
	
	private CFWField<String> description = CFWField.newString(FormFieldType.TEXTAREA, JobFields.DESCRIPTION)
			.setColumnDefinition("CLOB")
			.setDescription("The description of the job.")
			.addValidator(new LengthValidator(-1, 100000));
	
	private CFWField<LinkedHashMap<String, String>> properties =  CFWField.newValueLabel(JobFields.JSON_PROPERTIES)
			.setDescription("The Properties of the job.");
	
	private CFWField<CFWSchedule> schedule = 
			CFWField.newSchedule("JSON_SCHEDULE")
			.setLabel("Schedule")
			.setValue(null);
	
	public CFWJob() {
		initializeFields();
	}
	
		
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		
		//this.enableFulltextSearch(fullTextSearchColumns);
		
		this.addFields(id, 
				foreignKeyOwner,
				jobExecutorClassname,
				name, 
				description,
				schedule,
				properties
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
						JobFields.NAME.toString(),
						JobFields.JOB_EXECUTOR_CLASS.toString(),

				};
		
		String[] outputFields = 
				new String[] {
						JobFields.PK_ID.toString(), 
						JobFields.FK_ID_USER.toString(),
						JobFields.NAME.toString(),
						JobFields.DESCRIPTION.toString(),
						JobFields.JOB_EXECUTOR_CLASS.toString(),

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
	
	public String name() {
		return name.getValue();
	}
	
	public CFWJob name(String value) {
		this.name.setValue(value);
		return this;
	}
	
	public String executorDescription() {
		return description.getValue();
	}

	public CFWJob description(String description) {
		this.description.setValue(description);
		return this;
	}
	
	public String jobExecutorClassname() {
		return jobExecutorClassname.getValue();
	}
	
	public CFWJob jobExecutorClassname(Class<CFWJobTask> value) {
		this.jobExecutorClassname.setValue(value.getName());
		return this;
	}
	
	public LinkedHashMap<String, String> properties() {
		return properties.getValue();
	}
	
	public CFWJob properties(LinkedHashMap<String, String> value) {
		this.properties.setValue(value);
		return this;
	}
	
}
