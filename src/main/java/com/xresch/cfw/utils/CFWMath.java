package com.xresch.cfw.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

public class CFWMath {

	public static final int GLOBAL_SCALE = 6;
	
	public static final BigDecimal ZERO = BigDecimal.ZERO;
	public static final BigDecimal ONE = BigDecimal.ONE;
	public static final BigDecimal TWO = new BigDecimal(2);
	public static final BigDecimal BIG_NEG_ONE = new BigDecimal(-1);
	public static final BigDecimal BIG_100 = new BigDecimal(100);
	
	public static final RoundingMode ROUND_UP = RoundingMode.HALF_UP;
	
	
	/***********************************************************************************************
	 * Replaces all null values with zeros and returns a clone of the list.
	 ***********************************************************************************************/
	public static ArrayList<BigDecimal> nullToZero(List<BigDecimal> values) {
		
		ArrayList<BigDecimal> clone = new ArrayList<>();
		clone.addAll(values);
		
		clone.replaceAll(new UnaryOperator<BigDecimal>() {
			
			@Override
			public BigDecimal apply(BigDecimal t) {
				if(t != null) { return t; }
				return ZERO;
			}
		});
		
		return clone;
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
	 * Returns a moving difference between the last value and the value at the beginning of the period in percentage (n values back).
	 * Will return null if there are not enough datapoints.
	 * 
	 * @param values the list of values
	 * @param period number of points the value is back in the sequence of data
	 * @param precision the precision of digits for the resulting values.
	 * @return moving average value , null if list size is smaller than datapoints
	 ***********************************************************************************************/
	public static BigDecimal bigMovDiff(List<BigDecimal> values, int period, int precision) {
		
		while( values.remove(null) ); // remove all null values
		if(values.size() < period ) { return null; }
		
		BigDecimal youngerValue = values.get(values.size()-1);
		BigDecimal olderValue = values.get(values.size() - period);
		
		//---------------------------------
		// handle Zeros
		if(youngerValue.compareTo(ZERO) == 0) {
			if(olderValue.compareTo(ZERO) == 0) {
				return ZERO;
			}
			
			return null;
		}else if(olderValue.compareTo(ZERO) == 0) {
			return null;
		}
		
		//---------------------------------
		// Calculate Diff
		// ( (youngerValue - olderValue) / olderValue) * 100

		MathContext mc = new MathContext(precision, ROUND_UP);
		BigDecimal diffPercent = youngerValue
									.subtract(olderValue)
									.divide(olderValue, mc)
									.multiply(BIG_100, mc)
									;
		System.out.println("================================");
		System.out.println("lastValue: "+youngerValue.toPlainString());
		System.out.println("diffValue: "+olderValue.toPlainString());
		System.out.println("diff: "+youngerValue.subtract(olderValue).toPlainString());
		System.out.println("divide: "+youngerValue.subtract(olderValue).divide(youngerValue, mc));
		System.out.println("diffPercent: "+diffPercent.toPlainString());
		if(diffPercent == null) { return null; } 
		diffPercent = diffPercent.setScale(precision, ROUND_UP); // won't calculate decimals if not set
		
		return diffPercent;
		
	}
	
	/***********************************************************************************************
	 * Returns a moving average value for the last N values in the given list.
	 * Will return null if there are not enough datapoints.
	 * 
	 * @param values the list of values
	 * @param period number of points that should be used for calculating the moving average
	 * @param precision the precision of digits for the resulting values.
	 * @return moving average value , null if list size is smaller than datapoints
	 ***********************************************************************************************/
	public static BigDecimal bigMovAvg(List<BigDecimal> values, int period, int precision) {
		
		while( values.remove(null) ); // remove all null values
		if(values.size() < period ) { return null; }
		
		List<BigDecimal> partialValues = values.subList(values.size() - period, values.size());
		BigDecimal sum = bigSum(partialValues);
		sum = sum.setScale(precision, ROUND_UP); // won't calculate decimals if not set
		if(sum == null) { return null; } 
		
		BigDecimal count = new BigDecimal(partialValues.size());
		
		return sum.divide(count, ROUND_UP);
		
	}
	
	/***********************************************************************************************
	 * Returns the Moving Average
	 * @return average value in the list, null if list is empty or all values are null
	 ***********************************************************************************************/
	public static BigDecimal bigAvg(List<BigDecimal> values) {
		
		while( values.remove(null) ); // remove all null values
		if(values.isEmpty()) { return null; }
		
		BigDecimal sum = bigSum(values);
		sum = sum.setScale(GLOBAL_SCALE, ROUND_UP); // won't calculate decimals if not set
		if(sum == null) { return null; } 
		
		BigDecimal count = new BigDecimal(values.size());
		
		return sum.divide(count, ROUND_UP);
		
	}
	
	/***********************************************************************************************
	 * 
	 * @return sum value in the list, null if list is empty or all values are null
	 ***********************************************************************************************/
	public static BigDecimal bigSum(List<BigDecimal> values) {
		
		while( values.remove(null) ); // remove all null values
		if(values.isEmpty()) { return null; }
		
		BigDecimal sum = ZERO.setScale(GLOBAL_SCALE);

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
				return resultValue.add(values.get(percentilePosition)).divide(TWO, ROUND_UP);
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
	 * Returns a moving standard deviation value for the last N values in the given list.
	 * Will return null if there are not enough datapoints.
	 * 
	 * @param values the list of values
	 * @param period number of points that should be used for calculating the moving average
	 * @param precision TODO
	 * @return moving average value , null if list size is smaller than datapoints
	 ***********************************************************************************************/
	public static BigDecimal bigMovStdev(List<BigDecimal> values, int period, boolean usePopulation, int precision) {
		
		while( values.remove(null) ); // remove all null values
		if(values.size() < period ) { return null; }
		
		List<BigDecimal> partialValues = values.subList(values.size() - period, values.size());
		
		return bigStdev(partialValues, usePopulation, precision);
		
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public static BigDecimal bigStdev(List<BigDecimal> values, boolean usePopulation, int precision) {
		
		//while( values.remove(null) );
		
		// zero or one number will have standard deviation 0
		if(values.size() <= 1) {
			return ZERO;
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

		BigDecimal sumDistanceSquared = ZERO;
		
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
		
		BigDecimal divided = sumDistanceSquared.divide(divisor, ROUND_UP);
		
		// TODO JDK8 Migration: should work with JDK 9
		MathContext mc = new MathContext(precision, ROUND_UP);
		BigDecimal standardDeviation = divided.sqrt(mc);
		
		return standardDeviation;
	}
	
	
	/***********************************************************************************************
	 * Returns the Relative Strength Index(RSI) for the last N values in the given list.
	 * Will return null if there are not enough values.
	 * 
	 * It is recommended to use CFW.Math.nullToZero() before using this method if the method can contain
	 * null values. Null values will be removed from the list before calculations take place.
	 * 
	 * This method uses the calculation formulas as described here:
	 * https://www.zaner.com/3.0/education/technicalstudies/RSI.asp#top
	 * 
	 * @param values the list of values
	 * @param period number of points that should be used for calculating the moving average
	 * @param precision the precision of digits for the resulting values.
	 * 
	 * @return moving average value , null if list size is smaller than datapoints
	 ***********************************************************************************************/
	public static BigDecimal bigRSI(List<BigDecimal> values, int period, int precision) {

		while( values.remove(null) ); // remove all null values
		if(values.size() < period ) { return null; }
		
		//------------------------------------
		// STEP 1: Sums of Ups & Downs
		int i = values.size() - period;
		BigDecimal previousValue = values.get(i);
		BigDecimal sumGains = ZERO.setScale(precision);
		BigDecimal sumLosses = ZERO.setScale(precision);
		for(i++ ; i < values.size(); i++) {
			BigDecimal currentValue = values.get(i);
			BigDecimal diff = currentValue.subtract(previousValue);

			if(diff.compareTo(ZERO) > 0) {
				sumGains = sumGains.add(diff);
			}else{
				sumLosses = sumLosses.add(diff.abs());
			};
			
			previousValue = currentValue;
		}
		
		System.out.println("=================================");
		System.out.println("sumUps:"+sumGains.toPlainString());
		System.out.println("sumDowns:"+sumLosses.toPlainString());
		
		//------------------------------------
		// STEP 2: Averages of Ups & Downs
		BigDecimal count = new BigDecimal(period).setScale(precision);
		BigDecimal avgGains = sumGains.divide(count, ROUND_UP);
		BigDecimal avgLosses = sumLosses.divide(count, ROUND_UP);
		
		//------------------------------------
		// STEP 3: Calculate RSI
		// RSI = ( avgUps / (avgUps + avgDowns) ) * 100
		BigDecimal sumAvgUpDown = avgGains.add(avgLosses);

		BigDecimal rsi;
		if(avgLosses.compareTo(ZERO) == 0) {
			rsi = BIG_100; 
		}else if(avgGains.compareTo(ZERO) == 0) {
			rsi = ZERO; 
		}else {
			rsi = avgGains.divide(sumAvgUpDown, ROUND_UP).multiply(BIG_100);
		}
		
		//------------------------------------
		// STEP 3: Calculate RSI - Alternative
		// RSI = ( avgUps / (avgUps + avgDowns) ) * 100
//		BigDecimal rsi;
//		if(avgLosses.compareTo(ZERO) == 0) {
//			rsi = BIG_100; 
//		}else if(avgGains.compareTo(ZERO) == 0) {
//			rsi = ZERO; 
//		}else {
//			// Formula: RSI = (100 - (100 / (1 + avgGains / avgLosses)) );
//			
//			BigDecimal quotient = avgGains.divide(avgLosses, ROUND_UP);
//			BigDecimal quotientPlusOne = quotient.add(ONE);
//			BigDecimal percent = BIG_100.divide(quotientPlusOne, ROUND_UP);
//			rsi = BIG_100.subtract(percent);
//			
//			System.out.println("count:"+count.toPlainString());
//			System.out.println("avgGains:"+avgGains.toPlainString());
//			System.out.println("avgLosses:"+avgLosses.toPlainString());
//			System.out.println("quotient:"+quotient.toPlainString());
//			System.out.println("Formula: ("+ avgGains.toPlainString()
//								+" / ("+ avgGains.toPlainString()
//								+" + "+ avgLosses.toPlainString()
//								+") ) * 100 "
//								);
//		}
	
		return rsi.setScale(precision);
		
	}
	
}
