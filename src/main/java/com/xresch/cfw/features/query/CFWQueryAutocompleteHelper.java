package com.xresch.cfw.features.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteItem;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.FeatureQuery.CFWQueryComponentType;
import com.xresch.cfw.features.query.commands.CFWQueryCommandParamDefaults;
import com.xresch.cfw.features.query.parse.CFWQueryToken;
import com.xresch.cfw.features.query.parse.CFWQueryToken.CFWQueryTokenType;
import com.xresch.cfw.features.query.parse.CFWQueryTokenizer;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 **************************************************************************************************************/
public class CFWQueryAutocompleteHelper {
	
	private HttpServletRequest request;
	private	int cursorPosition;
	private	String fullQueryString;
	private	String queryBeforeCursor;
	private String signBeforeCursor = "";
	
	private	List<CFWQueryToken> currentQuery;
	//private List<CFWQueryToken> commandPart;
	
	private	ArrayList<CFWQueryToken> tokens;
	
	private List<CFWQueryToken> commandTokens;  // command at cursor position only
	private List<CFWQueryToken> commandTokensBeforeCursor; // command at cursor position only

	
	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQueryAutocompleteHelper(HttpServletRequest request, String fullQueryString, int cursorPosition) {
		this.request        = request;
		this.fullQueryString    = Strings.nullToEmpty(fullQueryString);
		this.cursorPosition = cursorPosition;
		this.queryBeforeCursor = Strings.nullToEmpty(fullQueryString.substring(0, cursorPosition));
		
		tokens = new CFWQueryTokenizer(fullQueryString, false, true)
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
		
		//------------------------------------------
		//Extract Current Command
		commandTokensBeforeCursor = new ArrayList<CFWQueryToken>();
		for(CFWQueryToken token : commandTokens) {
			if(token.position() < cursorPosition) {
				commandTokensBeforeCursor.add(token);
			}else {
				break;
			}
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
				endIndex--;
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
	public boolean isBeforeCursor(String value) {
		return queryBeforeCursor.trim().endsWith(value);
	}
	
	/******************************************************************
	 * Returns the amount of tokens the current command has.
	 * 
	 ******************************************************************/
	public int getCommandTokenCount() {
		return commandTokens.size();
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQueryToken getToken(int index) {
		return commandTokens.get(index);
	}
	
	/******************************************************************
	 * 
	 * @param offset 0 will give the token immediately before cursor.
	 *        1/-1 will give the second token before cursor etc...
	 * @return token or null if not available
	 ******************************************************************/
	public CFWQueryToken getTokenBeforeCursor(int offset) {
		int tokenCount = commandTokensBeforeCursor.size();
		if(commandTokensBeforeCursor.size() == 0) {
			return null;
		}
		
		int tokenIndex = tokenCount - 1 - Math.abs(offset);
		if(tokenIndex < 0) {
			return null;
		}
		
		return commandTokensBeforeCursor.get(tokenIndex);
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
	public String getFullQuery() {
		return fullQueryString;
	}

	/******************************************************************
	 *
	 ******************************************************************/
	public int getCursorPosition() {
		return cursorPosition;
	}
	
	/******************************************************************
	 * Returns true if the query contains the command paramdefaults or
	 * defaulparams.
	 ******************************************************************/
	public boolean hasParameters() {
		
		for(int i = 0; i < tokens.size(); i++) {
			
			CFWQueryToken token = tokens.get(i);

			if(token.type() == CFWQueryTokenType.OPERATOR_OR) {
				i++;
				if(i < tokens.size()) {
					
					String commandName = tokens.get(i).value().toLowerCase();
					
					if(commandName.equals(CFWQueryCommandParamDefaults.COMMAND_NAME.toLowerCase())
					 ||commandName.equals(CFWQueryCommandParamDefaults.COMMAND_NAME_ALIAS.toLowerCase())
					 ){
						return true;
					}
				}
			}
		}
		
		return false;
		
	}
	
	/********************************************************
	 * Creates s html string, representing a button opening
	 * the manual page on the query editor.
	 *  
	 ********************************************************/
	public static String createManualButton(CFWQueryComponentType type, String componentName) {
		return createManualButton(type, componentName, "Open Manual");
	}
	
	/********************************************************
	 * Creates s html string, representing a button opening
	 * the manual page on the query editor.
	 *  
	 ********************************************************/
	public static String createManualButton(CFWQueryComponentType type, String componentName, String buttonLabel) {
		return "<span class=\"badge badge-primary cursor-pointer mr-1\""
				+ " onclick=\"cfw_query_editor_getManualPage('"
						+ type
						+ "', '"+componentName+"' )\">"+buttonLabel+"</span>";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public void autocompleteContextSettingsForSource(String settingsType, AutocompleteResult result) {
		if( this.getCommandTokenCount() >= 2 ) {
			
			HashMap<Integer, Object> environmentMap = CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(settingsType);
			
			AutocompleteList list = new AutocompleteList();
			result.addList(list);
			int i = 0;
			for (Object envID : environmentMap.keySet() ) {

				Object envName = environmentMap.get(envID);
				
				JsonObject envJson = new JsonObject();
				envJson.addProperty("id", Integer.parseInt(envID.toString()));
				envJson.addProperty("name", envName.toString());
				String envJsonString = "environment="+CFW.JSON.toJSON(envJson)+" ";
				
				list.addItem(
					this.createAutocompleteItem(
						""
					  , envJsonString
					  , "Environment: "+envName
					  , envJsonString
					)
				);
				
				i++;
				
				if((i % 10) == 0) {
					list = new AutocompleteList();
					result.addList(list);
				}
				if(i == 50) { break; }
			}
		}
	}
	
	

}
