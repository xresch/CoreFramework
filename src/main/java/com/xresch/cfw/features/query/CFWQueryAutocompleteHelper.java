package com.xresch.cfw.features.query;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.xresch.cfw.features.core.AutocompleteItem;
import com.xresch.cfw.features.query.parse.CFWQueryToken;
import com.xresch.cfw.features.query.parse.CFWQueryToken.CFWQueryTokenType;
import com.xresch.cfw.features.query.parse.CFWQueryTokenizer;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 **************************************************************************************************************/
public class CFWQueryAutocompleteHelper {
	
	private HttpServletRequest request;
	private	String searchValue;
	private	int cursorPosition;
	
	private	List<CFWQueryToken> currentQuery;
	//private List<CFWQueryToken> commandPart;
	private String signBeforeCursor = "";
	
	private List<CFWQueryToken> commandTokens;
	
	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQueryAutocompleteHelper(HttpServletRequest request, String fullQueryString, int cursorPosition) {
		this.request        = request;
		this.searchValue    = Strings.nullToEmpty(fullQueryString);
		this.cursorPosition = cursorPosition;
		
		ArrayList<CFWQueryToken> tokens = new CFWQueryTokenizer(fullQueryString, false, true)
				.keywords("AND", "OR", "NOT")
				.getAllTokens();
		
		//------------------------------------------
		//Extract Current Query from Full Query String
		currentQuery = extractCurrentQueryPart(tokens, cursorPosition);
		
		//------------------------------------------
		//Extract Current Command
		commandTokens = extractCommandPart(currentQuery, cursorPosition);
		
		if(commandTokens.size() == 0) {
			return ;
		}
		
		if(cursorPosition > 0) {
			signBeforeCursor = fullQueryString.substring(cursorPosition-1, cursorPosition);
		}
		
	}

	/******************************************************************
	 *
	 ******************************************************************/
	private List<CFWQueryToken> extractCurrentQueryPart(List<CFWQueryToken> allTokens, int cursorPosition) {
		
		int queryStart = 0;
		int queryEnd = 0;
		for(int i = 0; i < allTokens.size(); i++) {
			CFWQueryToken current = allTokens.get(i);
			boolean isQuerySeparator = (current.type() == CFWQueryTokenType.SIGN_SEMICOLON);
			
			if(isQuerySeparator
			&& current.position() < cursorPosition){
				queryStart = i+1;
			}
			
			queryEnd = i+1;
			if(isQuerySeparator
			&& current.position() >= cursorPosition){
				break;
			}
		}
		
		return allTokens.subList(queryStart, queryEnd);
	
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private List<CFWQueryToken> extractCommandPart(List<CFWQueryToken> commandTokens, int cursorPosition) {
		
		int startIndex = 0;
		int endIndex = 0;
		for(int i = 0; i < commandTokens.size(); i++) {
			CFWQueryToken current = commandTokens.get(i);
			boolean isCommandSeparator = (current.type() == CFWQueryTokenType.OPERATOR_OR);
			
			if(isCommandSeparator
			&& current.position() < cursorPosition){
				startIndex = i+1;
			}
			
			endIndex = i+1;
			if(isCommandSeparator
			&& current.position() >= cursorPosition){
				break;
			}
		}
		
		return commandTokens.subList(startIndex, endIndex);
	
	}

	
	/******************************************************************
	 *
	 ******************************************************************/
	public boolean isEmptyQuery() {
		return currentQuery.size() == 0;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	public boolean isEmptyCommand() {
		return commandTokens.size() == 0;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	public int getTokenCount() {
		return commandTokens.size();
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQueryToken getToken(int index) {
		return commandTokens.get(index);
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
