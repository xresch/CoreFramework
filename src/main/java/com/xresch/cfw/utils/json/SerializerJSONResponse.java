package com.xresch.cfw.utils.json;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.response.JSONResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class SerializerJSONResponse implements JsonSerializer<JSONResponse> {

	public SerializerJSONResponse() {}
	@Override
	public JsonElement serialize(JSONResponse object, Type type, JsonSerializationContext context) {

		return CFW.JSON.stringToJsonElement(object.buildResponse().toString());
	}

}
