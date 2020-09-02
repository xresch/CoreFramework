package com.xresch.cfw.tests.performance;

public class TestPerformanceStackTrace {

	// Run without JIT: -Djava.compiler=NONE
	public static void main(String[] args) {

		long start = System.nanoTime();
        
		//-----------------------------------
		// 
		start = System.nanoTime();
		String clazz = "";
		String method = "";
		for(int i = 0; i < 100000; i++) {
			StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
			StackTraceElement last = stacktrace[stacktrace.length-1];
			clazz = last.getClassName();
			method = last.getMethodName();
		}
		System.out.println("class: "+clazz);
		System.out.println("method: "+method);
		System.out.println("stacktrace time: "+(System.nanoTime() - start)/1000000);

	}

}
