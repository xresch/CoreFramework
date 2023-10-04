package com.xresch.cfw.features.parameter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.features.dashboard.widgets.ManualPageWidget;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetForceRefresh;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetJavascript;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetParameter;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetPyConfig;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetPyScript;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetReplica;
import com.xresch.cfw.features.dashboard.widgets.advanced.WidgetThresholdLegend;
import com.xresch.cfw.features.dashboard.widgets.eastereggs.WidgetEasterEggsDiscoMode;
import com.xresch.cfw.features.dashboard.widgets.eastereggs.WidgetEasterEggsFireworks;
import com.xresch.cfw.features.dashboard.widgets.eastereggs.WidgetEasterEggsLightSwitch;
import com.xresch.cfw.features.dashboard.widgets.eastereggs.WidgetEasterEggsSnow;
import com.xresch.cfw.features.dashboard.widgets.eastereggs.WidgetHelloWorld;
import com.xresch.cfw.features.dashboard.widgets.standard.WidgetChecklist;
import com.xresch.cfw.features.dashboard.widgets.standard.WidgetHTMLEditor;
import com.xresch.cfw.features.dashboard.widgets.standard.WidgetImage;
import com.xresch.cfw.features.dashboard.widgets.standard.WidgetLabel;
import com.xresch.cfw.features.dashboard.widgets.standard.WidgetList;
import com.xresch.cfw.features.dashboard.widgets.standard.WidgetRefreshTime;
import com.xresch.cfw.features.dashboard.widgets.standard.WidgetTable;
import com.xresch.cfw.features.dashboard.widgets.standard.WidgetTags;
import com.xresch.cfw.features.dashboard.widgets.standard.WidgetText;
import com.xresch.cfw.features.dashboard.widgets.standard.WidgetWebsite;
import com.xresch.cfw.features.dashboard.widgets.standard.WidgetYoutubeVideo;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.parameter.Parameter;
import com.xresch.cfw.features.parameter.ParameterDefinitionBoolean;
import com.xresch.cfw.features.parameter.ParameterDefinitionDashboardID;
import com.xresch.cfw.features.parameter.ParameterDefinitionNumber;
import com.xresch.cfw.features.parameter.ParameterDefinitionSelect;
import com.xresch.cfw.features.parameter.ParameterDefinitionText;
import com.xresch.cfw.features.parameter.ParameterDefinitionTextarea;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.response.bootstrap.DynamicItemCreator;
import com.xresch.cfw.response.bootstrap.HierarchicalHTMLItem;
import com.xresch.cfw.response.bootstrap.MenuItem;
import com.xresch.cfw.spi.CFWAppFeature;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureParameter extends CFWAppFeature {
	
	public static final String URI_DASHBOARD_VIEW = "/app/dashboard/view";
	public static final String URI_DASHBOARD_VIEW_PUBLIC = "/public/dashboard/view";
	
	
	public static final String PACKAGE_RESOURCES = "com.xresch.cfw.features.parameter.resources";
	
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCES);

		//----------------------------------
		// Register Languages
		CFW.Localization.registerLocaleFile(Locale.ENGLISH, "/app/dashboard", new FileDefinition(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "lang_en_dashboard.properties"));
		CFW.Localization.registerLocaleFile(Locale.GERMAN, "/app/dashboard", new FileDefinition(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "lang_de_dashboard.properties"));
		
		CFW.Localization.registerLocaleFile(Locale.ENGLISH, URI_DASHBOARD_VIEW_PUBLIC, new FileDefinition(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "lang_en_dashboard.properties"));
		CFW.Localization.registerLocaleFile(Locale.GERMAN, URI_DASHBOARD_VIEW_PUBLIC, new FileDefinition(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "lang_de_dashboard.properties"));
		
    	//----------------------------------
    	// Register Objects
		CFW.Registry.Objects.addCFWObject(Parameter.class);

    	//----------------------------------
    	// Register Parameters
		CFW.Registry.Parameters.add(new ParameterDefinitionText());
		CFW.Registry.Parameters.add(new ParameterDefinitionTextarea());
		CFW.Registry.Parameters.add(new ParameterDefinitionSelect());
		CFW.Registry.Parameters.add(new ParameterDefinitionBoolean());
		CFW.Registry.Parameters.add(new ParameterDefinitionNumber());
		CFW.Registry.Parameters.add(new ParameterDefinitionDashboardID());
		

	}
	
	@Override
	public void initializeDB() {

		
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {
		
		//----------------------------------
    	// Servlets
    	//app.addAppServlet(ServletDashboardList.class,  URI_DASHBOARD_LIST);

		
	}

	@Override
	public void startTasks() {
		// TODO Auto-generated method stub
	}

	@Override
	public void stopFeature() {
		// TODO Auto-generated method stub
		
	}
	
	

	
}
