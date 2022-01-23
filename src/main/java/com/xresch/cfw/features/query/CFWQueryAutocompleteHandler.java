package com.xresch.cfw.features.query;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;


/**************************************************************************************************************
 * Class for Autocompleting Query inputs.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 **************************************************************************************************************/
final class CFWQueryAutocompleteHandler extends CFWAutocompleteHandler {
	
	// Cached for Autocomplete
	static ArrayList<CFWQueryCommand> commandList = CFW.Registry.Query.createCommandInstances(new CFWQuery());
	static CFWQueryCommand sourceCommand = CFW.Registry.Query.createCommandInstance(new CFWQuery(), "source");

	@Override
	public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
		
		AutocompleteResult result = new AutocompleteResult();
		
		CFWQueryAutocompleteHelper helper = new CFWQueryAutocompleteHelper(request, searchValue, cursorPosition);
		
		//----------------------------------------
		// Handle empty search
		if(helper.isEmptyQuery()) {
			AutocompleteList list = new AutocompleteList();
			
			list.addItem(
					helper.toStringInsert(sourceCommand.getUniqueName())
				  , sourceCommand.getUniqueName()
				  , sourceCommand.descriptionShort()
			 );
			
			result.addList(list);
			return result;
		}
		
		
		
		//----------------------------------------
		// Handle empty command
		if(helper.isEmptyCommand()) {
			AutocompleteList list = new AutocompleteList();

			int i = 0;
			for(CFWQueryCommand command : commandList) {
				list.addItem(
						helper.toStringInsert(command.getUniqueName())
						, command.getUniqueName()
						, command.descriptionShort()
					);
				i++;
				if(i == 10) { break; }
			}
			
			result.addList(list);
			return result;
			
		}
		
		
		return result;
	}
}