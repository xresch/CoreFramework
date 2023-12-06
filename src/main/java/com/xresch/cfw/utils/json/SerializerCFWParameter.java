package com.xresch.cfw.utils.json;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.xresch.cfw.features.parameter.CFWParameter;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class SerializerCFWParameter implements JsonSerializer<CFWParameter> {

	/****************************************************************
	 * 
	 ****************************************************************/
	public SerializerCFWParameter() {

	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public JsonElement serialize(CFWParameter object, Type type, JsonSerializationContext context) {
		
		return object.toJson();
	}

}
