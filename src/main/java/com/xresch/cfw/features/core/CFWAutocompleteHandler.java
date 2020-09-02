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
	private CFWField parent = null;
	
	public CFWAutocompleteHandler() {
		
	}
	
	public CFWAutocompleteHandler(int maxResults) {
		this.maxResults = maxResults;
	}
	
	/*******************************************************************************
	 * Return a hashmap with value / label combinations
	 * @param request 
	 * @return JSON string
	 *******************************************************************************/
	public abstract AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue);

	public int getMaxResults() {
		return maxResults;
	}

	public CFWAutocompleteHandler setMaxResults(int maxResults) {
		this.maxResults = maxResults;
		return this;
	}

	public CFWField getParent() {
		return parent;
	}

	public void setParent(CFWField parent) {
		this.parent = parent;
	}

	
	
	
}
