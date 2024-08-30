package com.xresch.cfw.extensions.databases.postgres;

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
import com.xresch.cfw.extensions.databases.WidgetBaseSQLQueryStatus;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public class WidgetPostgresQueryStatus extends WidgetBaseSQLQueryStatus {

	
	private static Logger logger = CFWLog.getLogger(WidgetPostgresQueryStatus.class.getName());
	
	/************************************************************
	 * 
	 ************************************************************/	
	@Override
	public String getWidgetType() {return "emp_postgresquerystatus";}
	
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
	public String widgetName() { return "Postgres Query Status"; }
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String descriptionHTML() {
		//return CFW.Files.readPackageResource(FeatureDBExtensionsPostgres.PACKAGE_RESOURCE, "widget_"+getWidgetType()+".html");
		return CFW.Files.readPackageResource(FeatureDBExtensions.PACKAGE_RESOURCE, "z_manual_widgets_database.html");
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public CFWField createEnvironmentSelectorField() {
		return PostgresSettingsFactory.createEnvironmentSelectorField();
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public DBInterface getDatabaseInterface(String environmentID) {

		PostgresEnvironment environment;
		if(environmentID != null) {
			 environment = PostgresEnvironmentManagement.getEnvironment(Integer.parseInt(environmentID));
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Postgres Query Status: The chosen environment seems not configured correctly.");
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
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDBExtensionsPostgres.PACKAGE_RESOURCE, "emp_widget_postgresquerystatus.js") );
		return array;
	}
	
	/************************************************************
	 * 
	 ************************************************************/	
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = super.getLocalizationFiles();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDBExtensionsPostgres.PACKAGE_RESOURCE, "lang_en_emp_postgres.properties"));
		return map;
	}
	
	
	/************************************************************
	 * 
	 ************************************************************/	
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureDBExtensionsPostgres.PERMISSION_POSTGRES);
	}
		
}


