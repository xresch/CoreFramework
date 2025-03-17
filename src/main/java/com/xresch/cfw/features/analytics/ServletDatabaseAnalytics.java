package com.xresch.cfw.features.analytics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogAction;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemAlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletDatabaseAnalytics extends HttpServlet
{
	private static final Logger logger = CFWLog.getLogger(ServletDatabaseAnalytics.class.getName());
	private static final long serialVersionUID = 1L;

	/********************************************************************************
	 *
	 ********************************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {

		if(CFW.Context.Request.hasPermission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS)) {
			
			String action = request.getParameter("action");
			
			if(action == null) {
			HTMLResponse html = new HTMLResponse("Database Analytics");

			html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureSystemAnalytics.RESOURCE_PACKAGE, "cfw_dbanalytics.js");
			
			html.addJavascriptCode("cfw_dbanalytics_draw();");
			
	        response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_OK);
			}else {
				handleActionRequest(request, response);
			}
		}else {
			CFWMessages.accessDenied();
		}
        
    }
	
	/********************************************************************************
	 *
	 ********************************************************************************/
	private void handleActionRequest(HttpServletRequest request, HttpServletResponse response) {
		
		String action = request.getParameter("action");
		String item = request.getParameter("item");
		
		JSONResponse jsonResponse = new JSONResponse();
		
		switch(action.toLowerCase()) {
			case "dbsnapshot":		
					new CFWLog(logger).audit(CFWAuditLogAction.CREATE, "DatabaseSnapshot", "Request manual database snapshot.");
					boolean isSuccess = CFW.DB.backupDatabaseFile("./snapshot", "h2_database_snapshot");
					if(isSuccess) {
						CFW.Messages.addSuccessMessage("Snapshot created on hard disk under {APP_RUN_DIR}/snapshot.");
					}else {
						CFW.Messages.addErrorMessage("Error while creating snapshot.");
					}
				break;
					
				
					
			case "exportscript":	
					new CFWLog(logger).audit(CFWAuditLogAction.EXPORT, "DatabaseScript", "Request export of database data to script.");
					boolean isExportSuccess = CFW.DB.exportScript("./snapshot", "h2database_exported_script");
					if(isExportSuccess) {
						CFW.Messages.addSuccessMessage("Snapshot created on hard disk under {APP_RUN_DIR}/snapshot.");
					}else {
						CFW.Messages.addErrorMessage("Error while creating snapshot.");
					}
				break;
				
				
			case "importscript":	
				new CFWLog(logger).audit(CFWAuditLogAction.IMPORT, "DatabaseScript", "Import database script.");
				String scriptFilePath = request.getParameter("filepath");
				boolean isImportSuccess = CFW.DB.importScript(scriptFilePath);
				if(isImportSuccess) {
					CFW.Messages.addSuccessMessage("Import successful!");
				}else {
					CFW.Messages.addErrorMessage("Error occured while importing script.");
				}
				break;
									
				
				
			case "reindexfulltextsearch":	
					new CFWLog(logger).audit(CFWAuditLogAction.RESET, "FulltextSearch", "Fulltext search will be reindexed. Existing indexes are dropped and newly created.");
					this.reindexFulltextSearch();
				break;
											
											
			case "fetch": 			
				switch(item.toLowerCase()) {
					case "tablerowcount": 		jsonResponse.getContent().append(CFW.DB.selectTableRowCountAsJSON());
	  											break;
	  											
					case "querystatistics":		jsonResponse.getContent().append(CFW.DB.selectQueryStatisticsAsJSON());
												break;		
	  				
					case "connectionpoolstats":	jsonResponse.getContent().append(DBInterface.getConnectionPoolStatsAsJSON());
												break;
												
					default: 					CFW.Messages.itemNotSupported(item);
												break;
				}
				break;
						
			default: 			CFW.Messages.actionNotSupported(action);
								break;
								
		}
	}
	
	/********************************************************************************
	 *
	 ********************************************************************************/
	private void reindexFulltextSearch() {

		ArrayList<CFWObject> objects = CFW.Registry.Objects.getCFWObjectInstances();
		for(CFWObject object : objects) {
			if(object.hasFulltextSearch()) {
				new CFWSQL(object).fulltextsearchReindex();
			}
		}
		// does not properly work
		// not really neccessary, as errors will be thrown by fulltextsearchReindex()
		//return success;
	}
		
	
}