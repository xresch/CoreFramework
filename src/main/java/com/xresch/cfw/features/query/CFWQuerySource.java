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
	 * Return a short description that can be shown in content assist and will be used as intro text
	 * in the manual. Do not use newlines in this description.
	 ***********************************************************************************************/
	public abstract String descriptionShort();
	
	/***********************************************************************************************
	 * Return a CFWObject with the parameters you will support.
	 * The source command will map the parameters to the object and execute the validation. If
	 * all parameter values are valid, it will be passed to the execute() method.
	 * Make sure to add a description for every parameter and a default value.
	 ***********************************************************************************************/
	public abstract CFWObject getParameters();
	
	/***********************************************************************************************
	 * Return the description for the manual page.
	 * This description will be shown on the manual under the header " <h2>Usage</h2>".
	 * If you add headers to your description it is recommended to use <h3> or lower headers.
	 ***********************************************************************************************/
	public abstract String descriptionHTML();
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public abstract void execute(CFWObject parameters, LinkedBlockingQueue<EnhancedJsonObject> outQueue, int limit ) throws Exception;
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public CFWQuery getParent() {
		return parent;
	}
	
}
