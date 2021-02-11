package com.xresch.cfw.datahandling;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw.features.core.AutocompleteResult;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021
 * @license MIT-License
 **************************************************************************************************************/
public abstract class CFWFormCustomAutocompleteHandler {
	

	/*******************************************************************************
	 * Return a hashmap with value / label combinations
	 * @param request 
	 * @return JSON string
	 *******************************************************************************/
	public abstract AutocompleteResult getAutocompleteData(
					HttpServletRequest request, 
					HttpServletResponse response, 
					CFWForm form, 
					CFWField field, 
					String searchValue
				);


}
