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
	 * This Method sorts with the following in ming:
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
