package com.xresch.cfw.db;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.dbcp2.BasicDataSource;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWChartSettings;
import com.xresch.cfw.datahandling.CFWSchedule;
import com.xresch.cfw.datahandling.CFWStoredFileReferences;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.config.FeatureConfig;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.xrutils.database.XRDBInterface;

import io.prometheus.client.Counter;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class DBInterface extends XRDBInterface {

	private static Logger logger = CFWLog.getLogger(DBInterface.class.getName());
	
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
	
	public DBInterface(String interfaceName, BasicDataSource pooledSource) {
		super(pooledSource);

		this.InterfaceName = interfaceName;
	}
	
	
	
	
	/********************************************************************************************
	 * Add a connection that was openend to the list of open connections.
	 * When connections remain after the Servlet returns, they will be closed 
	 * by the RequestHandler using hardCloseRemainingConnections().
	 * 
	 * @throws SQLException 
	 ********************************************************************************************/
	 @Override
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
	 * 
	 ********************************************************************************************/
	@Override
	protected void increaseDBCallsCount(Connection conn, boolean isError) {
		if(conn != null) {
			
			if(!isError) {
				dbcallCounter.labels(InterfaceName).inc();
			}else {
				dbcallErrorCounter.labels(InterfaceName).inc();
			}
			
		}
	}	
		
	/********************************************************************************************
	 * Returns the result or null if there was any issue.
	 * Errors will be written to log but not be propagated to client.
	 * 
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
	@Override 
	protected ResultSet preparedExecuteQuery(boolean isSilent, String sql, Object... values){	
        
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
			prepareStatement(prepared, values);
			
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
	 * Prepares custom types for the SQL statement.
	 * 
	 * @param prepared the statements with ?-placeholders that should be prepared
	 * @param value the value to be placed in the prepared statement. Supports String, Integer,
	 *               Boolean, Float, Date, Timestamp, Blob, Clob, Byte
	 * @throws SQLException 
	 * 
	 * @return true if value was prepared, false otherwise
	 ********************************************************************************************/
	@Override
	public boolean prepareCustomTypes(PreparedStatement prepared, int index, Object currentValue) throws SQLException{
		
		if (  currentValue instanceof CFWChartSettings
				|| currentValue instanceof CFWSchedule
				|| currentValue instanceof CFWTimeframe
				|| currentValue instanceof CFWStoredFileReferences
		){ 
			prepared.setString(index, CFW.JSON.toJSON(currentValue)); 
			return true;
		}
		
		return false;
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
		
		XRDBInterface.setDefaultConnectionPoolSettings(datasource);
		
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
	

}
