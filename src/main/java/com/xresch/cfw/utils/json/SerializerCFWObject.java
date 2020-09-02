package com.xresch.cfw.utils.json;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWObject;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class SerializerCFWObject implements JsonSerializer<CFWObject> {

	boolean enableEncryptedValues = false;
	public SerializerCFWObject(boolean enableEncryptedValues) {
		this.enableEncryptedValues = enableEncryptedValues;
	}
	@Override
	public JsonElement serialize(CFWObject object, Type type, JsonSerializationContext context) {
		
		JsonObject result = new JsonObject();
		
		for(CFWField field : object.getFields().values()) {
			CFW.JSON.addFieldAsProperty(result, field, enableEncryptedValues);
		}
		
		JsonArray children = new JsonArray();
		for (CFWObject child : object.getChildObjects().values()) {
			children.add(CFW.JSON.objectToJsonElement(child));
		}
		
		result.add("children", children);
		
		return result;
	}

}
