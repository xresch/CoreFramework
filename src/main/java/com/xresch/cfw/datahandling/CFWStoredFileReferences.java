package com.xresch.cfw.datahandling;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.filemanager.CFWStoredFile;

/**************************************************************************************************************
 * Keeps a JsonArray of file references associate with a specific File Picker.
 * Allows to create a CFWStoredFilesReference from a CFWStoredFile.
 * @author Reto Scheiwiller, (c) Copyright 2025
 * @license MIT-License
 **************************************************************************************************************/
public class CFWStoredFileReferences {

	public static final String MEMBER_ID = "id"; 
	public static final String MEMBER_NAME = "name"; // the filename, including file extension
	public static final String MEMBER_TYPE = "type"; // basically the file extension without dot
	public static final String MEMBER_SIZE = "size"; // basically the file extension without dot
	
	private JsonArray dbfileData;
			
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWStoredFileReferences() {
		setToDefaults();
		
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWStoredFileReferences(String jsonArrayString) {
		
		setToDefaults();
		
		if(Strings.isNullOrEmpty(jsonArrayString)) {
			return;
		}
		
		JsonElement element = CFW.JSON.stringToJsonElement(jsonArrayString);
		
		if(element.isJsonNull()) {
			return;
		}
		
		if(element.isJsonArray()) {
			dbfileData = element.getAsJsonArray();
			return;
		}
		
		if(element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			dbfileData.add(object);
		}
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	public CFWStoredFileReferences(CFWStoredFile file) {
		
		setToDefaults();
		
		if(file == null) { return; }
		
		JsonObject object = new JsonObject();
		if(file.name() != null) {		object.addProperty(MEMBER_NAME, file.name()); }
		if(file.id() != null) {			object.addProperty(MEMBER_ID, file.id()); }
		if(file.size() != null) {	object.addProperty(MEMBER_SIZE, file.size()); }
		if(file.mimetype() != null) {	object.addProperty(MEMBER_TYPE, file.mimetype()); }
		
		dbfileData.add(object);
	}
	
	/***************************************************************************************
	 * 
	 ***************************************************************************************/
	private void setToDefaults() {
		
		dbfileData = new JsonArray();
				
	}
	
	/***************************************************************************************
	 * Get number of files in this reference.
	 ***************************************************************************************/
	public long size() {
		return dbfileData.size();
	}
	
	/***************************************************************************************
	 * Get the file reference at the specified index.
	 ***************************************************************************************/
	public JsonObject get(int index) {
		return dbfileData.get(index).getAsJsonObject();
		
	}
	
	/***************************************************************************************
	 * Get the the ID of the file reference at the specified index.
	 ***************************************************************************************/
	public Integer getID(int index) {
		return dbfileData
					.get(index)
					.getAsJsonObject()
					.get(MEMBER_ID)
					.getAsInt()
					;
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
	public JsonArray getAsJsonArray() {
		return dbfileData.deepCopy();
	}

}
