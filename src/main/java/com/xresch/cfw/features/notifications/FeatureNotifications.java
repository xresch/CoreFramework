package com.xresch.cfw.features.notifications;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.jobs.CFWJobsReportingChannelAppLog;
import com.xresch.cfw.features.jobs.CFWJobsReportingChannelEMail;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.response.bootstrap.MenuItem;
import com.xresch.cfw.spi.CFWAppFeature;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureNotifications extends CFWAppFeature {
	
	private static final String URI_NOTIFICATIONS = "/app/notifications";
	public static final String PACKAGE_RESOURCE = "com.xresch.cfw.features.notifications.resources";
	public static final String PERMISSION_NOTIFICATIONS_USER = "Notifications: User";
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCE);
		
		//----------------------------------
		// Register Objects
		CFW.Registry.Objects.addCFWObject(Notification.class);
    	
		//----------------------------------
		// Register Global JavaScript
		CFW.Registry.Components.addGlobalJavascript(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE, "cfw_notifications.js");
		
		//----------------------------------
		// Register Languages
		//CFW.Localization.registerLocaleFile(Locale.ENGLISH, getNotificationsURI(), new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "lang_en_dashboard.properties"));
		//CFW.Localization.registerLocaleFile(Locale.GERMAN, getNotificationsURI(), new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDashboard.PACKAGE_RESOURCES, "lang_de_dashboard.properties"));
		
		//----------------------------------
    	// Register Alerting Channel
		CFW.Registry.JobsReporting.registerChannel(new CFWJobsReportingChannelNotification());
		
    	//----------------------------------
    	// Register Button Menu
		MenuItem notificationMenu = (MenuItem)new MenuItem("Notifications", "{!cfw_core_notifications!}") 
			.faicon("fas fa-bell")
			.addPermission(PERMISSION_NOTIFICATIONS_USER)
			.onclick("cfw_notifications_showModal();")
			.addCssClass("cfw-menuitem-notification")
			.addAttribute("id", "cfwMenuButtons-Notifications");
		
		CFW.Registry.Components.addButtonsMenuItem(notificationMenu, null);
		
	}

	@Override
	public void initializeDB() {
		
		//----------------------------------
    	// Permissions
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_NOTIFICATIONS_USER, FeatureUserManagement.CATEGORY_USER)
					.description("User can receive and view notifications."),
					true,
					true
			);

	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		app.addAppServlet(ServletNotification.class,  URI_NOTIFICATIONS);
		
	}

	@Override
	public void startTasks() {
		/*nothing todo */
	}

	@Override
	public void stopFeature() {
		/*nothing todo */
		
	}
	
	public static String getNotificationsURI() {
		return URI_NOTIFICATIONS;
	}

}
