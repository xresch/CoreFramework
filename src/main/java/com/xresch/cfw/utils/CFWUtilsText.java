package com.xresch.cfw.utils;

import java.util.ArrayList;
import java.util.Comparator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWUtilsText {
	
	private static final CFWUtilsText INSTANCE = new CFWUtilsText();
	private static AlphanumericComparator alphanumComparator = INSTANCE.new AlphanumericComparator();
	
	/*******************************************************************
	 * 
	 *******************************************************************/
	public static String capitalize(String string) {
		if(string == null) return null;
		return string.substring(0,1).toUpperCase() + string.substring(1).toLowerCase();
	}
		
	
	/*******************************************************************
	 * 
	 *******************************************************************/
	public static String fieldNameToLabel(String fieldName){
		
		String[] splitted = fieldName.split("[-_]");
		
		StringBuilder result = new StringBuilder();
		for(int i = 0; i < splitted.length; i++) {
			result.append(capitalize(splitted[i]));
			
			//only do if not last
			if(i+1 < splitted.length) {
				result.append(" ");
			}
		}
		
		return result.toString();
	}
	
	/*******************************************************************
	 * 
	 *******************************************************************/
	public static ArrayList<String> arrayToUppercase(ArrayList<String> list){
		ArrayList<String> lower = new ArrayList<String>();
		
		for(String entry : list ) {
			
			if(entry != null) {
				lower.add(entry.toUpperCase());
			}else {
				lower.add(null);
			}
			
		}
		
		return lower;
	}
	
	/*******************************************************************
	 * 
	 *******************************************************************/
	public static ArrayList<String> arrayToLowercase(ArrayList<String> list){
		ArrayList<String> lower = new ArrayList<String>();
		
		for(String entry : list ) {
			
			if(entry != null) {
				lower.add(entry.toLowerCase());
			}else {
				lower.add(null);
			}
			
		}
		
		return lower;
	}
	
	/*******************************************************************
	 * 
	 *******************************************************************/
	public static boolean isDigit(char ch)
    {
        return ((ch >= 48) && (ch <= 57));
    }
	
	/*******************************************************************
	 * Splits a text by a separator while being aware of quotes.
	 * Separators inside of quotes will not be used to split
	 *  
	 * The method will act as follows:
	 * <ul>
	 * 		<li><b>Separators:&nbsp;</b> Can be one character or a sequence of characters. Does not support regex.</li>
	 * 		<li><b>Double Quotes:&nbsp;</b> Can be used to wrap values to allow it to contain separators. Quotes inside can be escaped using '\';  </li>
	 * 		<li><b>Blanks:&nbsp;</b>Blanks at beginning and end of a values are ignored, except if a value is quoted.</li>
	 * 		<li><b>Skipped Values:&nbsp;</b> If two separators follow each other it will be considered like one separator. </li>
	 * </ul>
	 * @param separator the separator, can be multiple characters, does not support regex
	 * @param textToSplit the text to split
	 * @param isDoubleQuotesAware true if should be aware, false otherwise 
	 * @param isSingleQuotesAware true if should be aware, false otherwise 
	 * @param isBackticksAware true if should be aware, false otherwise 
	 * @param escapedSeparators true if separators can be escaped with backslashes
	 * @param escapedSeparators TODO
	 *******************************************************************/
	public static ArrayList<String> splitQuotesAware(String separator, String textToSplit
			, boolean isDoubleQuotesAware
			, boolean isSingleQuotesAware
			, boolean isBackticksAware
			, boolean escapedSeparators
			){
		
		//-----------------------------
		// Variables
		char previous = '½'; 	// the last character, initialized with random character, only used to check for escaping with '\'
		char current;			// the current character
		char separatorFirstChar = separator.charAt(0); // performance improvement, check first char instead of doing a substring every time
		int cursor = 0;			// position in parsing
		int startPos = -1; 		// used as starting position of a value

		ArrayList<String> result = new ArrayList<>();
		
		int LENGTH = textToSplit.length();
		
		//-----------------------------
		// Check starts with separator
		if(textToSplit.startsWith(separator)) {
			cursor += separator.length();
		}
		
		//-----------------------------
		// Parse String
		outer:
		while( cursor < LENGTH ) {
			
			if(cursor > 0) { previous = textToSplit.charAt(cursor-1); }
			current = textToSplit.charAt(cursor);
				
			//----------------------------
			// Grab Separated Text
			startPos = cursor;
			while(cursor < LENGTH ) {
				current = textToSplit.charAt(cursor);
				
				//----------------------------
				// Handle Quoted Text
				if((isDoubleQuotesAware && current == '"') 
				|| (isSingleQuotesAware && current == '\'')
				|| (isBackticksAware && current == '`')
				)  { 

					char quote = current;
					
					inner:
					while(cursor < LENGTH-1 ) {
						previous = current;
						current = textToSplit.charAt(++cursor);
		
						if(current == quote 
						&& previous != '\\'
						) {
							cursor++;
							break inner;
							
						}
					}
				} 
				
				//----------------------------
				// Check is Separator
				if(cursor < LENGTH ) {
					if(cursor > 0) { previous = textToSplit.charAt(cursor-1); }
					current = textToSplit.charAt(cursor);
	
					if(current == separatorFirstChar
					&& (!escapedSeparators || previous != '\\')
					&& textToSplit.substring(cursor).startsWith(separator)) {
	
						result.add( textToSplit.substring(startPos, cursor) );
						//cursor++;
						break;
					}
					
					cursor++;
				}
			}
			
			
			//----------------------------
			// Handle Separator
			if(cursor < LENGTH ) {
				if(cursor > 0) { previous = textToSplit.charAt(cursor-1); }
				current = textToSplit.charAt(cursor);
				if(current == separatorFirstChar
				&& (!escapedSeparators || previous != '\\')		
				&& textToSplit.substring(cursor).startsWith(separator) ) {
					cursor += separator.length();
					continue; 
				}
			}
						
			//----------------------------
			// Grab Last
			if(cursor >= LENGTH ) {
				result.add( textToSplit.substring(startPos, cursor) );
				break;
			}
		}
	
		return result;
	}
	
	/*******************************************************************
	 * This Method sorts with the following in mind:
	 *   - Numbers in strings are in order
	 *   - Lower and uppercase letters of same char are next to each other 
	 *    
	 *******************************************************************/
	public static int compareStringsAlphanum(String val1, String val2) {

		if (val1 == null) 	{ return 1; }
		if (val2 == null) 	{ return -1; }
		if (val1 == val2)		{ return 0;  }
		
		int len1 = val1.length();
		int len2 = val2.length();

		int lim = Math.min(len1, len2);
        
		//------------------------------
        // Iterate until 
		for (int i = 0; i < lim; i++) {
            char c1 = val1.charAt(i);
            char c2 = val2.charAt(i);
            
            //------------------------------
            // Compare As Digits if both digits
            if(isDigit(c1) && isDigit(c2)) {
            	
        		long digits1 = 0;
        		long digits2 = 0;
        		
        		int k1;
        		for(k1 = i+1; k1 < len1 && isDigit(c1); k1++ ) {
        			digits1 += (digits1*10) + Character.digit(c1, 10);
            		c1 = val1.charAt(k1);
        		}
        		int k2;
        		for(k2 = i+1; k2 < len2 && isDigit(c2); k2++ ) {
        			digits2 += (digits2*10) + Character.digit(c2, 10);
        			c2 = val2.charAt(k2);
        		}
        		
        		if(digits1 != digits2) {
        			if(digits1 < digits2) {
        				return -1;
        			}else {
        				return 1;
        			}
            		
        		}else {
        			// set i to lower digit end
        			i = (k1 < k2) ? k1 : k2;
        		}
        		
        	}
            
            //------------------------------
            // Compare As Characters
            if (c1 != c2) {
            	
            	//------------------------------
                // Keep same chars lower/uppercase together
            	if(Character.toUpperCase(c1) == Character.toUpperCase(c2)) {
            		if(Character.isLowerCase(c1)) {
            			return 1;
            		}else {
            			return -1;
            		}
            	}
            	
            	return Character.toUpperCase(c1) - Character.toUpperCase(c2);
            }
        }

        return len1 - len2;
	}
	
	/*******************************************************************
	 * 
	 *******************************************************************/
	public static String stacktraceToString(Throwable throwable) {
		
		StringBuilder buffer = new StringBuilder();
		buffer.append(throwable.getClass());
		buffer.append(": ");
		buffer.append(throwable.getMessage());
		
		for(StackTraceElement element : throwable.getStackTrace()){
			buffer.append(" <br/>  at ");
			buffer.append(element);
		}
		
		return buffer.toString();
	}
	
	/*******************************************************************
	 * 
	 *******************************************************************/
	public static AlphanumericComparator getAlphanumericComparator() {
		return alphanumComparator;
	}
	
	/*******************************************************************
	 * 
	 *******************************************************************/
	class AlphanumericComparator implements Comparator<String> {
        
		public int compare(String val1, String val2) {
			return compareStringsAlphanum(val1, val2);
        }

    }

}
