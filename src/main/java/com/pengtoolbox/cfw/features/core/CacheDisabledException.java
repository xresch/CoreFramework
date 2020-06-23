package com.pengtoolbox.cfw.features.core;

/***********************************************************
 * used to disable guava caches.
 * 
 * @author Reto Scheiwiller
 ***********************************************************/
public class CacheDisabledException extends Throwable {

	private static final long serialVersionUID = 1L;
	
	public CacheDisabledException(){
		super(null, null,false, false);
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}
}
