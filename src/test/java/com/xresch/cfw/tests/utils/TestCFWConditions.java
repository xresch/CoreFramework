package com.xresch.cfw.tests.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.utils.CFWHttpPacScriptMethods;
import com.xresch.cfw.utils.CFWPolyglotContext;
import com.xresch.cfw.utils.CFWConditions.ThresholdCondition;

public class TestCFWConditions {
	

	@Test
	public void testThresholdConditions() {
		ThresholdCondition condition;
		
		//--------------------------------
		// EXCELLENT
		condition = CFW.Conditions.getConditionForValue(1, 1, 2, 3, 4, 5, false);
		Assertions.assertEquals(ThresholdCondition.EXCELLENT, condition, "The method returns the expected condition.");
		
		//--------------------------------
		// GOOD
		condition = CFW.Conditions.getConditionForValue(2, 1, 2, 3, 4, 5, false);
		Assertions.assertEquals(ThresholdCondition.GOOD, condition, "The method returns the expected condition.");
		
		//--------------------------------
		// WARNING
		condition = CFW.Conditions.getConditionForValue(3, 1, 2, 3, 4, 5, false);
		Assertions.assertEquals(ThresholdCondition.WARNING, condition, "The method returns the expected condition.");
				
		//--------------------------------
		// EMERGENCY
		condition = CFW.Conditions.getConditionForValue(4, 1, 2, 3, 4, 5, false);
		Assertions.assertEquals(ThresholdCondition.EMERGENCY, condition, "The method returns the expected condition.");
				
		//--------------------------------
		// DANGER
		condition = CFW.Conditions.getConditionForValue(5, 1, 2, 3, 4, 5, false);
		Assertions.assertEquals(ThresholdCondition.DANGER, condition, "The method returns the expected condition.");
						
	}
	
	@Test
	public void testThresholdConditionsReverse() {
		ThresholdCondition condition;
		
		//--------------------------------
		// EXCELLENT
		condition = CFW.Conditions.getConditionForValue(5, 5, 4, 3, 2, 1, false);
		Assertions.assertEquals(ThresholdCondition.EXCELLENT, condition, "The method returns the expected condition.");
		
		//--------------------------------
		// GOOD
		condition = CFW.Conditions.getConditionForValue(4, 5, 4, 3, 2, 1, false);
		Assertions.assertEquals(ThresholdCondition.GOOD, condition, "The method returns the expected condition.");
		
		//--------------------------------
		// WARNING
		condition = CFW.Conditions.getConditionForValue(3, 5, 4, 3, 2, 1, false);
		Assertions.assertEquals(ThresholdCondition.WARNING, condition, "The method returns the expected condition.");
				
		//--------------------------------
		// EMERGENCY
		condition = CFW.Conditions.getConditionForValue(2, 5, 4, 3, 2, 1, false);
		Assertions.assertEquals(ThresholdCondition.EMERGENCY, condition, "The method returns the expected condition.");
				
		//--------------------------------
		// DANGER
		condition = CFW.Conditions.getConditionForValue(1, 5, 4, 3, 2, 1, false);
		Assertions.assertEquals(ThresholdCondition.DANGER, condition, "The method returns the expected condition.");
						
	}
	
	@Test
	public void testThresholdSkipped() {
		ThresholdCondition condition;
		
		//--------------------------------
		// NOT EVALUATED
		condition = CFW.Conditions.getConditionForValue(1, null, 2, null, 4, null, false);
		Assertions.assertEquals(ThresholdCondition.NOT_EVALUATED, condition, "The method returns the expected condition.");
				
		//--------------------------------
		// GOOD
		condition = CFW.Conditions.getConditionForValue(2, null, 2, null, 4, null, false);
		Assertions.assertEquals(ThresholdCondition.GOOD, condition, "The method returns the expected condition.");
		
		//--------------------------------
		// GOOD skipped WARNING
		condition = CFW.Conditions.getConditionForValue(3, null, 2, null, 4, null, false);
		Assertions.assertEquals(ThresholdCondition.GOOD, condition, "The method returns the expected condition.");
		
		//--------------------------------
		// EMERGENCY
		condition = CFW.Conditions.getConditionForValue(4, null, 2, null, 4, null, false);
		Assertions.assertEquals(ThresholdCondition.EMERGENCY, condition, "The method returns the expected condition.");
		
		//--------------------------------
		// EMERGENCY as DANGER is undefined
		condition = CFW.Conditions.getConditionForValue(5, null, 2, null, 4, null, false);
		Assertions.assertEquals(ThresholdCondition.EMERGENCY, condition, "The method returns the expected condition.");
			

		//--------------------------------
		// REVERSE NOT EVALUATED
		condition = CFW.Conditions.getConditionForValue(1, null, 4, null, 2, null, false);
		Assertions.assertEquals(ThresholdCondition.NOT_EVALUATED, condition, "The method returns the expected condition.");
				
		//--------------------------------
		// REVERSE GOOD as EXCELLENT is undefined
		condition = CFW.Conditions.getConditionForValue(5, null, 4, null, 2, null, false);
		Assertions.assertEquals(ThresholdCondition.GOOD, condition, "The method returns the expected condition.");
		
		//--------------------------------
		// REVERSE GOOD
		condition = CFW.Conditions.getConditionForValue(4, null, 4, null, 2, null, false);
		Assertions.assertEquals(ThresholdCondition.GOOD, condition, "The method returns the expected condition.");
		
		//--------------------------------
		// REVERSE EMERGENCY as WARNING is undefined
		condition = CFW.Conditions.getConditionForValue(3, null, 4, null, 2, null, false);
		Assertions.assertEquals(ThresholdCondition.EMERGENCY, condition, "The method returns the expected condition.");
		
		//--------------------------------
		// REVERSE EMERGENCY 
		condition = CFW.Conditions.getConditionForValue(2, null, 4, null, 2, null, false);
		Assertions.assertEquals(ThresholdCondition.EMERGENCY, condition, "The method returns the expected condition.");

	}
	
	@Test
	public void testSpecialConditions() {
		
		ThresholdCondition condition;
		//--------------------------------
		// NOT_EVALUATED, value below all thresholds
		condition = CFW.Conditions.getConditionForValue(0, 1, 2, 3, 4, 5, false);
		Assertions.assertEquals(ThresholdCondition.NOT_EVALUATED, condition, "The method returns the expected condition.");

		//--------------------------------
		// NOT_EVALUATED, value below all thresholds, reverse
		condition = CFW.Conditions.getConditionForValue(0, 5, 4, 3, 2, 1, false);
		Assertions.assertEquals(ThresholdCondition.NOT_EVALUATED, condition, "The method returns the expected condition.");
		
		//--------------------------------
		// DISABLED
		condition = CFW.Conditions.getConditionForValue(3, 1, 2, 3, 4, 5, true);
		Assertions.assertEquals(ThresholdCondition.DISABLED, condition, "The method returns the expected condition.");
		
		//--------------------------------
		// NONE value undefined
		condition = CFW.Conditions.getConditionForValue(null, 1, 2, 3, 4, 5, false);
		Assertions.assertEquals(ThresholdCondition.NONE, condition, "The method returns the expected condition.");
		
		//--------------------------------
		// NONE threshold undefined
		condition = CFW.Conditions.getConditionForValue(1, null, null, null, null, null, false);
		Assertions.assertEquals(ThresholdCondition.NONE, condition, "The method returns the expected condition.");
	}
	
	@Test
	public void testSeverity() {
		
		int severity;
		
		//--------------------------------
		// EXCELLENT
		severity = CFW.Conditions.getConditionSeverity(ThresholdCondition.EXCELLENT);
		Assertions.assertEquals(2, severity, "The method returns the expected severity.");

		//--------------------------------
		// GOOD
		severity = CFW.Conditions.getConditionSeverity(ThresholdCondition.GOOD);
		Assertions.assertEquals(4, severity, "The method returns the expected severity.");

		//--------------------------------
		// WARNING
		severity = CFW.Conditions.getConditionSeverity(ThresholdCondition.WARNING);
		Assertions.assertEquals(8, severity, "The method returns the expected severity.");

		//--------------------------------
		// EMERGENCY
		severity = CFW.Conditions.getConditionSeverity(ThresholdCondition.EMERGENCY);
		Assertions.assertEquals(16, severity, "The method returns the expected severity.");

		//--------------------------------
		// DANGER
		severity = CFW.Conditions.getConditionSeverity(ThresholdCondition.DANGER);
		Assertions.assertEquals(32, severity, "The method returns the expected severity.");

	}
	
	@Test
	public void testComparison() {
		
		boolean result;
		
		//--------------------------------
		// WARNING vs EXCELLENT
		result = CFW.Conditions.compareIsEqualsOrMoreDangerous(ThresholdCondition.WARNING, ThresholdCondition.EXCELLENT);
		Assertions.assertEquals(false, result, "The method returns the expected result.");

		//--------------------------------
		// WARNING vs GOOD
		result = CFW.Conditions.compareIsEqualsOrMoreDangerous(ThresholdCondition.WARNING, ThresholdCondition.GOOD);
		Assertions.assertEquals(false, result, "The method returns the expected result.");

		//--------------------------------
		// WARNING vs WARNING
		result = CFW.Conditions.compareIsEqualsOrMoreDangerous(ThresholdCondition.WARNING, ThresholdCondition.WARNING);
		Assertions.assertEquals(true, result, "The method returns the expected result.");
		
		//--------------------------------
		// WARNING vs EMERGENCY
		result = CFW.Conditions.compareIsEqualsOrMoreDangerous(ThresholdCondition.WARNING, ThresholdCondition.EMERGENCY);
		Assertions.assertEquals(true, result, "The method returns the expected result.");
		
		//--------------------------------
		// WARNING vs DANGER
		result = CFW.Conditions.compareIsEqualsOrMoreDangerous(ThresholdCondition.WARNING, ThresholdCondition.DANGER);
		Assertions.assertEquals(true, result, "The method returns the expected result.");
	}
	
	
}
