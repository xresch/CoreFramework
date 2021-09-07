package com.xresch.cfw.features.dashboard;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
	private static final String[] auditLogFieldnames = new String[] { DashboardWidgetFields.PK_ID.toString()};
	
	private static final Logger logger = CFWLog.getLogger(CFWDBDashboardWidget.class.getName());

	// WidgetID and DashboardWidget
	private static Cache<String, DashboardWidget> widgetCache = CFW.Caching.addCache("CFW Widgets", 
			CacheBuilder.newBuilder()
				.initialCapacity(100)
				.maximumSize(10000)
				.expireAfterAccess(65, TimeUnit.MINUTES)
			);
	
				
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
	// CACHING
	//####################################################################################################
	private static void removeFromCache(int id) { removeFromCache(id+""); }
	private static void removeFromCache(String id) { widgetCache.invalidate(id); }
	
	//####################################################################################################
	// CREATE
	//####################################################################################################
	public static boolean 	create(DashboardWidget item) 		{ 
		
		return CFWDBDefaultOperations.create(prechecksCreate, auditLogFieldnames, item);
	}
	public static int 		createGetPrimaryKey(DashboardWidget item) 	{ 
		removeFromCache(item.id());
		return CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreate, auditLogFieldnames, item);
	}
	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(DashboardWidget item) 		{ 
		removeFromCache(item.id());
		return CFWDBDefaultOperations.update(prechecksDeleteUpdate, auditLogFieldnames, item); 
	}
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean 	deleteByID(String id) { 
		return deleteByID(Integer.parseInt(id)); 
	}
	public static boolean 	deleteByID(int id) { 
		
		if(CFWDBDefaultOperations.deleteFirstBy(prechecksDeleteUpdate, auditLogFieldnames, cfwObjectClass, DashboardWidgetFields.PK_ID.toString(), id) ) {
			removeFromCache(id);
			//--------------------------------------------
			//Delete Job Tasks related to this widget
			if( 0 < CFW.DB.Jobs.getCountByCustomInteger(id)) {
				CFW.DB.Jobs.deleteFirstByCustomInteger(id);
			}
			
			return true;
		}
		
		return false;
	}
	

	/***************************************************************
	 * Deletes all widgets for the selected dashboard
	 * 
	 * @return Returns true if successful, false otherwise.
	 ****************************************************************/
	public static boolean deleteWidgetsForDashboard(String dashboardID) {
		
		 ArrayList<Integer> idsToDelete =  new CFWSQL(new DashboardWidget())
				.queryCache()
				.select(DashboardWidgetFields.PK_ID)
				.where(DashboardWidgetFields.FK_ID_DASHBOARD, dashboardID)
				.getAsIntegerArrayList(DashboardWidgetFields.PK_ID);
		
		 boolean success = true;
		 for(Integer id : idsToDelete) {
			 success &= deleteByID(id);
		 }
		 
		 return success;
	}

	//####################################################################################################
	// SELECT
	//####################################################################################################
	
	public static DashboardWidget selectByID(String id) {
		
		try {
			return widgetCache.get(id, new Callable<DashboardWidget>() {
				@Override
				public DashboardWidget call() throws Exception {
					System.out.println("Load from DB");
					return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, DashboardWidgetFields.PK_ID.toString(), id);
				}
			});
		} catch (ExecutionException e) {
			new CFWLog(logger).severe("Error while loading widget from DB or Cache: "+e.getMessage(), e);
		}
		
		return null;
		
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
		
		return new CFWSQL(new DashboardWidget())
				.queryCache()
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
