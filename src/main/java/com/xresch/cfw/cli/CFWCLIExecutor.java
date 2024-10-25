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
public class CFWCLIExecutor implements Runnable {
	
	
	private ArrayList<ArrayList<ProcessBuilder>> pipelines = new ArrayList<>();
	
	private OutputStream out;
	
	private boolean isCompleted = false;
	
	Thread thread;
	
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
	public CFWCLIExecutor(String workingDir, String cliCommands, OutputStream out) {
		
		this.out = out;
		//--------------------------------
		// Directory
		if(workingDir == null) { workingDir = ""; }
		
		File directory = null;
		if( ! workingDir.isBlank() ) {
			directory = new File(workingDir);
			
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
				
				if(command.isBlank()) { continue; }
				
				ArrayList<String> commandAndParams = CFW.Utils.Text.splitQuotesAware(" ", command.trim(), true, true, true, false);
				
				ProcessBuilder builder = new ProcessBuilder(commandAndParams);
				builder.redirectErrorStream(true);
				
				if(directory != null) {
					builder.directory(directory);
				}
				pipeline.add(builder);   
			}
			if(!pipeline.isEmpty()) {
				pipelines.add(pipeline);
			}
			
		}
		
		
	}
		
	/***************************************************************************
	 * 
	 * 
	 ***************************************************************************/
	public void execute() throws IOException, InterruptedException {
		
		thread = new Thread(this);
		
		thread.start();
	}
	
	/***************************************************************************
	 * 
	 * 
	 ***************************************************************************/
	public void waitForCompletionOrTimeout(long timeoutSeconds) {
		
		long timeoutMillis = timeoutSeconds * 1000;
		try {
			long starttime = System.currentTimeMillis();
			
			while(!isCompleted) {
				Thread.sleep(20);
				
				if( (System.currentTimeMillis() - starttime) >= timeoutMillis ) {
					thread.interrupt();
					break;
				}
			}	
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	/***************************************************************************
	 * 
	 * 
	 ***************************************************************************/
	@Override
	public void run() {
		
		isCompleted = false;
		
		try {
			for(ArrayList<ProcessBuilder> pipeline : pipelines) {

				Process last = null;
				try {
					List<Process> processes = ProcessBuilder.startPipeline(pipeline); 
					
					last = processes.get(processes.size() - 1);				    
				    
				    BufferedReader reader = new BufferedReader( new InputStreamReader(last.getInputStream()) );
				    InputStream lastStream = last.getInputStream();
				  
				    //---------------------
				    // Read the Output
				    String line;
				    while((line = reader.readLine()) != null) {
				    	//System.out.println("data: "+line);
				    	out.write((line+"\n").getBytes());
				    }

				}finally {
					if(last != null) {
						last.getInputStream().close();
					}
					
					
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			isCompleted = true;
		}
		
	}
		
}


