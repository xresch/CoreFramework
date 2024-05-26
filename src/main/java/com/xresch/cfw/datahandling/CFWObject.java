package com.xresch.cfw.datahandling;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.quartz.JobExecutionContext;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField.CFWFieldFlag;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.CFWSQL.CFWSQLReferentialAction;
import com.xresch.cfw.db.CFWSQL.ForeignKeyDefinition;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.utils.ResultSetUtils;
import com.xresch.cfw.utils.json.SerializerCFWObject;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021
 * @license MIT-License
 **************************************************************************************************************/
public class CFWObject {
	
	private static Logger logger = CFWLog.getLogger(CFWObject.class.getName());
	
	//------------------------
	// General
	@SuppressWarnings("rawtypes")
	private LinkedHashMap<String, CFWField> fields = new LinkedHashMap<String, CFWField>();
	private CFWHierarchyConfig hierarchyConfig = null;
	protected  CFWObject parent;
	protected  LinkedHashMap<Integer, CFWObject> childObjects = new LinkedHashMap<Integer, CFWObject>();
	
	//---------------------------
	// Database
	protected String tableName; 
	protected ArrayList<String> fulltextSearchColumns = null;
	protected CFWField<Integer> primaryField = null;
	private ArrayList<ForeignKeyDefinition> foreignKeys = new ArrayList<ForeignKeyDefinition>();
	
	/****************************************************************
	 * Map the request parameters to the objects CFWFields.
	 * Validates the values before they get stored to the CFWFields.
	 * Creates alert messages.
	 * @return true if all could be validated successfully, false otherwise
	 ****************************************************************/
	public boolean mapRequestParameters(HttpServletRequest request) {
		
		return CFWField.mapAndValidateParamsToFields(request, fields);
	}
	

	/****************************************************************
	 * Maps the JSON fields to this objects fields by name.
	 * Validates the fields and returns true if all are valid.
	 * If the string is null or empty it will return true.
	 * @param doValidation TODO
	 * @param doSanitize TODO
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public boolean mapJsonFields(String json, boolean doValidation, boolean doSanitize) {
		
		if(Strings.isNullOrEmpty(json)) {
			return true;
		}
		
		return mapJsonFields(CFW.JSON.fromJson(json), doValidation, doSanitize);
	}
	
	/****************************************************************
	 * Maps the JSON fields to this objects fields by name.
	 * Validates the fields and returns true if all are valid.
	 * If the string is null or empty it will return true.
	 * @param doValidation set true if values should be validated before assigning
	 *   them to the fields
	 * @param doSanitize set true if values should be sanitized. 
	 *   Will always be true if values are validated. Do not set to
	 *   false if user input is mapped. Only set false when reading
	 *   already persisted(validated) data.
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public boolean mapJsonFields(JsonElement element, boolean doValidation, boolean doSanitize) {
		if(!element.isJsonObject()) {
			new CFWLog(logger).severe("JsonElement has to be of the type object.", new IllegalArgumentException());
			return false;
		}
		
		if(doValidation) {
			return CFWField.mapAndValidateJsonToFields(element.getAsJsonObject(), fields);
		}else {
			return CFWField.mapJsonToFields(element.getAsJsonObject(), fields, doSanitize);
		}
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
	public boolean mapJobExecutionContext(JobExecutionContext context) {
		return CFWField.mapJobExecutionContextToFields(context, fields);
	}
	
	/*************************************************************************
	 * Validates all fields and returns a summarized result of all validation.
	 * 
	 * @return true if all fields are valid, false otherwise.
	 *************************************************************************/ 
	public boolean validateAllFields() {
		
		boolean isValid = true;
		for(CFWField field : this.getFields().values()) {
			isValid &= field.validate();
		}
		
		return isValid;
	}
	
	/*************************************************************************
	 * Returns the summarized result of the last validations of the CFWFields.
	 * Returns false if a field was not validated.
	 * 
	 * @return true if all validators returned true, false otherwise.
	 *************************************************************************/ 
	public boolean lastValidationResult() {
		
		boolean success = true;
		for(CFWField field : fields.values()) {
			success &= field.lastValidationResult();
		}
		return success;
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
				.severe("The field '"+fieldName+"' is not known for this CFWObject.");
			}
		}
		
		return form;
	}
	
		
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWObject addField(CFWField<?> field) {
		
		if(!fields.containsKey(field.getName())) {
			fields.put(field.getName(), field);
			field.setRelatedCFWObject(this);
		}else {
			new CFWLog(logger)
				.severe("The field with name '"+field.getName()+"' was already added to this object. Check the naming of the field.", new Exception());
		}
		
		return this;
	}
	
	/****************************************************************
	 * Adds the given field after the specified field that is already
	 * in the map.
	 * If the fieldname is null, inserts at the beginning.
	 * If the fieldname is not null but is not in the list, inserts
	 * at the end of the map.
	 * If the name if the added field is the same as the fieldname to 
	 * insert after, the existing field will be replaced.
	 * 
	 ****************************************************************/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public CFWObject addFieldAfter(CFWField<?> field, Object fieldname) {
				
		//---------------------------
		// Insert at beginning of null
		if(fieldname == null) {
			LinkedHashMap<String, CFWField> tempFields = (LinkedHashMap<String, CFWField>)fields.clone();
			fields.clear();
			fields.put(field.getName(), field);
			field.setRelatedCFWObject(this);
			fields.putAll(tempFields);
			return this;
		}
		
		//---------------------------
		// Insert at end if fieldname is not found
		String fieldnameString = fieldname.toString();
		if(!fields.containsKey(fieldnameString)) {
			addField(field);
			return this;
		}
		
		//---------------------------
		// Replace if the name of the inserted 
		// field already exists
		if(field.getName().equals(fieldnameString)) {
			addField(field);
			return this;
		}
		
		//---------------------------
		// Insert After
		LinkedHashMap<String, CFWField> tempFields = (LinkedHashMap<String, CFWField>)fields.clone();
		fields.clear();
		
		for(Entry<String, CFWField> entry : tempFields.entrySet()) {
			fields.put(entry.getKey(), entry.getValue());
			
			if(entry.getKey().equals(fieldnameString)) {
				fields.put(field.getName(), field);
				field.setRelatedCFWObject(this);
			}
		}
		
		return this;
	}
		
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWObject addAllFields(CFWField<?>[] fields) {
		for(CFWField<?> field : fields) {
			this.addField(field);
		}
		return this;
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@SuppressWarnings("rawtypes")
	public CFWObject addAllFields(LinkedHashMap<String,CFWField> fields) {
		for(CFWField<?> field : fields.values()) {
			this.addField(field);
		}
		return this;
	}
		
	/****************************************************************
	 * 
	 ****************************************************************/
	@SuppressWarnings("rawtypes")
	public void addFields(CFWField ...fields) {
		for(CFWField<?> field : fields) {
			this.addField(field);
		}
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@SuppressWarnings("rawtypes")
	public void addFields(LinkedHashMap<String, CFWField> fields) {
		this.fields.putAll(fields);
	}
	
	/****************************************************************
	 * Removes a field from this object and returns it.
	 * @param fieldname of type Object to support enum value
	 * 
	 ****************************************************************/
	@SuppressWarnings("rawtypes")
	public CFWField removeField(Object fieldname) {
		CFWField removed = fields.remove(fieldname.toString());

		if(removed != null) {
			removed.setRelatedCFWObject(null);
		}
		return removed;
	}
	
	/****************************************************************
	 * Removes the fields with the given fieldnames.
	 * 
	 * @param fieldnames of type Object to support enum values
	 ****************************************************************/
	@SuppressWarnings("rawtypes")
	public void removeFields(Object ...fieldnames) {
		for(Object name : fieldnames) {
			this.removeField(name.toString());
		}
	}
	
	/****************************************************************
	 * @return the field or null if not found.
	 * 
	 ****************************************************************/
	@SuppressWarnings("rawtypes")
	public CFWField getField(String fieldname) {
		// this method is here because for some unknown reason the below one was sometimes not recognized.
		return fields.get(fieldname);
	}
	
	/****************************************************************
	 * @return the field or null if not found.
	 * 
	 ****************************************************************/
	@SuppressWarnings("rawtypes")
	public CFWField getField(Object fieldname) {
		return fields.get(fieldname.toString());
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@SuppressWarnings("rawtypes")
	public CFWField getFieldIgnoreCase(String name) {
		
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
	@SuppressWarnings("rawtypes")
	public LinkedHashMap<String, CFWField> getFields(){
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
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWObject enableFulltextSearch(ArrayList<String> columnNames) {
		fulltextSearchColumns = columnNames;
		return this;
	}
			
	/****************************************************************
	 * 
	 ****************************************************************/
	public boolean hasFulltextSearch() {
		return fulltextSearchColumns != null && fulltextSearchColumns.size() > 0;
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public ArrayList<String> getFulltextColumns() {
		return fulltextSearchColumns;
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
	public CFWField<Integer> getPrimaryKeyField() {
		return primaryField;
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public String getPrimaryKeyFieldname() {
		return primaryField.getName();
	}
	/****************************************************************
	 * 
	 ****************************************************************/
	public Integer getPrimaryKeyValue() {
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
	public CFWObject addForeignKey(String fieldname, Class<? extends CFWObject> foreignObject, Object foreignFieldname, CFWSQLReferentialAction ondelete) {
		ForeignKeyDefinition fkd = new CFWSQL(null).new ForeignKeyDefinition();
		fkd.fieldname = fieldname;
		fkd.foreignObject = foreignObject;
		fkd.foreignFieldname = foreignFieldname.toString();
		fkd.referentialAction = ondelete;
		
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
			builder
				.append(field.getName())
				.append(": ")
				.append(field.getValue())
				.append("<br/>");
		}

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
		return (hierarchyConfig != null);
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWHierarchyConfig getHierarchyConfig() {
		return hierarchyConfig;		
	}

	/****************************************************************
	 * 
	 ****************************************************************/
	public void setHierarchyConfig(CFWHierarchyConfig hierarchyConfig) {
		this.hierarchyConfig = hierarchyConfig;
		CFWHierarchy.setHierarchyConfig(this, hierarchyConfig);
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
	protected void setChildObjects(LinkedHashMap<Integer, CFWObject> childObjects) {
		this.childObjects = childObjects;
	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public void removeChildObject(CFWObject child) {
		this.childObjects.remove(((Integer)child.getPrimaryKeyField().getValue()));
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWObject getDefaultObject() {
		return parent;
	}
	

	
	//##############################################################################
	// DATABASE
	//##############################################################################
	
	/****************************************************************
	 * Executed before createTable() is executed. 
	 * Raniming columns should be done through this method (hint: CFWSQL.renameColumn()).
	 * Can be overridden to migrate existing tables to use CFWObjects instead.
	 * @return CFWQuery for method chaining
	 ****************************************************************/
	public void migrateTable() {
		//can be overridden
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
	 * Can be overridden to update tables and table data.
	 * @return CFWQuery for method chaining
	 ****************************************************************/
	public void updateTable() {
		//can be overridden
	}
	
	/****************************************************************
	 * Will be executed after all createTable() methods where executed
	 * of all objects in the Registry. 
	 * 
	 ****************************************************************/
	public void initDB() {
		//can be overridden
	}
	
	/****************************************************************
	 * Will be executed after all initDB() methods where executed
	 * of all objects in the Registry. Use this in case you have 
	 * dependency on other data created first.
	 ****************************************************************/
	public void initDBSecond() {
		//can be overridden
	}
	
	/****************************************************************
	 * Will be executed after all initDBSecond() methods where executed
	 * of all objects in the Registry. Use this in case you have 
	 * dependency on other data created first.
	 ****************************************************************/
	public void initDBThird() {
		//can be overridden
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
		return new CFWSQL(this).select(fieldnames.toArray(new Object[] {}));
	}
	
	/****************************************************************
	 * Begins a SELECT statement including the specified fields.
	 * @param fieldnames
	 * @return CFWQuery for method chaining
	 ****************************************************************/
	public CFWSQL select(String ...fieldnames) {
		return new CFWSQL(this).select((Object[])fieldnames);
	}
	
	/****************************************************************
	 * Begins a SELECT statement including the specified fields.
	 * @param field names
	 * @return CFWQuery for method chaining
	 ****************************************************************/
	public CFWSQL select(Object ...fieldnames) {
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
	 *  Begins a SELECT COUNT(*) statement.
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
	public boolean insert(Object ...fieldnames) {
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
	public Integer insertGetPrimaryKey(Object ...fieldnames) {
		return new CFWSQL(this).insertGetPrimaryKey(fieldnames);
	}
	
	/****************************************************************
	 * Creates an update statement including all fields and executes
	 * the statement with the values assigned to the fields of the
	 * object.
	 * This method will not update any hierarchical fields. Please use
	 * the static methods of CFWHierarchy class to manage those.
	 * 
	 * @return true if successful, false otherwise
	 ****************************************************************/
	public boolean update() {
		return new CFWSQL(this).update();
	}
	
	/****************************************************************
	 * Creates an update statement including the specified fields
	 * and executes it with the values assigned to the fields of the
	 * object.
	 * @param fieldnames
	 * @return true if successful, false otherwise
	 ****************************************************************/
	public boolean update(Object ...fieldnames) {
		return new CFWSQL(this).update(fieldnames);
	}
	
	/****************************************************************
	 * Creates an update statement excluding the specified fields.
	 * and executes it with the values assigned to the fields of the
	 * object.
	 * @param fieldnames
	 * @return true if successful, false otherwise
	 ****************************************************************/
	public boolean updateWithout(String... fieldnames) {
		return new CFWSQL(this).updateWithout(fieldnames);
	}
	
	/****************************************************************
	 * Begins a DELETE statement.
	 * @return CFWStatement for method chaining
	 ****************************************************************/
	public CFWSQL delete() {
		return new CFWSQL(this).delete();
	}
	
	/****************************************************************
	 * Override to return a JSON String.
	 ****************************************************************/
	@Override
	public String toString() {		
		return toJSONEncrypted();
	}
	
	/****************************************************************
	 * Return a JSON string containing all values of the fields of this 
	 * object.
	 ****************************************************************/
	public String toJSON() {		
		return CFW.JSON.toJSON(this);
	}
	
	/****************************************************************
	 * Return a JSON string containing values of the fields of this 
	 * object, flagged or not flagged with the specified flags.
	 * 
	 * @param enableEncryption if true, encrypt values that have 
	 *        encryption enabled, false otherwise
	 * @param flags the flags for the filter
	 * @param includeFlagged if true, only includes the fields 
	 *        with the specified flag, if false exclude flagged and
	 *        keep the non-flagged
	 ****************************************************************/
	public String toJSON(boolean enableEncryption, EnumSet<CFWFieldFlag> flags, boolean includeFlagged) {	
		return this.toJSONElement(enableEncryption, flags, includeFlagged)
				   .toString();
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
	 * Return a JSON element containing values of the fields of this 
	 * object, flagged or not flagged with the specified flags.
	 * 
	 * @param enableEncryption if true, encrypt values that have 
	 *        encryption enabled, false otherwise
	 * @param flags the flags for the filter
	 * @param includeFlagged if true, only includes the fields 
	 *        with the specified flag, if false exclude flagged and
	 *        keep the non-flagged
	 ****************************************************************/
	public JsonElement toJSONElement(boolean enableEncryption, EnumSet<CFWFieldFlag> flags, boolean includeFlagged) {	
		SerializerCFWObject serializer = new SerializerCFWObject(enableEncryption, flags, includeFlagged);
		
		return serializer.serialize(this, getClass(), null);
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
	 * Creates a clone of the object with the data of the fields.
	 * All other settings and children are ignored.
	 * @param clazz the class of the cloned object
	 ****************************************************************/
	public <T extends CFWObject> T cloneObject(Class<T> clazz) {
		
		T clone = null;
		try {
			clone = clazz.newInstance();
			
			clone.mapJsonFields(this.toJSON(), false, false);
			
		} catch (Exception e) {
			new CFWLog(logger)
				.severe("Error cloning object.", e);
		} 

		return clone;
		
	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public String toJSONString(String... fieldnames) {
		
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

}
