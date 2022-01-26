package com.xresch.cfw.features.query;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.xresch.cfw.features.core.AutocompleteItem;
import com.xresch.cfw.features.query.parse.CFWQueryToken;
import com.xresch.cfw.features.query.parse.CFWQueryTokenizer;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 **************************************************************************************************************/
public class CFWQueryAutocompleteHelper {
	
	private HttpServletRequest request;
	private	String searchValue;
	private	int cursorPosition;
	
	private	String currentQuery = "";
	private String commandPart = "";
	private String signBeforeCursor = "";
	
	ArrayList<CFWQueryToken> commandTokens;
	
	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQueryAutocompleteHelper(HttpServletRequest request, String searchValue, int cursorPosition) {
		this.request        = request;
		this.searchValue    = Strings.nullToEmpty(searchValue);
		this.cursorPosition = cursorPosition;
		
		//------------------------------------------
		//Extract Current Query from Full Query String
		currentQuery = Strings.nullToEmpty(extractCurrentQueryPart(searchValue, cursorPosition));
		System.out.println("Query Part: '"+currentQuery+"'");
		
		//------------------------------------------
		//Extract Current Command
		String commandPart = Strings.nullToEmpty(extractCommandPart(currentQuery, cursorPosition));
		System.out.println("Command Part: '"+commandPart+"'");
		
		if(Strings.isNullOrEmpty(commandPart)) {
			return ;
		}
		if(cursorPosition > 0) {
			signBeforeCursor = searchValue.substring(cursorPosition-1, cursorPosition);
		}
		
		commandTokens = new CFWQueryTokenizer(commandPart, false)
				.keywords("AND", "OR", "NOT")
				.getAllTokens();
		
	}

	/******************************************************************
	 *
	 ******************************************************************/
	private String extractCurrentQueryPart(String searchValue, int cursorPosition) {
		
		if(Strings.isNullOrEmpty(searchValue) ) {
			return "";
		}
		
		int queryStart = searchValue.lastIndexOf(";", cursorPosition);
		int queryEnd = searchValue.indexOf(";", cursorPosition);
				
		if(queryEnd == -1) { queryEnd = searchValue.length();};
		
		return searchValue.substring(queryStart+1, queryEnd);
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private String extractCommandPart(String currentQuery, int cursorPosition) {
		
		if(Strings.isNullOrEmpty(currentQuery) ) {
			return "";
		}
		
		int commandStart = currentQuery.lastIndexOf("|", cursorPosition);
		int commandEnd = currentQuery.indexOf("|", cursorPosition);
				
		if(commandEnd == -1) { commandEnd = currentQuery.length();};
		
		return currentQuery.substring(commandStart+1, commandEnd);
	}
	

	
	/******************************************************************
	 *
	 ******************************************************************/
	public boolean isEmptyQuery() {
		return currentQuery.trim().isEmpty();
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	public boolean isEmptyCommand() {
		return commandPart.trim().isEmpty();
	}
	
	/******************************************************************
	 * Inserts the given string at the cursor position and returns the
	 * complete string for autocomplete replace.
	 ******************************************************************/
	public AutocompleteItem createAutocompleteItem(Object replaceThis, Object replacement, Object label) {
		
		return this.createAutocompleteItem(replaceThis, replacement, label, null);
	}
	
	/******************************************************************
	 * Inserts the given string at the cursor position and returns the
	 * complete string for autocomplete replace.
	 ******************************************************************/
	public AutocompleteItem createAutocompleteItem( Object replaceThis, Object replacement, Object label, Object description) {
		
		return new AutocompleteItem(replacement, label, description)
						.setMethodReplaceBeforeCursor(replaceThis.toString());
	}
	

	/******************************************************************
	 *
	 ******************************************************************/
	public HttpServletRequest getRequest() {
		return request;
	}

	/******************************************************************
	 *
	 ******************************************************************/
	public String getFullSearchValue() {
		return searchValue;
	}

	/******************************************************************
	 *
	 ******************************************************************/
	public int getCursorPosition() {
		return cursorPosition;
	}
	
	

}
