package com.xresch.cfw.features.config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.validation.LengthValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class Configuration extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_CONFIG";
	
	public enum ConfigFields{
		PK_ID,
		CATEGORY,
		NAME,
		DESCRIPTION,
		TYPE,
		VALUE,
		OPTIONS
	}

	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, ConfigFields.PK_ID.toString())
									.setPrimaryKeyAutoIncrement(this)
									.apiFieldType(FormFieldType.NUMBER)
									.setDescription("The id of the configuration.")
									.setValue(-999);
	
	private CFWField<String> category = CFWField.newString(FormFieldType.TEXT, ConfigFields.CATEGORY.toString())
									.setColumnDefinition("VARCHAR(255)")
									.setDescription("The category of the configuration.")
									.addValidator(new LengthValidator(1, 255))
									;
	
	private CFWField<String> name = CFWField.newString(FormFieldType.TEXT, ConfigFields.NAME.toString())
									.setColumnDefinition("VARCHAR(255) UNIQUE")
									.setDescription("The name of the configuration.")
									.addValidator(new LengthValidator(1, 255))
									;
	
	private CFWField<String> description = CFWField.newString(FormFieldType.TEXTAREA, ConfigFields.DESCRIPTION.toString())
											.setColumnDefinition("VARCHAR(4096)")
											.setDescription("A description of the configuration.")
											.addValidator(new LengthValidator(-1, 4096));
	
	private CFWField<String> type = CFWField.newString(FormFieldType.SELECT, ConfigFields.TYPE.toString())
			.setColumnDefinition("VARCHAR(32)")
			.setDescription("The form field type of the configuration.")
			.setOptions(FormFieldType.values())
			.addValidator(new LengthValidator(1, 32));
	
	private CFWField<String> value = CFWField.newString(FormFieldType.TEXT, ConfigFields.VALUE.toString())
			.setColumnDefinition("VARCHAR(1024)")
			.setDescription("The current value of the field. Can be null.")
			.addValidator(new LengthValidator(-1, 1024));
	
	private CFWField<Object[]> options = CFWField.newArray(FormFieldType.NONE, ConfigFields.OPTIONS.toString())
			.setColumnDefinition("ARRAY")
			.setDescription("The options available for the configuration(optional field).");
	
	
	public Configuration() {
		initialize();
	}
	
	public Configuration(String category, String name) {
		initialize();
		this.category.setValue(category);
		this.name.setValue(name);
	}
	
	public Configuration(ResultSet result) throws SQLException {
		initialize();
		this.mapResultSet(result);	
	}
	
	private void initialize() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, name, description, type, value, options, category);
	}
	
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
						
		String[] inputFields = 
				new String[] {
						ConfigFields.PK_ID.toString(), 
						ConfigFields.CATEGORY.toString(),
						ConfigFields.NAME.toString(),
						ConfigFields.TYPE.toString(),
						ConfigFields.VALUE.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						ConfigFields.PK_ID.toString(), 
						ConfigFields.CATEGORY.toString(),
						ConfigFields.NAME.toString(),
						ConfigFields.DESCRIPTION.toString(),
						ConfigFields.TYPE.toString(),
						ConfigFields.VALUE.toString(),
						ConfigFields.OPTIONS.toString(),
				};

		//----------------------------------
		// fetchData
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
	
	public int id() {
		return id.getValue();
	}
	
	public Configuration id(int id) {
		this.id.setValue(id);
		return this;
	}
	
	public String category() {
		return category.getValue();
	}
	
	public Configuration category(String category) {
		this.category.setValue(category);
		return this;
	}
	
	public String name() {
		return name.getValue();
	}
	
	public Configuration name(String name) {
		this.name.setValue(name);
		return this;
	}
	
	public String description() {
		return description.getValue();
	}

	public Configuration description(String description) {
		this.description.setValue(description);
		return this;
	}

	public String type() {
		return type.getValue();
	}

	public Configuration type(FormFieldType type) {
		this.type.setValue(type.toString());
		return this;
	}

	public String value() {
		return value.getValue();
	}

	public Configuration value(String value) {
		this.value.setValue(value);
		return this;
	}

	public Object[] options() {
		return options.getValue();
	}

	public Configuration options(Object[] options) {
		this.options.setValue(options);
		return this;
	}
	
	



	
	
}
