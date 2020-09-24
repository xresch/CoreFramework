package com.xresch.cfw.tests.performance;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import com.xresch.cfw.logging.CFWLog;

/*****************************************************************
* 
*
* � 2018 Reto Scheiwiller, all rights reserved
******************************************************************/
public class VeryDeepAndJustCrazyHumongousStacktraceSimulator {

	private static Logger logger = CFWLog.getLogger(VeryDeepAndJustCrazyHumongousStacktraceSimulator.class.getName());
	
	public static Object simulateDeepStacktrace(int targetDepth, int currentDepth, Class clazz, String methodName) {
		
		if(currentDepth < targetDepth){
			return simulateDeepStacktrace(targetDepth, ++currentDepth, clazz, methodName);
		}else{
		
			Object result = null;
			try {
				Method method = clazz.getMethod(methodName);
				result = method.invoke(null);
			} catch (NoSuchMethodException
					| SecurityException 
					| IllegalAccessException 
					| IllegalArgumentException 
					| InvocationTargetException e) {

				new CFWLog(logger).severe("Error occured.", e);
			}
			
			return result;
		}
	}
}
