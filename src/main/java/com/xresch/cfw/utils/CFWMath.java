package com.xresch.cfw.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CFWMath {

	private static final int GLOBAL_SCALE = 6;
	public static final BigDecimal BIGDEC_TWO = new BigDecimal(2);
	public static final BigDecimal BIGDEC_NEG_ONE = new BigDecimal(-1);
	
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
	 * Returns a moving average value for the last N values in the given list.
	 * Will return null if there are not enough datapoints.
	 * 
	 * @param values the list of values
	 * @param datapoints number of points that should be used for calculating the moving average
	 * @return moving average value , null if list size is smaller than datapoints
	 ***********************************************************************************************/
	public static BigDecimal bigMovAvg(List<BigDecimal> values, int datapoints) {
		
		while( values.remove(null) ); // remove all null values
		if(values.size() < datapoints ) { return null; }
		
		List<BigDecimal> partialValues = values.subList(values.size()-datapoints, values.size());
		BigDecimal sum = bigSum(partialValues);
		sum = sum.setScale(GLOBAL_SCALE, RoundingMode.HALF_UP); // won't calculate decimals if not set
		if(sum == null) { return null; } 
		
		BigDecimal count = new BigDecimal(partialValues.size());
		
		return sum.divide(count, RoundingMode.HALF_UP);
		
	}
	
	/***********************************************************************************************
	 * Returns the Moving Average
	 * @return average value in the list, null if list is empty or all values are null
	 ***********************************************************************************************/
	public static BigDecimal bigAvg(List<BigDecimal> values) {
		
		while( values.remove(null) ); // remove all null values
		if(values.isEmpty()) { return null; }
		
		BigDecimal sum = bigSum(values);
		sum = sum.setScale(GLOBAL_SCALE, RoundingMode.HALF_UP); // won't calculate decimals if not set
		if(sum == null) { return null; } 
		
		BigDecimal count = new BigDecimal(values.size());
		
		return sum.divide(count, RoundingMode.HALF_UP);
		
	}
	
	/***********************************************************************************************
	 * 
	 * @return sum value in the list, null if list is empty or all values are null
	 ***********************************************************************************************/
	public static BigDecimal bigSum(List<BigDecimal> values) {
		
		while( values.remove(null) ); // remove all null values
		if(values.isEmpty()) { return null; }
		
		BigDecimal sum = BigDecimal.ZERO.setScale(GLOBAL_SCALE);

		for(BigDecimal current : values) {
			if(sum == null) { sum = current; continue; }
			
			sum = sum.add(current);

		}
		
		return sum;
		
	}
	
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
		
		while( values.remove(null) ); // remove all null values
		
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
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public static BigDecimal bigStdev(List<BigDecimal> values, boolean usePopulation) {
		
		//while( values.remove(null) );
		
		// zero or one number will have standard deviation 0
		if(values.size() <= 1) {
			return BigDecimal.ZERO;
		}
	
//		How to calculate standard deviation:
//		Step 1: Find the mean/average.
//		Step 2: For each data point, find the square of its distance to the mean.
//		Step 3: Sum the values from Step 2.
//		Step 4: Divide by the number of data points.
//		Step 5: Take the square root.
		
		//-----------------------------------------
		// STEP 1: Find Average
		BigDecimal count = new BigDecimal(values.size());
		
		BigDecimal average = bigAvg(values);

		BigDecimal sumDistanceSquared = BigDecimal.ZERO;
		
		for(BigDecimal value : values) {
			//-----------------------------------------
			// STEP 2: For each data point, find the 
			// square of its distance to the mean.
			BigDecimal distance = value.subtract(average);
			//-----------------------------------------
			// STEP 3: Sum the values from Step 2.
			sumDistanceSquared = sumDistanceSquared.add(distance.pow(2));
		}
		
		//-----------------------------------------
		// STEP 4 & 5: Divide and take square root
		
		BigDecimal divisor = (usePopulation) ? count : count.subtract(BigDecimal.ONE);
		
		BigDecimal divided = sumDistanceSquared.divide(divisor, RoundingMode.HALF_UP);
		
		// TODO JDK8 Migration: should work with JDK 9
		MathContext mc = new MathContext(GLOBAL_SCALE, RoundingMode.HALF_UP);
		BigDecimal standardDeviation = divided.sqrt(mc);
		
		return standardDeviation;
	}
	
//	/***********************************************************************************************
//	 * 
//	 ***********************************************************************************************/
//	private BigDecimal calculateStandardDeviation(boolean usePopulation) {
//		
//		// zero or one number will have standard deviation 0
//		if(values.size() <= 1) {
//			return BigDecimal.ZERO;
//		}
//	
////		How to calculate standard deviation:
////		Step 1: Find the mean/average.
////		Step 2: For each data point, find the square of its distance to the mean.
////		Step 3: Sum the values from Step 2.
////		Step 4: Divide by the number of data points.
////		Step 5: Take the square root.
//		
//		//-----------------------------------------
//		// STEP 1: Find Average
//		BigDecimal count = new BigDecimal(values.size());
//		count.setScale(6);
//		
//		BigDecimal average = sum.divide(count, RoundingMode.HALF_UP);
//		
//		BigDecimal sumDistanceSquared = BigDecimal.ZERO;
//		for(BigDecimal value : values) {
//			//-----------------------------------------
//			// STEP 2: For each data point, find the 
//			// square of its distance to the mean.
//			BigDecimal distance = value.subtract(average);
//			//-----------------------------------------
//			// STEP 3: Sum the values from Step 2.
//			sumDistanceSquared = sumDistanceSquared.add(distance.pow(2));
//		}
//		
//		//-----------------------------------------
//		// STEP 4 & 5: Divide and take square root
//		
//		BigDecimal divisor = (usePopulation) ? count : count.subtract(BigDecimal.ONE);
//		
//		BigDecimal divided = sumDistanceSquared.divide(divisor, RoundingMode.HALF_UP);
//		
//		// TODO JDK8 Migration: should work with JDK 9
//		MathContext mc = new MathContext(6, RoundingMode.HALF_UP);
//		BigDecimal standardDeviation = divided.sqrt(mc);
//		
//		//reset values when calculation is done
//		values.clear();
//		sum = BigDecimal.ZERO.setScale(6);
//		return standardDeviation;
//	}
	
}
