package com.xresch.cfw.features.dashboard;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWField.CFWFieldFlag;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.api.FeatureAPI;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.dashboard.DashboardWidget.DashboardWidgetFields;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBDashboardWidget {
	
	private static Class<DashboardWidget> cfwObjectClass = DashboardWidget.class;
	private static final String[] auditLogFieldnames = new String[] { 
			DashboardWidgetFields.PK_ID.toString()
		  , DashboardWidgetFields.FK_ID_DASHBOARD.toString()
		  , DashboardWidgetFields.TYPE.toString()
		  , DashboardWidgetFields.TITLE.toString()
			};
	
	private static final Logger logger = CFWLog.getLogger(CFWDBDashboardWidget.class.getName());

	// WidgetID and DashboardWidget
	private static Cache<Integer, DashboardWidget> widgetCache = CFW.Caching.addCache("CFW Widget", 
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
	private static void removeFromCache(int id) { widgetCache.invalidate(id);  }
	
	//####################################################################################################
	// CREATE
	//####################################################################################################
	public static boolean 	create(DashboardWidget item) 		{ 
		return CFWDBDefaultOperations.create(prechecksCreate, auditLogFieldnames, item);
	}
	public static int createGetPrimaryKey(DashboardWidget item) 	{ 
		
		Integer primaryKey = null;
		
		CFW.DB.transactionStart();
			
			primaryKey = CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreate, auditLogFieldnames, item);
			boolean success =  (primaryKey != null); 
			success &= CFW.DB.Dashboards.updateLastUpdated(item.foreignKeyDashboard());
			
		CFW.DB.transactionEnd(success);
		
		return primaryKey;
	}
	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean update(DashboardWidget item) 		{ 
		removeFromCache(item.id());
		
		CFW.DB.transactionStart();
		
			boolean success =  CFWDBDefaultOperations.update(prechecksDeleteUpdate, auditLogFieldnames, item); 
			success &= CFW.DB.Dashboards.updateLastUpdated(item.foreignKeyDashboard());
			
		CFW.DB.transactionEnd(success);
		
		return success;
	}
	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean updateWithout(DashboardWidget item, String... fieldnames) 		{ 
		removeFromCache(item.id());
		
		CFW.DB.transactionStart();
		
			boolean success =  CFWDBDefaultOperations.updateWithout(prechecksDeleteUpdate, auditLogFieldnames, item, fieldnames); 
			success &= CFW.DB.Dashboards.updateLastUpdated(item.foreignKeyDashboard());
			
		CFW.DB.transactionEnd(success);
		
		return success; 
	}
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean deleteByID(String id) { 
		return deleteByID(Integer.parseInt(id)); 
	}
	
	
	public static boolean deleteByID(int id) { 
		
		if(CFWDBDefaultOperations.deleteFirstBy(prechecksDeleteUpdate, auditLogFieldnames, cfwObjectClass, DashboardWidgetFields.PK_ID.toString(), id) ) {
			removeFromCache(id);
			return deleteJobsForWidget(id);
			
		}
		
		return false;
	}
	
	/***************************************************************
	 * Deletes all widgets for the selected dashboard
	 * 
	 * @return Returns true if successful, false otherwise.
	 ****************************************************************/
	public static boolean deleteJobsForWidget(int id) {
		
		if( 0 < CFW.DB.Jobs.getCountByCustomInteger(id)) {
			return CFW.DB.Jobs.deleteFirstByCustomInteger(id);
		}
		
		return true;
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
		return selectByID(Integer.parseInt(id));
	}
	
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static DashboardWidget selectByID(int id) {
		
		try {
			// cache to avoid overloading backend systems.
			DashboardWidget widget = widgetCache.get(id, new Callable<DashboardWidget>() {
				@Override
				public DashboardWidget call() throws Exception {
					return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, DashboardWidgetFields.PK_ID.toString(), id);
				}
			});
			// clone to avoid editing errors
			return widget.cloneObject(DashboardWidget.class);
			
		} catch (ExecutionException e) {
			new CFWLog(logger).severe("Error while loading widget from DB or Cache: "+e.getMessage(), e);
		}
		
		
		return null;
		
	}
		
	/***************************************************************
	 * Return a list of all widgets for the specified dashboard.
	 * This method filters out any widget settings that are marked with
	 * SERVER_SIDE_ONLY.
	 * 
	 * @return Returns a resultSet with all widgets or null.
	 ****************************************************************/
	public static String getWidgetsForDashboardAsJSON(String dashboardID) {
		
		ArrayList<DashboardWidget> widgetList = CFW.DB.DashboardWidgets.getWidgetsForDashboard(dashboardID);
		
		//--------------------------------
		// Iterate Array and filter settings
		for(DashboardWidget widget : widgetList) {
			WidgetDefinition definition =  CFW.Registry.Widgets.getDefinition(widget.type());
			if(definition == null) {
				new CFWLog(logger).warn("Widget Type seems not to exist or is unavailable: "+widget.type() );
				continue;
			}
			CFWObject settingsObject = definition.getSettings();
			//do not sanitize to not mess up values from DB
			settingsObject.mapJsonFields(widget.settings(), false, false);
			
			
			//--------------------------------
			// Filter out fields flagged with
			// SERVER_SIDE_ONLY
			String filteredSettings = CFW.JSON.toJSON(
					settingsObject
					, true
					, EnumSet.of(CFWFieldFlag.SERVER_SIDE_ONLY)
					, false
				);
			
			//--------------------------------
			// Set Filtered Settings
			widget.settings(filteredSettings);
		}


		return CFW.JSON.toJSON(widgetList);
		
	}
	
	/***************************************************************
	 * Return a list of all widgets for the given dashboard.
	 * The data returned by this method is not save for sending to 
	 * the browser and should be used on server side only. 
	 * Use getWidgetsForDashboardAsJSON() to send
	 * data to the browser.
	 * 
	 * @return Returns an array with the widgets or an empty list.
	 ****************************************************************/
	public static ArrayList<DashboardWidget> getWidgetsForDashboard(String dashboardID) {
		
		return new CFWSQL(new DashboardWidget())
				.queryCache()
				.select()
				.where(DashboardWidgetFields.FK_ID_DASHBOARD, dashboardID)
				.getAsObjectListConvert(DashboardWidget.class);
		
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
			CFW.Messages.addErrorMessage(CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
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
			CFW.Messages.addInfoMessage("Please select a dashboard first.");
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
