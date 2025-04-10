package com.xresch.cfw.extensions.databases.mysql;

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
public class MySQLEnvironment extends AbstractContextSettings {
	
	public static final String SETTINGS_TYPE = "MySQL Environment";
	
	private DBInterface dbInstance = null;
	
	public enum MySQLEnvironmentFields{
		DB_HOST,
		DB_PORT,
		DB_NAME,
		DB_USER,
		DB_PASSWORD,
		IS_UPDATE_ALLOWED,
		TIME_ZONE,
	}
		
	private CFWField<String> dbHost = CFWField.newString(FormFieldType.TEXT, MySQLEnvironmentFields.DB_HOST)
			.setDescription("The server name of the database host.");
	
	private CFWField<Integer> dbPort = CFWField.newInteger(FormFieldType.NUMBER, MySQLEnvironmentFields.DB_PORT)
			.setDescription("The port used to access the database.");
	
	private CFWField<String> dbName = CFWField.newString(FormFieldType.TEXT, MySQLEnvironmentFields.DB_NAME)
			.setDescription("The name of the user for accessing the database.");
	
	private CFWField<String> dbUser = CFWField.newString(FormFieldType.TEXT, MySQLEnvironmentFields.DB_USER)
			.setDescription("The name of the user for accessing the database.");
	
	private CFWField<String> dbPassword = CFWField.newString(FormFieldType.PASSWORD, MySQLEnvironmentFields.DB_PASSWORD)
			.setDescription("The password of the DB user.")
			.disableSanitization()
			.enableEncryption("mysql_DB_PW_Salt");
	
	private CFWField<Boolean> isUpdateAllowed = CFWField.newBoolean(FormFieldType.BOOLEAN, MySQLEnvironmentFields.IS_UPDATE_ALLOWED)
			.setDescription("Defines if this database connection allows to execute updates.")
			.setValue(false);
	
	private CFWField<String> timezone = CFWField.newString(FormFieldType.TIMEZONEPICKER, MySQLEnvironmentFields.TIME_ZONE)
			.setDescription("The timezone the database is using. Needed to manage differences from GMT properly.");
	
	public MySQLEnvironment() {
		initializeFields();
	}
		
	private void initializeFields() {
		this.addFields(dbHost, dbPort, dbName, dbUser, dbPassword, isUpdateAllowed, timezone);
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
	 * Creates the DB instance if not not already exists.
	 * 
	 **************************************************************/
	public DBInterface getDBInstance() {
		
		//----------------------------------
		// Create Instance
		if(dbInstance == null) {
			int id = this.getDefaultObject().id();
			String name = this.getDefaultObject().name();
			
			// adding zeroDateTimeBehaviour to prevent SQLExceptions
			DBInterface db = DBInterface.createDBInterfaceMySQL(
					id+"-"+name+":MySQL",
					this.dbHost(), 
					this.dbPort(), 
					this.dbName()+"?zeroDateTimeBehavior=convertToNull", 
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
	
	/**************************************************************
	 * 
	 **************************************************************/
	@Override
	public boolean isDeletable(int id) {
		
		int count = new DashboardWidget()
			.selectCount()
			.whereLike(DashboardWidgetFields.JSON_SETTINGS, "%\"environment\":"+id+"%")
			.and().like(DashboardWidgetFields.TYPE, "emp_mysql%")
			.executeCount();
		
		if(count == 0) {
			return true;
		}else {
			CFW.Messages.addErrorMessage("The MySQL Environment cannot be deleted as it is still in use by "+count+"  widget(s).");
			return false;
		}

	}
	
	public boolean isDBDefined() {
		if(dbHost.getValue() != null
		&& dbPort.getValue() != null
		&& dbUser.getValue() != null) {
			return true;
		}
		
		return false;
	}
			
	public String dbHost() {
		return dbHost.getValue();
	}
	
	public MySQLEnvironment dbHost(String value) {
		this.dbHost.setValue(value);
		return this;
	}
		
	public int dbPort() {
		return dbPort.getValue();
	}
	
	public MySQLEnvironment dbPort(int value) {
		this.dbPort.setValue(value);
		return this;
	}
	
	public String dbName() {
		return dbName.getValue();
	}
	
	public MySQLEnvironment dbName(String value) {
		this.dbName.setValue(value);
		return this;
	}
	
	public String dbUser() {
		return dbUser.getValue();
	}
	
	public MySQLEnvironment dbUser(String value) {
		this.dbUser.setValue(value);
		return this;
	}
	
	public String dbPassword() {
		return dbPassword.getValue();
	}
	
	public MySQLEnvironment dbPassword(String value) {
		this.dbPassword.setValue(value);
		return this;
	}	
	
	public Boolean isUpdateAllowed() {
		return isUpdateAllowed.getValue();
	}
	
	public MySQLEnvironment isUpdateAllowed(Boolean value) {
		this.isUpdateAllowed.setValue(value);
		return this;
	}	
	
	public String timezone() {
		return timezone.getValue();
	}
	
	public MySQLEnvironment timezone(String value) {
		this.timezone.setValue(value);
		return this;
	}

	public void setDBInstance(DBInterface dbInstance) {
		this.dbInstance = dbInstance;
	}
	
}
