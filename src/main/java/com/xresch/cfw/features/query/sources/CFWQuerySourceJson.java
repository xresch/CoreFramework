package com.xresch.cfw.features.query.sources;

import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.utils.CFWRandom;
import com.xresch.cfw.utils.json.JsonTimerangeChecker;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;
	
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourceJson extends CFWQuerySource {

	private static final String FIELDNAME_DATA = "data";
	private static final String FIELDNAME_TIMEFIELD = "timefield";
	private static final String FIELDNAME_TIMEFORMAT = "timeformat";



	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceJson(CFWQuery parent) {
		super(parent);
	}

	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String uniqueName() {
		return "json";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Takes a json string as an input.";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionTime() {
		return "Use the parameters timefield and timeformat to specify the time filtering.(Default: no filtering by time)";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".sources", "source_json.html");
	}
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionRequiredPermission() {
		return "None";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return true;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public CFWObject getParameters() {
		return new CFWObject()
				.addField(
					CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_DATA)
						.setDescription("The JSON string that should be parsed. Either an array of JSON Objects or a single JSON Object.")
						.addValidator(new NotNullOrEmptyValidator())
				)
				.addField(
						CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_TIMEFIELD)
							.setDescription("The field of the json object that contains the time")	
					)
				
				.addField(
						CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_TIMEFORMAT)
							.setDescription("The format of the time in the time field. (Default: 'epoch').")	
							.setValue("epoch")
					)
			;
	}
	

	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public void execute(CFWObject parameters, LinkedBlockingQueue<EnhancedJsonObject> outQueue, long earliestMillis, long latestMillis, int limit) throws Exception {
		
		String data = (String)parameters.getField(FIELDNAME_DATA).getValue();
		String timefield = (String)parameters.getField(FIELDNAME_TIMEFIELD).getValue();
		String timeformat = (String)parameters.getField(FIELDNAME_TIMEFORMAT).getValue();
		
		
		JsonTimerangeChecker timerangeChecker = 
				new JsonTimerangeChecker(timefield, timeformat, earliestMillis, latestMillis)
					.epochAsNewField("_epoch");
		
		JsonElement element = CFW.JSON.fromJson(data);
		
		if(element.isJsonObject()) {
			
			if(timefield != null && !timerangeChecker.isInTimerange(element.getAsJsonObject(), false)) {
				return; 
			}
			
			outQueue.add( new EnhancedJsonObject(element.getAsJsonObject()) );
			return;
		}
		
		if(element.isJsonArray()) {
			for(JsonElement current : element.getAsJsonArray() ) {
				if(current.isJsonObject()) {
					
					if(timefield != null && !timerangeChecker.isInTimerange(current.getAsJsonObject(), false)) {
						continue; 
					}
					outQueue.add( new EnhancedJsonObject(current.getAsJsonObject()) );
				}
				
			}
		}
	
	}

}
