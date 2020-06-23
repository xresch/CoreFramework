package com.pengtoolbox.cfw.features.cpusampling;

import java.io.IOException;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.caching.FileDefinition.HandlingType;
import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.features.core.FeatureCore;
import com.pengtoolbox.cfw.response.HTMLResponse;
import com.pengtoolbox.cfw.response.JSONResponse;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage.MessageType;
import com.pengtoolbox.cfw.validation.NotNullOrEmptyValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class ServletCPUSampling extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	private static String EARLIEST = "EARLIEST";
	private static String LATEST = "LATEST";
	
	public ServletCPUSampling() {
	
	}
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {

		if(CFW.Context.Request.hasPermission(FeatureCore.PERMISSION_APP_ANALYTICS)) {
			
			String action = request.getParameter("action");
			
			if(action == null) {
			HTMLResponse html = new HTMLResponse("CPU Sampling");
			StringBuffer content = html.getContent();

			//html.addJSFileBottomSingle(new FileDefinition(HandlingType.JAR_RESOURCE, FileDefinition.CFW_JAR_RESOURCES_PATH+".js", "cfw_usermgmt.js"));
			html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureCPUSampling.RESOURCE_PACKAGE, "cfw_cpusampling.js");
			
			//------------------------------
			// Add Content
			content.append(CFW.Files.readPackageResource(FeatureCPUSampling.RESOURCE_PACKAGE, "cfw_cpusampling.html"));
			content.append(new TimeInputs().toForm("cfwCPUSamplingTimeInputs", "Load")
					.onclick("fetchAndRenderForSelectedTimeframe();")
					.getHTML() 
				);		
			
			html.addJavascriptCode("cfw_cpusampling_draw({tab: 'latest'});");
			
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
					case "cpusampling": 		getCPUSampling(jsonResponse, request);
	  											break;
	  										
					default: 					CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The value of item '"+item+"' is not supported.");
												break;
				}
				break;
						
			default: 			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The action '"+action+"' is not supported.");
								break;
								
		}
	}
		
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	private void getCPUSampling(JSONResponse jsonResponse, HttpServletRequest request) {
		
		if( CFW.Context.Request.hasPermission(FeatureCore.PERMISSION_APP_ANALYTICS) ) {

			//--------------------------
			// Get Input and Validate
			TimeInputs times = new TimeInputs();
			if(!times.mapRequestParameters(request)) {
				return;
			}
			
			//--------------------------
			// Fetch Data
			String signatures = CFWDBCPUSampleSignature.getSignatureListAsJSON();
			String timeseries =  CFWDBCPUSample.getForTimeframeAsJSON(times.getEarliest(), times.getLatest());

			jsonResponse.getContent()
				.append("{\"signatures\": ").append(signatures)
				.append(",\"timeseries\": ").append(timeseries)
				.append("}");
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Access to statistics denied.");
		}
		
	}
	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public class TimeInputs extends CFWObject{
		
		public CFWField<Timestamp> earliest = CFWField.newTimestamp(FormFieldType.DATETIMEPICKER, EARLIEST)
				.setDescription("The earliest time fetched for the CPU sample.")
				.setValue(new Timestamp(System.currentTimeMillis() - 1000 * 3600))
				.addValidator(new NotNullOrEmptyValidator());
		
		public CFWField<Timestamp> latest = CFWField.newTimestamp(FormFieldType.DATETIMEPICKER, LATEST)
				.setDescription("The latest time fetched for the CPU sample.")
				.setValue(new Timestamp(System.currentTimeMillis()))
				.addValidator(new NotNullOrEmptyValidator());
		
		public TimeInputs() {
			this.addFields(earliest, latest);
		}
		
		public Timestamp getEarliest() {
			return this.earliest.getValue();
		}
		
		public Timestamp getLatest() {
			return this.latest.getValue();
		}
	}
	
}