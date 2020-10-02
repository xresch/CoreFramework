package com.xresch.cfw.features.api;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.api.APIToken.APITokenFields;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2020
 * @license MIT-License
 **************************************************************************************************************/
public class APITokenPermissionMap extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_APITOKEN_PERMISSION_MAP";
	
	enum APITokenPermissionMapFields{
		PK_ID, 
		FK_ID_PERMISSION,
		FK_ID_TOKEN,
	}

	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, APITokenPermissionMapFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the mapping.")
			.apiFieldType(FormFieldType.NUMBER);
		
	private CFWField<Integer> foreignKeyToken = CFWField.newInteger(FormFieldType.HIDDEN, APITokenPermissionMapFields.FK_ID_TOKEN)
			.setForeignKeyCascade(this, APIToken.class, APITokenFields.PK_ID)
			.setDescription("The id of the token.")
			.apiFieldType(FormFieldType.NUMBER);
	
	private CFWField<Integer> foreignKeyPermission = CFWField.newInteger(FormFieldType.HIDDEN, APITokenPermissionMapFields.FK_ID_PERMISSION)
			.setForeignKeyCascade(this, APITokenPermission.class, APITokenPermissionMapFields.PK_ID)
			.setDescription("The id of the permission.")
			.apiFieldType(FormFieldType.NUMBER);

	
	public APITokenPermissionMap() {
		initializeFields();
	}
	
	public APITokenPermissionMap(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignKeyToken, foreignKeyPermission);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
				
		String[] inputFields = 
				new String[] {
						APITokenPermissionMapFields.PK_ID.toString(), 
						APITokenPermissionMapFields.FK_ID_PERMISSION.toString(),
						APITokenPermissionMapFields.FK_ID_TOKEN.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						APITokenPermissionMapFields.PK_ID.toString(), 
						APITokenPermissionMapFields.FK_ID_PERMISSION.toString(),
						APITokenPermissionMapFields.FK_ID_TOKEN.toString(),
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
	
	public APITokenPermissionMap id(Integer id) {
		this.id.setValue(id);
		return this;
	}

	public Integer foreignKeyAPIToken() {
		return foreignKeyToken.getValue();
	}
	
	public APITokenPermissionMap foreignKeyAPIToken(Integer foreignKeyAPIToken) {
		this.foreignKeyToken.setValue(foreignKeyAPIToken);
		return this;
	}	
	

	public Integer foreignKeyPermission() {
		return foreignKeyPermission.getValue();
	}
	
	public APITokenPermissionMap foreignKeyPermission(Integer foreignKeyPermission) {
		this.foreignKeyPermission.setValue(foreignKeyPermission);
		return this;
	}	
	
}
