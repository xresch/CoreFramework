package com.xresch.cfw.features.analytics;

import java.util.concurrent.ScheduledFuture;

import com.google.common.cache.CacheStats;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.features.config.FeatureConfiguration;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.response.bootstrap.MenuItem;

import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.guava.cache.CacheMetricsCollector;
import io.prometheus.client.hotspot.BufferPoolsExports;
import io.prometheus.client.hotspot.ClassLoadingExports;
import io.prometheus.client.hotspot.GarbageCollectorExports;
import io.prometheus.client.hotspot.MemoryAllocationExports;
import io.prometheus.client.hotspot.MemoryPoolsExports;
import io.prometheus.client.hotspot.StandardExports;
import io.prometheus.client.hotspot.ThreadExports;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
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
				(MenuItem)new MenuItem("Cache Statistics")
					.faicon("fas fa-sd-card")
					.addPermission(FeatureCore.PERMISSION_APP_ANALYTICS)
					.href("/app/cachestatistics")	
				, SYSTEM_ANALYTICS);
		
		CFW.Registry.Components.addAdminCFWMenuItem(
				(MenuItem)new MenuItem("Log Configuration")
					.faicon("fas fa-book-open")
					.addPermission(FeatureCore.PERMISSION_APP_ANALYTICS)
					.href("/app/logconfiguration")	
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
		
		//-----------------------------------------
		// Servlets
    	app.addAppServlet(ServletCPUSampling.class,  "/cpusampling");
    	app.addAppServlet(ServletDatabaseAnalytics.class,  "/dbanalytics");
    	app.addAppServlet(ServletContextTree.class,  "/servletcontexttree");
    	app.addAppServlet(ServletSystemProperties.class,  "/systemproperties");
    	app.addAppServlet(ServletCacheStatistics.class,  "/cachestatistics");
    	app.addAppServlet(ServletLogConfiguration.class,  "/logconfiguration");
		//-----------------------------------------
		// Prometheus Endpoint
	    new GarbageCollectorExports().register();
	    new ThreadExports().register();
	    new StandardExports().register();
	    new MemoryPoolsExports().register();
	    new MemoryAllocationExports().register();
	    new ClassLoadingExports().register();
	    new BufferPoolsExports().register();
	    
	    app.addUnsecureServlet(MetricsServlet.class,  	"/metrics");
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
