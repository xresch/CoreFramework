package com.pengtoolbox.cfw.db;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public abstract class DBInterface {

	protected static Logger logger = CFWLog.getLogger(DBInterface.class.getName());
	
	protected ThreadLocal<ArrayList<Connection>> myOpenConnections = new ThreadLocal<ArrayList<Connection>>();
	protected ThreadLocal<Connection> transactionConnection = new ThreadLocal<Connection>();

	
	/********************************************************************************************
	 * Get a connection from the connection pool or returns the current connection used for the 
	 * transaction.
	 * 
	 * @throws SQLException 
	 ********************************************************************************************/
	public abstract Connection getConnection() throws SQLException;
	
	/********************************************************************************************
	 * Add a connection that was openend to the list of open connections.
	 * When connections remain after the Servlet returns, they will be closed 
	 * by the RequestHandler using hardCloseRemainingConnections().
	 * 
	 * @throws SQLException 
	 ********************************************************************************************/
	public void forceCloseRemainingConnections() {	
		
		if(myOpenConnections.get() == null) {
			//all good, return
			return;
		}
		
		ArrayList<Connection> connArray = myOpenConnections.get();
		
		int counter = 0;
		
		//Create new array to avoid ConcurrentModificationException
		for(Connection con : connArray.toArray(new Connection[] {})) {
			

			try {
				if(!con.isClosed()) {
					counter++;			
					System.out.println("ForceClose: "+con.getClass());
					con.close();
				}
				connArray.remove(con);
			} catch (SQLException e) {
				new CFWLog(logger)
					.silent(true)
					.method("forceCloseRemainingConnections")
					.severe("Error on forced closing of DB connection.", e);
			}
		}
		
		if(counter > 0) {
			new CFWLog(logger)
				.silent(true)
				.method("forceCloseRemainingConnections")
				.warn(""+counter+" database connection(s) not closed properly.");
		}
	}
	
	/********************************************************************************************
	 * Add a connection that was openend to the list of open connections.
	 * When connections remain after the Servlet returns, they will be closed 
	 * by the RequestHandler using forceCloseRemainingConnections().
	 * 
	 * @throws SQLException 
	 ********************************************************************************************/
	protected void addOpenConnection(Connection connection) {	
		if(myOpenConnections.get() == null) {
			myOpenConnections.set(new ArrayList<Connection>());
		}
		
		myOpenConnections.get().add(connection);
	}
	
	/********************************************************************************************
	 * Removes a connection that was openend from the list of open connections.
	 * When connections remain after the Servlet returns, they will be closed 
	 * by the RequestHandler using hardCloseRemainingConnections().
	 * 
	 * @throws SQLException 
	 ********************************************************************************************/
	private void removeOpenConnection(Connection connection) {	
		
		if(myOpenConnections.get() == null) {
			return;
		}
		myOpenConnections.get().remove(connection);
	}
	
	/********************************************************************************************
	 * Starts a new transaction.
	 * 
	 * @throws SQLException 
	 ********************************************************************************************/
	public void beginTransaction() {	
		
		if(transactionConnection.get() != null) {
			new CFWLog(logger)
				.method("beginTransaction")
				.severe("A transaction was already started for this thread. Use commitTransaction() before starting another one.");
			return;
		}
		
		try {
			Connection con = this.getConnection();
			con.setAutoCommit(false);
			transactionConnection.set(con);
			addOpenConnection(con);
			new CFWLog(logger).method("beginTransaction").finer("DB transaction started.");
			
		} catch (SQLException e) {
			new CFWLog(logger)
				.method("beginTransaction")
				.severe("Error while retrieving DB connection.", e);
		}
		
	}
	
	/********************************************************************************************
	 * Starts a new transaction.
	 * 
	 * @throws SQLException 
	 ********************************************************************************************/
	public void commitTransaction() {	
		
		
		if(transactionConnection.get() == null) {
			new CFWLog(logger)
				.method("commitTransaction")
				.severe("There is no running transaction. Use beginTransaction() before using commit.");
			return;
		}
		
		Connection con = null;
		
		try {
			con = transactionConnection.get();
			con.commit();
			new CFWLog(logger).method("commitTransaction").finer("DB transaction committed.");
		} catch (SQLException e) {
			new CFWLog(logger)
				.method("commitTransaction")
				.severe("Error occured on commit transaction.", e);
		} finally {
			transactionConnection.set(null);
			if(con != null) { 
				try {
					removeOpenConnection(con);
					con.setAutoCommit(true);
					con.close();
				} catch (SQLException e) {
					new CFWLog(logger)
						.method("commitTransaction")
						.severe("Error occured closing DB resources.", e);
				}
				
			}
		}
		
	}
	
	/********************************************************************************************
	 * Rollbacks the transaction.
	 * 
	 * @throws SQLException 
	 ********************************************************************************************/
	public void rollbackTransaction() {	
		
		
		if(transactionConnection.get() == null) {
			new CFWLog(logger)
				.method("rollbackTransaction")
				.severe("There is no running transaction. Use beginTransaction() before using commit.");
			return;
		}
		
		Connection con = null;
		
		try {
			con = transactionConnection.get();
			con.rollback();
			new CFWLog(logger).method("rollbackTransaction").finer("DB transaction rolled back.");
		} catch (SQLException e) {
			new CFWLog(logger)
				.method("rollbackTransaction")
				.severe("Error occured on rollback transaction.", e);
		} finally {
			transactionConnection.set(null);
			if(con != null) { 
				try {
					con.setAutoCommit(true);
					con.close();
					removeOpenConnection(con);
				} catch (SQLException e) {
					new CFWLog(logger)
						.method("rollbackTransaction")
						.severe("Error occured closing DB resources.", e);
				}
				
			}
		}
		
	}
	
	/********************************************************************************************
	 * 
	 * @param request HttpServletRequest containing session data used for logging information(null allowed).
	 * @param sql string with placeholders
	 * @param values the values to be placed in the prepared statement
	 * @return true if update count is > 0, false otherwise
	 ********************************************************************************************/
	public boolean preparedExecute(String sql, Object... values){	
        
		CFWLog log = new CFWLog(logger).method("preparedExecute").start();
		Connection conn = null;
		PreparedStatement prepared = null;

		boolean result = false;
		try {
			//-----------------------------------------
			// Initialize Variables
			conn = this.getConnection();
			prepared = conn.prepareStatement(sql);
			
			//-----------------------------------------
			// Prepare Statement
			this.prepareStatement(prepared, values);
			
			//-----------------------------------------
			// Execute
			boolean isResultSet = prepared.execute();

			if(!isResultSet) {
				if(prepared.getUpdateCount() > 0) {
					result = true;
				}
			}
		} catch (SQLException e) {
			log.severe("Database Error: "+e.getMessage(), e);
		} finally {
			try {
				if(conn != null && transactionConnection.get() == null) { 
					removeOpenConnection(conn);
					conn.close(); 
				}
				if(prepared != null) { prepared.close(); }
			} catch (SQLException e) {
				log.severe("Issue closing resources.", e);
			}
			
		}
		
		log.custom("sql", sql).end();
		return result;
	}
	
	/********************************************************************************************
	 * 
	 * @param sql string with placeholders
	 * @param values the values to be placed in the prepared statement
	 * @return true if update count is > 0, false otherwise
	 ********************************************************************************************/
	public boolean preparedExecuteBatch(String sql, Object... values){	
        
		CFWLog log = new CFWLog(logger).method("preparedExecuteBatch").start();
		Connection conn = null;
		PreparedStatement prepared = null;

		boolean result = true;
		try {
			//-----------------------------------------
			// Initialize Variables
			conn = this.getConnection();
			prepared = conn.prepareStatement(sql);
			
			//-----------------------------------------
			// Prepare Statement
			DBInterface.prepareStatement(prepared, values);
			prepared.addBatch();
			
			//-----------------------------------------
			// Execute
			int[] resultCounts = prepared.executeBatch();

			for(int i : resultCounts) {
				if(i < 0) {
					result = false;
					break;
				}
			}
		} catch (SQLException e) {
			result = false;
			log.severe("Database Error: "+e.getMessage(), e);
		} finally {
			try {
				if(conn != null && transactionConnection.get() == null) { 
					removeOpenConnection(conn);
					conn.close(); 
				}
				if(prepared != null) { prepared.close(); }
			} catch (SQLException e) {
				log.severe("Issue closing resources.", e);
			}
			
		}
		
		log.custom("sql", sql).end();
		return result;
	}
	
	/********************************************************************************************
	 * Executes the insert and returns the generated Key of the new record. (what is a
	 * primary key in most cases)
	 * 
	 * @param sql string with placeholders
	 * @param generatedKeyName name of the column of the key to retrieve
	 * @param values the values to be placed in the prepared statement
	 * @return generated key, null if not successful
	 ********************************************************************************************/
	public Integer preparedInsertGetKey(String sql, String generatedKeyName, Object... values){	
        
		CFWLog log = new CFWLog(logger).method("preparedInsertGetKey").start();
		Connection conn = null;
		PreparedStatement prepared = null;

		Integer generatedID = null;
		try {
			//-----------------------------------------
			// Initialize Variables
			conn = this.getConnection();
			prepared = conn.prepareStatement(sql, new String[] {generatedKeyName});
			
			//-----------------------------------------
			// Prepare Statement
			prepareStatement(prepared, values);
			
			//-----------------------------------------
			// Execute
			int affectedRows = prepared.executeUpdate();

			if(affectedRows > 0) {
				ResultSet result = prepared.getGeneratedKeys();
				result.next();
				generatedID = result.getInt(generatedKeyName);
			}
		} catch (SQLException e) {
			log.severe("Database Error: "+e.getMessage(), e);
		} finally {
			try {
				if(conn != null && transactionConnection.get() == null) { 
					removeOpenConnection(conn);
					conn.close(); 
				}
				if(prepared != null) { prepared.close(); }
			} catch (SQLException e) {
				log.severe("Issue closing resources.", e);
			}
			
		}
		
		log.custom("sql", sql).end();
		return generatedID;
	}
	/********************************************************************************************
	 * Returns the result or null if there was any issue.
	 * 
	 * @param sql string with placeholders
	 * @param values the values to be placed in the prepared statement
	 * @throws SQLException 
	 ********************************************************************************************/
	public ResultSet preparedExecuteQuery(String sql, Object... values){
		return preparedExecuteQuery(false, sql, values);
	}
	
	/********************************************************************************************
	 * Returns the result or null if there was any issue.
	 * Errors will be written to log but not be propagated to client.
	 * @param sql string with placeholders
	 * @param values the values to be placed in the prepared statement
	 * @throws SQLException 
	 ********************************************************************************************/
	public ResultSet preparedExecuteQuerySilent(String sql, Object... values){
		return preparedExecuteQuery(true, sql, values);
	}
	
	/********************************************************************************************
	 * Returns the result or null if there was any issue.
	 * 
	 * @param isSilent write errors to log but do not propagate to client
	 * @param sql string with placeholders
	 * @param values the values to be placed in the prepared statement
	 * @throws SQLException 
	 ********************************************************************************************/
	private ResultSet preparedExecuteQuery(boolean isSilent, String sql, Object... values){	
        
		CFWLog log = new CFWLog(logger)
				.method("preparedExecuteQuery")
				.start();
		
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet result = null;
		try {
			//-----------------------------------------
			// Initialize Variables
			conn = this.getConnection();
			prepared = conn.prepareStatement(sql);
			
			//-----------------------------------------
			// Prepare Statement
			DBInterface.prepareStatement(prepared, values);
			
			//-----------------------------------------
			// Execute
			result = prepared.executeQuery();
			
		} catch (SQLException e) {
			log.silent(isSilent)
				.severe("Issue executing prepared statement: "+e.getLocalizedMessage(), e);
			try {
				if(conn != null && transactionConnection == null) { 
					removeOpenConnection(conn);
					conn.close(); 
					conn = null;
				}
				if(prepared != null) { prepared.close(); }
			} catch (SQLException e2) {
				log.silent(isSilent)
					.severe("Issue closing resources.", e2);
			}
		} 
		
		log.custom("sql", sql).end();
		return result;
	}
	
	/********************************************************************************************
	 * 
	 * @param request HttpServletRequest containing session data used for logging information(null allowed).
	 * @param sql string with placeholders
	 * @param values the values to be placed in the prepared statement. Supports String, Integer,
	 *               Boolean, Float, Date, Timestamp, Blob, Clob, Byte
	 * @throws SQLException 
	 ********************************************************************************************/
	public static void prepareStatement(PreparedStatement prepared, Object... values) throws SQLException{
		
		if(values != null) {
			for(int i = 0; i < values.length ; i++) {
				Object currentValue = values[i];
				// Could be a better solution
				//prepared.setObject(i+1, currentValue);
				if		(currentValue instanceof String) 	{ prepared.setString(i+1, (String)currentValue); }
				else if	(currentValue instanceof char[]) 	{ prepared.setString(i+1, new String((char[])currentValue)); }
				else if (currentValue instanceof Integer) 	{ prepared.setInt(i+1, (Integer)currentValue); }
				else if (currentValue instanceof Boolean) 	{ prepared.setBoolean(i+1, (Boolean)currentValue); }
				else if (currentValue instanceof Float) 	{ prepared.setFloat(i+1, (Float)currentValue); }
				else if (currentValue instanceof Date) 		{ prepared.setDate(i+1, (Date)currentValue); }
				else if (currentValue instanceof Timestamp) { prepared.setTimestamp(i+1, (Timestamp)currentValue); }
				else if (currentValue instanceof Blob) 		{ prepared.setBlob(i+1, (Blob)currentValue); }
				else if (currentValue instanceof Clob) 		{ prepared.setClob(i+1, (Clob)currentValue); }
				else if (currentValue instanceof Byte) 		{ prepared.setByte(i+1, (Byte)currentValue); }
				else if (currentValue instanceof Object[]) 	{ prepared.setArray(i+1, prepared.getConnection().createArrayOf("VARCHAR", (Object[])currentValue)); }
				else if (currentValue instanceof LinkedHashMap)	{ prepared.setString(i+1, CFW.JSON.toJSON(currentValue)); }
				else if (currentValue == null) 				{ prepared.setNull(i+1, Types.NULL); }
				else { throw new RuntimeException("Unsupported database field type: "+ currentValue.getClass().getName());}
			}
		}
		new CFWLog(logger).custom("preparedSQL", prepared.toString()).finest("Prepared SQL");
		//System.out.println(prepared);
	}
	
	/********************************************************************************************
	 * 
	 * @param request HttpServletRequest containing session data used for logging information(null allowed).
	 * @param resultSet which should be closed.
	 ********************************************************************************************/
	public void close(Connection conn){
		
		try {
			if(!conn.isClosed()) {
				removeOpenConnection(conn);
				conn.close();
				conn = null;
			}
		} catch (SQLException e) {
			new CFWLog(logger)
				.method("close")
				.severe("Exception occured while closing connection. ", e);
		}
	}
	/********************************************************************************************
	 * 
	 * @param request HttpServletRequest containing session data used for logging information(null allowed).
	 * @param resultSet which should be closed.
	 ********************************************************************************************/
	public void close(ResultSet resultSet){
		
		try {
			if(resultSet != null 
			&& transactionConnection.get() == null
			&& !resultSet.getStatement().isClosed()) {
				
				removeOpenConnection(resultSet.getStatement().getConnection());
				
				if(!resultSet.getStatement().getConnection().isClosed()) {
					resultSet.getStatement().getConnection().close();
					resultSet.close();
				}
			}
		} catch (SQLException e) {
			new CFWLog(logger)
				.method("close")
				.severe("Exception occured while closing ResultSet. ", e);
		}
	}

	/********************************************************************************************
	 * Returns a jsonString with an array containing a json object for each row.
	 * Returns an empty array in case of error.
	 * 
	 ********************************************************************************************/
	public static String resultSetToJSON(ResultSet resultSet) {
		return CFW.JSON.toJSON(resultSet);
	}
	
	/********************************************************************************************
	 * Returns a jsonString with an array containing a json object for each row.
	 * Returns an empty array in case of error.
	 * 
	 ********************************************************************************************/
	public static String resultSetToCSV(ResultSet resultSet, String delimiter) {
		StringBuffer csv = new StringBuffer();
		
		try {
			
			if(resultSet == null) {
				return "";
			}
			//--------------------------------------
			// Check has results
// Excluded as MSSQL might throw errors			
//			resultSet.beforeFirst();
//			if(!resultSet.isBeforeFirst()) {
//				return "";
//			}
			
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
					.method("resultSetToCSV")
					.severe("Exception occured while converting ResultSet to CSV.", e);
				
				return "";
		}

		return csv.toString();
	}
	
	/********************************************************************************************
	 * Returns a jsonString with an array containing a json object for each row.
	 * Returns an empty array in case of error.
	 * 
	 ********************************************************************************************/
	public static String resultSetToXML(ResultSet resultSet) {
		StringBuffer json = new StringBuffer();
		
		try {
			
			if(resultSet == null) {
				return "<data></data>";
			}
			//--------------------------------------
			// Check has results
// Excluded as MSSQL might throw errors			
//			resultSet.beforeFirst();
//			if(!resultSet.isBeforeFirst()) {
//				return "<data></data>";
//			}
			
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
					.method("resultSetToXML")
					.severe("Exception occured while converting ResultSet to XML.", e);
				
				return "<data></data>";
		}

		return json.toString();
	}

}
