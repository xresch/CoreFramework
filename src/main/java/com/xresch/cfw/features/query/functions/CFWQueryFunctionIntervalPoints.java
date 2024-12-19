package com.xresch.cfw.features.query.functions;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.TreeSet;

import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.QueryPartValue;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionIntervalPoints extends CFWQueryFunction {

	
	public static final String FIELDNAME_META_INTERVALPOINTS = "cfw-intervalMaxPoints";

	private static final String FUNCTION_NAME = "intervalpoints";

	public CFWQueryFunctionIntervalPoints(CFWQueryContext context) {
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
		tags.add(CFWQueryFunction.TAG_TIME);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(maxPoints)";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Sets the maximum amount of datapoints for the functions interval() and intervalunit(). ";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return   "<ul>"
				+"<li><b>maxPoints:&nbsp;</b> Maximum number of points for interval calculations.</li>"
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
		return false;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void aggregate(EnhancedJsonObject object,ArrayList<QueryPartValue> parameters) {
		// not supported
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public QueryPartValue execute(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters) {
		
		if( !parameters.isEmpty() ) {
			
			QueryPartValue value = parameters.get(0);
			
			if(value.isNumberOrNumberString()) {
				this.context.addMetadata(FIELDNAME_META_INTERVALPOINTS, value.getAsInteger());
			}
		}
		
		return QueryPartValue.newNull();
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	public static int getMaxDatapoints(CFWQueryContext context) {
		
		int maxDatapoints = 150;

		if(context.hasMetadata(FIELDNAME_META_INTERVALPOINTS)) {
			JsonElement element = context.getMetadata(FIELDNAME_META_INTERVALPOINTS);
			maxDatapoints = element.getAsInt();
		}

		return maxDatapoints;
	}
	

}
