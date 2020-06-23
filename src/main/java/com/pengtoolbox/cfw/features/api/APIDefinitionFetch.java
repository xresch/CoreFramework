package com.pengtoolbox.cfw.features.api;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;

import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.db.CFWSQL;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.JSONResponse;
import com.pengtoolbox.cfw.response.PlaintextResponse;
import com.pengtoolbox.cfw.utils.CFWArrayUtils;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class APIDefinitionFetch extends APIDefinition{
	
	private static final String APIFORMAT = "APIFORMAT";
	
	public APIDefinitionFetch(Class<? extends CFWObject> clazz,
							  String apiName, 
						      String actionName, 
						      String[] inputFieldnames,
						      String[] outputFieldnames) {

		super(clazz, apiName, actionName, inputFieldnames, outputFieldnames);

		this.setDescription("Standard API to fetch "+clazz.getSimpleName()+" data. The provided parameters will be used to create a select statement with a WHERE ... AND clause."
				+ " To retrieve everything leaf all the parameters empty."
				+ " The standard return format is JSON if the parameter APIFORMAT is not specified.");
		
		this.setRequestHandler(new APIRequestHandler() {
			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void handleRequest(HttpServletRequest request, HttpServletResponse response, APIDefinition definition) {
				
				JSONResponse json = new JSONResponse();
				
				//----------------------------------
				// Resolve Fields
				Enumeration<String> params = request.getParameterNames();
				
				CFWObject object;
				try {
					object = clazz.newInstance();
				} catch (Exception e) {
					new CFWLog(logger)
						.method("handleRequest")
						.severe("Could not create instance for '"+clazz.getSimpleName()+"'. Check if you have a constructor without parameters.", e);
				
					json.setSuccess(false);
					return;
				}
				
				ArrayList<CFWField> affectedFields = new ArrayList<CFWField>();
				ArrayList<String> fieldnames = new ArrayList<String>();
				boolean success = true;
				
				// iterate parameters
				while(params.hasMoreElements()) {
					String current = params.nextElement();
					String currentValue = request.getParameter(current);
					
					CFWField field = object.getFieldIgnoreCase(current);

					if(field != null) {
						if(currentValue != null && !currentValue.isEmpty()) {
							field.setValueValidated(request.getParameter(current));
							affectedFields.add(field);
							fieldnames.add(field.getName());
						}
					}
				}
				
				//----------------------------------
				// Create Response
				if(success) {
					
					CFWSQL statement = object.select(definition.getOutputFieldnames());
					
					for(int i = 0; i < affectedFields.size(); i++) {
						CFWField<?> currentField = affectedFields.get(i);
						if(i == 0) {
							statement.where(currentField.getName(), currentField.getValue(), false);
						}else {
							statement.and(currentField.getName(), currentField.getValue(), false);
						}
					}
					
					String format = request.getParameter(APIFORMAT);
					if(format == null || format.equals("")) {
						format = "JSON";
					}
					if(format.toUpperCase().equals(ReturnFormat.JSON.toString())) {
						json.getContent().append(statement.getAsJSON());
					}else if(format.toUpperCase().equals(ReturnFormat.CSV.toString())){		
						PlaintextResponse plaintext = new PlaintextResponse();
						
						plaintext.getContent().append(statement.getAsCSV());
					}else if(format.toUpperCase().equals(ReturnFormat.XML.toString())){		
						PlaintextResponse plaintext = new PlaintextResponse();
						
						plaintext.getContent().append(statement.getAsXML());
					}
					
				}else {
					response.setStatus(HttpStatus.BAD_REQUEST_400);
				}
				
				json.setSuccess(success);

			}
		});		
	}
	
	/*****************************************************************
	 * Add Additional Field
	 *****************************************************************/
	public CFWObject createObjectInstance() {
		CFWObject instance = super.createObjectInstance();
		
		CFWField<String> apiFormat = CFWField.newString(FormFieldType.SELECT, APIFORMAT)
				.setDescription("The return format of the api call.")
				.setOptions(ReturnFormat.values());
		
		instance.addField(apiFormat);
		if(!CFWArrayUtils.contains(this.getInputFieldnames(), APIFORMAT)) {
			this.addInputFieldname(APIFORMAT);
		}
		
		return instance;
	}
	
	
	

}
