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
import com.xresch.cfw.utils.CFWRandom;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;
	
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourceJson extends CFWQuerySource {

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
	public String descriptionHTML() {
		return "<p>To be done</p>";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public CFWObject getParameters() {
		return new CFWObject()
				.addField(
					CFWField.newString(FormFieldType.TEXTAREA, "data")
						.setDescription("The JSON string that should be parsed. Either an array of JSON Objects or a JSON Object.")
						.addValidator(new NotNullOrEmptyValidator())
				)
			;
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public void execute(CFWObject parameters, LinkedBlockingQueue<EnhancedJsonObject> outQueue) throws Exception {
		
		String data = (String)parameters.getField("data").getValue();

		JsonElement element = CFW.JSON.fromJson(data);
		
		if(element.isJsonObject()) {
			outQueue.add( new EnhancedJsonObject(element.getAsJsonObject()) );
			return;
		}
		
		if(element.isJsonArray()) {
			for(JsonElement current : element.getAsJsonArray() ) {
				if(current.isJsonObject()) {
					outQueue.add( new EnhancedJsonObject(current.getAsJsonObject()) );
				}
				
			}
		}
		

	}

}
