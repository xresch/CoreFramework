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
import com.xresch.cfw.features.api.APIDefinitionCreate;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.validation.LengthValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class Role extends CFWObject {
	
	public static final String TABLE_NAME = "CFW_ROLE";
	
	public enum RoleFields{
		PK_ID,
		CATEGORY,
		NAME,
		DESCRIPTION,
		IS_DELETABLE,
		IS_RENAMABLE,
		IS_GROUP,
	}

	private static Logger logger = CFWLog.getLogger(Role.class.getName());
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, RoleFields.PK_ID.toString())
			.setPrimaryKeyAutoIncrement(this)
			.setDescription("The id of the role.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	private CFWField<String> category = CFWField.newString(FormFieldType.NONE, RoleFields.CATEGORY.toString())
			.setColumnDefinition("VARCHAR(32)")
			.setDescription("The category of the role, either 'user' or 'space'.")
			.apiFieldType(FormFieldType.SELECT)
			.setOptions(new String[] {FeatureUserManagement.CATEGORY_USER})
			//.setOptions(new String[] {FeatureUserManagement.CATEGORY_USER, "space"})
			.addValidator(new LengthValidator(-1, 32));
	
	private CFWField<String> name = CFWField.newString(FormFieldType.TEXT, RoleFields.NAME.toString())
			.setColumnDefinition("VARCHAR(255) UNIQUE")
			.setDescription("The name of the role.")
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
	
	private CFWField<String> description = CFWField.newString(FormFieldType.TEXTAREA, RoleFields.DESCRIPTION)
			.setDescription("The description of the role.")
			.addValidator(new LengthValidator(-1, 2000000));
	
	private CFWField<Boolean> isDeletable = CFWField.newBoolean(FormFieldType.NONE, RoleFields.IS_DELETABLE)
			.setDescription("Flag to define if the role can be deleted or not.")
			.setColumnDefinition("BOOLEAN")
			.apiFieldType(FormFieldType.BOOLEAN)
			.setValue(true);
	
	private CFWField<Boolean> isRenamable = CFWField.newBoolean(FormFieldType.NONE, RoleFields.IS_RENAMABLE)
			.setColumnDefinition("BOOLEAN DEFAULT TRUE")
			.setDescription("Flag to define if the role can be renamed or not.")
			.apiFieldType(FormFieldType.BOOLEAN)
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
	
	private CFWField<Boolean> isGroup = CFWField.newBoolean(FormFieldType.NONE, RoleFields.IS_GROUP)
			.setDescription("Flag to define if the role is a Group.")
			.setColumnDefinition("BOOLEAN")
			.apiFieldType(FormFieldType.BOOLEAN)
			.setValue(false);
	
	public Role() {
		initializeFields();
	}
	
	public Role(String name, String category) {
		initializeFields();
		this.name.setValue(name);
		this.category.setValue(category);
	}
	
	public Role(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);	
	}
	
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, category, name, description, isDeletable, isRenamable, isGroup);
	}
	
	/**************************************************************************************
	 * Migrate Table
	 **************************************************************************************/
	@Override
	public void migrateTable() {
		
		//---------------------------
		// Rename Table
		new CFWSQL(null).renameTable("CFW_GROUP", this.getTableName());
		
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public void updateTable() {
								
		//---------------------------
		// Add defaults to new column category
		ArrayList<CFWObject> roleArray = 
				this.select()
					.where(RoleFields.CATEGORY.toString(), null)
					.getAsObjectList();

		for(CFWObject roleObject : roleArray) {
			Role role = (Role) roleObject;
			role.category(FeatureUserManagement.CATEGORY_USER)
				.update();
			
		}

		//---------------------------
		// Change Description Data Type
		new CFWSQL(this)
			.custom("ALTER TABLE IF EXISTS CFW_ROLE ALTER COLUMN IF EXISTS DESCRIPTION SET DATA TYPE VARCHAR;")
			.execute();
		
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public void initDB() {
		//-----------------------------------------
		// Create Role Superuser
		//-----------------------------------------
		if(!CFW.DB.Roles.checkExistsByName(CFWDBRole.CFW_ROLE_SUPERUSER)) {
			CFW.DB.Roles.create(new Role(CFWDBRole.CFW_ROLE_SUPERUSER, FeatureUserManagement.CATEGORY_USER)
				.description("Superusers have all the privileges in the system. They are above administrators. ")
				.isDeletable(false)
				.isRenamable(false)
			);
		}
		
		Role superuserRole = CFW.DB.Roles.selectFirstByName(CFWDBRole.CFW_ROLE_SUPERUSER);
		
		if(superuserRole != null) {
			superuserRole.isRenamable(false);
			CFW.DB.Roles.update(superuserRole);
		}else {
			new CFWLog(logger)
			.severe("User role '"+CFWDBRole.CFW_ROLE_SUPERUSER+"' was not found in the database.");
		}
		
		//-----------------------------------------
		// Create Role Admin
		//-----------------------------------------
		if(!CFW.DB.Roles.checkExistsByName(CFWDBRole.CFW_ROLE_ADMIN)) {
			CFW.DB.Roles.create(new Role(CFWDBRole.CFW_ROLE_ADMIN, FeatureUserManagement.CATEGORY_USER)
				.description("Administrators have the privileges to manage the application.")
				.isDeletable(false)
				.isRenamable(false)
			);
		}
		
		Role adminRole = CFW.DB.Roles.selectFirstByName(CFWDBRole.CFW_ROLE_ADMIN);
		
		if(adminRole != null) {
			adminRole.isRenamable(false);
			CFW.DB.Roles.update(adminRole);
		}else {
			new CFWLog(logger)
			.severe("User role '"+CFWDBRole.CFW_ROLE_ADMIN+"' was not found in the database.");
		}
		
		
		//-----------------------------------------
		// Create Role User
		//-----------------------------------------
		if(!CFW.DB.Roles.checkExistsByName(CFWDBRole.CFW_ROLE_USER)) {
			CFW.DB.Roles.create(new Role(CFWDBRole.CFW_ROLE_USER, FeatureUserManagement.CATEGORY_USER)
				.description("Default User role. New users will automatically be added to this role if they are not managed by a foreign source.")
				.isDeletable(false)
				.isRenamable(false)
			);
		}
		
		Role userRole = CFW.DB.Roles.selectFirstByName(CFWDBRole.CFW_ROLE_USER);
		
		if(userRole != null) {
			userRole.isRenamable(false);
			CFW.DB.Roles.update(userRole);
		}else {
			new CFWLog(logger)
			.severe("User role '"+CFWDBRole.CFW_ROLE_USER+"' was not found in the database.");
		}
		
		
		
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
		
				
		String[] outputFields = 
				new String[] {
						RoleFields.PK_ID.toString(), 
						RoleFields.CATEGORY.toString(),
						RoleFields.NAME.toString(),
						RoleFields.DESCRIPTION.toString(),
						RoleFields.IS_DELETABLE.toString(),
						RoleFields.IS_RENAMABLE.toString(),		
						RoleFields.IS_GROUP.toString(),		
				};

		//----------------------------------
		// Fetch
		
		String[] inputFieldsFetch = 
				new String[] {
						RoleFields.PK_ID.toString(), 
						RoleFields.CATEGORY.toString(),
						RoleFields.NAME.toString(),
						RoleFields.IS_DELETABLE.toString(),
						RoleFields.IS_RENAMABLE.toString(),	
						RoleFields.IS_GROUP.toString(),	
				};
		
		APIDefinitionFetch fetchDataAPI = 
				new APIDefinitionFetch(
						this.getClass(),
						this.getClass().getSimpleName(),
						"fetchData",
						inputFieldsFetch,
						outputFields
				);
		
		apis.add(fetchDataAPI);
		
		//----------------------------------
		// Create
		
		String[] inputFieldsCreate = 
				new String[] {
						RoleFields.CATEGORY.toString(),
						RoleFields.NAME.toString(),
						RoleFields.DESCRIPTION.toString(),
						RoleFields.IS_GROUP.toString(),	
				};
		APIDefinitionCreate createAPI = 
				new APIDefinitionCreate(
						this.getClass(),
						this.getClass().getSimpleName(),
						"create",
						inputFieldsCreate,
						outputFields
				);
		
		apis.add(createAPI);

		
		return apis;
	}

	public Integer id() {
		return id.getValue();
	}
	
	public Role id(Integer id) {
		this.id.setValue(id);
		return this;
	}
	
	public String category() {
		return category.getValue();
	}
	
	public Role category(String category) {
		this.category.setValue(category);
		return this;
	}
	
	public String name() {
		return name.getValue();
	}
	
	public Role name(String name) {
		this.name.setValue(name);
		return this;
	}
	
	public String description() {
		return description.getValue();
	}

	public Role description(String description) {
		this.description.setValue(description);
		return this;
	}

	public boolean isDeletable() {
		return isDeletable.getValue();
	}
	
	public Role isDeletable(boolean isDeletable) {
		this.isDeletable.setValue(isDeletable);
		return this;
	}	
	
	public boolean isRenamable() {
		return isRenamable.getValue();
	}
	
	public Role isRenamable(boolean isRenamable) {
		this.isRenamable.setValue(isRenamable);
		return this;
	}	
	
	public boolean isGroup() {
		return isGroup.getValue();
	}
	
	public Role isGroup(boolean value) {
		this.isGroup.setValue(value);
		return this;
	}	
	
}
