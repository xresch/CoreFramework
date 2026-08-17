package com.xresch.cfw.features.spaces;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.spaces.CFWSpace.CFWSpaceFields;
import com.xresch.cfw.features.usermgmt.Role;
import com.xresch.cfw.features.usermgmt.Role.RoleFields;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license Org Manager License
 **************************************************************************************************************/
public class CFWSpaceAdminGroupsMap extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_SPACES_ADMINGROUPS_MAP";
	
	enum CFWSpaceAdminGroupsMapFields{
		  PK_ID 
		, FK_ID_ROLE
		, FK_ID_SPACE
	}

	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, CFWSpaceAdminGroupsMapFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the mapping.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
		
	private CFWField<Integer> foreignKeySpace = CFWField.newInteger(FormFieldType.HIDDEN, CFWSpaceAdminGroupsMapFields.FK_ID_SPACE)
			.setForeignKeyCascade(this, CFWSpace.class, CFWSpaceFields.PK_ID)
			.setDescription("The id of the space.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> foreignKeyRole = CFWField.newInteger(FormFieldType.HIDDEN, CFWSpaceAdminGroupsMapFields.FK_ID_ROLE)
			.setForeignKeyCascade(this, Role.class, RoleFields.PK_ID)
			.setDescription("The id of the role.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);

	
	public CFWSpaceAdminGroupsMap() {
		initializeFields();
	}
	
	public CFWSpaceAdminGroupsMap(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignKeySpace, foreignKeyRole);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
				
		String[] inputFields = 
				new String[] {
						CFWSpaceAdminGroupsMapFields.PK_ID.toString(), 
						CFWSpaceAdminGroupsMapFields.FK_ID_ROLE.toString(),
						CFWSpaceAdminGroupsMapFields.FK_ID_SPACE.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						CFWSpaceAdminGroupsMapFields.PK_ID.toString(), 
						CFWSpaceAdminGroupsMapFields.FK_ID_ROLE.toString(),
						CFWSpaceAdminGroupsMapFields.FK_ID_SPACE.toString(),
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

	public Integer foreignKeySpace() {
		return foreignKeySpace.getValue();
	}
	
	public CFWSpaceAdminGroupsMap foreignKeySpace(Integer value) {
		this.foreignKeySpace.setValue(value);
		return this;
	}	
	
	public Integer foreignKeyRole() {
		return foreignKeyRole.getValue();
	}
	
	public CFWSpaceAdminGroupsMap foreignKeyRole(Integer value) {
		this.foreignKeyRole.setValue(value);
		return this;
	}	
	

}
