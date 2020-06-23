package com.pengtoolbox.cfw.features.usermgmt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.db.CFWSQL;
import com.pengtoolbox.cfw.features.api.APIDefinition;
import com.pengtoolbox.cfw.features.api.APIDefinitionFetch;
import com.pengtoolbox.cfw.features.usermgmt.Role.RoleFields;
import com.pengtoolbox.cfw.features.usermgmt.User.UserFields;
import com.pengtoolbox.cfw.datahandling.CFWObject;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class UserRoleMap extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_USER_ROLE_MAP";
	
	enum UserRoleMapFields{
		PK_ID, 
		FK_ID_USER,
		FK_ID_ROLE,
		IS_DELETABLE
	}

	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, UserRoleMapFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the mapping.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(-999);
	
	private CFWField<Integer> foreignKeyUser = CFWField.newInteger(FormFieldType.HIDDEN, UserRoleMapFields.FK_ID_USER)
			.setForeignKeyCascade(this, User.class, UserFields.PK_ID)
			.setDescription("The id of the user.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(-999);
	
	private CFWField<Integer> foreignKeyRole = CFWField.newInteger(FormFieldType.HIDDEN, UserRoleMapFields.FK_ID_ROLE)
			.setForeignKeyCascade(this, Role.class, RoleFields.PK_ID)
			.setDescription("The id of the role.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(-999);
	
	private CFWField<Boolean> isDeletable = CFWField.newBoolean(FormFieldType.HIDDEN, UserRoleMapFields.IS_DELETABLE)
			.setColumnDefinition("BOOLEAN")
			.setDescription("Flag to define if the mapping can be deleted or not.")
			.setValue(true);
	
	public UserRoleMap() {
		initializeFields();
	}
	
	public UserRoleMap(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignKeyUser, foreignKeyRole, isDeletable);
	}
	
	/**************************************************************************************
	 * Migrate Table
	 **************************************************************************************/
	public void migrateTable() {
		
		
		//###############################################
		// Migration from 2.0 to 2.1
		//###############################################
		
		//---------------------------
		// Rename Column 
		CFWSQL.renameColumn("CFW_USER_GROUP_MAP", "FK_ID_GROUP",  UserRoleMapFields.FK_ID_ROLE.toString());
		
		//---------------------------
		// Rename Foreign Key
		CFWSQL.renameForeignKey("CFW_USER_GROUP_MAP",
				  "FK_ID_GROUP",
				  this.getTableName(),
				  UserRoleMapFields.FK_ID_ROLE.toString());
		//---------------------------
		// Rename Foreign Key
		CFWSQL.renameForeignKey("CFW_USER_GROUP_MAP",
									  UserRoleMapFields.FK_ID_USER.toString(),
									  this.getTableName(),
									  UserRoleMapFields.FK_ID_USER.toString());
		//---------------------------
		// Rename Table
		CFWSQL.renameTable("CFW_USER_GROUP_MAP", this.getTableName());
			
	}
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
				
		String[] inputFields = 
				new String[] {
						UserRoleMapFields.PK_ID.toString(), 
						UserRoleMapFields.FK_ID_USER.toString(),
						UserRoleMapFields.FK_ID_ROLE.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						UserRoleMapFields.PK_ID.toString(), 
						UserRoleMapFields.FK_ID_USER.toString(),
						UserRoleMapFields.FK_ID_ROLE.toString(),
						UserRoleMapFields.IS_DELETABLE.toString(),
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

	public Integer foreignKeyUser() {
		return foreignKeyUser.getValue();
	}
	
	public UserRoleMap foreignKeyUser(Integer foreignKeyUser) {
		this.foreignKeyUser.setValue(foreignKeyUser);
		return this;
	}	
	
	public Integer foreignKeyRole() {
		return foreignKeyRole.getValue();
	}
	
	public UserRoleMap foreignKeyRole(Integer foreignKeyRole) {
		this.foreignKeyRole.setValue(foreignKeyRole);
		return this;
	}	
	
	
	public boolean isDeletable() {
		return isDeletable.getValue();
	}
	
	public UserRoleMap isDeletable(boolean isDeletable) {
		this.isDeletable.setValue(isDeletable);
		return this;
	}	
	
	
	
	
}
