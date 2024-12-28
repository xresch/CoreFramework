package com.xresch.cfw.utils;

public class ParabolicSAR {

	/**
	 * parabolicSars contains psar values
	 */
	private double[] parabolicSars;

	/**
	 * The trends represents the trends where above 0 is positive and below zero is negative
	 */
	private int[] trends;
	
	 /**
	   * The trendFlip when true indicates the trend change since last time
	   */
	private boolean[] trendFlip;

	public void calculate(double[] high, double[] low) {
		this.calculate(high, low, 0.02, 0.20);
	}

	/**
	 * Calculate PSAR
	 * @param high values
	 * @param low values
	 * @param acceleration used to calculate PSAR
	 * @param accelaration maximum used to calculate PSAR
	 */
	public void calculate(double[] high, double[] low, double acceleration, double accelerationMax) {

		/* 
		 Uptrend Formula:  		PSAR(i) = (HIGH(i-1) - PSAR(i-1)) * AF+PSAR(i-1)
		 Downtrend Formula: 	PSAR(i) = (LOW(i-1) - PSAR(i-1)) * AF+PSAR(i-1)
		 AF Formula: 			АF = 0,02 + ix*K
		 
		 PSAR is the Parabolic value. With index (i) it’s the current value, and with (i – 1) it’s the value preceding the calculated one.

		 Definitions:
		 - HIGH:	is the price high.
		 - LOW:		is the price low.
		 - AF:		is the acceleration factor. Its value grows with a step set for each period when new extreme price values ​​are reached. Wilder recommends using an initial factor of 0.02, which increases by 0.02 with each new bar until it reaches a maximum value of 0.2.
		 - ix: 		is the number of periods accumulated since the beginning of counting;
         - K:		is the step of price change, which by default is 0.02.
         
		*/
		
		//-----------------------------------
		// Variables
		this.parabolicSars = new double[high.length];
		this.trends = new int[high.length];
		this.trendFlip = new boolean[high.length];

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

	}

	public double[] getPsars() {
		return this.parabolicSars;
	}

	public int[] getTrends() {
		return this.trends;
	}
	
	public boolean[] getTrendChanged() {
		return this.trendFlip;
	}
}
