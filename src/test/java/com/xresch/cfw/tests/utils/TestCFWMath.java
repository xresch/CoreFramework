package com.xresch.cfw.tests.utils;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;

public class TestCFWMath {
		
	/*****************************************************
	 * 
	 *****************************************************/
	@Test
	public void testBigMovAvg() {

		ArrayList<BigDecimal> values = new ArrayList<>();
		BigDecimal result;
		
		//----------------------------
		// Check list empty
		result = CFW.Math.bigMovAvg(values, 3);
		Assertions.assertEquals(null, result);
		
		//----------------------------
		// Check list too small
		values.add(new BigDecimal(10));
		values.add(new BigDecimal(20));
		
		result = CFW.Math.bigMovAvg(values, 3);
		Assertions.assertEquals(null, result);
		
		//----------------------------
		// Check list exactly enough
		values.add(new BigDecimal(30));
		result = CFW.Math.bigMovAvg(values, 3);
		Assertions.assertEquals(20, result.intValue());
		
		//----------------------------
		// Check moving average is moving
		values.add(new BigDecimal(40));
		result = CFW.Math.bigMovAvg(values, 3);
		Assertions.assertEquals(30, result.intValue());
	
		//----------------------------
		// Check moving average is decimal 
		// (30 + 40 + 123.456) / 3 = 64.485333
		values.add(new BigDecimal("123.456"));
		result = CFW.Math.bigMovAvg(values, 3);
		Assertions.assertEquals("64.485333", result.toPlainString());
	}
	
	
	
}
