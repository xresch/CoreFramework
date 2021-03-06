package com.xresch.cfw.utils;

import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerWrapper;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class HandlerChainBuilder {
	
	private HandlerWrapper current = null;
	
	public HandlerChainBuilder(HandlerWrapper handler) {
		current = handler;
	}
	
	public HandlerChainBuilder chain(HandlerWrapper handler) {
		
		if(current != null) {
			current.setHandler(handler);
		}
		
		current = handler;
		
		return this;
	}
	
	public void chainLast(HandlerCollection collection) {
		
		if(current != null) {
			current.setHandler(collection);
		}
		
	}

}
