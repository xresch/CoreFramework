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
	
	public static final String TABLE_NAME = "CFW_APITOKEN_PERMISSION";
	
	public enum APITokenPermissionFields{
		PK_ID,
		API_NAME,
		ACTION_NAME,
	}

	private static Logger logger = CFWLog.getLogger(APITokenPermission.class.getName());
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, APITokenPermissionFields.PK_ID)
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the permission.")
			.apiFieldType(FormFieldType.NUMBER);
	
	private CFWField<String> apiName = CFWField.newString(FormFieldType.TEXT, APITokenPermissionFields.API_NAME)
			.setDescription("The apiName which can be used to access the API.")
			.addValidator(new LengthValidator(1, 512))
			.setValue(CFW.Random.stringAlphaNum(64));
	
	private CFWField<String> actionName = CFWField.newString(FormFieldType.TEXT, APITokenPermissionFields.ACTION_NAME)
			.setDescription("The action name of this permission.")
			.addValidator(new LengthValidator(-1, 2000000));
	
	
	public APITokenPermission() {
		initializeFields();
	}
		
	public APITokenPermission(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, apiName, actionName);
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
						APITokenPermissionFields.API_NAME.toString(),
						APITokenPermissionFields.ACTION_NAME.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						APITokenPermissionFields.PK_ID.toString(), 
						APITokenPermissionFields.API_NAME.toString(),
						APITokenPermissionFields.ACTION_NAME.toString(),
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
			
	public String apiName() {
		return apiName.getValue();
	}
	
	public APITokenPermission apiName(String name) {
		this.apiName.setValue(name);
		return this;
	}
	
	public String actionName() {
		return actionName.getValue();
	}

	public APITokenPermission actionName(String description) {
		this.actionName.setValue(description);
		return this;
	}
	
	public String getPermissionName() {
		return apiName.getValue()+"."+actionName.getValue();
	}
		
}
