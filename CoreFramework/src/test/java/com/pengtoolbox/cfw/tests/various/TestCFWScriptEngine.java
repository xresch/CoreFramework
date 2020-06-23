package com.pengtoolbox.cfw.tests.various;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.utils.CFWHttpPacScriptMethods;
import com.pengtoolbox.cfw.utils.CFWScriptEngine;

public class TestCFWScriptEngine {
	
	@Test
	public void printAvailableEngines() {
		
		CFW.Scripting.printAvailableEngines();
	}

	@Test
	public void testRunJavascript() {
		
		CFWScriptEngine engine = CFW.Scripting.createJavascriptEngine();
		engine.addScript("function myFunc(astring, anumber){ return astring+' '+anumber}");
		//--------------------------------
		// With parameter List
		Object result = engine.executeJavascript("myFunc", "Test", 123);
		
		System.out.println(result);
		Assertions.assertEquals("Test 123", result, "The method returned the expected value.");
		
		//--------------------------------
		// With parameter List
		Object result2 = engine.executeJavascript("myFunc('Hello', 456)");
		
		System.out.println(result2);
		Assertions.assertEquals("Hello 456", result2, "The method returned the expected value.");
	}
	
	@Test
	public void testRunJavascriptWithAdditionalMethods() {
		
		CFWScriptEngine engine = CFW.Scripting.createJavascriptEngine(CFWHttpPacScriptMethods.class);
		
		//--------------------------------
		// Call CFWHttpPacScriptMethods.myIpAddress();
		Object result = engine.executeJavascript("CFWHttpPacScriptMethods.myIpAddress();");
		System.out.println("CFWHttpPacScriptMethods.myIpAddress(): "+result);
		
		//--------------------------------
		// Call myIpAddress();
		result = engine.executeJavascript("myIpAddress();");
		System.out.println("myIpAddress(): "+result);
		
		//--------------------------------
		// With parameter List
		engine.addScript("function myFunc(astring, anumber){ return astring+' '+anumber}");
		result = engine.executeJavascript("myFunc('Hello', 456)");
		
		System.out.println(result);
		Assertions.assertEquals("Hello 456", result, "The method returned the expected value.");
	}
	
}
