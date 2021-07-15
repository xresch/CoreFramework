package com.xresch.cfw.features.jobs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.jobs.CFWJobTask.JobTaskProperty;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.datahandling.CFWFormHandler;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWRandom;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021
 **************************************************************************************************************/
public class ServletJobs extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	public ServletJobs() {
	
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		HTMLResponse html = new HTMLResponse("Jobs");
		
		if(CFW.Context.Request.hasPermission(FeatureJobs.PERMISSION_JOBS_USER)) {
			
			createForms();
			
			String action = request.getParameter("action");
			
			if(action == null) {

				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureJobs.RESOURCE_PACKAGE, "cfw_jobs.js");
				

				html.addJavascriptCode("cfwjobs_initialDraw();");
				
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
					case "myjoblist": 		jsonResponse.setPayLoad(new CFWJob().toJSONElement());
	  										break;
	  										
					default: 				CFW.Messages.itemNotSupported(item);
											break;
				}
				break;
			
			case "fetchpartial": 	
				
				String pagesize = request.getParameter("pagesize");
				String pagenumber = request.getParameter("pagenumber");
				String filterquery = request.getParameter("filterquery");
				
				switch(item.toLowerCase()) {
					case "myjoblist": 		
											jsonResponse.getContent().append(CFW.DB.Jobs.getPartialJobListAsJSONForAdmin(pagesize, pagenumber, filterquery));
	  										break;
	  										
					case "adminjoblist": 	jsonResponse.getContent().append(CFW.DB.Jobs.getPartialJobListAsJSONForAdmin(pagesize, pagenumber, filterquery));
											break;
	  										
					default: 				CFW.Messages.itemNotSupported(item);
											break;
				}
				break;	
			
			case "delete": 			
				switch(item.toLowerCase()) {

					case "job": 		deleteCFWJob(jsonResponse, ID);
										break;  
										
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;	
				
			case "duplicate": 			
				switch(item.toLowerCase()) {

					case "job": 	 	duplicateCFWJob(jsonResponse, ID);
										break;  
										
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;	
				
			case "getform": 			
				switch(item.toLowerCase()) {
					case "createjob": 	String taskName = request.getParameter("taskname");
										createCreateJobForm(jsonResponse, taskName);
										break;
										
					case "editjob": 	createEditForm(jsonResponse, ID);
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
	private void deleteCFWJob(JSONResponse jsonResponse, String ID) {
		CFWDBJob.deleteByID(Integer.parseInt(ID));
	}
	
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void duplicateCFWJob(JSONResponse jsonResponse, String id) {
		CFWDBJob.duplicateByID(id);
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void createForms() {
		
		//-------------------------------------
		// Select Task Form
		CFWForm selectJobTaskForm = 
				new CFWObject()
				.addField(
					CFWField.newString(FormFieldType.SELECT, "TASK")
						.setDescription("Select the Task which should be executed by the job.")
						.setOptions(CFWRegistryJobs.getTaskNames())
				)
				.toForm("cfwSelectJobTaskForm", "Select Job Task");
		
		selectJobTaskForm.onclick("cfwjobs_add_createJob(this);");
	
		//-------------------------------------
		// Create Job Form
		CFWForm createCFWJobForm = new CFWJob().toForm("cfwCreateCFWJobForm", "Create CFWJob");
		
		createCFWJobForm.setFormHandler(new CFWFormHandler() {
			
			@Override
			public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
								
				if(origin != null) {
					if(origin.mapRequestParameters(request)) {
						CFWJob CFWJob = (CFWJob)origin;
						
						if(CFWDBJob.create(CFWJob) ) {
							CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "CFWJob created successfully!");
						}
					}
				}
			}
		});
		
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void createCreateJobForm(JSONResponse json, String taskname) {

		//-----------------------------------
		// Get Task and Properties
		CFWJobTask task = CFW.Registry.Jobs.createTaskInstance(taskname);
		
		ArrayList<JobTaskProperty> propsArray = task.jobProperties();
		LinkedHashMap<String, String> propsMap = new LinkedHashMap<>();
		StringBuilder propDescription = new StringBuilder("<p>The following properties are available for the selected task:</p> <ul> ");
		for(JobTaskProperty property : propsArray) {
			propsMap.put(property.getKey(), property.getValue());
			propDescription.append("<li><b>"+property.getKey()+":&nbsp;</b>"+property.getDescription()+"</li>");
		}
		propDescription.append("</ul>");
		
		//-----------------------------------
		// Create Job Object
		CFWJob job = new CFWJob()
						.taskName(taskname)
						.properties(propsMap);

		//-----------------------------------
		// Create Form	
		CFWForm createCFWJobForm = job.toForm("cfwCreateCFWJobForm"+CFWRandom.randomStringAlphaNumerical(8), "Create Job");
		createCFWJobForm.setDescription(propDescription.toString());
		
		createCFWJobForm.setFormHandler(new CFWFormHandler() {
			
			@Override
			public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
				
				if(origin.mapRequestParameters(request)) {
					
					if(CFWDBJob.create((CFWJob)origin)) {
						CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Done!");
					}
						
				}
				
			}
		});
		
		createCFWJobForm.appendToPayload(json);
		json.setSuccess(true);	
		

	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void createEditForm(JSONResponse json, String ID) {

		CFWJob job = CFWDBJob.selectByID(Integer.parseInt(ID));
		
		//-----------------------------------
		// Get Task and Properties
		CFWJobTask task = CFW.Registry.Jobs.createTaskInstance(job.taskName());
		
		ArrayList<JobTaskProperty> propsArray = task.jobProperties();
		LinkedHashMap<String, String> propsMap = new LinkedHashMap<>();
		StringBuilder propDescription = new StringBuilder("<p>The following properties are available for the selected task:</p> <ul> ");
		for(JobTaskProperty property : propsArray) {
			propsMap.put(property.getKey(), property.getValue());
			propDescription.append("<li><b>"+property.getKey()+":&nbsp;</b>"+property.getDescription()+"</li>");
		}
		propDescription.append("</ul>");
		
		if(job != null) {
			
			CFWForm editCFWJobForm = job.toForm("cfwEditCFWJobForm"+ID, "Update CFWJob");
			editCFWJobForm.setDescription(propDescription.toString());
			
			editCFWJobForm.setFormHandler(new CFWFormHandler() {
				
				@Override
				public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
					
					if(origin.mapRequestParameters(request)) {
						CFWJob jobToUpdate = (CFWJob)origin;
						CFW.Messages.addInfoMessage("Seconds: "+jobToUpdate.schedule().getCalculatedIntervalSeconds());
						if(CFWDBJob.update((CFWJob)origin)) {
							CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Updated!");
						}	
					}
					
				}
			});
			
			editCFWJobForm.appendToPayload(json);
			json.setSuccess(true);	
		}

	}
}