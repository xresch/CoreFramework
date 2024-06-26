package com.xresch.cfw.features.usermgmt;

import java.net.HttpURLConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.StringJoiner;
import java.util.logging.Logger;

import com.google.common.base.Joiner;
import com.google.common.base.Joiner.MapJoiner;
import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWFieldChangeHandler;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.api.APIDefinitionSQL;
import com.xresch.cfw.features.api.APIExecutorSQL;
import com.xresch.cfw.features.usermgmt.User.UserFields;
import com.xresch.cfw.features.usermgmt.UserRoleMap.UserRoleMapFields;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.validation.EmailValidator;
import com.xresch.cfw.validation.LengthValidator;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class User extends CFWObject {
	
	private static final Logger logger = CFWLog.getLogger(User.class.getName());
	
	public static final String TABLE_NAME = "CFW_USER";
	
	// Used to avoid saving when users are acting under a different userid(e.g. API Tokens).
	private boolean isSaveable = true;
	
	public enum UserFields{
		PK_ID, 
		USERNAME,
		EMAIL, 
		FIRSTNAME, 
		LASTNAME, 
		PASSWORD_HASH,
		PASSWORD_SALT,
		DATE_CREATED, 
		LAST_LOGIN,
		STATUS, 
		IS_DELETABLE, 
		IS_RENAMABLE, 
		IS_FOREIGN
	}
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, UserFields.PK_ID)
								   .setPrimaryKeyAutoIncrement(this)
								   .setDescription("The user id.")
								   .apiFieldType(FormFieldType.NUMBER)
								   .setValue(null);
	
	private CFWField<String> username = CFWField.newString(FormFieldType.TEXT, UserFields.USERNAME)
			.setColumnDefinition("VARCHAR(255) UNIQUE")
			.setDescription("The username of the user account.")
			.addValidator(new LengthValidator(1, 255))
			.setChangeHandler(new CFWFieldChangeHandler<String>() {
				public boolean handle(String oldValue, String newValue) {
					if(username.isDisabled()) { 
						new CFWLog(logger)
						.severe("The username cannot be changed as the field is disabled.");
						return false; 
					}
					
					if( oldValue != null && !oldValue.equals(newValue)) {
						hasUsernameChanged = true;
					}
					return true;
				}
			});
	
	private CFWField<String> email = CFWField.newString(FormFieldType.EMAIL, UserFields.EMAIL)
			.setColumnDefinition("VARCHAR(255) UNIQUE")
			.setDescription("The user email address.")
			.addValidator(new LengthValidator(-1, 255))
			.addValidator(new EmailValidator());

	private CFWField<String> firstname = CFWField.newString(FormFieldType.TEXT, UserFields.FIRSTNAME)
			.setColumnDefinition("VARCHAR(255)")
			.setDescription("The firstname of the user.")
			.addValidator(new LengthValidator(-1, 255));
	
	private CFWField<String> lastname = CFWField.newString(FormFieldType.TEXT, UserFields.LASTNAME)
			.setColumnDefinition("VARCHAR(255)")
			.setDescription("The lastname of the user.")
			.addValidator(new LengthValidator(-1, 255));
	
	private CFWField<String> passwordHash = CFWField.newString(FormFieldType.NONE, UserFields.PASSWORD_HASH)
			.setColumnDefinition("VARCHAR(127)")
			.disableSanitization()
			.addValidator(new LengthValidator(-1, 255));
	
	private CFWField<String> passwordSalt = CFWField.newString(FormFieldType.NONE, UserFields.PASSWORD_SALT)
			.setColumnDefinition("VARCHAR(31)")
			.disableSanitization()
			.addValidator(new LengthValidator(-1, 255));
	
	private CFWField<String> status = CFWField.newString(FormFieldType.SELECT, UserFields.STATUS)
			.setColumnDefinition("VARCHAR(15)")
			.setOptions(new String[] {"Active", "Inactive"})
			.setDescription("The status of the user, either 'Active' or 'Inactive', Active users can login, inactive users are prohibited to login.")
			.addValidator(new LengthValidator(-1, 15))
			.setValue("Active");
				
	private CFWField<Timestamp> dateCreated = CFWField.newTimestamp(FormFieldType.NONE, UserFields.DATE_CREATED)
			.setDescription("The date and time the user account was created.")
			.setValue(new Timestamp(new Date().getTime()));
	
	private CFWField<Timestamp> lastLogin = CFWField.newTimestamp(FormFieldType.NONE, UserFields.LAST_LOGIN)
			.setDescription("The date and time the user has last logged in.");
	
	private CFWField<Boolean> isDeletable = CFWField.newBoolean(FormFieldType.NONE, UserFields.IS_DELETABLE)
			.setValue(true)
			.setDescription("Flag to define if the user can be deleted or not.")
			.setChangeHandler(new CFWFieldChangeHandler<Boolean>() {
				@Override
				public boolean handle(Boolean oldValue, Boolean newValue) {
					if(newValue) {
						status.isDisabled(false);
					}else {
						status.isDisabled(true);
					}
					
					return true;
				}
			});;;
												

	private CFWField<Boolean> isRenamable = CFWField.newBoolean(FormFieldType.NONE, UserFields.IS_RENAMABLE)
			.setValue(true)
			.setDescription("Flag to define if the user can be renamed or not.")
			.setChangeHandler(new CFWFieldChangeHandler<Boolean>() {
				
				@Override
				public boolean handle(Boolean oldValue, Boolean newValue) {
					if(newValue) {
						username.isDisabled(false);
					}else {
						username.isDisabled(true);
					}
					
					return true;
				}
			});;
	
	//Username and password is managed in another source, like LDAP or CSV
	private CFWField<Boolean> isForeign = CFWField.newBoolean(FormFieldType.BOOLEAN, UserFields.IS_FOREIGN)
					.setDescription("Foreign users are managed by other authentication providers like LDAP. Password in database is ignored when a foreign authentication provider is used.")
					.setValue(false);
	
	private boolean hasUsernameChanged = false;

	// private field, do not change the map or return it as it may contain the current users permission
	// implemented to reduce performance overhead
	private HashMap<String, Permission> permissions;
	

	public User() {
		initializeFields();
	}
	
	public User(String username) {
		initializeFields();
		this.username.setValue(username);
	}
	
	public User(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);

	}
		
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		this.addFields(id, 
				username, 
				email, 
				firstname, 
				lastname, 
				passwordHash,
				passwordSalt,
				dateCreated,
				lastLogin,
				status,
				isDeletable,
				isRenamable,
				isForeign
				);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	@Override
	public void initDBSecond() {
		
		//-----------------------------------------
		// Create anonymous user 
		//-----------------------------------------
		if(!CFW.Properties.AUTHENTICATION_ENABLED) {
			String initialPassword = CFW.Security.createPasswordSalt(32);
			if(!CFW.DB.Users.checkUsernameExists("anonymous")) {
			    CFW.DB.Users.create(
					new User("anonymous")
					.setNewPassword(initialPassword, initialPassword)
					.isDeletable(true)
					.isRenamable(false)
					.isForeign(false)
				);
			}
		
			User anonUser = CFW.DB.Users.selectByUsernameOrMail("anonymous");
			
			if(anonUser == null) {
				new CFWLog(logger)
				.severe("User 'anonymous' was not found in the database.");
			}
		}
		//-----------------------------------------
		// Create default admin user
		//-----------------------------------------
		
		if(!CFW.DB.Users.checkUsernameExists("admin")) {
			
		    CFW.DB.Users.create(
				new User("admin")
				.setNewPassword("admin", "admin")
				.isDeletable(false)
				.isRenamable(false)
				.isForeign(false)
			);
		}
		
		User adminUser = CFW.DB.Users.selectByUsernameOrMail("admin");
		
		if(adminUser == null) {
			new CFWLog(logger)
			.severe("User 'admin' was not found in the database.");
		}
		
		//-----------------------------------------
		// Add Admin to role Superuser
		//-----------------------------------------
		Role superuserRole = CFW.DB.Roles.selectFirstByName(CFWDBRole.CFW_ROLE_SUPERUSER);
		
		if(superuserRole == null) {
			new CFWLog(logger)
			.severe("Role 'Superuser' was not found in the database.");
			return;
		}
		if(!CFW.DB.UserRoleMap.checkIsUserInRole(adminUser, superuserRole)) {
			CFW.DB.UserRoleMap.addRoleToUser(adminUser, superuserRole, false);
		}
		
		//Needed for Upgrade
		if(adminUser != null && superuserRole != null) {
			CFW.DB.UserRoleMap.updateIsDeletable(adminUser.id(), superuserRole.id(), false);
		}

		if(!CFW.DB.UserRoleMap.checkIsUserInRole(adminUser, superuserRole)) {
			new CFWLog(logger)
			.severe("User 'admin' is not assigned to role 'Superuser'.");
		}
		
		//-----------------------------------------
		// Upgrade Step: Superuser permissions undeletable
		//-----------------------------------------
		HashMap<String, Permission> permissions = CFW.DB.RolePermissionMap.selectPermissionsForRole(superuserRole);

		for(Permission p : permissions.values()) {
			CFW.DB.RolePermissionMap.updateIsDeletable(p.id(), superuserRole.id(), false);
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
						UserFields.PK_ID.toString(), 
						UserFields.USERNAME.toString(),
						UserFields.EMAIL.toString(),
						UserFields.FIRSTNAME.toString(),
						UserFields.LASTNAME.toString(),
						UserFields.STATUS.toString()
				};
		
		String[] outputFields = 
				new String[] {
						UserFields.PK_ID.toString(), 
						UserFields.USERNAME.toString(),
						UserFields.EMAIL.toString(),
						UserFields.FIRSTNAME.toString(),
						UserFields.LASTNAME.toString(),
						UserFields.STATUS.toString(),
						UserFields.DATE_CREATED.toString(),
						UserFields.LAST_LOGIN.toString(),
						UserFields.IS_DELETABLE.toString(),
						UserFields.IS_RENAMABLE.toString(),
						UserFields.IS_FOREIGN.toString()
		
				};

		//----------------------------------
		// fetchJSON
		APIDefinitionFetch fetchDataAPI = 
				new APIDefinitionFetch(
						this.getClass(),
						"User",
						"fetchData",
						inputFields,
						outputFields
				);
		
		apis.add(fetchDataAPI);
		apis.add(createAPIAddRole());
		apis.add(createAPICreateUser());
		apis.add(createAPIGetUserPermissions());
		apis.add(createAPIGetUserPermissionsOverview());
		apis.add(createAPISetStatus());
		
		return apis;
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	private APIDefinitionSQL createAPIGetUserPermissionsOverview() {
		APIDefinitionSQL getUserPermissionsOverview = 
				new APIDefinitionSQL(
						this.getClass(),
						"User",
						"getPermissionOverview",
						new String[] {}
				);
		getUserPermissionsOverview.setDescription("Returns the permission for the specified userID.");
		
		APIExecutorSQL overviewExecutor = new APIExecutorSQL() {
			@Override
			public ResultSet execute(APIDefinitionSQL definition, CFWObject object) {
							
				return CFW.DB.RolePermissionMap.getPermissionOverview();
			}
		};
			
		getUserPermissionsOverview.setSQLExecutor(overviewExecutor);
		return getUserPermissionsOverview;
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	private APIDefinitionSQL createAPIGetUserPermissions() {
		APIDefinitionSQL getUserPermissionsAPI = 
				new APIDefinitionSQL(
						this.getClass(),
						"User",
						"getUserPermissions",
						new String[] {UserFields.PK_ID.toString()}
				);
		getUserPermissionsAPI.setDescription("Returns the permission for the specified userID."
				+ " The standard return format is JSON if the parameter APIFORMAT is not specified.");
		
		APIExecutorSQL executor = new APIExecutorSQL() {
			@Override
			public ResultSet execute(APIDefinitionSQL definition, CFWObject object) {
				
				return CFW.DB.RolePermissionMap.selectPermissionsForUserResultSet((User)object);
			}
		};
			
		getUserPermissionsAPI.setSQLExecutor(executor);
		return getUserPermissionsAPI;
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	private APIDefinitionSQL createAPICreateUser() {
		APIDefinitionSQL apiCreateUser = 
				new APIDefinitionSQL(
						APICreateUserObject.class,
						"User",
						"create",
						new String[] {
								  UserFields.USERNAME.toString()   
								, UserFields.FIRSTNAME.toString()   
								, UserFields.LASTNAME.toString()   
								, UserFields.EMAIL.toString()        
								, UserFields.IS_FOREIGN.toString()
								, APICreateUserObject.INITIAL_PASSWORD
						}
						);
		
		apiCreateUser.setDescription("Creates a new user. Username and password are mandatory fields."
				+ " If IS_FOREIGN is set to true, given password is ignored and replaced with a random, unretrievable password(as PW check is done on foreign system)."
				+ " The password has to match the systems password rules.");
		
		APIExecutorSQL createUserExecutor = new APIExecutorSQL() {
			@Override
			public ResultSet execute(APIDefinitionSQL definition, CFWObject object) {
				APICreateUserObject createUserObject = (APICreateUserObject)object;	
				User newUser = createUserObject.getUserWithPassword();

				boolean isSuccess = CFW.DB.Users.create(newUser);
				
				if(!isSuccess) {
					definition.setStatus(isSuccess, HttpURLConnection.HTTP_INTERNAL_ERROR );
					return null;
				}
				
				return new CFWSQL(new User())
						.selectWithout(
							  UserFields.PASSWORD_HASH.toString()
							, UserFields.PASSWORD_SALT.toString()
						)
						.where(UserFields.USERNAME.toString(), newUser.username())
						.getResultSet()
						;
			}
		};
		
		apiCreateUser.setSQLExecutor(createUserExecutor);
		return apiCreateUser;
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	private APIDefinitionSQL createAPIAddRole() {
		//----------------------------------
		// addRole
		APIDefinitionSQL apiAddRole = 
				new APIDefinitionSQL(
						UserRoleMap.class,
						"User",
						"addRole",
						new String[] {
							UserRoleMapFields.FK_ID_USER.toString()
							, UserRoleMapFields.FK_ID_ROLE.toString()
						}
				);
		
		apiAddRole.setDescription("Adds a role to a user. If successful, returns the created mapping in the database.");
		
		APIExecutorSQL addRoleExecutor = new APIExecutorSQL() {
			@Override
			public ResultSet execute(APIDefinitionSQL definition, CFWObject object) {
				UserRoleMap map = (UserRoleMap)object;	
				int userID = map.foreignKeyUser();
				int roleID = map.foreignKeyRole();
				
				boolean isSuccess = CFW.DB.UserRoleMap.addRoleToUser(userID, roleID, true);

				if(!isSuccess) {
					definition.setStatus(isSuccess, HttpURLConnection.HTTP_INTERNAL_ERROR );
				}
				
				return new CFWSQL(new UserRoleMap())
							.select()
							.where(UserRoleMapFields.FK_ID_USER.toString(), userID)
							.and(UserRoleMapFields.FK_ID_ROLE.toString(), roleID)
							.getResultSet()
							;
			}
		};
			
		apiAddRole.setSQLExecutor(addRoleExecutor);
		return apiAddRole;
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	private APIDefinitionSQL createAPISetStatus() {
		//----------------------------------
		// addRole
		APIDefinitionSQL definition = 
				new APIDefinitionSQL(
						User.class,
						"User",
						"setStatus",
						new String[] {
							  UserFields.PK_ID.toString()
							,  UserFields.USERNAME.toString()
							,  UserFields.EMAIL.toString()
							, UserFields.STATUS.toString()
						}
				);
		
		definition.setDescription("Sets the status of the specified user. Provide either PK_ID, USERNAME or EMAIL to select the user. "
				+ "If multiple are provided, the priority will be in the order as given in the last sentence.");
		
		APIExecutorSQL addRoleExecutor = new APIExecutorSQL() {
			@Override
			public ResultSet execute(APIDefinitionSQL definition, CFWObject object) {
				User inputData = (User)object;	
				Integer userID = inputData.id();
				String username = inputData.username();
				String email = inputData.email();
				String status = inputData.status();
				
				//--------------------------------------
				// Check Status
				HashMap availableOptions = inputData.getField(UserFields.STATUS.toString()).getOptions();
				if(!availableOptions.containsKey(status)) {
					CFW.Messages.addErrorMessage("The value provided for STATUS is invalid.");
					definition.setStatus(false, HttpURLConnection.HTTP_BAD_REQUEST);
					return null;
				}
				
				//--------------------------------------
				// Fetch User to Update
				User userFromDB = null;
				if(userID != null) {
					userFromDB = CFW.DB.Users.selectByID(userID);
				}else if(username != null) {
					userFromDB = CFW.DB.Users.selectByUsernameOrMail(username);
				}else if(email != null) {
					userFromDB = CFW.DB.Users.selectByUsernameOrMail(email);
				}else {
					CFW.Messages.addErrorMessage("Please specify either PK_ID, USERNAME or EMAIL to select the user .");
					definition.setStatus(false, HttpURLConnection.HTTP_BAD_REQUEST);
					return null;
				}

				//--------------------------------------
				// Check is not null
				if(userFromDB == null) {
					CFW.Messages.addErrorMessage("The user could not be found in the database.");
					definition.setStatus(false, HttpURLConnection.HTTP_BAD_REQUEST);
					return null;
				}
				
				//--------------------------------------
				// Check is not admin
				if(userFromDB.username().equalsIgnoreCase("admin")) {
					CFW.Messages.addErrorMessage("The admin user cannot be changed.");
					definition.setStatus(false, HttpURLConnection.HTTP_BAD_REQUEST);
					return null;
				}
				
				//--------------------------------------
				// Update user
				userFromDB.status(status);
				boolean isSuccess = new CFWSQL(userFromDB)
						.update(UserFields.STATUS)
						;
				
				if(isSuccess) {
					CFW.Messages.addSuccessMessage("User status updated.");
					definition.setStatus(isSuccess, HttpURLConnection.HTTP_OK);
					return null;
				}
				
				
				//--------------------------------------
				// Error Case
				definition.setStatus(isSuccess, HttpURLConnection.HTTP_INTERNAL_ERROR);
				return null;
			}
		};
			
		definition.setSQLExecutor(addRoleExecutor);
		return definition;
	}
	
	/**************************************************************************************
	 * Reset the permissions so they will be reloaded the next time.
	 **************************************************************************************/
	public void resetPermissions() {
		permissions = null;
	}
	
	/**************************************************************************************
	 * Load permissions if not already loaded.
	 **************************************************************************************/
	public void loadPermissions() {
		if(permissions == null) {
			if(this.id() == CFW.Context.Request.getUser().id()) {
				permissions = CFW.Context.Request.getUserPermissions();
			}else {
				permissions = CFW.DB.Users.selectPermissionsForUser(this);
			}
		}
	}
	
	/**************************************************************************************
	 * Check if the the user has the provided permission.
	 **************************************************************************************/
	public boolean hasPermission(String permission) {
		loadPermissions();
		return permissions.containsKey(permission);
	}
	
	public Integer id() {
		return id.getValue();
	}
	
	public User id(Integer id) {
		this.id.setValue(id);
		return this;
	}
	
	public String username() {
		return username.getValue();
	}
	
	public User username(String username) {
		this.username.setValue(username);
		return this;
	}
	
	public String email() {
		return email.getValue();
	}
	
	public User email(String email) {
		this.email.setValue(email);
		return this;
	}
	
	public String firstname() {
		return firstname.getValue();
	}
	
	public User firstname(String firstname) {
		this.firstname.setValue(firstname);
		return this;
	}
	
	public String lastname() {
		return lastname.getValue();
	}
	
	public User lastname(String lastname) {
		this.lastname.setValue(lastname);
		return this;
	}
	
	
	public String createUserLabel() {
		
		String label = this.username();
		
		if(!Strings.isNullOrEmpty(this.firstname())
		|| !Strings.isNullOrEmpty(this.lastname()) ) {
			label += "("+(
					Strings.nullToEmpty(this.firstname()) 
					+ " "
					+ Strings.nullToEmpty(this.lastname())
				).trim()+")";
		}
		
		return label;
	}

	public User setNewPassword(String password, String repeatedPassword) {
		
		if(!password.equals(repeatedPassword)) {
			new CFWLog(logger)
			.severe("The two provided passwords are not equal.");
			return null;
		}
		
		this.passwordSalt(CFW.Security.createPasswordSalt(31));
		this.passwordHash(CFW.Security.createPasswordHash(password, this.passwordSalt()) );
		
		return this;
	}
	
	public boolean changePassword(String oldPassword, String password, String repeatedPassword) {
		
		if(!passwordValidation(oldPassword)) {
			new CFWLog(logger)
			.severe("The provided old password was wrong.");
			return false;
		}
		
		if(!password.equals(repeatedPassword)) {
			new CFWLog(logger)
			.severe("The two provided passwords are not equal.");
			return false;
		}else {
			this.passwordSalt(CFW.Security.createPasswordSalt(31));
			this.passwordHash(CFW.Security.createPasswordHash(password, this.passwordSalt()) );
			
			return true;
		}
	}
	
	/**************************************************************************
	 * Validate if the correct password for the user account was provided.
	 * @param password
	 * @return true if correct password, false otherwise
	 **************************************************************************/
	public boolean passwordValidation(String providedPassword) {
		String providedPasswordHash = CFW.Security.createPasswordHash(providedPassword, this.passwordSalt());
		return (providedPasswordHash.equals(this.passwordHash()));
	}
		
	
	public String passwordHash() {
		return passwordHash.getValue();
	}

	public User passwordHash(String passwordHash) {
		this.passwordHash.setValue(passwordHash);
		return this;
	}

	public String passwordSalt() {
		return passwordSalt.getValue();
	}

	public User passwordSalt(String passwordSalt) {
		this.passwordSalt.setValue(passwordSalt);
		return this;
	}

	public Timestamp dateCreated() {
		return dateCreated.getValue();
	}
	
	public User dateCreated(Timestamp creationDate) {
		this.dateCreated.setValue(creationDate);
		return this;
	}
	
	public Timestamp lastLogin() {
		return lastLogin.getValue();
	}
	
	public User lastLogin(Timestamp value) {
		this.lastLogin.setValue(value);
		return this;
	}
	
	public boolean isDeletable() {
		return isDeletable.getValue();
	}
	
	public User isDeletable(boolean isDeletable) {
		this.isDeletable.setValue(isDeletable);
		return this;
	}
	
	public String status() {
		return status.getValue();
	}
		
	public User status(String status) {
		this.status.setValue(status);
		return this;
	}
	
	public boolean isStatusActive() {
		return this.status.getValue().equals("Active");

	}
	
	public boolean isRenamable() {
		return isRenamable.getValue();
	}
	
	public User isRenamable(boolean isRenamable) {
		this.isRenamable.setValue(isRenamable);
		return this;
	}
	
	public boolean hasUsernameChanged() {
		return hasUsernameChanged;
	}
	
	public boolean isForeign() {
		return isForeign.getValue();
	}
	
	public User isForeign(boolean isForeign) {
		this.isForeign.setValue(isForeign);
		return this;
	}
	
	/***********************************************************
	 * Not stored to DB.
	 * Used to avoid saving when users are acting under a different userid(e.g. API Tokens).
	 ***********************************************************/
	public boolean isSaveable() {
		return isSaveable;
	}
	
	/***********************************************************
	 * Not stored to DB.
	 * Used to avoid saving when users are acting under a different userid(e.g. API Tokens).
	 * Set to false to avoid saving the user.
	 ***********************************************************/
	public User isSaveable(boolean isForeign) {
		this.isSaveable = isForeign;
		return this;
	}
	
}
