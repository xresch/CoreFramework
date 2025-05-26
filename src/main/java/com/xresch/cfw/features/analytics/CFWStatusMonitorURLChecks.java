package com.xresch.cfw.features.analytics;

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
import com.xresch.cfw.utils.web.CFWHttp.CFWHttpResponse;

public class CFWStatusMonitorURLChecks implements CFWStatusMonitor {

	/*************************************************************
	 * 
	 *************************************************************/
	@Override
	public String category() {
		return "URL Checks";
	}

	/*************************************************************
	 * 
	 *************************************************************/
	@Override
	public String uniqueName() {
		return "URL Monitor";
	}

	/*************************************************************
	 * 
	 *************************************************************/
	@Override
	public HashMap<JsonObject, CFWStateOption> getStatuses() {
		
		
		LinkedHashMap<JsonObject, CFWStateOption> result = new LinkedHashMap<>();
		
		//----------------------------------
		// Generic JDBC
		ArrayList<String> urlList = 
				CFW.DB.Config.getConfigAsArrayList(
						  FeatureSystemAnalytics.CATEGORY_STATUS_MONITOR
						, FeatureSystemAnalytics.CONFIG_URL_CHECKS
				);
		
		for(String  url : urlList) {
			
			CFWHttpResponse response = CFW.HTTP.sendGETRequest(url);

			CFWStateOption state = response.getState(true);
			
			JsonObject object = new JsonObject();
			object.addProperty("URL", url); 
			object.addProperty("Status Code", response.getStatus() ); 
			
			if(response.errorOccured()) {
				object.addProperty("Error Message", response.errorMessage() ); 
			}
			
			result.put(object, state);
		}
		
		return result;
	}
	

}
