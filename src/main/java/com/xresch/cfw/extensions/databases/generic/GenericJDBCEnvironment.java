package com.xresch.cfw.extensions.databases.generic;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.DashboardWidget.DashboardWidgetFields;
import com.xresch.cfw.utils.CFWState.CFWStateOption;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021
 * @license MIT-License
 **************************************************************************************************************/
public class GenericJDBCEnvironment extends AbstractContextSettings {
	
	public static final String SETTINGS_TYPE = "Generic JDBC Environment";
	
	private DBInterface dbInstance = null;
	
	//createDBInterface(String uniquepoolName, String driverName, String url, String username, String password) {
	
	public enum GenericJDBCEnvironmentFields{
		DB_DRIVER,
		DB_CONNECTION_URL,
		DB_USER,
		DB_PASSWORD,
		IS_UPDATE_ALLOWED,
		TIME_ZONE,
	}
	
	private CFWField<String> dbDriver = CFWField.newString(FormFieldType.SELECT, GenericJDBCEnvironmentFields.DB_DRIVER)
			.setDescription("The JDBC driver for your database solution. Copy the driver jar-File(s) to the folder ./extensions in the application root directory. (needs restart)")
			.setOptions(DBInterface.getListofDriverClassnames())
			;
	
	private CFWField<String> dbConnectionURL = CFWField.newString(FormFieldType.TEXT, GenericJDBCEnvironmentFields.DB_CONNECTION_URL)
			.setDescription("The Connection URL for the database, including protocol.(for example: jdbc:sqlserver://servername:1433;databaseName=AdventureWorks)")
			.setValue("jbdc:{databasename}://{server}:{port};{parameters}");
	
	private CFWField<String> dbUser = CFWField.newString(FormFieldType.TEXT, GenericJDBCEnvironmentFields.DB_USER)
			.setDescription("The name of the user for accessing the database.");
	
	private CFWField<String> dbPassword = CFWField.newString(FormFieldType.PASSWORD, GenericJDBCEnvironmentFields.DB_PASSWORD)
			.setDescription("The password of the DB user.")
			.disableSanitization()
			.enableEncryption("genericjdbc_DB_PW_Salt");
	
	private CFWField<Boolean> isUpdateAllowed = CFWField.newBoolean(FormFieldType.BOOLEAN, GenericJDBCEnvironmentFields.IS_UPDATE_ALLOWED)
			.setDescription("Defines if this database connection allows to execute updates.")
			.setValue(false);
	
	private CFWField<String> timezone = CFWField.newString(FormFieldType.TIMEZONEPICKER, GenericJDBCEnvironmentFields.TIME_ZONE)
			.setDescription("The timezone the database is using. Needed to manage differences from GMT properly.");
			
	public GenericJDBCEnvironment() {
		initializeFields();
	}
		
	private void initializeFields() {
		this.addFields(dbDriver, dbConnectionURL, dbUser, dbPassword,isUpdateAllowed, timezone);
	}
		
	/**************************************************************
	 * 
	 **************************************************************/
	public boolean isMonitoringEnabled() {
		return true;
	}
	
	/**************************************************************
	 *
	 **************************************************************/
	public CFWStateOption getStatus() {
		
		if(!isDBDefined()) {
			return CFWStateOption.NONE;
		}
		
		this.getDBInstance();
		if(dbInstance == null) {
			return CFWStateOption.NONE;
		}
		
		if (this.dbInstance.checkCanConnect()) {
			return CFWStateOption.GREEN;
		}
		
		return CFWStateOption.RED;
		
	}
	
	/**************************************************************
	 * 
	 **************************************************************/
	@Override
	public boolean isDeletable(int id) {
		
		int count = new DashboardWidget()
			.selectCount()
			.whereLike(DashboardWidgetFields.JSON_SETTINGS, "%\"environment\":"+id+"%")
			.and().like(DashboardWidgetFields.TYPE, "emp_genericjdbc%")
			.executeCount();
		
		if(count == 0) {
			return true;
		}else {
			CFW.Messages.addErrorMessage("The Generic JDBC Environment cannot be deleted as it is still in use by "+count+"  widget(s).");
			return false;
		}

	}
	
	/**************************************************************
	 * 
	 **************************************************************/
	public boolean isDBDefined() {
		if(dbDriver.getValue() != null
		&& dbConnectionURL.getValue() != null
		&& dbUser.getValue() != null) {
			return true;
		}
		
		return false;
	}
	
	/**************************************************************
	 * Creates the DB instance if not not already exists.
	 * 
	 **************************************************************/
	public DBInterface getDBInstance() {
		
		//----------------------------------
		// Create Instance
		if(dbInstance == null) {
			int id = this.getDefaultObject().id();
			String name = this.getDefaultObject().name();
			
			DBInterface db = DBInterface.createDBInterface(
					id+"-"+name+":GenericJBDC",
					this.dbDriver(), 
					this.dbConnectionURL(), 
					this.dbUser(), 
					this.dbPassword()
			);
			
			dbInstance = db;
		}
		//----------------------------------
		// Create Instance
		return dbInstance;
	}
	
	/**************************************************************
	 * Resets the DB instance.
	 * 
	 **************************************************************/
	public void resetDBInstance() {
		this.dbInstance = null;
	}
		

	public String dbDriver() {
		return dbDriver.getValue();
	}
	
	public GenericJDBCEnvironment dbDriver(String value) {
		this.dbDriver.setValue(value);
		return this;
	}
		
	public String dbConnectionURL() {
		return dbConnectionURL.getValue();
	}
	
	public GenericJDBCEnvironment dbConnectionURL(String value) {
		this.dbConnectionURL.setValue(value);
		return this;
	}
		
	public String dbUser() {
		return dbUser.getValue();
	}
	
	public GenericJDBCEnvironment dbUser(String value) {
		this.dbUser.setValue(value);
		return this;
	}
	
	public String dbPassword() {
		return dbPassword.getValue();
	}
	
	public GenericJDBCEnvironment dbPassword(String value) {
		this.dbPassword.setValue(value);
		return this;
	}	
	
	public Boolean isUpdateAllowed() {
		return isUpdateAllowed.getValue();
	}
	
	public GenericJDBCEnvironment isUpdateAllowed(Boolean value) {
		this.isUpdateAllowed.setValue(value);
		return this;
	}	
	
	public String timezone() {
		return timezone.getValue();
	}
	
	public GenericJDBCEnvironment timezone(String value) {
		this.timezone.setValue(value);
		return this;
	}	
	
}
