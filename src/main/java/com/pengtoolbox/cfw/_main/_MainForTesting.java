package com.pengtoolbox.cfw._main;

import java.util.Locale;
import java.util.logging.Logger;

import com.pengtoolbox.cfw.caching.FileDefinition;
import com.pengtoolbox.cfw.caching.FileDefinition.HandlingType;
import com.pengtoolbox.cfw.features.config.Configuration;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.bootstrap.MenuItem;
import com.pengtoolbox.cfw.tests.assets.servlets.FormTestServlet;
import com.pengtoolbox.cfw.tests.assets.servlets.GeneralTestServlet;

public class _MainForTesting implements CFWAppInterface {
		
	public static Logger logger = CFWLog.getLogger(_MainForTesting.class.getName());
	protected static CFWLog log = new CFWLog(logger);
	
    public static void main( String[] args ) throws Exception
    {
    	_MainForTesting main = new _MainForTesting();
    	CFW.initializeApp(main, args);
        //###################################################################
        // Initialization
        //################################################################### 
    	
    }

	@Override
	public void settings() {
		
	}
	@Override
	public void register() {
		
		CFW.Localization.registerLocaleFile(Locale.ENGLISH, "/general", new FileDefinition(HandlingType.FILE, "./testdata", "testlang_en.properties"));
		
		CFW.Registry.Components.addRegularMenuItem(
				(MenuItem)new MenuItem("Test Pages")
					.faicon("fas fa-flask")
					.addCssClass("some-test-class")
					.addChild(new MenuItem("General Tests").href("./general"))
					.addChild(new MenuItem("Form Tests").faicon("fa fa-table").href("./form"))
				, null);
		
		CFW.Registry.Components.addRegularMenuItem(new MenuItem("Menu Test"), null);
		CFW.Registry.Components.addRegularMenuItem(new MenuItem("A").faicon("fa fa-star"), "Menu Test");
		CFW.Registry.Components.addRegularMenuItem(new MenuItem("B").faicon("fa fa-folder-open"), " Menu Test | A ");
		CFW.Registry.Components.addRegularMenuItem(new MenuItem("C"), " Menu Test | A | B");
	}

	@Override
	public void initializeDB() {
		//###################################################################
        // Change Config
        //################################################################### 
    	Configuration config = CFW.DB.Config.selectByName(Configuration.FILE_CACHING).value("false");
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

