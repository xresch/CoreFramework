package com.pengtoolbox.cfw.caching;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class StringCache {
	
	static String[] cachedStrings = new String[50];
	static int stringCounter = 0;

	/***********************************************************************
	 * Caches up to 15 files
	 * @param harContent
	 * @return the index of the cache use to retrieve the files with getHARFromCache()
	 ***********************************************************************/
	public static int cacheString(String fileContent) {
	
		synchronized(cachedStrings) {
			if( stringCounter == cachedStrings.length-1) {
				stringCounter = 0;
			}
			cachedStrings[stringCounter] = fileContent;
			stringCounter++;
			
			return stringCounter-1;
		}
	}

	/***********************************************************************
	 * Retrieve a cached file by it's index.
	 * @param index the index of the file to be retrieved
	 ***********************************************************************/
	public static String getCachedString(int index) {
		synchronized(cachedStrings) {
			if( index >= cachedStrings.length) {
				return null;
			}
			
			return cachedStrings[index];
		}
	}
	
}
