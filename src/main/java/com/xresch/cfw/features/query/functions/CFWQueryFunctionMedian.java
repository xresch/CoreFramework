package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.QueryPartValue;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
											//!!!!!!!!!!!!!!!!!!!!!!!
											//!!!!!!!!!!!!!!!!!!!!!!!
											// Beware of the mighty
											// extension!
											// vvvvvvvvvvvvvvvvvvvvvv
public class CFWQueryFunctionMedian extends CFWQueryFunctionPerc {
	
	public CFWQueryFunctionMedian(CFWQueryContext context) {
		super(context);
		
		percentile = 50;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return "median";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "median(valueOrFieldname, includeNulls)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Aggregation function to calculate median values.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>valueOrFieldname:&nbsp;</b>The value or fieldname used for the count.</p>"
			 + "<p><b>includeNulls:&nbsp;</b>(Optional)Toggle if null values should be included in the median(Default:false).</p>"
			;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_median.html");
	}
	
	
	/***********************************************************************************************
	 * Creates parameters for perc-Function by mapping of median function parameters and adding
	 ***********************************************************************************************/
	private ArrayList<QueryPartValue> createPercParams(ArrayList<QueryPartValue> parameters) {
		ArrayList<QueryPartValue> percParams = new ArrayList<QueryPartValue>();
		
		//-----------------------------------
		//handle value param
		if(parameters.size() > 0) {
			percParams.add(parameters.get(0));
			percParams.add(QueryPartValue.newNumber(50));
		}
		
		//-----------------------------------
		//handle value param
		if(parameters.size() > 1) {
			percParams.add(parameters.get(1));
		}
		return percParams;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void aggregate(EnhancedJsonObject object,ArrayList<QueryPartValue> parameters) {
		
		super.aggregate(object, createPercParams(parameters));
	
	}
	
	/**
	 * @return *********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public QueryPartValue execute(EnhancedJsonObject object,ArrayList<QueryPartValue> parameters) {
		
		return super.execute(object, createPercParams(parameters));
	
	}

}
