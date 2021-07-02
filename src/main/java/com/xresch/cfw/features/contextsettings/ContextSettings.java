package com.xresch.cfw.features.contextsettings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.validation.LengthValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ContextSettings extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_CONTEXT_SETTINGS";
	
	public enum ContextSettingsFields{
		PK_ID,
		CFW_CTXSETTINGS_TYPE,
		CFW_CTXSETTINGS_NAME,
		CFW_CTXSETTINGS_DESCRIPTION,
		JSON_RESTRICTED_TO_USERS,
		JSON_RESTRICTED_TO_GROUPS,
		JSON_CTXSETTINGS
	}
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, ContextSettingsFields.PK_ID.toString())
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the context setting.")
			.apiFieldType(FormFieldType.NUMBER);
	
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
	
	private CFWField<LinkedHashMap<String,String>> restrictedToUsers = CFWField.newTagsSelector(ContextSettingsFields.JSON_RESTRICTED_TO_USERS)
			.setLabel("Restricted to Users")
			.setDescription("If at least one user or group is defined for access restriction, will only allow the specified users to access the setting. if none is specified, everybody has access.")
			.setValue(null)
			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
					return CFW.DB.Users.autocompleteUser(searchValue, this.getMaxResults());					
				}
			});
	
	private CFWField<LinkedHashMap<String,String>> restrictedToGroups = CFWField.newTagsSelector(ContextSettingsFields.JSON_RESTRICTED_TO_GROUPS)
			.setLabel("Restricted to Groups")
			.setDescription("If at least one user or group is defined for access restriction, will only allow the specified users to access the setting. if none is specified, everybody has access.")
			.setValue(null)
			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
					return CFW.DB.Roles.autocompleteGroup(searchValue, this.getMaxResults());					
				}
			});
	
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
		this.addFields(id, type, name, description, restrictedToUsers, restrictedToGroups, settings);
	}
		
	public void migrateTable(){
		CFWSQL.renameColumn(TABLE_NAME, "JSON_RESTRICTED_TO_ROLES", ContextSettingsFields.JSON_RESTRICTED_TO_GROUPS.toString());
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
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
						ContextSettingsFields.JSON_RESTRICTED_TO_USERS.toString(),	
						ContextSettingsFields.JSON_RESTRICTED_TO_GROUPS.toString(),	
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
	
	public LinkedHashMap<String,String> restrictedToUsers() {
		return restrictedToUsers.getValue();
	}
	
	public ContextSettings restrictedToUsers(LinkedHashMap<String,String> value) {
		this.restrictedToUsers.setValue(value);
		return this;
	}
	
	public LinkedHashMap<String,String> restrictedToGroups() {
		return restrictedToGroups.getValue();
	}
	
	public ContextSettings restrictedToGroups(LinkedHashMap<String,String> value) {
		this.restrictedToGroups.setValue(value);
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
