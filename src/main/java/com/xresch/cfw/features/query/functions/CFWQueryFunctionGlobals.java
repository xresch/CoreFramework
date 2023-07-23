package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.TreeSet;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.QueryPartValue;

public class CFWQueryFunctionGlobals extends CFWQueryFunction {

	
	public CFWQueryFunctionGlobals(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return "globals";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public TreeSet<String> getTags(){
		TreeSet<String> tags = new TreeSet<>();
		tags.add(CFWQueryFunction.TAG_CODING);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return "globals(propertyName [, propertyValue])";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Sets or gets the value for the global property set with the command globals.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 
				"<p><b>propertyName:&nbsp;</b>The name of the property to get or set.</p>"
			  + "<p><b>propertyValue:&nbsp;</b>(Optional)if given, the value will be set.</p>"
			;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_globals.html");
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
		
		//----------------------------------
		// Return empty string if no params
		if(parameters.size() == 0) { return QueryPartValue.newString(""); }
		
		//----------------------------------
		// Get / Set Global Value
		String propertyName = parameters.get(0).getAsString();
		
		if(propertyName != null) { 

			//----------------------------------
			// Set Global Value
			JsonObject globalsObject = this.getContext().getGlobals();
			
			if(parameters.size() > 1) {
				parameters.get(1).addToJsonObject(propertyName, globalsObject);
			}
			
			//----------------------------------
			// Get Global Value
			return QueryPartValue.newFromJsonElement(globalsObject.get(propertyName)); 
		}
		
		return QueryPartValue.newString(""); 
		
		
	}

}
