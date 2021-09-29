package com.xresch.cfw.features.jobs;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.jobs.CFWJob.CFWJobFields;
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
		
		if(CFW.Context.Request.hasPermission(FeatureJobs.PERMISSION_JOBS_USER)
		|| CFW.Context.Request.hasPermission(FeatureJobs.PERMISSION_JOBS_ADMIN)) {
			
			createForms();
			
			String action = request.getParameter("action");
			
			if(action != null) {
				
				handleDataRequest(request, response);	
				
			}else {
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureJobs.RESOURCE_PACKAGE, "cfw_jobs.js");
				
				html.addJavascriptCode("cfwjobs_initialDraw();");
				
		        response.setContentType("text/html");
		        response.setStatus(HttpServletResponse.SC_OK);
			}
		}else {
			CFW.Messages.accessDenied();
		}
        
    }
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void handleDataRequest(HttpServletRequest request, HttpServletResponse response) {
		
		String action = request.getParameter("action");
		String item = request.getParameter("item");
		String ID = request.getParameter("id");
		
		JSONResponse jsonResponse = new JSONResponse();		

		switch(action.toLowerCase()) {
					
			case "fetchpartial": 	
				
				String pagesize = request.getParameter("pagesize");
				String pagenumber = request.getParameter("pagenumber");
				String filterquery = request.getParameter("filterquery");
				String sortby = request.getParameter("sortby");
				String sortbydirection = request.getParameter("sortbydirection");
				boolean isAscending = (Strings.isNullOrEmpty(sortbydirection) || !sortbydirection.contentEquals("desc")) ? true : false;
					
				switch(item.toLowerCase()) {
					case "myjoblist": 		if(CFW.Context.Request.hasPermission(FeatureJobs.PERMISSION_JOBS_USER)) {
												jsonResponse.getContent().append(CFW.DB.Jobs.getPartialJobListAsJSONForUser(pagesize, pagenumber, filterquery, sortby, isAscending));
											}else { CFW.Messages.noPermission(); }
											break;
	  										
					case "adminjoblist": 	if(CFW.Context.Request.hasPermission(FeatureJobs.PERMISSION_JOBS_ADMIN)) {
												jsonResponse.getContent().append(CFW.DB.Jobs.getPartialJobListAsJSONForAdmin(pagesize, pagenumber, filterquery, sortby, isAscending));
											}else { CFW.Messages.noPermission(); }
											break;
	  										
					default: 				CFW.Messages.itemNotSupported(item);
											break;
				}
				break;	
			
			case "fetch": 			
				switch(item.toLowerCase()) {

					case "tasks": 		fetchTasks(jsonResponse);
										break;  
										
					default: 			CFW.Messages.itemNotSupported(item);
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
				
			case "execute": 			
				switch(item.toLowerCase()) {

					case "job": 		executeCFWJob(jsonResponse, ID);
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
	private void fetchTasks(JSONResponse jsonResponse) {
		
		// Do the check here only, allows to create jobs
		// programmatically using CFWDBJobs, without the user
		// needing rights for job feature
		if(CFW.Context.Request.hasPermission(FeatureJobs.PERMISSION_JOBS_ADMIN)
		|| CFW.Context.Request.hasPermission(FeatureJobs.PERMISSION_JOBS_USER)) {
			
			jsonResponse.setPayLoad(CFW.Registry.Jobs.getTasksForUserAsJson());
			
			return;
		}else {
			CFW.Messages.noPermission();
		}
		
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void deleteCFWJob(JSONResponse jsonResponse, String ID) {
		
		// Do the check here only, allows to create jobs
		// programmatically using CFWDBJobs, without the user
		// needing rights for job feature
		if(CFW.Context.Request.hasPermission(FeatureJobs.PERMISSION_JOBS_ADMIN)) {
			CFW.DB.Jobs.deleteByID(Integer.parseInt(ID));
			return;
		}
		
		if(CFW.Context.Request.hasPermission(FeatureJobs.PERMISSION_JOBS_USER)
		&& CFW.DB.Jobs.checkIsCurrentUserOwner(ID)) {
			
			CFW.DB.Jobs.deleteByID(Integer.parseInt(ID));
			return;
		}else {
			CFW.Messages.noPermission();
		}
		
	}
		
	/******************************************************************
	 *
	 ******************************************************************/
	private void executeCFWJob(JSONResponse jsonResponse, String ID) {
		
		CFW.Registry.Jobs.executeJobManually(ID);
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
//		CFWForm selectJobTaskForm = 
//				new CFWObject()
//				.addField(
//					CFWField.newString(FormFieldType.SELECT, "TASK")
//						.setDescription("Select the Task which should be executed by the job.")
//						.setOptions(CFWRegistryJobs.getTaskNamesForUI())
//				)
//				.toForm("cfwSelectJobTaskForm", "Select Job Task");
//		
//		selectJobTaskForm.onclick("cfwjobs_add_createJob(this);");
	
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
		
		CFWObject propertyFields = task.getParameters();
		
		//-----------------------------------
		// Create Job Object
		CFWJob job = new CFWJob()
						.foreignKeyOwner(CFW.Context.Request.getUser().id())
						.taskName(taskname);

		job.addAllFields(propertyFields.getFields());
		
		//-----------------------------------
		// Create Form	
		CFWForm createCFWJobForm = job.toForm("cfwCreateCFWJobForm"+CFWRandom.randomStringAlphaNumerical(8), "Create Job");
		
		createCFWJobForm.setFormHandler(new CFWFormHandler() {
			
			@Override
			public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
				//-------------------------------------
				// Create new CFWJob as origin contains
				// additional fields.
				CFWJob jobToCreate = new CFWJob();
				
				if( jobToCreate.mapRequestParameters(request)
				 && propertyFields.mapRequestParameters(request)) {
					
					jobToCreate.properties(propertyFields);
					int scheduleIntervalSec = jobToCreate.schedule().getCalculatedIntervalSeconds();
					
					if( !task.isMinimumIntervalValid(scheduleIntervalSec) ) {
						return;
					}
					
					if(CFWDBJob.create(jobToCreate)) {
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

		//-------------------------------------
		// Check Permissions
		if(!CFW.Context.Request.hasPermission(FeatureJobs.PERMISSION_JOBS_ADMIN)
		&& !(
				CFW.Context.Request.hasPermission(FeatureJobs.PERMISSION_JOBS_USER)
		        && CFW.DB.Jobs.checkIsCurrentUserOwner(ID)
		    )
		) {
			CFW.Messages.noPermission();
		}
		
	
		//-----------------------------------
		// Get Job, Task and Properties
		CFWJob job = CFWDBJob.selectByID(Integer.parseInt(ID));
		CFWJobTask task = CFW.Registry.Jobs.createTaskInstance(job.taskName());
		
		CFWObject propertyFields = task.getParameters();
		propertyFields.mapJsonFields(job.properties());
		
		job.addAllFields(propertyFields.getFields());
		
		if(job != null) {
			
			//-----------------------------------
			// Create Form
			CFWForm editCFWJobForm = job.toForm("cfwEditCFWJobForm"+ID, "Update CFWJob");

			editCFWJobForm.setFormHandler(new CFWFormHandler() {
				
				@Override
				public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
					
					//-------------------------------------
					// Load Job from DB
					// - origin contains additional fields, would cause errors
					// - Last run has to be read from DB
					String jobID = request.getParameter(CFWJobFields.PK_ID.toString());
					CFWJob jobToSave = CFW.DB.Jobs.selectByID(jobID);
					
					if( jobToSave.mapRequestParameters(request)
					&& propertyFields.mapRequestParameters(request)) {
						
						jobToSave.properties(propertyFields);
						int scheduleIntervalSec = jobToSave.schedule().getCalculatedIntervalSeconds();
						
						if( !task.isMinimumIntervalValid(scheduleIntervalSec) ) {
							return;
						}
						
						if(CFWDBJob.update(jobToSave)) {
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