package com.xresch.cfw.utils.json;

import java.lang.reflect.Type;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.h2.jdbc.JdbcArray;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.utils.ResultSetUtils;
import com.xresch.cfw.utils.ResultSetUtils.ResultSetAsJsonReader;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class SerializerResultSet implements JsonSerializer<ResultSet> {
	

	@Override
	public JsonElement serialize(ResultSet resultSet, Type type, JsonSerializationContext context) {
		
		JsonArray result = new JsonArray();
		
		ResultSetAsJsonReader reader = ResultSetUtils.toJSONReader(resultSet);
		
		JsonObject object;
		while( (object = reader.next()) != null) {
			result.add(object);
		}

		return result;
	}

}
