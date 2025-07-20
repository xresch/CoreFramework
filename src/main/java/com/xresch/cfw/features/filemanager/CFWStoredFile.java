package com.xresch.cfw.features.filemanager;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.User.UserFields;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.validation.LengthValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public class CFWStoredFile extends CFWObject {

	public static final String TABLE_NAME = "CFW_STOREDFILE";
	
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
	
	public enum CFWStoredFileFields{
		PK_ID
		, FK_ID_OWNER
		, NAME
		, DESCRIPTION
		, DATA
		, SIZE
		, MIMETYPE
		, EXTENSION
		, LAST_MODIFIED
		, TAGS
		, IS_SHARED
		// About these fields:
		// First they have been only stored in this objects table CFW_STOREDFILE as JSON.
		// Now the primary data is stored in the respective tables >> CFW_STOREDFILE_*_MAP.
		// Data in CFW_STOREDFILE is secondary but still updated, as it is used for sharing management.
		, JSON_SHARE_WITH_USERS
		, JSON_SHARE_WITH_GROUPS
		, JSON_EDITORS
		, JSON_EDITOR_GROUPS
		
		, TIME_CREATED
		, LAST_UPDATED
		, IS_ARCHIVED
	}

	private static Logger logger = CFWLog.getLogger(CFWStoredFile.class.getName());
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, CFWStoredFileFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the Stored File.")
			.apiFieldType(FormFieldType.NUMBER)
			;
	
	private CFWField<Integer> foreignKeyOwner = CFWField.newInteger(FormFieldType.HIDDEN, CFWStoredFileFields.FK_ID_OWNER)
			.setForeignKeyCascade(this, User.class, UserFields.PK_ID)
			.setDescription("The user id of the owner of the Stored File.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<String> name = CFWField.newString(FormFieldType.TEXT, CFWStoredFileFields.NAME)
			.setColumnDefinition("VARCHAR(255)")
			.setDescription("The name used to identify the Stored File.")
			.addValidator(new LengthValidator(1, 255))
			;
	
	private CFWField<String> data = CFWField.newBlob(FormFieldType.NONE, CFWStoredFileFields.DATA)
			.setDescription("The data of the file.")
			;
	
	private CFWField<Long> size = CFWField.newLong(FormFieldType.UNMODIFIABLE_TEXT, CFWStoredFileFields.SIZE)
			.setDescription("The size of the file in bytes.")
			;
	
	private CFWField<String> mimetype = CFWField.newString(FormFieldType.UNMODIFIABLE_TEXT, CFWStoredFileFields.MIMETYPE)
			.setDescription("The mimetype of the file.")
			;
	
	private CFWField<String> extension = CFWField.newString(FormFieldType.UNMODIFIABLE_TEXT, CFWStoredFileFields.EXTENSION)
			.setDescription("(Optional)The file-extension of the file.")
			;
	
	private CFWField<Timestamp> lastModified = CFWField.newTimestamp(FormFieldType.NONE, CFWStoredFileFields.LAST_MODIFIED)
			.setDescription("(Optional)The modification time of the file.")
			;
	
	private CFWField<String> description = CFWField.newString(FormFieldType.TEXTAREA, CFWStoredFileFields.DESCRIPTION)
			.setDescription("The description of the Stored File.")
			.addValidator(new LengthValidator(-1, 2000000));
	
	private CFWField<ArrayList<String>> tags = CFWField.newArray(FormFieldType.TAGS, CFWStoredFileFields.TAGS)
			.setDescription("The tags for this Stored File.")
			.setAutocompleteHandler( new CFWAutocompleteHandler(10) {

				@Override
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String inputValue, int cursorPosition) {
					
					AutocompleteList list = new AutocompleteList();
					for(String tag : CFW.DB.StoredFile.getTags()) {
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
	
	private CFWField<Boolean> isShared = CFWField.newBoolean(FormFieldType.BOOLEAN, CFWStoredFileFields.IS_SHARED)
			.apiFieldType(FormFieldType.TEXT)
			.setDescription("Make the Stored File shared with other people or keep it private. If no shared users or shared groups are specified(defined editors have no impact), the Stored File is shared with all users having access to the Stored File features.")
			.setValue(CFW.DB.Config.getConfigAsBoolean(FeatureFilemanager.CONFIG_CATEGORY, FeatureFilemanager.CONFIG_DEFAULT_IS_SHARED));
	
	private CFWField<LinkedHashMap<String,String>> shareWithUsers = this.createSelectorFieldSharedUser(null);
	
	private CFWField<LinkedHashMap<String,String>> shareWithGroups = this.createSelectorFieldSharedGroups(null);
		
	private CFWField<LinkedHashMap<String,String>> editors = this.createSelectorFieldEditors(null);
		
	private CFWField<LinkedHashMap<String,String>> editorGroups = this.createSelectorFieldEditorGroups(null);
	
	private CFWField<Timestamp> timeCreated = CFWField.newTimestamp(FormFieldType.NONE, CFWStoredFileFields.TIME_CREATED)
			.setDescription("The date and time the Stored File was created.")
			.setValue(new Timestamp(new Date().getTime()));
	
	private CFWField<Timestamp> lastUpdated = CFWField.newTimestamp(FormFieldType.NONE, CFWStoredFileFields.LAST_UPDATED)
			.setDescription("The date and time the Stored File was last updated. Will be null if automatic version was created.")
			.setValue(new Timestamp(new Date().getTime()));
	

	private CFWField<Boolean> isArchived = CFWField.newBoolean(FormFieldType.NONE, CFWStoredFileFields.IS_ARCHIVED)
			.setColumnDefinition("BOOLEAN DEFAULT FALSE")
			.setDescription("Flag to define if the Stored File is archived.")
			.setValue(false)
			;
	
	
	public CFWStoredFile() {
		initializeFields();
	}
		
	public CFWStoredFile(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(
				  id
				, foreignKeyOwner
				, name
				, description
				, data
				, size
				, mimetype
				, extension
				, lastModified
				, tags
				, isShared
				, shareWithUsers
				, shareWithGroups
				, editors
				, editorGroups
				, timeCreated
				, lastUpdated
				, isArchived
			);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
		
		String[] inputFields = 
				new String[] {
						CFWStoredFileFields.PK_ID.toString(), 
						CFWStoredFileFields.NAME.toString(),
						CFWStoredFileFields.IS_ARCHIVED.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						CFWStoredFileFields.PK_ID.toString(), 
						CFWStoredFileFields.FK_ID_OWNER.toString(),
						CFWStoredFileFields.NAME.toString(),
						CFWStoredFileFields.DESCRIPTION.toString(),
						CFWStoredFileFields.TAGS.toString(),
						CFWStoredFileFields.IS_SHARED.toString(),
						CFWStoredFileFields.JSON_SHARE_WITH_USERS.toString(),
						CFWStoredFileFields.JSON_SHARE_WITH_GROUPS.toString(),
						CFWStoredFileFields.JSON_EDITORS.toString(),
						CFWStoredFileFields.JSON_EDITOR_GROUPS.toString(),
						CFWStoredFileFields.TIME_CREATED.toString(),
						CFWStoredFileFields.LAST_UPDATED.toString(),	
						CFWStoredFileFields.IS_ARCHIVED.toString(),
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
		this.addFieldAfter(sharedUserSelector, CFWStoredFileFields.IS_SHARED);
		
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
				selectedValue = CFW.DB.StoredFileSharedUsers
								.selectUsersForStoredFileAsKeyLabel(boardID);
		 }
		 
		//--------------------------------------
		// Create Field
		return CFWField.newTagsSelector(FIELDNAME_SHARE_WITH_USERS)
						.setDescription("Share this Stored File with specific users.")
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
			selectedValue = CFW.DB.StoredFileSharedGroups
								.selectGroupsForStoredFileAsKeyLabel(boardID);
		}
		
		//--------------------------------------
		// Create Field
		return CFWField.newTagsSelector(FIELDNAME_SHARE_WITH_GROUPS)
				.setLabel("Share with Groups")
				.setDescription("Share this Stored File with specific groups.")
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
				selectedValue = CFW.DB.StoredFileEditors
								.selectUsersForStoredFileAsKeyLabel(boardID);
		}
		 
		//--------------------------------------
		// Create Field
		return CFWField.newTagsSelector(FIELDNAME_EDITORS)
				.setLabel("Editors")
				.setDescription("Allow the specified users to view and edit the Stored File, even when the Stored File is not shared.")
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
			selectedValue = CFW.DB.StoredFileEditorGroups
								.selectGroupsForStoredFileAsKeyLabel(boardID);
		}
		
		//--------------------------------------
		// Create Field
		return CFWField.newTagsSelector(FIELDNAME_EDITOR_GROUPS)
				.setLabel("Editor Groups")
				.setDescription("Allow users having at least one of the specified groups to view and edit the Stored File, even when the Stored File is not shared.")
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
			isSuccess &= saveSelectorField(FIELDNAME_EDITOR_GROUPS);
		
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
			
			switch(fieldname) {
				case FIELDNAME_SHARE_WITH_USERS:
					success &= CFW.DB.StoredFileSharedUsers.updateUserStoredFileAssignments(this, selectedValues);
					break;
					
				case FIELDNAME_SHARE_WITH_GROUPS:
					success &= CFW.DB.StoredFileSharedGroups.updateGroupStoredFileAssignments(this, selectedValues);
					break;
					
				case FIELDNAME_EDITORS:
					success &= CFW.DB.StoredFileEditors.updateUserStoredFileAssignments(this, selectedValues);
					break;
					
				case FIELDNAME_EDITOR_GROUPS:
					success &= CFW.DB.StoredFileEditorGroups.updateGroupStoredFileAssignments(this, selectedValues);
					break;
				
				default: new CFWLog(logger).severe("Development Error: unsupported value.");
			}
			if( !success ){
				CFW.Messages.addErrorMessage("Error while saving user assignments for field: "+fieldname);
			}
		}
		
		return success;
	}
	
	
	/******************************************************************
	 *
	 ******************************************************************/
	public JsonObject createJsonObject() {
		
		JsonObject result = new JsonObject();
		
		result.addProperty("name", name.getValue());
		result.addProperty("domain", size.getValue());
		result.addProperty("hostname", mimetype.getValue());
		result.addProperty("url", extension.getValue());
		result.addProperty("custom", lastModified.getValue().getTime());
		
		return result;
	}
	
	

	public Integer id() {
		return id.getValue();
	}
	
	public CFWStoredFile id(Integer id) {
		this.id.setValue(id);
		return this;
	}
	
	public Integer foreignKeyOwner() {
		return foreignKeyOwner.getValue();
	}
	
	public CFWStoredFile foreignKeyOwner(Integer foreignKeyUser) {
		this.foreignKeyOwner.setValue(foreignKeyUser);
		return this;
	}
		
	public String name() {
		return name.getValue();
	}
	
	public CFWStoredFile name(String name) {
		this.name.setValue(name);
		return this;
	}
	
	public String mimetype() {
		return mimetype.getValue();
	}
	
	public CFWStoredFile mimetype(String value) {
		this.mimetype.setValue(value);
		return this;
	}
	
	public String extension() {
		return extension.getValue();
	}
	
	public CFWStoredFile extension(String value) {
		this.extension.setValue(value);
		return this;
	}
	
	public Long size() {
		return size.getValue();
	}
	
	public CFWStoredFile size(Long value) {
		this.size.setValue(value);
		return this;
	}
	
	public Timestamp lastModified() {
		return lastModified.getValue();
	}
	
	public CFWStoredFile lastModified(Long value) {
		this.lastModified.setValue(new Timestamp(value));
		return this;
	}
	
	public String description() {
		return description.getValue();
	}

	public CFWStoredFile description(String description) {
		this.description.setValue(description);
		return this;
	}
	

	public ArrayList<String> tags() {
		return tags.getValue();
	}
	
	public CFWStoredFile tags(ArrayList<String> tags) {
		this.tags.setValue(tags);
		return this;
	}
	
	public boolean isShared() {
		return isShared.getValue();
	}
	
	public CFWStoredFile isShared(boolean isShared) {
		this.isShared.setValue(isShared);
		return this;
	}
		
	public LinkedHashMap<String,String> sharedWithUsers() {
		if(shareWithUsers.getValue() == null) { return new LinkedHashMap<>(); }
		return shareWithUsers.getValue();
	}
	
	public CFWStoredFile sharedWithUsers(LinkedHashMap<String,String> sharedWithUsers) {
		this.shareWithUsers.setValue(sharedWithUsers);
		return this;
	}
	
	public LinkedHashMap<String,String> sharedWithGroups() {
		if(shareWithGroups.getValue() == null) { return new LinkedHashMap<>(); }
		return shareWithGroups.getValue();
	}
	
	public CFWStoredFile sharedWithGroups(LinkedHashMap<String,String> value) {
		this.shareWithGroups.setValue(value);
		return this;
	}
	public LinkedHashMap<String,String> editors() {
		if(editors.getValue() == null) { return new LinkedHashMap<>(); }
		return editors.getValue();
	}
	
	public CFWStoredFile editors(LinkedHashMap<String,String> editors) {
		this.editors.setValue(editors);
		return this;
	}
	
	public LinkedHashMap<String,String> editorGroups() {
		if(editorGroups.getValue() == null) { return new LinkedHashMap<>(); }
		return editorGroups.getValue();
	}
	
	public CFWStoredFile editorGroups(LinkedHashMap<String,String> value) {
		this.editorGroups.setValue(value);
		return this;
	}
	
	
	public Timestamp timeCreated() {
		return timeCreated.getValue();
	}
	
	public CFWStoredFile timeCreated(Timestamp creationDate) {
		this.timeCreated.setValue(creationDate);
		return this;
	}
	
	public Timestamp lastUpdated() {
		return lastUpdated.getValue();
	}
	
	public CFWStoredFile lastUpdated(Timestamp value) {
		this.lastUpdated.setValue(value);
		return this;
	}
	
	public boolean isArchived() {
		return isArchived.getValue();
	}
	
	public CFWStoredFile isArchived(boolean isRenamable) {
		this.isArchived.setValue(isRenamable);
		return this;
	}	
	
}
