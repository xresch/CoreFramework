package com.xresch.cfw.utils;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import com.xresch.cfw._main.CFW;

public class CFWUtilsAnalysis {

	/************************************************************
	 * Creates a thread dump to the disk. 
	 ************************************************************/
	public static void threadDumpToDisk(String folder, String filepath) {
		File folderFile = new File(folder);
        
        if(!folderFile.exists()) {
        	folderFile.mkdirs();
        }
        
        
        
        CFW.Files.writeFileContent(null, folder+"/"+filepath, createThreadDump());
	    
	}
	
	public static String createThreadDump() {
		
        final StringBuilder dump = new StringBuilder();
        final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        final ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true); //getThreadInfo(threadMXBean.getAllThreadIds(), 100);
        for (ThreadInfo threadInfo : threadInfos) {
        	dump.append(threadInfo.toString());
        }
        
        return dump.toString();
	    
	}
}
