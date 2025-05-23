package com.xresch.cfw.features.query.store;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
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
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.commands.CFWQueryCommandParamDefaults;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.CFWQueryToken;
import com.xresch.cfw.features.query.parse.CFWQueryToken.CFWQueryTokenType;
import com.xresch.cfw.features.query.parse.CFWQueryTokenizer;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.features.usermgmt.User.UserFields;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.logging.SysoutInterceptor;
import com.xresch.cfw.validation.LengthValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public class CFWStoredQuery extends CFWObject {
	
	// !!! IMPORTANT !!! never change these salt-string! 
	private static final String STOREDQUERY_PW_SALT = "StoredQueryPW-Default-Salt"; 
	private static final String STOREDQUERY_TOKEN_SALT = "StoredQueryToken-Default-Salt"; 
	private static final String STOREDQUERY_SECRET_SALT = "StoredQuerySecret-Default-Salt"; 

	public static final String TABLE_NAME = "CFW_QUERY_STORED";
	
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
	
	public enum CFWStoredQueryFields{
		  PK_ID
		, FK_ID_OWNER
		, NAME
		, DESCRIPTION
		, QUERY
		, MAKE_WIDGET
		, WIDGET_CATEGORY
		, CHECK_PERMISSIONS
		, IS_SHARED
		, QUERY_PARAMS_DEFINED // will be set when saved to DB
		, QUERY_PARAMS	// will be set when saved to DB
		, TAGS
		// About these fields:
		// First they have been only stored in this objects table CFW_QUERY_STORED as JSON.
		// Now the primary data is stored in the respective tables >> CFW_QUERY_STORED_*_MAP.
		// Data in CFW_QUERY_STORED is secondary but still updated, as it is used for sharing management.
		, JSON_SHARE_WITH_USERS
		, JSON_SHARE_WITH_GROUPS
		, JSON_EDITORS
		, JSON_EDITOR_GROUPS
		
		, TIME_CREATED
		, LAST_UPDATED
		, IS_ARCHIVED
	}

	private static Logger logger = CFWLog.getLogger(CFWStoredQuery.class.getName());
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, CFWStoredQueryFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the stored query.")
			.apiFieldType(FormFieldType.NUMBER)
			;
	
	private CFWField<Integer> foreignKeyOwner = CFWField.newInteger(FormFieldType.HIDDEN, CFWStoredQueryFields.FK_ID_OWNER)
			.setForeignKeyCascade(this, User.class, UserFields.PK_ID)
			.setDescription("The user id of the owner of the stored query.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<String> name = CFWField.newString(FormFieldType.TEXT, CFWStoredQueryFields.NAME)
			.setColumnDefinition("VARCHAR(255)")
			.setDescription("The name used to identify the stored query.")
			.addValidator(new LengthValidator(1, 255))
			;
	
	private CFWField<String> query = CFWField.newString(FormFieldType.QUERY_EDITOR, CFWStoredQueryFields.QUERY)
			.setDescription("The stored query.")
			.disableSanitization()
			.addValidator(new LengthValidator(1, -1))
			;
	
	private CFWField<Boolean> checkPermissions = CFWField.newBoolean(FormFieldType.BOOLEAN, CFWStoredQueryFields.CHECK_PERMISSIONS)
			.setDescription("(Optional)Set if permissions are checked while executing the stored query from another query.")
			.setValue(false)
			;
	
	private CFWField<Boolean> makeWidget = CFWField.newBoolean(FormFieldType.BOOLEAN, CFWStoredQueryFields.MAKE_WIDGET)
			.setDescription("Toggle if this stored query should be made into a widget. Make sure to define parameters as well.")
			.setValue(false)
			;
	
	private CFWField<String> widgetCategory = CFWField.newString(FormFieldType.TEXT, CFWStoredQueryFields.WIDGET_CATEGORY)
			.setDescription("(Optional)Define a category name for the widget.")
			;
	
	// will be filled by code
	private CFWField<Boolean> queryParamsDefined = CFWField.newBoolean(FormFieldType.NONE, CFWStoredQueryFields.QUERY_PARAMS_DEFINED)
			.setDescription("Indicates if the stored query has parameters defined in the query or not.")
			.setValue(false)
			;
	
	// will be filled by code
	private CFWField<String> queryParams = CFWField.newString(FormFieldType.NONE, CFWStoredQueryFields.QUERY_PARAMS)
			.setDescription("The query parameter definition that is used for autocomplete.")
			;
	
	private CFWField<String> description = CFWField.newString(FormFieldType.TEXTAREA, CFWStoredQueryFields.DESCRIPTION)
			.setDescription("(Optional)The description of the stored query.")
			.addValidator(new LengthValidator(-1, 2000000));
	
	private CFWField<ArrayList<String>> tags = CFWField.newArray(FormFieldType.TAGS, CFWStoredQueryFields.TAGS)
			.setDescription("The tags for this stored query.")
			.setAutocompleteHandler( new CFWAutocompleteHandler(10) {

				@Override
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String inputValue, int cursorPosition) {
					
					AutocompleteList list = new AutocompleteList();
					for(String tag : CFW.DB.StoredQuery.getTags()) {
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
	
	private CFWField<Boolean> isShared = CFWField.newBoolean(FormFieldType.BOOLEAN, CFWStoredQueryFields.IS_SHARED)
			.apiFieldType(FormFieldType.TEXT)
			.setDescription("Make the stored query shared with other people or keep it private. If no shared users or shared groups are specified(defined editors have no impact), the stored query is shared with all users having access to the stored query features.")
			.setValue(CFW.DB.Config.getConfigAsBoolean(FeatureStoredQuery.CONFIG_CATEGORY, FeatureStoredQuery.CONFIG_DEFAULT_IS_SHARED));
	
	private CFWField<LinkedHashMap<String,String>> shareWithUsers = this.createSelectorFieldSharedUser(null);
	
	private CFWField<LinkedHashMap<String,String>> shareWithGroups = this.createSelectorFieldSharedGroups(null);
		
	private CFWField<LinkedHashMap<String,String>> editors = this.createSelectorFieldEditors(null);
		
	private CFWField<LinkedHashMap<String,String>> editorGroups = this.createSelectorFieldEditorGroups(null);
	
	private CFWField<Timestamp> timeCreated = CFWField.newTimestamp(FormFieldType.NONE, CFWStoredQueryFields.TIME_CREATED)
			.setDescription("The date and time the stored query was created.")
			.setValue(new Timestamp(new Date().getTime()));
	
	private CFWField<Timestamp> lastUpdated = CFWField.newTimestamp(FormFieldType.NONE, CFWStoredQueryFields.LAST_UPDATED)
			.setDescription("The date and time the stored query was last updated. Will be null if automatic version was created.")
			.setValue(new Timestamp(new Date().getTime()));
	

	private CFWField<Boolean> isArchived = CFWField.newBoolean(FormFieldType.NONE, CFWStoredQueryFields.IS_ARCHIVED)
			.setColumnDefinition("BOOLEAN DEFAULT FALSE")
			.setDescription("Flag to define if the stored query is archived.")
			.setValue(false)
			;
	
	
	public CFWStoredQuery() {
		initializeFields();
	}
		
	public CFWStoredQuery(ResultSet result) throws SQLException {
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
				, query
				, makeWidget
				, widgetCategory
				, queryParamsDefined
				, queryParams
				, tags
				, checkPermissions
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
						CFWStoredQueryFields.PK_ID.toString(), 
						CFWStoredQueryFields.NAME.toString(),
						CFWStoredQueryFields.IS_ARCHIVED.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						CFWStoredQueryFields.PK_ID.toString(), 
						CFWStoredQueryFields.FK_ID_OWNER.toString(),
						CFWStoredQueryFields.NAME.toString(),
						CFWStoredQueryFields.DESCRIPTION.toString(),
						CFWStoredQueryFields.QUERY.toString(),
						CFWStoredQueryFields.TAGS.toString(),
						CFWStoredQueryFields.MAKE_WIDGET.toString(),
						CFWStoredQueryFields.WIDGET_CATEGORY.toString(),
						CFWStoredQueryFields.IS_SHARED.toString(),
						CFWStoredQueryFields.JSON_SHARE_WITH_USERS.toString(),
						CFWStoredQueryFields.JSON_SHARE_WITH_GROUPS.toString(),
						CFWStoredQueryFields.JSON_EDITORS.toString(),
						CFWStoredQueryFields.JSON_EDITOR_GROUPS.toString(),
						CFWStoredQueryFields.TIME_CREATED.toString(),
						CFWStoredQueryFields.LAST_UPDATED.toString(),	
						CFWStoredQueryFields.IS_ARCHIVED.toString(),
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
		this.addFieldAfter(sharedUserSelector, CFWStoredQueryFields.IS_SHARED);
		
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
				selectedValue = CFW.DB.StoredQuerySharedUsers
								.selectUsersForStoredQueryAsKeyLabel(boardID);
		 }
		 
		//--------------------------------------
		// Create Field
		return CFWField.newTagsSelector(FIELDNAME_SHARE_WITH_USERS)
						.setDescription("Share this stored query with specific users.")
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
			selectedValue = CFW.DB.StoredQuerySharedGroups
								.selectGroupsForStoredQueryAsKeyLabel(boardID);
		}
		
		//--------------------------------------
		// Create Field
		return CFWField.newTagsSelector(FIELDNAME_SHARE_WITH_GROUPS)
				.setLabel("Share with Groups")
				.setDescription("Share this stored query with specific groups.")
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
				selectedValue = CFW.DB.StoredQueryEditors
								.selectUsersForStoredQueryAsKeyLabel(boardID);
		}
		 
		//--------------------------------------
		// Create Field
		return CFWField.newTagsSelector(FIELDNAME_EDITORS)
				.setLabel("Editors")
				.setDescription("Allow the specified users to view and edit the stored query, even when the stored query is not shared.")
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
			selectedValue = CFW.DB.StoredQueryEditorGroups
								.selectGroupsForStoredQueryAsKeyLabel(boardID);
		}
		
		//--------------------------------------
		// Create Field
		return CFWField.newTagsSelector(FIELDNAME_EDITOR_GROUPS)
				.setLabel("Editor Groups")
				.setDescription("Allow users having at least one of the specified groups to view and edit the stored query, even when the stored query is not shared.")
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
					success &= CFW.DB.StoredQuerySharedUsers.updateUserStoredQueryAssignments(this, selectedValues);
					break;
					
				case FIELDNAME_SHARE_WITH_GROUPS:
					success &= CFW.DB.StoredQuerySharedGroups.updateGroupStoredQueryAssignments(this, selectedValues);
					break;
					
				case FIELDNAME_EDITORS:
					success &= CFW.DB.StoredQueryEditors.updateUserStoredQueryAssignments(this, selectedValues);
					break;
					
				case FIELDNAME_EDITOR_GROUPS:
					success &= CFW.DB.StoredQueryEditorGroups.updateGroupStoredQueryAssignments(this, selectedValues);
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
	public void updateQueryParams() {
		
		String queryString = this.query();
		
		CFWQueryAutocompleteHelper subHelper = 
				new CFWQueryAutocompleteHelper(null, this.query(), 0);
		
		String replacement = "";
		boolean hasParams = subHelper.hasParameters();
		this.queryParamsDefined(hasParams);
		
		if(hasParams) {

			String queryLower = queryString.toLowerCase();
			int startIndex 					  = queryLower.indexOf(CFWQueryCommandParamDefaults.COMMAND_NAME.toLowerCase());
			if(startIndex == -1) { startIndex = queryLower.indexOf(CFWQueryCommandParamDefaults.COMMAND_NAME_ALIAS.toLowerCase()); }
			
			//---------------------------------
			// Extract Params
			if(startIndex != -1) {
				int endIndex = queryLower.indexOf("|", startIndex);
				if(endIndex == -1) { endIndex = queryString.length();}
				String paramsCommand = queryString.substring(startIndex, endIndex);
				
				ArrayList<CFWQueryToken> tokens = new CFWQueryTokenizer(paramsCommand, false, true)
						.keywords("AND", "OR", "NOT")
						.getAllTokens();
				
				//----------------------------------
				// Skip if only command name and
				// no actual parameters
				if(tokens.size() <= 1) {
					this.queryParamsDefined(false);
					this.queryParams("");
					return;
				}
				
				//----------------------------------
				// Create object()-definition
				// i = 1 to skip command name
				StringBuilder builder = new StringBuilder("object(\n\t\t  ");
				
				for(int i = 1; i < tokens.size()-1 ; ) {
					CFWQueryToken paramName = tokens.get(i);
					i++; // go to "=" token
					int startPos = tokens.get(i).position() + 1;
					i++; // skip "=" token
					while( i < tokens.size()
						&& tokens.get(i).type() != CFWQueryTokenType.OPERATOR_EQUAL
						){
						i++;
					}
					
					int endPos = paramsCommand.length();
					if(i < tokens.size()) {
						i--; // go back to paramName
						endPos = tokens.get(i).position();

					}
					
					
					
					String paramValue = paramsCommand.substring(startPos, endPos);

					builder.append("\"" + paramName + "\"")
						   .append(", ")
						   .append(paramValue.strip())
						   .append("\n\t\t, ")
						   ;
				}
				
				builder.delete(builder.length() - 5, builder.length());
				builder.append("\n\t)");
				
				this.queryParams(builder.toString());
				
			}
		}
		
	}
	/******************************************************************
	 *
	 ******************************************************************/
	public JsonObject createJsonObject() {
		
		JsonObject result = new JsonObject();
		
		result.addProperty("name", name.getValue());
		result.addProperty("data", query.getValue());
		result.addProperty("checkPermissions", checkPermissions.getValue());
		
		return result;
	}
	
	

	public Integer id() {
		return id.getValue();
	}
	
	public CFWStoredQuery id(Integer id) {
		this.id.setValue(id);
		return this;
	}
	
	public Integer foreignKeyOwner() {
		return foreignKeyOwner.getValue();
	}
	
	public CFWStoredQuery foreignKeyOwner(Integer foreignKeyUser) {
		this.foreignKeyOwner.setValue(foreignKeyUser);
		return this;
	}
		
	public String name() {
		return name.getValue();
	}
	
	public CFWStoredQuery name(String name) {
		this.name.setValue(name);
		return this;
	}
	
	public String query() {
		return query.getValue();
	}
	
	public CFWStoredQuery query(String query) {
		this.query.setValue(query);
		return this;
	}
	
	public boolean makeWidget() {
		return makeWidget.getValue();
	}
	
	public CFWStoredQuery makeWidget(boolean value) {
		this.makeWidget.setValue(value);
		return this;
	}
	
	public String widgetCategory() {
		return widgetCategory.getValue();
	}
	
	public CFWStoredQuery widgetCategory(String value) {
		this.widgetCategory.setValue(value);
		return this;
	}
	
	public String queryParams() {
		return queryParams.getValue();
	}
	
	public CFWStoredQuery queryParams(String queryParams) {
		this.queryParams.setValue(queryParams);
		return this;
	}
	
	public boolean queryParamsDefined() {
		return queryParamsDefined.getValue();
	}
	
	public CFWStoredQuery queryParamsDefined(boolean value) {
		this.queryParamsDefined.setValue(value);
		return this;
	}
	
	public String description() {
		return description.getValue();
	}

	public CFWStoredQuery description(String description) {
		this.description.setValue(description);
		return this;
	}
	

	public ArrayList<String> tags() {
		return tags.getValue();
	}
	
	public CFWStoredQuery tags(ArrayList<String> tags) {
		this.tags.setValue(tags);
		return this;
	}
	
	
	public boolean checkPermissions() {
		return checkPermissions.getValue();
	}
	
	public CFWStoredQuery checkPermissions(boolean checkPermissions) {
		this.checkPermissions.setValue(checkPermissions);
		return this;
	}
	
	public boolean isShared() {
		return isShared.getValue();
	}
	
	public CFWStoredQuery isShared(boolean isShared) {
		this.isShared.setValue(isShared);
		return this;
	}
		
	public LinkedHashMap<String,String> sharedWithUsers() {
		if(shareWithUsers.getValue() == null) { return new LinkedHashMap<>(); }
		return shareWithUsers.getValue();
	}
	
	public CFWStoredQuery sharedWithUsers(LinkedHashMap<String,String> sharedWithUsers) {
		this.shareWithUsers.setValue(sharedWithUsers);
		return this;
	}
	
	public LinkedHashMap<String,String> sharedWithGroups() {
		if(shareWithGroups.getValue() == null) { return new LinkedHashMap<>(); }
		return shareWithGroups.getValue();
	}
	
	public CFWStoredQuery sharedWithGroups(LinkedHashMap<String,String> value) {
		this.shareWithGroups.setValue(value);
		return this;
	}
	public LinkedHashMap<String,String> editors() {
		if(editors.getValue() == null) { return new LinkedHashMap<>(); }
		return editors.getValue();
	}
	
	public CFWStoredQuery editors(LinkedHashMap<String,String> editors) {
		this.editors.setValue(editors);
		return this;
	}
	
	public LinkedHashMap<String,String> editorGroups() {
		if(editorGroups.getValue() == null) { return new LinkedHashMap<>(); }
		return editorGroups.getValue();
	}
	
	public CFWStoredQuery editorGroups(LinkedHashMap<String,String> value) {
		this.editorGroups.setValue(value);
		return this;
	}
	
	
	public Timestamp timeCreated() {
		return timeCreated.getValue();
	}
	
	public CFWStoredQuery timeCreated(Timestamp creationDate) {
		this.timeCreated.setValue(creationDate);
		return this;
	}
	
	public Timestamp lastUpdated() {
		return lastUpdated.getValue();
	}
	
	public CFWStoredQuery lastUpdated(Timestamp value) {
		this.lastUpdated.setValue(value);
		return this;
	}
	
	public boolean isArchived() {
		return isArchived.getValue();
	}
	
	public CFWStoredQuery isArchived(boolean isRenamable) {
		this.isArchived.setValue(isRenamable);
		return this;
	}	
	
}
