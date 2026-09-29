package com.xresch.cfw.features.api;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.spaces.FeatureSpaces;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.PlaintextResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class APIDefinitionFetch extends APIDefinition{
	
	private static final Logger logger = CFWLog.getLogger(APIDefinitionFetch.class.getName());
	
	protected static final String APIFORMAT = "APIFORMAT";
	
	
	/*****************************************************************
	 * Constructor
	 *****************************************************************/
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
				
				//-------------------------------
				// Get Salt Field
				CFWField<?> saltField = object.getSaltField();
				
				
				//-------------------------------
				// Find Affected Fields
				ArrayList<CFWField> filteringFields = new ArrayList<CFWField>();
				ArrayList<String> fieldnames = new ArrayList<String>();

				
				// iterate parameters
				while(params.hasMoreElements()) {
					String current = params.nextElement();
					String currentValue = request.getParameter(current);
					
					CFWField field = object.getFieldIgnoreCase(current);

					if(field != null 
					&& currentValue != null 
					&& !currentValue.isEmpty()) {
						field.setValueValidated(request.getParameter(current));
						filteringFields.add(field);
						fieldnames.add(field.getName());
					}
					
				}
				
				//----------------------------------
				// Create Response

				CFWSQL statement = object.select(definition.getOutputFieldnames());
				
				//--------------------------
				// Add Filters From API

				for(int i = 0; i < filteringFields.size(); i++) {
					CFWField<?> currentField = filteringFields.get(i);
					if(i == 0) {
						statement.where(currentField.getName(), currentField.getValue(), false);
					}else {
						statement.and(currentField.getName(), currentField.getValue(), false);
					}
				}
				
				//--------------------------
				// Add Space Filter
				if( isSpaced() ) {
					if(filteringFields.size() > 0) {
						statement.and().append(FeatureSpaces.getSQLFilterInclusive());
					}else {
						statement.where().append(FeatureSpaces.getSQLFilterInclusive());
					}
				}
				
				//--------------------------
				// Choose Format
				String format = request.getParameter(APIFORMAT);
				if(format == null || format.equals("")) {
					format = "JSON";
				}
				
				//--------------------------
				// Fetch Data
				
				// Note: Needed in case of encryption.
				ArrayList<CFWObject> objectList = statement.getAsObjectList();
				
				// do not return salt field
				if(saltField != null) {
					for(CFWObject o : objectList) {
						o.removeField(saltField.getName());
					}
				}
				
				//--------------------------
				// Format and Return
				JsonArray array = CFW.JSON.collectionToJsonArray(objectList);
				if(format.toUpperCase().equals(APIReturnFormat.JSON.toString())) {
					json.setPayload(array);
					//json.getContent().append(statement.getAsJSON());
				}else if(format.toUpperCase().equals(APIReturnFormat.CSV.toString())){		
					PlaintextResponse plaintext = new PlaintextResponse();
					
					plaintext.getContent().append(
						CFW.JSON.toCSV(array, ",")
					);
					
				}
				else if(format.toUpperCase().equals(APIReturnFormat.XML.toString())){		
					PlaintextResponse plaintext = new PlaintextResponse();
					plaintext.getContent().append(
							CFW.JSON.toXML(array, true)
						);
				}

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
