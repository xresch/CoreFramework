package com.pengtoolbox.cfw.features.dashboard;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.db.CFWDBDefaultOperations;
import com.pengtoolbox.cfw.db.CFWSQL;
import com.pengtoolbox.cfw.db.PrecheckHandler;
import com.pengtoolbox.cfw.features.api.FeatureAPI;
import com.pengtoolbox.cfw.features.dashboard.Dashboard.DashboardFields;
import com.pengtoolbox.cfw.features.dashboard.DashboardWidget.DashboardWidgetFields;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWDBDashboardWidget {
	
	private static Class<DashboardWidget> cfwObjectClass = DashboardWidget.class;
	
	public static Logger logger = CFWLog.getLogger(CFWDBDashboardWidget.class.getName());
		
	//####################################################################################################
	// Preckeck Initialization
	//####################################################################################################
	private static PrecheckHandler prechecksCreate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			DashboardWidget widget = (DashboardWidget)object;
			
			if(widget == null ) {
				new CFWLog(logger)
					.method("doCheck")
					.warn("The widget cannot be null.", new Throwable());
				return false;
			}

			return true;
		}
	};
	
	
	private static PrecheckHandler prechecksDeleteUpdate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			DashboardWidget widget = (DashboardWidget)object;
			
			if(widget == null) {
				return false;
			}
			
			// permmision check will be done in ServletDashboardView.java 
//			if(isWidgetOfCurrentUser(widget) == false
//			&& !CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
//				new CFWLog(logger)
//				.method("doCheck")
//				.severe("You are not allowed to modify this dashboard", new Throwable());
//				return false;
//			}
			
			return true;
		}
	};
		
	//####################################################################################################
	// CREATE
	//####################################################################################################
	public static boolean	create(DashboardWidget... items) 	{ return CFWDBDefaultOperations.create(prechecksCreate, items); }
	public static boolean 	create(DashboardWidget item) 		{ return CFWDBDefaultOperations.create(prechecksCreate, item);}
	public static int 		createGetPrimaryKey(DashboardWidget item) 	{ return CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreate, item);}
	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(DashboardWidget... items) 	{ return CFWDBDefaultOperations.update(prechecksDeleteUpdate, items); }
	public static boolean 	update(DashboardWidget item) 		{ return CFWDBDefaultOperations.update(prechecksDeleteUpdate, item); }
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean 	deleteByID(String id) 				{ return CFWDBDefaultOperations.deleteFirstBy(prechecksDeleteUpdate, cfwObjectClass, DashboardWidgetFields.PK_ID.toString(), Integer.parseInt(id)); }
	public static boolean 	deleteByID(int id) 					{ return CFWDBDefaultOperations.deleteFirstBy(prechecksDeleteUpdate, cfwObjectClass, DashboardWidgetFields.PK_ID.toString(), id); }
	public static boolean 	deleteMultipleByID(String itemIDs) 	{ return CFWDBDefaultOperations.deleteMultipleByID(cfwObjectClass, itemIDs); }
		
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static DashboardWidget selectByID(int id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, DashboardWidgetFields.PK_ID.toString(), id);
	}
		
	/***************************************************************
	 * Return a list of all user widgets
	 * 
	 * @return Returns a resultSet with all widgets or null.
	 ****************************************************************/
	public static String getWidgetsForDashboardAsJSON(String dashboardID) {
		
		return new DashboardWidget()
				.queryCache(CFWDBDashboardWidget.class, "getWidgetsForDashboardAsJSON")
				.select()
				.where(DashboardWidgetFields.FK_ID_DASHBOARD, dashboardID)
				.getAsJSON();
		
	}
	
	/***************************************************************
	 * Return a list of all user widgets
	 * 
	 * @return Returns a resultSet with all widgets or null.
	 ****************************************************************/
	public static ArrayList<CFWObject> getWidgetsForDashboard(String dashboardID) {
		
		return new DashboardWidget()
				.queryCache(CFWDBDashboardWidget.class, "getWidgetsForDashboard")
				.select()
				.where(DashboardWidgetFields.FK_ID_DASHBOARD, dashboardID)
				.getAsObjectList();
		
	}
	
	/***************************************************************
	 * Return a JSON string for export.
	 * 
	 * @return Returns a JsonArray or null on error.
	 ****************************************************************/
	public static JsonArray getJsonArrayForExport(String dashboardID) {
		
		if(CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)
		|| CFW.Context.Request.hasPermission(FeatureAPI.PERMISSION_CFW_API)) {
			CFWSQL selectForExport = new DashboardWidget()
				.queryCache(CFWDBDashboardWidget.class, "getJsonArrayForExport")
				.select();
			
			if(!Strings.isNullOrEmpty(dashboardID)) {
				selectForExport.where(DashboardWidgetFields.FK_ID_DASHBOARD, dashboardID);
				return  selectForExport.getAsJSONArray();
			}
							
			return null;
		 
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
			return null;
		}
	}

}
