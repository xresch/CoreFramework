package com.xresch.cfw.features.analytics;

import java.io.File;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.schedule.CFWScheduledTask;

public class TaskThreadDumps extends CFWScheduledTask {
	
	@Override
	public void execute() {
		
        File folder = new File("./threaddumps");
        
        if(!folder.exists()) {
        	folder.mkdirs();
        }
        
        String filepath = "threaddump_"+CFW.Time.currentTimestamp()+".txt";
        CFW.Files.writeFileContent(null, filepath, filepath);
	    
	}
	
}
