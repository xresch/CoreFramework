package com.xresch.cfw.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.xresch.cfw._main.CFW;

/**************************************************************************************************************
 * Takes one or multiple commands that should be executed on the command line of the server.
 * 
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public class CFWCLIExecutor {
	
	
	ArrayList<ArrayList<ProcessBuilder>> pipelines = new ArrayList<>();
	
	/***************************************************************************
	 * 
	 * The cliCommands will act as follows:
	 * <ul>
	 * 		<li><b>Lines:&nbsp;</b> Each line contains a command, or multiple commands separated with the pipe symbol "|".</li>
	 * 		<li><b>Escape Newlines:&nbsp;</b> Newlines can be escaped using '\';  </li>
	 * 		<li><b>Hash Comments:&nbsp;</b>Lines starting with a Hash are considered comments. Hashes not at the beginning of the line are handled as they would be by your operating system.</li>
	 * 		<li><b>Working Directory:&nbsp;</b> Can only be specified globally. Depending on your OS. </li>
	 * </ul>
	 * @param workingDir the working directory, if null or empty will use the working directory of the current process.
	 * @param cliCommands
	 ***************************************************************************/
	public CFWCLIExecutor(String workingDir, String cliCommands) {
		
		//--------------------------------
		// Directory
		if(workingDir == null) { workingDir = ""; }
		
		File directory = null;
		if( ! workingDir.isBlank() ) {
			directory = new File(System.getProperty(workingDir));
			
			if(!directory.exists()) {
				directory.mkdirs();
			}
		}
			
		
		//because \r might be an issue
		cliCommands = cliCommands.replaceAll("\r\n", "\n");
		
		// might be multiline if it has newline in quotes
		ArrayList<String> lines = CFW.Utils.Text.splitQuotesAware("\n", cliCommands, true, true, true, true);
		
		for(String currentLine : lines) {
			
			if(currentLine.trim().startsWith("#")) { continue; }
			
			ArrayList<String> commands = CFW.Utils.Text.splitQuotesAware("|", currentLine, true, true, true, false);
			
			ArrayList<ProcessBuilder> pipeline = new ArrayList<>();
			for(String command : commands) {
				
				ArrayList<String> commandAndParams = CFW.Utils.Text.splitQuotesAware(" ", command, true, true, true, false);
				
				ProcessBuilder builder = new ProcessBuilder(commandAndParams);
				builder.redirectErrorStream(true);
				if(directory != null) {
					builder.directory(directory);
				}
				pipeline.add(builder);   
			}
			pipelines.add(pipeline);
			
		}
		
		
	}
		
	/***************************************************************************
	 * 
	 * 
	 ***************************************************************************/
	public void execute(OutputStream out) throws IOException, InterruptedException {
		
		for(ArrayList<ProcessBuilder> pipeline : pipelines) {
			
			Process last = null;
			try {
				List<Process> processes = ProcessBuilder.startPipeline(pipeline); 
				
				last = processes.get(processes.size() - 1);
	
			    last.getInputStream().transferTo(out);
			    last.waitFor();
			}finally {
				if(last != null) {
					last.getInputStream().close();
				}
			}
		}
	}
		
}


