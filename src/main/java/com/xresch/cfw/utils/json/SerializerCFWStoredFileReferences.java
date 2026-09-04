package com.xresch.cfw.utils.json;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.xresch.cfw.datahandling.CFWStoredFileReferences;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class SerializerCFWStoredFileReferences implements JsonSerializer<CFWStoredFileReferences > {

	public SerializerCFWStoredFileReferences() {

	}
	
	@Override
	public JsonElement serialize(CFWStoredFileReferences object, Type type, JsonSerializationContext context) {
		
		return object.getAsJsonArray();
	}

}
