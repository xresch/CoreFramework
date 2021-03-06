package com.xresch.cfw.features.dashboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.datahandling.CFWFormHandler;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.dashboard.Dashboard.DashboardFields;
import com.xresch.cfw.features.dashboard.parameters.DashboardParameter;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletDashboardList extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = CFWLog.getLogger(ServletDashboardList.class.getName());
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		HTMLResponse html = new HTMLResponse("Dashboard List");
		
		if(CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_VIEWER)
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_CREATOR)
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
			
			createForms();
			
			String action = request.getParameter("action");
			
			if(action == null) {

				//html.addCSSFile(HandlingType.JAR_RESOURCE, FeatureSpaces.RESOURCE_PACKAGE, "cfw_dashboard.css");
				
				//html.addJSFileBottomSingle(new FileDefinition(HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE+".js", "cfw_usermgmt.js"));
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "cfw_dashboard_list.js");
				
				//content.append(CFW.Files.readPackageResource(FeatureSpaces.RESOURCE_PACKAGE, "cfw_dashboard.html"));
				
				html.addJavascriptCode("cfw_dashboardlist_initialDraw();");
				
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
	@Override
   protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
   {
		doGet(request, response);       
   }
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void handleDataRequest(HttpServletRequest request, HttpServletResponse response) {
		
		String action = request.getParameter("action");
		String item = request.getParameter("item");
		String ID = request.getParameter("id");
		String IDs = request.getParameter("ids");
		//int	userID = CFW.Context.Request.getUser().id();
			
		JSONResponse jsonResponse = new JSONResponse();
		
		//--------------------------------------
		// Check Permissions
		if(action.toLowerCase().equals("delete")
		|| action.toLowerCase().equals("copy")
		|| action.toLowerCase().equals("getform")) {
			if(!CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_CREATOR)
			   && !CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
				CFWMessages.noPermission();
				return;
			}
		}
		

		switch(action.toLowerCase()) {
		
			case "fetch": 			
				switch(item.toLowerCase()) {
					case "mydashboards": 		jsonResponse.getContent().append(CFW.DB.Dashboards.getUserDashboardListAsJSON());
	  											break;
	  											
					case "shareddashboards": 	jsonResponse.getContent().append(CFW.DB.Dashboards.getSharedDashboardListAsJSON());
												break;	
												
					case "admindashboards": 	jsonResponse.getContent().append(CFW.DB.Dashboards.getAdminDashboardListAsJSON());
												break;	
												
					case "export": 				jsonResponse.getContent().append(CFW.DB.Dashboards.getJsonArrayForExport(ID));
												break;									
																										
					default: 					CFW.Messages.itemNotSupported(item);
												break;
				}
				break;
			
			case "duplicate": 			
				switch(item.toLowerCase()) {

					case "dashboard": 	duplicateDashboard(jsonResponse, ID);
										break;  
										
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;	
			case "delete": 			
				switch(item.toLowerCase()) {

					case "dashboards": 	deleteDashboards(jsonResponse, IDs);
										break;  
										
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;	
				
			case "import": 			
				switch(item.toLowerCase()) {

					case "dashboards": 	String jsonString = request.getParameter("jsonString");
										CFW.DB.Dashboards.importByJson(jsonString, false);
										CFW.Context.Request.addAlertMessage(MessageType.INFO, "Import finished!");
										break;  
										
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;	
			case "getform": 			
				switch(item.toLowerCase()) {
					case "editdashboard": 	createEditDashboardForm(jsonResponse, ID);
											break;
					case "changeowner": 	createChangeDashboardOwnerForm(jsonResponse, ID);
											break;
					default: 				CFW.Messages.itemNotSupported(item);
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
	private void deleteDashboards(JSONResponse jsonResponse, String IDs) {
		// TODO Auto-generated method stub
		if(CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
			jsonResponse.setSuccess(CFW.DB.Dashboards.deleteMultipleByID(IDs));
		}else {
			int userid = CFW.Context.Request.getUser().id();
			jsonResponse.setSuccess(CFW.DB.Dashboards.deleteMultipleByIDForUser(userid, IDs));
		}
	}
	
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void duplicateDashboard(JSONResponse jsonResponse, String dashboardID) {
		// TODO Auto-generated method stub
		if(CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_CREATOR)
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
			Dashboard duplicate = CFW.DB.Dashboards.selectByID(dashboardID);
			duplicate.id(null);
			duplicate.foreignKeyOwner(CFW.Context.Request.getUser().id());
			duplicate.name(duplicate.name()+"(Copy)");
			duplicate.isShared(false);
			duplicate.sharedWithUsers(null);
			duplicate.editors(null);
			
			Integer newID = duplicate.insertGetPrimaryKey();
			
			if(newID != null) {
				
				//-----------------------------------------
				// Duplicate Widgets
				//-----------------------------------------
				ArrayList<CFWObject> widgetList = CFW.DB.DashboardWidgets.getWidgetsForDashboard(dashboardID);
				
				boolean success = true;
				for(CFWObject object : widgetList) {
					DashboardWidget widgetToCopy = (DashboardWidget)object;
					widgetToCopy.id(null);
					widgetToCopy.foreignKeyDashboard(newID);
					
					if(!widgetToCopy.insert()) {
						success = false;
						CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Error while duplicating widget.");
					}
				}
				
				//-----------------------------------------
				// Duplicate Parameters
				//-----------------------------------------
				ArrayList<CFWObject> parameterList = CFW.DB.DashboardParameters.getParametersForDashboard(dashboardID);
				
				for(CFWObject object : parameterList) {
					DashboardParameter paramToCopy = (DashboardParameter)object;
					paramToCopy.id(null);
					paramToCopy.foreignKeyDashboard(newID);
					
					if(!paramToCopy.insert()) {
						success = false;
						CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Error while duplicating parameter.");
					}
				}
				
				if(success) {
					CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Dashboard duplicated successfully.");
				}
				jsonResponse.setSuccess(success);
				
			}else {
				jsonResponse.setSuccess(false);
			}
			
		}else {
			jsonResponse.setSuccess(false);
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Insufficient permissions to duplicate the dashboard.");
		}
	}

	/******************************************************************
	 *
	 ******************************************************************/
	private void createForms() {
				
		//--------------------------------------
		// Create Dashboard Form
		if(CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_CREATOR)
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
			CFWForm createDashboardForm = new Dashboard().toForm("cfwCreateDashboardForm", "{!cfw_dashboard_create!}");
			
			createDashboardForm.setFormHandler(new CFWFormHandler() {
				
				@Override
				public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
									
					if(origin != null) {
						
						origin.mapRequestParameters(request);
						Dashboard dashboard = (Dashboard)origin;
						dashboard.foreignKeyOwner(CFW.Context.Request.getUser().id());
						if( CFW.DB.Dashboards.create(dashboard) ) {
							CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Dashboard created successfully!");
						}
					}
					
				}
			});
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void createEditDashboardForm(JSONResponse json, String ID) {
		
		if(CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_CREATOR)
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
			Dashboard dashboard = CFW.DB.Dashboards.selectByID(Integer.parseInt(ID));
			
			if(dashboard != null) {
				
				CFWForm editDashboardForm = dashboard.toForm("cfwEditDashboardForm"+ID, "Update Dashboard");
				
				editDashboardForm.setFormHandler(new CFWFormHandler() {
					
					@Override
					public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
						
						Dashboard dashboard = (Dashboard)origin;
						if(origin.mapRequestParameters(request) 
						&& CFW.DB.Dashboards.update((Dashboard)origin)) {
							
							
							CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Updated!");
							
							if(!dashboard.isShared()
							&& (dashboard.sharedWithUsers().size() > 0
							   || dashboard.sharedWithGroups().size() > 0
							   || dashboard.editors().size() > 0
							   || dashboard.editorGroups().size() > 0
								)
							) {
								
								CFW.Context.Request.addAlertMessage(
										MessageType.INFO, 
										"Users won't be able to access your dashboard until you set shared to true. The dashboard was saved as not shared and with at least one shared users or roles. "
									);
							}
							
							if(dashboard.isShared()
							&& dashboard.sharedWithUsers().size() == 0
							&& dashboard.sharedWithGroups().size() == 0
							&& dashboard.editors().size() == 0
							&& dashboard.editorGroups().size() == 0) {
										
								CFW.Context.Request.addAlertMessage(
										MessageType.INFO, 
										"All dashboard users will see this dashboard. The dashboard was saved as shared and no specific shared users or roles. "
									);
							}
						}
						
					}
				});
				
				editDashboardForm.appendToPayload(json);
				json.setSuccess(true);	
			}
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Insufficient permissions to execute action.");
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void createChangeDashboardOwnerForm(JSONResponse json, String ID) {
		
		if(CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
			Dashboard dashboard = CFW.DB.Dashboards.selectByID(Integer.parseInt(ID));
			
			final String NEW_OWNER = "JSON_NEW_OWNER";
			if(dashboard != null) {
				
				CFWForm changeOwnerForm = new CFWForm("cfwChangeDashboardOwnerForm"+ID, "Update Dashboard");
				
				changeOwnerForm.addField(
					CFWField.newTagsSelector(NEW_OWNER)
						.setLabel("New Owner")
						.addAttribute("maxTags", "1")
						.setDescription("Select the new owner of the Dashboard.")
						.addValidator(new NotNullOrEmptyValidator())
						.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
							public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
								return CFW.DB.Users.autocompleteUser(searchValue, this.getMaxResults());					
							}
						})
				);
				
				changeOwnerForm.setFormHandler(new CFWFormHandler() {
					
					@Override
					public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
						
						String newOwnerJson = request.getParameter(NEW_OWNER);
						if(form.mapRequestParameters(request)) {
							LinkedHashMap<String,String> mappedValue = CFW.JSON.fromJsonLinkedHashMap(newOwnerJson);
							String newOwner = mappedValue.keySet().iterator().next();
	
							if(!Strings.isNullOrEmpty(newOwner)) {
								
								new CFWLog(logger).audit("UPDATE", Dashboard.class, "Change owner ID of dashboard from "+dashboard.foreignKeyOwner()+" to "+newOwner);
								
								dashboard.foreignKeyOwner(Integer.parseInt(newOwner));
								
								if(dashboard.update(DashboardFields.FK_ID_USER)) {
									CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Updated!");
								}
							}
						}
					}
				});
				
				changeOwnerForm.appendToPayload(json);
				json.setSuccess(true);	
			}
		}else {
			CFWMessages.noPermission();
		}
	}
}