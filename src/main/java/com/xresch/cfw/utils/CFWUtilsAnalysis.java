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
		File folderFile = new File("folder");
        
        if(!folderFile.exists()) {
        	folderFile.mkdirs();
        }
        
        CFW.Files.writeFileContent(null, folder+"/"+filepath, createThreadDump());
	    
	}
	
	public static String createThreadDump() {
		
        final StringBuilder dump = new StringBuilder();
        final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        final ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(), 100);
        for (ThreadInfo threadInfo : threadInfos) {
            dump.append('"');
            dump.append(threadInfo.getThreadName());
            dump.append("\" ");
            final Thread.State state = threadInfo.getThreadState();
            dump.append("\n   java.lang.Thread.State: ");
            dump.append(state);
            final StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
            for (final StackTraceElement stackTraceElement : stackTraceElements) {
                dump.append("\n        at ");
                dump.append(stackTraceElement);
            }
            dump.append("\n\n");
        }
        
        return dump.toString();
	    
	}
}
