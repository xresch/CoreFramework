package com.xresch.cfw.features.dashboard;

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
import com.xresch.cfw.features.usermgmt.User;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class DashboardFavoritesMap extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_DASHBOARD_FAVORITE_MAP";
	
	public enum DashboardFavoritenMapFields{
		PK_ID, 
		FK_ID_DASHBOARD,
		FK_ID_USER,
	}

	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, DashboardFavoritenMapFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the mapping.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
		
	private CFWField<Integer> foreignKeyUser = CFWField.newInteger(FormFieldType.HIDDEN, DashboardFavoritenMapFields.FK_ID_USER)
			.setForeignKeyCascade(this, User.class, RoleFields.PK_ID)
			.setDescription("The id of the user.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> foreignKeyDashboard = CFWField.newInteger(FormFieldType.HIDDEN, DashboardFavoritenMapFields.FK_ID_DASHBOARD)
			.setForeignKeyCascade(this, Dashboard.class, DashboardFavoritenMapFields.PK_ID)
			.setDescription("The id of the dashboard.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);

	
	public DashboardFavoritesMap() {
		initializeFields();
	}
	
	public DashboardFavoritesMap(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignKeyUser, foreignKeyDashboard);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
				
		String[] inputFields = 
				new String[] {
						DashboardFavoritenMapFields.PK_ID.toString(), 
						DashboardFavoritenMapFields.FK_ID_DASHBOARD.toString(),
						DashboardFavoritenMapFields.FK_ID_USER.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						DashboardFavoritenMapFields.PK_ID.toString(), 
						DashboardFavoritenMapFields.FK_ID_DASHBOARD.toString(),
						DashboardFavoritenMapFields.FK_ID_USER.toString(),
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
	
	public DashboardFavoritesMap foreignKeyUser(Integer value) {
		this.foreignKeyUser.setValue(value);
		return this;
	}	
	
	public Integer foreignKeyDashboard() {
		return foreignKeyDashboard.getValue();
	}
	
	public DashboardFavoritesMap foreignKeyDashboard(Integer value) {
		this.foreignKeyDashboard.setValue(value);
		return this;
	}	
	

}
