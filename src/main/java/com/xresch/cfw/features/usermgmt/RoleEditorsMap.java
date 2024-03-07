package com.xresch.cfw.features.usermgmt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.usermgmt.Role.RoleFields;
import com.xresch.cfw.features.usermgmt.User.UserFields;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license Org Manager License
 **************************************************************************************************************/
public class RoleEditorsMap extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_ROLE_EDITORS_MAP";
	
	enum RoleEditorsMapFields{
		  PK_ID 
		, FK_ID_USER
		, FK_ID_ROLE
	}

	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, RoleEditorsMapFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the mapping.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
		
	private CFWField<Integer> foreignRole = CFWField.newInteger(FormFieldType.HIDDEN, RoleEditorsMapFields.FK_ID_ROLE)
			.setForeignKeyCascade(this, Role.class, RoleFields.PK_ID)
			.setDescription("The id of the role.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> foreignKeyUser = CFWField.newInteger(FormFieldType.HIDDEN, RoleEditorsMapFields.FK_ID_USER)
			.setForeignKeyCascade(this, User.class, UserFields.PK_ID)
			.setDescription("The id of the user.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);

	
	public RoleEditorsMap() {
		initializeFields();
	}
	
	public RoleEditorsMap(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignRole, foreignKeyUser);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
				
		String[] inputFields = 
				new String[] {
						RoleEditorsMapFields.PK_ID.toString(), 
						RoleEditorsMapFields.FK_ID_USER.toString(),
						RoleEditorsMapFields.FK_ID_ROLE.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						RoleEditorsMapFields.PK_ID.toString(), 
						RoleEditorsMapFields.FK_ID_USER.toString(),
						RoleEditorsMapFields.FK_ID_ROLE.toString(),
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

	public Integer foreignRole() {
		return foreignRole.getValue();
	}
	
	public RoleEditorsMap foreignRole(Integer value) {
		this.foreignRole.setValue(value);
		return this;
	}	
	
	public Integer foreignKeyUser() {
		return foreignKeyUser.getValue();
	}
	
	public RoleEditorsMap foreignKeyUser(Integer value) {
		this.foreignKeyUser.setValue(value);
		return this;
	}	
	

}
