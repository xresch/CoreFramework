package com.xresch.cfw.features.core;
/******************************************************************************************
 * The default implementation for the salting interface.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2026
 * @license MIT-License
 * 
 ******************************************************************************************/
public class CFWSalterDefault implements CFWSalterInterface {

	@Override
	public String credentialsPWSalt() {
		return "CredentialsPW-Default-Salt";
	}

	@Override
	public String credentialsTokenSalt() {
		return "CredentialsToken-Default-Salt";
	}

	@Override
	public String credentialsSecretSalt() {
		return "CredentialsSecret-Default-Salt";
	}

}
