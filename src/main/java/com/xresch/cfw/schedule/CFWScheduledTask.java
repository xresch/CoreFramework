package com.xresch.cfw.schedule;

import java.util.TimerTask;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;

public abstract class CFWScheduledTask extends TimerTask {
	
	private static Logger logger = CFWLog.getLogger(CFWScheduledTask.class.getName());
	@Override
		
	public void run() {
		
		// Remove Request instance if they were propagated to prevent CFWLog Exceptions
		CFW.Context.Request.setRequest(null);
		
		try {
			execute();
		}catch(Throwable t) {
			new CFWLog(logger)
				.method("run")
				.severe("Exception occured while running scheduled task.", t);
		}
			
	}
	
	public abstract void execute();

}
