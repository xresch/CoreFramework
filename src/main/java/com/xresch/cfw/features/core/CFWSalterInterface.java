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
}
