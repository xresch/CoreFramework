package com.xresch.cfw.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWHierarchy;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.utils.ResultSetUtils;
import com.xresch.cfw.utils.ResultSetUtils.ResultSetAsJsonReader;

/**************************************************************************************************************
 * Class used to create SQL statements for a CFWObject.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWSQL {
	
	private static Logger logger = CFWLog.getLogger(CFWSQL.class.getName());
	
	private static final Cache<String, String> queryCache = CFW.Caching.addCache("CFW SQL", 
			CacheBuilder.newBuilder()
				.initialCapacity(300)
				.maximumSize(3000)
				.expireAfterAccess(24, TimeUnit.HOURS)
		);
	
	private DBInterface dbInterface;
	private String queryName = null;
	private boolean isQueryCached = false;
	private boolean isNextSelectDistinct = false;
	
	private String ALIAS = " T";
	private CFWObject object;
	@SuppressWarnings("rawtypes")
	private LinkedHashMap<String, CFWField> fields;
	private LinkedHashMap<String, String> columnSubqueries;
	
	private StringBuilder query = new StringBuilder();
	private ArrayList<Object> values = new ArrayList<Object>();
	
	private ResultSet result = null;
	
	//--------------------------------
	// Referential Actions
	public enum CFWSQLReferentialAction{
		  CASCADE
		, SET_NULL
		, SET_DEFAULT
		, RESTRICT
		, NO_ACTION
		
	}
	
	//--------------------------------
	// Foreign Key Definition
	public class ForeignKeyDefinition{
		
		public String fieldname;
		public String foreignFieldname;
		public Class<? extends CFWObject> foreignObject;
		public CFWSQLReferentialAction referentialAction;
	}
	
	
	/****************************************************************
	 * Creates a CFWSQL object with the DBInterface of the Internal H2
	 * database
	 ****************************************************************/
	public CFWSQL(CFWObject object) {
		dbInterface = CFW.DB.getDBInterface();
		if(object != null) {
			this.object = object;
			this.fields = object.getFields();
		}
	} 
	
	/****************************************************************
	 * Creates a CFWSQL object for the given DBInterface.
	 ****************************************************************/
	public CFWSQL(DBInterface dbInterface, CFWObject object) {
		this.dbInterface = dbInterface;
		if(object != null) {
			this.object = object;
			this.fields = object.getFields();
		}
	} 
	
	/****************************************************************
	 * Reset this object and make it ready for another execution.
	 ****************************************************************/
	public CFWSQL reset() {
		query = new StringBuilder();
		values = new ArrayList<Object>();
		queryName = null;
		return this;
	}
	
	/****************************************************************
	 * Returns the current String representation of the query without
	 * caching the statement.
	 ****************************************************************/
	public String getStatementString() {
		return this.query.toString();
	}
	
	/****************************************************************
	 * Builds the statement while handling the caching.
	 ****************************************************************/
	public String getStatementCached() {
		String statement = "";
		
		if(queryName == null) {
			statement = query.toString();
		}else {
			try {
				statement = queryCache.get(queryName, new Callable<String>() {

					@Override
					public String call() throws Exception {
						isQueryCached = true;
						return query.toString();
					}

				});
			} catch (ExecutionException e) {
				new CFWLog(logger)
					.severe("Error loading query from cache.", e);
			}
			
		}
		
		return statement;
	}
	
	/****************************************************************
	 * Adds the value of the field, encrypts it if necessary.
	 ****************************************************************/
	private void addFieldValue(CFWField field) {
		if(!field.persistEncrypted()) {
			this.values.add(field.getValue());
		}else {
			this.values.add(field.getValueEncrypted());
		}
	}
	/****************************************************************
	 * Caches the query with the specified name for lower performance
	 * impact.
	 * @param Class of the class using the query.
	 * @param name of the query
	 ****************************************************************/
	public CFWSQL queryCache(Class<?> clazz, String name) {
		this.queryName = clazz.getName()+"."+name;
		return this;
	}
	
	/****************************************************************
	 * Caches the query using the signature of the calling method.
	 * Slightly less performant than the other method but error
	 * prone to copy & paste mistakes.
	 * 
	 * return CFWSQL for chaining
	 ****************************************************************/
	public CFWSQL queryCache() {
		
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement cfwLogInstantiatingMethod = stacktrace[2];
		this.queryName = cfwLogInstantiatingMethod.toString();
		return this;
	}
	

	
	/****************************************************************
	 * Check if the fieldname is valid.
	 * @return true if valid, false otherwise
	 ****************************************************************/
	private boolean isQueryCached() {
		
		if(isQueryCached) { return true; }
		
		if(queryName != null && queryCache.asMap().containsKey(queryName)) {
			//only check cache once
			isQueryCached = true;
			return true;
		}
		
		return false;
	}
		
	
	/****************************************************************
	 * Create the table for the associated CFWObject.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public boolean createTable() {
		
		//------------------------------------
		// Check has primary field
		if(object.getPrimaryKeyField() == null) {
			new CFWLog(logger)
				.severe("CFWObjects need a primary field to create a table out of it. ", new IllegalStateException());
			return false;
		}
		
		//------------------------------------
		// Create Table
		boolean success = true;
		String tableName = object.getTableName();
		
		String createTableSQL = "CREATE TABLE IF NOT EXISTS "+tableName;
		success &= dbInterface.preparedExecute(createTableSQL);
		
		//------------------------------------
		// Create Columns
		for(CFWField<?> field : fields.values()) {
			if(field.getColumnDefinition() != null) {
				String addColumnIsRenamable = "ALTER TABLE "+tableName
				 +" ADD COLUMN IF NOT EXISTS \""+field.getName()+"\" "+field.getColumnDefinition();
				success &= dbInterface.preparedExecute(addColumnIsRenamable);
			}else {
				new CFWLog(logger)
					.severe("The field "+field.getName()+" is missing a columnDefinition. Use CFWField.setColumnDefinition(). ");
				success &= false;
			}
		}
		
		//------------------------------------
		// Create ForeignKeys
		for(ForeignKeyDefinition fkd : object.getForeignKeys()) {
			String foreignTable;
			try {
				foreignTable = fkd.foreignObject.newInstance().getTableName();
					
				// Example String: ALTER TABLE PUBLIC.CORE_USERROLE_TO_PARAMETER ADD CONSTRAINT IF NOT EXISTS PUBLIC.CURTBP_USER_ID FOREIGN KEY(USER_ID) REFERENCES PUBLIC.CORE_USER(ID) NOCHECK;
				String createForeignKeysSQL = "ALTER TABLE "+tableName
				  + " ADD CONSTRAINT IF NOT EXISTS PUBLIC.FK_"+tableName+"_"+fkd.fieldname
				  + " FOREIGN KEY ("+fkd.fieldname
				  + ") REFERENCES "+foreignTable+"("+fkd.foreignFieldname+") ON DELETE "+fkd.referentialAction.toString().replace("_", " ");
			
				success &= dbInterface.preparedExecute(createForeignKeysSQL);
				
			} catch (Exception e) {
				new CFWLog(logger)
				.severe("An error occured trying to create foreign keys for table: "+tableName, e);
			} 
			
		}
		//------------------------------------
		// Create Fulltext Search Index
		if(object.hasFulltextSearch()) {
			
			//------------------------------------
			// Create Columns String
			ArrayList<String> columnNames = object.getFulltextColumns();
			String columnsString = Joiner.on(",").join(columnNames);
			
			// Check index exists
			int count = new CFWSQL(null)
			.custom("SELECT COUNT(*) FROM FTL.INDEXES WHERE \"TABLE\" = ? AND \"COLUMNS\" = ?", tableName, columnsString)
			.executeCount();
			
			//only create if the index does not already exist
			if(count == 0) {
				
				//Cleanup existing INDEXES
				new CFWSQL(null)
					.custom("CALL FTL_DROP_INDEX('PUBLIC', '"+tableName+"');")
					.executeDelete();
				
				new CFWLog(logger).info("Creating fulltext search index for table '"+tableName+"'. this might take some time.");
				success &= dbInterface.preparedExecute("CALL FTL_CREATE_INDEX('PUBLIC', '"+tableName+"', '"+columnsString+"');");
				
			}
		}
		
		return success;
		
	}
	
	/****************************************************************
	 * Drops an ex
	 ****************************************************************/
	public boolean fulltextsearchReindex() {
		//------------------------------------
		// Create Fulltext Search Index
		if(object.hasFulltextSearch()) {

			String tableName = object.getTableName();
			ArrayList<String> columnNames = object.getFulltextColumns();
			String columnsString = Joiner.on(",").join(columnNames);
			
			// Check index exists
			int count = new CFWSQL(null)
			.custom("SELECT COUNT(*) FROM FTL.INDEXES WHERE \"TABLE\" = ? AND \"COLUMNS\" = ?", tableName, columnsString)
			.executeCount();
			
			if(count != 0) {
				
				//Cleanup existing INDEXES
				new CFWSQL(null)
					.custom("CALL FTL_DROP_INDEX('PUBLIC', '"+tableName+"');")
					.executeDelete();
			}
			
			new CFWLog(logger).info("Creating fulltext search index for table '"+tableName+"'. this might take some time.");
			return dbInterface.preparedExecute("CALL FTL_CREATE_INDEX('PUBLIC', '"+tableName+"', '"+columnsString+"');");
				
		}
		
		return false;
	}
	
	/****************************************************************
	 * Renames a table.

	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public boolean renameTable(String oldname, String newname) {
		
		String renameTable = "ALTER TABLE IF EXISTS "+oldname+" RENAME TO "+newname;
		return dbInterface.preparedExecute(renameTable);
	}
	
	/****************************************************************
	 * Renames a column.

	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public boolean renameColumn(String tablename, String oldname, String newname) {
		
		String renameColumn = "ALTER TABLE IF EXISTS "+tablename+" ALTER COLUMN IF EXISTS "+oldname+" RENAME TO "+newname;
		return dbInterface.preparedExecute(renameColumn);
	}
	
	/****************************************************************
	 * Renames a foreignkey.
	 *
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public boolean renameForeignKey(String oldTablename, String oldFieldname, String newTablename, String newFieldname) {
		
		String renameForeignKey = "ALTER TABLE IF EXISTS "+oldTablename+
				" RENAME CONSTRAINT FK_"+oldTablename+"_"+oldFieldname
			  + " TO FK_"+newTablename+"_"+newFieldname;

		return dbInterface.preparedExecute(renameForeignKey);
	}
	
	
	/****************************************************************
	 * Add a column subquery which will be added to the select statement.
	 * This methd has to be called before you call the select*() method.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL columnSubquery(String asName, String query) {
		return this.columnSubquery(asName, query, (Object[])null);
	}
	
	/****************************************************************
	 * Add a column subquery which will be added to the select statement.
	 * This methd has to be called before you call the select*() method.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL columnSubquery(String asName, String query, Object... values) {
		
		if(columnSubqueries == null) {
			columnSubqueries = new LinkedHashMap<String, String>();
		}
		columnSubqueries.put(asName, query);
		
		if(values != null) {
			for(Object object : values) {
				this.values.add(object);
			}
		}
		
		return this;
	}
	
	/****************************************************************
	 * Add a column subquery which will be added to the select statement.
	 * This methd has to be called before you call the select*() method.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL columnSubqueryTotalRecords() {
		return this.columnSubquery("TOTAL_RECORDS", "COUNT(*) OVER()");
	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	private String getColumnSubqueriesString() {
		if(columnSubqueries != null && !columnSubqueries.isEmpty() ) {
			StringBuilder builder = new StringBuilder();
			for(Entry<String, String> entry : columnSubqueries.entrySet()) {
				builder.append(", (")
				.append(entry.getValue())
				.append(") AS ")
				.append(entry.getKey());
			}
			
			return builder.toString();
		}
		return "";
	}
	/****************************************************************
	 * 
	 ****************************************************************/
	private boolean hasColumnSubqueries() {
		if(columnSubqueries != null && !columnSubqueries.isEmpty() ) {
			return true;
		}
		return false;
	}
	
	/****************************************************************
	 * Begins a SELECT * statement.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL select() {
		if(!isQueryCached()) {
			
			String select = " SELECT";
			if(isNextSelectDistinct) {
				select += " DISTINCT";
				isNextSelectDistinct = false;
			}
			
			if(!this.hasColumnSubqueries()) {
				query.append(select).append(ALIAS+".* FROM "+object.getTableName()+ALIAS+" ");
			}else {
				query.append(select).append(ALIAS+".* "+this.getColumnSubqueriesString()+" FROM "+object.getTableName()+ALIAS+" ");
			}
		}
		return this;
	}
	
	/****************************************************************
	 * Begins a SELECT statement including the specified fields.
	 * @param field names
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL select(Object ...fieldnames) {
		if(!isQueryCached()) {
			
			String select = " SELECT";
			if(isNextSelectDistinct) {
				select += " DISTINCT";
				isNextSelectDistinct = false;
			}
			query.append(select);
			//---------------------------------
			// Add Fields
			for(Object fieldname : fieldnames) {
					query.append(ALIAS+".\"").append(fieldname).append("\",");
			}
			query.deleteCharAt(query.length()-1);
			
			//---------------------------------
			// Add Column Subqueries
			if(this.hasColumnSubqueries()) {
				query.append(this.getColumnSubqueriesString());
			}
			
			query.append(" FROM "+object.getTableName()+ALIAS+" ");
		}
		return this;
	}
	
	/****************************************************************
	 * Begins a SELECT statement including all fields except the 
	 * ones specified by the parameter.
	 * @param fieldnames
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL selectWithout(String ...fieldnames) {
		if(!isQueryCached()) {
			
			String select = " SELECT";
			if(isNextSelectDistinct) {
				select += " DISTINCT";
				isNextSelectDistinct = false;
			}
			query.append(select);
			
			//---------------------------------
			// Add Fields
			Arrays.sort(fieldnames);
			for(String name : fields.keySet()) {
				//add if name is not in fieldnames
				if(Arrays.binarySearch(fieldnames, name) < 0) {
					query.append(ALIAS+".").append(name).append(",");
				}
			}
			query.deleteCharAt(query.length()-1);
			
			//---------------------------------
			// Add Column Subqueries
			if(this.hasColumnSubqueries()) {
				query.append(this.getColumnSubqueriesString());
			}
			
			query.append(" FROM "+object.getTableName()+ALIAS+" ");
		}
		return this;
	}
	
	
	/****************************************************************
	 * Begins a SELECT COUNT(*) statement.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL selectCount() {
		if(!isQueryCached()) {
			String select = " SELECT";
			if(isNextSelectDistinct) {
				select += " DISTINCT";
				isNextSelectDistinct = false;
			}
			query.append(select);
			query.append(" COUNT(*) FROM "+object.getTableName()+ALIAS+" ");
		}
		return this;
	}
	
	/****************************************************************
	 * Makes the next select distinct.
	 ****************************************************************/
	public CFWSQL distinct() {
		if(!isQueryCached()) {
			isNextSelectDistinct = true;
		}
		return this;
	}

	/****************************************************************
	 * initializes a new Fulltext search searching a string in
	 * the given columns.
	 * If the filterquery or columnNames is null or empty,
	 * only a limit and offset
	 * will be added  to the query.
	 * Use the "getAs.." methods to convert the result.
	 * 
	 ****************************************************************/
	public CFWSQL fulltextSearchString(String searchString, int pageSize, int pageNumber, Object... columnNames) {
		
		if(Strings.isNullOrEmpty(searchString) 
		|| columnNames == null 
		|| columnNames.length == 0) {
			//-------------------------------------
			// Unfiltered
			return this.columnSubquery("TOTAL_RECORDS", "COUNT(*) OVER()")
				.select()
				.limit(pageSize)
				.offset(pageSize*(pageNumber-1));

		}else {
			
			//-------------------------------------
			// Filter with fulltext search
			// Enabled by CFWObject.enableFulltextSearch()
			// on the Person Object
			this.columnSubquery("TOTAL_RECORDS", "COUNT(*) OVER()")
					.select();
			
			for(int i = 0; i < columnNames.length; i++) {
				if(i == 0) { 
					this.where(); 
				} else { 
					this.or(); 
				}
				
				this.like(columnNames[i], "%"+searchString+"%");
			}
			
			return this.limit(pageSize)
				.offset(pageSize*(pageNumber-1));
		}
		
	}
	/****************************************************************
	 * initializes a new LuceneQuery fulltext search.
	 ****************************************************************/
	public CFWSQLLuceneQuery fulltextSearchLucene() {
		return new CFWSQLLuceneQuery(this);
	}
	
	/****************************************************************
	 * initializes a new Fulltext search.
	 * If the filterquery is null or empty, only a limit and offset
	 * will be added  to the query.
	 * Use the "getAs.." methods to convert the result.
	 
	 * @param filterquery a lucene filter query, can be null or empty string
	 * @param pageSize max number of results, use 0 for all results
	 * @param pageNumber the page that should be fetched. 
	 ****************************************************************/
	public CFWSQL fulltextSearchLucene(String luceneFilterquery, int pageSize, int pageNumber) {
		return fulltextSearchLucene(luceneFilterquery, null, true, pageSize, pageNumber);
	}
	
	/****************************************************************
	 * initializes a new Fulltext search.
	 * If the filterquery is null or empty, only a limit and offset
	 * will be added  to the query.
	 * Use the "getAs.." methods to convert the result.
	 * 
	 * @param filterquery a lucene filter query, can be null or empty string
	 * @param sortbyColumn name of the column to sort, can be null or empty string
	 * @param pageSize max number of results, use 0 for all results
	 * @param pageNumber the page that should be fetched.
	 ****************************************************************/
	public CFWSQL fulltextSearchLucene(String luceneFilterquery, String sortbyColumn, boolean sortAscending, int pageSize, int pageNumber) {
		
		if(Strings.isNullOrEmpty(luceneFilterquery)) {
			//-------------------------------------
			// Unfiltered
			CFWSQL query = this.columnSubquery("TOTAL_RECORDS", "COUNT(*) OVER()")
				.select();
			
			if( !Strings.isNullOrEmpty(sortbyColumn) ) {
				if(sortAscending) {
					query.orderby(sortbyColumn);
				}else {
					query.orderbyDesc(sortbyColumn);
				}
			}
			
			return query.limit(pageSize)
				.offset(pageSize*(pageNumber-1));

		}else {
			//-------------------------------------
			// Filter with fulltext search
			// Enabled by CFWObject.enableFulltextSearch()
			// on the Person Object
			return this.select()
						.fulltextSearchLucene()
						.custom(luceneFilterquery)
						.build(sortbyColumn, sortAscending, pageSize, pageNumber);
		}
		
	}
	
	/****************************************************************
	 * Creates the insert statement used by insert()
	 * and insertGetPrimaryKey();
	 ****************************************************************/
	private void createInsertStatement(Object ...fieldnames) {
		
			StringBuilder columnNames = new StringBuilder("(");
			StringBuilder placeholders = new StringBuilder("(");
			
			for(Object fieldname : fieldnames) {
				CFWField<?> field = fields.get(fieldname.toString());
				if(field != object.getPrimaryKeyField()
				|| (field == object.getPrimaryKeyField() && field.getValue() != null)
				) {
					if(!isQueryCached()) {
						columnNames.append("\""+field.getName()).append("\",");
						placeholders.append("?,");
					}
					this.addFieldValue(field);
				}
			}
			
			//Replace last comma with closing brace
			columnNames.deleteCharAt(columnNames.length()-1).append(")");
			placeholders.deleteCharAt(placeholders.length()-1).append(")");
			if(!isQueryCached()) {	
				query.append("INSERT INTO "+object.getTableName()+" "+columnNames
					  + " VALUES "+placeholders+";");
			}

	}
	/****************************************************************
	 * Creates an insert statement including all fields and executes
	 * the statement with the values assigned to the fields of the
	 * object.
	 * @return boolean
	 ****************************************************************/
	public boolean insert() {

		return insert(fields.keySet().toArray(new String[] {}));
	}
	
	/****************************************************************
	 * Creates an insert statement including the specified fields
	 * and executes it with the values assigned to the fields of the
	 * object.
	 * @param fieldnames
	 * @return boolean
	 ****************************************************************/
	public boolean insert(Object ...fieldnames) {
		createInsertStatement(fieldnames);
			
		return this.execute();
	}
	
	/****************************************************************
	 * Creates an insert statement including all fields and executes
	 * the statement with the values assigned to the fields of the
	 * object.
	 * @return  primary key or null if not successful
	 ****************************************************************/
	public Integer insertGetPrimaryKey() {
		return insertGetPrimaryKey(fields.keySet().toArray(new Object[] {}));
	}
	
	/****************************************************************
	 * Creates an insert statement including all fields and executes
	 * the statement with the values assigned to the fields of the
	 * object.
	 * @return  primary key or null if not successful
	 ****************************************************************/
	public Integer insertGetPrimaryKeyWithout(Object... fieldnamesToExclude) {
		Set<String> fieldnames = new HashSet<>();
		fieldnames.addAll(fields.keySet());
		for(Object fieldname : fieldnamesToExclude) {
			fieldnames.remove(fieldname.toString());
		}
		return insertGetPrimaryKey(fieldnames.toArray(new Object[] {}));
	}
	
	/****************************************************************
	 * Creates an insert statement including the specified fields
	 * and executes it with the values assigned to the fields of the
	 * object.
	 * @param fieldnames
	 * @return  id or null if not successful
	 ****************************************************************/
	public Integer insertGetPrimaryKey(Object ...fieldnames) {
		createInsertStatement(fieldnames);
		
		return this.executeInsertGetPrimaryKey();
	}
	
	/****************************************************************
	 * Creates an update statement including all fields and executes
	 * the statement with the values assigned to the fields of the
	 * object.
	 * This method will not update any hierarchical fields. Please use
	 * the static methods of CFWHierarchy class to manage those.
	 * @return true if successful, false otherwise
	 ****************************************************************/
	public boolean update() {
		
		// clone, do not modify the instance returned by field.keySet() 
		// as this would also remove the mapping from the map itself
		Set<String> fieldnames = new HashSet<String>();
		fieldnames.addAll(fields.keySet());
		
		fieldnames.remove(CFWHierarchy.H_DEPTH);
		fieldnames.remove(CFWHierarchy.H_LINEAGE);
		fieldnames.remove(CFWHierarchy.H_PARENT);
		fieldnames.remove(CFWHierarchy.H_POS);
		
		return update(fieldnames.toArray(new Object[] {}));
	}
	
	/****************************************************************
	 * Creates an update statement including the specified fields.
	 * and executes it with the values assigned to the fields of the
	 * object.
	 * @param fieldnames
	 * @return true if successful, false otherwise
	 ****************************************************************/
	public boolean update(Object ...fieldnames) {
		
		StringBuilder columnNames = new StringBuilder();
		StringBuilder placeholders = new StringBuilder();
		
		for(Object fieldname : fieldnames) {
			CFWField<?> field = fields.get(fieldname.toString());
			if(!field.equals(object.getPrimaryKeyField())) {
				
				if(!isQueryCached()) {
					columnNames.append("\""+field.getName()).append("\",");
					placeholders.append("?,");
				}
				this.addFieldValue(field);
			}
		}
		
		if(!isQueryCached()) {
			//Replace last comma with closing brace
			columnNames.deleteCharAt(columnNames.length()-1);
			placeholders.deleteCharAt(placeholders.length()-1);
		}
		
		this.addFieldValue(object.getPrimaryKeyField());
		
		if(!isQueryCached()) {
			query.append("UPDATE "+object.getTableName()+" SET ("+columnNames
					  + ") = ("+placeholders+")"
					  +" WHERE "
					  + object.getPrimaryKeyField().getName()+" = ?");
		}
		return this.execute();
	}
	
	/****************************************************************
	 * Creates an update statement including the specified fields.
	 * and executes it with the values assigned to the fields of the
	 * object.
	 * @param fieldnames
	 * @return true if successful, false otherwise
	 ****************************************************************/
	@SuppressWarnings("rawtypes")
	public boolean updateWithout(String ...fieldnames) {
		
		StringBuilder columnNames = new StringBuilder();
		StringBuilder placeholders = new StringBuilder();
		Arrays.sort(fieldnames);
		for(Entry<String, CFWField> entry : fields.entrySet()) {
			//add if name is not in fieldnames
			if(Arrays.binarySearch(fieldnames, entry.getKey()) < 0) {
				CFWField<?> field = entry.getValue();
				if(!field.equals(object.getPrimaryKeyField())) {
					
					if(!isQueryCached()) {
						columnNames.append("\""+field.getName()).append("\",");
						placeholders.append("?,");
					}
					this.addFieldValue(field);
				}
			}
		}
		
		if(!isQueryCached()) {
			//Replace last comma with closing brace
			columnNames.deleteCharAt(columnNames.length()-1);
			placeholders.deleteCharAt(placeholders.length()-1);
		}
		
		this.addFieldValue(object.getPrimaryKeyField());
		
		if(!isQueryCached()) {
			query.append("UPDATE "+object.getTableName()+" SET ("+columnNames
					  + ") = ("+placeholders+")"
					  +" WHERE "
					  + object.getPrimaryKeyField().getName()+" = ?");
		}
		return this.execute();
	}
	
	/****************************************************************
	 * Begins a DELETE statement.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL delete() {
		if(!isQueryCached()) {		
			query.append("DELETE FROM "+object.getTableName()+ALIAS+" ");
		}
		return this;
	}
	
	/****************************************************************
	 * Begins a DELETE TOP  statement.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL deleteTop(int count) {
		if(!isQueryCached()) {		
			query.append("DELETE TOP ? FROM "+object.getTableName()+ALIAS+" ");
		}
		values.add(count);
		
		return this;
	}
	/****************************************************************
	 * Adds an WHERE to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL where() {
		if(!isQueryCached()) {
			query.append(" WHERE ");
		}
		return this;
	}
	/****************************************************************
	 * Adds a WHERE clause to the query.
	 * This method is case sensitive.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL where(Object fieldname, Object value) {
		return where(fieldname, value, true);
	}
	
	/****************************************************************
	 * Adds a WHERE clause to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL where(Object fieldname, Object value, boolean isCaseSensitive) {
		if(!isQueryCached()) {
			if(value == null) {
				return where().isNull(fieldname);
			}
			if(isCaseSensitive) {
				query.append(" WHERE "+ALIAS+".\"").append(fieldname).append("\" = ?");	
			}else {
				query.append(" WHERE LOWER("+ALIAS+".").append(fieldname).append(") = LOWER(?)");	
			}
		}
		values.add(value);
		return this;
	}
	
	
	/****************************************************************
	 * Adds a WHERE clause to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL whereLike(Object fieldname, Object value) {
		if(!isQueryCached()) {
			if(value == null) {
				
				return where().isNull(fieldname);
			}
			query.append(" WHERE ").append(fieldname).append(" LIKE ?");	
		}
		values.add(value);
		return this;
	}
	
	/****************************************************************
	 * Adds a ARRAY_LENGTH and checks if it the array is null.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL arrayIsNull(Object fieldname) {
		if(!isQueryCached()) {
			query.append(" ARRAY_LENGTH(").append(fieldname).append(") IS NULL");	
		}
		return this;
	}
	
	/****************************************************************
	 * Adds a ARRAY_CONTAINS check.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL arrayContains(Object fieldname, Object value) {
		if(!isQueryCached()) {
			query.append(" ARRAY_CONTAINS(").append(fieldname).append(", ?)");	
		}
		values.add(value);
		return this;
	}
	
	
	/****************************************************************
	 * Adds a WHERE <fieldname> IN(?) clause to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL isNull(Object fieldname) {
		if(!isQueryCached()) {
			query.append(" ").append(ALIAS+"."+fieldname).append(" IS NULL");
		}
		return this;
	}

	/****************************************************************
	 * Adds a WHERE <fieldname> IN(?) clause to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL whereIn(Object fieldname, Object value) {
		if(!isQueryCached()) {
			query.append(" WHERE ").append(ALIAS+"."+fieldname).append(" IN(?)");
		}
		values.add(value);
		return this;
	}

	/****************************************************************
	 * Adds a WHERE <fieldname> IN(?,?...) clause to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL whereIn(Object fieldname, Object ...values) {
		return this.where().in(fieldname, values);
	}
	
	/****************************************************************
	 * Adds a WHERE <fieldname> IN(?,?...) clause to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	@SuppressWarnings("rawtypes")
	public CFWSQL whereIn(Object fieldname, ArrayList values) {	
		return this.where().in(fieldname, values);
	}
	
	/****************************************************************
	 * Adds a WHERE <fieldname> IN(?,?...) clause to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL in(Object fieldname, Object ...values) {
		
			StringBuilder placeholders = new StringBuilder();
			for(Object value : values) {
				placeholders.append("?,");
				this.values.add(value);
			}
			placeholders.deleteCharAt(placeholders.length()-1);
			
			if(!isQueryCached()) {
				query.append(" ").append(fieldname).append(" IN(").append(placeholders).append(")");
			}
		
		return this;
	}
	
	/****************************************************************
	 * Adds a "<fieldname> IN(?,?...)" clause to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	@SuppressWarnings("rawtypes")
	public CFWSQL in(Object fieldname, ArrayList values) {
			
		StringBuilder placeholders = new StringBuilder();
		for(Object value : values) {
			placeholders.append("?,");
			this.values.add(value);
		}
		placeholders.deleteCharAt(placeholders.length()-1);
		
		if(!isQueryCached()) {
			query.append(" ").append(fieldname).append(" IN(").append(placeholders).append(")");
		}
		
		return this;
	}
	
	/****************************************************************
	 * Adds a WHERE <fieldname> IN(?) clause to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL whereArrayContains(Object fieldname, Object value) {
		if(!isQueryCached()) {
			query.append(" WHERE ARRAY_CONTAINS(").append(ALIAS+"."+fieldname).append(", ?) ");
		}
		
		values.add(value);
		return this;
	}
	/****************************************************************
	 * Adds an AND to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL and() {
		if(!isQueryCached()) {
			query.append(" AND ");
		}
		return this;
	}
	
	/****************************************************************
	 * Adds an NOT to the query.
	 * Typical usage is ".and().not().is(...)"
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL not() {
		if(!isQueryCached()) {
			query.append(" NOT ");
		}
		return this;
	}
	/****************************************************************
	 * Adds a AND clause to the query.
	 * This method is case sensitive.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL and(Object fieldname, Object value) {
		return and(fieldname, value, true);
	}
	
	/****************************************************************
	 * Adds a WHERE clause to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL and(Object fieldname, Object value, boolean isCaseSensitive) {
		if(!isQueryCached()) {
			if(isCaseSensitive) {
				query.append(" AND ").append(ALIAS+"."+fieldname).append(" = ?");	
			}else {
				query.append(" AND LOWER(").append(ALIAS+"."+fieldname).append(") = LOWER(?)");	
			}
		}
		values.add(value);
		return this;
	}
	
	/****************************************************************
	 * Adds an OR to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL or() {
		if(!isQueryCached()) {
			query.append(" OR ");
		}
		return this;
	}
	/****************************************************************
	 * Adds a OR clause to the query.
	 * This method is case sensitive.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL or(Object fieldname, Object value) {
		return or(fieldname, value, true);
	}
	
	/****************************************************************
	 * Adds a OR clause to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL or(Object fieldname, Object value, boolean isCaseSensitive) {
		if(!isQueryCached()) {
			if(isCaseSensitive) {
				query.append(" OR ").append(ALIAS+"."+fieldname).append(" = ?");	
			}else {
				query.append(" OR LOWER(").append(ALIAS+"."+fieldname).append(") = LOWER(?)");	
			}
		}
		values.add(value);
		return this;
	}
	
	/****************************************************************
	 * Adds a equals operation to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL is(Object fieldname, Object value) {
		if(!isQueryCached()) {
			query.append(" ").append(ALIAS+"."+fieldname).append(" = ").append(" ? ");
		}
		
		values.add(value);
		return this;
	}
	

	
	/****************************************************************
	 * Adds a LIKE operation to the query which is case sensitive,
	 * except the column type is of IGNORECASE.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL like(Object fieldname, Object value) {
		return like(fieldname, value, true);
	}
	
	/****************************************************************
	 * Adds a LIKE operation to the query.
	 * If the parameter isCaseSensitive is variable, the query cannot
	 * be cached.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL like(Object fieldname, Object value, boolean isCaseSensitive) {
		if(!isQueryCached()) {
			if(isCaseSensitive) {
				query.append(" ").append(ALIAS+"."+fieldname).append(" LIKE ").append(" ? ");
			}else {
				query.append(" LOWER(").append(ALIAS+"."+fieldname).append(") LIKE LOWER(").append("?) ");
			}
		}

		values.add(value);

		return this;
	}
	
	
	/****************************************************************
	 * Adds a UNION to the statement.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL union() {
		if(!isQueryCached()) {
			query.append(" UNION ");
		}
		return this;
	}
	
	/****************************************************************
	 * Adds a UNION ALL to the statement.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL unionAll() {
		if(!isQueryCached()) {
			query.append(" UNION ALL ");
		}
		return this;
	}
	
	/****************************************************************
	 * Adds a NULLS FIRST to the statement.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL nullsFirst() {
		if(!isQueryCached()) {
			query.append(" NULLS FIRST");
		}
		return this;
	}
	
	/****************************************************************
	 * Adds an ORDER BY clause to the query. Is case sensitive for
	 * strings.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL orderby(Object fieldname) {
		if(!isQueryCached()) {
			if(fields == null) {
				query.append(" ORDER BY T."+fieldname);
			}else if(fields.get(fieldname.toString()) != null
				  && fields.get(fieldname.toString()).getValueClass() == String.class) {
				
				query.append(" ORDER BY LOWER(T."+fieldname+")");
			}else {
				query.append(" ORDER BY T."+fieldname);
			}
		}
		return this;
	}
	
	/****************************************************************
	 * Adds an ORDER BY clause to the query. Is case sensitive for
	 * strings.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL orderby(String... fieldnames) {
		if(!isQueryCached()) {
			query.append(" ORDER BY");
			for(Object fieldname : fieldnames) {
				if(fields != null && fields.get(fieldname.toString()).getValueClass() == String.class) {
					query.append(" LOWER(T.").append(fieldname).append("),");
				}else {
					query.append(" T.").append(fieldname).append(",");
				}
			}
			query.deleteCharAt(query.length()-1);
			
		}
		return this;
	}
	
	/****************************************************************
	 * Adds an ORDER BY clause to the query. Is case sensitive for
	 * strings. Sort order is descending.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL orderbyDesc(Object fieldname) {
		if(!isQueryCached()) {				
			orderby(fieldname);
			
			query.append(" DESC");
		}
		return this;
	}
	
	/****************************************************************
	 * Adds a LIMIT statement
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL limit(int limit) {
		if(!isQueryCached()) {
			query.append(" LIMIT ?");
		}
		values.add(limit);
		return this;
	}
	
	/****************************************************************
	 * Adds an OFFSET statement
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL offset(int offset) {
		if(!isQueryCached()) {
			query.append(" OFFSET ?");
		}
		values.add(offset);
		return this;
	}
	
	/****************************************************************
	 * Adds ORDER BY, LIMIT and OFFSET to the query.
	 * No ORDER BY will be done if sortby is null.
	 * 
	 * @param sortby the name of the column to sort by
	 * @param isAscending do ascending sort if true, descending otherwise
	 * @param pageSize the size of the page
	 * @param pageNumber the number of the page
	 * 
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL paginate(String sortby, boolean isAscending, int pageSize,  int pageNumber) {
		return paginate(sortby, null, isAscending, pageSize, pageNumber);
	}
	
	/****************************************************************
	 * Adds ORDER BY, LIMIT and OFFSET to the query.
	 * If sortbyColumn is null, defaultSortbyColumn will be used.
	 * No ORDER BY will be done if both are null.
	 * 
	 * @param sortby the name of the column to sort by
	 * @param sortbyDefault the name of the column to sort by if sortby is null
	 * @param isAscending do ascending sort if true, descending otherwise
	 * @param pageSize the size of the page
	 * @param pageNumber the number of the page
	 * 
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL paginate(String sortby, String defaultSortbyColumn, boolean isAscending, int pageSize,  int pageNumber) {
		if(!isQueryCached()) {
			//------------------
			// Sort
			if(sortby != null || defaultSortbyColumn != null) {
				
				if(sortby == null){ sortby = defaultSortbyColumn; }
				
				if(isAscending) {
					this.orderby(sortby);
				}else {
					this.orderbyDesc(sortby);
				}
			}
			
			//------------------
			// Execute
			this.limit(pageSize)
				.offset(pageSize*(pageNumber-1));
		}
		
		return this;
		
	}
	

	
	/****************************************************************
	 * Adds a custom part to the query and values for the binding.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL loadSQLResource(String packageName, String filename, Object... params) {
		
		if(!isQueryCached()) {
			String queryPart = CFW.Files.readPackageResource(packageName, filename);
			query.append(queryPart);
		}
		
		for(Object param : params) {
			values.add(param);
		}
		return this;
	}
	
	
	/****************************************************************
	 * Adds a custom part to the query and values for the binding.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL custom(String queryPart, Object... params) {
		if(!isQueryCached()) {
			query.append(" ").append(queryPart).append(" ");
		}
		
		for(Object param : params) {
			values.add(param);
		}
		return this;
	}
	
	/****************************************************************
	 * Adds a custom part to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL custom(String queryPart) {
		if(!isQueryCached()) {
			query.append(" ").append(queryPart).append(" ");
		}
		return this;
	}
	
	/****************************************************************
	 * Adds a custom part to the query.
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public CFWSQL append(CFWSQL partialQuery) {
		if(partialQuery != null) {
			if(!isQueryCached()) {
				query.append(partialQuery.getStatementString());
			}
			
			values.addAll(partialQuery.values);
		}
		return this;
	}

	/****************************************************************
	 * Executes any SQL statement built with this class.
	 * 
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public boolean execute() {
		return this.execute(false);
	}
	

	/****************************************************************
	 * Executes SELECT statements built with this class.
	 * Will fail if any other statement will be executed.
	 * 
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public boolean executeQuery() {
		return this.execute(true);
	}
	
	/****************************************************************
	 * Executes the query and saves the results in the global 
	 * variable.
	 * @param queryOnly true
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	private boolean execute(boolean queryOnly) {
		
		//----------------------------
		// Handle Caching
		String statement = getStatementCached();
		
		//----------------------------
		// Execute Statement 
		if(queryOnly || statement.trim().startsWith("SELECT")) {
			result = dbInterface.preparedExecuteQuery(statement, values.toArray());
			if(result != null) {
				return true;
			}else {
				return false;
			}
		}else {
			return dbInterface.preparedExecute(statement, values.toArray());
		}
	}
	
	/****************************************************************
	 * Executes the query and saves the results in the global 
	 * variable.
	 * 
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public boolean executeBatch() {
		
		//----------------------------
		// Handle Caching
		String statement = getStatementCached();
		
		//----------------------------
		// Execute Statement 
		boolean success = dbInterface.preparedExecuteBatch(statement, values.toArray());
		
		dbInterface.close(result);
		
		return success;

	}
	
	/****************************************************************
	 * Executes the query as an insert and returns the first generated 
	 * Key of the new record. (what is a primary key in most cases)
	 * 
	 * @return integer or null
	 ****************************************************************/
	private Integer executeInsertGetPrimaryKey() {
		
		//----------------------------
		// Handle Caching
		String statement = getStatementCached();
		
		//----------------------------
		// Execute Statement 
		if(statement.trim().startsWith("INSERT")) {
			return dbInterface.preparedInsertGetKey(statement, object.getPrimaryKeyField().getName(), values.toArray());
			
		}else {
			new CFWLog(logger)
			.severe("The query is not an insert statement: "+statement);
			
			return null;
		}
		
	}
	
	/****************************************************************
	 * Executes the query and saves the results in the global 
	 * variable.
	 * 
	 * @return CFWSQL for method chaining
	 ****************************************************************/
	public boolean executeDelete() {
		
		boolean success = this.execute(false);
		dbInterface.close(result);
		
		return success;
	}
	
	/****************************************************************
	 * Executes a "SELECT COUNT(*)" query and returns the resulting
	 * count.
	 * Or executes any other query and returns the row number of the 
	 * last row.
	 * Returns -1 in case of error.  
	 * 
	 * @return int count or -1 on error
	 ****************************************************************/
	public int executeCount() {
		
		try {
			
			this.execute(true);
			if(result != null) {	
				//----------------------------
				// Handle Caching
				String statement = getStatementCached();
				
				//----------------------------
				//Get Count
				if(statement.toString().trim().contains("SELECT COUNT(*)")) {
					if(result.next()) {
						return result.getInt(1);
					}
				}else {
					  result.last();    // moves cursor to the last row
					  return result.getRow();
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			dbInterface.close(result);
		}
		return -1;
	}
	
	/****************************************************************
	 * Executes the query and returns the result set.
	 * Don't forget to close the connection using CFW.DB.close();
	 * @return ResultSet or null 
	 ****************************************************************/
	public ResultSet getResultSet() {
		
		if(this.execute()) {
			return result;
		}else {
			return null;
		}
	}
	
	/****************************************************************
	 * Executes the query and returns the first result as object.
	 * @return CFWObject the resulting object or null if not found.
	 ****************************************************************/
	public CFWObject getFirstAsObject() {
		
		if(this.execute(true)) {
			return ResultSetUtils.getFirstAsObject(result, object.getClass());
		}
		
		return null;
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as Objects.
	 ****************************************************************/
	public ArrayList<CFWObject> getAsObjectList() {
		
		ArrayList<CFWObject> objectArray = new ArrayList<>();
		
		if(this.execute(true)) {
			objectArray = ResultSetUtils.toObjectList(result, object.getClass());
		}
		
		return objectArray;
		
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as CFWObjects of the type
	 * specified with the parameter.
	 * 
	 * @return list of object, empty if results set is null or an error occurs.
	 ****************************************************************/	
	public <T extends CFWObject> ArrayList<T> getAsObjectListConvert(Class<T> clazz) {
		
		ArrayList<T> objectArray = new ArrayList<>();
		
		if(this.execute(true)) {
			objectArray = ResultSetUtils.toObjectListConvert(result, clazz);
		}
		
		return objectArray;
		
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as a list of maps holding.
	 * the record values as key/value pairs.
	 ****************************************************************/
	public ArrayList<LinkedHashMap<String, Object>> getAsListOfKeyValueMaps() {
		
		ArrayList<LinkedHashMap<String, Object>> resultArray =  new ArrayList<>(); 
		
		if(this.execute(true)) {
			resultArray = ResultSetUtils.toListOfKeyValueMaps(result);
		}
		
		return resultArray;
		
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as a map of primary
	 * keys and objects.
	 ****************************************************************/
	public LinkedHashMap<Integer, CFWObject> getAsKeyObjectMap() {
		
		LinkedHashMap<Integer, CFWObject> objectMap = new LinkedHashMap<>();
		
		if(this.execute(true)) {
			objectMap = ResultSetUtils.toKeyObjectMap(result, object.getClass());
		}
		
		return objectMap;
		
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as a key value map.
	 ****************************************************************/
	public HashMap<Object, Object> getAsKeyValueMap(String keyColumnName, String valueColumnName) {
		
		HashMap<Object, Object> keyValueMap = new HashMap<Object, Object>();
		
		if(this.execute(true)) {
			keyValueMap = ResultSetUtils.toKeyValueMap(result, keyColumnName, valueColumnName);
		}
		
		return keyValueMap;
		
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as a key value map.
	 ****************************************************************/
	public LinkedHashMap<String, String> getAsKeyValueMapString(String keyColumnName, String valueColumnName) {
		
		LinkedHashMap<String, String> keyValueMap = new LinkedHashMap<>();
		
		if(this.execute(true)) {
			keyValueMap = ResultSetUtils.toKeyValueMapString(result, keyColumnName, valueColumnName);
		}
		
		return keyValueMap;
		
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as a key value map.
	 ****************************************************************/
	public HashMap<Integer, Object> getAsIDValueMap(Object idColumnName, Object valueColumnName) {
		
		HashMap<Integer, Object> keyValueMap = new HashMap<>();
		
		if(this.execute(true)) {
			keyValueMap = ResultSetUtils.toIDValueMap(result, idColumnName, valueColumnName);
		}
		
		return keyValueMap;
		
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as a string array.
	 ***************************************************************/
	public String[] getAsStringArray(Object columnName) {
		String[] stringArray = new String[] {}; 
		if(this.execute(true)) {
			stringArray = ResultSetUtils.toStringArray(result, columnName.toString());
		}
		
		return stringArray;
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as a string array list.
	 ***************************************************************/
	public ArrayList<String> getAsStringArrayList(Object columnName) {
		
		if(this.execute(true)) {
			return ResultSetUtils.toStringArrayList(result, columnName.toString());
		}else {
			return new ArrayList<String>();
		}
		
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as a string array list.
	 ***************************************************************/
	public ArrayList<Integer> getAsIntegerArrayList(Object columnName) {

		if(this.execute(true)) {
			return ResultSetUtils.toIntegerArrayList(result, columnName.toString());
		}else {
			return new ArrayList<Integer>();
		}
		
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as a LinkedHashMap.
	 ***************************************************************/
	public LinkedHashMap<Object, Object> getAsLinkedHashMap(Object keyColumnName, Object valueColumnName) {
		
		LinkedHashMap<Object, Object>  resultMap = new LinkedHashMap<Object, Object>();
		
		if(this.execute(true)) {
			resultMap = ResultSetUtils.toLinkedHashMap(result, keyColumnName, valueColumnName);
		}
		
		return resultMap;
		
	}

	
	/***************************************************************
	 * Execute the Query and gets the result as an AutocompleteResult
	 * with value and label.
	 ***************************************************************/
	public AutocompleteResult getAsAutocompleteResult(Object valueColumnName, Object labelColumnName) {
		
		if(this.execute(true)) {
			return ResultSetUtils.toAsAutocompleteResult(result, valueColumnName, labelColumnName);
		}else {
			return new AutocompleteResult(new AutocompleteList());
		}

	}
	
	/***************************************************************
	 * Execute the Query and gets the result as an AutocompleteResult
	 * with value label, and description.
	 ***************************************************************/
	public AutocompleteResult getAsAutocompleteResult(Object valueColumnName, Object labelColumnName, Object descriptionColumnName) {
		
		if(this.execute(true)) {
			return ResultSetUtils.toAsAutocompleteResult(result, valueColumnName, labelColumnName, descriptionColumnName);
		}else {
			return new AutocompleteResult(new AutocompleteList());
		}
		
	}
	
	
	/***************************************************************
	 * Execute the Query and gets the result as JSON string.
	 ****************************************************************/
	public String getAsJSON() {
		
		this.execute(true);
		String	string = ResultSetUtils.toJSON(result);
		
		return string;
		
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as JSON string.
	 ****************************************************************/
	public JsonArray getAsJSONArray() {
		
		this.execute(true);
		return ResultSetUtils.toJSONArray(result);
				
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as JSON string.
	 ****************************************************************/
	public ResultSetAsJsonReader getAsJSONReader() {
		
		this.execute(true);
		return ResultSetUtils.toJSONReader(result);
				
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as JsonElements by transforming
	 * the CFWObjects.
	 * 
	 ****************************************************************/
	public ArrayList<JsonElement> getObjectsAsJSONElements() {
		
		ArrayList<CFWObject> objects = this.getAsObjectList();
		ArrayList<JsonElement> elements = new ArrayList<JsonElement>();
		
		for(CFWObject current : objects) {
			elements.add(current.toJSONElement());
		}
		
		return elements;
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as JSON string.
	 ****************************************************************/
	public JsonArray getObjectsAsJSONArray() {
		
		ArrayList<CFWObject> objects = this.getAsObjectList();
		JsonArray elements = new JsonArray();
		
		for(CFWObject current : objects) {
			elements.add(current.toJSONElement());
		}
		
		return elements;
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as JSON string.
	 ****************************************************************/
	public String getAsCSV() {
		
		this.execute(true);
		String string = ResultSetUtils.toCSV(result, ";");
		dbInterface.close(result);
		
		return string;
		
	}
	
	/***************************************************************
	 * Execute the Query and gets the result as XML string.
	 ****************************************************************/
	public String getAsXML() {
		
		this.execute(true);
		String	string = ResultSetUtils.toXML(result);
		dbInterface.close(result);
		
		return string;
		
	}
	
	/***************************************************************
	 * Returns the CFWObject the instance was created with
	 ****************************************************************/
	public CFWObject getObject() {
		return this.object;
	}
	
}
