package com.xresch.cfw.features.query.functions;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.TreeSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.filemanager.CFWStoredFile;
import com.xresch.cfw.features.filemanager.CFWStoredFile.CFWStoredFileFields;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query._CFWQueryCommon;
import com.xresch.cfw.features.query.parse.QueryPart;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.xrutils.base.XR;
import com.xresch.xrutils.database.XRResultSet;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionFileExcel extends CFWQueryFunction {

	
	public static final String FUNCTION_NAME = "fileExcel";

	public CFWQueryFunctionFileExcel(CFWQueryContext context) {
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
		return FUNCTION_NAME+"(fileID, sheetName, header)";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns the contents of a Excel file stored in the file manager.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<ul>"
			  +"<li><b>fileID:&nbsp;</b>The id of the file, either an integer or object including field 'id'.</li>"
			  +"<li><b>sheetName:&nbsp;</b>(Optional) The name of the Excel sheet that should be read. (default: first sheet in the excel file)</li>"
			  +"<li><b>header:&nbsp;</b>(Optional) Toggle if the first row in the sheet is table a header. (default: true)</li>"
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
		// Get sheetName
		String sheetName =  null;
		if(parameters.size() >= 2) { 
			sheetName = parameters.get(1).getAsString();
		}
	
		//------------------------------------
		// Get header
		boolean header = true;
		if(parameters.size() >= 3) { 
			
			header = parameters.get(2).getAsBoolean();
		}
		
		//------------------------------------
		// Get File from DB
		CFWStoredFile file = CFW.DB.StoredFile.selectByID(fileID);
		
		if(file == null) {
			CFW.Messages.addWarningMessage("function "+FUNCTION_NAME+": The file with ID "+fileID+" could not be found.");
			return QueryPartValue.newNull();
		}
		
		//----------------------------
		// Excel
		XRResultSet cfwResult =CFW.DB.StoredFile.retrieveDataStreamObject(file);
		InputStream dataSream = cfwResult.getBytesStream(CFWStoredFileFields.DATA.toString());
		JsonArray array = CFW.Excel.readExcelSheetAsJsonArray(dataSream, sheetName, header);
		cfwResult.close();
		
		return QueryPartValue.newFromJsonElement( array );
				
	}
}
