package com.xresch.cfw.features.core;

/******************************************************************************************
 * The default implementation of the CoreFramework has the salts used for encryption checked
 * into the public repository.
 * To make applications more secure, this interface can be implemented to override the default
 * salts.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2026
 * @license MIT-License
 * 
 ******************************************************************************************/
public interface CFWSalterInterface {

	/***********************************************************
	 * Initialize method of the salter.
	 * This will be called by the framework after a DB connection 
	 * to the CFW H2 database has been established.
	 * 
	 ***********************************************************/
	public void initialize();
	
	/***********************************************************
	 * Return a constant string used for salting.
	 * @return salty string for salting
	 ***********************************************************/
	public String credentialsAccountSalt();
	
	/***********************************************************
	 * Return a constant string used for salting.
	 * @return salty string for salting
	 ***********************************************************/
	public String credentialsPWSalt();
	
	
	/***********************************************************
	 * Return a constant string used for salting.
	 * @return salty string for salting
	 ***********************************************************/
	public String credentialsTokenSalt();
	
	/***********************************************************
	 * Return a constant string used for salting.
	 * @return salty string for salting
	 ***********************************************************/
	public String credentialsSecretSalt();
	
	/***********************************************************
	 * Return a constant string used for salting.
	 * @return salty string for salting
	 ***********************************************************/
	public String credentialsDomainSalt();
	
	/***********************************************************
	 * Return a constant string used for salting.
	 * @return salty string for salting
	 ***********************************************************/
	public String credentialsHostnameSalt();
	
	/***********************************************************
	 * Return a constant string used for salting.
	 * @return salty string for salting
	 ***********************************************************/
	public String credentialsUrlSalt();
	
	/***********************************************************
	 * Return a constant string used for salting.
	 * @return salty string for salting
	 ***********************************************************/
	public String credentialsDataSalt();
	
	/***********************************************************
	 * Return a constant string used for salting.
	 * @return salty string for salting
	 ***********************************************************/
	public String credentialsCustomSalt();
	
	/***********************************************************
	 * Return a constant string used for salting.
	 * @return salty string for salting
	 ***********************************************************/
	public String dbJDBCSalt();
	
	/***********************************************************
	 * Return a constant string used for salting.
	 * @return salty string for salting
	 ***********************************************************/
	public String dbPostgresSalt();
	
	/***********************************************************
	 * Return a constant string used for salting.
	 * @return salty string for salting
	 ***********************************************************/
	public String dbMySQLSalt();
	
	/***********************************************************
	 * Return a constant string used for salting.
	 * @return salty string for salting
	 ***********************************************************/
	public String dbMSSQLSalt();
	
	/***********************************************************
	 * Return a constant string used for salting.
	 * @return salty string for salting
	 ***********************************************************/
	public String dbOracleSalt();
	
	/***********************************************************
	 * Return a constant string used for salting.
	 * @return salty string for salting
	 ***********************************************************/
	public String defaultASalt();
	
	/***********************************************************
	 * Return a constant string used for salting.
	 * @return salty string for salting
	 ***********************************************************/
	public String defaultBSalt();
	
	/***********************************************************
	 * Return a constant string used for salting.
	 * @return salty string for salting
	 ***********************************************************/
	public String defaultCSalt();
	
	/***********************************************************
	 * Return a constant string used for salting.
	 * @return salty string for salting
	 ***********************************************************/
	public String defaultDSalt();
	
	/***********************************************************
	 * Return a constant string used for salting.
	 * @return salty string for salting
	 ***********************************************************/
	public String defaultESalt();
	
	/***********************************************************
	 * Return a constant string used for salting.
	 * @return salty string for salting
	 ***********************************************************/
	public String defaultFSalt();
	
	/***********************************************************
	 * Return a constant string used for salting.
	 * @return salty string for salting
	 ***********************************************************/
	public String defaultGSalt();
	
	/***********************************************************
	 * Return a constant string used for salting.
	 * @return salty string for salting
	 ***********************************************************/
	public String defaultHSalt();
}
