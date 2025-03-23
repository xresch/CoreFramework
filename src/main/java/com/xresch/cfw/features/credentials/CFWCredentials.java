package com.xresch.cfw.features.credentials;

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
public class CFWCredentials extends CFWObject {
	
	// !!! IMPORTANT !!! never change these salt-string! 
	private static final String CREDENTIALS_PW_SALT = "CredentialsPW-Default-Salt"; 
	private static final String CREDENTIALS_TOKEN_SALT = "CredentialsToken-Default-Salt"; 
	private static final String CREDENTIALS_SECRET_SALT = "CredentialsSecret-Default-Salt"; 

	public static final String TABLE_NAME = "CFW_CREDENTIALS";
	
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
	
	public enum CFWCredentialsFields{
		PK_ID
		, FK_ID_OWNER
		, NAME
		, DESCRIPTION
		, ACCOUNT
		, PASSWORD
		, TOKEN
		, SECRET
		, DOMAIN
		, HOSTNAME
		, URL
		, DATA
		, CUSTOM
		, SALT
		, TAGS
		, IS_SHARED
		// About these fields:
		// First they have been only stored in this objects table CFW_CREDENTIALS as JSON.
		// Now the primary data is stored in the respective tables >> CFW_CREDENTIALS_*_MAP.
		// Data in CFW_CREDENTIALS is secondary but still updated, as it is used for sharing management.
		, JSON_SHARE_WITH_USERS
		, JSON_SHARE_WITH_GROUPS
		, JSON_EDITORS
		, JSON_EDITOR_GROUPS
		
		, TIME_CREATED
		, LAST_UPDATED
		, IS_ARCHIVED
	}

	private static Logger logger = CFWLog.getLogger(CFWCredentials.class.getName());
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, CFWCredentialsFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the credentials.")
			.apiFieldType(FormFieldType.NUMBER)
			;
	
	private CFWField<Integer> foreignKeyOwner = CFWField.newInteger(FormFieldType.HIDDEN, CFWCredentialsFields.FK_ID_OWNER)
			.setForeignKeyCascade(this, User.class, UserFields.PK_ID)
			.setDescription("The user id of the owner of the credentials.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<String> name = CFWField.newString(FormFieldType.TEXT, CFWCredentialsFields.NAME)
			.setColumnDefinition("VARCHAR(255)")
			.setDescription("The name used to identify the credentials.")
			.addValidator(new LengthValidator(1, 255))
			;
	
	private CFWField<String> account = CFWField.newString(FormFieldType.TEXT, CFWCredentialsFields.ACCOUNT)
			.setDescription("(Optional)The account or username of the credentials.")
			;
	
	private CFWField<String> password = CFWField.newString(FormFieldType.PASSWORD, CFWCredentialsFields.PASSWORD)
			.setDescription("(Optional)The password of the credentials.")
			.enableEncryption(CREDENTIALS_PW_SALT) // !!! IMPORTANT !!! never change this string! 
			;
	
	private CFWField<String> token = CFWField.newString(FormFieldType.PASSWORD, CFWCredentialsFields.TOKEN)
			.setDescription("(Optional)The token of the credentials.")
			.enableEncryption(CREDENTIALS_TOKEN_SALT) // !!! IMPORTANT !!! never change this string! 
			;
	
	private CFWField<String> secret = CFWField.newString(FormFieldType.PASSWORD, CFWCredentialsFields.SECRET)
			.setDescription("(Optional)The secret of the credentials.")
			.enableEncryption(CREDENTIALS_SECRET_SALT) // !!! IMPORTANT !!! never change this string! 
			;
	
	private CFWField<String> salt = CFWField.newString(FormFieldType.NONE, CFWCredentialsFields.SALT)
			.setDescription("The salt for the encrypting the password.")
			.setValue(CFW.Random.stringAlphaNumSpecial(32))
			;
	
	private CFWField<String> domain = CFWField.newString(FormFieldType.TEXT, CFWCredentialsFields.DOMAIN)
			.setDescription("(Optional)The domain of the credentials.")
			;
	
	private CFWField<String> hostname = CFWField.newString(FormFieldType.TEXT, CFWCredentialsFields.HOSTNAME)
			.setDescription("(Optional)The hostname of the credentials.")
			;
	
	private CFWField<String> url = CFWField.newString(FormFieldType.TEXT, CFWCredentialsFields.URL)
			.setDescription("(Optional)The URL of the credentials.")
			;
	
	private CFWField<String> data = CFWField.newString(FormFieldType.TEXTAREA, CFWCredentialsFields.DATA)
			.setDescription("(Optional)Te data for the credentials.")
			;
	
	private CFWField<String> custom = CFWField.newString(FormFieldType.TEXTAREA, CFWCredentialsFields.CUSTOM)
			.setDescription("(Optional)A custom value for the credentials.")
			;
	
	private CFWField<String> description = CFWField.newString(FormFieldType.TEXTAREA, CFWCredentialsFields.DESCRIPTION)
			.setDescription("The description of the credentials.")
			.addValidator(new LengthValidator(-1, 2000000));
	
	private CFWField<ArrayList<String>> tags = CFWField.newArray(FormFieldType.TAGS, CFWCredentialsFields.TAGS)
			.setDescription("The tags for this credentials.")
			.setAutocompleteHandler( new CFWAutocompleteHandler(10) {

				@Override
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String inputValue, int cursorPosition) {
					
					AutocompleteList list = new AutocompleteList();
					for(String tag : CFW.DB.Credentials.getTags()) {
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
	
	private CFWField<Boolean> isShared = CFWField.newBoolean(FormFieldType.BOOLEAN, CFWCredentialsFields.IS_SHARED)
			.apiFieldType(FormFieldType.TEXT)
			.setDescription("Make the credentials shared with other people or keep it private. If no shared users or shared groups are specified(defined editors have no impact), the credentials is shared with all users having access to the credentials features.")
			.setValue(CFW.DB.Config.getConfigAsBoolean(FeatureCredentials.CONFIG_CATEGORY, FeatureCredentials.CONFIG_DEFAULT_IS_SHARED));
	
	private CFWField<LinkedHashMap<String,String>> shareWithUsers = this.createSelectorFieldSharedUser(null);
	
	private CFWField<LinkedHashMap<String,String>> shareWithGroups = this.createSelectorFieldSharedGroups(null);
		
	private CFWField<LinkedHashMap<String,String>> editors = this.createSelectorFieldEditors(null);
		
	private CFWField<LinkedHashMap<String,String>> editorGroups = this.createSelectorFieldEditorGroups(null);
	
	private CFWField<Timestamp> timeCreated = CFWField.newTimestamp(FormFieldType.NONE, CFWCredentialsFields.TIME_CREATED)
			.setDescription("The date and time the credentials was created.")
			.setValue(new Timestamp(new Date().getTime()));
	
	private CFWField<Timestamp> lastUpdated = CFWField.newTimestamp(FormFieldType.NONE, CFWCredentialsFields.LAST_UPDATED)
			.setDescription("The date and time the credentials was last updated. Will be null if automatic version was created.")
			.setValue(new Timestamp(new Date().getTime()));
	

	private CFWField<Boolean> isArchived = CFWField.newBoolean(FormFieldType.NONE, CFWCredentialsFields.IS_ARCHIVED)
			.setColumnDefinition("BOOLEAN DEFAULT FALSE")
			.setDescription("Flag to define if the credentials is archived.")
			.setValue(false)
			;
	
	
	public CFWCredentials() {
		initializeFields();
	}
		
	public CFWCredentials(ResultSet result) throws SQLException {
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
				, account
				, password
				, token
				, secret
				, salt
				, domain
				, hostname
				, url
				, data
				, custom
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
						CFWCredentialsFields.PK_ID.toString(), 
						CFWCredentialsFields.NAME.toString(),
						CFWCredentialsFields.IS_ARCHIVED.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						CFWCredentialsFields.PK_ID.toString(), 
						CFWCredentialsFields.FK_ID_OWNER.toString(),
						CFWCredentialsFields.NAME.toString(),
						CFWCredentialsFields.DESCRIPTION.toString(),
						CFWCredentialsFields.TAGS.toString(),
						CFWCredentialsFields.IS_SHARED.toString(),
						CFWCredentialsFields.JSON_SHARE_WITH_USERS.toString(),
						CFWCredentialsFields.JSON_SHARE_WITH_GROUPS.toString(),
						CFWCredentialsFields.JSON_EDITORS.toString(),
						CFWCredentialsFields.JSON_EDITOR_GROUPS.toString(),
						CFWCredentialsFields.TIME_CREATED.toString(),
						CFWCredentialsFields.LAST_UPDATED.toString(),	
						CFWCredentialsFields.IS_ARCHIVED.toString(),
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
		this.addFieldAfter(sharedUserSelector, CFWCredentialsFields.IS_SHARED);
		
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
				selectedValue = CFW.DB.CredentialsSharedUsers
								.selectUsersForCredentialsAsKeyLabel(boardID);
		 }
		 
		//--------------------------------------
		// Create Field
		return CFWField.newTagsSelector(FIELDNAME_SHARE_WITH_USERS)
						.setDescription("Share this credentials with specific users.")
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
			selectedValue = CFW.DB.CredentialsSharedGroups
								.selectGroupsForCredentialsAsKeyLabel(boardID);
		}
		
		//--------------------------------------
		// Create Field
		return CFWField.newTagsSelector(FIELDNAME_SHARE_WITH_GROUPS)
				.setLabel("Share with Groups")
				.setDescription("Share this credentials with specific groups.")
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
				selectedValue = CFW.DB.CredentialsEditors
								.selectUsersForCredentialsAsKeyLabel(boardID);
		}
		 
		//--------------------------------------
		// Create Field
		return CFWField.newTagsSelector(FIELDNAME_EDITORS)
				.setLabel("Editors")
				.setDescription("Allow the specified users to view and edit the credentials, even when the credentials is not shared.")
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
			selectedValue = CFW.DB.CredentialsEditorGroups
								.selectGroupsForCredentialsAsKeyLabel(boardID);
		}
		
		//--------------------------------------
		// Create Field
		return CFWField.newTagsSelector(FIELDNAME_EDITOR_GROUPS)
				.setLabel("Editor Groups")
				.setDescription("Allow users having at least one of the specified groups to view and edit the credentials, even when the credentials is not shared.")
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
					success &= CFW.DB.CredentialsSharedUsers.updateUserCredentialsAssignments(this, selectedValues);
					break;
					
				case FIELDNAME_SHARE_WITH_GROUPS:
					success &= CFW.DB.CredentialsSharedGroups.updateGroupCredentialsAssignments(this, selectedValues);
					break;
					
				case FIELDNAME_EDITORS:
					success &= CFW.DB.CredentialsEditors.updateUserCredentialsAssignments(this, selectedValues);
					break;
					
				case FIELDNAME_EDITOR_GROUPS:
					success &= CFW.DB.CredentialsEditorGroups.updateGroupCredentialsAssignments(this, selectedValues);
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
		result.addProperty("account", account.getValue());
		result.addProperty("password", password.getValue());
		result.addProperty("token", token.getValue());
		result.addProperty("secret", secret.getValue());
		result.addProperty("domain", domain.getValue());
		result.addProperty("hostname", hostname.getValue());
		result.addProperty("url", url.getValue());
		result.addProperty("data", data.getValue());
		result.addProperty("custom", custom.getValue());
		
		return result;
	}
	
	

	public Integer id() {
		return id.getValue();
	}
	
	public CFWCredentials id(Integer id) {
		this.id.setValue(id);
		return this;
	}
	
	public Integer foreignKeyOwner() {
		return foreignKeyOwner.getValue();
	}
	
	public CFWCredentials foreignKeyOwner(Integer foreignKeyUser) {
		this.foreignKeyOwner.setValue(foreignKeyUser);
		return this;
	}
		
	public String name() {
		return name.getValue();
	}
	
	public CFWCredentials name(String name) {
		this.name.setValue(name);
		return this;
	}
	
	public String description() {
		return description.getValue();
	}

	public CFWCredentials description(String description) {
		this.description.setValue(description);
		return this;
	}
	
	public String getPasswordDecrypted() {
		String salt = this.salt.getValue();
		String decryptedValue = CFW.Security.decryptValue(password.getValue(), salt);
		return decryptedValue;
	}
	
	public CFWCredentials setPasswordEncrypted(String value) {
		String salt = this.salt.getValue();
		String encryptedValue = CFW.Security.encryptValue(value, salt);
		this.password.setValue(encryptedValue);
		return this;
	}
	
	public String getTokenDecrypted() {
		String salt = this.salt.getValue();
		String decryptedValue = CFW.Security.decryptValue(token.getValue(), salt);
		return decryptedValue;
	}
	
	public CFWCredentials setTokenEncrypted(String value) {
		String salt = this.salt.getValue();
		String encryptedValue = CFW.Security.encryptValue(value, salt);
		this.token.setValue(encryptedValue);
		return this;
	}
	
	public String getSecretDecrypted() {
		String salt = this.salt.getValue();
		String decryptedValue = CFW.Security.decryptValue(secret.getValue(), salt);
		return decryptedValue;
	}
	
	public CFWCredentials setSecretEncrypted(String value) {
		String salt = this.salt.getValue();
		String encryptedValue = CFW.Security.encryptValue(value, salt);
		this.secret.setValue(encryptedValue);
		return this;
	}
	

	public ArrayList<String> tags() {
		return tags.getValue();
	}
	
	public CFWCredentials tags(ArrayList<String> tags) {
		this.tags.setValue(tags);
		return this;
	}
	
	public boolean isShared() {
		return isShared.getValue();
	}
	
	public CFWCredentials isShared(boolean isShared) {
		this.isShared.setValue(isShared);
		return this;
	}
		
	public LinkedHashMap<String,String> sharedWithUsers() {
		if(shareWithUsers.getValue() == null) { return new LinkedHashMap<>(); }
		return shareWithUsers.getValue();
	}
	
	public CFWCredentials sharedWithUsers(LinkedHashMap<String,String> sharedWithUsers) {
		this.shareWithUsers.setValue(sharedWithUsers);
		return this;
	}
	
	public LinkedHashMap<String,String> sharedWithGroups() {
		if(shareWithGroups.getValue() == null) { return new LinkedHashMap<>(); }
		return shareWithGroups.getValue();
	}
	
	public CFWCredentials sharedWithGroups(LinkedHashMap<String,String> value) {
		this.shareWithGroups.setValue(value);
		return this;
	}
	public LinkedHashMap<String,String> editors() {
		if(editors.getValue() == null) { return new LinkedHashMap<>(); }
		return editors.getValue();
	}
	
	public CFWCredentials editors(LinkedHashMap<String,String> editors) {
		this.editors.setValue(editors);
		return this;
	}
	
	public LinkedHashMap<String,String> editorGroups() {
		if(editorGroups.getValue() == null) { return new LinkedHashMap<>(); }
		return editorGroups.getValue();
	}
	
	public CFWCredentials editorGroups(LinkedHashMap<String,String> value) {
		this.editorGroups.setValue(value);
		return this;
	}
	
	
	public Timestamp timeCreated() {
		return timeCreated.getValue();
	}
	
	public CFWCredentials timeCreated(Timestamp creationDate) {
		this.timeCreated.setValue(creationDate);
		return this;
	}
	
	public Timestamp lastUpdated() {
		return lastUpdated.getValue();
	}
	
	public CFWCredentials lastUpdated(Timestamp value) {
		this.lastUpdated.setValue(value);
		return this;
	}
	
	public boolean isArchived() {
		return isArchived.getValue();
	}
	
	public CFWCredentials isArchived(boolean isRenamable) {
		this.isArchived.setValue(isRenamable);
		return this;
	}	
	
}
