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
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.User.UserFields;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2026
 * @license MIT
 **************************************************************************************************************/
public class CFWSpaceEditorMap extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_SPACES_EDITOR_MAP";
	
	enum CFWSpaceEditorMapFields{
		PK_ID 
		, FK_ID_USER
		, FK_ID_SPACE
	}

	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, CFWSpaceEditorMapFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the mapping.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
		
	private CFWField<Integer> foreignKeySpace = CFWField.newInteger(FormFieldType.HIDDEN, CFWSpaceEditorMapFields.FK_ID_SPACE)
			.setForeignKeyCascade(this, CFWSpace.class, CFWSpaceFields.PK_ID)
			.setDescription("The id of the space.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> foreignKeyUser = CFWField.newInteger(FormFieldType.HIDDEN, CFWSpaceEditorMapFields.FK_ID_USER)
			.setForeignKeyCascade(this, User.class, UserFields.PK_ID)
			.setDescription("The id of the user that is allowed to cfw_spaces the space.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);

	
	public CFWSpaceEditorMap() {
		initializeFields();
	}
	
	public CFWSpaceEditorMap(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignKeySpace, foreignKeyUser);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
				
		String[] inputFields = 
				new String[] {
						CFWSpaceEditorMapFields.PK_ID.toString(), 
						CFWSpaceEditorMapFields.FK_ID_USER.toString(),
						CFWSpaceEditorMapFields.FK_ID_SPACE.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						CFWSpaceEditorMapFields.PK_ID.toString(), 
						CFWSpaceEditorMapFields.FK_ID_USER.toString(),
						CFWSpaceEditorMapFields.FK_ID_SPACE.toString(),
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
	
	public CFWSpaceEditorMap foreignKeySpace(Integer value) {
		this.foreignKeySpace.setValue(value);
		return this;
	}	
	
	public Integer foreignKeyUser() {
		return foreignKeyUser.getValue();
	}
	
	public CFWSpaceEditorMap foreignKeyUser(Integer value) {
		this.foreignKeyUser.setValue(value);
		return this;
	}	
	

}
