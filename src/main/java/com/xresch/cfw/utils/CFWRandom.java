package com.xresch.cfw.utils;

import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.xrutils.utils.XRRandom;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2020 
 **************************************************************************************************************/
public class CFWRandom extends XRRandom {
	
	/******************************************************************************
	 * Creates a random Message Type.
	 ******************************************************************************/
	public static MessageType messageType() { 
		return fromArray(MessageType.values());
	}
	
}
