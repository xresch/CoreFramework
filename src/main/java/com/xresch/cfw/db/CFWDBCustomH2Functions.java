package com.xresch.cfw.db;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.TreeSet;

import org.h2.tools.SimpleResultSet;

public class CFWDBCustomH2Functions {
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static void initialize(DBInterface db) {
		
		String clazz = CFWDBCustomH2Functions.class.getName();
		
		String[] functionNames = new String[] {
				  "COUNT_ROWS"
				, "CFW_ARRAY_DISTINCT"
			};
		
		for(String name : functionNames ) {
			db.preparedExecuteBatch("DROP ALIAS IF EXISTS "+name+"; CREATE ALIAS "+name+" for \""+clazz+"."+name+"\""); 
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
