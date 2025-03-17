package com.xresch.cfw.tests._master;

import java.util.Locale;
import java.util.logging.Logger;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.features.config.FeatureConfig;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemMenuItem;
import com.xresch.cfw.spi.CFWAppInterface;
import com.xresch.cfw.tests.assets.servlets.FormTestServlet;
import com.xresch.cfw.tests.assets.servlets.GeneralTestServlet;
import com.xresch.cfw.tests.features.contextsettings.TestMockupContextSettings;


@Tag("development")
public class _MainForTesting implements CFWAppInterface {
		
	private static final Logger logger = CFWLog.getLogger(_MainForTesting.class.getName());
	protected static CFWLog log = new CFWLog(logger);
	
	@Test
    public void startApp() throws Exception
    {
		String[] args = new String[] {};
    	_MainForTesting main = new _MainForTesting();
    	CFW.initializeApp(main, args);
        //###################################################################
        // Initialization
        //################################################################### 
    	
    }

	@Override
	public void settings() {
		CFW.AppSettings.enableDashboarding(true);
		CFW.AppSettings.enableContextSettings(true);
		CFW.AppSettings.enableSpaces(true);
	}
	@Override
	public void register() {
		
		//---------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(WebTestMaster.RESOURCE_PACKAGE);
		CFW.Localization.registerLocaleFile(Locale.ENGLISH, "/general", new FileDefinition(HandlingType.FILE, "./testdata", "testlang_en.properties"));
		
		//---------------------------------
		// Register Menu Items
		CFW.Registry.Components.addRegularMenuItem(
				(CFWHTMLItemMenuItem)new CFWHTMLItemMenuItem("Test Pages")
					.faicon("fas fa-flask")
					.addCssClass("some-test-class")
					.addChild(new CFWHTMLItemMenuItem("General Tests").href("./general"))
					.addChild(new CFWHTMLItemMenuItem("Form Tests").faicon("fa fa-table").href("./form"))
				, null);
		

		CFW.Registry.Components.addRegularMenuItem(new CFWHTMLItemMenuItem("Menu Test"), null);
		CFW.Registry.Components.addRegularMenuItem(new CFWHTMLItemMenuItem("A").faicon("fa fa-star"), "Menu Test");
		CFW.Registry.Components.addRegularMenuItem(new CFWHTMLItemMenuItem("B").faicon("fa fa-folder-open"), " Menu Test | A ");
		CFW.Registry.Components.addRegularMenuItem(new CFWHTMLItemMenuItem("C"), " Menu Test | A | B");
		
		//---------------------------------
		// Register ContextSettings
		CFW.Registry.ContextSettings.register(TestMockupContextSettings.SETTINGS_TYPE, TestMockupContextSettings.class);
	}

	@Override
	public void initializeDB() {
		//###################################################################
        // Change Config
        //################################################################### 
    	Configuration config = CFW.DB.Config.selectBy(FeatureConfig.CATEGORY_PERFORMANCE, FeatureConfig.CONFIG_FILE_CACHING).value("false");
    	CFW.DB.Config.update(config);
		
	}

	@Override
	public void startApp(CFWApplicationExecutor app) {

        app.addUnsecureServlet(GeneralTestServlet.class, "/general");
        app.addUnsecureServlet(FormTestServlet.class, "/form");
        //###################################################################
        // Startup
        //###################################################################
        app.setDefaultURL("/general", false);
        try {
			app.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void stopApp() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startTasks() {
		// TODO Auto-generated method stub
		
	}
}

