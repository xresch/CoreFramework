package com.xresch.cfw.extensions.databases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.extensions.databases.generic.GenericJDBCEnvironment;
import com.xresch.cfw.extensions.databases.mssql.MSSQLEnvironment;
import com.xresch.cfw.extensions.databases.mysql.MySQLEnvironment;
import com.xresch.cfw.extensions.databases.oracle.OracleEnvironment;
import com.xresch.cfw.extensions.databases.postgres.PostgresEnvironment;
import com.xresch.cfw.features.analytics.CFWStatusMonitor;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.utils.CFWState.CFWStateOption;

public class CFWStatusMonitorDatabase implements CFWStatusMonitor {

	/*************************************************************
	 * 
	 *************************************************************/
	@Override
	public String category() {
		return "Databases";
	}

	/*************************************************************
	 * 
	 *************************************************************/
	@Override
	public String uniqueName() {
		return "Database Context Settings Monitor";
	}

	/*************************************************************
	 * 
	 *************************************************************/
	@Override
	public HashMap<JsonObject, CFWStateOption> getStatuses() {
		
		
		LinkedHashMap<JsonObject, CFWStateOption> result = new LinkedHashMap<>();
		
		//----------------------------------
		// Generic JDBC
		ArrayList<AbstractContextSettings> jdbcList = CFW.DB.ContextSettings.getContextSettingsForType(GenericJDBCEnvironment.SETTINGS_TYPE, true);
		for(AbstractContextSettings  setting : jdbcList) {
			
			GenericJDBCEnvironment jdbc = (GenericJDBCEnvironment)setting;
			
			CFWStateOption state = jdbc.getStatus();
			
			JsonObject object = new JsonObject();
			object.addProperty("name", jdbc.getDefaultObject().name()); 
			object.addProperty("type", jdbc.getDefaultObject().type()); 
			object.addProperty("driver", jdbc.dbDriver()); 
			
			result.put(object, state);
		}
		
		//----------------------------------
		// PostGres
		ArrayList<AbstractContextSettings> postgresList = CFW.DB.ContextSettings.getContextSettingsForType(PostgresEnvironment.SETTINGS_TYPE, true);
		for(AbstractContextSettings  setting : postgresList) {
			
			PostgresEnvironment jdbc = (PostgresEnvironment)setting;
			
			CFWStateOption state = jdbc.getStatus();
			
			JsonObject object = new JsonObject();
			object.addProperty("name", jdbc.getDefaultObject().name()); 
			object.addProperty("type", jdbc.getDefaultObject().type()); 
			
			result.put(object, state);
		}
		
		//----------------------------------
		// MSSQL
		ArrayList<AbstractContextSettings> mssqlList = CFW.DB.ContextSettings.getContextSettingsForType(MSSQLEnvironment.SETTINGS_TYPE, true);
		for(AbstractContextSettings  setting : mssqlList) {
			
			MSSQLEnvironment jdbc = (MSSQLEnvironment)setting;
			
			CFWStateOption state = jdbc.getStatus();
			
			JsonObject object = new JsonObject();
			object.addProperty("name", jdbc.getDefaultObject().name()); 
			object.addProperty("type", jdbc.getDefaultObject().type()); 
			
			result.put(object, state);
		}
		
		//----------------------------------
		// MySQL
		ArrayList<AbstractContextSettings> mysqlList = CFW.DB.ContextSettings.getContextSettingsForType(MySQLEnvironment.SETTINGS_TYPE, true);
		for(AbstractContextSettings  setting : mysqlList) {
			
			MySQLEnvironment jdbc = (MySQLEnvironment)setting;
			
			CFWStateOption state = jdbc.getStatus();
			
			JsonObject object = new JsonObject();
			object.addProperty("name", jdbc.getDefaultObject().name()); 
			object.addProperty("type", jdbc.getDefaultObject().type()); 
			
			result.put(object, state);
		}
		
		//----------------------------------
		// Oracle
		ArrayList<AbstractContextSettings> oracleList = CFW.DB.ContextSettings.getContextSettingsForType(OracleEnvironment.SETTINGS_TYPE, true);
		for(AbstractContextSettings  setting : oracleList) {
			
			OracleEnvironment jdbc = (OracleEnvironment)setting;
			
			CFWStateOption state = jdbc.getStatus();
			
			JsonObject object = new JsonObject();
			object.addProperty("name", jdbc.getDefaultObject().name()); 
			object.addProperty("type", jdbc.getDefaultObject().type()); 
			
			result.put(object, state);
		}
		
		return result;
	}
	

}
