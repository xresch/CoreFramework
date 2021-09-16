package com.xresch.cfw.utils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDB;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.logging.CFWLog;

public class ResultSetUtils {
	
	private static Logger logger = CFWLog.getLogger(ResultSetUtils.class.getName());
	
	/***************************************************************************
	 * Converts the first result into a CFWObject.
	 * @return object, returns null if result set is empty or an error occurs.
	 ***************************************************************************/
	public static CFWObject getFirstAsObject(ResultSet result, Class<? extends CFWObject> clazz) {
		
		try {
			if(result.next()) {
				CFWObject instance = clazz.newInstance();
				instance.mapResultSet(result);
				return instance;
				
			}
		}catch (SQLException | InstantiationException | IllegalAccessException e) {
			new CFWLog(logger)
			.severe("Error reading object from database.", e);
			
		}finally {
			CFWDB.close(result);
		}
		
		return null;
		
	}
	
	/***************************************************************************
	 * Converts a ResultSet into an array list of CFWObjects.
	 * @return list of object, empty if results set is null or an error occurs.
	 ***************************************************************************/
	public static ArrayList<CFWObject> toObjectList(ResultSet result, Class<? extends CFWObject> clazz) {
		
		ArrayList<CFWObject> objectArray = new ArrayList<>();
		
		if(result == null) {
			return objectArray;
		}
		
		try {
			while(result.next()) {
				CFWObject current = clazz.newInstance();
				current.mapResultSet(result);
				objectArray.add(current);
			}
		} catch (SQLException | InstantiationException | IllegalAccessException e) {
			new CFWLog(logger)
				.severe("Error reading object from database.", e);
			
		}finally {
			CFWDB.close(result);
		}
		
		
		
		return objectArray;
		
	}
	
	/***************************************************************************
	 * Converts a ResultSet into an array list of CFWObjects for a specified Type.
	 * @param <T>
	 * @return list of object, empty if results set is null or an error occurs.
	 ***************************************************************************/
	public static <T extends CFWObject> ArrayList<T> toObjectListConvert(ResultSet result, Class<T> clazz) {
		
		ArrayList<T> objectArray = new ArrayList<>();
		
		if(result == null) {
			return objectArray;
		}
		
		try {
			while(result.next()) {
				CFWObject current = clazz.newInstance();
				current.mapResultSet(result);
				objectArray.add((T)current);
			}
		} catch (SQLException | InstantiationException | IllegalAccessException e) {
			new CFWLog(logger)
				.severe("Error reading object from database.", e);
			
		}finally {
			CFWDB.close(result);
		}
		
		
		
		return objectArray;
		
	}
	
	/***************************************************************************
	 * Converts a ResultSet into a map of Primary Keys and CFWObjects.
	 * @return list of object, empty if results set is null or an error occurs.
	 ***************************************************************************/
	public static LinkedHashMap<Integer, CFWObject> toKeyObjectMap(ResultSet result, Class<? extends CFWObject> clazz) {
		LinkedHashMap<Integer, CFWObject> objectMap = new LinkedHashMap<>();
		
		if(result == null) {
			return objectMap;
		}
		
		try {
			while(result.next()) {
				CFWObject current = clazz.newInstance();
				current.mapResultSet(result);
				objectMap.put(current.getPrimaryKey(), current);
			}
		} catch (SQLException | InstantiationException | IllegalAccessException e) {
			new CFWLog(logger)
				.severe("Error reading objects from database.", e);
			
		}finally {
			CFWDB.close(result);
		}
					
		return objectMap;
	}
	
	/***************************************************************************
	 * Converts a ResultSet into a map with the key/values of the selected columns.
	 * @return list of object, empty if results set is null or an error occurs.
	 ***************************************************************************/
	public static HashMap<Object, Object> toKeyValueMap(ResultSet result, String keyColumnName, String valueColumnName) {
		
		HashMap<Object, Object> keyValueMap = new HashMap<Object, Object>();
		
		if(result == null) {
			return keyValueMap;
		}
		
		try {
			while(result.next()) {
				Object key = result.getObject(keyColumnName);
				Object value = result.getObject(valueColumnName);
				keyValueMap.put(key, value);
			}
		} catch (SQLException e) {
			new CFWLog(logger)
				.severe("Error reading object from database.", e);
			
		}finally {
			CFWDB.close(result);
		}
			
		return keyValueMap;
	}
	
	/***************************************************************************
	 * Converts a ResultSet into a list of maps with key/values.
	 * @return list of maps holding key(column name) with values
	 ***************************************************************************/
	public static ArrayList<LinkedHashMap<String, Object>> toListOfKeyValueMaps(ResultSet result) {
		
		ArrayList<LinkedHashMap<String, Object>> resultList =  new ArrayList<>();
		
		if(result == null) {
			return resultList;
		}
		
		try {
			ResultSetMetaData meta = result.getMetaData();
			int columnCount = meta.getColumnCount();
			
			while(result.next()) {
				LinkedHashMap<String, Object> keyValueMap = new LinkedHashMap<>();
				
				for(int i = 1; i <= columnCount; i++) {
					String key = meta.getColumnLabel(i);
					Object value = result.getObject(key);
					keyValueMap.put(key, value);
				}
				resultList.add(keyValueMap);
				
			}
		} catch (SQLException e) {
			new CFWLog(logger)
			.severe("Error reading object from database.", e);
			
		}finally {
			CFWDB.close(result);
		}
		
		return resultList;
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as a string array.
	 ***************************************************************/
	public static String[] toStringArray(ResultSet result, String columnName) {
		return toStringArrayList(result, columnName).toArray(new String[] {});
	}
	
	/***************************************************************************
	 * Converts a ResultSet into a map with the key/values of the selected columns.
	 * @return list of object, empty if results set is null or an error occurs.
	 ***************************************************************************/
	public static ArrayList<String> toStringArrayList(ResultSet result, String columnName) {
		
		ArrayList<String> stringArray = new ArrayList<String>();
		
		if(result == null) {
			return stringArray;
		}
		
		try {
			while(result.next()) {
				Object value = result.getObject(columnName.toString());
				stringArray.add(value.toString());
			}
		} catch (SQLException e) {
			new CFWLog(logger)
			.severe("Error reading object from database.", e);
			
		}finally {
			CFWDB.close(result);
		}
			
		return stringArray;
	}
	
	
	/***************************************************************************
	 * Converts a ResultSet into a map with the key/values of the selected columns.
	 * @return list of object, empty if results set is null or an error occurs.
	 ***************************************************************************/
	public static ArrayList<Integer> toIntegerArrayList(ResultSet result, String columnName) {
		
		ArrayList<Integer> stringArray = new ArrayList<Integer>();
		
		if(result == null) {
			return stringArray;
		}
		
		try {
			while(result.next()) {
				int value = result.getInt(columnName.toString());
				stringArray.add(value);
			}
		} catch (SQLException e) {
			new CFWLog(logger)
			.severe("Error reading object from database.", e);
			
		}finally {
			CFWDB.close(result);
		}
			
		return stringArray;
	}
	
	/***************************************************************************
	 * Converts a ResultSet into a map with the key/values of the selected columns.
	 * @return list of object, empty if results set is null or an error occurs.
	 ***************************************************************************/
	public static LinkedHashMap<Object, Object> toLinkedHashMap(ResultSet result, Object keyColumnName, Object valueColumnName) {
		
		LinkedHashMap<Object, Object>  resultMap = new LinkedHashMap<Object, Object>();
		
		if(result == null) {
			return resultMap;
		}
		
		try {
			while(result.next()) {
				Object key = result.getObject(keyColumnName.toString());
				Object value = result.getObject(valueColumnName.toString());
				resultMap.put(key, value);
			}
		} catch (SQLException e) {
			new CFWLog(logger)
			.severe("Error reading object from database.", e);
			
		}finally {
			CFWDB.close(result);
		}
					
		return resultMap;
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as an AutocompleteResult
	 * with value and label.
	 ***************************************************************/
	public static AutocompleteResult toAsAutocompleteResult(ResultSet result, Object valueColumnName, Object labelColumnName) {
		
		AutocompleteList list = new AutocompleteList();

		if(result == null) {
			return new AutocompleteResult();
		}
		
		try {
			while(result.next()) {
				Object key = result.getObject(valueColumnName.toString());
				Object value = result.getObject(labelColumnName.toString());
				list.addItem(key, value);
			}
		} catch (SQLException e) {
			new CFWLog(logger)
			.severe("Error reading object from database.", e);
			
		}finally {
			CFWDB.close(result);
		}
			
		return new AutocompleteResult(list);
		
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as an AutocompleteResult
	 * with value and label.
	 ***************************************************************/
	public static AutocompleteResult toAsAutocompleteResult(ResultSet result, Object valueColumnName, Object labelColumnName, Object descriptionColumnName) {
		
		AutocompleteList list = new AutocompleteList();

		if(result == null) {
			return new AutocompleteResult();
		}
		
		try {
			while(result.next()) {
				Object key = result.getObject(valueColumnName.toString());
				Object value = result.getObject(labelColumnName.toString());
				Object description = result.getObject(descriptionColumnName.toString());
				list.addItem(key, value, description);
			}
		} catch (SQLException e) {
			new CFWLog(logger)
			.severe("Error reading object from database.", e);
			
		}finally {
			CFWDB.close(result);
		}
			
		return new AutocompleteResult(list);
		
	}

	/********************************************************************************************
	 * Returns a jsonString with an array containing a json object for each row.
	 * Returns an empty array in case of error.
	 * 
	 ********************************************************************************************/
	public static String toJSON(ResultSet resultSet) {
		return CFW.JSON.toJSON(resultSet);
	}
	
	/***************************************************************************
	 * Converts a ResultSet into a JsonArray.
	 * @return list of maps holding key(column name) with values
	 ***************************************************************************/
	public static JsonArray toJSONArray(ResultSet result) {
		
		JsonArray resultArray =  new JsonArray();
		
		if(result == null) {
			return resultArray;
		}
		
		try {
			ResultSetMetaData meta = result.getMetaData();
			int columnCount = meta.getColumnCount();
			
			while(result.next()) {
				JsonObject currentObject = new JsonObject();
				
				for(int i = 1; i <= columnCount; i++) {
					String key = meta.getColumnLabel(i);
					Object value = result.getObject(key);
					if(value instanceof String) { currentObject.addProperty(key, (String)value); }
					else if(value instanceof Boolean) { currentObject.addProperty(key, (Boolean)value); }
					else if(value instanceof Number) { currentObject.addProperty(key, (Number)value); }
					else if(value instanceof Character) { currentObject.addProperty(key, (Character)value); }
					else if(value  == null ) { currentObject.add(key, null); }
					else 								{ currentObject.addProperty(key, value.toString()); }
				}
				resultArray.add(currentObject);
				
			}
		} catch (SQLException e) {
			new CFWLog(logger)
			.severe("Error reading object from database.", e);
			
		}finally {
			CFWDB.close(result);
		}
		
		return resultArray;
	}
	
	

	/********************************************************************************************
	 * Converts the ResultSet into a CSV string.
	 * 
	 ********************************************************************************************/
	public static String toCSV(ResultSet resultSet, String delimiter) {
		StringBuilder csv = new StringBuilder();
		
		try {
			
			if(resultSet == null) {
				return "";
			}
			
			//--------------------------------------
			// Check has results
			/* Excluded as MSSQL might throw errors			
			resultSet.beforeFirst();
			if(!resultSet.isBeforeFirst()) {
				return "";
			} */
			
			//--------------------------------------
			// Iterate results
			ResultSetMetaData metadata = resultSet.getMetaData();
			int columnCount = metadata.getColumnCount();
			
			for(int i = 1 ; i <= columnCount; i++) {
				csv.append("\"")
				   .append(metadata.getColumnLabel(i))
				   .append("\"")
				   .append(delimiter);
			}
			csv.deleteCharAt(csv.length()-1); //remove last comma
			csv.append("\r\n");
			while(resultSet.next()) {
				for(int i = 1 ; i <= columnCount; i++) {
					
					String value = resultSet.getString(i);
					csv.append("\"")
					   .append(CFW.JSON.escapeString(value))
					   .append("\"")
					   .append(delimiter);
				}
				csv.deleteCharAt(csv.length()-1); //remove last comma
				csv.append("\r\n");
			}
			csv.deleteCharAt(csv.length()-1); //remove last comma
	
			
		} catch (SQLException e) {
				new CFWLog(logger)
					.severe("Exception occured while converting ResultSet to CSV.", e);
				
				return "";
		}
	
		return csv.toString();
	}

	/********************************************************************************************
	 * Returns an XML string with an array containing a record for each row.
	 * 
	 ********************************************************************************************/
	public static String toXML(ResultSet resultSet) {
		StringBuilder json = new StringBuilder();
		
		try {
			
			if(resultSet == null) {
				return "<data></data>";
			}
			//--------------------------------------
			// Check has results
			/* Excluded as MSSQL might throw errors			
			resultSet.beforeFirst();
			if(!resultSet.isBeforeFirst()) {
				return "<data></data>";			
			}*/
			
			//--------------------------------------
			// Iterate results
			ResultSetMetaData metadata = resultSet.getMetaData();
			int columnCount = metadata.getColumnCount();
	
			json.append("<data>\n");
			while(resultSet.next()) {
				json.append("\t<record>\n");
				for(int i = 1 ; i <= columnCount; i++) {
					String column = metadata.getColumnLabel(i);
					json.append("\t\t<").append(column).append(">");
					
					String value = resultSet.getString(i);
					json.append(value);
					json.append("</").append(column).append(">\n");
				}
				json.append("\t</record>\n");
			}
			json.append("</data>");
			
		} catch (SQLException e) {
				new CFWLog(logger)
					.severe("Exception occured while converting ResultSet to XML.", e);
				
				return "<data></data>";
		}
	
		return json.toString();
	}
	
		
}
