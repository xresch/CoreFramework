package com.xresch.cfw.db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Logger;

import org.apache.commons.dbcp2.BasicDataSource;
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
import com.xresch.cfw.utils.ResultSetUtils;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDB {

	//private static BasicDataSource pooledSource;
	//private static JdbcConnectionPool connectionPool;
	private static Server server;
	private static boolean isInitialized = false;

	private static DBInterface db;
	private static Logger logger = CFWLog.getLogger(CFWDB.class.getName());
	
	/********************************************************************************************
	 *
	 ********************************************************************************************/
	public static void initializeDB() {
		
		if(isInitialized) {
			new CFWLog(logger)
				.warn("Database was already initialized.");
			return;
		}
    	//---------------------------------------
    	// Get variables
		String mode 			= CFW.Properties.MODE;
		boolean doStartAsDBServer = mode.contains(CFW.MODE_DB);
		String server 			= CFWProperties.DB_SERVER;
		String storePath 		= CFWProperties.DB_STORE_PATH;
		String databaseName		= CFWProperties.DB_NAME;
		int port 				= CFWProperties.DB_PORT;
		String username			= CFWProperties.DB_USERNAME;
		String password			= CFWProperties.DB_PASSWORD;
		

		//---------------------------------------
    	// Create Folder  

		File datastoreFolder = new File(storePath);
    	if(!datastoreFolder.isDirectory()) {
    		datastoreFolder.mkdir();
    	}
	
    	//---------------------------------------
    	// Create File
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
		
		
		
		if(!doStartAsDBServer) {
			//---------------------------------------
	    	// Start as all in one, first application
			// instance accessing db will create a server
			db = DBInterface.createDBInterfaceH2AutoServer(port, storePath, databaseName, username, password);
			CFWDB.isInitialized = true;
			initializeFullTextSearch();
		}else {

				System.out.println("Bla");
				//---------------------------------------
		    	// Start as DB only instance
				//CFWDB.server = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "" +port).start();
				db = DBInterface.createDBInterfaceH2AutoServer(port, storePath, databaseName, username, password);
				db.preparedExecute("SELECT 1 FROM DUAL");
				CFWDB.isInitialized = true;
				initializeFullTextSearch();
		}
	}
		
	/********************************************************************************************
	 *
	 ********************************************************************************************/
	public static void stopDBServer() {
		if(server != null && server.isRunning(false)) {
			server.stop();
		}
	}
	
	
	/********************************************************************************************
	 *
	 ********************************************************************************************/
	private static void initializeFullTextSearch() {
		preparedExecute("CREATE ALIAS IF NOT EXISTS FTL_INIT FOR \"org.h2.fulltext.FullTextLucene.init\"");
		preparedExecute("CALL FTL_INIT()");
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
