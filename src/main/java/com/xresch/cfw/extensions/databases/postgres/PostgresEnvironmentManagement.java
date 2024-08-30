package com.xresch.cfw.extensions.databases.postgres;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.contextsettings.ContextSettingsChangeListener;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public class PostgresEnvironmentManagement {
	private static Logger logger = CFWLog.getLogger(PostgresEnvironmentManagement.class.getName());
	
	private static boolean isInitialized = false;
	
	// Contains ContextSettings id and the associated database interface
	private static HashMap<Integer, PostgresEnvironment> environmentsWithDB = new HashMap<Integer, PostgresEnvironment>();
	

	private PostgresEnvironmentManagement() {
		//hide public constructor
	}
	public static void initialize() {
		
		ContextSettingsChangeListener listener = 
				new ContextSettingsChangeListener(PostgresEnvironment.SETTINGS_TYPE) {
			
			@Override
			public void onChange(AbstractContextSettings setting, boolean isNew) {
				PostgresEnvironment env = (PostgresEnvironment)setting;
				PostgresEnvironmentManagement.createEnvironment(env);
			}

			@Override
			public void onDeleteOrDeactivate(AbstractContextSettings typeSettings) {
				environmentsWithDB.remove(typeSettings.getDefaultObject().id());
			}
		};
		
		CFW.DB.ContextSettings.addChangeListener(listener);
		
		createEnvironments();
		isInitialized = true;
	}
	
	private static void createEnvironments() {
		// Clear environments
		environmentsWithDB = new HashMap<Integer, PostgresEnvironment>();
		
		ArrayList<AbstractContextSettings> settingsArray = CFW.DB.ContextSettings.getContextSettingsForType(PostgresEnvironment.SETTINGS_TYPE, true);

		for(AbstractContextSettings settings : settingsArray) {
			PostgresEnvironment current = (PostgresEnvironment)settings;
			createEnvironment(current);
			
		}
	}
	
	private static void createEnvironment(PostgresEnvironment environment) {
		
		Integer id = environment.getDefaultObject().id();
		
		environmentsWithDB.remove(id);
		
		if(environment.isDBDefined()) {
			// adding zeroDateTimeBehaviour to prevent SQLExceptions
			DBInterface db = DBInterface.createDBInterfacePostgres(
					id+"-"+environment.getDefaultObject().name(),
					environment.dbHost(), 
					environment.dbPort(), 
					environment.dbName()+"?zeroDateTimeBehavior=convertToNull", 
					environment.dbUser(), 
					environment.dbPassword()
			);
			
			environment.setDBInstance(db);
			environmentsWithDB.put(environment.getDefaultObject().id(), environment);
		}
	}
	
	
	public static PostgresEnvironment getEnvironment(int id) {
		if(!isInitialized) { initialize(); }
		return environmentsWithDB.get(id);
	}

	
}
