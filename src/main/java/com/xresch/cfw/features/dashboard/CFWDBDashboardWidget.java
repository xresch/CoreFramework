package com.xresch.cfw.features.dashboard;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.api.FeatureAPI;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.dashboard.DashboardWidget.DashboardWidgetFields;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBDashboardWidget {
	
	private static Class<DashboardWidget> cfwObjectClass = DashboardWidget.class;
	
	private static final Logger logger = CFWLog.getLogger(CFWDBDashboardWidget.class.getName());
		
	//####################################################################################################
	// Preckeck Initialization
	//####################################################################################################
	private static PrecheckHandler prechecksCreate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			DashboardWidget widget = (DashboardWidget)object;
			
			if(widget == null ) {
				new CFWLog(logger)
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
			
			// permmission check will be done in ServletDashboardView.java 
			
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
	public static boolean 	deleteMultipleByID(String itemIDs) 	{ return CFWDBDefaultOperations.deleteMultipleByID(prechecksDeleteUpdate, cfwObjectClass, itemIDs); }
		
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
	 * @return Returns an array with the widgets or an empty list.
	 ****************************************************************/
	public static ArrayList<CFWObject> getWidgetsForDashboard(String dashboardID) {
		
		return new DashboardWidget()
				.queryCache(CFWDBDashboardWidget.class, "getWidgetsForDashboard")
				.select()
				.where(DashboardWidgetFields.FK_ID_DASHBOARD, dashboardID)
				.getAsObjectList();
		
	}
	
	/***************************************************************
	 * Return the widget as JSON string.
	 * 
	 ****************************************************************/
	public static String getWidgetAsJSON(String widgetID) {
		
		return new DashboardWidget()
				.queryCache(CFWDBDashboardWidget.class, "getWidgetAsJSON")
				.select()
				.where(DashboardWidgetFields.PK_ID, widgetID)
				.getAsJSON();
		
	}
	
	/***************************************************************
	 * Return a JSON string for export.
	 * 
	 * @return Returns a JsonArray or null on error.
	 ****************************************************************/
	public static JsonArray getJsonArrayForExport(String dashboardID) {
		
		if(CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)
		|| CFW.Context.Request.hasPermission(FeatureAPI.PERMISSION_CFW_API)
		|| CFW.DB.Dashboards.checkCanEdit(dashboardID)) {
			CFWSQL selectForExport = new DashboardWidget()
				.queryCache(CFWDBDashboardWidget.class, "getJsonArrayForExport")
				.select();
			
			if(!Strings.isNullOrEmpty(dashboardID)) {
				selectForExport.where(DashboardWidgetFields.FK_ID_DASHBOARD, dashboardID);
				return  selectForExport.getObjectsAsJSONArray();
			}
							
			return null;
		 
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
			return null;
		}
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	public static AutocompleteResult autocompleteWidget(String dashboardID, String searchValue, int maxResults) {
		
		if(Strings.isNullOrEmpty(searchValue)) {
			return null;
		}
		
		if(Strings.isNullOrEmpty(dashboardID)) {
			CFW.Context.Request.addAlertMessage(MessageType.INFO, "Please select a dashboard first.");
		}
		
		return new DashboardWidget()
			.queryCache(CFWDBDashboardWidget.class, "autocompleteWidget")
			.select(DashboardWidgetFields.PK_ID,
					DashboardWidgetFields.TITLE)
			.whereLike(DashboardWidgetFields.TITLE, "%"+searchValue+"%")
			.and(DashboardWidgetFields.FK_ID_DASHBOARD, dashboardID)
			.limit(maxResults)
			.getAsAutocompleteResult(DashboardWidgetFields.PK_ID.toString(), 
					DashboardWidgetFields.TITLE);
		
	}

}
