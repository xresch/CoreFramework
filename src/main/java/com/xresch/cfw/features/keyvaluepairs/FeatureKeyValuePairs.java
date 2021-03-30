package com.xresch.cfw.features.keyvaluepairs;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureKeyValuePairs extends CFWAppFeature {
	
	public static final String RESOURCE_PACKAGE = "com.xresch.cfw.features.keyvaluepairs.resources";
		
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		//----------------------------------
		// Register Objects
		CFW.Registry.Objects.addCFWObject(KeyValuePair.class);
		
	}

	@Override
	public void initializeDB() {
				
		CFW.DB.Config.updateCache();
		
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		//app.addAppServlet(ServletKeyValuePairs.class,  "/configuration");
	}

	@Override
	public void startTasks() {
		// nothing to start
	}

	@Override
	public void stopFeature() {
		// nothing to stop
	}

}
