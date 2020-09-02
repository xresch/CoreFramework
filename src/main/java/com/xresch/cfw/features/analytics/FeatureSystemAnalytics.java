package com.xresch.cfw.features.analytics;

import java.util.concurrent.ScheduledFuture;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.features.config.FeatureConfiguration;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.response.bootstrap.MenuItem;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class FeatureSystemAnalytics extends CFWAppFeature {

	public static final String RESOURCE_PACKAGE = "com.xresch.cfw.features.analytics.resources";
	
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
		String SYSTEM_ANALYTICS = "System Analytics";
		
		CFW.Registry.Components.addAdminCFWMenuItem(
				(MenuItem)new MenuItem("DB Analytics")
					.faicon("fas fa-database")
					.addPermission(FeatureCore.PERMISSION_APP_ANALYTICS)
					.href("/app/dbanalytics")	
				, SYSTEM_ANALYTICS);
		
		CFW.Registry.Components.addAdminCFWMenuItem(
				(MenuItem)new MenuItem("CPU Sampling")
					.faicon("fas fa-microchip")
					.addPermission(FeatureCore.PERMISSION_APP_ANALYTICS)
					.href("/app/cpusampling")	
				, SYSTEM_ANALYTICS);
		
		CFW.Registry.Components.addAdminCFWMenuItem(
				(MenuItem)new MenuItem("Servlet Context Tree")
					.faicon("fas fa-sitemap")
					.addPermission(FeatureCore.PERMISSION_APP_ANALYTICS)
					.href("/app/servletcontexttree")	
				, SYSTEM_ANALYTICS);
		
		CFW.Registry.Components.addAdminCFWMenuItem(
				(MenuItem)new MenuItem("System Properties")
					.faicon("fas fa-cubes")
					.addPermission(FeatureCore.PERMISSION_APP_ANALYTICS)
					.href("/app/systemproperties")	
				, SYSTEM_ANALYTICS);
		
		
		CFW.Registry.Components.addAdminCFWMenuItem(
				(MenuItem)new MenuItem("Metrics")
					.faicon("fas fa-thermometer-half")
					.addPermission(FeatureCore.PERMISSION_APP_ANALYTICS)
					.href("/metrics")	
					.addAttribute("target", "_blank")
				, SYSTEM_ANALYTICS);
	}

	@Override
	public void initializeDB() {
		
		//----------------------------------
    	// Enable SQL Statistics
		CFW.DB.preparedExecute("SET QUERY_STATISTICS_MAX_ENTRIES 500;");
		CFW.DB.preparedExecute("SET QUERY_STATISTICS TRUE;");
		
		//----------------------------------------
    	// Procedure: row_count() for table name
		CFW.DB.preparedExecuteBatch("DROP ALIAS IF EXISTS count_rows;\r\n" + 
				"create ALIAS count_rows as \r\n" + 
				"'long countRows(Connection conn, String tableName) \r\n" + 
				"    throws SQLException {\r\n" + 
				"ResultSet rs = conn.createStatement().\r\n" + 
				"    executeQuery(\"select count(*) from \" + tableName);\r\n" + 
				"rs.next();\r\n" + 
				"return rs.getLong(1); }';");
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
    	app.addAppServlet(ServletCPUSampling.class,  "/cpusampling");
    	app.addAppServlet(ServletDatabaseAnalytics.class,  "/dbanalytics");
    	app.addAppServlet(ServletContextTree.class,  "/servletcontexttree");
    	app.addAppServlet(ServletSystemProperties.class,  "/systemproperties");
	}

	@Override
	public void startTasks() {

		int seconds = CFW.DB.Config.getConfigAsInt(FeatureConfiguration.CONFIG_CPU_SAMPLING_SECONDS);
		CFW.Schedule.runPeriodically(0, seconds, new TaskCPUSampling());
		CFW.Schedule.runPeriodically(0, 600, new TaskCPUSamplingAggregation());
	}

	@Override
	public void stopFeature() {
		// TODO Auto-generated method stub
		
	}

}
