package com.pengtoolbox.cfw.utils.json;

import java.lang.reflect.Type;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWJson {
	
	private static Logger logger = CFWLog.getLogger(CFWJson.class.getName());
	
	public static Gson gsonInstance;
	
	public static Gson gsonInstanceEncrypted;
	static{
		//Type cfwobjectListType = new TypeToken<LinkedHashMap<CFWObject>>() {}.getType();
		
		gsonInstance = new GsonBuilder()
				.registerTypeHierarchyAdapter(CFWObject.class, new SerializerCFWObject(false))
				.registerTypeHierarchyAdapter(ResultSet.class, new SerializerResultSet())
				.serializeNulls()
				.create();
		
		gsonInstanceEncrypted = new GsonBuilder()
				.registerTypeHierarchyAdapter(CFWObject.class, new SerializerCFWObject(true))
				.serializeNulls()
				.create();
	}
			
	
	public static Gson exposedOnlyInstance = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
			.serializeNulls().create();

	/*************************************************************************************
	 * 
	 *************************************************************************************/
	private static final String escapes[][] = new String[][]{
	        {"\\", "\\\\"},
	        {"\"", "\\\""},
	        {"\n", "\\n"},
	        {"\r", "\\r"},
	        {"\b", "\\b"},
	        {"\f", "\\f"},
	        {"\t", "\\t"}
	};
	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public static Gson getGsonInstance() {
		return gsonInstance;
	}
	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public static String toJSON(Object object) {
		return gsonInstance.toJson(object);
	}
	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public static String toJSONEncrypted(CFWObject object) {
		return gsonInstanceEncrypted.toJson(object);
	}
	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public static JsonElement toJSONElement(Object object) {
		return gsonInstance.toJsonTree(object);
	}
	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public static JsonElement toJSONElementEncrypted(CFWObject object) {
		return gsonInstanceEncrypted.toJsonTree(object);
	}
	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public static JsonObject fromJson(String jsonString) {
		JsonParser parser = new JsonParser();
		JsonObject jsonObject = (JsonObject) parser.parse(jsonString);
		
		return jsonObject;
	}
	
	/*************************************************************************************
	 * Converts a json string to a LinkedHashMap 
	 *************************************************************************************/
	public static LinkedHashMap<String,String> fromJsonLinkedHashMap(String jsonString) {

		Type type = new TypeToken<LinkedHashMap<String,String>>(){}.getType();
		LinkedHashMap<String,String> clonedMap = gsonInstance.fromJson(jsonString, type); 
		return clonedMap;
	}
	
	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public static String toJSONExposedOnly(Object object) {
		return exposedOnlyInstance.toJson(object);
	}
	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public static String escapeString(String string) {

		if(string != null) {
	        for (String[] esc : escapes) {
	            string = string.replace(esc[0], esc[1]);
	        }
		}
        return string;
    }
	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public static JsonArray arrayToJsonArray(Object[] array) {
		JsonArray jsonArray = new JsonArray();
		for(Object o : array) {
			if(o instanceof String) 			{	jsonArray.add((String)o); }
			else if(o instanceof Number) 		{	jsonArray.add((Number)o); }
			else if(o instanceof Boolean) 		{	jsonArray.add((Boolean)o); }
			else if(o instanceof Character) 	{	jsonArray.add((Character)o); }
			else if(o instanceof JsonElement) 	{	jsonArray.add((JsonElement)o); }
			else {	
				jsonArray.add(gsonInstance.toJsonTree(o)); 
			}
		}
		
		return jsonArray;
	}
	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public static JsonElement objectToJsonElement(Object o) {
		return gsonInstance.toJsonTree(o);
	}
	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public static JsonElement jsonStringToJsonElement(String jsonString) {
		if(jsonString == null || jsonString.isEmpty()) {
			jsonString = "{}";
		}
		JsonElement result  = new JsonObject();
		try {
			result = new JsonParser().parse(jsonString);
		}catch(Exception e) {
			new CFWLog(logger) 
			.method("jsonStringToJsonElement")
			.severe("Error parsing jsonString: "+jsonString, e);
		}
		return result;
	}
	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public static void addObject(JsonObject target, String propertyName, Object object) {
		if(object instanceof String) 			{	target.addProperty(propertyName, (String)object); }
		else if(object instanceof Number) 		{	target.addProperty(propertyName, (Number)object); }
		else if(object instanceof Boolean) 		{	target.addProperty(propertyName, (Boolean)object); }
		else if(object instanceof Character) 	{	target.addProperty(propertyName, (Character)object); }
		else if(object instanceof Date) 		{	target.addProperty(propertyName, ((Date)object).getTime()); }
		else if(object instanceof Clob) 		{	target.addProperty(propertyName, ((Clob)object).toString()); }
		else if(object instanceof Blob) 		{	target.addProperty(propertyName, ((Blob)object).toString()); }
		else if(object instanceof Timestamp) 	{	target.addProperty(propertyName, ((Timestamp)object).getTime()); }
		else if(object instanceof OffsetDateTime) {	target.addProperty(propertyName, ((OffsetDateTime)object).toInstant().toEpochMilli()); }
		else if(object instanceof JsonElement) 	{	target.add(propertyName, (JsonElement)object); }
		else if(object instanceof Object[]) 	{	target.add(propertyName, CFW.JSON.arrayToJsonArray((Object[])object)); }
		else {	
			target.add(propertyName, gsonInstance.toJsonTree(object)); 
		}
	}
	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public static void addFieldAsProperty(JsonObject target, CFWField field, boolean encryptValues) {
		
		String name = field.getName();
		Object value = (!encryptValues) ? field.getValue() : field.getValueEncrypted();
		
		if(name.toUpperCase().startsWith("JSON")) {
			if(value == null) {
				value = "";
			}
			if(value instanceof String) {
				JsonElement asElement = CFW.JSON.jsonStringToJsonElement(value.toString());
				target.add(name, asElement);
			}else {
				target.add(name, gsonInstance.toJsonTree(value));
			}
			
		}else {
			CFW.JSON.addObject(target, name, value);
		}
		
	}

}
