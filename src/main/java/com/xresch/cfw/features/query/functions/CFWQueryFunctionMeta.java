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

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionMeta extends CFWQueryFunction {

	
	public CFWQueryFunctionMeta(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return "meta";
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
		return "meta(propertyName [, propertyValue])";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Sets or gets the value for the metadata property set with the command metadata.";
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
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "function_meta.html");
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
		// Get / Set Metadata Value
		String propertyName = parameters.get(0).getAsString();
		
		if(propertyName != null) { 

			//----------------------------------
			// Set Metadata Value
			JsonObject metadataObject = this.getContext().getMetadata();
			
			if(parameters.size() > 1) {
				parameters.get(1).addToJsonObject(propertyName, metadataObject);
			}
			
			//----------------------------------
			// Get Metadata Value
			return QueryPartValue.newFromJsonElement(metadataObject.get(propertyName)); 
		}
		
		return QueryPartValue.newString(""); 
		
		
	}

}
