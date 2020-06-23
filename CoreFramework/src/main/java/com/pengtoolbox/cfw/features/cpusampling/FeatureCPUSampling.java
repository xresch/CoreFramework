package com.pengtoolbox.cfw.features.cpusampling;

import java.util.concurrent.ScheduledFuture;

import org.eclipse.jetty.servlet.ServletContextHandler;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFWAppFeature;
import com.pengtoolbox.cfw._main.CFWApplicationExecutor;
import com.pengtoolbox.cfw.features.config.Configuration;
import com.pengtoolbox.cfw.features.core.FeatureCore;
import com.pengtoolbox.cfw.features.usermgmt.Permission;
import com.pengtoolbox.cfw.features.usermgmt.Role;
import com.pengtoolbox.cfw.response.bootstrap.MenuItem;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class FeatureCPUSampling extends CFWAppFeature {

	public static final String RESOURCE_PACKAGE = "com.pengtoolbox.cfw.features.cpusampling.resources";
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		//----------------------------------
		// Register Objects
		CFW.Registry.Objects.addCFWObject(CPUSampleSignature.class);
		CFW.Registry.Objects.addCFWObject(CPUSample.class);
    	
    	//----------------------------------
    	// Register Regular Menu
		
		CFW.Registry.Components.addAdminCFWMenuItem(
				(MenuItem)new MenuItem("CPU Sampling")
					.faicon("fas fa-microchip")
					.addPermission(FeatureCore.PERMISSION_APP_ANALYTICS)
					.href("/app/cpusampling")	
				, "System Analytics");
	}

	@Override
	public void initializeDB() {
		
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
    	app.addAppServlet(ServletCPUSampling.class,  "/cpusampling");
	}

	@Override
	public void startTasks() {

		int seconds = CFW.DB.Config.getConfigAsInt(Configuration.CPU_SAMPLING_SECONDS);
		ScheduledFuture<?> sampling = CFW.Schedule.runPeriodically(0, seconds, new TaskCPUSampling());
		ScheduledFuture<?> aggregation = CFW.Schedule.runPeriodically(0, 600, new TaskCPUSamplingAggregation());
	}

	@Override
	public void stopFeature() {
		// TODO Auto-generated method stub
		
	}

}
