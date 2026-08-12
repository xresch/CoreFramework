package com.xresch.cfw.features.query.functions;

import java.util.ArrayList;
import java.util.TreeSet;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.filemanager.CFWStoredFile;
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
public class CFWQueryFunctionFileJson extends CFWQueryFunction {

	
	public static final String FUNCTION_NAME = "fileJson";

	public CFWQueryFunctionFileJson(CFWQueryContext context) {
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
		tags.add(_CFWQueryCommon.TAG_CODING);
		tags.add(_CFWQueryCommon.TAG_FILE);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return FUNCTION_NAME+"(fileID)";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns the contents of a JSON file stored in the file manager.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<ul>"
			  +"<li><b>fileID:&nbsp;</b>The id of the file, either an integer or object including field 'id'.</li>"
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
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		CFW.DB.StoredFile.autocompleteFileForQuery(result, helper, null);
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public QueryPartValue execute(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters, ArrayList<QueryPart> unevalParams) {
		
		//----------------------------------
		// Return null if there is no parameter
		if(parameters.size() == 0) { 
			return QueryPartValue.newNull();
		}
		
		//------------------------------------
		// Get File ID
		
		Integer fileID = _CFWQueryCommon.getIDFromValue(parameters.get(0));
		
		if(fileID == null) {
			CFW.Messages.addWarningMessage("function "+FUNCTION_NAME+": The ID "+fileID+" could not be resolved to an integer.");
			return QueryPartValue.newNull();
		}
		
		//------------------------------------
		// Get File from DB
		CFWStoredFile file = CFW.DB.StoredFile.selectByID(fileID);
		
		if(file == null) {
			CFW.Messages.addWarningMessage("function "+FUNCTION_NAME+": The file with ID "+fileID+" could not be found.");
			return QueryPartValue.newNull();
		}
		
		//------------------------------------
		// Read as JSON
		String dataString = CFW.DB.StoredFile.retrieveDataAsString(file);
		
		return QueryPartValue.newFromJsonElement( CFW.JSON.fromJson(dataString) );
				
	}
}
