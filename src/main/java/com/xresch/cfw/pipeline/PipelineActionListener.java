package com.xresch.cfw.pipeline;

/**************************************************************************************************************
 * Used to listen and act on certain states a Pipeline action reaches.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 * 
 **************************************************************************************************************/
public abstract class PipelineActionListener {

	/*****************************************************
	 * Will be called when the Pipeline action sets its
	 * status to done.
	 *****************************************************/
	public abstract void onDone();
	
}
