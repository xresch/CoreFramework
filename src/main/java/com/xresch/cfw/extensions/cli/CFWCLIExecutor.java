package com.xresch.cfw.extensions.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.common.collect.EvictingQueue;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.utils.CFWMonitor;
import com.xresch.cfw.utils.CFWReadableOutputStream;

/**************************************************************************************************************
 * Takes one or multiple commands that should be executed on the command line of the server.
 * 
 * The cliCommands will act as follows:
 * <ul>
 * 		<li><b>Lines:&nbsp;</b> Each line contains a command, or multiple commands separated with the pipe symbol "|".</li>
 * 		<li><b>Escape Newlines:&nbsp;</b> Newlines can be escaped using '\';  </li>
 * 		<li><b>Hash Comments:&nbsp;</b>Lines starting with a Hash are considered comments. Hashes not at the beginning of the line are handled as they would be by your operating system.</li>
 * 		<li><b>Working Directory:&nbsp;</b> Can only be specified globally. Depending on your OS. </li>
 * </ul>
 * 
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 **************************************************************************************************************/
public class CFWCLIExecutor extends Thread {
	
	private static Logger logger = CFWLog.getLogger(CFWCLIExecutor.class.getName());
	
	
	private Map<String,String> envVariables = null;
	private CFWMonitor monitor = null;
	private ArrayList<ArrayList<ProcessBuilder>> pipelines = new ArrayList<>();
	
	CFWReadableOutputStream out = new CFWReadableOutputStream();
	
	private boolean isInterrupted = false;
	private boolean isCompleted = false;
	
	private Exception exceptionDuringRun = null;
	
	
	/***************************************************************************
	 * 
	 * @param workingDir the working directory, if null or empty will use the working directory of the current process.
	 * @param cliCommands see class documentation for details.
	 * @param out the stream where stdout and stderr should be written.
	 * 
	 ***************************************************************************/
	public CFWCLIExecutor(String workingDir, String cliCommands) {
		this(workingDir, cliCommands, null);
	}
	
	/***************************************************************************
	 * 
	 * @param workingDir the working directory, if null or empty will use the working directory of the current process.
	 * @param cliCommands see class documentation for details.
	 * @param out the stream where stdout and stderr should be written.
	 ***************************************************************************/
	public CFWCLIExecutor(String workingDir, String cliCommands, Map<String,String> envVariables) {

		this.envVariables = envVariables;
		
		//--------------------------------
		// Directory
		if(Strings.isNullOrEmpty(workingDir)) {
			workingDir = CFW.DB.Config.getConfigAsString(FeatureCLIExtensions.CONFIG_CATEGORY, FeatureCLIExtensions.CONFIG_DEFAULT_WORKDIR); 
		}
		
		File directory = null;
		if( workingDir != null && ! workingDir.isBlank() ) {
			directory = new File(workingDir);
			
			if(!directory.exists()) {
				directory.mkdirs();
			}
		}
			
		parseCommandsCreatePipelines(cliCommands, directory);
			
	}
	
	/***************************************************************************
	 * 
	 * 
	 ***************************************************************************/
	public CFWReadableOutputStream getOutputStream() {
		return out;
	}
	
	/***************************************************************************
	 * 
	 * 
	 ***************************************************************************/
	private void parseCommandsCreatePipelines(String cliCommands, File directory) {
		//because \r might be an issue
		cliCommands = cliCommands.replaceAll("\r\n", "\n");
		
		// might be multiline if it has newline in quotes
		ArrayList<String> lines = CFW.Utils.Text.splitQuotesAware("\n", cliCommands, true, true, true, true);
		
		for(String currentLine : lines) {
			
			if(currentLine.trim().startsWith("#")) { continue; }
			
			ArrayList<String> commands = CFW.Utils.Text.splitQuotesAware("|", currentLine, true, true, true, true);
			
			ArrayList<ProcessBuilder> pipeline = new ArrayList<>();
			for(String command : commands) {
				
				if(command.isBlank()) { continue; }
				
				ArrayList<String> commandAndParams = CFW.Utils.Text.splitQuotesAware(" ", command.trim(), true, true, true, false);
				
				ProcessBuilder builder = new ProcessBuilder(commandAndParams);
				builder.redirectErrorStream(true);
				
				if(directory != null) {		builder.directory(directory); }
				if(envVariables != null) { 	builder.environment().putAll(envVariables);}
				
				
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
		
		this.start();
	}
	
	/***************************************************************************
	 * 
	 * 
	 ***************************************************************************/
	public void waitForCompletionOrTimeout(long timeoutSeconds) throws Exception {
		
		long timeoutMillis = timeoutSeconds * 1000;
		try {
			long starttime = System.currentTimeMillis();
			
			while(!isCompleted && !Thread.interrupted()) {
				Thread.sleep(20);
				
				if( (System.currentTimeMillis() - starttime) >= timeoutMillis ) {
					this.interrupt();
					break;
					//throw new Exception("Timeout of "+timeoutSeconds+" seconds reached while executing command line.");
				}
			}
			
			if(exceptionDuringRun != null) {
				throw exceptionDuringRun;
			}
			
		} catch (InterruptedException e) {
			new CFWLog(logger).severe("Thread got interrupted while executing CLI commands.", e);
		}

	}
	
	/***************************************************************************
	 * 
	 * 
	 ***************************************************************************/
	public String readOutputOrTimeout(long timeoutSeconds, int head,int tail, boolean addSkippedCount) throws Exception {
		
		long timeoutMillis = timeoutSeconds * 1000;
		boolean isReadAll = (head <= 0) && (tail <= 0);
		
		StringBuilder result = new StringBuilder();
		try {
			long starttime = System.currentTimeMillis();
			
			int linesReadHead = 0;
			int skippedCount = 0;
			EvictingQueue<String> tailedLines = EvictingQueue.create(tail);

			while(!isCompleted && !Thread.interrupted()) {
				Thread.sleep(20);
				
				//-----------------------------
				// Check Timeout
				if( (System.currentTimeMillis() - starttime) >= timeoutMillis ) {
					this.interrupt();
					break;
				}
				
				//----------------------------------
				// Read Head
				while(out.hasLine() && (isReadAll || linesReadHead < head ) ) {
					result.append(out.readLine()).append("\n");
					linesReadHead++;
				}
								
				//----------------------------------
				// Read Tail
				if(isReadAll || linesReadHead >= head) {
					
					while(out.hasLine() ) {
						tailedLines.add(out.readLine());
						if(tailedLines.size() >= tail) {
							skippedCount++;
						}
					}
				}
				
				if(exceptionDuringRun != null) { throw exceptionDuringRun; }
				
				if(Thread.interrupted()) { this.interrupt(); }

			}
			
			//----------------------------------
			// Add Skipped Count
			if( !isReadAll && addSkippedCount) {
				
				if(skippedCount > 1) {
					result.append("[... "+(skippedCount-1)+" lines skipped ...]\n");
				}
			}
			
			//----------------------------------
			// Read Tail
			while( ! tailedLines.isEmpty() ) {
				result.append(tailedLines.poll()).append("\n");
			}

		} catch (InterruptedException e) {
			this.interrupt();
		}
		
		return result.toString();

	}
	
	/***************************************************************************
	 * 
	 * 
	 ***************************************************************************/
	public String waitForCompletionAndReadOutput(long timeoutSeconds, int headLines, int tailLines) throws Exception {
		
		StringBuilder result = new StringBuilder();
		
		long timeoutMillis = timeoutSeconds * 1000;
		try {
			long starttime = System.currentTimeMillis();
			
			while(!isCompleted && !Thread.interrupted()) {
				Thread.sleep(20);
				
				if( (System.currentTimeMillis() - starttime) >= timeoutMillis ) {
					this.interrupt();
					break;
				}
			}
						
			if(exceptionDuringRun != null) { throw exceptionDuringRun; }
			
			if(Thread.interrupted()) { this.interrupt(); }
			
		} catch (InterruptedException e) {
			this.interrupt();
		}
		
		return result.toString();

	}
	
	/***************************************************************************
	 * 
	 * 
	 ***************************************************************************/
	@Override
	public void interrupt() {
		isInterrupted = true;
		super.interrupt();
	}
	/***************************************************************************
	 * 
	 * 
	 ***************************************************************************/
	@Override
	public void start() {
		
		isCompleted = false;
		exceptionDuringRun = null;
		
		List<Process> processes = null;
		try {
			for(ArrayList<ProcessBuilder> pipeline : pipelines) {

				Process last = null;
				try {
					processes = ProcessBuilder.startPipeline(pipeline); 
					
					last = processes.get(processes.size() - 1);				    
				    
				    BufferedReader reader = new BufferedReader( new InputStreamReader(last.getInputStream()) );
				    InputStream lastStream = last.getInputStream();
				  
				    //---------------------
				    // Read the Output
				    String line;
				    while((line = reader.readLine()) != null && !isInterrupted) {
				    	//System.out.println("data: "+line);
				    	out.write((line+"\n").getBytes());
				    }
				    
				    

				}finally {
					
					if(processes != null) {
						for(Process p : processes) {
							p.destroy();
						}
					}
					
					if(last != null) {
						last.getInputStream().close();
					}
					

					
					
				}
			}
		}catch(Exception e) {
			
			exceptionDuringRun = e;
		}finally {
			isCompleted = true;
		}
		
	}
		
}


