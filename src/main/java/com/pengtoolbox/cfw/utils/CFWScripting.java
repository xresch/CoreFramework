package com.pengtoolbox.cfw.utils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Logger;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.pengtoolbox.cfw.logging.CFWLog;

public class CFWScripting {
	
	public static Logger logger = CFWLog.getLogger(CFWScripting.class.getName());
	
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
			System.out.printf("\tScript Engine: %s (%s)\n", engName, engVersion);
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
	public static CFWScriptEngine createJavascriptEngine() {
		
		//-----------------------------------
		// Create Engine
		ScriptEngine engine = manager.getEngineByName("Nashorn");
		
		return new CFWScriptEngine(engine);
		
	}
	
	/******************************************************************************************************
	 *  Create a new Javascript engine.
	 * 
	 * @param the name of the engine to use, e.g. Nashorn. Use printAvailableEngines() for a list of 
	 * available engines.
	 ******************************************************************************************************/
	public static CFWScriptEngine createJavascriptEngine(Class clazzToBind) {
		
		return createEngineWithAdditionalMethods("Nashorn", clazzToBind);
		
	}
	/******************************************************************************************************
	 * Execute a javascript call with defined parameters.
	 * 
	 * @param the name of the engine to use, e.g. Nashorn. Use printAvailableEngines() for a list of 
	 * available engines.
	 ******************************************************************************************************/
	public static CFWScriptEngine createEngineWithAdditionalMethods(String engineName, Class clazz) {
		
		//-----------------------------------
		// Create Engine
		ScriptEngine engine = manager.getEngineByName(engineName);

		//-----------------------------------
		// Add Class
		String clazzName = clazz.getSimpleName();
		try {
			engine.put(clazzName, clazz.newInstance());
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//-----------------------------------
		// Add Methods
		StringBuilder functionBuilder = new StringBuilder();
		functionBuilder.append("var ").append(clazzName).append(" = Java.type('"+clazz.getName()+"');\n");
		
		for(Method method : clazz.getMethods()){
			
			String methodName = method.getName();
			
			int paramCount = method.getParameterTypes().length;
						
			StringBuilder arguments = new StringBuilder("(");
			for(int i = 0; i < paramCount; i++) {
				arguments.append("arg").append(i);
				if(i < paramCount-1) {
					arguments.append(",");
				}
			}
			
			arguments.append(")");
			
			functionBuilder.append(methodName).append(" = function ").append(arguments)
					.append("{ return ").append(clazzName).append(".").append(methodName).append(arguments)
					.append(";}\n");
			
		  
		}
		
		//System.out.println("====== functionBuilder.toString ======()\n"+functionBuilder.toString());
		
		try {
			engine.eval(functionBuilder.toString());
		} catch (ScriptException e) {
			new CFWLog(logger)
			.method("createEngineWithAdditionalMethods")
			.severe("The engine could not be initialized correctly.", e);
		}
		
		return new CFWScriptEngine(engine);
	}
	
}
