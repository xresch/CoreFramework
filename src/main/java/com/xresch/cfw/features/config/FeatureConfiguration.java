package com.xresch.cfw.features.config;

import java.util.ArrayList;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.features.usermgmt.Role;
import com.xresch.cfw.response.bootstrap.MenuItem;
import com.xresch.cfw.spi.CFWAppFeature;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureConfiguration extends CFWAppFeature {
	
	public static final String RESOURCE_PACKAGE = "com.xresch.cfw.features.config.resources";
	
	public static final String PERMISSION_CONFIGURATION = "Configuration Management";
	
	//---------------------------------
	// Performance
	public static final String CONFIG_FILE_CACHING = "Cache Files";
	public static final String CONFIG_CPU_SAMPLING_SECONDS = "CPU Sampling Seconds";
	public static final String CONFIG_CPU_SAMPLING_AGGREGATION = "CPU Sampling Aggregation";

	//---------------------------------
	// Look & Feel
	public static final String CONFIG_LANGUAGE = "Default Language";
	public static final String CONFIG_LOGO_PATH = "Logo Path";
	public static final String CONFIG_THEME = "Theme";
	public static final String CONFIG_CODE_THEME = "Code Theme";
	public static final String CONFIG_MENU_TITLE = "Menu Title";
	public static final String CONFIG_MENU_TITLE_IN_TAB = "Menu Title in Tab";
	
	//---------------------------------
	// Backup
	public static final String CONFIG_DB_BACKUP_ENABLED = "Database Backup Enabled";
	public static final String CONFIG_DB_BACKUP_TIME = "Database Backup Starttime";
	public static final String CONFIG_DB_BACKUP_INTERVAL = "Database Backup Interval";
	public static final String CONFIG_DB_BACKUP_FOLDER = "Database Backup Folder";
	public static final String CONFIG_DB_DRIVERS = "Database Drivers";
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		//----------------------------------
		// Register Objects
		CFW.Registry.Objects.addCFWObject(Configuration.class);
    	
    	//----------------------------------
    	// Register Regular Menu
		
		CFW.Registry.Components.addAdminCFWMenuItem(
				(MenuItem)new MenuItem("Configuration", "{!cfw_core_configuration!}")
					.faicon("fas fa-cog")
					.addPermission(PERMISSION_CONFIGURATION)
					.href("/app/configuration")	
				, null);
		
	}

	@Override
	public void initializeDB() {
		
		Role adminRole = CFW.DB.Roles.selectFirstByName(CFW.DB.Roles.CFW_ROLE_ADMIN);
		//Role userRole = CFW.DB.Roles.selectFirstByName(CFW.DB.Roles.CFW_ROLE_USER);
		
		//============================================================
		// PERMISSIONS
		//============================================================
		//-----------------------------------------
		// 
		//-----------------------------------------
		if(!CFW.DB.Permissions.checkExistsByName(PERMISSION_CONFIGURATION)) {
			
			CFW.DB.Permissions.create(new Permission(PERMISSION_CONFIGURATION, FeatureUserManagement.CATEGORY_USER)
				.description("Gives the user the ability to view and update the configurations in the database.")
			);
			
			Permission permission = CFW.DB.Permissions.selectByName(PERMISSION_CONFIGURATION);
			CFW.DB.RolePermissionMap.addPermissionToRole(permission, adminRole, true);

		}
		
		//============================================================
		// CONFIGURATION
		//============================================================
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Performance", FeatureConfiguration.CONFIG_FILE_CACHING)
				.description("Enables the caching of files read from the disk.")
				.type(FormFieldType.BOOLEAN)
				.value("true")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Look and Feel", FeatureConfiguration.CONFIG_LANGUAGE)
				.description("Set the default language of the application.")
				.type(FormFieldType.LANGUAGE)
				.value("EN")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Look and Feel", FeatureConfiguration.CONFIG_THEME)
				.description("Set the application look and feel. 'Slate' is the default and recommended theme, all others are not 100% tested. For custom the file has to be placed under ./resources/css/bootstrap-theme-custom.css.")
				.type(FormFieldType.SELECT)
				.options(new String[]{"custom", "darkblue", "flatly", "lumen", "materia", "minty", "pulse", "sandstone", "simplex", "slate", "slate-edged", "spacelab", "superhero", "united", "warm-soft", "warm-edged"})
				.value("slate-edged")
		);
		
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Look and Feel", FeatureConfiguration.CONFIG_CODE_THEME)
				.description("Set the style for the code highlighting.")
				.type(FormFieldType.SELECT)
				.options(new String[]{"androidstudio", "arduino-light", "magula", "pojoaque", "sunburst", "zenburn"})
				.value("zenburn")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Look and Feel", FeatureConfiguration.CONFIG_MENU_TITLE )
				.description("Set the title displayed in the menu bar. Applies to all new sessions, login/logout required to see the change.")
				.type(FormFieldType.TEXT)
				.value("")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Look and Feel", FeatureConfiguration.CONFIG_MENU_TITLE_IN_TAB )
				.description("Set to true if you want to prepend the menu title to the title of the tab.")
				.type(FormFieldType.BOOLEAN)
				.value("false")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Look and Feel", FeatureConfiguration.CONFIG_LOGO_PATH )
				.description("The path of the logo displayed in the menu bar. Relativ to the installation directory or a valid URL.")
				.type(FormFieldType.TEXT)
				.value("/resources/images/applogo.png")
		);
		
		
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Performance", FeatureConfiguration.CONFIG_CPU_SAMPLING_SECONDS )
				.description("The interval in seconds between two CPU samplings. Changes to this value needs a restart to take effect.")
				.type(FormFieldType.SELECT)
				.options(new String[]{"0.05", "0.1", "0.2","0.5", "1", "5", "10", "30", "60"})
				.value("10")
		);
				
		//-----------------------------------------
		// 
		//-----------------------------------------
		
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Performance", FeatureConfiguration.CONFIG_CPU_SAMPLING_AGGREGATION )
				.description("The period in minutes used for the aggregation of the statistics and writing them to the database.")
				.type(FormFieldType.SELECT)
				.options(new Integer[]{3, 15, 60, 240, 720, 1440})
				.value("3")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Database Backup", FeatureConfiguration.CONFIG_DB_BACKUP_ENABLED )
				.description("Enable or disable the dackup of the database.")
				.type(FormFieldType.BOOLEAN)
				.value("true")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Database Backup", FeatureConfiguration.CONFIG_DB_BACKUP_TIME )
				.description("The start time of the backup. For example, choose a Sunday at 02:00 AM and set the interval to 7 days to create a weekly backup.")
				.type(FormFieldType.DATETIMEPICKER)
				.value("1286668800000")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Database Backup", FeatureConfiguration.CONFIG_DB_BACKUP_INTERVAL )
				.description("The interval in days to create the backup. For example, choose a Sunday at 02:00 AM and set the interval to 7 days to create a weekly backup.")
				.type(FormFieldType.NUMBER)
				.value("7")
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Database Backup", FeatureConfiguration.CONFIG_DB_BACKUP_FOLDER )
				.description("The path of the folder where the backup files should be created. (default: ./backup)")
				.type(FormFieldType.TEXT)
				.value("./backup")
		);
		
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		CFW.DB.Config.updateCache();
		
		app.addAppServlet(ServletConfiguration.class,  "/configuration");
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
