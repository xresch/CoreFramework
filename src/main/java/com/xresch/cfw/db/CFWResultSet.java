package com.xresch.cfw.db;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.utils.ResultSetUtils;
import com.xresch.cfw.utils.ResultSetUtils.ResultSetAsJsonReader;

public class CFWResultSet {
	
	private static Logger logger = CFWLog.getLogger(CFWResultSet.class.getName());
	
	private DBInterface dbInterface;
	
	private String sqlString;
	private Object[] values;
	
	private int updateCount = -999;
	private Connection connection;
	private PreparedStatement prepared;
	
	
	private boolean isSuccess = false;
	private boolean isResultSet = false;
	private boolean executionResult = false;
	
	private boolean isSilent = false;
	
	/***********************************************************************************
	 *
	 ***********************************************************************************/
	public CFWResultSet(DBInterface dbInterface, boolean isSuccess) {
		this.dbInterface = dbInterface;
		this.isSuccess = isSuccess;
	}
	
	/***********************************************************************************
	 *
	 ***********************************************************************************/
	public boolean isSuccess() {
		return this.isSuccess;
	}
	
	/***********************************************************************************
	 *
	 ***********************************************************************************/
	public CFWResultSet connection(Connection value) {
		this.connection = value;
		return this;
	}
	
	/***********************************************************************************
	 *
	 ***********************************************************************************/
	public Connection connection() {
		return this.connection;
	}
	
	/***********************************************************************************
	 * Close the connection.
	 ***********************************************************************************/
	public void close() {
		CFW.DB.close(connection);
	}
	
	/***********************************************************************************
	 *
	 ***********************************************************************************/
	public CFWResultSet isResultSet(boolean value) {
		this.isResultSet = value;
		return this;
	}
	
	/***********************************************************************************
	 *
	 ***********************************************************************************/
	public boolean isResultSet() {
		return this.isResultSet;
	}
	
	/***********************************************************************************
	 *
	 ***********************************************************************************/
	public ResultSet getResultSet() {
		if(prepared == null || !isResultSet) {
			return null;
		}
		
		try {
			return prepared.getResultSet();
			
		} catch (SQLException e) {

			new CFWLog(logger).silent(isSilent).severe("Issue executing prepared statement: "+e.getLocalizedMessage(), e);
			try {
				if(connection != null && dbInterface.transactionConnection.get() == null ) { 
					dbInterface.removeOpenConnection(connection);
					connection.close(); 
				}
				if(prepared != null) { prepared.close(); }
			} catch (SQLException e2) {
				new CFWLog(logger).silent(isSilent)
					.severe("Issue closing resources.", e2);
			}
		} 
		
		return null;
	}
	
	/***********************************************************************************
	 * Streams bytes from a column to the defined output stream.
	 * @param columnName name of the column
	 * @param out the output stream
	 * @return true if successful or when result was empty, false on errors
	 ***********************************************************************************/
	public boolean streamBytes(Object columnName, OutputStream out) {
		
		if(prepared == null || !isResultSet) {
			return true;
		}
		
		try {
			
			ResultSet resultSet = prepared.getResultSet();
			
			 while (resultSet.next()) {
                InputStream input = resultSet.getBinaryStream(columnName.toString());
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) > 0) {
                    out.write(buffer, 0, bytesRead);
                }
            }
	           return true;
			
		} catch (Exception e) {

			new CFWLog(logger).silent(isSilent).severe("Issue streaming bytes to output stream: "+e.getLocalizedMessage(), e);
			try {
				if(connection != null && dbInterface.transactionConnection.get() == null ) { 
					dbInterface.removeOpenConnection(connection);
					connection.close(); 
				}
				if(prepared != null) { prepared.close(); }
			} catch (SQLException e2) {
				new CFWLog(logger).silent(isSilent)
					.severe("Issue closing resources.", e2);
			}
		} 
		
		return false;
	}
	
	
	/***********************************************************************************
	 *
	 ***********************************************************************************/
	public ResultSetAsJsonReader toJSONReader() {
		return ResultSetUtils.toJSONReader(getResultSet());
	}
	
	/***********************************************************************************
	 * INTERNAL USE ONLY
	 * Set isSilent, if true, write errors to log but do not propagate them to client.
	 ***********************************************************************************/
	public CFWResultSet isSilent(boolean value) {
		this.isSilent = value;
		return this;
	}
	
	/***********************************************************************************
	 * Retrieve isSilent, if true, write errors to log but do not propagate them to client.
	 ***********************************************************************************/
	public boolean isSilent() {
		return this.isSilent;
	}

	/***********************************************************************************
	 * INTERNAL USE ONLY
	 * Set the update count for update queries.
	 ***********************************************************************************/
	public CFWResultSet updateCount(int value) {
		this.updateCount = value;
		return this;
	}
	
	/***********************************************************************************
	 * 
	 * @return number of rows updated, -999 if count was not set
	 ***********************************************************************************/
	public int updateCount() {
		return this.updateCount;
	}

	/***********************************************************************************
	 *
	 ***********************************************************************************/
	public CFWResultSet sqlString(String value) {
		this.sqlString = value;
		return this;
	}
	
	/***********************************************************************************
	 *
	 ***********************************************************************************/
	public String sqlString() {
		return this.sqlString;
	}
	
	/***********************************************************************************
	 *
	 ***********************************************************************************/
	public CFWResultSet values(Object[] values) {
		this.values = values;
		return this;
	}
	
	/***********************************************************************************
	 *
	 ***********************************************************************************/
	public Object[] values() {
		return this.values;
	}
	
	/***********************************************************************************
	 *
	 ***********************************************************************************/
	public CFWResultSet executionResult(boolean value) {
		this.executionResult = value;
		return this;
	}
	
	/***********************************************************************************
	 *
	 ***********************************************************************************/
	public boolean executionResult() {
		return this.executionResult;
	}
	
	/***********************************************************************************
	 *
	 ***********************************************************************************/
	public CFWResultSet preparedStatement(PreparedStatement value) {
		this.prepared = value;
		return this;
	}
	
	/***********************************************************************************
	 *
	 ***********************************************************************************/
	public PreparedStatement preparedStatement() {
		return this.prepared;
	}


	
}
