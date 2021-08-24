package com.xresch.cfw.datahandling;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWMultiFormHandlerDefault extends CFWMultiFormHandler {

	@Override
	public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWMultiForm form,
			LinkedHashMap<Integer, CFWObject> originsMap) {
		
		form.mapRequestParameters(request);
		
		//revert uniques of the fields to be able to save to the database.
		form.revertFieldNames();
			for(CFWObject object : originsMap.values()) {
				if(!object.update()) {
					CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The data with the ID '"+object.getPrimaryKey()+"' could not be saved to the database.");
				};
			}
		//make fieldnames Unique again to be able to edit and save again.
		form.makeFieldNamesUnique();
		CFW.Messages.done();
	}

}
