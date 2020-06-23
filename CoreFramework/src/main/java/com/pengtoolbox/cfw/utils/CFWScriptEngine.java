package com.pengtoolbox.cfw.utils;

import java.util.logging.Logger;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import com.pengtoolbox.cfw.logging.CFWLog;

public class CFWScriptEngine {
	
	private ScriptEngine engine = null;
	public static Logger logger = CFWLog.getLogger(CFWScriptEngine.class.getName());
	
	public CFWScriptEngine(ScriptEngine engine) {
		this.engine = engine;
	}
	
	/******************************************************************************************************
	 * Add the defined script to this engine.
	 * 
	 * @param script that should be loaded containing the methods that should be executed.
	 * @param methodName the name of the method to be executed
	 * @param parameters values to be passed to method.
	 ******************************************************************************************************/
	public CFWScriptEngine addScript(String script) {
		try {
			engine.eval(script);
		} catch (ScriptException e) {
			new CFWLog(logger)
				.method("executeJavascript")
				.severe("An exception occured while executing a javascript: "+e.getMessage(), e);
				e.printStackTrace();
		}
		return this;
	}
	
	/******************************************************************************************************
	 * Execute a javascript call with defined parameters.
	 * 
	 * @param script that should be loaded containing the methods that should be executed.
	 * @param methodName the name of the method to be executed
	 * @param parameters values to be passed to method.
	 ******************************************************************************************************/
	public Object executeJavascript(String methodName, Object... parameters) {
		
		//-----------------------------------
		// Create Invocable
		Invocable invocableEngine = (Invocable) engine;
		
		//-----------------------------------
		// Execute Script Engine
		try {
			Object result = invocableEngine.invokeFunction(methodName, parameters);
			
			return result;
			
		} catch (NoSuchMethodException e) {
			new CFWLog(logger)
				.method("executeJavascript")
				.severe("The method '"+methodName+"' doesn't exist. ", e);
		} catch (ScriptException e) {
			new CFWLog(logger)
				.method("executeJavascript")
				.severe("An exception occured while executing a javascript: "+e.getMessage(), e);
				e.printStackTrace();
		}
		
		return null;
	}
	/******************************************************************************************************
	 * Execute a javascript call with defined parameters.
	 * 
	 * @param script that should be loaded containing the methods that should be executed.
	 * @param methodCallWithParams a string representation of the function call, e.g. "foobar('Test')"
	 ******************************************************************************************************/
	public Object executeJavascript(String methodCallWithParams) {
				
		//-----------------------------------
		// Execute Script Engine
		try {
			Object result = engine.eval(methodCallWithParams);
			
			return result;
			
		} catch (ScriptException e) {
			new CFWLog(logger)
			.method("executeJavascript")
			.severe("An exception occured while executing a javascript: "+e.getMessage(), e);
			e.printStackTrace();
		}
		
		return null;
	}
	
}
