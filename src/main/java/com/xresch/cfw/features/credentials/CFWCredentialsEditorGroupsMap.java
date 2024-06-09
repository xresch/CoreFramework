package com.xresch.cfw.features.credentials;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.credentials.CFWCredentials.CFWCredentialsFields;
import com.xresch.cfw.features.usermgmt.Role;
import com.xresch.cfw.features.usermgmt.Role.RoleFields;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license Org Manager License
 **************************************************************************************************************/
public class CFWCredentialsEditorGroupsMap extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_DASHBOARD_EDITORGROUPS_MAP";
	
	enum CFWCredentialsEditorGroupsMapFields{
		  PK_ID 
		, FK_ID_ROLE
		, FK_ID_DASHBOARD
	}

	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, CFWCredentialsEditorGroupsMapFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the mapping.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
		
	private CFWField<Integer> foreignCredentials = CFWField.newInteger(FormFieldType.HIDDEN, CFWCredentialsEditorGroupsMapFields.FK_ID_DASHBOARD)
			.setForeignKeyCascade(this, CFWCredentials.class, CFWCredentialsFields.PK_ID)
			.setDescription("The id of the credentials.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> foreignKeyRole = CFWField.newInteger(FormFieldType.HIDDEN, CFWCredentialsEditorGroupsMapFields.FK_ID_ROLE)
			.setForeignKeyCascade(this, Role.class, RoleFields.PK_ID)
			.setDescription("The id of the role.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);

	
	public CFWCredentialsEditorGroupsMap() {
		initializeFields();
	}
	
	public CFWCredentialsEditorGroupsMap(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignCredentials, foreignKeyRole);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
				
		String[] inputFields = 
				new String[] {
						CFWCredentialsEditorGroupsMapFields.PK_ID.toString(), 
						CFWCredentialsEditorGroupsMapFields.FK_ID_ROLE.toString(),
						CFWCredentialsEditorGroupsMapFields.FK_ID_DASHBOARD.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						CFWCredentialsEditorGroupsMapFields.PK_ID.toString(), 
						CFWCredentialsEditorGroupsMapFields.FK_ID_ROLE.toString(),
						CFWCredentialsEditorGroupsMapFields.FK_ID_DASHBOARD.toString(),
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
	
	public Integer id() {
		return id.getValue();
	}

	public Integer foreignCredentials() {
		return foreignCredentials.getValue();
	}
	
	public CFWCredentialsEditorGroupsMap foreignCredentials(Integer value) {
		this.foreignCredentials.setValue(value);
		return this;
	}	
	
	public Integer foreignKeyRole() {
		return foreignKeyRole.getValue();
	}
	
	public CFWCredentialsEditorGroupsMap foreignKeyRole(Integer value) {
		this.foreignKeyRole.setValue(value);
		return this;
	}	
	

}
