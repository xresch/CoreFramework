package com.pengtoolbox.cfw.datahandling;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.db.CFWSQL;
import com.pengtoolbox.cfw.features.api.APIDefinition;
import com.pengtoolbox.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWObject {
	
	private static Logger logger = CFWLog.getLogger(CFWObject.class.getName());
	
	//------------------------
	// General
	private LinkedHashMap<String, CFWField<?>> fields = new LinkedHashMap<String, CFWField<?>>();
	protected int hierarchyLevels = 0;
	protected  CFWObject parent;
	protected  LinkedHashMap<Integer, CFWObject> childObjects = new LinkedHashMap<Integer, CFWObject>();
	
	//---------------------------
	// Database
	protected String tableName; 
	protected CFWField<Integer> primaryField = null;
	private ArrayList<ForeignKeyDefinition> foreignKeys = new ArrayList<ForeignKeyDefinition>();
	

	
	public class ForeignKeyDefinition{
		public String fieldname;
		public String foreignFieldname;
		public Class<? extends CFWObject> foreignObject;
		public String ondelete;
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWObject() {

	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public boolean mapRequestParameters(HttpServletRequest request) {
		
		return CFWField.mapAndValidateParamsToFields(request, fields);
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public boolean mapJsonFields(String json) {
		return mapJsonFields(CFW.JSON.fromJson(json));
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public boolean mapJsonFields(JsonObject object) {
		return CFWField.mapAndValidateJsonToFields(object, fields);
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public boolean mapResultSet(ResultSet result) {
		return CFWField.mapResultSetColumnsToFields(result, fields);
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWForm toForm(String formID, String submitLabel) {
		
		CFWForm form = new CFWForm(formID, submitLabel);
		form.setOrigin(this);
		
		for(CFWField<?> field : fields.values()) {
			form.addField(field);
		}
		
		return form;
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWForm toForm(String formID, String submitLabel, String ...fieldNames) {
		
		CFWForm form = new CFWForm(formID, submitLabel);
		form.setOrigin(this);
		
		for(String fieldName : fieldNames) {
			if(fields.containsKey(fieldName)) {
				form.addField(fields.get(fieldName));
			}else {
				new CFWLog(logger)
				.method("toForm")
				.severe("The field '"+fieldName+"' is not known for this CFWObject.");
			}
		}
		
		return form;
	}
	
	/****************************************************************
	 * Return a JSON string containing all values of the fields of this 
	 * object.
	 ****************************************************************/
	public String toJSON() {		
		return CFW.JSON.toJSON(this);
	}
	
	/****************************************************************
	 * Return a JSON string containing all values of the fields of this 
	 * object. The fields that have encryption enabled will have 
	 * encrypted values.
	 ****************************************************************/
	public String toJSONEncrypted() {		
		return CFW.JSON.toJSONEncrypted(this);
	}
	
	/****************************************************************
	 * Return a JSON Element containing all values of the fields of this 
	 * object.
	 ****************************************************************/
	public JsonElement toJSONElement() {		
		return CFW.JSON.toJSONElement(this);
	}
	
	/****************************************************************
	 * Return a JSON Element containing all values of the fields of this 
	 * object. The fields that have encryption enabled will have 
	 * encrypted values.
	 ****************************************************************/
	public JsonElement toJSONElementEncrypted() {		
		return CFW.JSON.toJSONElementEncrypted(this);
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWObject addField(CFWField<?> field) {
		
		if(!fields.containsKey(field.getName())) {
			fields.put(field.getName(), field);
		}else {
			new CFWLog(logger)
				.method("addField")
				.severe("The field with name '"+field.getName()+"' was already added to this object. Check the naming of the field.");
		}
		
		return this;
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public void addAllFields(CFWField<?>[] fields) {
		for(CFWField<?> field : fields) {
			this.addField(field);
		}
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public void addFields(CFWField<?> ...fields) {
		for(CFWField<?> field : fields) {
			this.addField(field);
		}
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public void addFields(LinkedHashMap<String, CFWField<?>> fields) {
		this.fields.putAll(fields);
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWField<?> getField(String name) {
		return fields.get(name);
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWField<?> getFieldIgnoreCase(String name) {
		
		for(String current : fields.keySet()) {
			if(current.toLowerCase().equals(name.toLowerCase())) {
				return fields.get(current);
			}
		}
		return null;
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public LinkedHashMap<String, CFWField<?>> getFields(){
		return fields;
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public String[] getFieldnames() {
		return fields.keySet().toArray(new String[] {});
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public String getTableName() {
		return tableName;
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWObject setTableName(String tableName) {
		this.tableName = tableName;
		return this;
	}
			
	/*****************************************************************************
	 * Override this method to register API definitions.
	 * 
	 *****************************************************************************/
	public ArrayList<APIDefinition> getAPIDefinitions() {
		return null;
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWField<Integer> getPrimaryField() {
		return primaryField;
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public Integer getPrimaryKey() {
		return primaryField.getValue();
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	protected CFWObject setPrimaryField(CFWField<Integer> primaryField) {
		if(this.primaryField == null) {
			this.primaryField = primaryField;
		}else {
			new CFWLog(logger)
				.method("setPrimaryField")
				.severe("Attempt to set a second primary key on CFWObject. ", new IllegalStateException());
		}
		return this;
	}
	
	/*****************************************************************************
	 * Add a foreign key definition to this object.
	 * Only one foreign key per foreign object is allowed.
	 * 
	 * @param foreignObject
	 * @param fieldname
	 * @return instance for chaining
	 *****************************************************************************/
	public CFWObject addForeignKey(String fieldname, Class<? extends CFWObject> foreignObject, String foreignFieldname, String ondelete) {
		ForeignKeyDefinition fkd = new ForeignKeyDefinition();
		fkd.fieldname = fieldname;
		fkd.foreignObject = foreignObject;
		fkd.foreignFieldname = foreignFieldname;
		fkd.ondelete = ondelete;
		
		foreignKeys.add(fkd);
		return this;
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public ArrayList<ForeignKeyDefinition> getForeignKeys() {
		return foreignKeys;
	}

	
	/****************************************************************
	 * 
	 ****************************************************************/
	public String dumpFieldsAsKeyValueString() {
		
		StringBuilder builder = new StringBuilder();
		
		for(CFWField<?> field : fields.values()) {
			builder.append("\n")
			.append(field.getName())
			.append(": ");
			if(!(field.getValue() instanceof Object[])) {
				builder.append(field.getValue());
			}else {
				builder.append(Arrays.toString((Object[])field.getValue()));
			}
			
		}

		return builder.toString();
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public String dumpFieldsAsKeyValueHTML() {
		
		StringBuilder builder = new StringBuilder();
		
		for(CFWField<?> field : fields.values()) {
			builder.append("<br/>")
			.append(field.getName())
			.append(": ")
			.append(field.getValue());
		}

		return builder.toString();
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public String dumpFieldsAsJSON(String... fieldnames) {
		
		StringBuilder builder = new StringBuilder();
		
		builder.append("{");
		for(String fieldname : fieldnames) {
			CFWField<?> field = fields.get(fieldname);
			builder
				.append("\"")
				.append(field.getName())
				.append("\": \"")
				.append(field.getValue())
				.append("\", ");
		}
		
		if(builder.length() > 1) {
			builder.deleteCharAt(builder.length()-1);
			builder.deleteCharAt(builder.length()-1);
		}
		builder.append("}");

		return builder.toString();
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public String dumpFieldsAsPlaintext(String... fieldnames) {
		
		StringBuilder builder = new StringBuilder();
		
		for(String fieldname : fieldnames) {
			builder
				.append(fields.get(fieldname).getValue())
				.append(" - ");
		}
		
		if(builder.length() > 1) {
			builder.delete(builder.length()-3, builder.length()-1);
		}

		return builder.toString();
	}
	//##############################################################################
	// HIERARCHY
	//##############################################################################
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public boolean isHierarchical() {
		return (hierarchyLevels > 0);
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public int getHierarchyLevels() {
		return hierarchyLevels;		
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public void setHierarchyLevels(int hierarchyLevels) {
		CFWHierarchy.setHierarchyLevels(this, hierarchyLevels);
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public LinkedHashMap<Integer, CFWObject> getChildObjects() {
		return childObjects;
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public void getChildObjects(LinkedHashMap<Integer, CFWObject> childObjects) {
		this.childObjects = childObjects;
	}
	
//	public CFWObject addChildObject(CFWObject child) {
//		this.childObjects.put(((Integer)child.getPrimaryField().getValue()), child);
//		return this;
//	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public void removeChildObject(CFWObject child) {
		this.childObjects.remove(((Integer)child.getPrimaryField().getValue()));
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWObject getDefaultObject() {
		return parent;
	}
	
	/****************************************************************
	 * Set the parent object of this object and adds it to the 
	 * The childs db entry has to be updated manually afterwards.
	 * 
	 * @return true if succesful, false otherwise.
	 * 
	 ****************************************************************/
	public boolean setParent(CFWObject parent) {
		return CFWHierarchy.setParent(parent, this);
	}
	
	
	//##############################################################################
	// DATABASE
	//##############################################################################
	
	/****************************************************************
	 * Executed before createTable() is executed. Can be overriden
	 * to migrate existing tables to use CFWObjects instead.
	 * @return CFWQuery for method chaining
	 ****************************************************************/
	public void migrateTable() {
		
	}

	/****************************************************************
	 * Create the table for this Object.
	 * Will be executed after all migrateTable() methods where executed
	 * of all objects in the Registry. 
	 * 
	 * @return true if successful, false otherwise
	 ****************************************************************/
	public boolean createTable() {
		return new CFWSQL(this).createTable();
	}
	
	/****************************************************************
	 * Executed after createTable() and before initDB() is executed. 
	 * Can be overriden to update tables and table data.
	 * @return CFWQuery for method chaining
	 ****************************************************************/
	public void updateTable() {
		
	}
	
	/****************************************************************
	 * Will be executed after all createTable() methods where executed
	 * of all objects in the Registry. 
	 * 
	 ****************************************************************/
	public void initDB() {
		
	}
	
	/****************************************************************
	 * Will be executed after all initDB() methods where executed
	 * of all objects in the Registry. Use this in case you have 
	 * dependency on other data created first.
	 ****************************************************************/
	public void initDBSecond() {
		
	}
	
	/****************************************************************
	 * Will be executed after all initDBSecond() methods where executed
	 * of all objects in the Registry. Use this in case you have 
	 * dependency on other data created first.
	 ****************************************************************/
	public void initDBThird() {
		
	}
	
	/****************************************************************
	 * Caches the query with the specified name for lower performance
	 * impact.
	 * @param Class of the class using the query.
	 * @param name of the query
	 ****************************************************************/
	public CFWSQL queryCache(Class<?> clazz, String name) {
		return new CFWSQL(this).queryCache(clazz, name);
	}
	
	/****************************************************************
	 * Begins a SELECT * statement.
	 * @return CFWQuery for method chaining
	 ****************************************************************/
	public CFWSQL select() {
		return new CFWSQL(this).select();
	}
	
	/****************************************************************
	 * Begins a SELECT statement including the specified fields.
	 * @param field names
	 * @return CFWQuery for method chaining
	 ****************************************************************/
	public CFWSQL select(ArrayList<String> fieldnames) {
		return new CFWSQL(this).select(fieldnames.toArray(new String[] {}));
	}
	
	/****************************************************************
	 * Begins a SELECT statement including the specified fields.
	 * @param field names
	 * @return CFWQuery for method chaining
	 ****************************************************************/
	public CFWSQL select(String ...fieldnames) {
		return new CFWSQL(this).select(fieldnames);
	}
	
	/****************************************************************
	 * Begins a SELECT statement including all fields except the 
	 * ones specified by the parameter.
	 * @param fieldnames
	 * @return CFWStatement for method chaining
	 ****************************************************************/
	public CFWSQL selectWithout(String ...fieldnames) {
		return new CFWSQL(this).selectWithout(fieldnames);
	}
	/****************************************************************
	 * Begins a SELECT * statement.
	 * @return CFWQuery for method chaining
	 ****************************************************************/
	public CFWSQL selectCount() {
		return new CFWSQL(this).selectCount();
	}
	
	/****************************************************************
	 * Creates an insert statement including all fields and executes
	 * the statement with the values assigned to the fields of the
	 * object.
	 * @return boolean
	 ****************************************************************/
	public boolean insert() {
		return new CFWSQL(this).insert();
	}
	
	/****************************************************************
	 * Creates an insert statement including the specified fields
	 * and executes it with the values assigned to the fields of the
	 * object.
	 * @param fieldnames
	 * @return true if successful, false otherwise
	 ****************************************************************/
	public boolean insert(String ...fieldnames) {
		return new CFWSQL(this).insert(fieldnames);
	}
	
	/****************************************************************
	 * Creates an insert statement including all fields and executes
	 * the statement with the values assigned to the fields of the
	 * object.
	 * @return  primary key or null if not successful
	 ****************************************************************/
	public Integer insertGetPrimaryKey() {
		return new CFWSQL(this).insertGetPrimaryKey();
	}
	
	/****************************************************************
	 * Creates an insert statement including the specified fields
	 * and executes it with the values assigned to the fields of the
	 * object.
	 * @param fieldnames
	 * @return  id or null if not successful
	 ****************************************************************/
	public Integer insertGetPrimaryKey(String ...fieldnames) {
		return new CFWSQL(this).insertGetPrimaryKey(fieldnames);
	}
	
	/****************************************************************
	 * Creates an update statement including all fields and executes
	 * the statement with the values assigned to the fields of the
	 * object.
	 * @return CFWQuery for method chaining
	 ****************************************************************/
	public boolean update() {
		return new CFWSQL(this).update();
	}
	
	/****************************************************************
	 * Creates an update statement including the specified fields
	 * and executes it with the values assigned to the fields of the
	 * object.
	 * @param fieldnames
	 * @return CFWQuery for method chaining
	 ****************************************************************/
	public boolean update(String ...fieldnames) {
		return new CFWSQL(this).update(fieldnames);
	}
	
	/****************************************************************
	 * Begins a DELETE statement.
	 * @return CFWStatement for method chaining
	 ****************************************************************/
	public CFWSQL delete() {
		return new CFWSQL(this).delete();
	}

}
