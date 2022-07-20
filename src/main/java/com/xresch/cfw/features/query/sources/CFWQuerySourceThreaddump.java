package com.xresch.cfw.features.query.sources;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.ParseException;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.analytics.FeatureSystemAnalytics;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWAuditLogDBMethods;
import com.xresch.cfw.utils.ResultSetUtils.ResultSetAsJsonReader;
import com.xresch.cfw.utils.json.JsonTimerangeChecker;
	
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourceThreaddump extends CFWQuerySource {


	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceThreaddump(CFWQuery parent) {
		super(parent);
	}

	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String uniqueName() {
		return "threaddump";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Creates a threaddump and returns the information as records.";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionTime() {
		return "Time is ignored.";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".sources", "source_threaddump.html");
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
		return user.hasPermission(FeatureSystemAnalytics.PERMISSION_SYSTEM_ANALYTICS);
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
		return new CFWObject();
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
				
        final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        final ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true); //getThreadInfo(threadMXBean.getAllThreadIds(), 100);
        
        for (ThreadInfo info : threadInfos) {

        	StackTraceElement[] elements = info.getStackTrace();

        	for(StackTraceElement element : elements) {
            	JsonObject currentObject = new JsonObject();
            	
	        	currentObject.addProperty("THREAD_ID", info.getThreadId());
	        	currentObject.addProperty("THREAD_NAME", info.getThreadName());
	        	currentObject.addProperty("THREAD_STATE", info.getThreadState().name());

	        	currentObject.addProperty("BLOCKING_THREAD", info.getLockOwnerId());
	        	currentObject.addProperty("STACK_ELEMENT", element.toString());
	        	
	        	outQueue.add( new EnhancedJsonObject(currentObject) );
        	}
        }			
	}
}
