package com.pengtoolbox.cfw.utils.json;

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
import com.pengtoolbox.cfw._main.CFW;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
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
					
					if(name.toUpperCase().startsWith("JSON")) {
						JsonElement asElement = CFW.JSON.jsonStringToJsonElement(resultSet.getString(i));
						row.add(name, asElement);
					}else {
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
		}
				
		return result;
	}

}
