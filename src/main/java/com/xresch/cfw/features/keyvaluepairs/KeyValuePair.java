package com.xresch.cfw.features.keyvaluepairs;

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
public class KeyValuePair extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_KEY_VALUE_PAIRS";
	
	public static final String CATEGORY_MIGRATION = "Migration";
	
	public enum KeyValuePairFields{
		PK_ID,
		CATEGORY,
		KEY,
		DESCRIPTION,
		VALUE
	}

	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, KeyValuePairFields.PK_ID.toString())
									.setPrimaryKeyAutoIncrement(this)
									.apiFieldType(FormFieldType.NUMBER)
									.setDescription("The id of the key value pair.")
									.setValue(null);
	
	private CFWField<String> category = CFWField.newString(FormFieldType.TEXT, KeyValuePairFields.CATEGORY.toString())
									.setColumnDefinition("VARCHAR(255)")
									.setDescription("The category of the key value pair.")
									.addValidator(new LengthValidator(1, 255))
									;
	
	private CFWField<String> key = CFWField.newString(FormFieldType.TEXT, KeyValuePairFields.KEY.toString())
									.setColumnDefinition("VARCHAR(255) UNIQUE")
									.setDescription("The key of the key value pair.")
									.addValidator(new LengthValidator(1, 255))
									;
	
	private CFWField<String> value = CFWField.newString(FormFieldType.TEXT, KeyValuePairFields.VALUE.toString())
			.setColumnDefinition("VARCHAR(4098)")
			.setDescription("The current value of the field. Can be null.")
			.addValidator(new LengthValidator(-1, 1024));
	
	private CFWField<String> description = CFWField.newString(FormFieldType.TEXTAREA, KeyValuePairFields.DESCRIPTION.toString())
											.setColumnDefinition("VARCHAR(4096)")
											.setDescription("The description of the key value pair.")
											.addValidator(new LengthValidator(-1, 4096));
	
	public KeyValuePair() {
		initialize();
	}
	
	public KeyValuePair(String category, String name) {
		initialize();
		this.category.setValue(category);
		this.key.setValue(name);
	}
	
	public KeyValuePair(ResultSet result) throws SQLException {
		initialize();
		this.mapResultSet(result);	
	}
	
	private void initialize() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, category, key, value, description);
	}
	
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
						
		String[] inputFields = 
				new String[] {
						KeyValuePairFields.PK_ID.toString(), 
						KeyValuePairFields.CATEGORY.toString(),
						KeyValuePairFields.KEY.toString(),
						KeyValuePairFields.VALUE.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						KeyValuePairFields.PK_ID.toString(), 
						KeyValuePairFields.CATEGORY.toString(),
						KeyValuePairFields.KEY.toString(),
						KeyValuePairFields.DESCRIPTION.toString(),
						KeyValuePairFields.VALUE.toString(),
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
	
	public KeyValuePair id(int id) {
		this.id.setValue(id);
		return this;
	}
	
	public String category() {
		return category.getValue();
	}
	
	public KeyValuePair category(String category) {
		this.category.setValue(category);
		return this;
	}
	
	public String key() {
		return key.getValue();
	}
	
	public KeyValuePair key(String key) {
		this.key.setValue(key);
		return this;
	}
	
	public String description() {
		return description.getValue();
	}

	public KeyValuePair description(String description) {
		this.description.setValue(description);
		return this;
	}

	public String value() {
		return value.getValue();
	}

	public KeyValuePair value(String value) {
		this.value.setValue(value);
		return this;
	}

	
	



	
	
}
