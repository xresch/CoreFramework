package com.xresch.cfw.tests.assets;

import org.opentest4j.AssertionFailedError;

import com.beust.jcommander.Strings;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023
 * @license MIT-License
 **************************************************************************************************************/
public class CFWTestUtils {
	
			
	/********************************************************************************************
	 * 
	 ********************************************************************************************/
	public static void assertIsBetween(Number lowerInclusive, Number upperInclusive, Number checkThis) { 	
		
		if (checkThis.floatValue() >= lowerInclusive.floatValue() 
			&& checkThis.floatValue() <= upperInclusive.floatValue()) {
			return;
		}else {
			throw new AssertionFailedError("Number not in range.", lowerInclusive + " to " + upperInclusive , checkThis);
		}
	}
	
	/********************************************************************************************
	 * 
	 ********************************************************************************************/
	public static void assertIsEither(String checkThis, String... values) { 	
		
		for(String value : values) {
			if(value.equals(checkThis)) {
				return;
			}
		}
		
		throw new AssertionFailedError("Number not either of the expected.", Strings.join(",", values) , checkThis);
	}
		


}
