package com.xresch.cfw.utils;

import java.math.BigDecimal;
import java.util.ArrayList;

public class CFWMath {

	public static final BigDecimal BIGDEC_TWO = new BigDecimal(2);
	public static final BigDecimal BIGDEC_NEG_ONE = new BigDecimal(-1);
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public static BigDecimal bigMedian(ArrayList<BigDecimal> values) {
		
		int count = values.size();
		
		if(count == 0) {
			return null;
		}
		
		int percentile = 50;
		int percentilePosition = (int)Math.ceil( count * (percentile / 100f) );
		
		//---------------------------
		// Retrieve number
		boolean isEvenCount = (count % 2 == 0);
		values.sort(null);
		
		if(percentilePosition > 0) {
			BigDecimal resultValue = values.get(percentilePosition-1);
			if(isEvenCount) {
				return resultValue.add(values.get(percentilePosition)).divide(BIGDEC_TWO);
			}else {
				return resultValue;
			}
		}else {
			return values.get(0);
		}
		
	}
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public static BigDecimal bigPercentile(ArrayList<BigDecimal> values, int percentile) {
		
		int count = values.size();
		
		if(count == 0) {
			return null;
		}
				
		int percentilePosition = (int)Math.ceil( count * (percentile / 100f) );
		
		//---------------------------
		// Retrieve number
		values.sort(null);
		
		if(percentilePosition > 0) {
			// one-based position, minus 1 to get index
			return values.get(percentilePosition-1);
		}else {
			return values.get(0);
		}
		
	}
	
}
