package com.xresch.cfw.features.analytics;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.config.ConfigChangeListener;
import com.xresch.cfw.features.config.FeatureConfig;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.response.bootstrap.CFWHTMLItem;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemCustom;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemDynamic;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemMenuItem;
import com.xresch.cfw.spi.CFWAppFeature;
import com.xresch.cfw.utils.CFWTime.CFWTimeUnit;

import io.prometheus.client.exporter.MetricsServlet;
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
	
	private static ScheduledFuture<?> taskcpuSampling;
	private static ScheduledFuture<?> taskCpuSamplingAgeOut;
	private static ScheduledFuture<?> taskStatusMonitorUpdate;

	public static final String PERMISSION_SYSTEM_ANALYTICS = "System Analytics";
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
		// Register Job Tasks
		CFW.Registry.Jobs.registerTask(new CFWJobTaskThreadDumps());
		CFW.Registry.Jobs.registerTask(new CFWJobTaskTestAlerting());
		
		//----------------------------------
		// Register Job Tasks
		CFW.Registry.Components.addGlobalJavascript(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "cfw_statusmonitor_common.js");
    	
		//----------------------------------
    	// Register Button Menu
		CFWHTMLItemMenuItem statusMonitorMenu = (CFWHTMLItemMenuItem)new CFWHTMLItemMenuItem("Status Monitor", "{!cfw_core_statusmonitor!}") 
			.faicon("fas fa-circle")
			.onclick("cfw_statusmonitor_loadInMenu();")
			.addCssClass("cfw-menuitem-statusmonitor")
			.addAttribute("id", "cfwMenuButtons-StatusMonitor")
			// hacking the dynamic color or the enu item
			.setDynamicCreator(new CFWHTMLItemDynamic() {		
				
				@Override
				public ArrayList<CFWHTMLItem> createDynamicItems() {
					ArrayList<CFWHTMLItem> list = new ArrayList<>();
					
					CFWHTMLItemMenuItem custom = 
							(CFWHTMLItemMenuItem)new CFWHTMLItemMenuItem("") 
							.addCssClass("d-none")
							.addAttribute("id", "cfw-worst-status")
							.addAttribute(
									  "data-worst-status"
									, CFW.Registry.StatusMonitor.getWorstStatus().toString()
							)
							;
					
					list.add(custom);
					return list;
				}
			});
		
		CFW.Registry.Components.addButtonsMenuItem(statusMonitorMenu, null);
		
    	//----------------------------------
    	// Register Admin Menu
		String SYSTEM_ANALYTICS = "System Analytics";
		
		CFW.Registry.Components.addAdminCFWMenuItem(
				(CFWHTMLItemMenuItem)new CFWHTMLItemMenuItem(SYSTEM_ANALYTICS)
					.faicon("fas fa-traffic-light")
					.addPermission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS)
					.addAttribute("id", "cfwMenuSystemAnalytics")
				, null);
		
		//------------------------------
		CFW.Registry.Components.addAdminCFWMenuItem(
				(CFWHTMLItemMenuItem)new CFWHTMLItemMenuItem("Status Monitor")
				.faicon("fas fa-tv")
				.addPermission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS)
				.href("/app/statusmonitor")
				.addAttribute("id", "cfwMenuSystemAnalytics-StatusMonitor")
				, SYSTEM_ANALYTICS);
		
		//------------------------------
		CFW.Registry.Components.addAdminCFWMenuItem(
				(CFWHTMLItemMenuItem)new CFWHTMLItemMenuItem("DB Analytics")
					.faicon("fas fa-database")
					.addPermission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS)
					.href("/app/dbanalytics")
					.addAttribute("id", "cfwMenuSystemAnalytics-DBAnalytics")
				, SYSTEM_ANALYTICS);
		
		//------------------------------
		CFW.Registry.Components.addAdminCFWMenuItem(
				(CFWHTMLItemMenuItem)new CFWHTMLItemMenuItem("CPU Sampling")
					.faicon("fas fa-microchip")
					.addPermission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS)
					.href("/app/cpusampling")	
					.addAttribute("id", "cfwMenuSystemAnalytics-CPUSampling")
				, SYSTEM_ANALYTICS);
		
		//------------------------------
		CFW.Registry.Components.addAdminCFWMenuItem(
				(CFWHTMLItemMenuItem)new CFWHTMLItemMenuItem("Servlet Context Tree")
					.faicon("fas fa-sitemap")
					.addPermission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS)
					.href("/app/servletcontexttree")
					.addAttribute("id", "cfwMenuSystemAnalytics-ServletContextTree")
				, SYSTEM_ANALYTICS);
		
		//------------------------------
		CFW.Registry.Components.addAdminCFWMenuItem(
				(CFWHTMLItemMenuItem)new CFWHTMLItemMenuItem("Session Overview")
					.faicon("fas fa-database")
					.addPermission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS)
					.href("/app/sessionoverview")	
					.addAttribute("id", "cfwMenuSystemAnalytics-SessionOverview")
				, SYSTEM_ANALYTICS);
		
		//------------------------------
		CFW.Registry.Components.addAdminCFWMenuItem(
				(CFWHTMLItemMenuItem)new CFWHTMLItemMenuItem("System Properties")
					.faicon("fas fa-cubes")
					.addPermission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS)
					.href("/app/systemproperties")	
					.addAttribute("id", "cfwMenuSystemAnalytics-SystemProperties")
				, SYSTEM_ANALYTICS);
		
		//------------------------------
		CFW.Registry.Components.addAdminCFWMenuItem(
				(CFWHTMLItemMenuItem)new CFWHTMLItemMenuItem("Versions")
				.faicon("fas fa-cubes")
				.addPermission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS)
				.href("/app/versions")	
				.addAttribute("id", "cfwMenuSystemAnalytics-Versions")
				, SYSTEM_ANALYTICS);
		
		//------------------------------
		CFW.Registry.Components.addAdminCFWMenuItem(
				(CFWHTMLItemMenuItem)new CFWHTMLItemMenuItem("Cache Statistics")
					.faicon("fas fa-sd-card")
					.addPermission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS)
					.href("/app/cachestatistics")	
					.addAttribute("id", "cfwMenuSystemAnalytics-CacheStats")
				, SYSTEM_ANALYTICS);
		
		//------------------------------
		CFW.Registry.Components.addAdminCFWMenuItem(
				(CFWHTMLItemMenuItem)new CFWHTMLItemMenuItem("Log Configuration")
					.faicon("fas fa-book-open")
					.addPermission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS)
					.href("/app/logconfiguration")	
					.addAttribute("id", "cfwMenuSystemAnalytics-LogConfig")
				, SYSTEM_ANALYTICS);
		
		//------------------------------
		CFW.Registry.Components.addAdminCFWMenuItem(
				(CFWHTMLItemMenuItem)new CFWHTMLItemMenuItem("Metrics")
					.faicon("fas fa-thermometer-half")
					.addPermission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS)
					.href("/metrics")	
					.addAttribute("target", "_blank")
					.addAttribute("id", "cfwMenuSystemAnalytics-Metrics")
				, SYSTEM_ANALYTICS);
	}

	@Override
	public void initializeDB() {
		
		//----------------------------------
    	// Enable SQL Statistics
		CFW.DB.preparedExecute("SET QUERY_STATISTICS_MAX_ENTRIES 500;");
		CFW.DB.preparedExecute("SET QUERY_STATISTICS TRUE;");
		
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
    	app.addAppServlet(ServletSessionOverview.class,  "/sessionoverview");
    	app.addAppServlet(ServletStatusMonitor.class,  "/statusmonitor");
    	app.addAppServlet(ServletVersions.class,  "/versions");
    	
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
	    
		//-------------------------------
		// Create Change Listener
		ConfigChangeListener listener = new ConfigChangeListener(
				FeatureConfig.CONFIG_CPU_SAMPLING_SECONDS,
				FeatureConfig.CONFIG_CPU_SAMPLING_AGGREGATION
			) {
			
			@Override
			public void onChange() {
				startTasks();
			}
		};
		
		CFW.DB.Config.addChangeListener(listener);
	}

	@Override
	public void startTasks() {
		
		
		//--------------------------------
		// Setup Status Monitor Update
		if(taskStatusMonitorUpdate != null) {
			taskStatusMonitorUpdate.cancel(false);
		}
		
		int millisOf5min = (int)CFWTimeUnit.m.toMillis(5);
		taskStatusMonitorUpdate = CFW.Schedule.runPeriodicallyMillis(0, millisOf5min, new TaskStatusMonitorUpdate());
		
		//-----------------------------
		// CPU Sampling
		String mode = CFW.Properties.MODE;
		if(mode.equals(CFW.MODE_FULL) || mode.equals(CFW.MODE_APP)) {
			
			//--------------------------------
			// Setup CPU Sampling Task
			if(taskcpuSampling != null) {
				taskcpuSampling.cancel(false);
			}
			int millis = (int)(1000 * CFW.DB.Config.getConfigAsFloat(FeatureConfig.CATEGORY_PERFORMANCE, FeatureConfig.CONFIG_CPU_SAMPLING_SECONDS));
			taskcpuSampling = CFW.Schedule.runPeriodicallyMillis(0, millis, new TaskCPUSampling());
			
			//--------------------------------
			// Setup CPU Sampling AgeOut Task
			if(taskCpuSamplingAgeOut != null) {
				taskCpuSamplingAgeOut.cancel(false);
			}
			
			taskCpuSamplingAgeOut = CFW.Schedule.runPeriodically(0, 3000, new TaskCPUSamplingAgeOut());
		}
	}

	@Override
	public void stopFeature() {
		// TODO Auto-generated method stub
		
	}

}
