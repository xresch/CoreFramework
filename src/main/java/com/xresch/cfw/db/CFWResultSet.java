package com.xresch.cfw.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

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
	
	public CFWResultSet(DBInterface dbInterface, boolean isSuccess) {
		this.dbInterface = dbInterface;
		this.isSuccess = isSuccess;
	}
	
	public boolean isSuccess() {
		return this.isSuccess;
	}
	
	public CFWResultSet connection(Connection value) {
		this.connection = value;
		return this;
	}
	
	public Connection connection() {
		return this.connection;
	}

	public CFWResultSet isResultSet(boolean value) {
		this.isResultSet = value;
		return this;
	}
	
	public boolean isResultSet() {
		return this.isResultSet;
	}
	
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
	
	public ResultSetAsJsonReader toJSONReader() {
		return ResultSetUtils.toJSONReader(getResultSet());
	}
	
	public CFWResultSet isSilent(boolean value) {
		this.isSilent = value;
		return this;
	}
	
	public boolean isSilent() {
		return this.isSilent;
	}

	public CFWResultSet updateCount(int value) {
		this.updateCount = value;
		return this;
	}
	
	/***********************************************************
	 * 
	 * @return number of rows updated, -999 if count was not set
	 ***********************************************************/
	public int updateCount() {
		return this.updateCount;
	}

	public CFWResultSet sqlString(String value) {
		this.sqlString = value;
		return this;
	}
	
	public String sqlString() {
		return this.sqlString;
	}

	public CFWResultSet values(Object[] values) {
		this.values = values;
		return this;
	}
	
	public Object[]  values() {
		return this.values;
	}

	public CFWResultSet executionResult(boolean value) {
		this.executionResult = value;
		return this;
	}
	
	public boolean executionResult() {
		return this.executionResult;
	}

	public CFWResultSet preparedStatement(PreparedStatement value) {
		this.prepared = value;
		return this;
	}
	
	public PreparedStatement preparedStatement() {
		return this.prepared;
	}


	
}
