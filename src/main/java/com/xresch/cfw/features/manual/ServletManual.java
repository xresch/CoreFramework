package com.xresch.cfw.features.manual;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.usermgmt.CFWSessionData;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletManual extends HttpServlet
{

	private static final long serialVersionUID = 1L;

	
	/*********************************************************************************************
	 *
	 *********************************************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {

		if(CFW.Context.Request.hasPermission(FeatureManual.PERMISSION_MANUAL)) {
			
			String action = request.getParameter("action");
			
			if(action == null) {
				HTMLResponse html = new HTMLResponse("Manual");
				StringBuilder content = html.getContent();
	
				//html.addJSFileBottomSingle(new FileDefinition(HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE+".js", "cfw_usermgmt.js"));
				html.addCSSFile(HandlingType.JAR_RESOURCE, FeatureManual.PACKAGE_RESOURCES, "cfw_manual.css");
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureManual.PACKAGE_RESOURCES, "cfw_manual_common.js");
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureManual.PACKAGE_RESOURCES, "cfw_manual.js");
				
				content.append(CFW.Files.readPackageResource(FeatureManual.PACKAGE_RESOURCES, "cfw_manual.html"));
				
				html.addJavascriptCode("cfw_manual_draw();");
				
		        response.setContentType("text/html");
		        response.setStatus(HttpServletResponse.SC_OK);
			}else {
				handleDataRequest(request, response);
			}
		}else {
			CFW.Messages.addErrorMessage(CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
		}
        
    }
	
	/*********************************************************************************************
	 *
	 *********************************************************************************************/
	private void handleDataRequest(HttpServletRequest request, HttpServletResponse response) {
		
		String action = request.getParameter("action");
		String item = request.getParameter("item");

		
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
													CFW.Messages.addErrorMessage("The page with the path '"+path+"' was not found.");
												}
												break;
												
												
					default: 					CFW.Messages.addErrorMessage("The value of item '"+item+"' is not supported.");
												break;
				}
				break;
				
			case "search": 			
				switch(item.toLowerCase()) {
					case "page": 		searchManual(request, jsonResponse);
					break;
									
					
					default: 			CFW.Messages.addErrorMessage("The value of item '"+item+"' is not supported.");
					break;
				}
				break;
						
			default: 			CFW.Messages.addErrorMessage("The action '"+action+"' is not supported.");
								break;
								
		}
	}
		
	/*********************************************************************************************
	 *
	 *********************************************************************************************/
	private void searchManual(HttpServletRequest request, JSONResponse jsonResponse) {
		String query = request.getParameter("query");
		
		HashMap<ManualPage, String> searchResults = ManualSearchEngine.searchManual(query);
		
		JsonArray jsonArray = new JsonArray();
		int count = 0;
		for(Entry<ManualPage, String> entry : searchResults.entrySet()) {
			
			ManualPage page = entry.getKey();
			String resultSnipped = entry.getValue();
			
			JsonObject object = new JsonObject();
			object.addProperty("title", page.getLabel() );
			object.addProperty("path", page.resolvePath(null) );
			object.addProperty("snippet", resultSnipped );
			
			jsonArray.add(object);
			
			count++;
			if(count >= 100) {
				break;
			}
		}
		
		jsonResponse.setPayload(jsonArray);
	}
}