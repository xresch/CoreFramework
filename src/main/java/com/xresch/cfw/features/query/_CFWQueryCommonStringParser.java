package com.xresch.cfw.features.query;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeSet;

import org.w3c.dom.Document;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.utils.web.CFWHttp.CFWHttpResponse;

/**************************************************************************************************************
 * 
 * Class contains common string parsers that parses string input into Query Result records
 * 
 * @author Reto Scheiwiller
 * 
 * (c) Copyright 2024
 * 
 * @license MIT-License
 **************************************************************************************************************/
public class _CFWQueryCommonStringParser {
	
	
	/*******************************************************************************
	 * Enum for defining available String Parsers
	 *******************************************************************************/
	public enum CFWQueryStringParserType {

		  json("Parse the whole response as a json object or array.")
		, jsonlines("Parse each line of the response as a json object.")
		, html("Parse the response as HTML and convert it into a flat table.")
		, htmltables("Parse the response as HTML and extracts all table data found in the HTML.")
		, htmltree("Parse the response as HTML and convert it into a json structure.")
		, xml("Parse the response as XML and convert it into a flat table.")
		, xmltree("Parse the response as XML and convert it into a json structure.")
		, csv("Parse the response as comma-separated-values(CSV).")
		, plain("Parse the response as plain text and convert it to a single record with field 'response'.")
		, http("Parse the response as HTTP and creates a single record containing either Plain HTML or if a full response is given: HTTP status, headers and body.")
		, lines("Parse the response as text and return every line as its own record.")
		;
		
		//==============================
		// Caches
		private static TreeSet<String> enumNames = null;		
		
		//==============================
		// Fields
		private String shortDescription;

		//==============================
		// Constructor
		private CFWQueryStringParserType(String shortDescription) {
			this.shortDescription = shortDescription;
		}
				
		public String shortDescription() { return this.shortDescription; }
		
		//==============================
		// Returns a set with all names
		public static TreeSet<String> getNames() {
			if(enumNames == null) {
				enumNames = new TreeSet<>();
				
				for(CFWQueryStringParserType unit : CFWQueryStringParserType.values()) {
					enumNames.add(unit.name());
				}
			}
			return enumNames;
		}
				
		//==============================
		// Check if enum exists by name
		public static boolean has(String enumName) {
			return getNames().contains(enumName);
		}
		
		//==============================
		// Create a HTML List 
		public static String getDescriptionHTMLList() {
			StringBuilder builder = new StringBuilder();
			
			builder.append("<ul>");
				for(CFWQueryStringParserType type : CFWQueryStringParserType.values()) {
					builder.append("<li><b>"+type.toString()+":&nbsp;</b>"+type.shortDescription+"</li>");
				}
			builder.append("</ul>");
			return builder.toString();
		}

	}
	
	/****************************************************************************
	 * Parse the string with the given Type
	 ****************************************************************************/
	public static ArrayList<EnhancedJsonObject> parse(String type, CFWHttpResponse response) throws Exception {
		
		type = type.trim().toLowerCase();
		
		if( !CFWQueryStringParserType.has(type) ){
			CFW.Messages.addErrorMessage(" Parser Type: "+type+"' is not known."
						+" Available options: "+CFW.JSON.toJSON( CFWQueryStringParserType.getNames()) );
			return new ArrayList<>();
		}
		
		return parse( type, response);
	}
	/****************************************************************************
	 * Parse the string with the given Type
	 * @param csvSeparator TODO
	 ****************************************************************************/
	public static ArrayList<EnhancedJsonObject> parse(CFWQueryStringParserType type, CFWHttpResponse response, String csvSeparator) throws Exception{
		
		if(response == null) { return new ArrayList<>();}
		
		if(type == CFWQueryStringParserType.http) {
			return parseAsHTTP(response);
		}else {
			return parse(type, response.getResponseBody(), csvSeparator);
		}
	}
	
	/****************************************************************************
	 * Parse the string with the given Type
	 * @param csvSeparator TODO
	 ****************************************************************************/
	public static ArrayList<EnhancedJsonObject> parse(String type, String data, String csvSeparator) throws Exception {
		
		type = type.trim().toLowerCase();
		
		if( !CFWQueryStringParserType.has(type) ){
			CFW.Messages.addErrorMessage(" Parser Type: "+type+"' is not known."
						+" Available options: "+CFW.JSON.toJSON( CFWQueryStringParserType.getNames()) );
			return new ArrayList<>();
		}
		
		return parse( CFWQueryStringParserType.valueOf(type), data, csvSeparator);
		
	}
	
	/******************************************************************
	 * Parses the string with the 
	 * @param csvSeparator TODO
	 ******************************************************************/
	public static ArrayList<EnhancedJsonObject> parse(CFWQueryStringParserType type, String data, String csvSeparator) throws Exception {
		ArrayList<EnhancedJsonObject> result = new ArrayList<>();

		switch(type) {
			
			case json:			result = parseAsJson(data);	break;	
			case jsonlines:		result = parseAsJsonLines(data);	break;	
			case html:			result = parseAsHTML(data); 	break;
			case htmltables:	result = parseAsHTMLTables(data); 	break;
			case htmltree:		result = parseAsHTMLTree(data); 	break;
			case xml:			result = parseAsXML(data); 	break;
			case xmltree:		result = parseAsXMLTree(data); 	break;
			case csv:			result = parseAsCSV(data, csvSeparator); 	break;
			case plain:			result = parseAsPlain(data); 	break;
			case lines:			result = parseAsLines(data); 	break;
			
			// Fall back to HTML
			case http:			result = parseAsPlain(data); break;

		}


		return result;
	}
		
	
	/******************************************************************
	 *
	 ******************************************************************/
	public static ArrayList<EnhancedJsonObject> parseAsJsonLines( String data ) throws Exception {
		
		ArrayList<EnhancedJsonObject> result = new ArrayList<>();
		 
		Scanner scanner = new Scanner(data.trim());

		while(scanner.hasNextLine()) {
			
			String line = scanner.nextLine();
			result.addAll(parseAsJson(line));
		}
		
		return result;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	public static ArrayList<EnhancedJsonObject> parseAsJson( String data ) throws Exception {
		
		ArrayList<EnhancedJsonObject> result = new ArrayList<>();
		
		//------------------------------------
		// Parse Data
		JsonElement element;
		
		element = CFW.JSON.fromJson(data);
		
		//------------------------------------
		// Handle Object
		if(element.isJsonObject()) {
			
			result.add( new EnhancedJsonObject(element.getAsJsonObject()) );
			return result;
		}
		
		//------------------------------------
		// Handle Array
		if(element.isJsonArray()) {
			
			for(JsonElement current : element.getAsJsonArray() ) {
				if(current.isJsonObject()) {
					result.add( new EnhancedJsonObject(current.getAsJsonObject()) );
				}
			}
			
		}
		
		return result;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	public static ArrayList<EnhancedJsonObject> parseAsXML(String data) throws Exception {
		
		ArrayList<EnhancedJsonObject> result = new ArrayList<>();
		 
		//------------------------------------
		// Parse Data
		
		Document document = CFW.XML.parseToDocument(data);
		JsonArray array = CFW.XML.convertDocumentToJson(document, "", true);
		
		for(JsonElement element : array) {
			EnhancedJsonObject object = new EnhancedJsonObject(element.getAsJsonObject());
			result.add( object );
		}
			
		return result;
		
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	public static ArrayList<EnhancedJsonObject> parseAsCSV(String data, String separator) throws Exception {
		
		ArrayList<EnhancedJsonObject> result = new ArrayList<>();
		
		//------------------------------------
		// Parse Data
		JsonArray array = CFW.CSV.toJsonArray(data, separator, false, true);
		
		for(JsonElement element : array) {
			EnhancedJsonObject object = new EnhancedJsonObject(element.getAsJsonObject());
			result.add( object );
		}
		
		return result;
		
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	public static ArrayList<EnhancedJsonObject> parseAsXMLTree(String data) throws Exception {
		
		ArrayList<EnhancedJsonObject> result = new ArrayList<>();
		 
		//------------------------------------
		// Parse Data
		Document document = CFW.XML.parseToDocument(data);
		JsonArray array = CFW.XML.convertDocumentToJson(document, "", false);
		
		for(JsonElement element : array) {
			EnhancedJsonObject object = new EnhancedJsonObject(element.getAsJsonObject());
			result.add( object );
		}
			
		return result;
		
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	public static ArrayList<EnhancedJsonObject> parseAsHTML(String data) throws Exception {
		
		ArrayList<EnhancedJsonObject> result = new ArrayList<>();
		 		
		//------------------------------------
		// Parse Data
		org.jsoup.nodes.Document document = CFW.HTML.parseToDocument(data);
		JsonArray array = CFW.HTML.convertDocumentToJson(document, "", true);
		
		for(JsonElement element : array) {
			EnhancedJsonObject object = new EnhancedJsonObject(element.getAsJsonObject());
			result.add( object );
		}
		
		return result;
		
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	public static ArrayList<EnhancedJsonObject> parseAsHTMLTree(String data) throws Exception{
		
		ArrayList<EnhancedJsonObject> result = new ArrayList<>();
		 
		//------------------------------------
		// Parse Data

		org.jsoup.nodes.Document document = CFW.HTML.parseToDocument(data);
		JsonArray array = CFW.HTML.convertDocumentToJson(document, "", false);
		
		for(JsonElement element : array) {
			EnhancedJsonObject object = new EnhancedJsonObject(element.getAsJsonObject());
			result.add( object );
		}
			
		return result;
		
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	public static ArrayList<EnhancedJsonObject> parseAsHTMLTables(String data) throws Exception{
		
		ArrayList<EnhancedJsonObject> result = new ArrayList<>();
		 
		//------------------------------------
		// Parse Data
		org.jsoup.nodes.Document document = CFW.HTML.parseToDocument(data);
		JsonArray array = CFW.HTML.extractTablesAsJsonArray(document);
		
		for(JsonElement element : array) {
			EnhancedJsonObject object = new EnhancedJsonObject(element.getAsJsonObject());
			result.add( object );
		}
			
		return result;
		
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	public static ArrayList<EnhancedJsonObject> parseAsPlain(String data) throws Exception {
		
		ArrayList<EnhancedJsonObject> result = new ArrayList<>();
		 
		//------------------------------------
		// Parse Data	
		EnhancedJsonObject object = new EnhancedJsonObject();
		object.addProperty("result", data);
		result.add( object );
			
		return result;
		
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	public static ArrayList<EnhancedJsonObject> parseAsLines(String data) throws Exception {
		
		ArrayList<EnhancedJsonObject> result = new ArrayList<>();
		 
		//------------------------------------
		// Parse Data

		if(!Strings.isNullOrEmpty(data)) {
			
			for( String line : data.split("\n\r|\n") ){
				EnhancedJsonObject object = new EnhancedJsonObject();
				object.addProperty("line", line);
				result.add( object );
			}
		}
			
		return result;
		
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	public static ArrayList<EnhancedJsonObject> parseAsHTTP(CFWHttpResponse response) throws Exception {
		
		ArrayList<EnhancedJsonObject> result = new ArrayList<>();
		
		//------------------------------------
		// Parse Data
		EnhancedJsonObject object = new EnhancedJsonObject();
		object.addProperty("url", response.getURL().toString());
		object.addProperty("status", response.getStatus());
		object.addProperty("duration", response.getDuration());
		object.add("headers", response.getHeadersAsJson());
		object.addProperty("body", response.getResponseBody());
		result.add( object );
			
		return result;
		
	}
	
	
	
	
}