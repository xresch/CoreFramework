package com.xresch.cfw.utils.json;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.xresch.cfw.datahandling.CFWChartSettings;
import com.xresch.cfw.datahandling.CFWSchedule;
import com.xresch.cfw.features.query.EnhancedJsonObject;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class SerializerEnhancedJsonObject implements JsonSerializer<EnhancedJsonObject> {

	public SerializerEnhancedJsonObject() {

	}
	
	@Override
	public JsonElement serialize(EnhancedJsonObject object, Type type, JsonSerializationContext context) {
		
		return object.getWrappedObject();
	}

}
