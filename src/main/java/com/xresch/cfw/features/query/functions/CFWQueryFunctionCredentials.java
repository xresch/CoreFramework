package com.xresch.cfw.features.query.functions;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.TreeSet;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.credentials.CFWCredentials;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
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
		tags.add(CFWQueryFunction.TAG_GENERAL);
		return tags;
	}
	
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(credentialsName)";
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
				+"<li><b>credentialsName:&nbsp;</b>The name of the credentials.</li>"
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
	 *************************************************************************/
	public boolean validateQueryParts(ArrayList<QueryPart> partsArray) throws ParseException {
		
		for(QueryPart current : partsArray) {
			if( !(current instanceof QueryPartValue) ) {
				throw new ParseException("function credentials(): For security reasons, parameter must be a literal(hardcoded) string.", current.position());
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
	public QueryPartValue execute(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters) {
		
		//-----------------------------------
		// Check Has Params
		if(parameters.size() == 0) {
			return QueryPartValue.newNull();
		}
		
		//-----------------------------------
		// Get Credentials Name
		QueryPartValue literalCredentialsName = parameters.get(0);
		String name = literalCredentialsName.getAsString();
		
		if(Strings.isNullOrEmpty(name)) {
			 return QueryPartValue.newNull();
		}
		
		//-----------------------------------
		// Fetch & Create Result
		CFWCredentials credentials = CFW.DB.Credentials.selectFirstByName(name);
		
		
		if(credentials == null) {
			 credentials = new CFWCredentials();
			 this.getContext().addMessageWarning("Credentials with name '"+name+"' could not be found.");
		}
		
		return QueryPartValue.newFromJsonElement(
					credentials.createJsonObject()
				);

	}

}
