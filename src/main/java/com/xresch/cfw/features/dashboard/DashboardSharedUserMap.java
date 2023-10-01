package com.xresch.cfw.features.dashboard;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.dashboard.Dashboard.DashboardFields;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.User.UserFields;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license Org Manager License
 **************************************************************************************************************/
public class DashboardSharedUserMap extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_DASHBOARD_SHAREDUSER_MAP";
	
	enum DashboardSharedUserMapFields{
		  PK_ID 
		, FK_ID_USER
		, FK_ID_DASHBOARD
	}

	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, DashboardSharedUserMapFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the mapping.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
		
	private CFWField<Integer> foreignDashboard = CFWField.newInteger(FormFieldType.HIDDEN, DashboardSharedUserMapFields.FK_ID_DASHBOARD)
			.setForeignKeyCascade(this, Dashboard.class, DashboardFields.PK_ID)
			.setDescription("The id of the dashboard.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> foreignKeyUser = CFWField.newInteger(FormFieldType.HIDDEN, DashboardSharedUserMapFields.FK_ID_USER)
			.setForeignKeyCascade(this, User.class, UserFields.PK_ID)
			.setDescription("The id of the user.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);

	
	public DashboardSharedUserMap() {
		initializeFields();
	}
	
	public DashboardSharedUserMap(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignDashboard, foreignKeyUser);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
				
		String[] inputFields = 
				new String[] {
						DashboardSharedUserMapFields.PK_ID.toString(), 
						DashboardSharedUserMapFields.FK_ID_USER.toString(),
						DashboardSharedUserMapFields.FK_ID_DASHBOARD.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						DashboardSharedUserMapFields.PK_ID.toString(), 
						DashboardSharedUserMapFields.FK_ID_USER.toString(),
						DashboardSharedUserMapFields.FK_ID_DASHBOARD.toString(),
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

	public Integer foreignDashboard() {
		return foreignDashboard.getValue();
	}
	
	public DashboardSharedUserMap foreignDashboard(Integer value) {
		this.foreignDashboard.setValue(value);
		return this;
	}	
	
	public Integer foreignKeyUser() {
		return foreignKeyUser.getValue();
	}
	
	public DashboardSharedUserMap foreignKeyUser(Integer value) {
		this.foreignKeyUser.setValue(value);
		return this;
	}	
	

}
