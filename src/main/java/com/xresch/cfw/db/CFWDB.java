package com.xresch.cfw.db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.logging.Logger;

import org.h2.tools.Server;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
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
		boolean doStartDBServer = (CFW.Properties.DB_MODE.toUpperCase().equals("SERVER") || mode.contains(CFW.MODE_DATABASE));
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
		
		boolean dbFileExisted = true;
    	if(!datastoreFile.isFile()) {
    		dbFileExisted = false;
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
    	
    	//---------------------------------------
    	// Upgrade DB File if necessary
    	
    	// this does not really work
    	
//		if(dbFileExisted) {
//	    	int oldVersion = 212;
//	    	
//	    	String urlPart = server+":"+port+"/"+storePath+"/"+databaseName;
//			String connectionURL = "jdbc:h2:tcp://"+urlPart+";MODE=MYSQL;IGNORECASE=TRUE";
//			Properties connectionInfo = new Properties();
//			connectionInfo.put("user", username);
//			connectionInfo.put("password", password);
//	
//	    	try {
//				Upgrade.upgrade(connectionURL, connectionInfo, oldVersion);
//			} catch (Exception e) {
//				new CFWLog(logger).warn("Exception encountered while upgrading database file: ", e);
//				;
//			}
//		}
//    	
    	//---------------------------------------
    	// Start Database
		if(doStartDBServer) {
			//---------------------------------------
	    	// Standalone DB Server
			try {
				CFWDB.server = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "" +port).start();
				db = DBInterface.createDBInterfaceH2(server, port, storePath, databaseName, username, password);
				
				CFWDBCustomH2Functions.initialize(db);
				initializeFullTextSearch();
				CFWDB.isInitialized = true;
			} catch (SQLException e) {
				new CFWLog(logger).severe("Error starting database server: "+e.getMessage(), e);
			}
			
		}else {
			//---------------------------------------
	    	// Start in MIXED mode, first application
			// instance accessing db will create a server
			db = DBInterface.createDBInterfaceH2AutoServer(port, storePath, databaseName, username, password);

			CFWDBCustomH2Functions.initialize(db);
			initializeFullTextSearch();
			
			CFWDB.isInitialized = true;
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
			
			String filePath = folderPath+filename+"_"+CFW.Time.formatDate(ZonedDateTime.now(),"YYYY-MM-dd_HH-mm")+".zip";
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
	public static boolean exportScript(String folderPath, String filename) {
		

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

			String filePath = folderPath+filename+"_"+CFW.Time.formatDate(ZonedDateTime.now(),"YYYY-MM-dd_HH-mm")+".sql";
			new CFWSQL(null).custom("SCRIPT DROP TO '"+filePath+"';")
				.execute();
			
			return true;
			
		}else {
			new CFWLog(logger)
				.severe("Exported script file could not be created, folder is not accessible: "+folderPath);
		}
		
		return false;
		
	}
	
	/********************************************************************************************
	 *
	 ********************************************************************************************/
	public static boolean importScript(String fullScriptFilePath) {
		
		return new CFWSQL(null).custom("RUNSCRIPT FROM '"+fullScriptFilePath+"';")
				.execute();
			
	}
	
	
	/********************************************************************************************
	 *
	 ********************************************************************************************/
	public static void resetAdminPW() {
		
		if(CFW.Properties.RESET_ADMIN_PW) {
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
	 * Return the DBInterface to the internal H2 Database.
	 * 
	 ********************************************************************************************/
	public static DBInterface getDBInterface() {	
		return db;
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
	public static boolean transactionIsStarted() {	
		return db.transactionIsStarted();
	}
	
	
	/********************************************************************************************
	 * Starts a new transaction.
	 * 
	 * @throws SQLException 
	 ********************************************************************************************/
	public static void transactionStart() {	
		db.transactionStart();
	}
	
	/********************************************************************************************
	 * Commits a new transaction.
	 * 
	 * @throws SQLException 
	 ********************************************************************************************/
	public static void transactionCommit() {	
		db.transactionCommit();
	}
	
	/********************************************************************************************
	 * Rollbacks the transaction.
	 * 
	 * @throws SQLException 
	 ********************************************************************************************/
	public static void transactionRollback() {	
		db.transactionRollback();
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
	public static int preparedExecuteBatch(String sql, Object... values){	
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
