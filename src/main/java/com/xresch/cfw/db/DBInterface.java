package com.xresch.cfw.db;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWChartSettings;
import com.xresch.cfw.datahandling.CFWSchedule;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.config.FeatureConfig;
import com.xresch.cfw.logging.CFWLog;

import io.prometheus.client.Counter;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class DBInterface {

	private static Logger logger = CFWLog.getLogger(DBInterface.class.getName());
	
	protected ThreadLocal<ArrayList<Connection>> myOpenConnections = new ThreadLocal<>();
	protected ThreadLocal<Connection> transactionConnection = new ThreadLocal<>();

	private BasicDataSource pooledSource;

	private static final Counter dbcallCounter = Counter.build()
	         .name("cfw_db_calls_success_count")
	         .help("Number of database calls executed successfully through the internal CFW DBInterface.")
	         .labelNames("db")
	         .register();
	
	private static final Counter dbcallErrorCounter = Counter.build()
	         .name("cfw_db_calls_exception_count")
	         .help("Number of database calls executed through the internal CFW DBInterface and ended with and exception.")
	         .labelNames("db")
	         .register();
	
	private String InterfaceName = "";

	private static HashMap<String, BasicDataSource> managedConnectionPools = new HashMap<>();
	
	public DBInterface(String interfaceName, BasicDataSource pooledSource) {
		this.pooledSource = pooledSource;
		this.InterfaceName = interfaceName;
	}
	
	/********************************************************************************************
	 * Get the Datasource for this DBInterface.
	 * 
	 * @throws SQLException 
	 ********************************************************************************************/
	public DataSource getDatasource() {
		return pooledSource;
	}
	
	/********************************************************************************************
	 * Get a connection from the connection pool or returns the current connection used for the 
	 * transaction.
	 * 
	 * @throws SQLException 
	 ********************************************************************************************/
	public Connection getConnection() throws SQLException {
		
		//Improve performance, reduce memory overhead
		if(logger.isLoggable(Level.FINER)) {
			new CFWLog(logger)
				.finer("DB Connections Active: "+pooledSource.getNumActive());
		}
		
		if(transactionConnection.get() != null) {
			return transactionConnection.get();
		}else {
			synchronized (pooledSource) {
				Connection connection = pooledSource.getConnection();
				addOpenConnection(connection);
				return connection;
			}
		}				
	}
	
	
	/********************************************************************************************
	 * Add a connection that was openend to the list of open connections.
	 * When connections remain after the Servlet returns, they will be closed 
	 * by the RequestHandler using hardCloseRemainingConnections().
	 * 
	 * @throws SQLException 
	 ********************************************************************************************/
	public void forceCloseRemainingConnections() {	
		
		//--------------------------------------
		// Add transaction connection to handling
		if(transactionConnection.get() != null) {
			
			if(myOpenConnections.get() == null) {
				myOpenConnections.set( new ArrayList<Connection>() );
			}
			
			myOpenConnections.get().add( transactionConnection.get() );
			transactionConnection.remove();
		}
		
		//--------------------------------------
		// Return if null
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
					new CFWLog(logger)
						.minimal(true)
						.silent(true)
						.warn("DBInterface.forceCloseRemainingConnections: "+con.getClass());
					con.close();
				}
				connArray.remove(con);
			} catch (SQLException e) {
				new CFWLog(logger)
					.silent(true)
					.severe("Error on forced closing of DB connection.", e);
			}
		}
		
		if(counter > 0) {
			new CFWLog(logger)
				.silent(true)
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
	protected void removeOpenConnection(Connection connection) {	
		
		if(myOpenConnections.get() == null) {
			return;
		}
		myOpenConnections.get().remove(connection);
	}
	
	/********************************************************************************************
	 * Returns if a DB transaction was already started in the current thread.
	 * 
	 * @throws SQLException 
	 ********************************************************************************************/
	public boolean transactionIsStarted() {	
		return transactionConnection.get() != null;
	}
	
	/********************************************************************************************
	 * Starts a new transaction.
	 * 
	 * @throws SQLException 
	 ********************************************************************************************/
	public void transactionStart() {	
		
		if(transactionConnection.get() != null) {
			new CFWLog(logger)
				.severe("A transaction was already started for this thread. Use commitTransaction() before starting another one.", new Throwable());
			return;
		}
		
		try {
			Connection con = this.getConnection();
			con.setAutoCommit(false);
			transactionConnection.set(con);
			addOpenConnection(con);
			new CFWLog(logger).finer("DB transaction started.");
			
		} catch (SQLException e) {
			new CFWLog(logger)
				.severe("Error while retrieving DB connection.", e);
		}
		
	}
	
	/********************************************************************************************
	 * Commits a new transaction.
	 * 
	 * @throws SQLException 
	 ********************************************************************************************/
	public void transactionEnd(boolean isSuccess) {
		if(isSuccess) {
			transactionCommit();
		}else {
			transactionRollback();
		}
	}
	/********************************************************************************************
	 * Commits the transaction started with transactionStart.
	 * 
	 * @throws SQLException 
	 ********************************************************************************************/
	public void transactionCommit() {	
		
		
		if(transactionConnection.get() == null) {
			new CFWLog(logger)
				.finer("There is no running transaction. Use beginTransaction() before using commit.");
			return;
		}
		
		Connection con = null;
		
		try {
			con = transactionConnection.get();
			con.commit();
			new CFWLog(logger).finer("DB transaction committed.");
		} catch (SQLException e) {
			new CFWLog(logger)
				.severe("Error occured on commit transaction.", e);
		} finally {
			transactionConnection.remove();
			if(con != null) { 
				try {
					removeOpenConnection(con);
					con.setAutoCommit(true);
					con.close();
				} catch (SQLException e) {
					new CFWLog(logger)
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
	public void transactionRollback() {	
		
		
		if(transactionConnection.get() == null) {
			new CFWLog(logger)
				.finer("There is no running transaction. Use beginTransaction() before using commit.");
			return;
		}
		
		Connection con = null;
		
		try {
			con = transactionConnection.get();
			con.rollback();
			new CFWLog(logger)
				.finer("DB transaction rolled back.");
		} catch (SQLException e) {
			new CFWLog(logger)
				.severe("Error occured on rollback transaction.", e);
		} finally {
			transactionConnection.remove();
			if(con != null) { 
				try {
					con.setAutoCommit(true);
					con.close();
					removeOpenConnection(con);
				} catch (SQLException e) {
					new CFWLog(logger)
						.severe("Error occured closing DB resources.", e);
				}
				
			}
		}
		
	}
	
	/********************************************************************************************
	 * 
	 ********************************************************************************************/
	private void increaseDBCallsCount(Connection conn, boolean isError) {
		if(conn != null) {
			
			if(!isError) {
				dbcallCounter.labels(InterfaceName).inc();
			}else {
				dbcallErrorCounter.labels(InterfaceName).inc();
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
        
		CFWLog log = new CFWLog(logger).start();
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
			prepareStatement(prepared, values);
			
			//-----------------------------------------
			// Execute
			boolean isResultSet = prepared.execute();

			if(!isResultSet && prepared.getUpdateCount() > 0) {
				result = true;
			}
			increaseDBCallsCount(conn, false);
			
		} catch (SQLException e) {
			increaseDBCallsCount(conn, true);
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
		
		log.custom("sql", sql).end(Level.FINE);
		return result;
	}
	
	
	/********************************************************************************************
	 * 
	 * @param sql string with placeholders
	 * @param values the values to be placed in the prepared statement
	 * @return int number of updated rows, -1 in case of error
	 ********************************************************************************************/
	public int unpreparedExecuteBatch(String sql){	
		
		int totalRows = -1;
		
		CFWLog log = new CFWLog(logger).start();
		Connection conn = null;
		PreparedStatement prepared = null;
		
		try {
			//-----------------------------------------
			// Initialize Variables
			conn = this.getConnection();
			Statement statement = conn.createStatement();
			statement.addBatch(sql);

			//-----------------------------------------
			// Execute
			int[] resultCounts = statement.executeBatch();
			
			for(int i : resultCounts) {
				if(i >= 0) {
					totalRows += i;
				}
			}
			increaseDBCallsCount(conn, false);
			
		} catch (SQLException e) {
			increaseDBCallsCount(conn, true);
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
		
		log.custom("sql", sql).end(Level.FINE);
		return totalRows;
	}
	/********************************************************************************************
	 * 
	 * @param sql string with placeholders
	 * @param values the values to be placed in the prepared statement
	 * @return int number of updated rows, -99 in case of error
	 ********************************************************************************************/
	public int preparedExecuteBatch(String sql, Object... values){	
		
		int totalRows = -99;
		
		CFWLog log = new CFWLog(logger).start();
		Connection conn = null;
		PreparedStatement prepared = null;

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

			totalRows = 0;
			for(int i : resultCounts) {
				if(i >= 0) {
					int currentCount = resultCounts[i];
					if( currentCount != Statement.SUCCESS_NO_INFO
					&&  currentCount != Statement.EXECUTE_FAILED) {
						totalRows += i;
					}
				}
			}
			increaseDBCallsCount(conn, false);

		} catch (SQLException e) {
			increaseDBCallsCount(conn, true);
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
		
		log.custom("sql", sql).end(Level.FINE);
		return totalRows;
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
        
		CFWLog log = new CFWLog(logger).start();
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
			increaseDBCallsCount(conn, false);
		} catch (SQLException e) {
			increaseDBCallsCount(conn, true);
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
		
		log.custom("sql", sql).end(Level.FINE);
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
			increaseDBCallsCount(conn, false);
		} catch (SQLException e) {
			increaseDBCallsCount(conn, true);
			log.silent(isSilent)
				.severe("Issue executing prepared statement: "+e.getLocalizedMessage(), e);
			try {
				if(conn != null && transactionConnection.get() == null) { 
					removeOpenConnection(conn);
					conn.close(); 
				}
				if(prepared != null) { prepared.close(); }
			} catch (SQLException e2) {
				log.silent(isSilent)
					.severe("Issue closing resources.", e2);
			}
		} 
		
		log.custom("sql", sql).end(Level.FINE);
				 
		return result;
	}
	
	/********************************************************************************************
	 * Returns the CFWResultSet.
	 * Note: This adjusted copy of preparedExecuteQuery() is very similar. Because of performance
	 * it was decided to accept this kind of code redundancy.
	 * 
	 * @param isSilent write errors to log but do not propagate to client
	 * @param sql string with placeholders
	 * @param values the values to be placed in the prepared statement
	 ********************************************************************************************/
	public CFWResultSet preparedExecuteQueryCFWResultSet(boolean isSilent, String sql, Object... values){	
        
		CFWLog log = new CFWLog(logger)
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
			increaseDBCallsCount(conn, false);
			
			CFWResultSet cfwResult = new CFWResultSet(this, true);
			
			cfwResult.connection(conn)
				     .isSilent(isSilent)
					 .isResultSet(true) 
					 .preparedStatement(prepared) 
					 .sqlString(sql)
					 .values(values) 
					 .executionResult(true) 
					 .updateCount(-1) 
					 ;
			
			return cfwResult;
			
		} catch (SQLException e) {
			increaseDBCallsCount(conn, true);
			log.silent(isSilent)
				.severe("Issue executing prepared statement: "+e.getLocalizedMessage(), e);
			try {
				if(conn != null && transactionConnection.get() == null) { 
					removeOpenConnection(conn);
					conn.close(); 
				}
				if(prepared != null) { prepared.close(); }
			} catch (SQLException e2) {
				log.silent(isSilent)
					.severe("Issue closing resources.", e2);
			}
		} 
		
		log.custom("sql", sql).end(Level.FINE);
				 
		return new CFWResultSet(this, false);
	}

	/********************************************************************************************
	 * Returns the CFWResultSet.
	 * Note: This adjusted copy of preparedExecute() is very similar. Because of performance
	 * it was decided to accept this kind of code redundancy.
	 * 
	 * @param request HttpServletRequest containing session data used for logging information(null allowed).
	 * @param sql string with placeholders
	 * @param values the values to be placed in the prepared statement
	 * @return true if update count is > 0, false otherwise
	 ********************************************************************************************/
	public CFWResultSet preparedExecuteCFWResultSet(String sql, Object... values){	
        
		CFWLog log = new CFWLog(logger).start();
		Connection conn = null;
		PreparedStatement prepared = null;
				
		try {
			//-----------------------------------------
			// Initialize Variables
			conn = this.getConnection();
			
			prepared = conn.prepareStatement(sql);
			
			//-----------------------------------------
			// Prepare Statement
			prepareStatement(prepared, values);
			
			//-----------------------------------------
			// Execute
			boolean isResultSet = prepared.execute();
			int  updateCount = prepared.getUpdateCount();
			boolean result = false;
			
			if(!isResultSet && updateCount > 0) {
				result = true;
			}
			increaseDBCallsCount(conn, false);
			
			CFWResultSet cfwResult = new CFWResultSet(this, true);
			
			cfwResult.connection(conn)
					 .isResultSet(isResultSet) 
					 .preparedStatement(prepared) 
					 .sqlString(sql)
					 .values(values) 
					 .executionResult(result) 
					 .updateCount(updateCount ) 
					 ;

			log.custom("sql", sql).end(Level.FINE);
			return cfwResult;
			
		} catch (SQLException e) {
			increaseDBCallsCount(conn, true);
			log.severe("Database Error: "+e.getMessage(), e);
			
			try {
				if(conn != null && transactionConnection.get() == null) { 
					removeOpenConnection(conn);
					conn.close(); 
				}
				if(prepared != null) { prepared.close(); }
			} catch (SQLException e2) {
				log.severe("Issue closing resources.", e2);
			}
		} 
		
		log.custom("sql", sql).end(Level.FINE);
		return new CFWResultSet(this, false);
	}
	
	/********************************************************************************************
	 * 
	 * @param request HttpServletRequest containing session data used for logging information(null allowed).
	 * @param sql string with placeholders
	 * @param values the values to be placed in the prepared statement. Supports String, Integer,
	 *               Boolean, Float, Date, Timestamp, Blob, Clob, Byte
	 * @throws SQLException 
	 ********************************************************************************************/
	@SuppressWarnings("rawtypes")
	public static void prepareStatement(PreparedStatement prepared, Object... values) throws SQLException{
		
		try {
			if(values != null) {
				for(int i = 1; i <= values.length ; i++) {
					Object currentValue = values[i-1];
					// TODO: Could be a better/faster solution: prepared.setObject(i+1, currentValue);
	
					if		(currentValue instanceof String) 		{ prepared.setString(i, (String)currentValue); }
					else if	(currentValue instanceof StringBuilder) { prepared.setString(i, currentValue.toString() ); }
					else if	(currentValue instanceof char[]) 		{ prepared.setString(i, new String((char[])currentValue)); }
					else if (currentValue instanceof Integer) 		{ prepared.setInt(i, (Integer)currentValue); }
					else if (currentValue instanceof Boolean) 		{ prepared.setBoolean(i, (Boolean)currentValue); }
					else if (currentValue == null) 					{ prepared.setNull(i, Types.NULL); }
					else if (currentValue instanceof Long) 			{ prepared.setLong(i, (Long)currentValue); }
					else if (currentValue instanceof Float) 		{ prepared.setFloat(i, (Float)currentValue); }
					else if (currentValue instanceof BigDecimal) 	{ prepared.setBigDecimal(i, (BigDecimal)currentValue); }
					else if (currentValue instanceof Date) 			{ prepared.setDate(i, (Date)currentValue); }
					else if (currentValue instanceof Timestamp) 	{ prepared.setTimestamp(i, (Timestamp)currentValue); }
					else if (currentValue instanceof Blob) 			{ prepared.setBlob(i, (Blob)currentValue); }
					else if (currentValue instanceof Clob) 			{ prepared.setClob(i, (Clob)currentValue); }
					else if (currentValue instanceof Byte) 			{ prepared.setByte(i, (Byte)currentValue); }
					else if (currentValue instanceof ArrayList) 	{ prepared.setArray(i, prepared.getConnection().createArrayOf("VARCHAR", ((ArrayList)currentValue).toArray() )); }
					else if (currentValue instanceof InputStream) 	{ prepared.setBinaryStream(i, (InputStream)currentValue); }
					else if (currentValue instanceof Integer[]) 	{ prepared.setArray(i, prepared.getConnection().createArrayOf("INTEGER", (Integer[])currentValue)); }
					else if (currentValue instanceof Object[]) 		{ prepared.setArray(i, prepared.getConnection().createArrayOf("VARCHAR", (Object[])currentValue)); }
					else if (currentValue instanceof LinkedHashMap)	{ prepared.setString(i, CFW.JSON.toJSON(currentValue)); }
					else if (  currentValue instanceof CFWChartSettings
							|| currentValue instanceof CFWSchedule
							|| currentValue instanceof CFWTimeframe)	{ prepared.setString(i, CFW.JSON.toJSON(currentValue)); }
					else if (currentValue.getClass().isEnum()) 		{ prepared.setString(i, currentValue.toString());}
					else { throw new RuntimeException("Unsupported database field type: "+ currentValue.getClass().getName());}
				}
			}
		}catch(Exception e){
			//do this to also log below when an error occurs
			throw e;
		}finally {
			if(logger.isLoggable(Level.FINEST) && prepared != null ) {
				new CFWLog(logger)
					.custom("preparedSQL", prepared.toString())
					.finest("Debug: Prepared Statement");
			}
		}

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
			}
		} catch (SQLException e) {
			new CFWLog(logger)
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
			&& resultSet.getStatement() != null 
			&& !resultSet.getStatement().isClosed()) {
				
				removeOpenConnection(resultSet.getStatement().getConnection());
				
				if(!resultSet.getStatement().getConnection().isClosed()) {
					resultSet.getStatement().getConnection().close();
					resultSet.close();
				}
			}
		} catch (SQLException e) {
			new CFWLog(logger)
				.severe("Exception occured while closing ResultSet. ", e);
		}
	}
	
	
	/************************************************************************
	 * Returns the list of available JDBC drivers.
	 * A combination of already registered drivers and the ones specified 
	 * in the application configuration.
	 ************************************************************************/
	public static Set<String> getListofDriverClassnames() {
		
		TreeSet<String> driverSet = new TreeSet<>();
		
		Enumeration<Driver> e = DriverManager.getDrivers();
		while(e.hasMoreElements()) {
			driverSet.add(e.nextElement().getClass().getName());
		}
		
		driverSet.addAll(
				CFW.DB.Config.getConfigAsArrayList(FeatureConfig.CATEGORY_DATABASE, FeatureConfig.CONFIG_DB_DRIVERS)
		);
		
		return driverSet;
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static DBInterface createDBInterfaceH2(String servername, int port, String storePath, String databaseName, String username, String password) {
		
		String urlPart = servername+":"+port+"/"+storePath+"/"+databaseName;
		String uniqueName = "H2:"+urlPart;
		String connectionURL = "jdbc:h2:tcp://"+urlPart+";MODE=MYSQL;IGNORECASE=TRUE";
		String driverClass = "org.h2.Driver";

		return createDBInterface(
				uniqueName, 
				driverClass, 
				connectionURL, 
				username, 
				password);
		
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static DBInterface createDBInterfaceH2AutoServer(int port, String storePath, String databaseName, String username, String password) {
		
		String urlPart = storePath+"/"+databaseName;
		String uniqueName = "H2:"+urlPart;
		String connectionURL = "jdbc:h2:"+urlPart+";IGNORECASE=TRUE;AUTO_SERVER=TRUE;AUTO_SERVER_PORT="+port;
		String driverClass = "org.h2.Driver";

		return createDBInterface(
				uniqueName, 
				driverClass, 
				connectionURL, 
				username, 
				password);
		
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static DBInterface createDBInterfaceMySQL(String uniqueNamePrefix, String servername, int port, String dbName, String username, String password) {
		
		
		String urlPart = servername+":"+port+"/"+dbName;
		String uniqueName = uniqueNamePrefix+"MySQL:"+servername+":"+port;
		String connectionURL = "jdbc:mysql://"+urlPart;
		String driverClass = "com.mysql.cj.jdbc.Driver";
		
		return createDBInterface(
				uniqueName, 
				driverClass, 
				connectionURL, 
				username, 
				password);
		
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static DBInterface createDBInterfacePostgres(String uniqueNamePrefix, String servername, int port, String dbName, String username, String password) {
		
		String urlPart = servername+":"+port+"/"+dbName;
		String uniqueName = uniqueNamePrefix+"MySQL:"+servername+":"+port;
		String connectionURL = "jdbc:postgresql://"+urlPart;
		String driverClass = "org.postgresql.Driver";

		return createDBInterface(
				uniqueName, 
				driverClass, 
				connectionURL, 
				username, 
				password);
		
	}


	/************************************************************************
	 * 
	 ************************************************************************/
	public static DBInterface createDBInterfaceMSSQL(String uniqueNamePrefix, String servername, int port, String dbName, String username, String password) {
		
		String urlPart = servername+":"+port+";databaseName="+dbName;
		String uniqueName = uniqueNamePrefix+":MSSQL:"+servername+":"+port;
		String connectionURL = "jdbc:sqlserver://"+urlPart;
		String driverClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		
		return createDBInterface(
				uniqueName, 
				driverClass, 
				connectionURL, 
				username, 
				password);
		
	}


	/************************************************************************
	 * 
	 ************************************************************************/
	public static DBInterface createDBInterfaceOracle(String uniqueNamePrefix, String servername, int port, String name, String type, String username, String password) {
		
		String urlPart = "";
		if(type.trim().equals("SID")) {
			//jdbc:oracle:thin:@myHost:myport:sid
			urlPart = servername+":"+port+":"+name;
		}else {
			//jdbc:oracle:thin:@//myHost:1521/service_name
			urlPart = servername+":"+port+"/"+name;
		}

		String uniqueName = uniqueNamePrefix+":Oracle:"+servername+":"+port;
		String connectionURL = "jdbc:oracle:thin:@"+urlPart;
		String driverClass = "oracle.jdbc.OracleDriver";
		String validationQuery = null;
		
		return createDBInterface(
				uniqueName, 
				driverClass, 
				connectionURL, 
				username, 
				password,
				validationQuery);
		
	}
	
	/************************************************************************
	 * Creates a DBInterface with a pooled datasource with a default validation
	 * query 'SELECT 1'.
	 * Adds the connection pool to the Connection pool management.
	 * Sets default connection pool settings.
	 * 
	 * @return DBInterface
	 * 
	 ************************************************************************/
	public static DBInterface createDBInterface(String uniquepoolName, String driverName, String url, String username, String password) {
		return createDBInterface(uniquepoolName, driverName, url, username, password, "SELECT 1");
	}

	/************************************************************************
	 * Creates a DBInterface with a pooled datasource.
	 * Adds the connection pool to the Connection pool management.
	 * Sets default connection pool settings.
	 * 
	 * @return DBInterface
	 * 
	 ************************************************************************/
	public static DBInterface createDBInterface(String uniquepoolName, String driverName, String url, String username, String password, String validationQuery) {
		
		BasicDataSource datasource;
		
		//Driver name com.microsoft.sqlserver.jdbc.SQLServerDriver
		//Connection URL Example: "jdbc:sqlserver://localhost:1433;databaseName=AdventureWorks;user=MyUserName;password=*****;";  
		datasource = new BasicDataSource();
		
		datasource.setDriverClassName(driverName);
		datasource.setUrl(url);	
		
		// try to recover when DB connection was lost
		datasource.setRemoveAbandonedOnBorrow(true);
		datasource.setRemoveAbandonedTimeout(60);
		datasource.setTestOnBorrow(true);
		
		
		if(validationQuery != null) {
			datasource.setValidationQuery(validationQuery);
		}
		
		datasource.setUsername(username);
		datasource.setPassword(password);
		
		DBInterface.setDefaultConnectionPoolSettings(datasource);
		
		//----------------------------------
		// Test connection
		//pooledSource.setLoginTimeout(5);
		DBInterface db = new DBInterface(uniquepoolName, datasource);
		
		if ( db.checkCanConnect() ) {
			DBInterface.registerManagedConnectionPool(uniquepoolName, datasource);
			new CFWLog(logger).off("Created DBInteface: "+ url);
		}

		return db;
	}
	
	/********************************************************************************************
	 * Checks if the DBInterface can connect with the given pool.
	 ********************************************************************************************/
	public boolean checkCanConnect() {
		Connection connection = null;
		try {
			
			if(pooledSource == null) {
				return false;
			}
			connection = pooledSource.getConnection();
		} catch (Exception e) {
			new CFWLog(logger).severe("Failed to connect to database: "+e.getMessage(), e);
			return false;
		}finally {
			if(connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					new CFWLog(logger).severe("Failed to close connection.", e);
				}
			}
		}
		
		return true;
	}
	
	/********************************************************************************************
	 *
	 ********************************************************************************************/
	public static void setDefaultConnectionPoolSettings(BasicDataSource pooledSource) {
		pooledSource.setMaxConnLifetimeMillis(60*60*1000);
		pooledSource.setTimeBetweenEvictionRunsMillis(5*60*1000);
		pooledSource.setInitialSize(10);
		pooledSource.setMinIdle(10);
		pooledSource.setMaxIdle(70);
		pooledSource.setMaxTotal(90);
		pooledSource.setMaxOpenPreparedStatements(100);
	}
	
	/********************************************************************************************
	 * Add a connection pool as a managed connection pool.
	 * The connection pool will show up in the Database Analytics.
	 * 
	 ********************************************************************************************/
	public static void registerManagedConnectionPool(String uniqueName, BasicDataSource datasource) {	
		
		if(!managedConnectionPools.containsKey(uniqueName)) {
			managedConnectionPools.put(uniqueName, datasource);
		}else {
			removeManagedConnectionPool(uniqueName);
			managedConnectionPools.put(uniqueName, datasource);
			
			new CFWLog(logger).silent(true).info("A connection pool with the name '"+uniqueName+"' was already registered and was updated.");
		}
		
			
		
	}
	
	/********************************************************************************************
	 * Remove connection pool from the managed connection pools.
	 ********************************************************************************************/
	public static void removeManagedConnectionPool(String uniqueName) {	
		BasicDataSource removedPool = managedConnectionPools.remove(uniqueName);	
		// issue with AWA, to be investigated.
//		try {
//			removedPool.close();
//		} catch (SQLException e) {
//			new CFWLog(logger).silent(true).severe("Error closing connection pool: "+e.getMessage(), e);
//		}
	}
	
	/********************************************************************************************
	 * Remove connection pool from the managed connection pools.
	 * 
	 * @throws SQLException 
	 ********************************************************************************************/
	public static JsonArray getConnectionPoolStatsAsJSON() {	
		
		JsonArray result = new JsonArray();
		for(Entry<String, BasicDataSource> entry : managedConnectionPools.entrySet()) {
			
			JsonObject stats = new JsonObject();
			
			BasicDataSource source = entry.getValue();
			
			stats.addProperty("NAME", entry.getKey());
			stats.addProperty("MAX_CONNECTION_LIFETIME", source.getMaxConnLifetimeMillis());
			stats.addProperty("EVICTION_INTERVAL", source.getTimeBetweenEvictionRunsMillis());
			stats.addProperty("MIN_IDLE_CONNECTIONS", source.getMinIdle());
			stats.addProperty("MAX_IDLE_CONNECTIONS", source.getMaxIdle());
			stats.addProperty("MAX_TOTAL_CONNECTIONS", source.getMaxTotal());
			stats.addProperty("IDLE_COUNT", source.getNumIdle());
			stats.addProperty("ACTIVE_COUNT", source.getNumActive());
			
			result.add(stats);
		}
		
		return result;
	}

}
