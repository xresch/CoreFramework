package com.xresch.cfw.features.dashboard.parameters;

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
import com.xresch.cfw.features.dashboard.FeatureDashboard;
import com.xresch.cfw.features.dashboard.parameters.DashboardParameter.DashboardParameterFields;
import com.xresch.cfw.features.dashboard.parameters.DashboardParameter.DashboardParameterMode;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBDashboardParameter {
	
	private static Class<DashboardParameter> cfwObjectClass = DashboardParameter.class;
	
	private static final Logger logger = CFWLog.getLogger(CFWDBDashboardParameter.class.getName());
		
	//####################################################################################################
	// Preckeck Initialization
	//####################################################################################################
	private static PrecheckHandler prechecksCreate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			DashboardParameter parameter = (DashboardParameter)object;
			
			if(parameter == null ) {
				new CFWLog(logger)
					.warn("The parameter cannot be null.", new Throwable());
				return false;
			}
			if(!checkIsParameterNameUsedOnCreate(parameter)) {
				return true;
			}else {
				new CFWLog(logger).severe("The parameter name is already in use: "+parameter.name());
				return false;
			}
		}
	};
	
	
	private static PrecheckHandler prechecksDeleteUpdate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			DashboardParameter parameter = (DashboardParameter)object;
			
			if(parameter == null ) {
				new CFWLog(logger)
					.warn("The parameter cannot be null.", new Throwable());
				return false;
			}
			if(!checkIsParameterNameUsedOnUpdate(parameter)) {
				return true;
			}else {
				new CFWLog(logger).severe("The parameter name is already in use: "+parameter.name());
				return false;
			}
			
		}
	};
		
	//####################################################################################################
	// CREATE
	//####################################################################################################
	public static boolean	create(DashboardParameter... items) 	{ return CFWDBDefaultOperations.create(prechecksCreate, items); }
	public static boolean 	create(DashboardParameter item) 		{ return CFWDBDefaultOperations.create(prechecksCreate, item);}
	public static Integer	createGetPrimaryKey(DashboardParameter item) 	{ return CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreate, item);}
	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(DashboardParameter... items) 	{ return CFWDBDefaultOperations.update(prechecksDeleteUpdate, items); }
	public static boolean 	update(DashboardParameter item) 		{ return CFWDBDefaultOperations.update(prechecksDeleteUpdate, item); }
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean 	deleteByID(String id) 				{ return CFWDBDefaultOperations.deleteFirstBy(prechecksDeleteUpdate, cfwObjectClass, DashboardParameterFields.PK_ID.toString(), Integer.parseInt(id)); }
	public static boolean 	deleteByID(int id) 					{ return CFWDBDefaultOperations.deleteFirstBy(prechecksDeleteUpdate, cfwObjectClass, DashboardParameterFields.PK_ID.toString(), id); }
	public static boolean 	deleteMultipleByID(String itemIDs) 	{ return CFWDBDefaultOperations.deleteMultipleByID(prechecksDeleteUpdate, cfwObjectClass, itemIDs); }
		
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static DashboardParameter selectByID(int id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, DashboardParameterFields.PK_ID.toString(), id);
	}
		
	/***************************************************************
	 * Return a list of all user parameters
	 * 
	 * @return Returns a resultSet with all parameters or null.
	 ****************************************************************/
	public static String getParametersForDashboardAsJSON(String dashboardID) {
		
		return new CFWSQL(new DashboardParameter())
				.queryCache()
				.select()
				.where(DashboardParameterFields.FK_ID_DASHBOARD, dashboardID)
				.orderby(DashboardParameterFields.WIDGET_TYPE.toString(), DashboardParameterFields.LABEL.toString())
				.getAsJSON();
		
	}
	
	/***************************************************************
	 * Return a list of all user parameters
	 * 
	 * @return Returns an array with the parameters or an empty list.
	 ****************************************************************/
	public static ArrayList<CFWObject> getParametersForDashboard(String dashboardID) {
		
		return new CFWSQL(new DashboardParameter())
				.queryCache()
				.select()
				.where(DashboardParameterFields.FK_ID_DASHBOARD, dashboardID)
				.orderby(DashboardParameterFields.WIDGET_TYPE.toString(), DashboardParameterFields.LABEL.toString())
				.getAsObjectList();
		
	}
	
	
	/***************************************************************
	 * Return the parameter as JSON string.
	 * 
	 ****************************************************************/
	public static String getParameterAsJSON(String parameterID) {
		
		return new CFWSQL(new DashboardParameter())
				.queryCache()
				.select()
				.where(DashboardParameterFields.PK_ID, parameterID)
				.getAsJSON();
		
	}
	
	
	/***************************************************************
	 * @return Returns true if the parameter name is already in use
	 * for this dashboard, ignores the the given parameter in the check.
	 ****************************************************************/
	public static boolean checkIsParameterNameUsedOnUpdate(DashboardParameter parameter) {
		
		return  0 < new CFWSQL(new DashboardParameter())
				.queryCache()
				.selectCount()
				.where(DashboardParameterFields.FK_ID_DASHBOARD, parameter.foreignKeyDashboard())
				.and(DashboardParameterFields.NAME, parameter.name())
				.and().not().is(DashboardParameterFields.PK_ID, parameter.id())
				.getCount();
		
	}
	/***************************************************************
	 * @return Returns true if the parameter name is already in use
	 * for this dashboard.
	 ****************************************************************/
	public static boolean checkIsParameterNameUsedOnCreate(DashboardParameter parameter) {
		
		return  0 < new CFWSQL(new DashboardParameter())
				.queryCache()
				.selectCount()
				.where(DashboardParameterFields.FK_ID_DASHBOARD, parameter.foreignKeyDashboard())
				.and(DashboardParameterFields.NAME, parameter.name())
				.getCount();
		
	}
	
	/***************************************************************
	 * @return Returns true if the parameter is of the specified dashboard
	 ****************************************************************/
	public static boolean checkIsParameterOfDashboard(String dashboardID, String parameterID) {
		
		return  1 == new CFWSQL(new DashboardParameter())
				.queryCache()
				.selectCount()
				.where(DashboardParameterFields.FK_ID_DASHBOARD, dashboardID)
				.and(DashboardParameterFields.PK_ID, parameterID)
				.getCount();
		
	}
	
	
	/***************************************************************
	 * Return a list of available parameters for the given field.
	 * 
	 * @return Returns an array with the parameters or an empty list.
	 ****************************************************************/
	public static ArrayList<CFWObject> getAvailableParamsForDashboard(String dashboardID, String widgetType, String widgetSetting, boolean allowGenericParams) {
		
		CFWSQL sql = new DashboardParameter()
				.queryCache(CFWDBDashboardParameter.class, "autocompleteParametersForDashboard"+allowGenericParams)
				.select()
				.where(DashboardParameterFields.FK_ID_DASHBOARD, dashboardID)
				.and(DashboardParameterFields.MODE, DashboardParameterMode.MODE_SUBSTITUTE.toString())
				.and().custom("(")
						.is(DashboardParameterFields.WIDGET_TYPE, widgetType);
						if(allowGenericParams) sql.or().isNull(DashboardParameterFields.WIDGET_TYPE);
						
			sql.custom(")")
				.and().custom("(")
					.is(DashboardParameterFields.LABEL, widgetSetting);
					if(allowGenericParams) sql.or().isNull(DashboardParameterFields.WIDGET_TYPE);
					
		return sql.custom(")")
				.getAsObjectList();
		
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
			
			CFWSQL selectForExport = new CFWSQL(new DashboardParameter())
				.queryCache()
				.select();
			
			if(!Strings.isNullOrEmpty(dashboardID)) {
				selectForExport.where(DashboardParameterFields.FK_ID_DASHBOARD, dashboardID);
				return  selectForExport.getObjectsAsJSONArray();
			}
							
			return null;
		 
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
			return null;
		}
	}
	

}
