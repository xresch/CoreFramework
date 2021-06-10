package com.xresch.cfw.features.manual;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.usermgmt.CFWSessionData;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletManual extends HttpServlet
{

	private static final long serialVersionUID = 1L;

	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {

		if(CFW.Context.Request.hasPermission(FeatureManual.PERMISSION_MANUAL)) {
			
			String action = request.getParameter("action");
			
			if(action == null) {
				HTMLResponse html = new HTMLResponse("Manual");
				StringBuilder content = html.getContent();
	
				//html.addJSFileBottomSingle(new FileDefinition(HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE+".js", "cfw_usermgmt.js"));
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureManual.RESOURCE_PACKAGE, "cfw_manual.js");
				html.addCSSFile(HandlingType.JAR_RESOURCE, FeatureManual.RESOURCE_PACKAGE, "cfw_manual.css");
				
				content.append(CFW.Files.readPackageResource(FeatureManual.RESOURCE_PACKAGE, "cfw_manual.html"));
				
				html.addJavascriptCode("cfw_manual_draw();");
				
		        response.setContentType("text/html");
		        response.setStatus(HttpServletResponse.SC_OK);
			}else {
				handleDataRequest(request, response);
			}
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
		}
        
    }
	
	private void handleDataRequest(HttpServletRequest request, HttpServletResponse response) {
		
		String action = request.getParameter("action");
		String item = request.getParameter("item");
		//String ID = request.getParameter("id");
		//String IDs = request.getParameter("ids");
		//int	userID = CFW.Context.Request.getUser().id();
		
		JSONResponse jsonResponse = new JSONResponse();

		switch(action.toLowerCase()) {
		
			case "fetch": 			
				switch(item.toLowerCase()) {
					case "menuitems": 			CFWSessionData sessionData = CFW.Context.Session.getSessionData();
												jsonResponse.getContent().append(CFW.Registry.Manual.getManualPagesForUserAsJSON(sessionData).toString());
	  											break;
	  				
					case "page": 				String path = request.getParameter("path");
												ManualPage page = CFW.Registry.Manual.getPageByPath(path);
												if(page != null) {
													jsonResponse.getContent().append(page.toJSONObjectWithContent());
												}else {
													CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The page with the path '"+path+"' was not found.");
												}
												break;
												
												
					default: 					CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The value of item '"+item+"' is not supported.");
												break;
				}
				break;
						
			default: 			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The action '"+action+"' is not supported.");
								break;
								
		}
	}
		

}