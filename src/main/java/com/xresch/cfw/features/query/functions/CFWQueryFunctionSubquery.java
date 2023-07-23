package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.TreeSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryExecutor;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.CFWQueryResultList;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.QueryPartValue;

public class CFWQueryFunctionSubquery extends CFWQueryFunction {

	public static final String FUNCTION_NAME = "subquery";

	public CFWQueryFunctionSubquery(CFWQueryContext context) {
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
		tags.add(CFWQueryFunction.TAG_CODING);
		return tags;
	}
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(queryString, doUnpack, doSimplify)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Executes a query and returns it's result.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 
				"<ul>"
					+"<li><b>queryString:&nbsp;</b>The query to execute.</li>"
					+"<li><b>doUnpack:&nbsp;</b>(Optional)If true and only one result is returned, unpack the result out of the array.(Default:false)</li>"
					+"<li><b>doSimplify:&nbsp;</b>(Optional)If true and only one result is returned, and the result only contains a single value, return only the value.(Default:false)</li>"
				+ "</ul>"
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
		
		//----------------------------------
		// Check has Parameter
		if(parameters.size() == 0) { 
			return QueryPartValue.newNull();
		}
			
		//----------------------------------
		// Get Query String
		QueryPartValue queryStringValue = parameters.get(0);
		if(!queryStringValue.isString()) {
			return QueryPartValue.newNull();
		}
		
		//----------------------------------
		// Get doUnpack
		boolean doUnpack = false;
		if(parameters.size() >= 2) { 
			QueryPartValue simplifyValue = parameters.get(1);
			if(simplifyValue.isBoolOrBoolString()) {
				doUnpack = simplifyValue.getAsBoolean();
			}
		}
		
		//----------------------------------
		// Get simplify
		boolean doSimplify = false;
		if(parameters.size() >= 3) { 
			QueryPartValue simplifyValue = parameters.get(2);
			if(simplifyValue.isBoolOrBoolString()) {
				doSimplify = simplifyValue.getAsBoolean();
			}
		}
		
		//----------------------------------
		// Execute Subquery
		String queryString = queryStringValue.getAsString();
		CFWQueryExecutor executor = new CFWQueryExecutor();

		CFWQueryContext subqueryContext = this.getContext().createClone(false);

		CFWQueryResultList resultList = executor.parseAndExecuteAll(subqueryContext, queryString, null, null);
		
		//----------------------------------
		// Convert Results
		JsonArray allResults = new JsonArray();
		for(int i = 0; i < resultList.size(); i++) {
			allResults.addAll(resultList.get(i).getResults());
		}
		
		if(allResults.size() == 0) {
			return QueryPartValue.newNull();
		}
		
		if(allResults.size() == 1 && (doUnpack | doSimplify) ) {
			JsonElement element = allResults.get(0);
			if(element.isJsonObject()) {
				JsonObject singleObject = element.getAsJsonObject();
				if(doSimplify && singleObject.size() == 1) {
					JsonElement value = singleObject.entrySet().iterator().next().getValue();
					return QueryPartValue.newFromJsonElement(value);
				}else if (doUnpack) {
					return QueryPartValue.newFromJsonElement(singleObject);
				}
			}
			
		}
		
		
		return QueryPartValue.newFromJsonElement(allResults);
	
	}

}
