package com.xresch.cfw.features.dashboard;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIRequestHandler;
import com.xresch.cfw.features.dashboard.Dashboard.DashboardFields;
import com.xresch.cfw.response.JSONResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class APIDashboardExport extends APIDefinition{
	
	private static final String[] inputFieldnames = new String[] {DashboardFields.PK_ID.toString()};
	private static final String[] outputFieldnames = new Dashboard().getFieldnames();
	public APIDashboardExport(String apiName, 
						      String actionName) {
		
		super(Dashboard.class, apiName, actionName, inputFieldnames, outputFieldnames);

		this.setDescription("Export one or all dashboards as a JSON string that can be imported through the import API.");
		
		this.setRequestHandler(new APIRequestHandler() {
			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void handleRequest(HttpServletRequest request, HttpServletResponse response, APIDefinition definition, String bodyContents) {
				
				JSONResponse json = new JSONResponse();
											
				//----------------------------------
				// Create Response
				String id = request.getParameter(DashboardFields.PK_ID.toString());
				json.getContent().append( CFW.DB.Dashboards.getJsonForExport(id) );
				
				json.setSuccess(true);

			}
		});		
	}
}
