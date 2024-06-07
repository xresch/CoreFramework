package com.xresch.cfw.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public class CFWXML {
	
	public static final Logger logger = CFWLog.getLogger(CFWXML.class.getName());
	
	/**************************************************************************************
	 * Parses an XML String and returns a document.
	 * 
	 * @param xmlString
	 * @return document or null on error
	 **************************************************************************************/		
	public static Document parseToDocument(String xmlString) {
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
		dbf.setExpandEntityReferences(false);
		DocumentBuilder builder;
		Document document = null;
		
		try {
			builder = dbf.newDocumentBuilder();
			builder.setEntityResolver(new EntityResolver() {

	            @Override
	            public InputSource resolveEntity(String publicId, String systemId)
	                    throws SAXException, IOException {
	            	// Ignore .dtd-Files
	                return new InputSource(new StringReader(""));
	            }
	        });

			document = builder.parse( new InputSource(new StringReader(xmlString)) );

			builder.reset();
		} catch (Exception e) { 
			new CFWLog(logger).severe("Error while parsing XML: "+e.getMessage(), e);
		}

		return document;
	}
	
	/**************************************************************************************
	 * Transforms an xml-file to csv values, filtered
	 * by the file filter defined in the arguments.
	 *
	 * @param document the document to convert
	 * @return JsonArray containing JsonObjects, or empty
	 **************************************************************************************/		
	public static JsonArray convertDocumentToJsonFlat(Document document, String prefix) {
		
		
		JsonArray result = new JsonArray();
		
		if(document != null) {
			NodeList nodes = document.getChildNodes();
			convertNodesToJsonFlat(prefix, nodes, result);
		}

		return result;
	}
	
	/**************************************************************************************
	 * Transforms the nodes of the nodelist to csv key value pairs.
	 * This method will be called recursively for each child node.
	 * 
	 * @param parentKey the prefix added to the key to represent the folder/file/node structure.
	 * @param nodes the node list to transform
	 * @param result the CSVData instance were the results will be stored
	 **************************************************************************************/	
	public static void convertNodesToJsonFlat(String parentKey, NodeList nodes, JsonArray result){
		
		//----------------------------------
		// Check Nodes
		if(nodes == null || nodes.getLength() == 0) {
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
		for(int i = 0; i < nodes.getLength(); i++){
			Node currentNode = nodes.item(i);
			JsonObject row = new JsonObject();
			
			if(currentNode.getNodeType() == Node.CDATA_SECTION_NODE){
				String key = parentKey+".cdata";
				String value = currentNode.getNodeValue().replaceAll("\n|\r\n|\t", " ");
				
				row.addProperty("Key", key);
				row.addProperty("Value", value);
				result.add(row);

				logger.info("Transformed xml-Node: "+key+" >> "+value);
			}else if(currentNode.getNodeType() == Node.ELEMENT_NODE) {
				
				String currentName = currentNode.getNodeName();
					
				//----------------------------------
				// Get Element Text
				String elementText = "";
				
				if(currentNode.hasChildNodes()){
					
					Node firstChild = currentNode.getFirstChild();
					
					if(firstChild.getNodeType() == Node.TEXT_NODE){
						elementText = firstChild.getNodeValue();
						elementText = elementText.replaceAll("\r\n|\r|\n|\t", " ");
					}
				}
					
				//----------------------------------
				// Create unique key 
				String nextKey = parentKey + currentName;
				
				// Create a list which only contain the element nodes
				// with the same name as the currentNode
				ArrayList<Node> filteredElementNodes = new ArrayList<Node>();
				for(int j = 0; j < nodes.getLength(); j++){
					Node node = nodes.item(j);
					if(   node.getNodeType() == Node.ELEMENT_NODE
					   && node.getNodeName().equals(currentName)){
						filteredElementNodes.add(nodes.item(j));
					}
				
				}
				
				//Place an index 
				for( Node element : filteredElementNodes){
					String checkName = element.getNodeName();
					if(currentName.equals(checkName) && !element.equals(currentNode)){
						int index = filteredElementNodes.indexOf(currentNode);
						nextKey = nextKey + "[" + index + "]";
						break;
					}
				}

				//----------------------------------------------
				// Create Value: check for Attributes
				StringBuilder currentValue = new StringBuilder();
				
				if(currentNode.hasAttributes()){
					NamedNodeMap attributes = currentNode.getAttributes();
					
					for(int k = 0; k < attributes.getLength();k++){
						Node attribute = attributes.item(k);
						currentValue.append(attribute.getNodeName())
									.append("=")
									.append(attribute.getNodeValue());

						//add comma if it is not the last attribute
						if(k != attributes.getLength()-1 ) {
							currentValue.append(" / ");
						}
					}
					
					if(!elementText.toString().trim().isEmpty()){
						currentValue.append(" / text=")
									.append(elementText);
					}
				}else{
					currentValue.append(elementText);
				}
				
				//----------------------------------
				// Add row to result 
				if(!currentValue.toString().trim().isEmpty() ){
					
					row.addProperty("Key", nextKey);
					row.addProperty("Value", currentValue.toString());
					result.add(row);
					logger.info("Transformed xml-Node to csv: "+nextKey+" >> "+currentValue.toString());
				}
				
				if(currentNode.hasChildNodes()){
					convertNodesToJsonFlat(nextKey, currentNode.getChildNodes(), result);
				}
				
			}
		}
	}

}
