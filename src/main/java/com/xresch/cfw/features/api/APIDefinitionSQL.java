package com.xresch.cfw.features.api;

import java.net.HttpURLConnection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.asynchttpclient.util.HttpConstants.ResponseStatusCodes;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.PlaintextResponse;
import com.xresch.cfw.utils.CFWUtilsArray;
import com.xresch.cfw.utils.ResultSetUtils;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class APIDefinitionSQL extends APIDefinition{
	
	private static final Logger logger = CFWLog.getLogger(APIDefinitionSQL.class.getName());
	
	private static final String APIFORMAT = "APIFORMAT";
	
	private APISQLExecutor executor;
	private boolean isSuccess = true;
	private int httpStatusCode = HttpURLConnection.HTTP_OK;
		
	public APIDefinitionSQL(Class<? extends CFWObject> clazz,
							  String apiName, 
						      String actionName, 
						      String[] inputFieldnames) {

		super(clazz, apiName, actionName, inputFieldnames, null);
		APIDefinitionSQL sqlAPIDef = this;
		
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
				
				ArrayList<CFWField> affectedFields = new ArrayList<CFWField>();
				ArrayList<String> fieldnames = new ArrayList<String>();

				// iterate parameters
				while(params.hasMoreElements()) {
					String current = params.nextElement();
					String currentValue = request.getParameter(current);
					
					CFWField field = object.getFieldIgnoreCase(current);

					if(field != null 
					&& currentValue != null 
					&& !currentValue.isEmpty() ) {
						field.setValueValidated(request.getParameter(current));
						affectedFields.add(field);
						fieldnames.add(field.getName());
					}
					
				}
				
				//----------------------------------
				// Create Response
					
				ResultSet result = executor.execute(sqlAPIDef, object);
				 
				try {
					String format = request.getParameter(APIFORMAT);
					if(format == null || format.equals("")) {
						format = "JSON";
					}
					if(format.toUpperCase().equals(ReturnFormat.JSON.toString())) {
						json.getContent().append(ResultSetUtils.toJSON(result));
						
					}else if(format.toUpperCase().equals(ReturnFormat.CSV.toString())){		
						PlaintextResponse plaintext = new PlaintextResponse();
						plaintext.getContent().append(ResultSetUtils.toCSV(result, ";"));
						
					}else if(format.toUpperCase().equals(ReturnFormat.XML.toString())){		
						PlaintextResponse plaintext = new PlaintextResponse();
						plaintext.getContent().append(ResultSetUtils.toXML(result));
					}
				}finally {
					CFWDB.close(result);
				}

				
				json.setSuccess(isSuccess);
				response.setStatus(httpStatusCode);
			}
		});		
	}



	public void setSQLExecutor(APISQLExecutor executor) {
		this.executor = executor;
	}
	
	/*****************************************************************
	 * Set the status of the response.
	 * 
	 *****************************************************************/
	public void setStatus(boolean success, int httpStatusCode) {
		this.isSuccess = success;
		this.httpStatusCode = httpStatusCode;
	}
	
	/*****************************************************************
	 * Add Additional Field
	 *****************************************************************/
	@Override
	public CFWObject createObjectInstance() {
		CFWObject objectInstance = super.createObjectInstance();
		
		CFWField<String> apiFormat = CFWField.newString(FormFieldType.SELECT, APIFORMAT)
				.setDescription("The return format of the api call.")
				.setOptions(ReturnFormat.values());
		
		objectInstance.addField(apiFormat);
		this.addInputFields(apiFormat);
		
		return objectInstance;
	}

}
