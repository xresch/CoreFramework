package com.xresch.cfw.utils;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;

public class CFWPolyglotContext {
	
	private Context polyglot = null;
	private String language = null;
	private static final Logger logger = CFWLog.getLogger(CFWPolyglotContext.class.getName());
	
	public CFWPolyglotContext(String language, Context context) {
		this.language = language;
		this.polyglot = context;
		
	}
	
	/******************************************************************************************************
	 * Add the defined script to this engine.
	 * 
	 * @param script that should be loaded containing the methods that should be executed.
	 * @param methodName the name of the method to be executed
	 * @param parameters values to be passed to method.
	 ******************************************************************************************************/
	public CFWPolyglotContext addScript(String scriptname, String script) {
			synchronized (polyglot) {
				Source source = Source.newBuilder(language, script, scriptname).buildLiteral();
				polyglot.eval(source);
			}
		return this;
	}
	
	/******************************************************************************************************
	 * Execute a javascript call with defined parameters.
	 * 
	 * @param script that should be loaded containing the methods that should be executed.
	 * @param methodName the name of the method to be executed
	 * @param parameters values to be passed to method, only strings and primitives.
	 ******************************************************************************************************/
	public Value executeFunction(String methodName, Object... parameters) {
		
		//-----------------------------------
		// Create method call
		String methodCallWithParams = methodName + "(";
		
		for(Object parameter : parameters) {
			
			if(parameter instanceof String) {
				methodCallWithParams += CFW.JSON.toJSON(parameter)+",";
			}else {
				methodCallWithParams += parameter.toString()+",";
			}
				
		}
		methodCallWithParams.substring(0, methodCallWithParams.length()-1);
		methodCallWithParams += ");";
		
		//-----------------------------------
		// Execute Script Engine
		Value result = polyglot.eval(language, methodCallWithParams);
			
		return result;
		
	}
	
	/******************************************************************************************************
	 * Execute a javascript call with defined parameters.
	 * 
	 * @param script that should be loaded containing the methods that should be executed.
	 * @param methodCallWithParams a string representation of the function call, e.g. "foobar('Test')"
	 ******************************************************************************************************/
	public Value executeScript(String methodCallWithParams) {
				
		//-----------------------------------
		// Execute Script Engine
		Value result = polyglot.eval(language, methodCallWithParams);
			
		return result;

	}

	public Context getWrappedContext() {
		return polyglot;
	}

	public String getLanguage() {
		return language;
	}
	
	public Value getBindings() {
		return polyglot.getBindings(language);
	}
	
	public CFWPolyglotContext putMemberWithFunctions(Object objectToBind ) {
		
		String classname = objectToBind.getClass().getSimpleName();
		polyglot.getBindings(language).putMember(classname, objectToBind);
		
		for(Method method : objectToBind.getClass().getMethods()) {
			String methodName = method.getName();
			
			polyglot.getBindings(language).putMember(methodName, method);

		}
		
		return this;
	}
	

	
}
