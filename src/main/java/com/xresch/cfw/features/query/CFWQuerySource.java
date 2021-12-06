package com.xresch.cfw.features.query;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.JsonObject;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.pipeline.PipelineActionContext;

public abstract class CFWQuerySource{

	protected CFWQuery parent;
	
	public CFWQuerySource(CFWQuery parent) {
		this.parent = parent;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public abstract String uniqueName();
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public abstract String shortDescription();
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public abstract CFWObject getParameters();
	
	public abstract void execute(CFWObject parameters, LinkedBlockingQueue<JsonObject> outQueue ) throws Exception;
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQuery getParent() {
		return parent;
	}
	
}
