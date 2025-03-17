package com.xresch.cfw.features.query.sources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.analytics.FeatureSystemAnalytics;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemAlertMessage.MessageType;
import com.xresch.cfw.utils.FileBackwardsInputReader;
import com.xresch.cfw.utils.json.JsonTimerangeChecker;
	
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourceApplog extends CFWQuerySource {
	
	private static Logger logger = CFWLog.getLogger(CFWQuerySourceApplog.class.getName());
	
	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceApplog(CFWQuery parent) {
		super(parent);
	}

	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String uniqueName() {
		return "applog";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Fetches log events from the last application log file.";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionTime() {
		return "Time from time range picker is automatically applied. No special handling required.";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".sources", "source_applog.html");
	}
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionRequiredPermission() {
		return FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS;
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission( FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS);
	}
		
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		// do nothing
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public CFWObject getParameters() {
		return new CFWObject()
				.addField(
						CFWField.newString(FormFieldType.TEXT, "scope")
							.setDescription("The scope of the log scanning. 'last' searches in the last log file, 'all' (default) will search in all log files.")
							.setValue("all")
					)
			;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public void parametersPermissionCheck(CFWObject parameters) throws ParseException {
		//do nothing
	}
	
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public void execute(CFWObject parameters, LinkedBlockingQueue<EnhancedJsonObject> outQueue, long earliestMillis, long latestMillis, int limit) throws Exception {
		
		//-----------------------------------
		// Preparations
		String scope = (String)parameters.getField("scope").getValue();

		TimeZone machineZone = CFW.Time.getMachineTimeZone();
		JsonTimerangeChecker timerangeChecker = 
				new JsonTimerangeChecker("time", CFW.Time.FORMAT_TIMESTAMP, machineZone, earliestMillis, latestMillis)
					.epochAsNewField("_epoch");
		
		
		//-----------------------------------
		// Get Last Log File
		ArrayList<File> fileArray = CFWLog.getAllLogfiles();
		if(fileArray.size() == 0) {
			return;
		}
		
		if(!scope.trim().equalsIgnoreCase("all")) {
			File newestFile = fileArray.get(0);
			fileArray.clear();
			fileArray.add(newestFile);
			System.out.println(newestFile.getName());
		}
		
		//-----------------------------------
		// Process Log
		int recordCounter = 0;
		for(File logFile : fileArray) {
			
			//-----------------------------------
			// Worth to process this log?
			if(!isFileInTimeRange(timerangeChecker, logFile)) {
				continue;
			}
			
			//-----------------------------------
			// Read the log
			int errorCounter = 0;
			try (BufferedReader reader = new BufferedReader (new InputStreamReader (new FileBackwardsInputReader(logFile))) ){
			
				while(true) {
					String currentLine = reader.readLine();
					
					if(currentLine == null) {
						break;
					}
					
					if( isLimitReached(limit, recordCounter)) { break; }
					
					try {
						JsonElement element = CFW.JSON.fromJson(currentLine);
					
						if(element != null && element.isJsonObject()) {
							if(timerangeChecker.isInTimerange(element.getAsJsonObject(), false)) {
								recordCounter++;
								outQueue.add( new EnhancedJsonObject(element.getAsJsonObject()) );
							}
						}
						
					}catch(Throwable t) {
						if(errorCounter == 0) {
							this.parent.getContext().addMessage(MessageType.WARNING, "Some log line could not be parsed: "+currentLine);
						}
						errorCounter++;
					}
					
				}
			}
		}
	
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private boolean isFileInTimeRange(JsonTimerangeChecker timerangeChecker, File logFile) {
		
		boolean hasLogsInRange = false;

		//--------------------------------------
		// Check Last Line in File
		try (BufferedReader reader = new BufferedReader (new InputStreamReader (new FileBackwardsInputReader(logFile))) ){
			
			String currentLine = reader.readLine();
			
			if(currentLine != null) {
				try {
					JsonElement element = CFW.JSON.fromJson(currentLine);
					if(element != null && element.isJsonObject()) {
						if(timerangeChecker.isInTimerange(element.getAsJsonObject(), false)) {
							hasLogsInRange = true;
						}
					}
				}catch(Throwable t){
					// do nothing
				}
			}
		} catch (Exception e) {
			new CFWLog(logger).severe(e);
		} 
		
		//--------------------------------------
		// Check First Line in File
		if(!hasLogsInRange) {
			try (BufferedReader reader = new BufferedReader (new InputStreamReader (new FileInputStream(logFile))) ){
				
				String currentLine = reader.readLine();
				
				if(currentLine != null) {
					try {
						JsonElement element = CFW.JSON.fromJson(currentLine);
						if(element != null && element.isJsonObject()) {
							if(timerangeChecker.isInTimerange(element.getAsJsonObject(), false)) {
								hasLogsInRange = true;
							}
						}
					}catch(Throwable t){
						// do nothing
					}
				}
			} catch (Exception e) {
				new CFWLog(logger).severe(e);
			} 
		}
		
		return hasLogsInRange;
	}

}
