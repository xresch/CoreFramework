package com.xresch.cfw._main;

import com.xresch.cfw.spi.CFWAppInterface;

public class _Main {
			
    public static void main( String[] args ) throws Exception
    {
    	
    	//------------------------------------
    	// Load application Extension
    	CFWAppInterface app = CFW.loadExtensionApplication();
    	    	
    	//------------------------------------
    	// Start Application
    	CFW.initializeApp(app, args);

    }

}

