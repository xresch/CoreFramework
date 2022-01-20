package com.xresch.cfw.features.dashboard;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWFieldChangeHandler;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.contextsettings.ContextSettings.ContextSettingsFields;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.User.UserFields;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.validation.LengthValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class Dashboard extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_DASHBOARD";
	
	public enum DashboardFields{
		PK_ID,
		FK_ID_USER,
		NAME,
		DESCRIPTION,
		TAGS,
		
		IS_SHARED,
		JSON_SHARE_WITH_USERS,
		JSON_SHARE_WITH_GROUPS,
		JSON_EDITORS,
		JSON_EDITOR_GROUPS,
		IS_PUBLIC,
		IS_DELETABLE,
		IS_RENAMABLE,
	}

	private static Logger logger = CFWLog.getLogger(Dashboard.class.getName());
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, DashboardFields.PK_ID.toString())
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the dashboard.")
			.apiFieldType(FormFieldType.NUMBER);
	
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
	
	private CFWField<ArrayList<String>> tags = CFWField.newArray(FormFieldType.TAGS, DashboardFields.TAGS)
			.setDescription("The tags for this dashboard.")
			.setAutocompleteHandler( new CFWAutocompleteHandler(10) {

				@Override
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String inputValue, int cursorPosition) {
					
					AutocompleteList list = new AutocompleteList();
					for(String tag : CFW.DB.Dashboards.getTags()) {
						if(tag.toLowerCase().contains(inputValue.toLowerCase())) {
							list.addItem(tag);
							if(list.size() >= this.getMaxResults()) {
								break;
							}
						}
					}
					return new AutocompleteResult(list);
				}
				
			} );
	
	private CFWField<Boolean> isShared = CFWField.newBoolean(FormFieldType.BOOLEAN, DashboardFields.IS_SHARED)
			.apiFieldType(FormFieldType.TEXT)
			.setDescription("Make the dashboard shared with other people or keep it private. If no shared users or shared groups are specified(defined editors have no impact), the dashboard is shared with all users having access to the dashboard features.")
			.setValue(false);
	
	private CFWField<LinkedHashMap<String,String>> shareWithUsers = CFWField.newTagsSelector(DashboardFields.JSON_SHARE_WITH_USERS)
			.setLabel("Share with Users")
			.setDescription("Share this dashboard with specific users.")
			.setValue(null)
			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
					return CFW.DB.Users.autocompleteUser(searchValue, this.getMaxResults());					
				}
			});
	
	private CFWField<LinkedHashMap<String,String>> shareWithGroups = CFWField.newTagsSelector(DashboardFields.JSON_SHARE_WITH_GROUPS)
			.setLabel("Share with Groups")
			.setDescription("Share this dashboard with specific groups.")
			.setValue(null)
			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
					return CFW.DB.Roles.autocompleteGroup(searchValue, this.getMaxResults());					
				}
			});
	
	private CFWField<LinkedHashMap<String,String>> editors = CFWField.newTagsSelector(DashboardFields.JSON_EDITORS)
			.setLabel("Editors")
			.setDescription("Allow the specified users to view and edit the dashboard, even when the dashboard is not shared.")
			.setValue(null)
			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
				
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
					return CFW.DB.Users.autocompleteUser(searchValue, this.getMaxResults());
				}
			});
	
	private CFWField<LinkedHashMap<String,String>> editorGroups = CFWField.newTagsSelector(DashboardFields.JSON_EDITOR_GROUPS)
			.setLabel("Editor Groups")
			.setDescription("Allow users having at least one of the specified groups to view and edit the dashboard, even when the dashboard is not shared.")
			.setValue(null)
			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
				
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
					return CFW.DB.Roles.autocompleteGroup(searchValue, this.getMaxResults());
				}
			});
	
	private CFWField<Boolean> isPublic = CFWField.newBoolean(FormFieldType.BOOLEAN, DashboardFields.IS_PUBLIC)
			.apiFieldType(FormFieldType.TEXT)
			.setColumnDefinition("BOOLEAN DEFAULT FALSE")
			.setDescription("Make the dashboard accessible through a public URL without the need for sign-in or having an account. This is unrelated to any other sharing options.")
			.setValue(false);
	
	private CFWField<Boolean> isDeletable = CFWField.newBoolean(FormFieldType.NONE, DashboardFields.IS_DELETABLE.toString())
			.setDescription("Flag to define if the dashboard can be deleted or not.")
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
		this.addFields(id, foreignKeyOwner, name, description, tags, isShared, shareWithUsers, shareWithGroups, editors, editorGroups, isPublic, isDeletable, isRenamable);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public void migrateTable() {
		//----------------------------------------
		// Migration from v3.0.0 to next version
		new CFWSQL(null).renameColumn(TABLE_NAME, "JSON_SHARE_WITH_ROLES", DashboardFields.JSON_SHARE_WITH_GROUPS.toString());
		new CFWSQL(null).renameColumn(TABLE_NAME, "JSON_EDITOR_ROLES", DashboardFields.JSON_EDITOR_GROUPS.toString());
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public void updateTable() {
								
		//---------------------------
		// Change Description Data Type
		new CFWSQL(this)
			.custom("ALTER TABLE IF EXISTS CFW_DASHBOARD ALTER COLUMN IF EXISTS NAME SET DATA TYPE VARCHAR_IGNORECASE;")
			.execute();
		

		
	}
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
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
						DashboardFields.TAGS.toString(),
						DashboardFields.IS_SHARED.toString(),
						DashboardFields.JSON_SHARE_WITH_USERS.toString(),
						DashboardFields.JSON_SHARE_WITH_GROUPS.toString(),
						DashboardFields.JSON_EDITORS.toString(),
						DashboardFields.JSON_EDITOR_GROUPS.toString(),
						DashboardFields.IS_PUBLIC.toString(),
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
	
	public Dashboard foreignKeyOwner(Integer foreignKeyUser) {
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

	public ArrayList<String> tags() {
		return tags.getValue();
	}
	
	public Dashboard tags(ArrayList<String> tags) {
		this.tags.setValue(tags);
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
	
	public LinkedHashMap<String,String> sharedWithGroups() {
		return shareWithGroups.getValue();
	}
	
	public Dashboard sharedWithGroups(LinkedHashMap<String,String> value) {
		this.shareWithGroups.setValue(value);
		return this;
	}
	public LinkedHashMap<String,String> editors() {
		return editors.getValue();
	}
	
	public Dashboard editors(LinkedHashMap<String,String> editors) {
		this.editors.setValue(editors);
		return this;
	}
	
	public LinkedHashMap<String,String> editorGroups() {
		return editorGroups.getValue();
	}
	
	public Dashboard editorGroups(LinkedHashMap<String,String> value) {
		this.editorGroups.setValue(value);
		return this;
	}
	
	public boolean isPublic() {
		return isPublic.getValue();
	}
	
	public Dashboard isPublic(boolean value) {
		this.isPublic.setValue(value);
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
