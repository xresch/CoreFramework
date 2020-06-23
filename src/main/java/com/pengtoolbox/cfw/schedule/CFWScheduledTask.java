package com.pengtoolbox.cfw.schedule;

import java.util.TimerTask;
import java.util.logging.Logger;

import com.pengtoolbox.cfw.logging.CFWLog;

public abstract class CFWScheduledTask extends TimerTask {
	
	private static Logger logger = CFWLog.getLogger(CFWScheduledTask.class.getName());
	@Override
	public void run() {
		
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
