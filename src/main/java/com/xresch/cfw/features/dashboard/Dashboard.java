package com.xresch.cfw.features.dashboard;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
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
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.keyvaluepairs.KeyValuePair;
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
	
	public static final String FIELDNAME_SHARE_WITH_USERS = "JSON_SHARE_WITH_USERS";
	public static final String FIELDNAME_SHARE_WITH_GROUPS = "JSON_SHARE_WITH_GROUPS";
	public static final String FIELDNAME_EDITORS = "JSON_EDITORS";
	public static final String FIELDNAME_EDITOR_GROUPS = "JSON_EDITOR_GROUPS";
	
	public static final String[] SELECTOR_FIELDS = new String[] {
			FIELDNAME_SHARE_WITH_USERS
			, FIELDNAME_SHARE_WITH_GROUPS
			, FIELDNAME_EDITORS
			, FIELDNAME_EDITOR_GROUPS
		};
	
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
		ALLOW_EDIT_SETTINGS,
		TIME_CREATED,
		IS_PUBLIC,
		START_FULLSCREEN,
		IS_DELETABLE,
		IS_RENAMABLE, 
	}

	private static Logger logger = CFWLog.getLogger(Dashboard.class.getName());
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, DashboardFields.PK_ID.toString())
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the dashboard.")
			.apiFieldType(FormFieldType.NUMBER)
			;
	
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
			.setValue(CFW.DB.Config.getConfigAsBoolean(FeatureDashboard.CONFIG_DEFAULT_IS_SHARED));
	
	private CFWField<LinkedHashMap<String,String>> shareWithUsers = this.createSelectorFieldSharedUser(null);
	
//	private CFWField<LinkedHashMap<String,String>> shareWithUsers = CFWField.newTagsSelector(DashboardFields.JSON_SHARE_WITH_USERS)
//			.setLabel("Share with Users")
//			.setDescription("Share this dashboard with specific users.")
//			.setValue(null)
//			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
//				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
//					return CFW.DB.Users.autocompleteUser(searchValue, this.getMaxResults());					
//				}
//			});
	
	private CFWField<LinkedHashMap<String,String>> shareWithGroups = this.createSelectorFieldSharedGroups(null);
	
//	private CFWField<LinkedHashMap<String,String>> shareWithGroups = CFWField.newTagsSelector(DashboardFields.JSON_SHARE_WITH_GROUPS)
//			.setLabel("Share with Groups")
//			.setDescription("Share this dashboard with specific groups.")
//			.setValue(null)
//			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
//				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
//					return CFW.DB.Roles.autocompleteGroup(searchValue, this.getMaxResults());					
//				}
//			});
//	
	private CFWField<LinkedHashMap<String,String>> editors = this.createSelectorFieldEditors(null);
			
//	private CFWField<LinkedHashMap<String,String>> editors = CFWField.newTagsSelector(DashboardFields.JSON_EDITORS)
//			.setLabel("Editors")
//			.setDescription("Allow the specified users to view and edit the dashboard, even when the dashboard is not shared.")
//			.setValue(null)
//			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
//				
//				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
//					return CFW.DB.Users.autocompleteUser(searchValue, this.getMaxResults());
//				}
//			});
		
	private CFWField<LinkedHashMap<String,String>> editorGroups = this.createSelectorFieldEditorGroups(null);
	
//	private CFWField<LinkedHashMap<String,String>> editorGroups = CFWField.newTagsSelector(DashboardFields.JSON_EDITOR_GROUPS)
//			.setLabel("Editor Groups")
//			.setDescription("Allow users having at least one of the specified groups to view and edit the dashboard, even when the dashboard is not shared.")
//			.setValue(null)
//			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
//				
//				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
//					return CFW.DB.Roles.autocompleteGroup(searchValue, this.getMaxResults());
//				}
//			});
	
	
	private CFWField<Boolean> alloweEditSettings = CFWField.newBoolean(FormFieldType.BOOLEAN, DashboardFields.ALLOW_EDIT_SETTINGS)
			.apiFieldType(FormFieldType.TEXT)
			.setColumnDefinition("BOOLEAN DEFAULT TRUE")
			.setDescription("Allow editors of the dashboard to change the settings of the dashboard.")
			.setValue(true)
			;
	
	private CFWField<Timestamp> timeCreated = CFWField.newTimestamp(FormFieldType.NONE, DashboardFields.TIME_CREATED)
			.setDescription("The date and time the dashboard was created.")
			.setValue(new Timestamp(new Date().getTime()));
	
	private CFWField<Boolean> isPublic = CFWField.newBoolean(FormFieldType.BOOLEAN, DashboardFields.IS_PUBLIC)
			.apiFieldType(FormFieldType.TEXT)
			.setColumnDefinition("BOOLEAN DEFAULT FALSE")
			.setDescription("Make the dashboard accessible through a public URL without the need for sign-in or having an account. This is unrelated to any other sharing options.")
			.setValue(false)
			;
	
	private CFWField<Boolean> startFullscreen = CFWField.newBoolean(FormFieldType.BOOLEAN, DashboardFields.START_FULLSCREEN)
			.apiFieldType(FormFieldType.TEXT)
			.setColumnDefinition("BOOLEAN DEFAULT FALSE")
			.setDescription("Make the dashboard start in fullscreen mode.")
			.setValue(false)
			;
	
	private CFWField<Boolean> isDeletable = CFWField.newBoolean(FormFieldType.NONE, DashboardFields.IS_DELETABLE.toString())
			.setDescription("Flag to define if the dashboard can be deleted or not.")
			.setValue(true)
			;
	
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
		this.addFields(id
				, foreignKeyOwner
				, name
				, description
				, tags
				, isShared
				//, shareWithUsers
				//, shareWithGroups
				//, editors
				//, editorGroups
				, alloweEditSettings
				, timeCreated
				, isPublic
				, startFullscreen
				, isDeletable
				, isRenamable
			);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public void migrateTable() {
		//----------------------------------------
		// Migration from v3.0.0 to next version
		//new CFWLog(logger).off("Migration: Rename Columns of DB table CFW_DASHBOARD.");
		//new CFWSQL(null).renameColumn(TABLE_NAME, "JSON_SHARE_WITH_ROLES", DashboardFields.JSON_SHARE_WITH_GROUPS.toString());
		//new CFWSQL(null).renameColumn(TABLE_NAME, "JSON_EDITOR_ROLES", DashboardFields.JSON_EDITOR_GROUPS.toString());
	
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public void updateTable() {
								
		//---------------------------
		// Change Description Data Type
		new CFWLog(logger).off("Migration: Change type of database column CFW_DASHBOARD.NAME to VARCHAR_IGNORECASE.");
		new CFWSQL(this)
			.custom("ALTER TABLE IF EXISTS CFW_DASHBOARD ALTER COLUMN IF EXISTS NAME SET DATA TYPE VARCHAR_IGNORECASE;")
			.execute();
		
		//----------------------------------------
		// Migration from v7.0.0 to next version
		String MIGRATION_KEY = "CFW-Migration-SharedEditors_isMigrated";
		
		Boolean isMigrated = CFW.DB.KeyValuePairs.getValueAsBoolean(MIGRATION_KEY);

		if(isMigrated == null || !isMigrated) {
		
			new CFWLog(logger).off("Migration: CFW_DASHBOARD - Move sharing columns to separate tables.");
			
			ArrayList<Dashboard> dashboardList = new CFWSQL(new Dashboard())
				.select()
				.getAsObjectListConvert(Dashboard.class);
		
			CFW.DB.transactionStart();
			
				boolean isSuccess = true;
				for(Dashboard board : dashboardList) {
					isSuccess &= CFWDBDashboardSharedUserMap.migrateOldStructure(board);
					isSuccess &= CFWDBDashboardSharedGroupsMap.migrateOldStructure(board);
					isSuccess &= CFWDBDashboardEditorsMap.migrateOldStructure(board);
					isSuccess &= CFWDBDashboardEditorGroupsMap.migrateOldStructure(board);
				}
			
				new CFWLog(logger).off("Migration Result: "+isSuccess);
				
			CFW.DB.transactionEnd(isSuccess);
			
			CFW.DB.KeyValuePairs.setValue(
					KeyValuePair.CATEGORY_MIGRATION
					, MIGRATION_KEY
					, ""+isSuccess
				);
		}
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
						//DashboardFields.JSON_SHARE_WITH_USERS.toString(),
						//DashboardFields.JSON_SHARE_WITH_GROUPS.toString(),
						//DashboardFields.JSON_EDITORS.toString(),
						DashboardFields.JSON_EDITOR_GROUPS.toString(),
						DashboardFields.ALLOW_EDIT_SETTINGS.toString(),
						DashboardFields.TIME_CREATED.toString(),
						DashboardFields.IS_PUBLIC.toString(),
						DashboardFields.START_FULLSCREEN.toString(),
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
	
	/******************************************************************
	 *
	 *@param type either "shareuser" or "admin"
	 ******************************************************************/
	public void updateSelectorFields() {
		updateSelectorFields(this.id());
	}
	
	/******************************************************************
	 *
	 *@param type either "shareuser" or "admin"
	 ******************************************************************/
	private void updateSelectorFields(Integer boardID) {
		//--------------------------------------
		// Shared Users
		CFWField<LinkedHashMap<String, String>> sharedUserSelector = this.createSelectorFieldSharedUser(boardID);
		this.removeField(FIELDNAME_SHARE_WITH_USERS);
		shareWithUsers = sharedUserSelector;
		this.addFieldAfter(sharedUserSelector, DashboardFields.IS_SHARED);
		
		//--------------------------------------
		// Shared Groups
		CFWField<LinkedHashMap<String, String>> sharedGroupsSelector = this.createSelectorFieldSharedGroups(boardID);
		this.removeField(FIELDNAME_EDITORS);
		shareWithGroups = sharedGroupsSelector;
		this.addFieldAfter(sharedGroupsSelector, FIELDNAME_SHARE_WITH_USERS);
		
		//--------------------------------------
		// Editors 
		CFWField<LinkedHashMap<String, String>> editorsSelector = this.createSelectorFieldEditors(boardID);
		this.removeField(FIELDNAME_EDITORS);
		editors = editorsSelector;
		this.addFieldAfter(editorsSelector, FIELDNAME_SHARE_WITH_GROUPS);
		
		//--------------------------------------
		// Editor Groups
		CFWField<LinkedHashMap<String, String>> editorGroupsSelector = this.createSelectorFieldEditorGroups(boardID);
		this.removeField(FIELDNAME_EDITOR_GROUPS);
		editors = editorGroupsSelector;
		this.addFieldAfter(editorGroupsSelector, FIELDNAME_EDITORS);
		
	}
	
	/******************************************************************
	 *
	 *@param fieldname is either of the FIELDNAME_* constants
	 ******************************************************************/
	private CFWField<LinkedHashMap<String,String>> createSelectorFieldSharedUser(Integer boardID) {
			
		//--------------------------------------
		// Initialize Variables
		LinkedHashMap<String,String> selectedValue = new LinkedHashMap<>();
		 if(boardID != null ) {
				selectedValue = CFW.DB.DashboardSharedUsers
								.selectUsersForDashboardAsKeyLabel(boardID);
		 }
		 
		//--------------------------------------
		// Create Field
		return CFWField.newTagsSelector(FIELDNAME_SHARE_WITH_USERS)
						.setDescription("Share this dashboard with specific users.")
						.setLabel("Shared with Users")
						.addAttribute("maxTags", "256")
						.setValue(selectedValue)
						.setAutocompleteHandler(new CFWAutocompleteHandler(10,2) {
							public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
								return CFW.DB.Users.autocompleteUser(searchValue, this.getMaxResults());					
							}
						});

	}
	
	/******************************************************************
	 *
	 *@param fieldname is either of the FIELDNAME_* constants
	 ******************************************************************/
	private CFWField<LinkedHashMap<String,String>> createSelectorFieldSharedGroups(Integer boardID) {
		
		//--------------------------------------
		// Initialize Variables
		LinkedHashMap<String,String> selectedValue = new LinkedHashMap<>();
		if(boardID != null ) {
			selectedValue = CFW.DB.DashboardSharedGroups
								.selectGroupsForDashboardAsKeyLabel(boardID);
		}
		
		//--------------------------------------
		// Create Field
		return CFWField.newTagsSelector(FIELDNAME_SHARE_WITH_GROUPS)
				.setLabel("Share with Groups")
				.setDescription("Share this dashboard with specific groups.")
				.addAttribute("maxTags", "256")
				.setValue(selectedValue)
				.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
					public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
						return CFW.DB.Roles.autocompleteGroup(searchValue, this.getMaxResults());					
					}
				});
		
	}
	
	
	/******************************************************************
	 *
	 *@param fieldname is either of the FIELDNAME_* constants
	 ******************************************************************/
	private CFWField<LinkedHashMap<String,String>> createSelectorFieldEditors(Integer boardID) {
			
		//--------------------------------------
		// Initialize Variables
		LinkedHashMap<String,String> selectedValue = new LinkedHashMap<>();
		 if(boardID != null ) {
				selectedValue = CFW.DB.DashboardSharedUsers
								.selectUsersForDashboardAsKeyLabel(boardID);
		}
		 
		//--------------------------------------
		// Create Field
		return CFWField.newTagsSelector(FIELDNAME_EDITORS)
				.setLabel("Editors")
				.setDescription("Allow the specified users to view and edit the dashboard, even when the dashboard is not shared.")
				.addAttribute("maxTags", "256")
				.setValue(selectedValue)
				.setAutocompleteHandler(new CFWAutocompleteHandler(10,2) {
					public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
						return CFW.DB.Users.autocompleteUser(searchValue, this.getMaxResults());					
					}
				});

	}
	
	/******************************************************************
	 *
	 *@param fieldname is either of the FIELDNAME_* constants
	 ******************************************************************/
	private CFWField<LinkedHashMap<String,String>> createSelectorFieldEditorGroups(Integer boardID) {
		
		//--------------------------------------
		// Initialize Variables
		LinkedHashMap<String,String> selectedValue = new LinkedHashMap<>();
		if(boardID != null ) {
			selectedValue = CFW.DB.DashboardSharedGroups
								.selectGroupsForDashboardAsKeyLabel(boardID);
		}
		
		//--------------------------------------
		// Create Field
		return CFWField.newTagsSelector(FIELDNAME_EDITOR_GROUPS)
				.setLabel("Editor Groups")
				.setDescription("Allow users having at least one of the specified groups to view and edit the dashboard, even when the dashboard is not shared.")
				.addAttribute("maxTags", "256")
				.setValue(selectedValue)
				.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
					public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
						return CFW.DB.Roles.autocompleteGroup(searchValue, this.getMaxResults());					
					}
				});
		
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	public boolean saveSelectorFields() {
		
		boolean isSuccess = true;
		
			isSuccess &= saveSelectorField(FIELDNAME_SHARE_WITH_USERS);
			isSuccess &= saveSelectorField(FIELDNAME_SHARE_WITH_GROUPS);
			isSuccess &= saveSelectorField(FIELDNAME_EDITORS);
		
		return isSuccess;
		
	}
	/******************************************************************
	 *
	 ******************************************************************/
	private boolean saveSelectorField(String fieldname) {
		boolean success = true;
				
		//--------------------------
		// Update Selected Users
		if(this.getFields().containsKey(fieldname)) {
			CFWField<LinkedHashMap<String,String>> selector = this.getField(fieldname);
			
			LinkedHashMap<String,String> selectedValues = selector.getValue();
			
			boolean isSuccess = true;
			
			switch(fieldname) {
				case FIELDNAME_SHARE_WITH_USERS:
					CFW.DB.DashboardSharedUsers.updateUserDashboardAssignments(this, selectedValues);
					break;
					
				case FIELDNAME_SHARE_WITH_GROUPS:
					CFW.DB.DashboardSharedGroups.updateGroupDashboardAssignments(this, selectedValues);
					break;
					
				case FIELDNAME_EDITORS:
					CFW.DB.DashboardEditors.updateUserDashboardAssignments(this, selectedValues);
					break;
					
				case FIELDNAME_EDITOR_GROUPS:
					CFW.DB.DashboardEditors.updateUserDashboardAssignments(this, selectedValues);
					break;
				
				default: new CFWLog(logger).severe("Development Error: unsupported value.");
			}
			if( !isSuccess ){
				success = false;
				CFW.Messages.addErrorMessage("Error while saving user assignments for field: "+fieldname);
			}
		}
		
		return success;
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
	
	public boolean alloweEditSettings() {
		return alloweEditSettings.getValue();
	}
	
	public Dashboard alloweEditSettings(boolean isShared) {
		this.alloweEditSettings.setValue(isShared);
		return this;
	}
	
	public Timestamp timeCreated() {
		return timeCreated.getValue();
	}
	
	public Dashboard timeCreated(Timestamp creationDate) {
		this.timeCreated.setValue(creationDate);
		return this;
	}
	
	public boolean isPublic() {
		return isPublic.getValue();
	}
	
	public Dashboard isPublic(boolean value) {
		this.isPublic.setValue(value);
		return this;
	}
	public boolean startFullscreen() {
		return startFullscreen.getValue();
	}
	
	public Dashboard startFullscreen(boolean value) {
		this.startFullscreen.setValue(value);
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
