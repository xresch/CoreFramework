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
public class APITokenPermission extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_APITOKEN";
	
	public enum APITokenPermissionFields{
		PK_ID,
		FK_ID_TOKEN,
		TOKEN,
		DESCRIPTION,
		JSON_RESPONSIBLE_USERS,
	}

	private static Logger logger = CFWLog.getLogger(APITokenPermission.class.getName());
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, APITokenPermissionFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the permission.")
			.apiFieldType(FormFieldType.NUMBER);
	
	private CFWField<Integer> foreignKeyToken = CFWField.newInteger(FormFieldType.HIDDEN, APITokenPermissionFields.FK_ID_TOKEN)
			.setForeignKeyCascade(this, APIToken.class, APITokenPermissionFields.PK_ID)
			.setDescription("The id of the token which has this permission.")
			.apiFieldType(FormFieldType.NUMBER);
	
	private CFWField<String> token = CFWField.newString(FormFieldType.TEXT, APITokenPermissionFields.TOKEN)
			.setDescription("The token which can be used to access the API.")
			.addValidator(new LengthValidator(1, 512))
			.setValue(CFW.Random.randomStringAlphaNumerical(64));
	
	private CFWField<String> description = CFWField.newString(FormFieldType.TEXTAREA, APITokenPermissionFields.DESCRIPTION)
			.setDescription("An optional description for the token.")
			.addValidator(new LengthValidator(-1, 2000000));
	

	private CFWField<LinkedHashMap<String,String>> responsibleUsers = CFWField.newTagsSelector(APITokenPermissionFields.JSON_RESPONSIBLE_USERS)
			.setLabel("Responsible Users")
			.setDescription("Specify the users responsible for this token.")
			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
					return CFW.DB.Users.autocompleteUser(searchValue, this.getMaxResults());					
				}
			});
	
	public APITokenPermission() {
		initializeFields();
	}
		
	public APITokenPermission(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, foreignKeyToken, token, description, responsibleUsers);
	}
		
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
		
		
		String[] inputFields = 
				new String[] {
						APITokenPermissionFields.PK_ID.toString(), 
						APITokenPermissionFields.FK_ID_TOKEN.toString(),
						APITokenPermissionFields.TOKEN.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						APITokenPermissionFields.PK_ID.toString(), 
						APITokenPermissionFields.FK_ID_TOKEN.toString(),
						APITokenPermissionFields.TOKEN.toString(),
						APITokenPermissionFields.DESCRIPTION.toString(),
						APITokenPermissionFields.JSON_RESPONSIBLE_USERS.toString(),
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
	
	public APITokenPermission id(Integer id) {
		this.id.setValue(id);
		return this;
	}
	
	public Integer foreignKeyOwner() {
		return foreignKeyToken.getValue();
	}
	
	public APITokenPermission foreignKeyOwner(Integer foreignKeyUser) {
		this.foreignKeyToken.setValue(foreignKeyUser);
		return this;
	}
		
	public String token() {
		return token.getValue();
	}
	
	public APITokenPermission token(String name) {
		this.token.setValue(name);
		return this;
	}
	
	public String description() {
		return description.getValue();
	}

	public APITokenPermission description(String description) {
		this.description.setValue(description);
		return this;
	}

	
	public LinkedHashMap<String,String> responsibleUsers() {
		return responsibleUsers.getValue();
	}
	
	public APITokenPermission responsibleUsers(LinkedHashMap<String,String> responsibleUsers) {
		this.responsibleUsers.setValue(responsibleUsers);
		return this;
	}
		
}
