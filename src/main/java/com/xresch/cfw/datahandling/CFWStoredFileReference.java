package com.xresch.cfw.datahandling;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.filemanager.CFWStoredFile;

public class CFWStoredFileReference {

	private static final String MEMBER_ID = "id";
	private static final String MEMBER_NAME = "name"; // the filename, including file extension
	private static final String MEMBER_TYPE = "type"; // basically the file extension without dot
	private static final String MEMBER_SIZE = "size"; // basically the file extension without dot
	
	private JsonObject dbfileData;
			
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWStoredFileReference() {
		setToDefaults();
		
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWStoredFileReference(String jsonString) {
		
		setToDefaults();
		
		if(Strings.isNullOrEmpty(jsonString)) {
			return;
		}
		
		JsonElement element = CFW.JSON.stringToJsonElement(jsonString);
		if(!element.isJsonNull() && element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			
			if(object.keySet().isEmpty()) {
				return;
			}
						
			if(object.has(MEMBER_NAME)) {
				dbfileData.add(MEMBER_NAME, object.get(MEMBER_NAME));
			}
			
			if(object.has(MEMBER_ID)) {
				dbfileData.add(MEMBER_ID, object.get(MEMBER_ID));
			}
			
			if(object.has(MEMBER_TYPE)) {
				dbfileData.add(MEMBER_TYPE, object.get(MEMBER_TYPE));
			}
			
			if(object.has(MEMBER_SIZE)) {
				dbfileData.add(MEMBER_SIZE, object.get(MEMBER_SIZE));
			}
			
		}
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWStoredFileReference(CFWStoredFile file) {
		
		setToDefaults();
		
		if(file == null) { return; }
				
		if(file.name() != null) {		dbfileData.addProperty(MEMBER_NAME, file.name()); }
		if(file.id() != null) {			dbfileData.addProperty(MEMBER_ID, file.id()); }
		if(file.size() != null) {	dbfileData.addProperty(MEMBER_SIZE, file.size()); }
		if(file.mimetype() != null) {	dbfileData.addProperty(MEMBER_TYPE, file.mimetype()); }
		
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	private void setToDefaults() {
		
		dbfileData = new JsonObject();

		dbfileData.add(MEMBER_NAME, JsonNull.INSTANCE);
		dbfileData.add(MEMBER_ID, JsonNull.INSTANCE);
		dbfileData.add(MEMBER_TYPE, JsonNull.INSTANCE);
		dbfileData.add(MEMBER_SIZE, JsonNull.INSTANCE);
				
	}
	
	

	/***************************************************************************************
	 * Returns the earliest time of the timeframe as epoch millis.
	 ***************************************************************************************/
	public long getName() {
		return dbfileData.get(MEMBER_NAME).getAsLong();
		
	}
	
	/***************************************************************************************
	 * Convert to JSON String
	 ***************************************************************************************/
	@Override
	public String toString() {
		return dbfileData.toString();
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public JsonObject getAsJsonObject() {
		return dbfileData.deepCopy();
	}

}
