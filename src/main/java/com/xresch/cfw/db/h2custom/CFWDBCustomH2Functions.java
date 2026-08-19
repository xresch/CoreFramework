package com.xresch.cfw.db.h2custom;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.h2.tools.SimpleResultSet;

import com.xresch.cfw.db.DBInterface;

public class CFWDBCustomH2Functions {
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static void initialize(DBInterface db) {
		
		registerRegularFunctions(db);
		registerAggregateFunctions(db);
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	private static void registerRegularFunctions(DBInterface db) {
		String clazz = CFWDBCustomH2Functions.class.getName();
		
		String[] functionNames = new String[] {
				  "COUNT_ROWS"
				, "CFW_ARRAY_DISTINCT"
				, "CFW_ARRAY_CONTAINS_ANY_INT"
				, "CFW_ARRAY_CONTAINS_ALL_INT"
				, "CFW_BIGDEC_DIVIDE"
			};
		
		for(String name : functionNames ) {
			db.preparedExecuteBatch("DROP ALIAS IF EXISTS "+name+"; CREATE ALIAS "+name+" for \""+clazz+"."+name+"\""); 
		}
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	private static void registerAggregateFunctions(DBInterface db) {

		// Function name & Class Name
		LinkedHashMap<String, String > classMap = new LinkedHashMap<>(); 
		classMap.put(CFW_ARRAY_MERGE.class.getSimpleName(), CFW_ARRAY_MERGE.class.getName() );
		
		for(Entry<String, String> entry : classMap.entrySet() ) {
			db.preparedExecuteBatch("DROP AGGREGATE IF EXISTS "+entry.getKey()+"; CREATE AGGREGATE "+entry.getKey()+" FOR \""+entry.getValue()+"\" ");
			//db.preparedExecuteBatch("DROP AGGREGATE IF EXISTS "+entry.getKey()+"; CREATE AGGREGATE "+entry.getKey()+" FOR \""+entry.getValue()+"\" ");
		}
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static long COUNT_ROWS(Connection conn, String tableName) throws SQLException {
		ResultSet rs = conn.createStatement().
		    executeQuery("select count(*) from " + tableName);
		rs.next();
		return rs.getLong(1); 
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static BigDecimal CFW_BIGDEC_DIVIDE(Connection conn, BigDecimal dividend, BigDecimal divisor) throws SQLException {
		System.out.println("===================");
		System.out.println("dividend-float: "+dividend.floatValue());
		System.out.println("dividend-scale: "+dividend.scale());
		System.out.println("dividend-precision: "+dividend.scale());
		System.out.println("result: " + dividend.divide(divisor, RoundingMode.HALF_UP));
		
		return dividend.divide(divisor, RoundingMode.HALF_UP); 
	}
	
	/************************************************************************
	 * Checks if the Array first array contains any of the integers 
	 * contained in the seconds array.
	 * Useful to check if a Hierarchy item has at least one similar parent.
	 ************************************************************************/
	public static boolean CFW_ARRAY_CONTAINS_ANY_INT(Connection conn, Array arrayToSearchIn, Array arrayWithValues) throws SQLException {
		
		
		ResultSet resultToSearch = arrayToSearchIn.getResultSet();
		ResultSet resultWithValues = arrayWithValues.getResultSet();
		
		while(resultToSearch.next()) {
			
			Integer currentInt = resultToSearch.getInt("VALUE");
			resultWithValues.beforeFirst();
			while(resultWithValues.next()) {
				Integer currentValue = resultWithValues.getInt("VALUE");
				//---------------------------
				// Handle Null Values
				if(currentInt == null || currentValue == null ) {
					if(currentInt == currentValue) {
						return true;
					}else {
						continue;
					}
				}
				
				//---------------------------
				// Handle Int Values
				if(currentInt.intValue() == currentValue.intValue()) {
					return true;
				}
			}
		}
		
		return false; 
	}
	
	/************************************************************************
	 * Checks if the Array second array matches all of the integers 
	 * contained in the first array.
	 * This is a partial match, not an equals match. 
	 * Useful to check if a Hierarchy item is in the same parental line.
	 * </br></br>
	 * For Example:
	 * </br>- arrayToMatch    = [1,2,3,4]
	 * </br>- arrayWithValues = [1,2,3]
	 * </br>- Result: true, as it matches fully or partially
	 * </br></br>
	 * For Example:
	 * </br>- arrayToMatch    = [1,2,3]
	 * </br>- arrayWithValues = [1,2,3,4]
	 * </br>- Result: false, as it has additional values
	 * 
	 * @param arrayToMatch the potentially bigger array
	 * @param arrayWithValues the array <= in size of arrayToMatch
	 * 
	 * @return true if partial match, false if not or if arrayWithValues is empty
	 ************************************************************************/
	public static boolean CFW_ARRAY_CONTAINS_ALL_INT(Connection conn, Array arrayToMatch, Array arrayWithValues) throws SQLException {
		
		//----------------------------
		// Variables
		ResultSet resultToMatch = arrayToMatch.getResultSet();
		ResultSet resultWithValues = arrayWithValues.getResultSet();
		
		//----------------------------
		// Check Empty
		if( ! resultWithValues.isBeforeFirst() ) {
			return false;
		}
		
		//----------------------------
		// Check Match
		boolean isPartialMatch = true;

		while(resultWithValues.next()) {
			
			Integer currentInt = resultWithValues.getInt("VALUE");
			resultToMatch.beforeFirst();
			boolean found = false;
			while(resultToMatch.next()) {
				Integer currentValue = resultToMatch.getInt("VALUE");
				//---------------------------
				// Handle Null Values
				if(currentInt == null || currentValue == null ) {
					if(currentInt == currentValue) {
						found = true;
						break;
					}else {
						continue;
					}
				}
				
				//---------------------------
				// Handle Int Values
				if(currentInt.intValue() == currentValue.intValue()) {
					found = true;
					break;
				}
			}
			
			isPartialMatch &= found;
		}
		
		return isPartialMatch; 
	}
	
	/************************************************************************
	 * Extracts distinct values from all arrays in an array column.
	 * Sorts by natural order.
	 ************************************************************************/
	public static SimpleResultSet CFW_ARRAY_DISTINCT(Connection conn, String tableName, String columnName) throws SQLException {
		
		//--------------------------
		// Fetch Distinct Arrays
		PreparedStatement prepared = conn.prepareStatement("SELECT DISTINCT "+columnName+" FROM "+tableName);

		ResultSet resultSet = prepared.executeQuery();
		
		//--------------------------
		// Create Distinct List of Values

	    TreeSet<String> distinctValues = new TreeSet<String>();
	    
		while(resultSet.next()) {
			
			Array tagsArray = resultSet.getArray(1);

			if(tagsArray != null) {
				Object[] objectArray = (Object[])tagsArray.getArray();
				for(int i = 0 ; i < objectArray.length; i++) {
					String objectString = objectArray[i] != null ? objectArray[i].toString() : null;
					distinctValues.add(objectString);
				}
			}
		}
		
		//--------------------------
		// Create Result
		SimpleResultSet rs = new SimpleResultSet();
		rs.addColumn(columnName, Types.VARCHAR, 1000000000, 0);
		
		for(String value : distinctValues) {
			rs.addRow(value);
		}

		return rs;
	}
	
}
