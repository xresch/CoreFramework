package com.xresch.cfw.logging;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Logger;

public class SysoutInterceptor extends PrintStream {
	
	private static final Logger logger = CFWLog.getLogger(SysoutInterceptor.class.getName());
	
	private SysoutType type;
	public enum SysoutType{ SYSOUT, SYSERR }
	
    public SysoutInterceptor(SysoutType type, OutputStream out)
    {
        super(out, true);
		this.type = type;
		
		if(type == SysoutType.SYSOUT) {
		    System.setOut(this);
		} else {
			System.setErr(this); 	 
		}
    }
    @Override
    public void print(String s)
    {
    	
    	if(type == SysoutType.SYSOUT) {
    		new CFWLog(logger)
    			.custom("sysout", "true")
    			.info(s);
    	}else {
    		new CFWLog(logger)
			.custom("sysout", "true")
			.severe(s, new Exception());
    	}
    	//propagatte to original stream
        super.print(s);
    }
}
