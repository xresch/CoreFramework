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
public class CFWCredentialsSharedGroupsMap extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_CREDENTIALS_SHAREDGROUPS_MAP";
	
	enum CFWCredentialsSharedGroupsMapFields{
		  PK_ID 
		, FK_ID_ROLE
		, FK_ID_CREDENTIALS
	}

	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, CFWCredentialsSharedGroupsMapFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the mapping.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
		
	private CFWField<Integer> foreignCredentials = CFWField.newInteger(FormFieldType.HIDDEN, CFWCredentialsSharedGroupsMapFields.FK_ID_CREDENTIALS)
			.setForeignKeyCascade(this, CFWCredentials.class, CFWCredentialsFields.PK_ID)
			.setDescription("The id of the credentials.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> foreignKeyRole = CFWField.newInteger(FormFieldType.HIDDEN, CFWCredentialsSharedGroupsMapFields.FK_ID_ROLE)
			.setForeignKeyCascade(this, Role.class, RoleFields.PK_ID)
			.setDescription("The id of the role.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);

	
	public CFWCredentialsSharedGroupsMap() {
		initializeFields();
	}
	
	public CFWCredentialsSharedGroupsMap(ResultSet result) throws SQLException {
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
						CFWCredentialsSharedGroupsMapFields.PK_ID.toString(), 
						CFWCredentialsSharedGroupsMapFields.FK_ID_ROLE.toString(),
						CFWCredentialsSharedGroupsMapFields.FK_ID_CREDENTIALS.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						CFWCredentialsSharedGroupsMapFields.PK_ID.toString(), 
						CFWCredentialsSharedGroupsMapFields.FK_ID_ROLE.toString(),
						CFWCredentialsSharedGroupsMapFields.FK_ID_CREDENTIALS.toString(),
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
	
	public CFWCredentialsSharedGroupsMap foreignCredentials(Integer value) {
		this.foreignCredentials.setValue(value);
		return this;
	}	
	
	public Integer foreignKeyRole() {
		return foreignKeyRole.getValue();
	}
	
	public CFWCredentialsSharedGroupsMap foreignKeyRole(Integer value) {
		this.foreignKeyRole.setValue(value);
		return this;
	}	
	

}
