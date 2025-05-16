package com.xresch.cfw.features.query.store;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.query.store.CFWStoredQuery.CFWStoredQueryFields;
import com.xresch.cfw.features.usermgmt.Role;
import com.xresch.cfw.features.usermgmt.Role.RoleFields;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license Org Manager License
 **************************************************************************************************************/
public class CFWStoredQuerySharedGroupsMap extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_QUERY_STORED_SHAREDGROUPS_MAP";
	
	enum CFWStoredQuerySharedGroupsMapFields{
		  PK_ID 
		, FK_ID_ROLE
		, FK_ID_STOREDQUERY
	}

	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, CFWStoredQuerySharedGroupsMapFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the mapping.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
		
	private CFWField<Integer> foreignStoredQuery = CFWField.newInteger(FormFieldType.HIDDEN, CFWStoredQuerySharedGroupsMapFields.FK_ID_STOREDQUERY)
			.setForeignKeyCascade(this, CFWStoredQuery.class, CFWStoredQueryFields.PK_ID)
			.setDescription("The id of the storedQuery.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> foreignKeyRole = CFWField.newInteger(FormFieldType.HIDDEN, CFWStoredQuerySharedGroupsMapFields.FK_ID_ROLE)
			.setForeignKeyCascade(this, Role.class, RoleFields.PK_ID)
			.setDescription("The id of the role.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);

	
	public CFWStoredQuerySharedGroupsMap() {
		initializeFields();
	}
	
	public CFWStoredQuerySharedGroupsMap(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignStoredQuery, foreignKeyRole);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
				
		String[] inputFields = 
				new String[] {
						CFWStoredQuerySharedGroupsMapFields.PK_ID.toString(), 
						CFWStoredQuerySharedGroupsMapFields.FK_ID_ROLE.toString(),
						CFWStoredQuerySharedGroupsMapFields.FK_ID_STOREDQUERY.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						CFWStoredQuerySharedGroupsMapFields.PK_ID.toString(), 
						CFWStoredQuerySharedGroupsMapFields.FK_ID_ROLE.toString(),
						CFWStoredQuerySharedGroupsMapFields.FK_ID_STOREDQUERY.toString(),
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

	public Integer foreignStoredQuery() {
		return foreignStoredQuery.getValue();
	}
	
	public CFWStoredQuerySharedGroupsMap foreignStoredQuery(Integer value) {
		this.foreignStoredQuery.setValue(value);
		return this;
	}	
	
	public Integer foreignKeyRole() {
		return foreignKeyRole.getValue();
	}
	
	public CFWStoredQuerySharedGroupsMap foreignKeyRole(Integer value) {
		this.foreignKeyRole.setValue(value);
		return this;
	}	
	

}
