package com.xresch.cfw.tests.utils;

import java.io.IOException;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.cli.ArgumentsException;
import com.xresch.cfw.utils.scriptengine.CFWScriptingContext;
import com.xresch.cfw.utils.web.CFWHttpPacScriptMethods;

public class TestCFWScriptEngine {
	
	@Test
	public void printAvailableEngines() {
		
		CFW.Scripting.printAvailableEngines();
	}
	
	
	@Test
	public void testGraalVM() {
		
		Context polyglot = Context.newBuilder("js")
		        .allowHostClassLookup(s -> true)
		        .allowHostAccess(HostAccess.ALL)
		        .build();
		
		//-------------------------------------
		// Basic Javascript Function call
		polyglot.eval("js", "function myFunc(astring, anumber){ return astring+' '+anumber}");
		
		Value result = polyglot.eval("js", "myFunc('Test', 123)");
		System.out.println("Result:"+result.asString());
		
		//-------------------------------------
		// Java Function call from JS
		polyglot.getBindings("js").putMember("CFWHttpPacScriptMethods", new CFWHttpPacScriptMethods());

		result = polyglot.eval("js", "CFWHttpPacScriptMethods.myIpAddress();");
		System.out.println("CFWHttpPacScriptMethods.myIpAddress(): "+result.asString());
		
	}

	
	
	@Test
	public void testRunJavascript() {
		
		CFWScriptingContext engine = CFW.Scripting.createJavascriptContext();
		engine.addScript("test.js", "function myFunc(astring, anumber){ return astring+' '+anumber}");
		//--------------------------------
		// With parameter List
		Value result = engine.executeFunction("myFunc", "Test", 123);
		
		System.out.println(result);
		Assertions.assertEquals("Test 123", result.asString(), "The method returned the expected value.");
		
		//--------------------------------
		// With parameter List
		result = engine.executeScript("myFunc('Hello', 456)");
		
		System.out.println(result);
		Assertions.assertEquals("Hello 456", result.asString(), "The method returned the expected value.");
	}
	
	@Test
	public void testRunJavascriptWithAdditionalMethods() {
		
		CFWScriptingContext engine = CFW.Scripting.createPolyglotJavascript(new CFWHttpPacScriptMethods());
		
		//--------------------------------
		// Call CFWHttpPacScriptMethods.myIpAddress();
		Value result = engine.executeScript("CFWHttpPacScriptMethods.myIpAddress();");
		System.out.println("CFWHttpPacScriptMethods.myIpAddress(): "+result);
		
		//--------------------------------
		// Call myIpAddress();
		result = engine.executeScript("CFWHttpPacScriptMethods.myIpAddress();");
		System.out.println("myIpAddress(): "+result);
		
		//--------------------------------
		// With parameter List
		engine.addScript("test.js", "function myFunc(astring, anumber){ return astring+' '+anumber}");
		result = engine.executeScript("myFunc('Hello', 456)");
		
		System.out.println(result);
		Assertions.assertEquals("Hello 456", result.toString(), "The method returned the expected value.");
	}
	
	
	@Test
	public void testProxyPAC() {
		
		try {
			CFW.initializeCore(new String[] {});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		CFWScriptingContext polyglot = CFW.Scripting.createPolyglotJavascript(new CFWHttpPacScriptMethods());
		
		//--------------------------------
		// Load PAC Script
		String proxyPAC = CFW.Files.getFileContent(CFW.Context.Request.getRequest(), CFW.Properties.PROXY_PAC);
		
		proxyPAC = CFWHttpPacScriptMethods.preparePacScript(proxyPAC);
		polyglot.addScript("proxypac.js", proxyPAC);
		
		
		//--------------------------------
		// With parameter List
		Value result =  polyglot.executeScript("FindProxyForURL('localhost:9090/test', 'localhost');");
		
		System.out.println(result.asString());
		Assertions.assertEquals("PROXY localhost:9999", result.asString(), "The method returned the expected value.");
		
	}
	
	
	
	
	
}
