package com.xresch.cfw.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xresch.cfw.datahandling.CFWField;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWUtilsText {
	
	private static final CFWUtilsText INSTANCE = new CFWUtilsText();
	private static AlphanumericComparator alphanumComparator = INSTANCE.new AlphanumericComparator();
	
	private static SimpleCache<String, Pattern> regexPatternCache = new SimpleCache<>(10000);
	
	public enum CheckType {
		  CONTAINS
		, DOES_NOT_CONTAIN
		, STARTS_WITH
		, ENDS_WITH
		, EQUALS
		, NOT_EQUALS
		, MATCH_REGEX
		, DO_NOT_MATCH_REGEX
	}
	
	private static LinkedHashMap<String, String> checkTypeOptions = new LinkedHashMap<>();
	static {
		checkTypeOptions.put(CheckType.CONTAINS.toString(), "Contains");
		checkTypeOptions.put(CheckType.DOES_NOT_CONTAIN.toString(), "Does Not Contain");
		checkTypeOptions.put(CheckType.STARTS_WITH.toString(), "Starts With");
		checkTypeOptions.put(CheckType.ENDS_WITH.toString(), "Ends With");
		checkTypeOptions.put(CheckType.EQUALS.toString(), "Equals");
		checkTypeOptions.put(CheckType.NOT_EQUALS.toString(), "Not Equals");
		checkTypeOptions.put(CheckType.MATCH_REGEX.toString(), "Match Regex");
		checkTypeOptions.put(CheckType.DO_NOT_MATCH_REGEX.toString(), "Does Not Match Regex");
	}
	
	/*******************************************************************
	 * 
	 *******************************************************************/
	public static CFWField<String> getCheckTypeOptionField(String fieldname, String label, String description) {
		LinkedHashMap<String, String> clone = new LinkedHashMap<>();
		clone.putAll(checkTypeOptions);
		
		return CFWField.newString(CFWField.FormFieldType.SELECT, fieldname)
				.setLabel(label)
				.setDescription(description)
				.setOptions(clone);
	}
	
	/*******************************************************************
	 * Returns true if the condition matches.
	 * 
	 *******************************************************************/
	public static boolean checkTextForContent(String checkType, String text, String checkFor) {
		return checkTextForContent(CheckType.valueOf(checkType), text, checkFor);
	}
	
	/*******************************************************************
	 * Returns true if the condition matches.
	 * 
	 *******************************************************************/
	public static boolean checkTextForContent(CheckType checkType, String text, String checkFor) {
	
		switch (checkType) {
		
			case STARTS_WITH:		return text.startsWith(checkFor);
			case ENDS_WITH:			return text.endsWith(checkFor);	
			
			case CONTAINS:			return text.contains(checkFor);
			case DOES_NOT_CONTAIN:	return ! text.contains(checkFor);	
			
			case EQUALS:			return text.equals(checkFor);
			case NOT_EQUALS:		return ! text.equals(checkFor);
			
			case MATCH_REGEX:		return getRegexMatcherCached(checkFor, text).find();
			case DO_NOT_MATCH_REGEX: return ! getRegexMatcherCached(checkFor, text).find();

		}
		
		return false;
	}
	
	/*******************************************************************
	 * Will return a regex matcher for a pattern that applies the flags
	 * Pattern.MULTILINE and Pattern.DOTALL.
	 *******************************************************************/
	private static Matcher getRegexMatcherCached(String regex, String textToMatch) {
				
		return getRegexPatternCached(regex, textToMatch).matcher(textToMatch);
	}
	
	/*******************************************************************
	 * Will return a regex pattern that also applies the flags
	 * Pattern.MULTILINE and Pattern.DOTALL.
	 * 
	 *******************************************************************/
	private static Pattern getRegexPatternCached(String regex, String textToMatch) {
		
		if(!regexPatternCache.containsKey(regex)) {
			Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL);
			regexPatternCache.put(regex, pattern);
		}
		
		return regexPatternCache.get(regex);
	}
	
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
	 * Converts a size of bytes into a bytes String with a suffix like
	 * KB, MB, GB etc... that can be read by a human being.
	 * 
	 * @param size the size in bytes
	 * @param decimals the number of decimals
	 * 
	 *******************************************************************/
	public static String toHumanReadableBytes(Long size, int decimals){

			if(size == null) { return "0 B"; }
			
		    String readable = null;

		    DecimalFormat dec = new DecimalFormat("0."+ "0".repeat(decimals) );

		    if ( 		size >= 1_099_511_627_776L ){	readable = dec.format( size / 1_099_511_627_776.0 ).concat(" TB");
		    } else if ( size >= 1_073_741_824L ){ 		readable = dec.format( size / 1_073_741_824.0 ).concat(" GB");
		    } else if ( size >= 1_048_576L ){			readable = dec.format( size / 1_048_576.0 ).concat(" MB");
		    } else if ( size >= 1024L ){ 				readable = dec.format( size / 1024.0 ).concat(" KB");
		    } else { 									readable = size+" B";	}

		    return readable;

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
		char quoteChar = '½';
		
		ArrayList<String> result = new ArrayList<>();
		
		int LENGTH = textToSplit.length();
		
		//-----------------------------
		// Check starts with separator
		if(textToSplit.startsWith(separator)) {
			cursor += separator.length();
		}
		
		//-----------------------------
		// Parse String
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
				quoteChar = '½';
				if((isDoubleQuotesAware && current == '"') 
				|| (isSingleQuotesAware && current == '\'')
				|| (isBackticksAware && current == '`')
				)  { 

					quoteChar = current;

					while(cursor < LENGTH-1 ) {
						current = textToSplit.charAt(++cursor);
		
						if(current == quoteChar 
						&& !isCharacterEscaped(textToSplit, cursor)
						) {
							cursor++;
							break;
						}
					}
				} 
				
				//----------------------------
				// Check is Separator
				if(cursor < LENGTH ) {
					if(cursor > 0) { previous = textToSplit.charAt(cursor-1); }
					current = textToSplit.charAt(cursor);

					if(current == separatorFirstChar) {
						
						if( escapedSeparators && isCharacterEscaped(textToSplit, cursor) ) {
							textToSplit = textToSplit.substring(0, cursor-1) + textToSplit.substring(cursor);
							LENGTH = textToSplit.length();
						}
						
						if( textToSplit.substring(cursor).startsWith(separator) ) {
							//--------------------------
							// Create Part
							String splittedPart = textToSplit.substring(startPos, cursor);
							if(quoteChar != '½') {		
								splittedPart = trimCharFromText(splittedPart, quoteChar, true);
								// a mighty sacrifice of a double-escape to the Java God of Escapades
								splittedPart = splittedPart.replaceAll("\\\\"+quoteChar, ""+quoteChar);
							}
							
							//--------------------------
							// Add to Results
							result.add( splittedPart );
							//cursor++;
							break;
						}

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
				//--------------------------
				// Create Part
				String splittedPart = textToSplit.substring(startPos, cursor);
				if(quoteChar != '½') {		
					splittedPart = trimCharFromText(splittedPart, quoteChar, true);
					// a mighty sacrifice of a double-escape to the Java God of Escapades
					splittedPart = splittedPart.replaceAll("\\\\"+quoteChar, ""+quoteChar);
				}
				
				//--------------------------
				// Add to Results
				result.add( splittedPart );
				break;
			}
		}
	
		return result;
	}

	/*******************************************************************
	 * Removes the character from the start and end of the string.
	 * 
	 * @param text the text to be trimmed
	 * @param quoteChar the character to remove
	 * @param mustBeBoth true if it has to be at both start&end to be replaced
	 *    
	 *******************************************************************/
	private static String trimCharFromText(String text, char quoteChar, boolean mustBeBoth) {
		// Remove quotes at start/end
		if(mustBeBoth) {
			if(text.startsWith(""+quoteChar)
			&& text.endsWith(""+quoteChar)) { 
				text = text.substring(1);
				text = text.substring(0, text.length() -1);
			}
		}else {
			if(text.startsWith(""+quoteChar)) { text = text.substring(1); }
			if(text.endsWith(""+quoteChar)) { text = text.substring(0, text.length() -1); }
		}
			
		return text;
	}
	
	/*******************************************************************
	 * Checks if the current character at the cursor is escaped with \.
	 * This method also checks for escaped backslashes.
	 * 
	 * @param text the text to check
	 * @param cursor the position of the character
	 *    
	 *******************************************************************/
	public static boolean isCharacterEscaped(String text, int cursor) {

		int count = 0;
		while(cursor >= 0 ) {
		  cursor--;
		  if(text.charAt(cursor) == '\\' ) { count++; }
		  else{ break; }
		}
		
		return   (count % 2) != 0 ;
		
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
