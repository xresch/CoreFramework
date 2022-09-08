package com.xresch.cfw.utils.json;

import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.LinkedHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.CFWFieldFlag;
import com.xresch.cfw.datahandling.CFWObject;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class SerializerCFWObject implements JsonSerializer<CFWObject> {

	private boolean enableEncryptedValues = false;
	private EnumSet<CFWFieldFlag> flags;
	private boolean includeFlagged = false;
	
	/************************************************************************
	 * @param enableEncryption
	 ************************************************************************/
	public SerializerCFWObject(boolean enableEncryption) {
		this.enableEncryptedValues = enableEncryption;
	}
	
	/****************************************************************
	 * Return a JSON element containing values of the fields of this 
	 * object, flagged or not flagged with the specified flags.
	 * 
	 * @param enableEncryption if true, encrypt values that have 
	 *        encryption enabled, false otherwise
	 * @param flags the flags for the filter
	 * @param includeFlagged if true, only includes the fields 
	 *        with the specified flag, if false exclude flagged and
	 *        keep the non-flagged
	 ****************************************************************/
	public SerializerCFWObject(boolean enableEncryption, EnumSet<CFWFieldFlag> flags, boolean includeFlagged) {
		this.enableEncryptedValues = enableEncryption;
		this.flags = flags;
		this.includeFlagged = includeFlagged;
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public JsonElement serialize(CFWObject object, Type type, JsonSerializationContext context) {
		
		JsonObject result = new JsonObject();
		
		for(CFWField field : object.getFields().values()) {
			
			if(flags == null
			|| (field.hasFlag(flags) == includeFlagged) ) {
				CFW.JSON.addFieldAsProperty(result, field, enableEncryptedValues);
			}
		}
		
		LinkedHashMap<Integer, CFWObject> childrenMap = object.getChildObjects();
		if(!childrenMap.isEmpty()) {
			JsonArray children = new JsonArray();
			for (CFWObject child : object.getChildObjects().values()) {
				children.add(CFW.JSON.objectToJsonElement(child));
			}

			result.add("children", children);
		}
		return result;
	}

}
