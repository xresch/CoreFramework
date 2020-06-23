package com.pengtoolbox.cfw.features.dashboard;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.features.api.APIDefinition;
import com.pengtoolbox.cfw.features.api.APIRequestHandler;
import com.pengtoolbox.cfw.response.JSONResponse;
import com.pengtoolbox.cfw.utils.CFWArrayUtils;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class APIDashboardImport extends APIDefinition{
	
	private static final String KEEP_OWNER = "KEEP_OWNER";
	private static final String JSON_DATA = "JSON_DATA";
	
	private static final String[] inputFieldnames = new String[] {};
	private static final String[] outputFieldnames = new String[] {};
	
	public APIDashboardImport(String apiName, 
						      String actionName) {

		super(Dashboard.class, apiName, actionName, inputFieldnames, outputFieldnames);

		this.setDescription("Import one or all dashboards represented as a JSON string that was be exported through the export API.");
		
		this.setRequestHandler(new APIRequestHandler() {
			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void handleRequest(HttpServletRequest request, HttpServletResponse response, APIDefinition definition) {
				
				JSONResponse json = new JSONResponse();
											
				//----------------------------------
				// Create Response
				String keepOwner = request.getParameter(KEEP_OWNER);
				String jsonData = request.getParameter(JSON_DATA);
				json.getContent().append( CFW.DB.Dashboards.importByJson(jsonData, Boolean.parseBoolean(keepOwner)) );
				
				json.setSuccess(true);

			}
		});		
	}
	
	
	/*****************************************************************
	 * Add Additional Field
	 *****************************************************************/
	public CFWObject createObjectInstance() {
		CFWObject instance = super.createObjectInstance();
		
		//-----------------------------
		// Keep Owner
		CFWField<Boolean> keepOwner = CFWField.newBoolean(FormFieldType.BOOLEAN, KEEP_OWNER)
				.setDescription("If true, tries to keep the dashboard owner by searching for the username in the database. If false, sets the dashboard owner to the importing user.")
				.setValue(true);
		
		instance.addField(keepOwner);
		if(!CFWArrayUtils.contains(this.getInputFieldnames(), KEEP_OWNER)) {
			this.addInputFieldname(KEEP_OWNER);
		}
		
		//-----------------------------
		// JSON Data
		CFWField<Boolean> jsonData = CFWField.newBoolean(FormFieldType.TEXTAREA, JSON_DATA)
				.setDescription("The data to import that was exported with the export API.")
				.addAttribute("rows", "10");
		
		instance.addField(jsonData);
		if(!CFWArrayUtils.contains(this.getInputFieldnames(), JSON_DATA)) {
			this.addInputFieldname(JSON_DATA);
		}
		
		return instance;
	}
}
