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
public class CFWSpaceEditorGroupsMap extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_SPACES_EDITORGROUPS_MAP";
	
	enum CFWSpaceEditorGroupsMapFields{
		  PK_ID 
		, FK_ID_ROLE
		, FK_ID_SPACE
	}

	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, CFWSpaceEditorGroupsMapFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the mapping.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
		
	private CFWField<Integer> foreignKeySpace = CFWField.newInteger(FormFieldType.HIDDEN, CFWSpaceEditorGroupsMapFields.FK_ID_SPACE)
			.setForeignKeyCascade(this, CFWSpace.class, CFWSpaceFields.PK_ID)
			.setDescription("The id of the space.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> foreignKeyRole = CFWField.newInteger(FormFieldType.HIDDEN, CFWSpaceEditorGroupsMapFields.FK_ID_ROLE)
			.setForeignKeyCascade(this, Role.class, RoleFields.PK_ID)
			.setDescription("The id of the role.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);

	
	public CFWSpaceEditorGroupsMap() {
		initializeFields();
	}
	
	public CFWSpaceEditorGroupsMap(ResultSet result) throws SQLException {
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
						CFWSpaceEditorGroupsMapFields.PK_ID.toString(), 
						CFWSpaceEditorGroupsMapFields.FK_ID_ROLE.toString(),
						CFWSpaceEditorGroupsMapFields.FK_ID_SPACE.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						CFWSpaceEditorGroupsMapFields.PK_ID.toString(), 
						CFWSpaceEditorGroupsMapFields.FK_ID_ROLE.toString(),
						CFWSpaceEditorGroupsMapFields.FK_ID_SPACE.toString(),
				};

		//----------------------------------
		// fetchData
		APIDefinition fetchDataAPI = 
				new APIDefinitionFetch(
						this.getClass(),
						this.getClass().getSimpleName(),
						"fetchData",
						inputFields,
						outputFields
				).isSpaced(false);
		
		apis.add(fetchDataAPI);
		
		return apis;
	}
	
	public Integer id() {
		return id.getValue();
	}

	public Integer foreignKeySpace() {
		return foreignKeySpace.getValue();
	}
	
	public CFWSpaceEditorGroupsMap foreignKeySpace(Integer value) {
		this.foreignKeySpace.setValue(value);
		return this;
	}	
	
	public Integer foreignKeyRole() {
		return foreignKeyRole.getValue();
	}
	
	public CFWSpaceEditorGroupsMap foreignKeyRole(Integer value) {
		this.foreignKeyRole.setValue(value);
		return this;
	}	
	

}
