package com.xresch.cfw.utils.csv;

import java.util.ArrayList;
import java.util.Scanner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.utils.json.CFWJson;


/**************************************************************************************************************
 * Collection of static methods to handle CSV input.
 * 
 * License: MIT License
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * 
 **************************************************************************************************************/
public class CFWCSV {

	/*******************************************************************
	 * Splits a row of a CSV record while being aware of quotes.
	 * The method will act as follows:
	 * <ul>
	 * 		<li><b>Separators:&nbsp;</b> Can be one character or a sequence of characters. Does not support regex.</li>
	 * 		<li><b>Double Quotes:&nbsp;</b> Can be used to wrap values to allow it to contain separators. Quotes inside can be escaped using '\';  </li>
	 * 		<li><b>Blanks:&nbsp;</b>Blanks at beginning and end of a values are ignored, except if a value is quoted.</li>
	 * 		<li><b>Skipped Values:&nbsp;</b> If two separators follow each other(with or without blanks in between), the value is considered skipped and will be returned as null. </li>
	 * 		<li><b>Skipped First&Last:&nbsp;</b>If a record starts/ends with a separator, the first/last value will be considered skipped  and will be returned as null.  </li>
	 * </ul>
	 * 
	 * @param csvRecord a single line of CSV data
	 * @param separator the separator, can be multiple characters, does not support regex
	 *******************************************************************/
	public static ArrayList<String> splitCSVQuotesAware(String separator, String csvRecord){
		
		//-----------------------------
		// Variables
		char previous = '½'; 	// the last character, initialized with random character, only used to check for escaping with '\'
		char current;			// the current character
		char separatorFirstChar = separator.charAt(0); // performance improvement, check first char instead of doing a substring every time
		int cursor = 0;			// position in parsing
		int startPos = -1; 		// used as starting position of a value
		boolean separatorSkipped = false; // used to detect subsequent separators
		
		ArrayList<String> result = new ArrayList<>();
		
		String line = csvRecord.trim();
		int LENGTH = line.length();
		
		//-----------------------------
		// Check first value omitted
		if(line.startsWith(separator)) {
			result.add(null);
			separatorSkipped = true;
			cursor += separator.length();
			
		}
		
		//-----------------------------
		// Parse CSV Record 
		outer:
		while( cursor < LENGTH ) {
			
			if(cursor > 0) { previous = line.charAt(cursor-1); }
			current = line.charAt(cursor);
			
			//----------------------------
			// Skip Blanks
			while(current == ' ') { current = line.charAt(++cursor); }
			
			
			//----------------------------
			// Grab Quoted Text
			if(current == '"') {
				
				//----------------------------
				// Prevent Endless Loops
				if(cursor == LENGTH-1) {
					break outer;
				}
					
				//----------------------------
				// Loop Content
				separatorSkipped = false;
				startPos = cursor;

				inner:
				while(cursor < LENGTH-1 ) {

					previous = current;
					current = line.charAt(++cursor);
			
					if(current == '"') {
						
						//----------------------------
						// Check Might be Escape Character
						if(cursor < LENGTH-1) {
							char next = line.charAt(cursor+1);
							if(next == '"') {
								continue;
							}
						}
						
						//----------------------------
						// Check is Escaped
						int escapeCount = 0;
						if(previous == '"') {
							
							int i = cursor-1;
							for(; i > startPos; i--) {
								if(line.charAt(i) == previous) {
									escapeCount++;
								}else {
									break;
								}
							}
							if(line.charAt(i) == '\\') {
								escapeCount--;
							}
						}else if(previous == '\\') {
							
							int i = cursor-1;
							for(; i > startPos; i--) {
								if(line.charAt(i) == '\\') {
									escapeCount++;
								}else {
									break;
								}
							}
						}
						
						boolean isEscaped = (escapeCount % 2 != 0) ;
						if(isEscaped) {
							continue;
						}
						
						//----------------------------
						// Extract Column Value
						String potentialEscapedQuotes = line.substring(startPos+1, cursor);
						String noEscapes = potentialEscapedQuotes.replace("\\\"", "\"").replace("\"\"", "\"");
						result.add(noEscapes);
						cursor++;
						if(cursor < LENGTH ) {
							break inner;
						}else {
							break outer;
						}
					}
					
				}
			}

			//----------------------------
			// Skip Blanks
			current = line.charAt(cursor);
			while(current == ' ') { current = line.charAt(++cursor); }
			
			//----------------------------
			// Skip Separator
			current = line.charAt(cursor);
			if(current == separatorFirstChar
			&& line.substring(cursor).startsWith(separator) ) {
				cursor += separator.length();
				if(separatorSkipped) {
					result.add(null);
				}
				separatorSkipped = true;
				continue; 
			}else {
				separatorSkipped = false;
			}
			
			//----------------------------
			// Grab Separated Text
			startPos = cursor;
			while(cursor < LENGTH ) {
				separatorSkipped = false;
				current = line.charAt(cursor);
				
				if(current == '"' ) { break; } // break and go let quotes section do the work
	
				if(current == separatorFirstChar
				&& line.substring(cursor).startsWith(separator)) {
					result.add(line.substring(startPos, cursor).trim());
					//cursor++;
					break;
				}
				
				cursor++;
			}
			
			//----------------------------
			// Grab Last
			if(cursor >= LENGTH) {
				result.add(line.substring(startPos, cursor).trim());
				break;
			}
		}
		
		//-----------------------------
		// Check Last Value omitted
		if(line.endsWith(separator)) {
			result.add(null);
			cursor += separator.length();
		}
	
		return result;
	}
	
	

	/*************************************************************************************
	 * Creates a JsonArray containing JsonObjects from a CSV string.
	 * First line has to be a header with column names. Column names will be used as field names.
	 * This method supports the use of quotes for field values and escaped quotes (\") 
	 * inside of quotes.
	 * If a CSV record has more columns than the header row the additional columns will 
	 * be ignored.
	 * 
	 * @param csv the CSV multi-line string including a header
	 * @param separator the separator, one or multiple characters, does not support regex
	 * @param makeFieldsLowercase set to true to make fieldnames lowercase, useful to make 
	 *        user input more save and stable to process.
	 * @param parseJsonStrings if set to true, attempts to convert values starting with either 
	 *       "{" or "[" to a JsonObject or JsonArray.
	 *        
	 *************************************************************************************/
	public static JsonArray toJsonArray(
								  String csv
								, String separator
								, boolean makeFieldsLowercase
								, boolean parseJsonStrings
							) {
	

		Scanner scanner = new Scanner(csv.trim());
	
		//----------------------------
		// Skip if Empty
		if(!scanner.hasNext()) {
			scanner.close();
			return new JsonArray();
		}
		
		//----------------------------
		// Get Headers
		String header = scanner.nextLine();
		
		ArrayList<String> headerArray = splitCSVQuotesAware(separator, header);
		
		if(makeFieldsLowercase) {
			headerArray = CFW.Utils.Text.arrayToLowercase(headerArray);
		}
		
		//----------------------------
		// Get CSV
		return toJsonArray(separator, headerArray, parseJsonStrings, scanner);
		
	}
	
	/*************************************************************************************
	 * Creates a JsonElement from a CSV string.
	 * This will either:
	 * 	- Return an empty JsonObject of there is no record
	 *  - Return the record as JsonObject if there is exactly one record
	 *  - Return an array if there is an array
	 *  
	 * First line has to be a header with column names. Column names will be used as field names.
	 * This method supports the use of quotes for field values and escaped quotes (\") 
	 * inside of quotes.
	 * If a CSV record has more columns than the header row the additional columns will 
	 * be ignored.
	 * 
	 * @param csv the CSV multi-line string including a header
	 * @param separator the separator, one or multiple characters, does not support regex
	 * @param makeFieldsLowercase set to true to make fieldnames lowercase, useful to make 
	 *        user input more save and stable to process.
	 * @param parseJsonStrings if set to true, attempts to convert values starting with either 
	 *       "{" or "[" to a JsonObject or JsonArray.
	 *        
	 *************************************************************************************/
	public static JsonElement toJsonElement(
								  String csv
								, String separator
								, boolean makeFieldsLowercase
								, boolean parseJsonStrings
							) {
	
		JsonArray array = toJsonArray(csv, separator, makeFieldsLowercase, parseJsonStrings);
		
		if(array.isEmpty()) {
			return new JsonObject();
		}else if(array.size() == 1) {
			return array.get(0);
		}
		
		return array;
		
	}
	
	/*************************************************************************************
	 * Creates a JsonArray containing JsonObjects from a CSV string.
	 * First line has to be a header with column names. Column names will be used as field names.
	 * This method supports the use of quotes for field values and escaped quotes (\") 
	 * inside of quotes.
	 * If a CSV record has more columns than the header row the additional columns will 
	 * be ignored.
	 * 
	 * @param csv the CSV multi-line string including a header
	 * @param separator the separator, one or multiple characters, does not support regex
	 * @param headerArray the headers for the CSV
	 * @param makeFieldsLowercase set to true to make fieldnames lowercase, useful to make 
	 *        user input more save and stable to process.
	 * @param parseJsonStrings if set to true, attempts to convert values starting with either 
	 *       "{" or "[" to a JsonObject or JsonArray.
	 *        
	 *************************************************************************************/
	public static JsonArray toJsonArray(
								  String csv
								, String separator
								, ArrayList<String> headerArray
								, boolean parseJsonStrings
							) {

		Scanner scanner = new Scanner(csv.trim());
	
		//----------------------------
		// Skip if Empty
		if(!scanner.hasNext()) {
			scanner.close();
			return new JsonArray();
		}

		return toJsonArray(separator, headerArray, parseJsonStrings, scanner);
		
	}
	
	/*************************************************************************************
	 * Creates a JsonElement from a CSV string.
	 * This will either:
	 * 	- Return an empty JsonObject of there is no record
	 *  - Return the record as JsonObject if there is exactly one record
	 *  - Return an array if there is an array
	 *  
	 * First line has to be a header with column names. Column names will be used as field names.
	 * This method supports the use of quotes for field values and escaped quotes (\") 
	 * inside of quotes.
	 * If a CSV record has more columns than the header row the additional columns will 
	 * be ignored.
	 * 
	 * @param csv the CSV multi-line string including a header
	 * @param separator the separator, one or multiple characters, does not support regex
	 * @param headerArray the headers for the CSV
	 * @param makeFieldsLowercase set to true to make fieldnames lowercase, useful to make 
	 *        user input more save and stable to process.
	 * @param parseJsonStrings if set to true, attempts to convert values starting with either 
	 *       "{" or "[" to a JsonObject or JsonArray.
	 *        
	 *************************************************************************************/
	public static JsonElement toJsonElement(
								  String csv
								, String separator
								, ArrayList<String> headerArray
								, boolean parseJsonStrings
							) {

		JsonArray array = toJsonArray(csv, separator, headerArray, parseJsonStrings);
		
		if(array.isEmpty()) {
			return new JsonObject();
		}else if(array.size() == 1) {
			return array.get(0);
		}
		
		return array;
	}


	/*************************************************************************************
	 * Creates a JsonArray containing JsonObjects from a CSV string.
	 * First line has to be a header with column names. Column names will be used as field names.
	 * This method supports the use of quotes for field values and escaped quotes (\") 
	 * inside of quotes.
	 * If a CSV record has more columns than the header row the additional columns will 
	 * be ignored.
	 * 
	 * @param csv the CSV multi-line string including a header
	 * @param separator the separator, one or multiple characters, does not support regex
	 * @param headerArray the headers for the CSV
	 * @param scanner that is used to parse through the lines of the CSV
	 * @param parseJsonStrings if set to true, attempts to convert values starting with either 
	 *       "{" or "[" to a JsonObject or JsonArray.
	 *        
	 *************************************************************************************/
	private static JsonArray toJsonArray(
								  String separator
								, ArrayList<String> headerArray
								, boolean parseJsonStrings
								, Scanner scanner
							) {
		
		JsonArray result = new JsonArray();
		//----------------------------
		// Process Records
		while(scanner.hasNext()) {
			String csvRecord = scanner.nextLine();
			ArrayList<String> valuesArray = splitCSVQuotesAware(separator, csvRecord);
			
			JsonObject object = new JsonObject();
			for(int i = 0 ; i < headerArray.size(); i++) {
				String fieldname = headerArray.get(i);
				
				JsonElement value = JsonNull.INSTANCE;
				
				if(i < valuesArray.size()) {  
					String valueString = valuesArray.get(i); 
					
					if(valueString != null) {
						value = new JsonPrimitive(valueString);
					
						if(parseJsonStrings
						&& (  valueString.startsWith("{")
						   || valueString.startsWith("[") 
						   )
						){
							try {
								value = CFWJson.fromJson(valueString);
							}catch(Throwable e) {
								new CFWLog(CFWJson.logger).warn("JSON from CSV: error while parsing: "+e.getMessage());
							}
						}
					}
				}
				
				object.add(fieldname, value);
				
			}
			
			result.add(object);
		}
	
		scanner.close();
		return result;
	}

}
