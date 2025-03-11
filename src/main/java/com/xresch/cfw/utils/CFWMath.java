package com.xresch.cfw.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

import com.xresch.cfw._main.CFW;

public class CFWMath {

	
	private static CFWMath INSTANCE = new CFWMath();
	public static final int GLOBAL_SCALE = 6;
	
	public static final BigDecimal ZERO = BigDecimal.ZERO;
	public static final BigDecimal ONE = BigDecimal.ONE;
	public static final BigDecimal ONE_POINT_FIVE = new BigDecimal(1.5);
	public static final BigDecimal TWO = new BigDecimal(2);
	public static final BigDecimal BIG_NEG_ONE = new BigDecimal(-1);
	public static final BigDecimal BIG_100 = new BigDecimal(100);
	
	public static final RoundingMode ROUND_UP = RoundingMode.HALF_UP;
	
	/** Math Context with precision of CFW.Math.GLOBAL_SCALE and rounding mode HALF_UP*/
	public static final MathContext MATHCONTEXT_HALF_UP = new MathContext(GLOBAL_SCALE, ROUND_UP);
	
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
	 * Returns the Average
	 * @param precision TODO
	 * @return average value in the list, null if list is empty or all values are null
	 ***********************************************************************************************/
	public static BigDecimal bigAvg(List<BigDecimal> values, int precision) {
		
		while( values.remove(null) ); // remove all null values
		if(values.isEmpty()) { return null; }
		
		BigDecimal sum = bigSum(values, precision);
		sum = sum.setScale(precision, ROUND_UP); // won't calculate decimals if not set
		if(sum == null) { return null; } 
		
		BigDecimal count = new BigDecimal(values.size());
		
		return sum.divide(count, ROUND_UP);
		
	}
	
	/***********************************************************************************************
	 * 
	 * @param precision TODO
	 * @return sum value in the list, null if list is empty or all values are null
	 ***********************************************************************************************/
	public static BigDecimal bigSum(List<BigDecimal> values, int precision) {
		
		while( values.remove(null) ); // remove all null values
		if(values.isEmpty()) { return null; }
		
		BigDecimal sum = ZERO.setScale(precision);

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
		
		ArrayList<BigDecimal> sortedClone = new ArrayList<>();
		sortedClone.addAll(values);	
		sortedClone.sort(null);
		
		return bigMedian(sortedClone, true);
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public static BigDecimal bigMedian(List<BigDecimal> values, boolean isSorted) {
		
		int count = values.size();
		
		if(count == 0) {
			return null;
		}
		
		int percentile = 50;
		int percentilePosition = (int)Math.ceil( count * (percentile / 100f) );
		
		//---------------------------
		// Retrieve number
		boolean isEvenCount = (count % 2 == 0);
		
		if(!isSorted) {
			values.sort(null);
		}
		
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
		BigDecimal sum = bigSum(partialValues, precision);
		sum = sum.setScale(precision, ROUND_UP); // won't calculate decimals if not set
		if(sum == null) { return null; } 
		
		BigDecimal count = new BigDecimal(partialValues.size());
		
		return sum.divide(count, ROUND_UP);
		
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
	
		if(diffPercent == null) { return null; } 
		diffPercent = diffPercent.setScale(precision, ROUND_UP); // won't calculate decimals if not set
		
		return diffPercent;
		
	}

	/***********************************************************************************************
	 * Returns an array of booleans an outlier based on Inter-Quantile-Range(IQR).
	 * 
	 * @param values the list of values
	 * @param sensitivity the multiplier for the IQR, higher values mean less sensitive to outliers (default 1.5)
	 * @return array of booleans, may contain null if input number was null.
	 ***********************************************************************************************/
    public static ArrayList<Boolean> bigOutlier(List<BigDecimal> values, BigDecimal sensitivity) {
    	        
    	ArrayList<Boolean> result = new ArrayList<>();
    	
    	if(values.isEmpty()) {
    		return result;
    	}
    	
    	if(sensitivity == null) { sensitivity = ONE_POINT_FIVE; }
    	
    	List<BigDecimal> valuesToSort = new ArrayList<>();
    	
    	//bigPercentile sorts the values, we don't want that on the original array
    	valuesToSort.addAll(values);
        BigDecimal perc25 = bigPercentile(25, valuesToSort);
        BigDecimal perc75 =  bigPercentile(75, valuesToSort);

        BigDecimal iqr = perc75.subtract(perc25);

        BigDecimal lowerBound = perc25.subtract(iqr.multiply(sensitivity));
        BigDecimal upperBound = perc75.add(iqr.multiply(sensitivity));


        for(BigDecimal value : values) {
        	
        	if(value == null) {
        		result.add(null);
        	}else if (value.compareTo(lowerBound) < 0 
        		   || value.compareTo(upperBound) > 0 ){
        		result.add(true);
        		
	        }else {
	        	result.add(false);
	        }

        }
        
        return result;
        
    }
    
	/***********************************************************************************************
	 * Returns true if the value is an outlier based on Modified Z-Score.
	 * Will return false if there are not enough datapoints.
	 * 
	 * @param values the list of values
	 * @param period number of points that should be used for calculating the moving average
	 * @param precision TODO
	 * @param sensitivity 
	 * @return boolean , null if list size is smaller than datapoints
	 ***********************************************************************************************/
	public static Boolean bigIsOutlierModifiedZScore(List<BigDecimal> values, BigDecimal value, int precision, BigDecimal sensitivity) {
		
		if(value == null ) { return null; }
		while( values.remove(null) );
		if(values.isEmpty() ) { return null; }
		
		if(sensitivity == null) {
			sensitivity = new BigDecimal("3.5");
		}

		
		BigDecimal median = bigMedian(values);
		BigDecimal mad = bigMedianAbsoluteDeviation(values, median);

		if (mad.compareTo(BigDecimal.ZERO) == 0) {
			return false; // Avoid division by zero
		}

		// Compute the Modified Z-score
		BigDecimal modifiedZ = 
				value.subtract(median)
					 .divide(mad, precision, RoundingMode.HALF_UP)
					 .multiply(new BigDecimal("0.6745"))
					 ;

		 return modifiedZ.abs().compareTo(sensitivity) > 0; // Threshold for outlier detection
 
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
		
		BigDecimal average = bigAvg(values, precision);

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
	 * 
	 ***********************************************************************************************/
	public static BigDecimal bigMedianAbsoluteDeviation(List<BigDecimal> values) {
		if(values.size() <= 1) {
			return ZERO;
		}
		BigDecimal median = bigMedian(values);
		
		return bigMedianAbsoluteDeviation(values, median);
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public static BigDecimal bigMedianAbsoluteDeviation(List<BigDecimal> values, BigDecimal median) {
		
		while( values.remove(null) );
		
		// zero or one number will have MAD of 0
		if(values.size() <= 1) {
			return ZERO;
		}
	
		ArrayList<BigDecimal> deviations = new ArrayList<>();
        for (BigDecimal num : values) {
            deviations.add(num.subtract(median).abs());
        }
        
        return bigMedian(deviations);
		
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
			
		return rsi.setScale(precision, ROUND_UP);
		
	}
	
	
	/***********************************************************************************************
	 * Returns a new instance of periodic math.
	 * 
	 * @param period number of points that should be used for calculating the moving average
	 * @param precision the precision of digits for the resulting values.
	 * 
	 * @return CFWMathPeriodic
	 ***********************************************************************************************/
	public static CFWMathPeriodic createPeriodic(int period, int precision) {
		return INSTANCE.new CFWMathPeriodic(period, precision);
	}
	
	/***********************************************************************************************
	 * Returns a new instance of the parabolic SAR math object.
	 * 
	 * @param acceleration the acceleration, e.g. 0.02
	 * @param acceleration the maximum acceleration, e.g. 0.2
	 * 
	 * @return CFWMathParabolicSAR
	 ***********************************************************************************************/
	public static CFWMathParabolicSAR createParabolicSAR(double acceleration, double accelerationMax, int precision) {
		return INSTANCE.new CFWMathParabolicSAR(acceleration, accelerationMax, precision);
	}
	
	/***********************************************************************************************
	 * A class to do calculations based on periods.
	 * This class was introduced to make the calculations more efficient by storing values and reuse them
	 * .
	 * @author retos
	 *
	 ***********************************************************************************************/
	public class CFWMathPeriodic {
		
		
		int period = -1;
		BigDecimal bigPeriod = null;
		BigDecimal bigPeriodMinusOne = null;
		private int precision = 3;
		
		private boolean nonNullFound = false;
		private List<BigDecimal> inputValues = new ArrayList<>();
		
		private List<BigDecimal> rsiValues = new ArrayList<>();

		
		/***********************************************************************************************
		 * 
		 * @param period amount of datapoints used in the calculation
		 * @param precision decimal precision, number of digits after the decimal point
		 * 
		 ***********************************************************************************************/
		public CFWMathPeriodic(int period, int precision){
			this.period = period;
			this.bigPeriod = new BigDecimal(period).setScale(precision);
			this.bigPeriodMinusOne = bigPeriod.subtract(ONE);
			this.precision = precision;
		}
		
		
		/***********************************************************************************************
		 * Returns a moving average value for the values subsequently passed to this method.
		 * Will return null if there are not enough datapoints.
		 * 
		 * @param values the list of values
		 * @param acceleration number of points that should be used for calculating the moving average
		 * @param precision the precision of digits for the resulting values.
		 * @return moving average value, null if list size is smaller than datapoints, or for every
		 * null value at the beginning of the data.
		 ***********************************************************************************************/
		public BigDecimal calcMovAvg(BigDecimal value) {
			
			//-------------------------------
			// Handle Nulls
			if(value == null) { 
				if(nonNullFound) {value = ZERO;}
				else{ return null; }
			}else {
				nonNullFound = true;
			}
			
			//-------------------------------
			// Add Value
			inputValues.add(value);
			
			if(inputValues.size() < period ) { 
				return null; 
			}
			
			List<BigDecimal> partialValues = inputValues.subList(inputValues.size() - period, inputValues.size());
			BigDecimal sum = bigSum(partialValues, precision);
			sum = sum.setScale(precision, ROUND_UP); // won't calculate decimals if not set
			if(sum == null) { return null; } 
			
			BigDecimal count = new BigDecimal(partialValues.size());
			
			return sum.divide(count, ROUND_UP);
			
		}
		
		/***********************************************************************************************
		 * Returns a moving difference value for the values subsequently passed to this method.
		 * Will return null if there are not enough data points.
		 * 
		 * @param values the list of values
		 * @param acceleration number of points that should be used for calculating the moving average
		 * @param precision the precision of digits for the resulting values.
		 * @return moving average value, null if list size is smaller than data points or for every
		 * null value at the beginning of the data.
		 ***********************************************************************************************/
		public BigDecimal calcMovDiff(BigDecimal value) {
			
			//-------------------------------
			// Handle Nulls
			if(value == null) { 
				if(nonNullFound) {value = ZERO;}
				else{ return null; }
			}else {
				nonNullFound = true;
			}
						
			//-------------------------------
			// Add Value
			inputValues.add(value);
			
			if(inputValues.size() < period ) { 
				return null; 
			}
			
			BigDecimal youngerValue = inputValues.get(inputValues.size()-1);
			BigDecimal olderValue = inputValues.get(inputValues.size() - period);
			
			//---------------------------------
			// Handle Zeros
			if(youngerValue.compareTo(ZERO) == 0) {
				if(olderValue.compareTo(ZERO) == 0) {
					return ZERO;
				}
				
				return null;
			}else if(olderValue.compareTo(ZERO) == 0) {
				return null;
			}
			
			//---------------------------------
			// Calculate
			MathContext mc = new MathContext(precision, ROUND_UP);
			BigDecimal diffPercent = youngerValue
										.subtract(olderValue)
										.divide(olderValue, mc)
										.multiply(BIG_100, mc)
										;
			
			if(diffPercent == null) { return null; } 
			diffPercent = diffPercent.setScale(precision, ROUND_UP); // won't calculate decimals if not set
			
			return diffPercent;
			
		}
		/***********************************************************************************************
		 * Returns a moving standard deviation value for the last N values in the given list.
		 * Will return null if there are not enough datapoints.
		 * 
		 * @param values the list of values
		 * @param acceleration number of points that should be used for calculating the moving average
		 * @param precision TODO
		 * @return moving average value , null if list size is smaller than datapoints
		 ***********************************************************************************************/
		public BigDecimal calcMovStdev(BigDecimal value, boolean usePopulation) {
			
			//-------------------------------
			// Handle Nulls
			if(value == null) { 
				if(nonNullFound) {value = ZERO;}
				else{ return null; }
			}else {
				nonNullFound = true;
			}
			
			//-------------------------------
			// Add Value
			inputValues.add(value);
			
			if(inputValues.size() < period ) { 
				return null; 
			}
			
			List<BigDecimal> partialValues = inputValues.subList(inputValues.size() - period, inputValues.size());
			
			return bigStdev(partialValues, usePopulation, precision);
			
		}
		

		
		/***********************************************************************************************
		 * Returns the Relative Strength Index(RSI) for the values subsequently passed to this method.
		 * Will return null if there are not enough values.
		 * 
		 * It is recommended to use CFW.Math.nullToZero() before using this method if the method can contain
		 * null values. Null values will be removed from the list before calculations take place.
		 * 
		 * This method uses the calculation formulas as described here:
		 * https://www.zaner.com/3.0/education/technicalstudies/RSI.asp#top
		 * 
		 * @param rsiValues the list of values
		 * @param bigAcceleration number of points that should be used for calculating the moving average
		 * @param precision the precision of digits for the resulting values.
		 * 
		 * @return moving average value , null if list size is smaller than datapoints
		 ***********************************************************************************************/
		public BigDecimal calcRSI(BigDecimal value) {
			
			//-------------------------------
			// Handle Nulls
			if(value == null) { 
				if(nonNullFound) {value = ZERO;}
				else{ return null; }
			}else {
				nonNullFound = true;
			}
			
			//-------------------------------
			// Add Value
			rsiValues.add(value);
			
			if(rsiValues.size() < period ) { 
				return null; 
			}

			// this has very low performance
			// probably because of calculating BigDecimals and also JIT compiler
			// looping the array in STEP 1 below every time is much more efficient.
			// The difference is as high as 4 seconds and 0.2 seconds 
			
//			if( rsiAvgGains != null) {
//				
//				//======================================================
//				// Calculate subsequent RSI
//				
//				//------------------------------------
//				// STEP 2.5: Update Averages of Ups & Downs
//				// rsiAvgGains = ( (rsiAvgGains * (period-1) ) + currentUp) / period
//				// rsiAvgLosses = ( (rsiAvgLosses * (period-1) ) + currentDown) / period
//				BigDecimal diff = value.subtract(rsiPreviousValue);
//
//				if(diff.compareTo(ZERO) >= 0) {
//					rsiAvgGains = rsiAvgGains.multiply(bigPeriodMinusOne).add(diff).divide(bigPeriod, ROUND_UP);
//					rsiAvgLosses = rsiAvgLosses.multiply(bigPeriodMinusOne).divide(bigPeriod, ROUND_UP); // basically add "zero"
//				}else{
//					rsiAvgGains = rsiAvgGains.multiply(bigPeriodMinusOne).divide(bigPeriod, ROUND_UP); // basically add "zero"
//					rsiAvgLosses = rsiAvgLosses.multiply(bigPeriodMinusOne).add(diff.abs()).divide(bigPeriod, ROUND_UP);
//				}
//				
//				rsiPreviousValue = value;
//								
//			} else {
			
			//======================================================
			// Calculate first RSI
			
			//------------------------------------
			// STEP 1: Sums of Ups & Downs
			int i = rsiValues.size() - period;
			BigDecimal rsiPreviousValue = rsiValues.get(i);
			BigDecimal sumGains = ZERO.setScale(precision);
			BigDecimal sumLosses = ZERO.setScale(precision);
			
			for(i++ ; i < rsiValues.size(); i++) {
				
				BigDecimal currentValue = rsiValues.get(i);
				BigDecimal diff = currentValue.subtract(rsiPreviousValue);

				if(diff.compareTo(ZERO) > 0) {
					sumGains = sumGains.add(diff);
				}else{
					sumLosses = sumLosses.add(diff.abs());
				};
				
				rsiPreviousValue = currentValue;
				
			}
			//------------------------------------
			// STEP 2: Averages of Ups & Downs
			BigDecimal rsiAvgGains = sumGains.divide(bigPeriod, ROUND_UP);
			BigDecimal rsiAvgLosses = sumLosses.divide(bigPeriod, ROUND_UP);
							
			//------------------------------------
			// STEP 3: Calculate RSI
			// RSI = ( avgUps / (avgUps + avgDowns) ) * 100
			BigDecimal sumAvgUpDown = rsiAvgGains.add(rsiAvgLosses);

			BigDecimal rsi;
			if(rsiAvgLosses.compareTo(ZERO) == 0) {
				rsi = BIG_100; 
			}else if(rsiAvgGains.compareTo(ZERO) == 0) {
				rsi = ZERO; 
			}else {
				rsi = rsiAvgGains.divide(sumAvgUpDown, ROUND_UP).multiply(BIG_100);
			}
					
			return rsi.setScale(precision, ROUND_UP);

		}
		
	}
	
	/***********************************************************************************************
	 * A class to do calculations based on periods.
	 * This class was introduced to make the calculations more efficient by storing values and reuse them
	 * .
	 * @author retos
	 *
	 ***********************************************************************************************/
	public class CFWMathParabolicSAR {
		
		double acceleration = -1;
		double accelerationMax = -1;
		private int precision = 3;
		private boolean nonNullFound = false;
		
		BigDecimal bigAcceleration = null;
		BigDecimal bigAccelerationMax = null;
		BigDecimal accelerationFactor = ZERO;
		
		BigDecimal extremePoint;
		
		//-------------------------------------------
		// Parabolic SAR
		private List<BigDecimal> parabolicSars = new ArrayList<>();
		private List<Integer> trends = new ArrayList<>();
		private List<Boolean> trendFlip = new ArrayList<>();
		private List<BigDecimal> psarHighs = new ArrayList<>();
		private List<BigDecimal> psarLows = new ArrayList<>();

		/***********************************************************************************************
		 * 
		 * @param acceleration amount of datapoints used in the calculation
		 * @param precision decimal precision, number of digits after the decimal point
		 * 
		 ***********************************************************************************************/
		public CFWMathParabolicSAR(double acceleration, double accelerationMax, int precision){
			this.acceleration = acceleration;
			this.accelerationMax = accelerationMax;
			
			this.bigAcceleration = new BigDecimal(acceleration).setScale(precision, ROUND_UP);
			this.bigAccelerationMax = new BigDecimal(accelerationMax).setScale(precision, ROUND_UP);
			this.accelerationFactor = bigAcceleration;

			this.precision = precision;
		}
		
		/***********************************************************************************************
		 * Calculate PSAR
		 * 
		 * Uptrend Formula:  	PSAR(i) =  PSAR(i-1) + ( AF * (HIGH(i-1) - PSAR(i-1)) )
		 * Downtrend Formula: 	PSAR(i) =  PSAR(i-1) + ( AF * (LOW(i-1)  - PSAR(i-1)) )
		 * AF Formula: 			АF = 0,02 + (ix * K)
		 * 
		 * PSAR is the Parabolic value. With index (i) it’s the current value, and with (i – 1) it’s the value preceding the calculated one.
		 * 
		 * Definitions:
		 * - HIGH:	is the price high.
		 * - LOW:		is the price low.
		 * - AF:		is the acceleration factor. Its value grows with a step set for each period when new extreme price values ​​are reached. Wilder recommends using an initial factor of 0.02, which increases by 0.02 with each new bar until it reaches a maximum value of 0.2.
		 * - ix: 		is the number of periods accumulated since the beginning of counting;
         * - K:		is the step of price change, which by default is 0.02.
	     *     
		 * @param high values
		 * @param low values
		 * 
		 * @return null for the first value pair, afterwards PSAR for every successive call
		 * 
		 ***********************************************************************************************/
		public BigDecimal calcPSAR(BigDecimal high, BigDecimal low) {
			//-------------------------------
			// Handle Nulls
			if(high == null && low == null) { 
				if(nonNullFound) {high = ZERO; low = ZERO;}
				else{ return null; }
			}else {
				nonNullFound = true;
			}
			
			//-----------------------------------
			// Sanitize
			if(high == null) { high = ZERO; }
			if(low == null) { low = ZERO; }
			
			//-----------------------------------
			// Is first call
			psarHighs.add(high);
			psarLows.add(low);
			
			if(psarHighs.size() == 1) {
				trends.add(0);
				trendFlip.add(false);
				return null;
			}
			
			//-----------------------------------
			// On Second Call: Initialize
			if(psarHighs.size() == 2) {
				BigDecimal firstHigh = psarHighs.get(0);
				BigDecimal secondHigh = psarHighs.get(1);
				BigDecimal firstLow = psarHighs.get(0);
				BigDecimal secondLow = psarHighs.get(1);
				
				//int trend = (high[1] >= high[0] || low[0] <= low[1]) ? +1 : -1;
				int trend = (secondHigh.compareTo(firstHigh) >= 0 
						  || firstLow.compareTo(secondLow) <= 0) ? +1 : -1;
				
				BigDecimal parabolicSar = (trend > 0) ? firstLow : firstHigh;
				extremePoint = (trend > 0) ? firstHigh : firstLow;
				
				parabolicSars.add(parabolicSar);
				trends.add(trend);
				trendFlip.add(false);
				return null;
			}


			//-----------------------------------
			// Init first Parabolic Sar and Trend values
			// SAR Results

			BigDecimal nextSar;
			BigDecimal lastPSAR = parabolicSars.get(parabolicSars.size()-1);
			
			BigDecimal tomorrowsHigh = psarHighs.get( psarHighs.size()-1 );
			BigDecimal currentHigh = psarHighs.get( psarHighs.size()-2 );
			BigDecimal lastHigh = psarHighs.get( psarHighs.size()-3 );
			
			BigDecimal tomorrowsLow = psarLows.get( psarLows.size()-1 );
			BigDecimal currentLow = psarLows.get( psarLows.size()-2 );
			BigDecimal lastLow = psarLows.get( psarLows.size()-3 );
			
			Integer currentTrend = trends.get(trends.size()-1);
			
			//-----------------------------------
			// Up Trend if trend is bigger then 0 else it's a down trend
			if (currentTrend > 0) {

				//-----------------------------------
				// Higher highs, accelerate
				if (currentHigh.compareTo(extremePoint) > 0) {
					extremePoint = currentHigh;
					accelerationFactor = bigAccelerationMax.min( accelerationFactor.add(bigAcceleration) );
				}

				//-----------------------------------
				// Next Parabolic SAR based on today's close/price value
				// nextSar = parabolicSar + (accelerationFactor * (extremePoint - parabolicSar) );
				
				BigDecimal diffEPtoSAR = extremePoint.subtract(lastPSAR).setScale(precision, ROUND_UP);
				BigDecimal accelarated = diffEPtoSAR.multiply(accelerationFactor);
				nextSar = lastPSAR.add(accelarated);
				
				//-----------------------------------
				// Rule: Parabolic SAR can not be above prior period's low or
				// the current low.
				nextSar = currentLow.min(lastLow).min(nextSar);

				//-----------------------------------
				// Rule: If Parabolic SAR crosses tomorrow's price range, the
				// trend switches.
				if (nextSar.compareTo(tomorrowsLow) > 0) {
					currentTrend = -1;
					nextSar = extremePoint;
					extremePoint = tomorrowsLow;
					accelerationFactor = bigAcceleration;
				}

			} else {
				//-----------------------------------
				// Making lower lows: accelerate
				if (currentLow.compareTo(extremePoint) < 0) {
					extremePoint = currentLow;
					accelerationFactor = bigAccelerationMax.min( accelerationFactor.add(bigAcceleration) );
				}

				//-----------------------------------
				// Next Parabolic SAR based on today's close/price value
				// nextSar = lastPSAR + ( accelerationFactor * (extremePoint - lastPSAR));
				BigDecimal diffEPtoSAR = extremePoint.subtract(lastPSAR).setScale(precision, ROUND_UP);
				BigDecimal accelarated = diffEPtoSAR.multiply(accelerationFactor);
				nextSar = lastPSAR.add(accelarated);
				
				//-----------------------------------
				// Rule: Parabolic SAR can not be below prior period's high or
				// the current high.
				//nextSar = (i > 0) ? Math.max(Math.max(high[i], high[i - 1]), nextSar) : Math.max(high[i], nextSar);
				nextSar = currentHigh.max(lastHigh).max(nextSar);
				
				//-----------------------------------
				// Rule: If Parabolic SAR crosses tomorrow's price range, the
				// trend switches.
				if (nextSar.compareTo(tomorrowsHigh) < 0) {
					currentTrend = +1;
					nextSar = extremePoint;
					extremePoint = tomorrowsHigh;
					accelerationFactor = bigAcceleration;
				}
			}


			//-----------------------------------
			// Handle Trend
			this.trends.add(currentTrend);
			
			boolean isTrendFlip = (trends.get(trends.size()-2) != currentTrend) ? true : false;
			this.trendFlip.add(isTrendFlip);	
			
			//-----------------------------------
			// Return Value
			this.parabolicSars.add(nextSar); 
			return nextSar;
		

			/* Original
			 
			//-----------------------------------
			// Variables

			int trend = (high[1] >= high[0] || low[0] <= low[1]) ? +1 : -1;

			double parabolicSar = (trend > 0) ? low[0] : high[0];

			double extremePoint = (trend > 0) ? high[0] : low[0];

			double accelerationFactor = 0;

			//-----------------------------------
			// Init first Parabolic Sar and Trend values
			this.parabolicSars[1] = parabolicSar; // SAR Results
			this.trends[1] = trend; // Trend Directions

			int ct = this.parabolicSars.length - 1;

			for (int i = 1; i < ct; i++) {

				double nextSar;

				//-----------------------------------
				// Up Trend if trend is bigger then 0 else it's a down trend
				if (trend > 0) {

					//-----------------------------------
					// Higher highs, accelerate
					if (high[i] > extremePoint) {
						extremePoint = high[i];
						accelerationFactor = Math.min(accelerationMax, accelerationFactor + acceleration);
					}

					//-----------------------------------
					// Next Parabolic SAR based on today's close/price value
					nextSar = parabolicSar + accelerationFactor * (extremePoint - parabolicSar);

					//-----------------------------------
					// Rule: Parabolic SAR can not be above prior period's low or
					// the current low.
					nextSar = (i > 0) ? Math.min(Math.min(low[i], low[i - 1]), nextSar) : Math.min(low[i], nextSar);

					//-----------------------------------
					// Rule: If Parabolic SAR crosses tomorrow's price range, the
					// trend switches.
					if (nextSar > low[i + 1]) {
						trend = -1;
						nextSar = extremePoint;
						extremePoint = low[i + 1];
						accelerationFactor = acceleration;
					}

				} else {
					//-----------------------------------
					// Making lower lows: accelerate
					if (low[i] < extremePoint) {
						extremePoint = low[i];
						accelerationFactor = Math.min(accelerationMax, accelerationFactor + acceleration);
					}

					//-----------------------------------
					// Next Parabolic SAR based on today's close/price value
					nextSar = parabolicSar + accelerationFactor * (extremePoint - parabolicSar);

					//-----------------------------------
					// Rule: Parabolic SAR can not be below prior period's high or
					// the current high.
					nextSar = (i > 0) ? Math.max(Math.max(high[i], high[i - 1]), nextSar) : Math.max(high[i], nextSar);

					//-----------------------------------
					// Rule: If Parabolic SAR crosses tomorrow's price range, the
					// trend switches.
					if (nextSar < high[i + 1]) {
						trend = +1;
						nextSar = extremePoint;
						extremePoint = high[i + 1];
						accelerationFactor = acceleration;
					}
				}

				//-----------------------------------
				// System.out.println(extremePoint + " " + accelerationFactor);

				this.parabolicSars[i + 1] = Math.round(nextSar); // TODO round BigDecimal precision 2
				this.trends[i + 1] = trend;
				
				if(this.trends[i] != this.trends[i+1]) {
					this.trendFlip[i+1] = true;	
				} else {
					this.trendFlip[i+1] = false;	
				}

				parabolicSar = nextSar;
			}
			 */
		}
		
	}
	
	
}
