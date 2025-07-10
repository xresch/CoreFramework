package com.xresch.cfw.features.filemanager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.filemanager.CFWStoredFile.CFWStoredFileFields;
import com.xresch.cfw.features.usermgmt.Role;
import com.xresch.cfw.features.usermgmt.Role.RoleFields;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license Org Manager License
 **************************************************************************************************************/
public class CFWStoredFileSharedGroupsMap extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_STOREDFILE_SHAREDGROUPS_MAP";
	
	enum CFWStoredFileSharedGroupsMapFields{
		  PK_ID 
		, FK_ID_ROLE
		, FK_ID_STOREDFILE
	}

	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, CFWStoredFileSharedGroupsMapFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the mapping.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
		
	private CFWField<Integer> foreignStoredFile = CFWField.newInteger(FormFieldType.HIDDEN, CFWStoredFileSharedGroupsMapFields.FK_ID_STOREDFILE)
			.setForeignKeyCascade(this, CFWStoredFile.class, CFWStoredFileFields.PK_ID)
			.setDescription("The id of the Stored File.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> foreignKeyRole = CFWField.newInteger(FormFieldType.HIDDEN, CFWStoredFileSharedGroupsMapFields.FK_ID_ROLE)
			.setForeignKeyCascade(this, Role.class, RoleFields.PK_ID)
			.setDescription("The id of the role.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);

	
	public CFWStoredFileSharedGroupsMap() {
		initializeFields();
	}
	
	public CFWStoredFileSharedGroupsMap(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignStoredFile, foreignKeyRole);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
				
		String[] inputFields = 
				new String[] {
						CFWStoredFileSharedGroupsMapFields.PK_ID.toString(), 
						CFWStoredFileSharedGroupsMapFields.FK_ID_ROLE.toString(),
						CFWStoredFileSharedGroupsMapFields.FK_ID_STOREDFILE.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						CFWStoredFileSharedGroupsMapFields.PK_ID.toString(), 
						CFWStoredFileSharedGroupsMapFields.FK_ID_ROLE.toString(),
						CFWStoredFileSharedGroupsMapFields.FK_ID_STOREDFILE.toString(),
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

	public Integer foreignStoredFile() {
		return foreignStoredFile.getValue();
	}
	
	public CFWStoredFileSharedGroupsMap foreignStoredFile(Integer value) {
		this.foreignStoredFile.setValue(value);
		return this;
	}	
	
	public Integer foreignKeyRole() {
		return foreignKeyRole.getValue();
	}
	
	public CFWStoredFileSharedGroupsMap foreignKeyRole(Integer value) {
		this.foreignKeyRole.setValue(value);
		return this;
	}	
	

}
