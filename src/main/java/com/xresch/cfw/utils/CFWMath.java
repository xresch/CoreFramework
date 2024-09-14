package com.xresch.cfw.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CFWMath {

	public static final BigDecimal BIGDEC_TWO = new BigDecimal(2);
	public static final BigDecimal BIGDEC_NEG_ONE = new BigDecimal(-1);
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public static BigDecimal bigMedian(List<BigDecimal> values) {
		
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
				return resultValue.add(values.get(percentilePosition)).divide(BIGDEC_TWO, RoundingMode.HALF_UP);
			}else {
				return resultValue;
			}
		}else {
			return values.get(0);
		}
		
	}
	
	/***********************************************************************************************
	 * 
	 * @return minimum value in the list, null if list is empty
	 ***********************************************************************************************/
	public static BigDecimal bigMin(List<BigDecimal> values) {
		
		if(values.isEmpty()) { return null; }
		
		BigDecimal min = null;
		
		for(BigDecimal current: values) {
			
			if(min == null) { min = current; continue; }
			
			if(current != null && current.compareTo(min) < 0) {
				min = current;
			}
		}
		
		return min;
		
	}
	
	/***********************************************************************************************
	 * 
	 * @return maximum value in the list, null if list is empty
	 ***********************************************************************************************/
	public static BigDecimal bigMax(List<BigDecimal> values) {
		
		if(values.isEmpty()) { return null; }
		
		BigDecimal max = null;
		
		for(BigDecimal current: values) {
			
			if(max == null) { max = current; continue; }
			
			if(current != null && current.compareTo(max) > 0) {
				max = current;
			}
		}
		
		return max;
		
	}
	
	/***********************************************************************************************
	 * 
	 * @return average value in the list, null if list is empty or all values are null
	 ***********************************************************************************************/
	public static BigDecimal bigAvg(List<BigDecimal> values) {
		
		if(values.isEmpty()) { return null; }
		
		BigDecimal sum = bigSum(values);
		
		if(sum == null) { return null; } 
		
		BigDecimal count = new BigDecimal(values.size());
		
		return sum.divide(count, RoundingMode.HALF_UP);
		
	}
	
	/***********************************************************************************************
	 * 
	 * @return sum value in the list, null if list is empty or all values are null
	 ***********************************************************************************************/
	public static BigDecimal bigSum(List<BigDecimal> values) {
		
		if(values.isEmpty()) { return null; }
		
		BigDecimal sum = null;

		for(BigDecimal current: values) {
			if(sum == null) { sum = current; continue; }
			
			if(current != null) {
				sum.add(current);
			}
		}
		
		return sum;
		
	}
	
	/***********************************************************************************************
	 * 
	 * @percentile a value between 0 and 100
	 ***********************************************************************************************/
	public static BigDecimal bigPercentile(int percentile, BigDecimal... values) {
		return bigPercentile(percentile, Arrays.asList(values));
	}
	
	/***********************************************************************************************
	 * 
	 * @percentile a value between 0 and 100
	 ***********************************************************************************************/
	public static BigDecimal bigPercentile(int percentile, List<BigDecimal> values) {
		
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
