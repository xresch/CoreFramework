package com.xresch.cfw.features.core;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw.datahandling.CFWField;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public abstract class CFWAutocompleteHandler {
	
	private int maxResults = 10;
	private int minChars = 3;
	private CFWField parent = null;
	
	public CFWAutocompleteHandler() {
		
	}
	
	public CFWAutocompleteHandler(int maxResults) {
		this.maxResults = maxResults;
	}
	
	public CFWAutocompleteHandler(int maxResults, int minChars) {
		this.minChars = minChars;
	}
	
	/*******************************************************************************
	 * Return a hashmap with value / label combinations
	 * @param request 
	 * @param cursorPosition TODO
	 * @return JSON string
	 *******************************************************************************/
	public abstract AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition);

	public int getMaxResults() {
		return maxResults;
	}

	public CFWAutocompleteHandler setMaxResults(int maxResults) {
		this.maxResults = maxResults;
		return this;
	}
	
	public int getMinChars() {
		return minChars;
	}

	public CFWAutocompleteHandler setMinChars(int minChars) {
		this.minChars = minChars;
		return this;
	}

	public CFWField getParent() {
		return parent;
	}

	public void setParent(CFWField parent) {
		this.parent = parent;
	}

	
	
	
}
