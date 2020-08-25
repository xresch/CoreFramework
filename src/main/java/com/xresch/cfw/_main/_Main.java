package com.xresch.cfw._main;

import java.util.logging.Logger;

import com.xresch.cfw.logging.CFWLog;

public class _Main {
		
	public static final Logger logger = CFWLog.getLogger(_Main.class.getName());
	protected static CFWLog log = new CFWLog(logger);
	
    public static void main( String[] args ) throws Exception
    {
    	
    	//------------------------------------
    	// Load application Extension
    	CFWAppInterface app = CFW.loadExtentionApplication();
    	
    	//------------------------------------
    	// Create empty Default if null
    	if(app == null) {
    		app = new CFWAppInterface() {
    			@Override public void settings() { CFW.AppSettings.setEnableDashboarding(true); }
				@Override public void startApp(CFWApplicationExecutor executor) { executor.setDefaultURL("/dashboard/list", true); }
				@Override public void register() { /* Do nothing */ }
				@Override public void initializeDB() { /* Do nothing */ }
				@Override public void startTasks() { /* Do nothing */ }
				@Override public void stopApp() { /* Do nothing */ }
			};
    	}
    	
    	//------------------------------------
    	// Start Application
    	CFW.initializeApp(app, args);

    }

}

