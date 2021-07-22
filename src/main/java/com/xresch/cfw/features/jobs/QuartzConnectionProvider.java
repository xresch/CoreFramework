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
		
		return CFW.DB.getDBInterface().getConnection();
	}

	@Override
	public void shutdown() throws SQLException {
		
	}



}
