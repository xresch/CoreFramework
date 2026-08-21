package com.xresch.cfw.features.spaces;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWHierarchy;
import com.xresch.cfw.datahandling.CFWHierarchyConfig;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.features.spaces.CFWSpaceAdminMap.CFWSpaceAdminMapFields;
import com.xresch.cfw.features.spaces.FeatureSpaces.FeatureSpacesDefaults;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.validation.EmailValidator;
import com.xresch.cfw.validation.ExcludeStringsValidator;
import com.xresch.cfw.validation.LengthValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2026
 * @license MIT
 **************************************************************************************************************/
public class CFWSpace extends CFWObject {
	
	private static final Logger logger = CFWLog.getLogger(CFWSpace.class.getName());
	
	public static String TABLE_NAME = "CFW_SPACES";
	
	public static final String FIELDNAME_USERS 			= CFWSpaceFields.JSON_USERS.toString();
	public static final String FIELDNAME_USER_GROUPS 	= CFWSpaceFields.JSON_USER_GROUPS.toString();
	public static final String FIELDNAME_ADMINS 		= CFWSpaceFields.JSON_ADMINS.toString();
	public static final String FIELDNAME_ADMIN_GROUPS 	= CFWSpaceFields.JSON_ADMIN_GROUPS.toString();
	
	public static final String[] SELECTOR_FIELDS = new String[] {
			  FIELDNAME_USERS
			, FIELDNAME_USER_GROUPS
			, FIELDNAME_ADMINS
			, FIELDNAME_ADMIN_GROUPS
		};
	
	public enum CFWSpaceFields{
		PK_ID, 
		UUID,
		TYPE, 
		NAME, 
		ABBREVIATION, 
		DESCRIPTION,
		SHARED_EMAIL, 
		IS_ENABLED,
		IS_GLOBAL,
		JSON_USERS,
		JSON_USER_GROUPS,
		JSON_ADMINS,
		JSON_ADMIN_GROUPS,
	}
	
	public enum CFWSpaceType{
		ROOT_SPACE,
		SPACE
	}		
	
	public static final CFWHierarchyConfig hierarchyConfig = 
		new CFWHierarchyConfig(
				  CFWSpace.class
				, new Object[] {CFWSpaceFields.ABBREVIATION, CFWSpaceFields.NAME}
				, new Object[] {CFWSpaceFields.PK_ID, CFWSpaceFields.NAME}
				  )
		{
	
		@Override
		public boolean canBeReordered(CFWObject targetParent, CFWObject sortedElement) {
			
			CFWSpace sorted = (CFWSpace)sortedElement;
			CFWSpace target = (CFWSpace)targetParent;
			
			//----------------------------------------
			// Prevent Moving ORG
			if(sorted.type() == CFWSpaceType.ROOT_SPACE
			&& target != null) {
				CFW.Messages.addWarningMessage("Cannot move a root space below another root space.");
				return false;
			}
			
			//----------------------------------------
			// Prevent Moving to Other Space
			if(sorted.type() != CFWSpaceType.ROOT_SPACE) {
				//TODO
			}
			return true;
		}
		
		@Override
		public boolean canAccessHierarchy(String rootElementID) {
			return true;
		}
	};
	
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, CFWSpaceFields.PK_ID)
								   .setPrimaryKeyAutoIncrement(this)
								   .setDescription("The id of the space in the local database.")
								   .apiFieldType(FormFieldType.NUMBER)
								   .setValue(null);
	
	private CFWField<String> type = CFWField.newString(FormFieldType.UNMODIFIABLE_TEXT, CFWSpaceFields.TYPE)
			.setDescription("The type of the space.")
			;
	
	private CFWField<String> abbreviation = CFWField.newString(FormFieldType.TEXT, CFWSpaceFields.ABBREVIATION)
			.setDescription("The abbreviation used for this space.")
			.addValidator(new LengthValidator(1, 16))
			.addValidator(new ExcludeStringsValidator(new String[] {",", "\""}))
			;
	
	private CFWField<String> name = CFWField.newString(FormFieldType.TEXT, CFWSpaceFields.NAME)
			.setDescription("The name of the space.")
			.addValidator(new LengthValidator(2, 255))
			.addValidator(new ExcludeStringsValidator(new String[] {",", "\""}))
			;
	
	private CFWField<String> description = CFWField.newString(FormFieldType.TEXTAREA, CFWSpaceFields.DESCRIPTION)
			.setDescription("(Optional) The description of this space.")
			.addValidator(new LengthValidator(-1, 255));
	
	
	private CFWField<String> email = CFWField.newString(FormFieldType.EMAIL, CFWSpaceFields.SHARED_EMAIL)
			.setDescription("(Optional) A mailbox that can be used to contact this space(a shared mailbox, not a personal one).")
			.addValidator(new LengthValidator(-1, 255))
			.addValidator(new EmailValidator());

	
	private CFWField<Boolean> isEnabled = CFWField.newBoolean(FormFieldType.BOOLEAN, CFWSpaceFields.IS_ENABLED)
					.setDescription("Defines if the space is enabled or disabled. Disabled spaces will be hidden in the UI.")
					.setValue(true);
	
	private CFWField<Boolean> isGlobal = CFWField.newBoolean(FormFieldType.BOOLEAN, CFWSpaceFields.IS_GLOBAL)
				.setDescription("Defines if the entities in this space are available globally."
						+ " If the space is a Root Space, every space can access the items in this space."
						+ " If the space is a Regular Space, all spaces with the same Root Space can access it's items.")
				.setValue(false);
	
	private CFWField<LinkedHashMap<String,String>> assignedUsers = this.createSelectorFieldAssignedUsers(null);
	
	private CFWField<LinkedHashMap<String,String>> assignedGroups = this.createSelectorFieldUserGroups(null);
		
	private CFWField<LinkedHashMap<String,String>> admins = this.createSelectorFieldAdmins(null);
		
	private CFWField<LinkedHashMap<String,String>> adminGroups = this.createSelectorFieldAdminGroups(null);
	
	
	public CFWSpace() {
		initializeFields();
	}
	
	public CFWSpace(String username) {
		initializeFields();
	}
	
	public CFWSpace(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);
	}
		
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		
		this.setHierarchyConfig(hierarchyConfig);
		
		this.addFields(
				  id
				, type
				, abbreviation
				, name
				, description
				, email
				, isEnabled
				, isGlobal
				, assignedUsers
				, assignedGroups
				, admins
				, adminGroups
				);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	public void initDB() {

	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
		
		String[] inputFields = 
				new String[] {
						CFWSpaceFields.PK_ID.toString(), 
						CFWSpaceFields.UUID.toString(), 
						CFWSpaceFields.TYPE.toString(), 
						CFWSpaceFields.SHARED_EMAIL.toString(),
						CFWSpaceFields.NAME.toString(),
						CFWSpaceFields.ABBREVIATION.toString(),
						CFWSpaceFields.DESCRIPTION.toString(),
						CFWSpaceFields.IS_ENABLED.toString(),				
						CFWSpaceFields.IS_GLOBAL.toString()				
						};
		
		String[] outputFields = 
				new String[] {
						CFWSpaceFields.PK_ID.toString(), 
						CFWSpaceFields.UUID.toString(), 
						CFWSpaceFields.TYPE.toString(), 
						CFWSpaceFields.SHARED_EMAIL.toString(),
						CFWSpaceFields.NAME.toString(),
						CFWSpaceFields.ABBREVIATION.toString(),
						CFWSpaceFields.DESCRIPTION.toString(),
						CFWSpaceFields.IS_ENABLED.toString(),
						CFWSpaceFields.IS_GLOBAL.toString(),
						CFWSpaceFields.JSON_USERS.toString(),
						CFWSpaceFields.JSON_USER_GROUPS.toString(),
						CFWSpaceFields.JSON_ADMINS.toString(),
						CFWSpaceFields.JSON_ADMIN_GROUPS.toString(),
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
		
		return apis;
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public String createSpaceLabel() {
		return "["+this.abbreviation()+"] "+this.name();
	}

	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public String createBreadcrumbsString() {
		
		StringBuilder builder = new StringBuilder("");
		
		for(Number id : this.hierachyLineage()) {
			CFWSpace current = CFW.DB.Spaces.getFromCache(id.intValue());
			builder.append( "[" + current.abbreviation() + "] ");
		}
		
		return builder.toString() + "[" + this.abbreviation() + "] " + this.name();

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
	private void updateSelectorFields(Integer spaceID) {
		//--------------------------------------
		// Shared Users
		CFWField<LinkedHashMap<String, String>> assignedUserSelector = this.createSelectorFieldAssignedUsers(spaceID);
		this.removeField(FIELDNAME_USERS);
		assignedUsers = assignedUserSelector;
		this.addFieldAfter(assignedUserSelector, CFWSpaceFields.IS_ENABLED);
		
		//--------------------------------------
		// Shared Groups
		CFWField<LinkedHashMap<String, String>> assignedGroupsSelector = this.createSelectorFieldUserGroups(spaceID);
		this.removeField(FIELDNAME_USER_GROUPS);
		assignedGroups = assignedGroupsSelector;
		this.addFieldAfter(assignedGroupsSelector, FIELDNAME_USERS);
		
		//--------------------------------------
		// Editors 
		CFWField<LinkedHashMap<String, String>> editorsSelector = this.createSelectorFieldAdmins(spaceID);
		this.removeField(FIELDNAME_ADMINS);
		admins = editorsSelector;
		this.addFieldAfter(editorsSelector, FIELDNAME_USER_GROUPS);
		
		//--------------------------------------
		// Editor Groups
		CFWField<LinkedHashMap<String, String>> editorGroupsSelector = this.createSelectorFieldAdminGroups(spaceID);
		this.removeField(FIELDNAME_ADMIN_GROUPS);
		adminGroups = editorGroupsSelector;
		this.addFieldAfter(editorGroupsSelector, FIELDNAME_ADMINS);
		
	}
	
	/******************************************************************
	 *
	 *@param spaceID the id of the Space
	 ******************************************************************/
	private CFWField<LinkedHashMap<String,String>> createSelectorFieldAssignedUsers(Integer spaceID) {
			
		//--------------------------------------
		// Initialize Variables
		LinkedHashMap<String,String> selectedValue = new LinkedHashMap<>();
		 if(spaceID != null ) {
				selectedValue =  CFW.DB.SpaceUserMap.selectUsersForSpaceAsKeyLabel(spaceID);
		 }
		 
		//--------------------------------------
		// Create Field
		boolean isRootSpace = this.type() == CFWSpaceType.ROOT_SPACE;
		return CFWField.newTagsSelector(FIELDNAME_USERS)
						.setDescription("The users that are assigned to this space and have access to it. Start typing to get suggestions.")
						.setLabel("Assigned Users")
						.addAttribute("maxTags", "256")
						.setValue(selectedValue)
						.setAutocompleteHandler(new CFWAutocompleteHandler(10,2) {
							public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
								
								if(isRootSpace) {
									return CFW.DB.Users.autocompleteUser(searchValue, this.getMaxResults());	
								}else {
									return CFW.DB.Users.autocompleteUserForSpace(searchValue, this.getMaxResults());	
								}
								
									
							}
						});

	}
	
	/******************************************************************
	 *
	 *@param spaceID the id of the Space
	 ******************************************************************/
	private CFWField<LinkedHashMap<String,String>> createSelectorFieldUserGroups(Integer spaceID) {
		
		//--------------------------------------
		// Initialize Variables
		LinkedHashMap<String,String> selectedValue = new LinkedHashMap<>();
		if(spaceID != null) {
			selectedValue =  CFW.DB.SpaceUserGroupsMap.selectGroupsForSpaceAsKeyLabel(spaceID);
		}
		
		//--------------------------------------
		// Create Field
		return CFWField.newTagsSelector(FIELDNAME_USER_GROUPS)
				.setLabel("Assigned Groups")
				.setDescription("The groups whose usershave access to this space. Start typing to get suggestions.")
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
	 *@param spaceID the id of the Space
	 ******************************************************************/
	private CFWField<LinkedHashMap<String,String>> createSelectorFieldAdmins(Integer spaceID) {
			
		//--------------------------------------
		// Initialize Variables
		LinkedHashMap<String,String> selectedValue = new LinkedHashMap<>();
		 if(spaceID != null ) {
				selectedValue = CFW.DB.SpaceAdminMap.selectAdminsForSpaceAsKeyLabel(spaceID);
		}
		 
		//--------------------------------------
		// Create Field
		boolean isRootSpace = this.type() == CFWSpaceType.ROOT_SPACE;
		return CFWField.newTagsSelector(FIELDNAME_ADMINS)
				.setLabel("Admins")
				.setDescription("The users that are allowed to add more spaces to this space and change space settings.")
				.addAttribute("maxTags", "256")
				.setValue(selectedValue)
				.setAutocompleteHandler(new CFWAutocompleteHandler(10,2) {
					public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
						
						if(isRootSpace) {
							return CFW.DB.Users.autocompleteUser(searchValue, this.getMaxResults());	
						}else {
							return CFW.DB.Users.autocompleteUserForSpace(searchValue, this.getMaxResults());	
						}
						
					}
				});

	}
	
	/******************************************************************
	 *
	 *@param spaceID the id of the Space
	 ******************************************************************/
	private CFWField<LinkedHashMap<String,String>> createSelectorFieldAdminGroups(Integer spaceID) {
		
		//--------------------------------------
		// Initialize Variables
		LinkedHashMap<String,String> selectedValue = new LinkedHashMap<>();
		if(spaceID != null ) {
			selectedValue = CFW.DB.SpaceAdminGroupsMap.selectGroupsForSpaceAsKeyLabel(spaceID);
		}
		
		//--------------------------------------
		// Create Field
		return CFWField.newTagsSelector(FIELDNAME_ADMIN_GROUPS)
				.setLabel("Admin Groups")
				.setDescription("The groups that are allowed to add more spaces to this space and change space settings.")
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
		
			isSuccess &= saveSelectorField(CFWSpaceFields.JSON_USERS);
			isSuccess &= saveSelectorField(CFWSpaceFields.JSON_USER_GROUPS);
			isSuccess &= saveSelectorField(CFWSpaceFields.JSON_ADMINS);
			isSuccess &= saveSelectorField(CFWSpaceFields.JSON_ADMIN_GROUPS);
		
		return isSuccess;
		
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@SuppressWarnings("unchecked")
	private boolean saveSelectorField(CFWSpaceFields fieldname) {
		boolean success = true;
				
		//--------------------------
		// Update Selected Users
		if(this.getFields().containsKey(fieldname.toString())) {
			CFWField<LinkedHashMap<String,String>> selector = this.getField(fieldname);
			
			LinkedHashMap<String,String> selectedValues = selector.getValue();
			
			switch(fieldname) {
				case JSON_USERS:
					success &= CFW.DB.SpaceUserMap.updateUserSpaceAssignments(this, selectedValues);
					break;
					
				case JSON_USER_GROUPS:
					success &= CFW.DB.SpaceUserGroupsMap.updateGroupSpaceAssignments(this, selectedValues);
					break;
					
				case JSON_ADMINS:
					success &= CFW.DB.SpaceAdminMap.updateAdminSpacesAssignments(this, selectedValues);
					break;
					
				case JSON_ADMIN_GROUPS:
					success &= CFW.DB.SpaceAdminGroupsMap.updateGroupSpaceAssignments(this, selectedValues);
					break;
				
				default: new CFWLog(logger).severe("Development Error: unsupported value.");
			}
			if( !success ){
				CFW.Messages.addErrorMessage("Error while saving user assignments for field: "+fieldname);
			}
		}
		
		return success;
	}
	
	
	public Integer id() {
		return id.getValue();
	}
	
	public CFWSpace id(Integer value) {
		this.id.setValue(value);
		return this;
	}
		
	public CFWSpaceType type() {
		if(type.getValue() == null) { return null; }
		return CFWSpaceType.valueOf(type.getValue());
	}
	
	public CFWSpace type(CFWSpaceType value) {
		this.type.setValue(value.toString());
		return this;
	}
	
	
	public String name() {
		return name.getValue();
	}
	
	public CFWSpace name(String value) {
		this.name.setValue(value);
		return this;
	}
	
	public String abbreviation() {
		return abbreviation.getValue();
	}
	
	public CFWSpace abbreviation(String value) {
		this.abbreviation.setValue(value);
		return this;
	}
	
	public String description() {
		return description.getValue();
	}
	
	public CFWSpace description(String value) {
		this.description.setValue(value);
		return this;
	}
	
	public String email() {
		return email.getValue();
	}
	
	public CFWSpace email(String value) {
		this.email.setValue(value);
		return this;
	}
	
	public boolean isEnabled() {
		return isEnabled.getValue();
	}
	
	public CFWSpace isEnabled(boolean value) {
		this.isEnabled.setValue(value);
		return this;
	}
	
	public boolean isGlobal() {
		return isGlobal.getValue();
	}
	
	public CFWSpace isGlobal(boolean value) {
		this.isGlobal.setValue(value);
		return this;
	}
	
	public LinkedHashMap<String,String> sharedWithUsers() {
		if(assignedUsers.getValue() == null) { return new LinkedHashMap<>(); }
		return assignedUsers.getValue();
	}
	
	public CFWSpace sharedWithUsers(LinkedHashMap<String,String> sharedWithUsers) {
		this.assignedUsers.setValue(sharedWithUsers);
		return this;
	}
	
	public LinkedHashMap<String,String> assignedGroups() {
		if(assignedGroups.getValue() == null) { return new LinkedHashMap<>(); }
		return assignedGroups.getValue();
	}
	
	public CFWSpace assignedGroups(LinkedHashMap<String,String> value) {
		this.assignedGroups.setValue(value);
		return this;
	}
	public LinkedHashMap<String,String> admins() {
		if(admins.getValue() == null) { return new LinkedHashMap<>(); }
		return admins.getValue();
	}
	
	public CFWSpace admins(LinkedHashMap<String,String> editors) {
		this.admins.setValue(editors);
		return this;
	}
	
	public LinkedHashMap<String,String> adminGroups() {
		if(adminGroups.getValue() == null) { return new LinkedHashMap<>(); }
		return adminGroups.getValue();
	}
	
	public CFWSpace adminGroups(LinkedHashMap<String,String> value) {
		this.adminGroups.setValue(value);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Number> hierachyLineage() {
		return (ArrayList<Number>)this.getField(CFWHierarchy.H_LINEAGE).getValue();
	}
	
	public Integer hierachyDepth() {
		return (Integer)this.getField(CFWHierarchy.H_DEPTH).getValue();
	}
	
	public Integer hierachyParent() {
		return (Integer)this.getField(CFWHierarchy.H_PARENT).getValue();
	}
		
	
}
