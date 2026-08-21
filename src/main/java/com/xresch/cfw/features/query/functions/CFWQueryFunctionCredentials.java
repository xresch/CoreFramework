package com.xresch.cfw.features.query.functions;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.TreeSet;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.credentials.CFWCredentials;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query._CFWQueryCommon;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartValue;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionCredentials extends CFWQueryFunction {

	
	public static final String FUNCTION_NAME = "credentials";

	public CFWQueryFunctionCredentials(CFWQueryContext context) {
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
		tags.add(_CFWQueryCommon.TAG_GENERAL);
		return tags;
	}
	
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(credentialsID)";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns an object containing the values of the selected credentials.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return 
			"<ul>"
				+"<li><b>credentialsID:&nbsp;</b>The id, name or object {&quot;id&quot;: 123, ...} to select the credentials.</li>"
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
	
	
	/*************************************************************************
	 * Override this method to validate if only QueryParts of a certain type
	 * are passed to this method.
	 * For example only allowing literal string values.
	 * This method is responsible to throw a ParseException in case something
	 * is not right.
	 *  
	 * @param partsArray the queryParts passed to this function
	 * @param doCheckPermissions toggle if permissions should be checked if
	 * the user has the required permissions to execute the function with the 
	 * given parameters.
	 *************************************************************************/
	@Override
	public boolean validateQueryParts(ArrayList<QueryPart> partsArray, boolean doCheckPermissions) throws ParseException {
		
		//-------------------------------------------------
		// Check Params are a static value and not dynamic
		for(QueryPart current : partsArray) {
			if( !(current instanceof QueryPartValue) ) {
				throw new ParseException("function credentials(): For security reasons, parameter must be a static value and cannot be dynamic.", current.position());
			}
		}
		
		//-------------------------------------------------
		// Check Params are a static value and not dynamic
		if(doCheckPermissions) {
			
			if(partsArray.size() > 0){
				QueryPart first = partsArray.get(0);
				QueryPartValue credsValue = first.determineValue(null);
				String name = credsValue.getAsString();
				
				//----------------------------------
				// Check Exists
				CFWCredentials credentials = fetchCredentialsFromDB(credsValue);
				
				if(credentials == null) {
					 credentials = new CFWCredentials();
					 this.getContext().addMessageWarning("Credentials '" + credsValue.getAsString() + "' could not be found.");
				}
				
				if( !CFW.DB.Credentials.hasUserAccessToCredentials(credentials.id()) ){
					throw new ParseException("function credentials(): You do not have permission to use the credentials '"+name+"'.", first.position());
				}
				
				
	
			}
		}
		
		return true;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public boolean receiveStringParamsLiteral() {
		return true;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		CFW.DB.Credentials.autocompleteCredentialsForQuery(result, helper, null);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public QueryPartValue execute(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters, ArrayList<QueryPart> unevalParams) {
		
		//-----------------------------------
		// Check Has Params
		if(parameters.size() == 0) {
			return QueryPartValue.newNull();
		}
		
		//-----------------------------------
		// Get Credentials 
		QueryPartValue credsValue = parameters.get(0);
		
		if(credsValue.isNullOrEmptyString()) {
			 return QueryPartValue.newNull();
		}
		
		CFWCredentials credentials = fetchCredentialsFromDB(credsValue);

		//-----------------------------------
		// Fetch & Create Result
		
		if(credentials == null) {
			 credentials = new CFWCredentials();
			 this.getContext().addMessageWarning("Credentials '" + credsValue.getAsString() + "' could not be found.");
		}
		
		return QueryPartValue.newFromJsonElement(
					credentials.createJsonObject()
				);

	}

	private CFWCredentials fetchCredentialsFromDB(QueryPartValue credsValue) {
		CFWCredentials credentials = null;
		
		System.out.println(credsValue.type());
		System.out.println(credsValue);
		if(credsValue.isString()) {
			String credsName = credsValue.getAsString();
			credentials = CFW.DB.Credentials.selectFirstByName(credsName);
		}else if(credsValue.isInteger()) {
			Integer credsID = credsValue.getAsInteger();
			credentials = CFW.DB.Credentials.selectByID(credsID);
		}else if(credsValue.isJsonObject()) {
			JsonObject credsObject = credsValue.getAsJsonObject();
			if(credsObject.has("id")) {
				Integer credsID = credsObject.get("id").getAsInt();
				credentials = CFW.DB.Credentials.selectByID(credsID);
			}
		}
		return credentials;
	}

}
