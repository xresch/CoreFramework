package com.xresch.cfw.extensions.databases.generic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.contextsettings.ContextSettingsChangeListener;
import com.xresch.cfw.logging.CFWLog;

public class GenericJDBCEnvironmentManagement {
	private static Logger logger = CFWLog.getLogger(GenericJDBCEnvironmentManagement.class.getName());
	
	private static boolean isInitialized = false;
	
	// Contains ContextSettings id and the associated database interface
	private static HashMap<Integer, GenericJDBCEnvironment> environmentsWithDB = new HashMap<Integer, GenericJDBCEnvironment>();
	

	private GenericJDBCEnvironmentManagement() {
		//hide public constructor
	}
	public static void initialize() {
		
		ContextSettingsChangeListener listener = 
				new ContextSettingsChangeListener(GenericJDBCEnvironment.SETTINGS_TYPE) {
			
			@Override
			public void onChange(AbstractContextSettings setting, boolean isNew) {
				GenericJDBCEnvironment env = (GenericJDBCEnvironment)setting;
				GenericJDBCEnvironmentManagement.createEnvironment(env);
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
		environmentsWithDB = new HashMap<Integer, GenericJDBCEnvironment>();
		
		ArrayList<AbstractContextSettings> settingsArray = CFW.DB.ContextSettings.getContextSettingsForType(GenericJDBCEnvironment.SETTINGS_TYPE, true);

		for(AbstractContextSettings settings : settingsArray) {
			GenericJDBCEnvironment current = (GenericJDBCEnvironment)settings;
			createEnvironment(current);
			
		}
	}
	
	private static void createEnvironment(GenericJDBCEnvironment environment) {

		Integer id = environment.getDefaultObject().id();
		environmentsWithDB.remove(id);
		
		if(environment.isDBDefined()) {
			
			environment.resetDBInstance();
			environment.getDBInstance();
			environmentsWithDB.put(environment.getDefaultObject().id(), environment);
		}
	}
	
	
	public static GenericJDBCEnvironment getEnvironment(int id) {
		if(!isInitialized) { initialize(); }
		return environmentsWithDB.get(id);
	}
	
	
}
