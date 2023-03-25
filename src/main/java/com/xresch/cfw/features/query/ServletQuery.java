package com.xresch.cfw.features.query;

import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023
 * @license MIT-License
 **************************************************************************************************************/
public class ServletQuery extends HttpServlet
{

	private static final String AUTOCOMPLETE_FORMID = "cfwQueryAutocompleteForm";
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(ServletQuery.class.getName());
	
	public ServletQuery() {
		
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{
		
		doGet(request, response);
	}
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		HTMLResponse html = new HTMLResponse("Query");
		
		if( CFW.Context.Request.hasPermission(FeatureQuery.PERMISSION_QUERY_USER)
		 || CFW.Context.Request.hasPermission(FeatureQuery.PERMISSION_QUERY_ADMIN)) {
			
			//-----------------------------------------------
			// Create query object to handle autocompletion
			// Field will be created on client side
			CFWObject queryObject = createQueryObject();
			queryObject.toForm(AUTOCOMPLETE_FORMID, "Form for Autocomplete Handling");
			
			//cfw_autocompleteInitialize('cfwExampleHandlerForm','JSON_TAGS_SELECTOR',1,10);
			String action = request.getParameter("action");
			
			if(action == null) {

				html.addCSSFile(HandlingType.JAR_RESOURCE, FeatureQuery.PACKAGE_RESOURCES, "cfw_query.css");
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureQuery.PACKAGE_RESOURCES, "cfw_query_rendering.js");
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureQuery.PACKAGE_RESOURCES, "cfw_query.js");
				
				html.addJavascriptCode("cfw_query_initialDraw();");
				html.addJavascriptData("formID", AUTOCOMPLETE_FORMID);
				html.addJavascriptData("requestHeaderMaxSize", CFW.Properties.HTTP_MAX_REQUEST_HEADER_SIZE);
		        response.setContentType("text/html");
		        response.setStatus(HttpServletResponse.SC_OK);
			}else {
				handleDataRequest(request, response);
			}
		}else {
			CFWMessages.accessDenied();
		}
        
    }
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void handleDataRequest(HttpServletRequest request, HttpServletResponse response) {
		
		String action = request.getParameter("action");
		String item = request.getParameter("item");
		String ID = request.getParameter("id");
		
		//int	userID = CFW.Context.Request.getUser().id();
			
		JSONResponse jsonResponse = new JSONResponse();		

		switch(action.toLowerCase()) {
		
			case "fetch": 			
				switch(item.toLowerCase()) {
					case "personlist": 		//jsonResponse.getContent().append(PersonDBMethods.getPersonListAsJSON());
	  										break;
	  										
					default: 				CFW.Messages.itemNotSupported(item);
											break;
				}
				break;
			
			case "fetchpartial": 			
				switch(item.toLowerCase()) {
					case "personlist": 		String pagesize = request.getParameter("pagesize");
											String pagenumber = request.getParameter("pagenumber");
											String filterquery = request.getParameter("filterquery");
											String sortby = request.getParameter("sortby");
											String isAscendingString = request.getParameter("isascending");
											boolean isAscending = (isAscendingString == null || isAscendingString.equals("true")) ? true : false;
											
											//jsonResponse.getContent().append(PersonDBMethods.getPartialPersonListAsJSON(pagesize, pagenumber, filterquery, sortby, isAscending));
	  										break;
	  										
					default: 				CFW.Messages.itemNotSupported(item);
											break;
				}
				break;	
			
			case "delete": 			
				switch(item.toLowerCase()) {

					case "person": 		//deletePerson(jsonResponse, ID);
										break;  
										
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;	
				
			case "execute": 			
				switch(item.toLowerCase()) {

					case "query": 	 	executeQuery(request, jsonResponse);
										break;  
										
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;	
				
			case "getform": 			
				switch(item.toLowerCase()) {
					case "editperson": 	createEditForm(jsonResponse, ID);
										break;
					
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;
						
			default: 			CFW.Messages.actionNotSupported(action);
								break;
								
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void executeQuery(HttpServletRequest request, JSONResponse jsonResponse) {
		String query = request.getParameter("query");
		String earliest = request.getParameter("earliest");
		String latest = request.getParameter("latest");
		int timezoneOffsetMinutes = Integer.parseInt(request.getParameter("timezoneOffsetMinutes"));

		CFWQueryResultList resultList = new CFWQueryExecutor().parseAndExecuteAll(query, Long.parseLong(earliest), Long.parseLong(latest), timezoneOffsetMinutes);
		
		if(resultList != null) {
			jsonResponse.setPayLoad(resultList.toJson());
			jsonResponse.setSuccess(true);
		}else {
			jsonResponse.setSuccess(false);
		}


		//PersonDBMethods.deleteByID(Integer.parseInt(ID));
	}
	
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void duplicatePerson(JSONResponse jsonResponse, String id) {
		//PersonDBMethods.duplicateByID(id);
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private CFWObject createQueryObject() {
			
		//Create the object
		CFWObject queryObject = new CFWObject()
				.addField(
					CFWField.newString(FormFieldType.TEXTAREA, "query")
						.setAutocompleteHandler(new CFWQueryAutocompleteHandler())
				);
				
		return queryObject;
				
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void createEditForm(JSONResponse json, String ID) {

//		Person Person = PersonDBMethods.selectByID(Integer.parseInt(ID));
//		
//		if(Person != null) {
//			
//			CFWForm editPersonForm = Person.toForm("cfwEditPersonForm"+ID, "Update Person");
//			
//			editPersonForm.setFormHandler(new CFWFormHandler() {
//				
//				@Override
//				public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
//					
//					if(origin.mapRequestParameters(request)) {
//						
//						if(PersonDBMethods.update((Person)origin)) {
//							CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Updated!");
//						}
//							
//					}
//					
//				}
//			});
//			
//			editPersonForm.appendToPayload(json);
//			json.setSuccess(true);	
//		}

	}
}