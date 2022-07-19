package com.xresch.cfw.features.query.sources;

import java.text.ParseException;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.analytics.FeatureSystemAnalytics;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWAuditLogDBMethods;
import com.xresch.cfw.utils.ResultSetUtils.ResultSetAsJsonReader;
import com.xresch.cfw.utils.json.JsonTimerangeChecker;
	
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourceAuditlog extends CFWQuerySource {


	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceAuditlog(CFWQuery parent) {
		super(parent);
	}

	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String uniqueName() {
		return "auditlog";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Reads records from the audit logs in the database.";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionTime() {
		return "No special handling required.";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".sources", "source_auditlog.html");
	}
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionRequiredPermission() {
		return FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS;
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS);
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		// do nothing
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public CFWObject getParameters() {
		return new CFWObject();
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public void parametersPermissionCheck(CFWObject parameters) throws ParseException {
		//do nothing
	}
	
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public void execute(CFWObject parameters, LinkedBlockingQueue<EnhancedJsonObject> outQueue, long earliestMillis, long latestMillis, int limit) throws Exception {
				
//		JsonTimerangeChecker timerangeChecker = 
//				new JsonTimerangeChecker("TIMESTAMP", "epoch", earliestMillis, latestMillis)
//					.epochAsNewField("_epoch");
		
		ResultSetAsJsonReader reader = CFWAuditLogDBMethods.selectByTimerange(earliestMillis, latestMillis);
		
		JsonObject currentObject;
		while((currentObject = reader.next()) != null) {
			outQueue.add( new EnhancedJsonObject(currentObject) );
		}
			
	}

}
