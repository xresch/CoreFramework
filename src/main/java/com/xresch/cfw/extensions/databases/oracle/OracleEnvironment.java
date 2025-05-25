package com.xresch.cfw.extensions.databases.oracle;

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
public class OracleEnvironment extends AbstractContextSettings {
	
	public static final String SETTINGS_TYPE = "Oracle Environment";
	
	private DBInterface dbInstance = null;
	
	public enum OracleEnvironmentFields{
		DB_HOST,
		DB_PORT,
		DB_NAME,
		DB_TYPE,
		DB_USER,
		DB_PASSWORD,
		IS_UPDATE_ALLOWED,
		TIME_ZONE,
	}
		
	private CFWField<String> dbHost = CFWField.newString(FormFieldType.TEXT, OracleEnvironmentFields.DB_HOST)
			.setDescription("The server name of the database host.");
	
	private CFWField<Integer> dbPort = CFWField.newInteger(FormFieldType.NUMBER, OracleEnvironmentFields.DB_PORT)
			.setDescription("The port used to access the database.");
	
	private CFWField<String> dbName = CFWField.newString(FormFieldType.TEXT, OracleEnvironmentFields.DB_NAME)
			.setDescription("The name of the user for accessing the database.");
	
	private CFWField<String> dbType = CFWField.newString(FormFieldType.SELECT, OracleEnvironmentFields.DB_TYPE)
			.setDescription("The type of the oracle service.")
			.setOptions(new String[] {"Service Name", "SID"})
			.setValue("SID");
	
	private CFWField<String> dbUser = CFWField.newString(FormFieldType.TEXT, OracleEnvironmentFields.DB_USER)
			.setDescription("The name of the user for accessing the database.");
	
	private CFWField<String> dbPassword = CFWField.newString(FormFieldType.PASSWORD, OracleEnvironmentFields.DB_PASSWORD)
			.setDescription("The password of the DB user.")
			.disableSanitization()
			.enableEncryption("oracle_DB_PW_Salt");
	
	private CFWField<Boolean> isUpdateAllowed = CFWField.newBoolean(FormFieldType.BOOLEAN, OracleEnvironmentFields.IS_UPDATE_ALLOWED)
			.setDescription("Defines if this database connection allows to execute updates.")
			.setValue(false);
	
	private CFWField<String> timezone = CFWField.newString(FormFieldType.TIMEZONEPICKER, OracleEnvironmentFields.TIME_ZONE)
			.setDescription("The timezone the database is using. Needed to manage differences from GMT properly.");
	
	
	public OracleEnvironment() {
		initializeFields();
	}
		
	private void initializeFields() {
		this.addFields(dbHost, dbPort, dbName, dbType, dbUser, dbPassword, isUpdateAllowed, timezone);
	}
		
	
	@Override
	public boolean isDeletable(int id) {
		
		int count = new DashboardWidget()
			.selectCount()
			.whereLike(DashboardWidgetFields.JSON_SETTINGS, "%\"environment\":"+id+"%")
			.and().like(DashboardWidgetFields.TYPE, "emp_oracle%")
			.executeCount();
		
		if(count == 0) {
			return true;
		}else {
			CFW.Messages.addErrorMessage("The Oracle Environment cannot be deleted as it is still in use by "+count+"  widget(s).");
			return false;
		}

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
		
		if(!isDBDefined()  || !this.isActive() ) {
			return CFWStateOption.DISABLED;
		}
		
		this.getDBInstance();
		if(dbInstance == null) {
			return CFWStateOption.DISABLED;
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
			DBInterface db = DBInterface.createDBInterfaceOracle(
					"CFW_Oracle",
					this.dbHost(), 
					this.dbPort(), 
					this.dbName(), 
					this.dbType(), 
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
	
	public boolean isDBDefined() {
		if(dbHost.getValue() != null
		&& dbPort.getValue() != null
		&& dbUser.getValue() != null) {
			return true;
		}
		
		return false;
	}
	

	public String dbType() {
		return dbType.getValue();
	}
	
	public OracleEnvironment dbType(String value) {
		this.dbType.setValue(value);
		return this;
	}
			
	public String dbHost() {
		return dbHost.getValue();
	}
	
	public OracleEnvironment dbHost(String value) {
		this.dbHost.setValue(value);
		return this;
	}
		
	public int dbPort() {
		return dbPort.getValue();
	}
	
	public OracleEnvironment dbPort(int value) {
		this.dbPort.setValue(value);
		return this;
	}
	
	public String dbName() {
		return dbName.getValue();
	}
	
	public OracleEnvironment dbName(String value) {
		this.dbName.setValue(value);
		return this;
	}
	
	public String dbUser() {
		return dbUser.getValue();
	}
	
	public OracleEnvironment dbUser(String value) {
		this.dbUser.setValue(value);
		return this;
	}
	
	public String dbPassword() {
		return dbPassword.getValue();
	}
	
	public OracleEnvironment dbPassword(String value) {
		this.dbPassword.setValue(value);
		return this;
	}
	
	public Boolean isUpdateAllowed() {
		return isUpdateAllowed.getValue();
	}
	
	public OracleEnvironment isUpdateAllowed(Boolean value) {
		this.isUpdateAllowed.setValue(value);
		return this;
	}	
	
	public String timezone() {
		return timezone.getValue();
	}
	
	public OracleEnvironment timezone(String value) {
		this.timezone.setValue(value);
		return this;
	}	

	public void setDBInstance(DBInterface dbInstance) {
		this.dbInstance = dbInstance;
	}
	
}
