package com.xresch.cfw.features.parameter;

import java.util.LinkedHashMap;
import java.util.Locale;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.spi.CFWAppFeature;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureParameter extends CFWAppFeature {
	
	public static final String URI_PARAMETER = "/parameter";
	
	public static final String PACKAGE_RESOURCES = "com.xresch.cfw.features.parameter.resources";
	public static final String PACKAGE_MANUAL = "com.xresch.cfw.features.parameter.manual";
	
	public static ManualPage ROOT_MANUAL_PAGE;

	public static final String CFW_PARAMS = "params";
	
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCES);
		CFW.Files.addAllowedPackage(PACKAGE_MANUAL);

		//----------------------------------
		// Register Languages
		CFW.Localization.registerLocaleFile(Locale.ENGLISH, "/app/dashboard", new FileDefinition(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "lang_en_dashboard.properties"));
		CFW.Localization.registerLocaleFile(Locale.GERMAN, "/app/dashboard", new FileDefinition(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCES, "lang_de_dashboard.properties"));
		
    	//----------------------------------
    	// Register Objects
		CFW.Registry.Objects.addCFWObject(CFWParameter.class);

    	//----------------------------------
    	// Register Parameters
		CFW.Registry.Parameters.add(new ParameterDefinitionText());
		CFW.Registry.Parameters.add(new ParameterDefinitionTextarea());
		CFW.Registry.Parameters.add(new ParameterDefinitionSelect());
		CFW.Registry.Parameters.add(new ParameterDefinitionBoolean());
		CFW.Registry.Parameters.add(new ParameterDefinitionNumber());
		CFW.Registry.Parameters.add(new ParameterDefinitionDashboardID());
		CFW.Registry.Parameters.add(new ParameterDefinitionChartTypes());
		CFW.Registry.Parameters.add(new ParameterDefinitionChartSettings());
		CFW.Registry.Parameters.add(new ParameterDefinitionTimeRange());
		
		//----------------------------------
    	// Register Manual
		registerManual();
	}
	
	@Override
	public void initializeDB() {

	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {
		
		//----------------------------------
    	// Servlets
    	app.addUnsecureServlet(ServletParameter.class,  URI_PARAMETER);
		
		
	}

	@Override
	public void startTasks() {
		// TODO Auto-generated method stub
	}

	@Override
	public void stopFeature() {
		// TODO Auto-generated method stub
		
	}
	
	
	/******************************************************************
	 *
	 ******************************************************************/
	public void registerManual() {
		//----------------------------------
		// Pages for each Command
		
		ROOT_MANUAL_PAGE = CFW.Registry.Manual.addManualPage(null, 
				new ManualPage("Parameter")
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_mainpage.html"))
				;
		//----------------------------------
		// Pages for each Command
		LinkedHashMap<String, ParameterDefinition> paramList = CFW.Registry.Parameters.getParameterDefinitions();
		
		
		for(ParameterDefinition definition : paramList.values()) {
			
			new CFWQueryManualPageParameter(ROOT_MANUAL_PAGE, definition);
		
		}
	}
	
	

	
}
