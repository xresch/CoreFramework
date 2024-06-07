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
	 * @return document or null on error
	 **************************************************************************************/		
	public static Document parseToDocument(String htmlString) {

		Document doc = Jsoup.parse(htmlString);
		
		return doc;
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/		
	public static JsonArray convertDocumentToJsonFlat(Document doc, String prefix) {
		
		
		JsonArray result = new JsonArray();
		
		if(doc != null) {
			Elements elements = doc.children();
			convertElementsToJsonFlat(prefix, elements, result);
		}

		return result;
	}
	
	/**************************************************************************************
	 * Transforms the elements to a flat JsonStructure.
	 * This method will be called recursively for each child node.
	 * 
	 * @param parentKey the prefix added to the key to represent the folder/file/node structure.
	 * @param elements the node list to transform
	 * @param result the CSVData instance were the results will be stored
	 **************************************************************************************/	
	public static void convertElementsToJsonFlat(String parentKey, Elements elements, JsonArray result){
		
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
			JsonObject row = new JsonObject();
		
			String currentName = current.tagName();
					
			//----------------------------------
			// Get Element Text
			String elementText = current.ownText();

			if(!Strings.isNullOrEmpty(elementText)){
				elementText = elementText.replaceAll("\r\n|\r|\n|\t", " ");
			}
				
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
			JsonObject attributesObject = new JsonObject();
			row.add("attributes", attributesObject);	
			
			attributesObject.addProperty("owntext", elementText);
			
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
				convertElementsToJsonFlat(nextKey, current.children(), result);
			}
			
		}
		
	}


}
