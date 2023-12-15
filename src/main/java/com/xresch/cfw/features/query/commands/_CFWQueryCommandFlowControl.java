package com.xresch.cfw.features.query.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryCommand;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.CFWQueryParser;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartArray;
import com.xresch.cfw.features.query.parse.QueryPartAssignment;
import com.xresch.cfw.features.query.parse.QueryPartGroup;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.pipeline.PipelineActionContext;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public abstract class _CFWQueryCommandFlowControl extends CFWQueryCommand {
	

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public _CFWQueryCommandFlowControl(CFWQuery parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}
	
	/***********************************************************************************************
	 * The input channel for other flow control elements, if they have not yet handled the 
	 * record given in the parameter.
	 * 
	 * @param object to add to the queue, null to signalize that the last record is reached 
	 * 
	 ***********************************************************************************************/
	public abstract void putIntoFlowControlQueue(EnhancedJsonObject object) throws Exception;

}
