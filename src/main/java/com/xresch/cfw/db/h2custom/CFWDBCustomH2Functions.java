package com.xresch.cfw.db.h2custom;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.h2.api.AggregateFunction;
import org.h2.tools.SimpleResultSet;

import com.xresch.cfw._main.CFW;
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
