package com.xresch.cfw.utils;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public class CFWHTML {
	
	public static final Logger logger = CFWLog.getLogger(CFWHTML.class.getName());
	
	/**************************************************************************************
	 * Parses a HTML String and returns a document.
	 * 
	 * @param xmlString
	 * @param doFlat if true returns a flat structure, else returns a hierarchical structure
	 * @return document or null on error
	 **************************************************************************************/		
	public static Document parseToDocument(String htmlString) {

		Document doc = Jsoup.parse(htmlString);
		
		return doc;
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/		
	public static JsonArray extractTablesAsJsonArray(Document doc) {
		
		
		JsonArray result = new JsonArray();
		
		if(doc != null) {
			Elements tableElements = doc.getElementsByTag("table");
			
			int tableIndex = 0;
			for(Element table : tableElements) {
				
				//---------------------------
				// Extract headers
				ArrayList<String> headers = new ArrayList<>();
				for(Element headerCell : table.getElementsByTag("th")) {
					headers.add(headerCell.text());
				}
				
				
				//-----------------------------
				// Attributes
				JsonObject attributesObject = new JsonObject();
				for(Attribute attribute : table.attributes()) {
					attributesObject.addProperty(attribute.getKey(), attribute.getValue());
				}
				
				//-----------------------------
				// Extract Data
				for(Element row : table.select("tr")) {
					JsonObject record = new JsonObject();
					JsonObject data = new JsonObject();
					record.addProperty("tableindex", tableIndex);
					record.add("attributes", attributesObject);
					record.add("data", data);
					
					
					int columnIndex = 0;
					for(Element cell : row.select("td") ) {
						
						String columnName = "column-"+columnIndex;
						if(columnIndex < headers.size()) {
							columnName = headers.get(columnIndex);
						}
						
						data.addProperty(columnName, cell.text());
						
						columnIndex++;
					}
					
					result.add(record);
				}
				
				
				tableIndex++;
			}
		}

		return result;
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/		
	public static JsonArray convertDocumentToJson(Document doc, String prefix, boolean doFlat) {
		
		
		JsonArray result = new JsonArray();
		
		if(doc != null) {
			Elements elements = doc.children();
			convertElementsToJson(prefix, elements, result, doFlat);
		}

		return result;
	}
	
	
	/**************************************************************************************
	 * Transforms the elements to a flat JsonArray with JsonObjects.
	 * This method will be called recursively for each child node.
	 * 
	 * @param parentKey the prefix added to the key to represent the folder/file/node structure.
	 * @param elements the node list to transform
	 * @param result the CSVData instance were the results will be stored
	 * @param doFlat if true returns a flat structure, else returns a hierarchical structure
	 **************************************************************************************/	
	public static void convertElementsToJson(String parentKey, Elements elements, JsonArray result, boolean doFlat){
		
		//----------------------------------
		// Check Nodes
		if(elements == null || elements.size() == 0) {
			return;
		}
		
		//----------------------------------
		// Prepare Key
		if(!Strings.isNullOrEmpty(parentKey)) {
			parentKey += ".";
		}else {
			parentKey = "";
		}
		
		//----------------------------------
		// Iterate Nodes
		for(int i = 0; i < elements.size(); i++){
			Element current = elements.get(i);
			String currentName = current.tagName();
			
			JsonObject row = new JsonObject();
			row.addProperty("tag", currentName);
					
			//----------------------------------
			// Get Element Text
			String elementText = current.ownText();

			elementText = (elementText != null) ? elementText : "";
			elementText = elementText.replaceAll("\n|\r\n|\t", " ").trim();
			
			//----------------------------------
			// Create unique key 
			String nextKey = parentKey + currentName;
			
			// Create a list which only contain the element nodes
			// with the same name as the currentNode
			ArrayList<Element> filteredElementNodes = new ArrayList<Element>();
			for(int j = 0; j < elements.size(); j++){
				Element element = elements.get(j);
				if(element.tagName().equals(currentName)){
					filteredElementNodes.add(elements.get(j));
				}
			
			}
			
			//Place an index 
			for( Element element : filteredElementNodes){
				String checkName = element.tagName();
				if(currentName.equals(checkName) && !element.equals(current)){
					int index = filteredElementNodes.indexOf(current);
					nextKey = nextKey + "[" + index + "]";
					break;
				}
			}
			
			//----------------------------------------------
			// Create Value: check for Attributes
			row.addProperty("key", nextKey);
			row.addProperty("text", elementText);
			
			JsonObject attributesObject = new JsonObject();
			row.add("attributes", attributesObject);	
			
			
			//----------------------------------------------
			// Create Value: check for Attributes
			if(current.attributesSize() > 0){
				Attributes attributes = current.attributes();
				
				for(Attribute attribute : attributes){
					attributesObject.addProperty(attribute.getKey(), attribute.getValue());
				}
				
			}
			
			result.add(row);
			
			if(current.childNodeSize() > 0){
				
				JsonArray nextResult = result;
				
				if( !doFlat ) {
					JsonArray childrenArray = new JsonArray();
					row.add("children", childrenArray);
					nextResult = childrenArray;
				}
				
				convertElementsToJson(nextKey, current.children(), nextResult, doFlat);
			}
			
		}
		
	}

}
