package com.pengtoolbox.cfw.features.dashboard;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.datahandling.CFWAutocompleteHandler;
import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.datahandling.CFWFieldChangeHandler;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.features.api.APIDefinition;
import com.pengtoolbox.cfw.features.api.APIDefinitionFetch;
import com.pengtoolbox.cfw.features.usermgmt.User;
import com.pengtoolbox.cfw.features.usermgmt.User.UserFields;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.validation.LengthValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class Dashboard extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_DASHBOARD";
	
	public enum DashboardFields{
		PK_ID,
		FK_ID_USER,
		NAME,
		DESCRIPTION,
		IS_SHARED,
		JSON_SHARE_WITH_USERS,
		JSON_EDITORS,
		IS_DELETABLE,
		IS_RENAMABLE,
	}

	private static Logger logger = CFWLog.getLogger(Dashboard.class.getName());
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, DashboardFields.PK_ID.toString())
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the dashboard.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(-999);
	
	private CFWField<Integer> foreignKeyOwner = CFWField.newInteger(FormFieldType.HIDDEN, DashboardFields.FK_ID_USER)
			.setForeignKeyCascade(this, User.class, UserFields.PK_ID)
			.setDescription("The user id of the owner of the dashboard.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<String> name = CFWField.newString(FormFieldType.TEXT, DashboardFields.NAME.toString())
			.setColumnDefinition("VARCHAR(255)")
			.setDescription("The name of the dashboard.")
			.addValidator(new LengthValidator(1, 255))
			.setChangeHandler(new CFWFieldChangeHandler<String>() {
				public boolean handle(String oldValue, String newValue) {
					if(name.isDisabled()) { 
						new CFWLog(logger)
						.method("handle")
						.severe("The name cannot be changed as the field is disabled.");
						return false; 
					}
					return true;
				}
			});
	
	private CFWField<String> description = CFWField.newString(FormFieldType.TEXTAREA, DashboardFields.DESCRIPTION)
			.setColumnDefinition("CLOB")
			.setDescription("The description of the dashboard.")
			.addValidator(new LengthValidator(-1, 2000000));
	
	private CFWField<Boolean> isShared = CFWField.newBoolean(FormFieldType.BOOLEAN, DashboardFields.IS_SHARED)
			.apiFieldType(FormFieldType.TEXT)
			.setDescription("Make the dashboard shared with other people or keep it private.")
			.setValue(false);
	
	private CFWField<LinkedHashMap<String,String>> shareWithUsers = CFWField.newTagsSelector(DashboardFields.JSON_SHARE_WITH_USERS)
			.setLabel("Share with Users")
			.setDescription("Share this dashboard only with specific users. If none is specified, all users will see the dashboard.")
			.setValue(null)
			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
				public LinkedHashMap<Object, Object> getAutocompleteData(HttpServletRequest request, String searchValue) {
					return CFW.DB.Users.autocompleteUser(searchValue, this.getMaxResults());					
				}
			});
	
	private CFWField<LinkedHashMap<String,String>> editors = CFWField.newTagsSelector(DashboardFields.JSON_EDITORS)
			.setLabel("Editors")
			.setDescription("Allow other users to view and edit the dashboard, even when the dashboard is not shared.")
			.setValue(null)
			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
				
				public LinkedHashMap<Object, Object> getAutocompleteData(HttpServletRequest request, String searchValue) {
					return CFW.DB.Users.autocompleteUser(searchValue, this.getMaxResults());
				}
			});
	
	private CFWField<Boolean> isDeletable = CFWField.newBoolean(FormFieldType.NONE, DashboardFields.IS_DELETABLE.toString())
			.setDescription("Flag to define if the dashboard can be deleted or not.")
			.setColumnDefinition("BOOLEAN")
			.setValue(true);
	
	private CFWField<Boolean> isRenamable = CFWField.newBoolean(FormFieldType.NONE, DashboardFields.IS_RENAMABLE.toString())
			.setColumnDefinition("BOOLEAN DEFAULT TRUE")
			.setDescription("Flag to define if the dashboard can be renamed or not.")
			.setValue(true)
			.setChangeHandler(new CFWFieldChangeHandler<Boolean>() {
				
				@Override
				public boolean handle(Boolean oldValue, Boolean newValue) {
					if(!newValue) {
						name.isDisabled(true);
					}else {
						name.isDisabled(false);
					}
					
					return true;
				}
			});
	
	public Dashboard() {
		initializeFields();
	}
		
	public Dashboard(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignKeyOwner, name, description, isShared, shareWithUsers, editors, isDeletable, isRenamable);
	}
		
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
		
		
		String[] inputFields = 
				new String[] {
						DashboardFields.PK_ID.toString(), 
//						DashboardFields.CATEGORY.toString(),
						DashboardFields.NAME.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						DashboardFields.PK_ID.toString(), 
						DashboardFields.FK_ID_USER.toString(),
						DashboardFields.NAME.toString(),
						DashboardFields.DESCRIPTION.toString(),
						DashboardFields.IS_SHARED.toString(),
						DashboardFields.JSON_SHARE_WITH_USERS.toString(),
						DashboardFields.JSON_EDITORS.toString(),
						DashboardFields.IS_DELETABLE.toString(),
						DashboardFields.IS_RENAMABLE.toString(),		
				};

		//----------------------------------
		// fetchJSON
		APIDefinitionFetch fetchDataAPI = 
				new APIDefinitionFetch(
						this.getClass(),
						this.getClass().getSimpleName(),
						"fetchData",
						inputFields,
						outputFields
				);
		
		apis.add(fetchDataAPI);
		
		//----------------------------------
		// Export
		apis.add(new APIDashboardExport(this.getClass().getSimpleName(), "export"));
		
		//----------------------------------
		// Import
		apis.add(new APIDashboardImport(this.getClass().getSimpleName(), "import"));
		return apis;
	}

	public Integer id() {
		return id.getValue();
	}
	
	public Dashboard id(Integer id) {
		this.id.setValue(id);
		return this;
	}
	
	public Integer foreignKeyOwner() {
		return foreignKeyOwner.getValue();
	}
	
	public Dashboard foreignKeyUser(Integer foreignKeyUser) {
		this.foreignKeyOwner.setValue(foreignKeyUser);
		return this;
	}
		
	public String name() {
		return name.getValue();
	}
	
	public Dashboard name(String name) {
		this.name.setValue(name);
		return this;
	}
	
	public String description() {
		return description.getValue();
	}

	public Dashboard description(String description) {
		this.description.setValue(description);
		return this;
	}

	public boolean isShared() {
		return isShared.getValue();
	}
	
	public Dashboard isShared(boolean isShared) {
		this.isShared.setValue(isShared);
		return this;
	}
	
	public LinkedHashMap<String,String> sharedWithUsers() {
		return shareWithUsers.getValue();
	}
	
	public Dashboard sharedWithUsers(LinkedHashMap<String,String> sharedWithUsers) {
		this.shareWithUsers.setValue(sharedWithUsers);
		return this;
	}
	
	public LinkedHashMap<String,String> editors() {
		return editors.getValue();
	}
	
	public Dashboard editors(LinkedHashMap<String,String> editors) {
		this.editors.setValue(editors);
		return this;
	}
	
	public boolean isDeletable() {
		return isDeletable.getValue();
	}
	
	public Dashboard isDeletable(boolean isDeletable) {
		this.isDeletable.setValue(isDeletable);
		return this;
	}	
	
	public boolean isRenamable() {
		return isRenamable.getValue();
	}
	
	public Dashboard isRenamable(boolean isRenamable) {
		this.isRenamable.setValue(isRenamable);
		return this;
	}	
	
}
