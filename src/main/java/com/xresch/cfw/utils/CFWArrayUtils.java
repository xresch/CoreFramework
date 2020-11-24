package com.xresch.cfw.utils;

import java.util.Arrays;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWArrayUtils {
	
	
	public static boolean contains(Object[] array, Object object) {
		if(array != null) {
			return Arrays.asList(array).contains(object);
		}
		return false;
	}
	
	public static Object[] add(Object[] array, Object object) {
		Object[] copy = new Object[array.length + 1];
		System.arraycopy(array, 0, copy, 0, array.length);
		copy[copy.length-1] = object;
		return copy;
	}
	
	public static String[] add(String[] array, String string) {
		String[] copy = new String[array.length + 1];
		System.arraycopy(array, 0, copy, 0, array.length);
		copy[copy.length-1] = string;
		return copy;
	}
	
	/*********************************************************************************
	 * Converts an object array into a string array using the toString()-Method of the
	 * objects.
	 * 
	 * @param objectArray
	 * @return
	 *********************************************************************************/
	public static String[] objectToStringArray(Object[] objectArray) {
		String[] resultArray = new String[objectArray.length];
		for(int i=0; i < objectArray.length; i++) {
			resultArray[i] = objectArray[i].toString();
		}
		// Not working with none String values like Enums
		//return Arrays.copyOf(objectArray, objectArray.length, String[].class);
		return resultArray;
	}
		
	public static String[] merge(String[] firstArray, String[] secondArray) {
		
		int fal = firstArray.length;        
		int sal = secondArray.length;   
		String[] result = new String[fal + sal];  
		System.arraycopy(firstArray, 0, result, 0, fal);  
		System.arraycopy(secondArray, 0, result, fal, sal);   
		
		return result;
	}
	
	

}
