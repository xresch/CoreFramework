package com.xresch.cfw.utils.json;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.xresch.cfw.datahandling.CFWSchedule;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class SerializerCFWSchedule implements JsonSerializer<CFWSchedule> {

	public SerializerCFWSchedule() {

	}
	
	@Override
	public JsonElement serialize(CFWSchedule object, Type type, JsonSerializationContext context) {
		
		return object.getAsJsonObject();
	}

}
