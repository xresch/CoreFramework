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
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.User.UserFields;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license Org Manager License
 **************************************************************************************************************/
public class CFWStoredFileSharedUserMap extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_STOREDFILE_SHAREDUSER_MAP";
	
	enum CFWStoredFileSharedUserMapFields{
		  PK_ID 
		, FK_ID_USER
		, FK_ID_STOREDFILE
	}

	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, CFWStoredFileSharedUserMapFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the mapping.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
		
	private CFWField<Integer> foreignStoredFile = CFWField.newInteger(FormFieldType.HIDDEN, CFWStoredFileSharedUserMapFields.FK_ID_STOREDFILE)
			.setForeignKeyCascade(this, CFWStoredFile.class, CFWStoredFileFields.PK_ID)
			.setDescription("The id of the Stored File.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> foreignKeyUser = CFWField.newInteger(FormFieldType.HIDDEN, CFWStoredFileSharedUserMapFields.FK_ID_USER)
			.setForeignKeyCascade(this, User.class, UserFields.PK_ID)
			.setDescription("The id of the user.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);

	
	public CFWStoredFileSharedUserMap() {
		initializeFields();
	}
	
	public CFWStoredFileSharedUserMap(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignStoredFile, foreignKeyUser);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
				
		String[] inputFields = 
				new String[] {
						CFWStoredFileSharedUserMapFields.PK_ID.toString(), 
						CFWStoredFileSharedUserMapFields.FK_ID_USER.toString(),
						CFWStoredFileSharedUserMapFields.FK_ID_STOREDFILE.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						CFWStoredFileSharedUserMapFields.PK_ID.toString(), 
						CFWStoredFileSharedUserMapFields.FK_ID_USER.toString(),
						CFWStoredFileSharedUserMapFields.FK_ID_STOREDFILE.toString(),
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
	
	public CFWStoredFileSharedUserMap foreignStoredFile(Integer value) {
		this.foreignStoredFile.setValue(value);
		return this;
	}	
	
	public Integer foreignKeyUser() {
		return foreignKeyUser.getValue();
	}
	
	public CFWStoredFileSharedUserMap foreignKeyUser(Integer value) {
		this.foreignKeyUser.setValue(value);
		return this;
	}	
	

}
