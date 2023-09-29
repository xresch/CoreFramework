package com.xresch.cfw.extensions.databases.mssql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.extensions.databases.FeatureDBExtensions;
import com.xresch.cfw.extensions.databases.WidgetBaseSQLQueryChart;
import com.xresch.cfw.extensions.databases.mysql.FeatureDBExtensionsMySQL;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class WidgetMSSQLQueryChart extends WidgetBaseSQLQueryChart {

	private static Logger logger = CFWLog.getLogger(WidgetMSSQLQueryChart.class.getName());
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String getWidgetType() {return "emp_mssqlquerychart";}
		
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetCategory() {
		return FeatureDBExtensions.WIDGET_CATEGORY_DATABASE;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetName() { return "MSSQL Query Chart"; }
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureDBExtensionsMSSQL.PACKAGE_RESOURCE, "widget_"+getWidgetType()+".html");
	}	
	
	/************************************************************
	 * 
	 ************************************************************/
	@SuppressWarnings("rawtypes")
	@Override
	public CFWField createEnvironmentSelectorField() {
		return MSSQLSettingsFactory.createEnvironmentSelectorField();
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public DBInterface getDatabaseInterface(String environmentID) {

		MSSQLEnvironment environment;
		if(environmentID != null) {
			 environment = MSSQLEnvironmentManagement.getEnvironment(Integer.parseInt(environmentID));
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "MSSQL Query Chart: The chosen environment seems not configured correctly.");
			return null;
		}
		
		//---------------------------------
		// Get DB
		return environment.getDBInstance();

	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = super.getJavascriptFiles();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDBExtensionsMSSQL.PACKAGE_RESOURCE, "emp_widget_mssqlquerychart.js") );
		return array;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = super.getLocalizationFiles();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDBExtensionsMSSQL.PACKAGE_RESOURCE, "lang_en_emp_mssql.properties"));
		return map;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureDBExtensionsMSSQL.PERMISSION_MSSQL);
	}

}
