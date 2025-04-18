package com.xresch.cfw.features.analytics;

import java.io.IOException;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletCPUSampling extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	private static String EARLIEST = "EARLIEST";
	private static String LATEST = "LATEST";
	
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {

		if(CFW.Context.Request.hasPermission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS)) {
			
			String action = request.getParameter("action");
			
			if(action == null) {
			HTMLResponse html = new HTMLResponse("CPU Sampling");
			StringBuilder content = html.getContent();

			//html.addJSFileBottomSingle(new FileDefinition(HandlingType.JAR_RESOURCE, FeatureCore.RESOURCE_PACKAGE+".js", "cfw_usermgmt.js"));
			html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureSystemAnalytics.RESOURCE_PACKAGE, "cfw_cpusampling.js");
			
			//------------------------------
			// Add Content
			content.append(CFW.Files.readPackageResource(FeatureSystemAnalytics.RESOURCE_PACKAGE, "cfw_cpusampling.html"));
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
			CFWMessages.accessDenied();
		}
        
    }
	
	private void handleDataRequest(HttpServletRequest request, HttpServletResponse response) {
		
		String action = request.getParameter("action");
		String item = request.getParameter("item");
		
		JSONResponse jsonResponse = new JSONResponse();
		
		switch(action.toLowerCase()) {
		
			case "fetch": 			
				switch(item.toLowerCase()) {
					case "cpusampling": 		getCPUSampling(jsonResponse, request);
	  											break;
	  										
					default: 					CFW.Messages.itemNotSupported(item);
												break;
				}
				break;
						
			default: 			CFW.Messages.actionNotSupported(action);
								break;
								
		}
	}
		
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	private void getCPUSampling(JSONResponse jsonResponse, HttpServletRequest request) {
		
		if( CFW.Context.Request.hasPermission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS) ) {

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
			CFW.Messages.addErrorMessage("Access to statistics denied.");
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