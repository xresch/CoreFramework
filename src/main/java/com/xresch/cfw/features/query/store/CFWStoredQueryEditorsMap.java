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
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.User.UserFields;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license Org Manager License
 **************************************************************************************************************/
public class CFWStoredQueryEditorsMap extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_QUERY_STORED_EDITORS_MAP";
	
	enum CFWStoredQueryEditorsMapFields{
		  PK_ID 
		, FK_ID_USER
		, FK_ID_STOREDQUERY
	}

	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, CFWStoredQueryEditorsMapFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the mapping.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
			
	private CFWField<Integer> foreignStoredQuery = CFWField.newInteger(FormFieldType.HIDDEN, CFWStoredQueryEditorsMapFields.FK_ID_STOREDQUERY)
			.setForeignKeyCascade(this, CFWStoredQuery.class, CFWStoredQueryFields.PK_ID)
			.setDescription("The id of the storedQuery.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> foreignKeyUser = CFWField.newInteger(FormFieldType.HIDDEN, CFWStoredQueryEditorsMapFields.FK_ID_USER)
			.setForeignKeyCascade(this, User.class, UserFields.PK_ID)
			.setDescription("The id of the user.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);

	
	public CFWStoredQueryEditorsMap() {
		initializeFields();
	}
	
	public CFWStoredQueryEditorsMap(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignStoredQuery, foreignKeyUser);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
				
		String[] inputFields = 
				new String[] {
						CFWStoredQueryEditorsMapFields.PK_ID.toString(), 
						CFWStoredQueryEditorsMapFields.FK_ID_USER.toString(),
						CFWStoredQueryEditorsMapFields.FK_ID_STOREDQUERY.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						CFWStoredQueryEditorsMapFields.PK_ID.toString(), 
						CFWStoredQueryEditorsMapFields.FK_ID_USER.toString(),
						CFWStoredQueryEditorsMapFields.FK_ID_STOREDQUERY.toString(),
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
	
	public CFWStoredQueryEditorsMap foreignStoredQuery(Integer value) {
		this.foreignStoredQuery.setValue(value);
		return this;
	}	
	
	public Integer foreignKeyUser() {
		return foreignKeyUser.getValue();
	}
	
	public CFWStoredQueryEditorsMap foreignKeyUser(Integer value) {
		this.foreignKeyUser.setValue(value);
		return this;
	}	
	

}
