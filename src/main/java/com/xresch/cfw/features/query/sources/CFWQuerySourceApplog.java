package com.xresch.cfw.features.query.sources;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.analytics.FeatureSystemAnalytics;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.utils.FileBackwardsInputReader;
	
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourceApplog extends CFWQuerySource {

	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceApplog(CFWQuery parent) {
		super(parent);
	}

	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String uniqueName() {
		return "applog";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Fetches log events from the last application log file.";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionHTML() {
		return "<p>To be done</p>";
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
		return user.hasPermission( FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS);
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public CFWObject getParameters() {
		return new CFWObject()
//				.addField(
//					CFWField.newString(FormFieldType.TEXTAREA, "data")
//						.setDescription("The JSON string that should be parsed. Either an array of JSON Objects or a JSON Object.")
//						.addValidator(new NotNullOrEmptyValidator())
//				)
			;
	}
	

	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public void execute(CFWObject parameters, LinkedBlockingQueue<EnhancedJsonObject> outQueue, int limit) throws Exception {
		
		//String data = (String)parameters.getField("data").getValue();

		try (BufferedReader reader = new BufferedReader (new InputStreamReader (new FileBackwardsInputReader("./log/applog_0_0.log"))) ){
		
			while(true) {
				
				String currentLine = reader.readLine();
				
				if(currentLine == null) {
					break;
				}
				
				JsonElement element = CFW.JSON.fromJson(currentLine);
				
				if(element.isJsonObject()) {
					outQueue.add( new EnhancedJsonObject(element.getAsJsonObject()) );
				}
				
			}
		
		}
	
	}

}
