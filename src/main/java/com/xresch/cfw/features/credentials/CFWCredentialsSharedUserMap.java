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
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.User.UserFields;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license Org Manager License
 **************************************************************************************************************/
public class CFWCredentialsSharedUserMap extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_CREDENTIALS_SHAREDUSER_MAP";
	
	enum CFWCredentialsSharedUserMapFields{
		  PK_ID 
		, FK_ID_USER
		, FK_ID_CREDENTIALS
	}

	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, CFWCredentialsSharedUserMapFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the mapping.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
		
	private CFWField<Integer> foreignCredentials = CFWField.newInteger(FormFieldType.HIDDEN, CFWCredentialsSharedUserMapFields.FK_ID_CREDENTIALS)
			.setForeignKeyCascade(this, CFWCredentials.class, CFWCredentialsFields.PK_ID)
			.setDescription("The id of the credentials.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> foreignKeyUser = CFWField.newInteger(FormFieldType.HIDDEN, CFWCredentialsSharedUserMapFields.FK_ID_USER)
			.setForeignKeyCascade(this, User.class, UserFields.PK_ID)
			.setDescription("The id of the user.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);

	
	public CFWCredentialsSharedUserMap() {
		initializeFields();
	}
	
	public CFWCredentialsSharedUserMap(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignCredentials, foreignKeyUser);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
				
		String[] inputFields = 
				new String[] {
						CFWCredentialsSharedUserMapFields.PK_ID.toString(), 
						CFWCredentialsSharedUserMapFields.FK_ID_USER.toString(),
						CFWCredentialsSharedUserMapFields.FK_ID_CREDENTIALS.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						CFWCredentialsSharedUserMapFields.PK_ID.toString(), 
						CFWCredentialsSharedUserMapFields.FK_ID_USER.toString(),
						CFWCredentialsSharedUserMapFields.FK_ID_CREDENTIALS.toString(),
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
	
	public CFWCredentialsSharedUserMap foreignCredentials(Integer value) {
		this.foreignCredentials.setValue(value);
		return this;
	}	
	
	public Integer foreignKeyUser() {
		return foreignKeyUser.getValue();
	}
	
	public CFWCredentialsSharedUserMap foreignKeyUser(Integer value) {
		this.foreignKeyUser.setValue(value);
		return this;
	}	
	

}
