package com.pengtoolbox.cfw.db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.Server;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFW.Properties;
import com.pengtoolbox.cfw._main.CFWProperties;
import com.pengtoolbox.cfw.features.usermgmt.Permission;
import com.pengtoolbox.cfw.features.usermgmt.Role;
import com.pengtoolbox.cfw.features.usermgmt.User;
import com.pengtoolbox.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
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
				datastoreFile.createNewFile();
			} catch (IOException e) {
				new CFWLog(logger)
				.method("startDatabase")
				.severe("Error creating database file.", e);
				
			}
    	}
    	
		String h2_url 		= "jdbc:h2:tcp://"+server+":"+port+"/"+storePath+"/"+databaseName;
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
							.method("getConnection")
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
				.method("initialize")
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
	public static void resetAdminPW() {
		
		if(Properties.RESET_ADMIN_PW) {
			User admin = CFW.DB.Users.selectByUsernameOrMail("admin");
			admin.setNewPassword("admin", "admin");
			
			if(CFW.DB.Users.update(admin)) {
				new CFWLog(logger)
				.method("resetAdminPW")
				.info("Admin password was reset to default!");
			}else{
				new CFWLog(logger)
				.method("resetAdminPW")
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
	 * Create Testdata for testing purposes
	 ********************************************************************************************/
	public static void createTestData() {
		
		Role testroleA, testroleB, testroleC;
		User testuserA, testuserB, testuserC;
		
		Permission  permissionA, permissionAA, permissionAAA, 
					permissionB, permissionBB,
					permissionC;
		//------------------------------
		// Roles
		CFW.DB.Roles.create(new Role("TestroleA", "user").description("This is the testrole A."));
		testroleA = CFW.DB.Roles.selectFirstByName("TestroleA");
		
		CFW.DB.Roles.create(new Role("TestroleB", "user").description("This is the testrole B."));
		testroleB = CFW.DB.Roles.selectFirstByName("TestroleB");
		
		CFW.DB.Roles.create(new Role("TestroleC", "user").description("This is the testrole C."));
		testroleC = CFW.DB.Roles.selectFirstByName("TestroleC");
		
		//------------------------------
		// Users
		CFW.DB.Users.create(new User("TestuserA")
				.setNewPassword("TestuserA", "TestuserA")
				.email("testuserA@cfwtest.com")
				.firstname("Testika")
				.lastname("Testonia"));
		testuserA = CFW.DB.Users.selectByUsernameOrMail("TestuserA");
		CFW.DB.UserRoleMap.addUserToRole(testuserA, testroleA, true);
		CFW.DB.UserRoleMap.addUserToRole(testuserA, testroleB, true);
		CFW.DB.UserRoleMap.addUserToRole(testuserA, testroleC, true);
		
		CFW.DB.Users.create(new User("TestuserB")
				.setNewPassword("TestuserB", "TestuserB")
				.email("testuserB@cfwtest.com")
				.firstname("Jane")
				.lastname("Doe"));
		testuserB = CFW.DB.Users.selectByUsernameOrMail("TestuserB");
		CFW.DB.UserRoleMap.addUserToRole(testuserB, testroleA, true);
		CFW.DB.UserRoleMap.addUserToRole(testuserB, testroleB, true);
		
		CFW.DB.Users.create(new User("TestuserC")
				.setNewPassword("TestuserC", "TestuserC")
				.email("testuserC@cfwtest.com")
				.firstname("Paola")
				.lastname("Pernandez"));	
		testuserC = CFW.DB.Users.selectByUsernameOrMail("TestuserC");
		CFW.DB.UserRoleMap.addUserToRole(testuserC, testroleC, true);
		
		//------------------------------
		// Permissions
		CFW.DB.Permissions.create(new Permission("PermissionA", "user").description("This is the permission A."));
		permissionA = CFW.DB.Permissions.selectByName("PermissionA");
		CFW.DB.RolePermissionMap.addPermissionToRole(permissionA, testroleA, true);
		
		CFW.DB.Permissions.create(new Permission("PermissionAA", "user").description("This is the permission AA."));
		permissionAA = CFW.DB.Permissions.selectByName("PermissionAA");
		CFW.DB.RolePermissionMap.addPermissionToRole(permissionAA, testroleA, true);
		
		CFW.DB.Permissions.create(new Permission("PermissionAAA", "user").description("This is the permission AAA."));
		permissionAAA = CFW.DB.Permissions.selectByName("PermissionAAA");
		CFW.DB.RolePermissionMap.addPermissionToRole(permissionAAA, testroleA, true);
		
		CFW.DB.Permissions.create(new Permission("PermissionB", "user").description("This is the permission B."));
		permissionB = CFW.DB.Permissions.selectByName("PermissionB");
		CFW.DB.RolePermissionMap.addPermissionToRole(permissionB, testroleB, true);
		
		CFW.DB.Permissions.create(new Permission("PermissionBB", "user").description("This is the permission BB."));
		permissionBB = CFW.DB.Permissions.selectByName("PermissionBB");
		CFW.DB.RolePermissionMap.addPermissionToRole(permissionBB, testroleB, true);
		
		CFW.DB.Permissions.create(new Permission("PermissionC", "user").description("This is the permission C."));
		permissionC = CFW.DB.Permissions.selectByName("PermissionC");
		CFW.DB.RolePermissionMap.addPermissionToRole(permissionC, testroleC, true);
	}

}
