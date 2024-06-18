package com.xresch.cfw.features.query.parse;

import com.xresch.cfw.features.query.EnhancedJsonObject;

public interface LeftRightEvaluatable {

	/******************************************************************************************************
	 * Evaluates the binary expression and returns the resulting value.
	 * For the evaluation of the binary expressions, The left value will be taken from the left object, 
	 * and the right value will be taken from the right object.
	 ******************************************************************************************************/
	public abstract QueryPartValue evaluateLeftRightValues(
							  EnhancedJsonObject leftObject
							, EnhancedJsonObject rightObject) 
							throws Exception
							;
	
	
}
