package com.xresch.cfw.utils.json;

import java.lang.reflect.Type;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.CFWDB;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class SerializerResultSet implements JsonSerializer<ResultSet> {

	@Override
	public JsonElement serialize(ResultSet resultSet, Type type, JsonSerializationContext context) {
		
		JsonArray result = new JsonArray();
		ResultSetMetaData metadata;
		try {
			metadata = resultSet.getMetaData();
			int columnCount = metadata.getColumnCount();
			
			while(resultSet.next()) {
				JsonObject row = new JsonObject();
				for(int i = 1 ; i <= columnCount; i++) {
					String name = metadata.getColumnLabel(i);
					System.out.println("===== JSON =====");
					System.out.println("name: "+name);
					System.out.println("Value: "+resultSet.getString(i));
					
					if(name.toUpperCase().startsWith("JSON")) {
						System.out.println("A");
						JsonElement asElement = CFW.JSON.jsonStringToJsonElement(resultSet.getString(i));
						row.add(name, asElement);
					}else {
						System.out.println("B");
						Object value = resultSet.getObject(i);
						if(!(value instanceof Clob)) {
							CFW.JSON.addObject(row, name, value);
						}else {
							CFW.JSON.addObject(row, name, resultSet.getString(i));
						}
					}
				}
				result.add(row);
			}
		} catch (SQLException e) {
			
			e.printStackTrace();
		}finally {
			CFWDB.close(resultSet);
		}
		return result;
	}

}
