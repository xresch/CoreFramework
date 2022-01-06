package com.xresch.cfw.features.query;

import java.util.concurrent.LinkedBlockingQueue;

import com.xresch.cfw.datahandling.CFWObject;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
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
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public abstract void execute(CFWObject parameters, LinkedBlockingQueue<EnhancedJsonObject> outQueue ) throws Exception;
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQuery getParent() {
		return parent;
	}
	
}