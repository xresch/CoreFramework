package com.xresch.cfw.features.dashboard;

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

class DashboardParameterAutocompleteWrapper extends CFWAutocompleteHandler {

	private CFWField wrappedField; 
	private String dashboardID;
	private String widgetType;
	private CFWAutocompleteHandler wrappedHandler;
	
	public DashboardParameterAutocompleteWrapper(CFWField settingsFieldToWrap, String dashboardID, String widgetType) {
		this.dashboardID = dashboardID;
		this.widgetType = widgetType;
		
		this.wrappedField = settingsFieldToWrap;		
		this.wrappedHandler = wrappedField.getAutocompleteHandler();
		
		//---------------------------
		// Replace old handler 
		wrappedField.setAutocompleteHandler(this);
	}
	@Override
	public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
		
		//---------------------------
		// Get Regular Result
		AutocompleteResult wrappedResult = null;
		if(wrappedHandler != null) {
			wrappedResult = wrappedHandler.getAutocompleteData(request, searchValue);
		};
		
		//---------------------------
		// Get Param Result
		String widgetSetting = wrappedField.getName();
		ArrayList<CFWObject> availableParams = CFW.DB.DashboardParameters.getAvailableParamsForDashboard(dashboardID, widgetType, widgetSetting, true);
		
		//---------------------------
		// Prepare params Autocomplete
		if(availableParams == null || availableParams.isEmpty()) {
			return wrappedResult;
		}
		
		AutocompleteList paramList = new AutocompleteList();
		for(int i = 0 ; i < availableParams.size(); i++) {
			DashboardParameter param = (DashboardParameter)availableParams.get(i);
			
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
