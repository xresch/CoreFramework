package com.xresch.cfw.utils.json;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.xresch.cfw.datahandling.CFWTimeframe;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class SerializerCFWTimeframe implements JsonSerializer<CFWTimeframe> {

	public SerializerCFWTimeframe() {

	}
	
	@Override
	public JsonElement serialize(CFWTimeframe object, Type type, JsonSerializationContext context) {
		
		return object.getAsJsonObject();
	}

}
