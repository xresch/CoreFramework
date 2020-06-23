package com.pengtoolbox.cfw.features.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;
import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.caching.FileDefinition.HandlingType;
import com.pengtoolbox.cfw.datahandling.CFWForm;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.features.usermgmt.User;
import com.pengtoolbox.cfw.response.HTMLResponse;
import com.pengtoolbox.cfw.response.JSONResponse;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class ServletDashboardView extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	public ServletDashboardView() {
	
	}
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {

		if(CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_VIEWER)
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_CREATOR)
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
			
			String action = request.getParameter("action");
			
			if(action == null) {
				HTMLResponse html = new HTMLResponse("Dashboard");
				StringBuffer content = html.getContent();
				
				html.addCSSFile(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "gridstack.min.css");
				html.addCSSFile(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "cfw_dashboard.css");
				
				//html.addJSFileBottomSingle(new FileDefinition(HandlingType.JAR_RESOURCE, FileDefinition.CFW_JAR_RESOURCES_PATH+".js", "cfw_usermgmt.js"));
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "gridstack.all.js");
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "cfw_dashboard.js");
				
				content.append(CFW.Files.readPackageResource(FeatureDashboard.PACKAGE_RESOURCES, "cfw_dashboard.html"));
				
				//--------------------------------------
				// Add widget CSS and JS files based on
				// user permissions
				CFW.Registry.Widgets.addFilesToResponse(html);
				
				Dashboard dashboard = CFW.DB.Dashboards.selectByID(request.getParameter("id"));
				html.setPageTitle(dashboard.name());
				html.addJavascriptData("dashboardName",  dashboard.name());
				html.addJavascriptData("canEdit", canEdit(request.getParameter("id")) );
				html.addJavascriptCode("cfw_dashboard_initialDraw();");
				
		        response.setContentType("text/html");
		        response.setStatus(HttpServletResponse.SC_OK);
			}else {
				handleDataRequest(request, response);
			}
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
		}
        
    }
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {

		if(CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_VIEWER)
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_CREATOR)
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
			handleDataRequest(request, response);
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
		}
        
    }
	
	private void handleDataRequest(HttpServletRequest request, HttpServletResponse response) {
		
		String action = request.getParameter("action");
		String item = request.getParameter("item");
		String type = request.getParameter("type");
		String dashboardID = request.getParameter("dashboardid");
		//String ID = request.getParameter("id");
		//String IDs = request.getParameter("ids");
		//int	userID = CFW.Context.Request.getUser().id();
		
		JSONResponse jsonResponse = new JSONResponse();

		switch(action.toLowerCase()) {
		
			case "fetch": 			
				switch(item.toLowerCase()) {
					case "widgets": 			fetchWidgets(jsonResponse, dashboardID);
	  											break;	
					case "widgetdata": 			getWidgetData(request, response, jsonResponse);
												break;	
												
					case "settingsform": 		getSettingsForm(request, response, jsonResponse);
												break;	
												
					default: 					CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The value of item '"+item+"' is not supported.");
												break;
				}
				break;
			case "create": 			
				switch(item.toLowerCase()) {
					case "widget": 				createWidget(jsonResponse, type, dashboardID);
	  											break;
	  																
					default: 					CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The value of item '"+item+"' is not supported.");
												break;
				}
				break;	
				
			case "update": 			
				switch(item.toLowerCase()) {
					case "widget": 				updateWidget(request, response, jsonResponse);
	  											break;
	  																
					default: 					CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The value of item '"+item+"' is not supported.");
												break;
				}
				break;
				
			case "delete": 			
				switch(item.toLowerCase()) {
					case "widget": 				deleteWidget(request, response, jsonResponse);
	  											break;
	  																
					default: 					CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The value of item '"+item+"' is not supported.");
												break;
				}
				break;	
				
			default: 			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The action '"+action+"' is not supported.");
								break;
								
		}
	}
	
	private void fetchWidgets(JSONResponse response, String dashboardID) {
		
		Dashboard dashboard = CFW.DB.Dashboards.selectByID(dashboardID);
		
		if(dashboard.isShared() || canEdit(dashboardID)) {
			
			response.getContent().append(CFW.DB.DashboardWidgets.getWidgetsForDashboardAsJSON(dashboardID));
			
		}else{
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Insufficient rights to view this dashboard.");
		}
	}
		
	private void createWidget(JSONResponse response, String type, String dashboardID) {

		if(canEdit(dashboardID)) {
			
			//----------------------------
			// Create Widget
			DashboardWidget newWidget = new DashboardWidget();
			newWidget.type(type);
			newWidget.foreignKeyDashboard(Integer.parseInt(dashboardID));
			
			WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(type);
			if(definition != null) {
				newWidget.settings(definition.getSettings().toJSON());
			}
			
			int id = CFW.DB.DashboardWidgets.createGetPrimaryKey(newWidget);
			newWidget.id(id);
			
			response.getContent().append(CFW.JSON.toJSON(newWidget));
			
		}else{
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Insufficient rights to execute action.");
		}

	}
	
	private void updateWidget(HttpServletRequest request, HttpServletResponse response, JSONResponse json) {
		
		String dashboardID = request.getParameter("FK_ID_DASHBOARD");

		if(canEdit(dashboardID)) {
			//----------------------------
			// Get Values
			String widgetType = request.getParameter("TYPE");
			String JSON_SETTINGS = request.getParameter("JSON_SETTINGS");
			JsonObject jsonObject = CFW.JSON.fromJson(JSON_SETTINGS);
			WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(widgetType);
			
			//----------------------------
			// Validate
			CFWObject settings = definition.getSettings();
			
			boolean isValid = settings.mapJsonFields(jsonObject);
			
			if(isValid) {
				DashboardWidget widgetToUpdate = new DashboardWidget();
				
				// check if default settings are valid
				if(widgetToUpdate.mapRequestParameters(request)) {
					//Use sanitized values
					widgetToUpdate.settings(settings.toJSON());
					CFW.DB.DashboardWidgets.update(widgetToUpdate);
				}
			}
			
		}else{
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Insufficient rights to execute action.");
		}

	}
		
	private void deleteWidget(HttpServletRequest request, HttpServletResponse response, JSONResponse json) {
		
		String dashboardID = request.getParameter("dashboardid");
		
		if(canEdit(dashboardID)) {
			
			String widgetID = request.getParameter("widgetid");
			
			boolean success = CFW.DB.DashboardWidgets.deleteByID(widgetID);
			
			json.setSuccess(success);
		}else{
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Insufficient rights to execute action.");
		}

	}
	
	
	private void getSettingsForm(HttpServletRequest request, HttpServletResponse response, JSONResponse json) {
		
		String dashboardID = request.getParameter("FK_ID_DASHBOARD");

		if(canEdit(dashboardID)) {
			
			//----------------------------
			// Get Values
			String widgetType = request.getParameter("TYPE");
			String JSON_SETTINGS = request.getParameter("JSON_SETTINGS");
			JsonObject jsonObject = CFW.JSON.fromJson(JSON_SETTINGS);
			WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(widgetType);
			
			//----------------------------
			// Create Form
			CFWObject settings = definition.getSettings();
			settings.mapJsonFields(jsonObject);
			
			CFWForm form = settings.toForm("cfwWidgetFormSettings"+CFW.Security.createRandomStringAtoZ(6), "n/a-willBeRemoved");
			
			form.appendToPayload(json);
			
		}else{
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Insufficient rights to execute action.");
		}

	}
	
	private void getWidgetData(HttpServletRequest request, HttpServletResponse response, JSONResponse jsonResponse) {
		
		//----------------------------
		// Get Values
		String widgetType = request.getParameter("TYPE");
		String JSON_SETTINGS = request.getParameter("JSON_SETTINGS");
		JsonObject jsonSettingsObject = CFW.JSON.fromJson(JSON_SETTINGS);
		WidgetDefinition definition = CFW.Registry.Widgets.getDefinition(widgetType);
		
		//----------------------------
		// Create Response
		definition.fetchData(jsonResponse, jsonSettingsObject);
					
	}
	
	private boolean canEdit(String dashboardID) {
		
		Dashboard dashboard = CFW.DB.Dashboards.selectByID(dashboardID);
		User user = CFW.Context.Request.getUser();
		
		if( dashboard.foreignKeyOwner().equals(user.id())
		|| ( dashboard.editors() != null && dashboard.editors().containsKey(user.id().toString()) )
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
			return true;
		}else {
			return false;
		}
	}
}