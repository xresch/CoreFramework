package com.xresch.cfw.tests.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.xresch.cfw.utils.CFWMonitor;

public class TestCFWMonitor {
		
	/*****************************************************
	 * 
	 *****************************************************/
	@Test
	public void testCFWMonitor() {

		//------------------------------------------------
		//  Monitor True
		CFWMonitor monitorTrue = new CFWMonitor() {
			@Override
			protected boolean monitorCondition() { return true; }
		};
		
		//------------------------------------------------
		//  Monitor False
		CFWMonitor monitorFalse = new CFWMonitor() {
			@Override
			protected boolean monitorCondition() { return false; }
		};
		
		//------------------------------------------------
		//  Monitor False Again
		CFWMonitor monitorFalseAgain = new CFWMonitor() {
			@Override
			protected boolean monitorCondition() { return false; }
		};
		
		
		Assertions.assertEquals(true, monitorTrue.check());
		Assertions.assertEquals(false, monitorFalse.check());
		
		//------------------------------------------------
		// Monitor chain as AND
		monitorTrue.chain(monitorFalse);
		Assertions.assertEquals(false, monitorTrue.check());
		
		//------------------------------------------------
		// Check doesn't cause Stack overflow
		monitorTrue.chain(monitorFalse);

		
		//------------------------------------------------
		// Monitor chain as OR
		monitorTrue.chain(monitorFalse)
				   .chainUseORCondition(true);
		
		Assertions.assertEquals(true, monitorTrue.check());
		
		//------------------------------------------------
		// Monitor chain another
		monitorTrue.chain(monitorFalseAgain);
		
		Assertions.assertEquals(true, monitorTrue.check());
		
		//------------------------------------------------
		// Monitor chain another
		monitorTrue.chainUseORCondition(false);
		
		Assertions.assertEquals(false, monitorTrue.check());
		
	}
	
	
	
}
