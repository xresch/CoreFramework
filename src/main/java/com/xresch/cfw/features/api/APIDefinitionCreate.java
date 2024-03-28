package com.xresch.cfw.features.api;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.PlaintextResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class APIDefinitionCreate extends APIDefinition{
	
	private static final Logger logger = CFWLog.getLogger(APIDefinitionCreate.class.getName());
	
	protected static final String APIFORMAT = "APIFORMAT";
	
	public APIDefinitionCreate(Class<? extends CFWObject> clazz,
							  String apiName, 
						      String actionName, 
						      String[] inputFieldnames,
						      String[] outputFieldnames) {

		super(clazz, apiName, actionName, inputFieldnames, outputFieldnames);

		this.setDescription("Standard API to create "+clazz.getSimpleName()+" data. The provided parameters will be used to create an insert statement."
				+ " You will get an error message if something is missing.");
		
		this.setRequestHandler(new APIRequestHandler() {
			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void handleRequest(HttpServletRequest request, HttpServletResponse response, APIDefinition definition, String bodyContents) {
				
				JSONResponse json = new JSONResponse();
				
				//----------------------------------
				// Resolve Fields
				Enumeration<String> params = request.getParameterNames();
				
				CFWObject object;
				try {
					object = clazz.newInstance();
				} catch (Exception e) {
					new CFWLog(logger)
						.severe("Could not create instance for '"+clazz.getSimpleName()+"'. Check if you have a constructor without parameters.", e);
				
					json.setSuccess(false);
					return;
				}
				
				
				//----------------------------------
				// Map Values
				ArrayList<String> fieldnames = new ArrayList<String>();
				boolean success = true;
				
				// iterate parameters
				while(params.hasMoreElements()) {
					String current = params.nextElement();
					String currentValue = request.getParameter(current);
					
					CFWField field = object.getFieldIgnoreCase(current);

					if(field != null 
					&& currentValue != null 
					&& !currentValue.isEmpty()) {
						field.setValueConvert(request.getParameter(current), true);
						fieldnames.add(field.getName());
					}
				}
				
				
				//----------------------------------
				// Validate
				if(!object.validateAllFields()) {
					for(CFWField field : object.getFields().values()) {
						json.setSuccess(false);
						
						if(!field.lastValidationResult()) {
							ArrayList<String> messageArray = field.getInvalidationMessages();
							if(messageArray != null) {
								for(String message : messageArray) {
									CFW.Context.Request.addAlertMessage(MessageType.ERROR, message);
								}
							}
						}
						
					}
					return;
				}
				
				
				//----------------------------------
				// Intsert into database
				Integer id = object.insertGetPrimaryKey();
				if(id == null) {
					json.setSuccess(false);
					return;
				}
				
				//----------------------------------
				// Create Response
				if(success) {
					
					CFWSQL statement = object.select((Object[])definition.getOutputFieldnames());
					
					statement.where(object.getPrimaryKeyField().getName(), id);

					String format = request.getParameter(APIFORMAT);
					if(format == null || format.equals("")) {
						format = "JSON";
					}
					if(format.toUpperCase().equals(APIReturnFormat.JSON.toString())) {
						json.getContent().append(statement.getAsJSON());
					}else if(format.toUpperCase().equals(APIReturnFormat.CSV.toString())){		
						PlaintextResponse plaintext = new PlaintextResponse();
						
						plaintext.getContent().append(statement.getAsCSV());
					}else if(format.toUpperCase().equals(APIReturnFormat.XML.toString())){		
						PlaintextResponse plaintext = new PlaintextResponse();
						
						plaintext.getContent().append(statement.getAsXML());
					}
					
				}else {
					response.setStatus(400);
				}
				
				json.setSuccess(success);

			}
		});		
	}
	
	/*****************************************************************
	 * Add Additional Field
	 *****************************************************************/
	@Override
	public CFWObject createObjectInstance() {
		CFWObject instance = super.createObjectInstance();
		
		CFWField<String> apiFormat = CFWField.newString(FormFieldType.SELECT, APIFORMAT)
				.setDescription("The return format of the api call.")
				.setOptions(APIReturnFormat.values());
		
		instance.addField(apiFormat);
		this.addInputFields(apiFormat);
		
		return instance;
	}
	
	
	

}
