package com.xresch.cfw.extensions.databases.generic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.extensions.databases.FeatureDBExtensions;
import com.xresch.cfw.extensions.databases.WidgetBaseSQLQueryStatus;
import com.xresch.cfw.extensions.databases.mssql.FeatureDBExtensionsMSSQL;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class WidgetGenericJDBCQueryStatus extends WidgetBaseSQLQueryStatus {

	private static Logger logger = CFWLog.getLogger(WidgetGenericJDBCQueryStatus.class.getName());
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String getWidgetType() {return "emp_genericjdbcquerystatus";}
	
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
	public String widgetName() { return "Generic JDBC Query Status"; }
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String descriptionHTML() {
		//return CFW.Files.readPackageResource(FeatureDBExtensionsGenericJDBC.PACKAGE_RESOURCE, "widget_"+getWidgetType()+".html");
		return CFW.Files.readPackageResource(FeatureDBExtensions.PACKAGE_RESOURCE, "z_manual_widgets_database.html");
	}	
	
	/************************************************************
	 * 
	 ************************************************************/
	@SuppressWarnings("rawtypes")
	@Override
	public CFWField createEnvironmentSelectorField() {
		return GenericJDBCSettingsFactory.createEnvironmentSelectorField();
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public DBInterface getDatabaseInterface(String environmentID) {

		GenericJDBCEnvironment environment;
		if(environmentID != null) {
			 environment = GenericJDBCEnvironmentManagement.getEnvironment(Integer.parseInt(environmentID));
		}else {
			CFW.Messages.addWarningMessage("Generic JDBC Query Status: The chosen environment seems not configured correctly.");
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
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDBExtensionsGenericJDBC.PACKAGE_RESOURCE, "emp_widget_genericjdbcquerystatus.js") );
		return array;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = super.getLocalizationFiles();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDBExtensionsGenericJDBC.PACKAGE_RESOURCE, "lang_en_emp_genericjdbc.properties"));
		return map;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureDBExtensionsGenericJDBC.PERMISSION_GENERICJDBC);
	}

}
