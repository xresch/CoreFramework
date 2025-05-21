package com.xresch.cfw.features.parameter;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.core.AutocompleteItem;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.parameter.CFWParameter.CFWParameterScope;

class ParameterAutocompleteWrapper extends CFWAutocompleteHandler {

	private CFWField wrappedField; 
	private CFWParameterScope scope;
	private String itemID; // either dashboard or query ID
	private String widgetType;
	private CFWAutocompleteHandler wrappedHandler;
	
	public ParameterAutocompleteWrapper(CFWField settingsFieldToWrap, CFWParameterScope scope, String itemID, String widgetType) {
		this.itemID = itemID;
		this.scope = scope;
		this.widgetType = widgetType;
		
		this.wrappedField = settingsFieldToWrap;		
		this.wrappedHandler = wrappedField.getAutocompleteHandler();
		
		//---------------------------
		// Replace old handler 
		wrappedField.setAutocompleteHandler(this);
	}
	@Override
	public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
				
		//---------------------------
		// Get Regular Result
		AutocompleteResult wrappedResult = null;
		if(wrappedHandler != null) {
			wrappedResult = wrappedHandler.getAutocompleteData(request, searchValue, cursorPosition);
		};
		
		//---------------------------
		// Get Param Result
		String fieldName = wrappedField.getName();

		ArrayList<CFWObject> availableParams = null;
		switch(scope) {
			case dashboard:	availableParams = CFW.DB.Parameters.getAvailableParamsForDashboard(itemID, widgetType, fieldName, true); break;
			case query:		availableParams = CFW.DB.Parameters.getAvailableParamsForQuery(itemID, fieldName);		break;
			default:		CFW.Messages.addErrorMessage("Unsupported scope: "+scope.toString());
							return wrappedResult;
		}
		
		//---------------------------
		// Prepare params Autocomplete
		if(availableParams == null || availableParams.isEmpty()) {
			return wrappedResult;
		}
		
		AutocompleteList paramList = new AutocompleteList();
		for(int i = 0 ; i < availableParams.size(); i++) {
			CFWParameter param = (CFWParameter)availableParams.get(i);
			
			AutocompleteItem item = new AutocompleteItem("$"+param.name()+"$", "Parameter: $"+param.name()+"$",
					 "<b>Type:&nbsp;</b>"+param.paramType()
					+"&nbsp;<b>Value:&nbsp;</b>"+StringUtils.abbreviate(param.value(), 200) );
			
			item.setMethodAppend();
			paramList.addItem(item);
		}
		//---------------------------
		// Ultimate Combination!!!
		if(wrappedResult == null) {
			wrappedResult = new AutocompleteResult();
		}
		wrappedResult.addList(paramList);
		return wrappedResult;
	}

}
