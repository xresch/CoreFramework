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
import com.xresch.cfw.features.usermgmt.Role;
import com.xresch.cfw.features.usermgmt.Role.RoleFields;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license Org Manager License
 **************************************************************************************************************/
public class DashboardEditorGroupsMap extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_DASHBOARD_EDITORGROUPS_MAP";
	
	enum DashboardEditorGroupsMapFields{
		  PK_ID 
		, FK_ID_ROLE
		, FK_ID_DASHBOARD
	}

	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, DashboardEditorGroupsMapFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the mapping.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
		
	private CFWField<Integer> foreignDashboard = CFWField.newInteger(FormFieldType.HIDDEN, DashboardEditorGroupsMapFields.FK_ID_DASHBOARD)
			.setForeignKeyCascade(this, Dashboard.class, DashboardFields.PK_ID)
			.setDescription("The id of the dashboard.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<Integer> foreignKeyRole = CFWField.newInteger(FormFieldType.HIDDEN, DashboardEditorGroupsMapFields.FK_ID_ROLE)
			.setForeignKeyCascade(this, Role.class, RoleFields.PK_ID)
			.setDescription("The id of the role.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);

	
	public DashboardEditorGroupsMap() {
		initializeFields();
	}
	
	public DashboardEditorGroupsMap(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignDashboard, foreignKeyRole);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
				
		String[] inputFields = 
				new String[] {
						DashboardEditorGroupsMapFields.PK_ID.toString(), 
						DashboardEditorGroupsMapFields.FK_ID_ROLE.toString(),
						DashboardEditorGroupsMapFields.FK_ID_DASHBOARD.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						DashboardEditorGroupsMapFields.PK_ID.toString(), 
						DashboardEditorGroupsMapFields.FK_ID_ROLE.toString(),
						DashboardEditorGroupsMapFields.FK_ID_DASHBOARD.toString(),
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
	
	public DashboardEditorGroupsMap foreignDashboard(Integer value) {
		this.foreignDashboard.setValue(value);
		return this;
	}	
	
	public Integer foreignKeyRole() {
		return foreignKeyRole.getValue();
	}
	
	public DashboardEditorGroupsMap foreignKeyRole(Integer value) {
		this.foreignKeyRole.setValue(value);
		return this;
	}	
	

}
