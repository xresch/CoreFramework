package com.pengtoolbox.cfw.logging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;

/**************************************************************************************************************
 * Handler that provides async log handling, extends FileHandler.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class AsyncLogHandler extends FileHandler implements Runnable{

	private static ArrayBlockingQueue<LogRecord> logQueue = new ArrayBlockingQueue<>(5000);
	
	private static boolean runWorker = true;
	private static Thread worker;
	private static int intervalMillis = 50;
	
	public AsyncLogHandler() throws SecurityException, IOException{
		super();
		this.setFormatter(new LogFormatterJSON());
		worker = new Thread(this);
		
		worker.start();
	}
	
	public void publish(LogRecord record){
		try {
			logQueue.put(record);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void run(){
				
		while(runWorker){
			
			if(logQueue.isEmpty()){
			
				try {
					Thread.sleep(intervalMillis);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}else{
				ArrayList<LogRecord> records = new ArrayList<LogRecord>();
				logQueue.drainTo(records);
				
				for(LogRecord rec : records){
					super.publish(rec);
				}
			}
		}
				
	}
}
