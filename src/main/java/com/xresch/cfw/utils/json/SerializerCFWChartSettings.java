package com.xresch.cfw.utils.json;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.xresch.cfw.datahandling.CFWChartSettings;
import com.xresch.cfw.datahandling.CFWSchedule;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class SerializerCFWChartSettings implements JsonSerializer<CFWChartSettings> {

	public SerializerCFWChartSettings() {

	}
	
	@Override
	public JsonElement serialize(CFWChartSettings object, Type type, JsonSerializationContext context) {
		
		return object.getAsJsonObject();
	}

}
