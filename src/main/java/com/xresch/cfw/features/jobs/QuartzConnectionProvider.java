package com.xresch.cfw.features.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.quartz.utils.ConnectionProvider;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;

public class QuartzConnectionProvider implements ConnectionProvider {
	
	private static Logger logger = CFWLog.getLogger(QuartzConnectionProvider.class.getName());
	
	@Override
	public void initialize() throws SQLException {
		
		String sql = CFW.Files.readPackageResource(FeatureJobs.RESOURCE_PACKAGE, "create_quartz_tables.sql");

		if( !CFW.DB.getDBInterface().preparedExecuteBatch(sql)) {
			new CFWLog(logger)
				.severe("Error while initializing Quartz Tables.");
		}
		
	}
	
	@Override
	public Connection getConnection() throws SQLException {
		// Use new connection from Datasource.
		// As Quartz commits it's transactions, it would mess up ongoing transactions
		// if CFW.DB.getDBInterface().getConnection() would be used, as this can return
		// an existing transaction connection.
		return CFW.DB.getDBInterface().getDatasource().getConnection();
	}

	@Override
	public void shutdown() throws SQLException {
		
	}



}
