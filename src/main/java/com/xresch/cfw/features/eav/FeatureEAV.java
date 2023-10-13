package com.xresch.cfw.features.eav;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.spi.CFWAppFeature;

/**************************************************************************************************************
 * Feature for Entry-Attribute-Value(EAV) data.
 *  
 * @author Reto Scheiwiller, (c) Copyright 2023
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureEAV extends CFWAppFeature {
	
	public static final String RESOURCE_PACKAGE = "com.xresch.cfw.features.eav.resources";
		
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		//----------------------------------
		// Register Objects
		CFW.Registry.Objects.addCFWObject(EAVEntity.class);
		CFW.Registry.Objects.addCFWObject(EAVAttribute.class);
		CFW.Registry.Objects.addCFWObject(EAVValue.class);
		
	}

	@Override
	public void initializeDB() {
		
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
