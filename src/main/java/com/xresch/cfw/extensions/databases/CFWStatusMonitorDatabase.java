package com.xresch.cfw.extensions.databases;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.extensions.databases.generic.GenericJDBCEnvironment;
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
		
		HashMap<JsonObject, CFWStateOption> result = new HashMap<>();
		
		ArrayList<AbstractContextSettings> jdbcList = CFW.DB.ContextSettings.getContextSettingsForType(GenericJDBCEnvironment.SETTINGS_TYPE, true);
		for(AbstractContextSettings  setting : jdbcList) {
			
			GenericJDBCEnvironment jdbc = (GenericJDBCEnvironment)setting;
			
			CFWStateOption state = jdbc.getStatus();
			
			JsonObject object = new JsonObject();
			object.addProperty("name", jdbc.getDefaultObject().name()); 
			object.addProperty("type", jdbc.getDefaultObject().type()); 
			
			result.put(object, state);
		}
		
		return result;
	}
	

}
