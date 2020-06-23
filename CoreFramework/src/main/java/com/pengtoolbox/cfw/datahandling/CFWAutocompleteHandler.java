package com.pengtoolbox.cfw.datahandling;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
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
	public abstract LinkedHashMap<Object, Object> getAutocompleteData(HttpServletRequest request, String searchValue);

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
