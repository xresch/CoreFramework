package com.xresch.cfw.utils.json;

import java.lang.reflect.Type;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWSchedule;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWJson {
	
	private static final Logger logger = CFWLog.getLogger(CFWJson.class.getName());
	
	private static Gson gsonInstance;
	private static Gson gsonInstancePretty;
	
	private static Gson gsonInstanceEncrypted;
	private static Gson prettyTabWriter;
	
	static{
		//Type cfwobjectListType = new TypeToken<LinkedHashMap<CFWObject>>() {}.getType();
		
		gsonInstance = new GsonBuilder()
				.registerTypeHierarchyAdapter(ResultSet.class, new SerializerResultSet())
				.registerTypeHierarchyAdapter(CFWObject.class, new SerializerCFWObject(false))
				.registerTypeHierarchyAdapter(CFWSchedule.class, new SerializerCFWSchedule())
				.registerTypeHierarchyAdapter(CFWTimeframe.class, new SerializerCFWTimeframe())
				.registerTypeHierarchyAdapter(JSONResponse.class, new SerializerJSONResponse())
				.serializeNulls()
				.create();
		
		gsonInstancePretty = new GsonBuilder()
				.registerTypeHierarchyAdapter(ResultSet.class, new SerializerResultSet())
				.registerTypeHierarchyAdapter(CFWObject.class, new SerializerCFWObject(false))
				.registerTypeHierarchyAdapter(CFWSchedule.class, new SerializerCFWSchedule())
				.registerTypeHierarchyAdapter(CFWTimeframe.class, new SerializerCFWTimeframe())
				.registerTypeHierarchyAdapter(JSONResponse.class, new SerializerJSONResponse())
				.serializeNulls()
				.setPrettyPrinting()
				.create();
		
		gsonInstanceEncrypted = new GsonBuilder()
				.registerTypeHierarchyAdapter(CFWObject.class, new SerializerCFWObject(true))
				.serializeNulls()
				.create();
	}
			
	
	private static Gson exposedOnlyInstance = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
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
	public static String toJSONPretty(Object object) {
		
		return gsonInstancePretty.toJson(object);
	}
	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public static String toJSONPrettyDebugOnly(Object object) {
		
		return gsonInstancePretty.toJson(object).replaceAll("  ", "\t");
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
	public static JsonElement fromJson(String jsonString) {
		
		if(!Strings.isNullOrEmpty(jsonString)) {
			JsonElement jsonElement = JsonParser.parseString(jsonString);
			
			return jsonElement;
		}else {
			return JsonNull.INSTANCE;
		}
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
	public static void arraySortBy(JsonArray jsonArray, Comparator<? super JsonElement> comparator) {
		
		// Create a JsonArray to a List view instance
		final List<JsonElement> jsonElements = JsonArrayListView.of(jsonArray);
		// Sorting the jsonElements object
		Collections.sort(jsonElements, comparator);
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
	public static JsonElement stringToJsonElement(String jsonString) {
		if(jsonString == null || jsonString.isEmpty()) {
			jsonString = "{}";
		}
		JsonElement result  = new JsonObject();
		try {
			result = new JsonParser().parse(jsonString);
		}catch(Exception e) {
			new CFWLog(logger)
			.severe("Error parsing jsonString: "+jsonString, e);
		}
		return result;
	}
		
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public static JsonObject stringToJsonObject(String jsonString) {
		if(jsonString == null || jsonString.isEmpty()) {
			jsonString = "{}";
		}
		JsonObject result  = new JsonObject();
		try {
			result = gsonInstance.fromJson(jsonString, JsonObject.class);
		}catch(Exception e) {
			new CFWLog(logger)
			.severe("Error parsing jsonString: "+jsonString, e);
		}
		return result;
	}
	
	/*************************************************************************************
	 * Converts every member of the JsonObject into a record in the map
	 *************************************************************************************/
	public static LinkedHashMap<String,String> objectToMap(JsonObject object) {
		
		LinkedHashMap<String,String> map = new  LinkedHashMap<>();
		
		if(object != null) {
			for(Entry<String, JsonElement> entry : object.entrySet()) {
				
				map.put(entry.getKey(), CFW.JSON.elementValueAsString(entry.getValue()));
			}
			
		}
		return map;
	}
	
	/*************************************************************************************
	 * Gets the Element as a string without these double quotes, or null.
	 *************************************************************************************/
	public static String elementValueAsString(JsonElement element) {
		
		if(element.isJsonNull()) {
			return null;
		}
		
		if(element.isJsonPrimitive()) {
			JsonPrimitive primitive = element.getAsJsonPrimitive();
			
				 if(primitive.isBoolean()) {	return primitive.getAsBoolean()+""; }
			else if(primitive.isNumber()) {		return primitive.getAsNumber()+""; }
			else  							{	return primitive.getAsString(); }
			
		}
		
		if(element.isJsonObject()) {
			return element.getAsJsonObject().toString();
		}
		
		if(element.isJsonArray()) {
			return element.getAsJsonArray().toString();
		}
		
		return null;
	}
	
	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public static Object[] jsonToObjectArray(JsonArray jsonArray) {
		return gsonInstance.fromJson(jsonArray, Object[].class);  
	}
	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public static void addObject(JsonObject target, String propertyName, Object object) {
		if(object instanceof String) 			{	target.addProperty(propertyName, (String)object); }
		else if(object instanceof JsonElement) 	{	target.add(propertyName, (JsonElement)object); }
		else if(object instanceof Number) 		{	target.addProperty(propertyName, (Number)object); }
		else if(object instanceof Boolean) 		{	target.addProperty(propertyName, (Boolean)object); }
		else if(object instanceof Character) 	{	target.addProperty(propertyName, (Character)object); }
		else if(object instanceof Date) 		{	target.addProperty(propertyName, ((Date)object).getTime()); }
		else if(object instanceof Clob) 		{	target.addProperty(propertyName, ((Clob)object).toString()); }
		else if(object instanceof Blob) 		{	target.addProperty(propertyName, ((Blob)object).toString()); }
		else if(object instanceof Timestamp) 	{	target.addProperty(propertyName, ((Timestamp)object).getTime()); }
		else if(object instanceof OffsetDateTime) {	target.addProperty(propertyName, ((OffsetDateTime)object).toInstant().toEpochMilli()); }
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
				JsonElement asElement = CFW.JSON.stringToJsonElement(value.toString());
				target.add(name, asElement);
			}else {
				target.add(name, gsonInstance.toJsonTree(value));
			}
			
		}else {
			CFW.JSON.addObject(target, name, value);
		}
		
	}
	
	/*************************************************************************************
	 * Makes a HTML table string from a JSON Object.
	 * 
	 * @param thisToTable the object to covnert
	 * @param narrow add the Bootstrap table-sm class to the table if true
	 * 
	 *************************************************************************************/
	public static String formatJsonObjectToHTMLTable(JsonObject thisToTable, boolean narrowTable) {
		
		String narrowClass = "";
		if(narrowTable) { narrowClass = "table-sm"; }
		
		StringBuilder html = new StringBuilder("<table class=\"table table-striped "+narrowClass+"\">");
		html.append("<thead> <th>Key</th>  <th>Value</th> </thead>");
		html.append("<tbody>");
		for(Entry<String, JsonElement> entry : thisToTable.entrySet()) {
			html.append("<tr>")
				.append("<td>").append( CFW.Utils.Text.fieldNameToLabel(entry.getKey()) ).append("</td>")
				.append("<td>").append(entry.getValue().getAsString()).append("</td>")
				.append("</tr>")
				;
		}
		html.append("</tbody></table>");
		
		return html.toString();
		
	}
	
	
	/*************************************************************************************
	 * Makes a CSV string from a JsonArray containing JsonObjects.
	 * 
	 * Takes the fieldnames of the first object as headers and to select values from
	 * every consecutive object.
	 * 
	 * @param thisToTable the object to covnert
	 * @param narrow add the Bootstrap table-sm class to the table if true
	 * 
	 * @return empty string if array is empty
	 *************************************************************************************/
	public static String formatJsonArrayToCSV(JsonArray convertThis, String delimiter) {
		
		String narrowClass = "";
		if(convertThis == null || convertThis.isEmpty() ) { return ""; }
		
		
		//------------------------------------------
		// Create Header 
		StringBuilder csv = new StringBuilder();
		ArrayList<String> memberNames = new ArrayList<>(); 
		
		for(Entry<String, JsonElement> entry : convertThis.get(0).getAsJsonObject().entrySet()) {
			memberNames.add(entry.getKey());
			csv.append("\"")
			   .append(CFW.JSON.escapeString(entry.getKey()))
			   .append("\"")
			   .append(delimiter);
		}
		csv.deleteCharAt(csv.length()-1); //remove last comma
		csv.append("\r\n");
		
		//------------------------------------------
		// Create Rows
		
		convertThis.forEach(new Consumer<JsonElement>() {

			@Override
			public void accept(JsonElement element) {
				
				if(element != null && element.isJsonObject()) {
					JsonObject object = element.getAsJsonObject();
					for(String name : memberNames) {
						
						JsonElement currentValue = object.get(name);
						
						String stringValue = "";
						if(currentValue.isJsonPrimitive()) 
							{ stringValue = currentValue.getAsString(); }
						else if(currentValue.isJsonArray()
								|| currentValue.isJsonObject()) {
							stringValue = CFW.JSON.toJSON(currentValue);
						}
							
							
						csv.append("\"")
						   .append(CFW.JSON.escapeString(stringValue))
						   .append("\"")
						   .append(delimiter);
					}
					csv.deleteCharAt(csv.length()-1); //remove last comma
					csv.append("\r\n");
				}
			}
		});
		

		return csv.toString();
		
	}

}
