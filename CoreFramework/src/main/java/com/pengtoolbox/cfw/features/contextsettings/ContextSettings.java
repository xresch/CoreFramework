package com.pengtoolbox.cfw.features.contextsettings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.features.api.APIDefinition;
import com.pengtoolbox.cfw.features.api.APIDefinitionFetch;
import com.pengtoolbox.cfw.validation.LengthValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class ContextSettings extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_CONTEXT_SETTINGS";
	
	public enum ContextSettingsFields{
		PK_ID,
		CFW_CTXSETTINGS_TYPE,
		CFW_CTXSETTINGS_NAME,
		CFW_CTXSETTINGS_DESCRIPTION,
		JSON_CTXSETTINGS
	}
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, ContextSettingsFields.PK_ID.toString())
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the context setting.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(-999);
	
	private CFWField<String> type = CFWField.newString(FormFieldType.HIDDEN, ContextSettingsFields.CFW_CTXSETTINGS_TYPE.toString())
			.setColumnDefinition("VARCHAR(255)")
			.setDescription("The type of the context setting.")
			.addValidator(new LengthValidator(1, 255));
	
	private CFWField<String> name = CFWField.newString(FormFieldType.TEXT, ContextSettingsFields.CFW_CTXSETTINGS_NAME.toString())
			.setLabel("Name")
			.setColumnDefinition("VARCHAR(255)")
			.setDescription("The name of the context setting.")
			.addValidator(new LengthValidator(1, 255));
	
	private CFWField<String> description = CFWField.newString(FormFieldType.TEXTAREA, ContextSettingsFields.CFW_CTXSETTINGS_DESCRIPTION)
			.setLabel("Description")
			.setDescription("The description of the context setting.")
			.addValidator(new LengthValidator(-1, 200000));
	
	private CFWField<String> settings = CFWField.newString(FormFieldType.NONE, ContextSettingsFields.JSON_CTXSETTINGS.toString())
			.setDescription("The custom settings of the environment as JSON.")
			.disableSecurity();
	
	public ContextSettings() {
		initializeFields();
	}
		
	public ContextSettings(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, type, name, description, settings);
	}
		
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
		
		String[] inputFields = 
				new String[] {
						ContextSettingsFields.PK_ID.toString(), 
						ContextSettingsFields.CFW_CTXSETTINGS_NAME.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						ContextSettingsFields.PK_ID.toString(), 
						ContextSettingsFields.CFW_CTXSETTINGS_TYPE.toString(),
						ContextSettingsFields.CFW_CTXSETTINGS_NAME.toString(),
						ContextSettingsFields.CFW_CTXSETTINGS_DESCRIPTION.toString(),	
						ContextSettingsFields.JSON_CTXSETTINGS.toString()
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
	
	public ContextSettings id(Integer id) {
		this.id.setValue(id);
		return this;
	}
	
	public String type() {
		return type.getValue();
	}
	
	public ContextSettings type(String type) {
		this.type.setValue(type);
		return this;
	}
	public String name() {
		return name.getValue();
	}
	
	public ContextSettings name(String name) {
		this.name.setValue(name);
		return this;
	}
	
	public String description() {
		return description.getValue();
	}

	public ContextSettings description(String description) {
		this.description.setValue(description);
		return this;
	}
	
	public String settings() {
		return settings.getValue();
	}
	
	public ContextSettings settings(String settings) {
		this.settings.setValue(settings);
		return this;
	}
	
}
