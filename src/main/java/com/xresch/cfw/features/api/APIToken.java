package com.xresch.cfw.features.api;

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
public class APIToken extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_APITOKEN";
	
	public enum APITokenFields{
		PK_ID,
		FK_ID_CREATOR,
		TOKEN,
		DESCRIPTION,
		IS_ACTIVE,
		JSON_RESPONSIBLE_USERS,
	}

	private static Logger logger = CFWLog.getLogger(APIToken.class.getName());
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, APITokenFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the token.")
			.apiFieldType(FormFieldType.NUMBER);
	
	private CFWField<Integer> foreignKeyCreator = CFWField.newInteger(FormFieldType.HIDDEN, APITokenFields.FK_ID_CREATOR)
			.setForeignKeyCascade(this, User.class, UserFields.PK_ID)
			.setDescription("The id of the user who created the token.")
			.apiFieldType(FormFieldType.NUMBER);
	
	private CFWField<String> token = CFWField.newString(FormFieldType.TEXT, APITokenFields.TOKEN)
			.setDescription("The token which can be used to access the API.")
			.addValidator(new LengthValidator(1, 512))
			.setValue(CFW.Random.randomStringAlphaNumerical(32));
	
	private CFWField<String> description = CFWField.newString(FormFieldType.TEXTAREA, APITokenFields.DESCRIPTION)
			.setDescription("An optional description for the token.")
			.addValidator(new LengthValidator(-1, 2000000));
	
	private CFWField<Boolean> isActive = CFWField.newBoolean(FormFieldType.BOOLEAN, APITokenFields.IS_ACTIVE)
			.setDescription("Define if the token is active. Set to false to disable the token.")
			.setValue(true);
	
	private CFWField<LinkedHashMap<String,String>> responsibleUsers = CFWField.newTagsSelector(APITokenFields.JSON_RESPONSIBLE_USERS)
			.setLabel("Responsible Users")
			.setDescription("Specify the users responsible for this token.")
			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
					return CFW.DB.Users.autocompleteUser(searchValue, this.getMaxResults());					
				}
			});
	

	
	public APIToken() {
		initializeFields();
	}
		
	public APIToken(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignKeyCreator, token, description, isActive, responsibleUsers);
	}
		
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
		
		
		String[] inputFields = 
				new String[] {
						APITokenFields.PK_ID.toString(), 
						APITokenFields.FK_ID_CREATOR.toString(),
						APITokenFields.TOKEN.toString(),
						APITokenFields.IS_ACTIVE.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						APITokenFields.PK_ID.toString(), 
						APITokenFields.FK_ID_CREATOR.toString(),
						APITokenFields.TOKEN.toString(),
						APITokenFields.DESCRIPTION.toString(),
						APITokenFields.IS_ACTIVE.toString(),
						APITokenFields.JSON_RESPONSIBLE_USERS.toString(),
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

	public Integer id() {
		return id.getValue();
	}
	
	public APIToken id(Integer id) {
		this.id.setValue(id);
		return this;
	}
	
	public Integer foreignKeyCreator() {
		return foreignKeyCreator.getValue();
	}
	
	public APIToken foreignKeyCreator(Integer foreignKeyCreator) {
		this.foreignKeyCreator.setValue(foreignKeyCreator);
		return this;
	}
		
	public String token() {
		return token.getValue();
	}
	
	public APIToken token(String name) {
		this.token.setValue(name);
		return this;
	}
	
	public String description() {
		return description.getValue();
	}

	public APIToken description(String description) {
		this.description.setValue(description);
		return this;
	}

	
	public LinkedHashMap<String,String> responsibleUsers() {
		return responsibleUsers.getValue();
	}
	
	public APIToken isActive(boolean isActive) {
		this.isActive.setValue(isActive);
		return this;
	}
	
	public boolean isActive() {
		return isActive.getValue();
	}
	
	public APIToken responsibleUsers(LinkedHashMap<String,String> responsibleUsers) {
		this.responsibleUsers.setValue(responsibleUsers);
		return this;
	}
		
}
