package com.xresch.cfw.features.query.functions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.TreeSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.ibm.icu.math.MathContext;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query._CFWQueryCommon;
import com.xresch.cfw.features.query.parse.QueryPartValue;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionCorr extends CFWQueryFunction {

	public static final String FUNCTION_NAME = "corr";

	private ArrayList<BigDecimal> valuesX = new ArrayList<>(); 
	private ArrayList<BigDecimal> valuesY = new ArrayList<>(); 
	
	private boolean isAggregated = false;
	
	public CFWQueryFunctionCorr(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return FUNCTION_NAME;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public TreeSet<String> getTags(){
		TreeSet<String> tags = new TreeSet<>();
		tags.add(_CFWQueryCommon.TAG_AGGREGATION);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(valueOrFieldname, valueOrFieldname)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Aggregation function to calculate the correlation of two values.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 
			 "<ul>"
			 +"<li><b>valueOrFieldname:&nbsp;</b>The value or fieldname.</li>"
			+"</ul>"
			;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_"+FUNCTION_NAME+".html");
	}


	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public boolean supportsAggregation() {
		return true;
	}
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	private QueryPartValue calculateCorrelation() {
		
		if(valuesX.isEmpty() || valuesY.isEmpty()) {
			return QueryPartValue.newNull();
		}
		
		int size = Math.min(valuesX.size(), valuesY.size());
		BigDecimal bigSize = new BigDecimal(size);
        BigDecimal sumX = BigDecimal.ZERO;
        BigDecimal sumY = BigDecimal.ZERO;
        BigDecimal sumXY = BigDecimal.ZERO;
        BigDecimal sumX2 = BigDecimal.ZERO;
        BigDecimal sumY2 = BigDecimal.ZERO;

        // Compute sums
        for (int i = 0; i < size; i++) {
            BigDecimal x = valuesX.get(i);
            BigDecimal y = valuesY.get(i);

            sumX = sumX.add(x);
            sumY = sumY.add(y);
            sumXY = sumXY.add(x.multiply(y));
            sumX2 = sumX2.add(x.multiply(x));
            sumY2 = sumY2.add(y.multiply(y));
        }

        // Compute standard deviation components
        BigDecimal numerator = sumXY.subtract( sumX.multiply(sumY).divide(bigSize, CFW.Math.MATHCONTEXT_HALF_UP) );
        BigDecimal denominatorX = sumX2.subtract( sumX.multiply(sumX).divide(bigSize, CFW.Math.MATHCONTEXT_HALF_UP) );
        BigDecimal denominatorY = sumY2.subtract( sumY.multiply(sumY).divide(bigSize, CFW.Math.MATHCONTEXT_HALF_UP) );

        // Compute correlation
        BigDecimal denominator = (denominatorX.multiply(denominatorY)).sqrt(CFW.Math.MATHCONTEXT_HALF_UP);
        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            return QueryPartValue.newNumber(0); // Avoid division by zero
        }

        return QueryPartValue.newNumber(
        			numerator.divide(denominator, CFW.Math.MATHCONTEXT_HALF_UP)
        		);
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	private void addValueToAggregation(JsonElement valueX, JsonElement valueY) {
		addValueToAggregation(
				  QueryPartValue.newFromJsonElement(valueX)
				, QueryPartValue.newFromJsonElement(valueY)
			);
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	private void addValueToAggregation(QueryPartValue valueX, QueryPartValue valueY) {
		
		if(valueX.isNumberOrNumberString()) {
			valuesX.add(valueX.getAsBigDecimal());
		}else {
			valuesX.add(BigDecimal.ZERO);
		}
		
		if(valueY.isNumberOrNumberString()) {
			valuesY.add(valueY.getAsBigDecimal());
		}else {
			valuesY.add(BigDecimal.ZERO);
		}
	}
	

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void aggregate(EnhancedJsonObject object,ArrayList<QueryPartValue> parameters) {
		
		isAggregated = true;
		
		int paramCount = parameters.size();
		if(paramCount < 2) {
			return;
		}

		QueryPartValue valueX = parameters.get(0);
		QueryPartValue valueY = parameters.get(1);
		
		addValueToAggregation(valueX, valueY);
		
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public QueryPartValue execute(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters) {
				
		//-------------------------------
		// Do Not Aggregated
		if(!isAggregated) {
	
			if(parameters.size() < 2) {
				return QueryPartValue.newNull();
			}
			
			QueryPartValue valueX = parameters.get(0);
			QueryPartValue valueY = parameters.get(1);
			
			//---------------------
			// ValueX
			if(valueX.isJsonArray() && valueY.isJsonArray() ){
				JsonArray arrayX = valueX.getAsJsonArray();
				JsonArray arrayY = valueY.getAsJsonArray();
				int size = Math.min(arrayX.size(), arrayY.size());
				for(int i = 0; i < size; i++) {
					addValueToAggregation(arrayX.get(i), arrayY.get(i));
				}
			}else {
				addValueToAggregation(valueX, valueY);
			}

		}
				
		//-------------------------------
		// Create Array and Return
		return calculateCorrelation();

	}

}
