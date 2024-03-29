package com.xresch.cfw.features.usermgmt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.usermgmt.Role.RoleFields;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class RolePermissionMap extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_ROLE_PERMISSION_MAP";
	
	enum RolePermissionMapFields{
		PK_ID, 
		FK_ID_PERMISSION,
		FK_ID_ROLE,
		IS_DELETABLE,
	}

	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, RolePermissionMapFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the mapping.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
		
	private CFWField<Integer> foreignKeyRole = CFWField.newInteger(FormFieldType.HIDDEN, RolePermissionMapFields.FK_ID_ROLE)
			.setForeignKeyCascade(this, Role.class, RoleFields.PK_ID)
			.setDescription("The id of the role.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> foreignKeyPermission = CFWField.newInteger(FormFieldType.HIDDEN, RolePermissionMapFields.FK_ID_PERMISSION)
			.setForeignKeyCascade(this, Permission.class, RolePermissionMapFields.PK_ID)
			.setDescription("The id of the permission.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Boolean> isDeletable = CFWField.newBoolean(FormFieldType.HIDDEN, RolePermissionMapFields.IS_DELETABLE)
			.setColumnDefinition("BOOLEAN")
			.setDescription("Flag to define if the mapping can be deleted or not.")
			.setValue(true);
	
	public RolePermissionMap() {
		initializeFields();
	}
	
	public RolePermissionMap(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignKeyRole, foreignKeyPermission,  isDeletable);
	}
	
	/**************************************************************************************
	 * Migrate Table
	 **************************************************************************************/
	@Override
	public void migrateTable() {
				
		//---------------------------
		// Rename Column 
		new CFWSQL(null).renameColumn("CFW_GROUP_PERMISSION_MAP", "FK_ID_GROUP",  RolePermissionMapFields.FK_ID_ROLE.toString());
		
		//---------------------------
		// Rename Foreign Key
		new CFWSQL(null).renameForeignKey("CFW_GROUP_PERMISSION_MAP",
				  "FK_ID_GROUP",
				  this.getTableName(),
				  RolePermissionMapFields.FK_ID_ROLE.toString());
		//---------------------------
		// Rename Foreign Key
		new CFWSQL(null).renameForeignKey("CFW_GROUP_PERMISSION_MAP",
									  RolePermissionMapFields.FK_ID_PERMISSION.toString(),
									  this.getTableName(),
									  RolePermissionMapFields.FK_ID_PERMISSION.toString());
		//---------------------------
		// Rename Table
		new CFWSQL(null).renameTable("CFW_GROUP_PERMISSION_MAP", this.getTableName());

		
	}
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
				
		String[] inputFields = 
				new String[] {
						RolePermissionMapFields.PK_ID.toString(), 
						RolePermissionMapFields.FK_ID_PERMISSION.toString(),
						RolePermissionMapFields.FK_ID_ROLE.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						RolePermissionMapFields.PK_ID.toString(), 
						RolePermissionMapFields.FK_ID_PERMISSION.toString(),
						RolePermissionMapFields.FK_ID_ROLE.toString(),
						RolePermissionMapFields.IS_DELETABLE.toString(),
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

	public Integer foreignKeyRole() {
		return foreignKeyRole.getValue();
	}
	
	public RolePermissionMap foreignKeyRole(Integer foreignKeyRole) {
		this.foreignKeyRole.setValue(foreignKeyRole);
		return this;
	}	
	

	public Integer foreignKeyPermission() {
		return foreignKeyPermission.getValue();
	}
	
	public RolePermissionMap foreignKeyPermission(Integer foreignKeyPermission) {
		this.foreignKeyPermission.setValue(foreignKeyPermission);
		return this;
	}	
	
	public boolean isDeletable() {
		return isDeletable.getValue();
	}
	
	public RolePermissionMap isDeletable(boolean isDeletable) {
		this.isDeletable.setValue(isDeletable);
		return this;
	}	
	
	
}
