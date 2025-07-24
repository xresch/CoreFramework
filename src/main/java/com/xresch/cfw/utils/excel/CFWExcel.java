package com.xresch.cfw.utils.excel;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.CellAddress;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.cfw.logging.CFWLog;

public class CFWExcel {

	public static final Logger logger = CFWLog.getLogger(CFWExcel.class.getName());
	
	
	/********************************************************************************
	 * The fieldnames will either be the letters of the excel columns, or the values
	 * of the first non-empty row if parameter firstIsHeader is set to true.
	 * 
	 * @param filepath the path to the excel file
	 * @param sheetName the name of the sheet, if null, reads the first sheet
	 * @return JsonArray
	 ********************************************************************************/
	public static JsonArray readExcelSheetAsJsonArray(String filepath, String sheetName, boolean firstIsHeader) {
		
		try (InputStream is = new FileInputStream(filepath) ) {
			return readExcelSheetAsJsonArray(is, sheetName, firstIsHeader);
		} catch (Exception e) {
			new CFWLog(logger).severe("Exception while reading Excel file: "+e.getMessage(), e);
		}
		
		return null;
				
	}
	
	
	/********************************************************************************
	 * Reads an excel file and returns the results as a JsonArray.
	 * The fieldnames will either be the letters of the excel columns, or the values
	 * of the first non-empty row if parameter firstIsHeader is set to true.
	 * 
	 * @param filepath the path to the excel file
	 * @param sheetName the name of the sheet, if null, reads the first sheet
	 * @param isFirstHeader toggle if the first row should be used for the fieldnames
	 * @return JsonArray
	 ********************************************************************************/
	public static JsonArray readExcelSheetAsJsonArray(InputStream is, String sheetName, boolean firstIsHeader) {
		
		JsonArray result = new JsonArray();
		boolean isFirstRow = true;
		
		List<Row> rows = readExcelSheetAsRows(is, sheetName);
		
		HashMap<String, String> fieldnameMap = new HashMap<>();
		
		//-------------------------------
		// Iterate all rows
		for(Row row : rows) {
			
			//-------------------------------
			// Skip empty rows
			Optional<Cell> firstNonEmpty = row.getFirstNonEmptyCell();
			if( !row.getFirstNonEmptyCell().isPresent() ) {
				continue;
			}
			
			//-------------------------------
			// Read from First nonEmptyColumn
			int startColumnIndex = firstNonEmpty.get().getColumnIndex();
			
			List<Cell> cells = row.getCells(startColumnIndex, row.getCellCount());
			
			//-------------------------------
			// Read from First nonEmptyColumn
			if(firstIsHeader && isFirstRow) {
				isFirstRow = false;
				fieldnameMap = getNamesFromFirstRow(cells);
				continue;
			}
			//-------------------------------
			// Make Object
			JsonObject rowObject = new JsonObject();
			int columnIndex = 0;
			for(Cell cell : cells) {
				
				//---------------------------
				// handle null Cells (why fastexcel team... WHYYY!?!?!)
				if(cell == null) {
					
					columnIndex++;
					String columnName = CellAddress.convertNumToColString(columnIndex);
					if(fieldnameMap.containsKey(columnName)) {
						columnName = fieldnameMap.get(columnName);
					}
					
					rowObject.add(columnName, JsonNull.INSTANCE);
					continue;
				}
				
				//---------------------------
				// Handle Other Cells
				
				columnIndex = cell.getAddress().getColumn();
				String columnName = CellAddress.convertNumToColString(columnIndex);
				if(fieldnameMap.containsKey(columnName)) {
					columnName = fieldnameMap.get(columnName);
				}
				
				switch(cell.getType()) {
					case STRING: 	rowObject.addProperty(columnName, cell.asString()); 	break;
					case NUMBER: 	rowObject.addProperty(columnName, cell.asNumber()); 	break;
					case BOOLEAN: 	rowObject.addProperty(columnName, cell.asBoolean());	break;
					case EMPTY:   	rowObject.addProperty(columnName, ""); 					break;
					case FORMULA: 	rowObject.addProperty(columnName, cell.getText()); 		break;
					case ERROR: 	rowObject.addProperty(columnName, "#Error"); 			break;
					default:		rowObject.addProperty(columnName, cell.getText());  	break;
				}
			}
			
			//-------------------------------
			// Add Object to Array
			result.add(rowObject);
		}
		
		//-------------------------------
		// Return Array
		return result;
				
	}
	
	/********************************************************************************
	 *
	 ********************************************************************************/
	private static HashMap<String, String> getNamesFromFirstRow(List<Cell> cells) {
		
		HashMap<String, String> fieldnameMap = new HashMap<>();
		HashMap<String, Integer> occurrenceCounter = new HashMap<>();
		int columnIndex = 0;
		
		for(Cell cell : cells) {
			
			//---------------------------
			// Handle null Cells (why fastexcel team... WHYYY!?!?!)
			if(cell == null) {
				continue;
			}
			
			//---------------------------
			// Handle Other Cells
			columnIndex = cell.getAddress().getColumn();
			String oldName = CellAddress.convertNumToColString(columnIndex);
			String newName = cell.getText();
			
			if( ! occurrenceCounter.containsKey(newName) ) {
				fieldnameMap.put(oldName, newName);
				occurrenceCounter.put(newName, 1);
			}else {
				
				int occurrence = occurrenceCounter.get(newName);

				occurrence++;
				
				fieldnameMap.put(oldName, newName+"("+occurrence+")" );
				occurrenceCounter.put(newName, occurrence);
			}
		}

		return fieldnameMap;
	}
	
	/********************************************************************************
	 * Reads an excel file and returns the results as a list of rows.
	 * @param filepath the path to the excel file
	 * @param sheetName the name of the sheet, if null, reads the first sheet
	 * @return list of Rows
	 ********************************************************************************/
	public static  List<Row> readExcelSheetAsRows(String filepath, String sheetName) {
		
		try (InputStream is = new FileInputStream(filepath) ) {
			return readExcelSheetAsRows(is, sheetName);
		} catch (Exception e) {
			new CFWLog(logger).severe("Exception while reading Excel file: "+e.getMessage(), e);
		}
		
		return null;
				
	}
		
	/********************************************************************************
	 * Reads an excel file and returns the results as a list of rows.
	 * @param filepath the path to the excel file
	 * @param sheetName the name of the sheet, if null, reads the first sheet
	 * @return list of Rows, empty list if not found or on error
	 ********************************************************************************/
	public static  List<Row> readExcelSheetAsRows(InputStream inputStream, String sheetName) {
							
		try ( ReadableWorkbook wb = new ReadableWorkbook(inputStream)) {
			
		    Sheet sheet = null;
		    
		    if(sheetName == null) {
		    	sheet = wb.getFirstSheet();
		    }else {
		    	Optional<Sheet> optionalSheet = wb.findSheet(sheetName);
		    	if(optionalSheet.isPresent()) {
		    		sheet = optionalSheet.get();
		    	}else {
		    		 CFW.Messages.addWarningMessage("Excel sheet with name '"+sheet+"' was not found.");
		    		 return new ArrayList<Row>();
		    	}
		  
		    }
		    
		    return sheet.read();
		    
		}catch (Exception e) {
			new CFWLog(logger).severe("Exception while reading Excel sheet: "+e.getMessage(), e);
		}
		
		return new ArrayList<Row>();
	}
}
