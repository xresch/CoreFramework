package com.xresch.cfw.db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Logger;

import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.Server;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFW.Properties;
import com.xresch.cfw._main.CFWProperties;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDB {

	private static JdbcDataSource dataSource;
	private static JdbcConnectionPool connectionPool;
	private static Server server;
	private static boolean isInitialized = false;

	private static DBInterface db;
	private static Logger logger = CFWLog.getLogger(CFWDB.class.getName());
	
	/********************************************************************************************
	 *
	 ********************************************************************************************/
	public static void startDBServer() {
		
		if(isInitialized) {
			new CFWLog(logger)
				.warn("Database was already initialized.");
			return;
		}
    	//---------------------------------------
    	// Get variables
		String server 		= CFWProperties.DB_SERVER;
		String storePath 	= CFWProperties.DB_STORE_PATH;
		String databaseName	= CFWProperties.DB_NAME;
		int port 			= CFWProperties.DB_PORT;
		String username		= CFWProperties.DB_USERNAME;
		String password		= CFWProperties.DB_PASSWORD;
		
		//---------------------------------------
    	// Create Folder  
		File datastoreFolder = new File(storePath);
    	if(!datastoreFolder.isDirectory()) {
    		datastoreFolder.mkdir();
    	}
    	
    	//---------------------------------------
    	// Create Folder 
		File datastoreFile = new File(storePath+"/"+databaseName+".mv.db");
    	if(!datastoreFile.isFile()) {
    		try {
				if(!datastoreFile.createNewFile()) {
					new CFWLog(logger)
						.severe("Error creating database file.");
				}
			} catch (IOException e) {
				new CFWLog(logger)
				.severe("Error creating database file.", e);
				
			}
    	}
    	
		String h2_url 		= "jdbc:h2:tcp://"+server+":"+port+"/"+storePath+"/"+databaseName+";IGNORECASE=TRUE";
		new CFWLog(logger).info("H2 DB URL: "+ h2_url);
		
		try {
			
			CFWDB.server = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "" +port).start();
			
			connectionPool = JdbcConnectionPool.create(h2_url, username, password);
			connectionPool.setMaxConnections(90);

			CFWDB.dataSource = new JdbcDataSource();
			CFWDB.dataSource.setURL(h2_url);
			CFWDB.dataSource.setUser(username);
			CFWDB.dataSource.setPassword(password);
			
			CFWDB.isInitialized = true;
			
			db = new DBInterface() {

				@Override
				public Connection getConnection() throws SQLException {	
					
					if(isInitialized) {
						new CFWLog(logger)
							.finer("DB Connections Active: "+connectionPool.getActiveConnections());
						
						if(transactionConnection.get() != null) {
							return transactionConnection.get();
						}else {
							synchronized (connectionPool) {
								Connection connection = connectionPool.getConnection();
								addOpenConnection(connection);
								return connection;
							}
						}
					}else {
						throw new SQLException("DB not initialized, call CFWDB.initialize() first.");
					}
				}
				
			};
						
		} catch (SQLException e) {
			CFWDB.isInitialized = false;
			new CFWLog(CFWDB.logger)
				.severe("Issue initializing H2 Database.", e);
			e.printStackTrace();
		}
	}
	
	/********************************************************************************************
	 *
	 ********************************************************************************************/
	public static void stopDBServer() {
		server.stop();
	}
	
	/********************************************************************************************
	 *
	 ********************************************************************************************/
	public static boolean backupDatabaseFile(String folderPath, String filename) {
		

		if(Strings.isNullOrEmpty(folderPath)) {
			folderPath = "./backup/";
		}
		
		if(!folderPath.endsWith("/") && !folderPath.endsWith("\\") ) {
			folderPath = folderPath + "/";
		}
		
		File folder = new File(folderPath);
		folder.mkdirs();
		if(folder.isDirectory()
		&& folder.canWrite()) {
			
			String filePath = folderPath+filename+"_"+CFW.Time.formatDate(new Date(),"YYYY-MM-dd_HH-mm")+".zip";
			new CFWSQL(null).custom("BACKUP TO '"+filePath+"';")
				.execute();
			
			return true;
			
		}else {
			new CFWLog(logger)
				.severe("Database backup could not be created, folder is not accessible: "+folderPath);
		}
		
		return false;
		
	}
	
	
	/********************************************************************************************
	 *
	 ********************************************************************************************/
	public static void resetAdminPW() {
		
		if(Properties.RESET_ADMIN_PW) {
			User admin = CFW.DB.Users.selectByUsernameOrMail("admin");
			admin.setNewPassword("admin", "admin");
			
			if(CFW.DB.Users.update(admin)) {
				new CFWLog(logger)
				.info("Admin password was reset to default!");
			}else{
				new CFWLog(logger)
				.warn("Admin password was reset failed!");
			};
		}
	}
			
	
	/********************************************************************************************
	 * Add a connection that was openend to the list of open connections.
	 * When connections remain after the Servlet returns, they will be closed 
	 * by the RequestHandler using hardCloseRemainingConnections().
	 * 
	 * @throws SQLException 
	 ********************************************************************************************/
	public static void forceCloseRemainingConnections() {	
		db.forceCloseRemainingConnections();
	}
	
	
	/********************************************************************************************
	 * Starts a new transaction.
	 * 
	 * @throws SQLException 
	 ********************************************************************************************/
	public static void beginTransaction() {	
		db.beginTransaction();
	}
	
	/********************************************************************************************
	 * Commits a new transaction.
	 * 
	 * @throws SQLException 
	 ********************************************************************************************/
	public static void commitTransaction() {	
		db.commitTransaction();
	}
	
	/********************************************************************************************
	 * Rollbacks the transaction.
	 * 
	 * @throws SQLException 
	 ********************************************************************************************/
	public static void rollbackTransaction() {	
		db.rollbackTransaction();
	}
	
	/********************************************************************************************
	 * 
	 * @param request HttpServletRequest containing session data used for logging information(null allowed).
	 * @param sql string with placeholders
	 * @param values the values to be placed in the prepared statement
	 * @return true if update count is > 0, false otherwise
	 ********************************************************************************************/
	public static boolean preparedExecute(String sql, Object... values){	
        return db.preparedExecute(sql, values);
	}
	
	/********************************************************************************************
	 * 
	 * @param sql string with placeholders
	 * @param values the values to be placed in the prepared statement
	 * @return true if update count is > 0, false otherwise
	 ********************************************************************************************/
	public static boolean preparedExecuteBatch(String sql, Object... values){	
		return db.preparedExecuteBatch(sql, values);
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
	public static Integer preparedInsertGetKey(String sql, String generatedKeyName, Object... values){	
		return db.preparedInsertGetKey(sql, generatedKeyName, values);
	}
	
	/********************************************************************************************
	 * Returns the result or null if there was any issue.
	 * 
	 * @param request HttpServletRequest containing session data used for logging information(null allowed).
	 * @param sql string with placeholders
	 * @param values the values to be placed in the prepared statement
	 * @throws SQLException 
	 ********************************************************************************************/
	public static ResultSet preparedExecuteQuery(String sql, Object... values){	
		return db.preparedExecuteQuery(sql, values);
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
		DBInterface.prepareStatement(prepared, values);
	}
	
	/********************************************************************************************
	 * 
	 * @param request HttpServletRequest containing session data used for logging information(null allowed).
	 * @param resultSet which should be closed.
	 ********************************************************************************************/
	public static void close(Connection conn){
		db.close(conn);
	}
	/********************************************************************************************
	 * 
	 * @param request HttpServletRequest containing session data used for logging information(null allowed).
	 * @param resultSet which should be closed.
	 ********************************************************************************************/
	public static void close(ResultSet resultSet){
		db.close(resultSet);
	}

	/********************************************************************************************
	 * Returns a jsonString with an array containing a json object for each row.
	 * Returns an empty array in case of error.
	 * 
	 ********************************************************************************************/
	public static String resultSetToJSON(ResultSet resultSet) {
		return DBInterface.resultSetToJSON(resultSet);
	}
	
	/********************************************************************************************
	 * Returns a jsonString with an array containing a json object for each row.
	 * Returns an empty array in case of error.
	 * 
	 ********************************************************************************************/
	public static String resultSetToCSV(ResultSet resultSet, String delimiter) {
		return DBInterface.resultSetToCSV(resultSet, delimiter);
	}
	
	/********************************************************************************************
	 * Returns a jsonString with an array containing a json object for each row.
	 * Returns an empty array in case of error.
	 * 
	 ********************************************************************************************/
	public static String resultSetToXML(ResultSet resultSet) {
		return DBInterface.resultSetToXML(resultSet);
	}
	
	/********************************************************************************************
	 * 
	 ********************************************************************************************/
	public static String selectTableRowCountAsJSON() {
		
		return new CFWSQL(null)
			.custom("SELECT TABLE_NAME, COUNT_ROWS(table_name) AS ROW_COUNT" + 
					" FROM INFORMATION_SCHEMA.TABLES" + 
					" WHERE table_schema = 'PUBLIC'" + 
					" ORDER BY TABLE_NAME;")
			.getAsJSON();

	}
	
	/********************************************************************************************
	 * 
	 ********************************************************************************************/
	public static String selectQueryStatisticsAsJSON() {
		
		return new CFWSQL(null)
			.loadSQLResource(FeatureCore.RESOURCE_PACKAGE+".sql", "h2_query_statistics.sql")
			.getAsJSON();

	}


}
