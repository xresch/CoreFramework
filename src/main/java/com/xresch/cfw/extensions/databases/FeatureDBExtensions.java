package com.xresch.cfw.extensions.databases;

import java.util.Locale;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.dashboard.FeatureDashboard;
import com.xresch.cfw.features.jobs.FeatureJobs;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.spi.CFWAppFeature;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureDBExtensions extends CFWAppFeature {
	
	public static final String PACKAGE_RESOURCE = "com.xresch.cfw.extensions.databases.resources";
	
	public static final String WIDGET_CATEGORY_DATABASE = "Database";
	
	@Override
	public void register() {
		//----------------------------------
		// Register Settings
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCE);
		
		//----------------------------------
		// Register Locales
		
		FileDefinition english = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDBExtensions.PACKAGE_RESOURCE, "lang_en_dbextensions.properties");
		
		CFW.Localization.registerLocaleFile(Locale.ENGLISH, FeatureDashboard.URI_DASHBOARD_VIEW, english);
		CFW.Localization.registerLocaleFile(Locale.ENGLISH, FeatureDashboard.URI_DASHBOARD_VIEW_PUBLIC, english);
		CFW.Localization.registerLocaleFile(Locale.ENGLISH, FeatureManual.URI_MANUAL, english);
		
		CFW.Localization.registerLocaleFile(Locale.ENGLISH, FeatureJobs.getJobsURI(), english);
		
		//----------------------------------
		// Register Manual Page
		CFW.Registry.Manual.addManualPage(null,
				new ManualPage("Databases")
					.faicon("fas fa-database")
					.addPermission(FeatureManual.PERMISSION_MANUAL)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE, "z_manual_widgets_database.html")
			);
		
	}

	@Override
	public void initializeDB() {
		/* do nothing */
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		/* do nothing */
	}

	@Override
	public void startTasks() {
		/* do nothing */
	}

	@Override
	public void stopFeature() {
		/* do nothing */
	}

}
