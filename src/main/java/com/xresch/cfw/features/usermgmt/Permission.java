package com.xresch.cfw.features.usermgmt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWFieldChangeHandler;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.validation.LengthValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class Permission extends CFWObject{
	
	public static final String TABLE_NAME = "CFW_PERMISSION";
	
	public enum PermissionFields{
		PK_ID, 
		CATEGORY,
		NAME,
		DESCRIPTION
	}
	
	private static Logger logger = CFWLog.getLogger(Permission.class.getName());
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, PermissionFields.PK_ID.toString())
									.setPrimaryKeyAutoIncrement(this)
									.setDescription("The id of the permission.")
									.apiFieldType(FormFieldType.NUMBER)
									.setValue(null);
	
	private CFWField<String> category = CFWField.newString(FormFieldType.NONE, PermissionFields.CATEGORY.toString())
									.setColumnDefinition("VARCHAR(32)")
									.setDescription("The catogery of the permission, either 'user' or 'space'.")
									.apiFieldType(FormFieldType.SELECT)
									.setOptions(new String[] {FeatureUserManagement.CATEGORY_USER, "space"})
									.addValidator(new LengthValidator(-1, 32));
	
	private CFWField<String> name = CFWField.newString(FormFieldType.TEXT, PermissionFields.NAME.toString())
									.setColumnDefinition("VARCHAR(255) UNIQUE")
									.setDescription("The name of the permission.")
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
	
	private CFWField<String> description = CFWField.newString(FormFieldType.TEXTAREA, PermissionFields.DESCRIPTION.toString())
											.setDescription("The description of the permission.")
											.addValidator(new LengthValidator(-1, 2000000));
	

	public Permission() {
		initializeFields();
	}
	
	public Permission(String name, String category) {
		initializeFields();
		this.name.setValue(name);
		this.category.setValue(category);
	}
	
	public Permission(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, category, name, description);
	}
	
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public void updateTable() {
				
		//---------------------------
		// Add defaults to new column category
		ArrayList<CFWObject> permissionArray = 
				this.select()
					.where(PermissionFields.CATEGORY.toString(), null)
					.getAsObjectList();

		for(CFWObject permissionObject : permissionArray) {
			Permission permission = (Permission) permissionObject;
			permission.category(FeatureUserManagement.CATEGORY_USER)
				.update();
			
		}
		
		//---------------------------
		// Change Description Data Type
		new CFWSQL(this)
			.custom("ALTER TABLE IF EXISTS CFW_PERMISSION ALTER COLUMN IF EXISTS DESCRIPTION SET DATA TYPE VARCHAR;")
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
						PermissionFields.PK_ID.toString(), 
						PermissionFields.NAME.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						PermissionFields.PK_ID.toString(), 
						PermissionFields.CATEGORY.toString(),
						PermissionFields.NAME.toString(),
						PermissionFields.DESCRIPTION.toString(),
				};

		//----------------------------------
		// fetchData
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
	
	public Permission id(Integer id) {
		this.id.setValue(id);
		return this;
	}
	
	public String name() {
		return name.getValue();
	}
	
	public String category() {
		return category.getValue();
	}
	
	public Permission category(String category) {
		this.category.setValue(category);
		return this;
	}
	
	public Permission name(String name) {
		this.name.setValue(name);
		return this;
	}
	
	public String description() {
		return description.getValue();
	}

	public Permission description(String description) {
		this.description.setValue(description);
		return this;
	}
		
}
