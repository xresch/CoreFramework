package com.xresch.cfw._main;

public class _Main {
			
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

