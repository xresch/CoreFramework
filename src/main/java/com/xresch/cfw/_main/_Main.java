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
    	// Start Application
    	CFW.initializeApp(app, args);

    }

}

