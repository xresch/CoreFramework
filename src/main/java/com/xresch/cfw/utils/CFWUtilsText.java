package com.xresch.cfw.utils;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWUtilsText {
	
	
	public static String capitalize(String string) {
		if(string == null) return null;
		return string.substring(0,1).toUpperCase() + string.substring(1).toLowerCase();
	}
	
	
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

}
