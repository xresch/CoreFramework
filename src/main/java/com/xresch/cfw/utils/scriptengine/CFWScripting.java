package com.xresch.cfw.utils.scriptengine;

import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;

import com.oracle.truffle.js.scriptengine.GraalJSEngineFactory;
import com.xresch.cfw.logging.CFWLog;

public class CFWScripting {
	
	private static final Logger logger = CFWLog.getLogger(CFWScripting.class.getName());
	
	private static ScriptEngineManager manager = new ScriptEngineManager();
	
	/******************************************************************************************************
	 * Print all available scripting engines. 
	 ******************************************************************************************************/
	public static void printAvailableEngines() {

	    List<ScriptEngineFactory> factories = manager.getEngineFactories();
	   
	    for (ScriptEngineFactory factory : factories){
			System.out.println("ScriptEngineFactory Info");
			String engName = factory.getEngineName();
			String engVersion = factory.getEngineVersion();
			String langName = factory.getLanguageName();
			String langVersion = factory.getLanguageVersion();
			System.out.printf("\tScript Engine: %s (%s, %s, %s)\n", engName, engVersion, langName, langVersion);
			List<String> engNames = factory.getNames();
			for (String name : engNames)
			{
			   System.out.printf("\tEngine Alias: %s\n", name);
			}
			System.out.printf("\tLanguage: %s (%s)\n", langName, langVersion);
	    }
	}

	/******************************************************************************************************
	 *  Create a new Javascript engine.
	 * 
	 * @param the name of the engine to use, e.g. Nashorn. Use printAvailableEngines() for a list of 
	 * available engines.
	 ******************************************************************************************************/
	public static CFWScriptingContext createContext(String language) {
		
		//-----------------------------------
		// Create Engine

		Context context = Context.newBuilder(language)
		        .allowHostClassLookup(s -> true)
		        .allowHostAccess(HostAccess.ALL)
		        .build();

		return new CFWScriptingContext(language, context);
		
	}
	/******************************************************************************************************
	 *  Create a new Javascript engine.
	 * 
	 * @param the name of the engine to use, e.g. Nashorn. Use printAvailableEngines() for a list of 
	 * available engines.
	 ******************************************************************************************************/
	public static CFWScriptingContext createJavascriptContext() {
		
		return createContext("js");
		
	}
	
	/******************************************************************************************************
	 *  Create a new Javascript engine.
	 * 
	 * @param the name of the engine to use, e.g. Nashorn. Use printAvailableEngines() for a list of 
	 * available engines.
	 ******************************************************************************************************/
	public static CFWScriptingContext createPolyglotJavascript(Object objectToBind) {
		
		return createPolyglotWithAdditionalBindings("js", objectToBind);
		
	}
	/******************************************************************************************************
	 * Execute a javascript call with defined parameters.
	 * 
	 * @param the name of the engine to use, e.g. Nashorn. Use printAvailableEngines() for a list of 
	 * available engines.
	 ******************************************************************************************************/
	private static CFWScriptingContext createPolyglotWithAdditionalBindings(String language, Object objectToBind) {
		
		//-----------------------------------
		// Create Engine

		CFWScriptingContext polyglot = createContext(language);

		polyglot.getBindings().putMember(objectToBind.getClass().getSimpleName(), objectToBind);

		return polyglot;
		
		
	}
	
}
