package com.xresch.cfw.features.query;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.query.FeatureQuery.CFWQueryComponentType;
import com.xresch.cfw.features.query.parse.CFWQueryToken;
import com.xresch.cfw.features.query.parse.CFWQueryToken.CFWQueryTokenType;


/**************************************************************************************************************
 * Class for Autocompleting Query inputs.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 **************************************************************************************************************/
final class CFWQueryAutocompleteHandler extends CFWAutocompleteHandler {
	
	// Cached for Autocomplete
	private static TreeMap<String, CFWQueryCommand> commandMapCached;

	/********************************************************
	 * 
	 ********************************************************/
	private static TreeMap<String, CFWQueryCommand> getCachedCommands() {
		if(commandMapCached == null) {
			commandMapCached = CFW.Registry.Query.createCommandInstances(new CFWQuery());
		}
		return commandMapCached;
	}
	
	/********************************************************
	 * 
	 ********************************************************/
	@Override
	public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
		
		AutocompleteResult result = new AutocompleteResult();
		
		CFWQueryAutocompleteHelper helper = new CFWQueryAutocompleteHelper(request, searchValue, cursorPosition);
		
		TreeMap<String, CFWQueryCommand> commandMap = getCachedCommands();
		//----------------------------------------
		// Handle empty search
		if(helper.isEmptyQuery()) {
			AutocompleteList list = new AutocompleteList();
			
			CFWQueryCommand sourceCommand = getCachedCommands().get("source");
			
			list.addItem(
					helper.createAutocompleteItem(
						""
					  , sourceCommand.getUniqueName()
					  , sourceCommand.getUniqueName()
					  , sourceCommand.descriptionShort()
					)
			 );
			
			result.addList(list);
			return result;
		}
		
		
		//----------------------------------------
		// Handle empty command
		if( helper.isEmptyCommand() ) {
			AutocompleteList list = new AutocompleteList();

			int i = 0;
			for(Entry<String, CFWQueryCommand> entry : commandMap.entrySet()) {
				
				String commandName = entry.getKey();
				CFWQueryCommand command = entry.getValue();
				list.addItem(
						helper.createAutocompleteItem(
								""
							  , commandName
							  , commandName
							  , command.descriptionShort()
							)
					);
					
				i++;
				if(i == 10) { break; }
			}
			
			result.addList(list);
			return result;
			
		}
		
		//----------------------------------------
		// Handle Function Autocomplete "f:"
		if( helper.isBeforeCursor("f:") ) {
			this.addListOfFunctions(result, helper, null);
			return result;
		}
		
		//----------------------------------------
		// Handle Function "f:xxx"
		CFWQueryToken checkIsColon = helper.getTokenBeforeCursor(-1);
		CFWQueryToken checkIsCharF = helper.getTokenBeforeCursor(-2);

		if( checkIsColon != null && checkIsColon.type() == CFWQueryTokenType.SIGN_COLON
		&& checkIsCharF != null && checkIsCharF.value().equalsIgnoreCase("f") ) {
			CFWQueryToken partialFunctionNameToken = helper.getTokenBeforeCursor(0);

			this.addListOfFunctions(result, helper, partialFunctionNameToken.value());
			return result;
		}
		
		//----------------------------------------
		// Handle Function "xxx("
		CFWQueryToken checkIsBrace = helper.getTokenBeforeCursor(0);

		if( checkIsBrace != null 
		&& checkIsBrace.type() == CFWQueryTokenType.SIGN_BRACE_ROUND_OPEN) {
			CFWQueryToken partialFunctionNameToken = helper.getTokenBeforeCursor(-1);
			
			if(partialFunctionNameToken != null) {
				String functionName = partialFunctionNameToken.value();
				if(CFW.Registry.Query.functionExists(functionName)) {
					CFWQueryFunction function = CFW.Registry.Query.createFunctionInstance(new CFWQueryContext(), functionName);
					result.setHTMLDescription(
							CFWQueryAutocompleteHelper.createManualButton(CFWQueryComponentType.FUNCTION, function.uniqueName())
							+ "<br><b>Description:&nbsp</b>"+function.descriptionShort()
							+ "<br><b>Syntax:&nbsp</b>"+CFW.Security.escapeHTMLEntities(function.descriptionSyntax())
							+ function.descriptionSyntaxDetailsHTML()
						);
					return result;
				}else {
					// do nothing, not a function, so try other autocompletes below
				}
			}

		}
		
		//----------------------------------------
		// Handle Command with or without params
		if( helper.getCommandTokenCount() >= 1 ) {
			CFWQueryToken commandNameToken = helper.getToken(0);
			
			if(commandNameToken.type() == CFWQueryTokenType.LITERAL_STRING) {
				String partialOrFullCommandName = commandNameToken.value();
				
				if(commandMap.containsKey(partialOrFullCommandName)) {
					//--------------------------------
					// Full Command Name already entered
					CFWQueryCommand command = commandMap.get(partialOrFullCommandName);
					result.setHTMLDescription(
						CFWQueryAutocompleteHelper.createManualButton(CFWQueryComponentType.COMMAND, command.getUniqueName())
						+ "<br><b>Description:&nbsp</b>"+command.descriptionShort()
						+ "<br><b>Syntax:&nbsp</b>"+CFW.Security.escapeHTMLEntities(command.descriptionSyntax())
						+ command.descriptionSyntaxDetailsHTML()
					);
					
					command.autocomplete(result, helper);
					
				}else {
					//--------------------------------
					// Partial Command name Entered
					AutocompleteList list = createListOfCommands(helper, commandMap, partialOrFullCommandName);
					
					result.addList(list);
				}	
				
			}else {
				result.setHTMLDescription("Command has to start with a literal string:"+commandNameToken.value());
				return result;
			}
			
		}
		
		return result;
	}
	
	/********************************************************
	 * 
	 ********************************************************/
	private AutocompleteList createListOfCommands(CFWQueryAutocompleteHelper helper,
			TreeMap<String, CFWQueryCommand> commandMap, String partialOrFullCommandName) {
		AutocompleteList list = new AutocompleteList();
		int i = 0;
		for (String currentName : commandMap.keySet() ) {
			
			if(currentName.contains(partialOrFullCommandName)) {
				
				CFWQueryCommand command = commandMap.get(currentName);
				
				list.addItem(
					helper.createAutocompleteItem(
						partialOrFullCommandName
					  , currentName
					  , currentName
					  , command.descriptionShort()
					)
				);
				
				i++;
				if(i == 10) { break; }
			}
		}
		return list;
	}
	
	/********************************************************
	 * 
	 ********************************************************/
	private void addListOfFunctions(AutocompleteResult result,
				CFWQueryAutocompleteHelper helper,
				String filter) {
		
		boolean isFullList = (filter == null); 
		AutocompleteList list = new AutocompleteList();
		result.addList(list);
		int i = 0;
		TreeMap<String, Class<? extends CFWQueryFunction>> functionMap = CFW.Registry.Query.getFunctionList();

		for (String currentName : functionMap.keySet() ) {
			
			if(isFullList || currentName.contains(filter)) {
				
				CFWQueryFunction command = 
						CFW.Registry.Query.createFunctionInstance(new CFWQueryContext(), currentName);
				
				String replaceThis = (isFullList) ? "f:" : "f:"+filter;
				
				list.addItem(
						helper.createAutocompleteItem(
								replaceThis
								, currentName+"("
								, currentName+"()"
								, command.descriptionShort()
								)
						);
				
				i++;
				
				if((i % 10) == 0) {
					if(!isFullList) { break; }
					list = new AutocompleteList();
					result.addList(list);
				}
				if(i == 50) { break; }
			}
		}

	}
	
}