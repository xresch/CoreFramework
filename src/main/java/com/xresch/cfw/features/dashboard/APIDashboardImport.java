package com.xresch.cfw.features.dashboard;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIRequestHandler;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.utils.CFWUtilsArray;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
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
			public void handleRequest(HttpServletRequest request, HttpServletResponse response, APIDefinition definition, String bodyContents) {
				
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
	@Override
	public CFWObject createObjectInstance() {
		CFWObject instance = super.createObjectInstance();
		
		//-----------------------------
		// Keep Owner
		CFWField<Boolean> keepOwner = CFWField.newBoolean(FormFieldType.BOOLEAN, KEEP_OWNER)
				.setDescription("If true, tries to keep the dashboard owner by searching for the username in the database. If false, sets the dashboard owner to the importing user.")
				.setValue(true);
		
		instance.addField(keepOwner);
		this.addInputFields(keepOwner);
		
		//-----------------------------
		// JSON Data
		CFWField<Boolean> jsonData = CFWField.newBoolean(FormFieldType.TEXTAREA, JSON_DATA)
				.setDescription("The data to import that was exported with the export API.")
				.addAttribute("rows", "10");
		
		instance.addField(jsonData);
		this.addInputFields(jsonData);
		
		return instance;
	}
}
