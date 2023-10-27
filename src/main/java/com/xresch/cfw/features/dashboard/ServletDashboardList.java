package com.xresch.cfw.features.dashboard;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
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
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.dashboard.Dashboard.DashboardFields;
import com.xresch.cfw.features.notifications.Notification;
import com.xresch.cfw.features.parameter.CFWParameter;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
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

	private static final String MESSAGE_SHARED_GLOBAL = "All dashboard users will see this dashboard. The dashboard was saved as shared and no specific shared users or roles. ";

	private static final String MESSAGE_NOT_SHARED = "Users won't be able to access your dashboard until you set shared to true. The dashboard was saved as not shared and with at least one shared users or roles. ";

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
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "cfw_dashboard_common.js");
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
	  											
					case "faveddashboards": 	jsonResponse.getContent().append(CFW.DB.Dashboards.getFavedDashboardListAsJSON());
												break;
	  											
					case "shareddashboards": 	jsonResponse.getContent().append(CFW.DB.Dashboards.getSharedDashboardListAsJSON());
												break;	
												
					case "admindashboards": 	jsonResponse.getContent().append(CFW.DB.Dashboards.getAdminDashboardListAsJSON());
												break;	
												
					case "dashboardstats": 		String timeframeString = request.getParameter("timeframe");
												CFWTimeframe time = new CFWTimeframe(timeframeString);
												jsonResponse.setPayload(CFW.DB.Dashboards.getEAVStats(ID, time.getEarliest(), time.getLatest()));
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
				
			case "update": 			
				switch(item.toLowerCase()) {

					case "favorite": 	Integer userID = CFW.Context.Request.getUserID();
										String dashboardID = request.getParameter("listitemid");
										jsonResponse.setSuccess(CFW.DB.DashboardFavorites.toogleDashboardInUserFavs(dashboardID, ""+userID));
										break;
										
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;	
				
			case "delete": 			
				switch(item.toLowerCase()) {
				
				case "dashboard": 	deleteDashboard(jsonResponse, ID);
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
	private void deleteDashboard(JSONResponse jsonResponse, String ID) {
		// TODO Auto-generated method stub
		if(CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
			jsonResponse.setSuccess(CFW.DB.Dashboards.deleteByID(ID));
		}else {
			jsonResponse.setSuccess(CFW.DB.Dashboards.deleteByIDForCurrentUser(ID));
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
			duplicate.timeCreated( new Timestamp(new Date().getTime()) );
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
				ArrayList<DashboardWidget> widgetList = CFW.DB.DashboardWidgets.getWidgetsForDashboard(dashboardID);
				
				boolean success = true;
				for(DashboardWidget widgetToCopy : widgetList) {
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
				ArrayList<CFWObject> parameterList = CFW.DB.Parameters.getParametersForDashboard(dashboardID);
				
				for(CFWObject object : parameterList) {
					CFWParameter paramToCopy = (CFWParameter)object;
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
			
			Dashboard newBoard = new Dashboard();
			newBoard.updateSelectorFields();
			
			CFWForm createDashboardForm = newBoard.toForm("cfwCreateDashboardForm", "{!cfw_dashboard_create!}");
			
			createDashboardForm.setFormHandler(new CFWFormHandler() {
				
				@Override
				public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
									
					if(origin != null) {
						
						origin.mapRequestParameters(request);
						Dashboard dashboard = (Dashboard)origin;
						dashboard.foreignKeyOwner(CFW.Context.Request.getUser().id());
						
						Integer newID = CFW.DB.Dashboards.createGetPrimaryKey(dashboard);
						
						if( newID != null ) {
							dashboard.id(newID);
							CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Dashboard created successfully!");
							generateSharedMessages(dashboard);
							
							dashboard.saveSelectorFields();
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
				
				dashboard.updateSelectorFields();
				
				if(!CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_CREATOR_PUBLIC)
				&& !CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
					dashboard.getField(DashboardFields.IS_PUBLIC.toString()).isDisabled(true);
				}
				
				CFWForm editDashboardForm = dashboard.toForm("cfwEditDashboardForm"+ID, "Update Dashboard");

				editDashboardForm.setFormHandler(new CFWFormHandler() {
					
					@Override
					public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
						
						Dashboard dashboard = (Dashboard)origin;
						if(origin.mapRequestParameters(request) 
						&& CFW.DB.Dashboards.update(dashboard)) {
							
							
							CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Updated!");
							
							generateSharedMessages(dashboard);
							
							dashboard.saveSelectorFields();
							
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
							public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
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
								
								new CFWLog(logger).audit(CFWAuditLogAction.UPDATE, Dashboard.class, "Change owner ID of dashboard from "+dashboard.foreignKeyOwner()+" to "+newOwner);
								
								Integer oldOwner = dashboard.foreignKeyOwner();
								dashboard.foreignKeyOwner(Integer.parseInt(newOwner));
								
								if(dashboard.update(DashboardFields.FK_ID_USER)) {
									CFW.Context.Request.addAlertMessage(MessageType.SUCCESS, "Updated!");
									
									User currentUser = CFW.Context.Request.getUser();
									//----------------------------------
									// Send Notification to New Owner
									Notification newOwnerNotification = 
											new Notification()
													.foreignKeyUser(Integer.parseInt(newOwner))
													.messageType(MessageType.INFO)
													.title("Dashboard assigned to you: '"+dashboard.name()+"'")
													.message("The user '"+currentUser.createUserLabel()+"' has assigned the dashboard to you. You are now the new owner of the dashboard.");

									CFW.DB.Notifications.create(newOwnerNotification);
									
									//----------------------------------
									// Send Notification to Old Owner
									User user = CFW.DB.Users.selectByID(newOwner);
									Notification oldOwnerNotification = 
											new Notification()
													.foreignKeyUser(oldOwner)
													.messageType(MessageType.INFO)
													.title("Owner of dashboard '"+dashboard.name()+"' is now "+user.createUserLabel())
													.message("The user '"+currentUser.createUserLabel()+"' has changed the owner of your former dashboard to the user '"+user.createUserLabel()+"'. ");

									CFW.DB.Notifications.create(oldOwnerNotification);
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
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void generateSharedMessages(Dashboard dashboard) {
		if(!dashboard.isShared()
		&& (dashboard.sharedWithUsers().size() > 0
		   || dashboard.sharedWithGroups().size() > 0
		   || dashboard.editors().size() > 0
		   || dashboard.editorGroups().size() > 0
			)
		) {
			
			CFW.Context.Request.addAlertMessage(
					MessageType.INFO, 
					MESSAGE_NOT_SHARED
				);
		}
		
		if(dashboard.isShared()
		&& dashboard.sharedWithUsers().size() == 0
		&& dashboard.sharedWithGroups().size() == 0
		&& dashboard.editors().size() == 0
		&& dashboard.editorGroups().size() == 0) {
					
			CFW.Context.Request.addAlertMessage(
					MessageType.INFO, 
					MESSAGE_SHARED_GLOBAL
				);
		}
	}
}