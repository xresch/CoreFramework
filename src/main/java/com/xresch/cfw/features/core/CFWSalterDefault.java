package com.xresch.cfw.features.core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

import com.google.common.io.Files;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.utils.LinkedProperties;

/******************************************************************************************
 * The default implementation for the salting interface.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2026
 * @license MIT-License
 * 
 ******************************************************************************************/
public class CFWSalterDefault implements CFWSalterInterface {
	
	public static final Logger logger = CFWLog.getLogger(CFWSalterDefault.class.getName());
	
	private static final String FILE_PATH = "salt.properties";
	
	private static final LinkedProperties saltingProperties = new LinkedProperties();
	
	private enum DefaultSalterFields {
		  saltCredentialsAccount("CredentialsAccount-Default-Salt")
		, saltCredentialsPW("CredentialsPW-Default-Salt")
		, saltCredentialsToken("CredentialsToken-Default-Salt")
		, saltCredentialsSecret("CredentialsSecret-Default-Salt")
		, saltCredentialsDomain("CredentialsDomain-Default-Salt")
		, saltCredentialsHostname("CredentialsHostname-Default-Salt")
		, saltCredentialsUrl("CredentialsUrl-Default-Salt")
		, saltCredentialsData("CredentialsData-Default-Salt")
		, saltCredentialsCustom("CredentialsCustom-Default-Salt")
		
		, saltDBJDBC("genericjdbc_DB_PW_Salt")
		, saltDBMySQL("mysql_DB_PW_Salt")
		, saltDBMSSQL("mssql_DB_PW_Salt")
		, saltDBOracle("oracle_DB_PW_Salt")
		, saltDBPostgres("postgres_DB_PW_Salt")
		;
		
		private String defaultValue;
		
		private DefaultSalterFields(String defaultValue) {
			this.defaultValue = defaultValue;
		}
		
		public String defaultValue() { return defaultValue; }
		
	}
	/**************************************************************
	 * 
	 **************************************************************/
	@Override
	public void initialize() {
		
		String folder = CFW.CLI.getValue(CFW.CLI.VM_CONFIG_FOLDER);

		try {
			//------------------------------------------
			// Create Default File if not exists
			createDefaultSaltFile(folder, FILE_PATH);
			
			//------------------------------------------
			// Load File
			String filePath = folder + File.separator + FILE_PATH;
			saltingProperties.load(new FileReader(new File(filePath)));
			
		} catch (Exception e) {
			new CFWLog(logger).severe("Error while loading salt.properties: "+e.getMessage(), e);
		} 
		
	}
	
	/**************************************************************
	 * 
	 **************************************************************/
	private void createDefaultSaltFile(String folder, String filename) throws IOException {
		
		String filePath = folder + File.separator + filename;
		
		//----------------------------
		// do nothing if file exists
		File defaultFile = new File(filePath);
		if(  defaultFile.isFile() ) { return; }
		
		//----------------------------
		// Create dirs if not exists
		Files.createParentDirs(defaultFile);
		
		//========================================================
		// Create Default File
		StringBuilder defaultContents = new StringBuilder();
		defaultContents.append("###################################################################################################");
		defaultContents.append("\n#                                   !!!!!!!!!!!!!!!!!");
		defaultContents.append("\n#                                   !!! IMPORTANT !!!");
		defaultContents.append("\n#                                   !!!!!!!!!!!!!!!!!");
		defaultContents.append("\n# ONLY CHANGE THE SALT STRINGS IN THIS FILE RIGHT AFTER INSTALLATION! ");
		defaultContents.append("\n# MAKE A BACKUP OF THIS FILE IF YOU EVER CHANGE ITS VALUES! ");
		defaultContents.append("\n# IF YOU LOSE YOUR CUSTOMIZED FILE, YOUR APPLICATION WON'T BE ABLE TO DECRYPT YOUR DATA ANYMORE.");
		defaultContents.append("\n# ");
		defaultContents.append("\n# This file is created on application startup with default values, which also ensures backward");
		defaultContents.append("\n# compatibility with older versions.");
		defaultContents.append("\n# ");
		defaultContents.append("\n###################################################################################################");
		defaultContents.append("\n");
		
		for(DefaultSalterFields field : DefaultSalterFields.values()) {
			defaultContents.append(field.toString())
					.append("=")	
					.append(field.defaultValue())
					.append("\n")
					;	
		}
		
		Files.write(defaultContents.toString().getBytes(), defaultFile);
		
		//----------------------------
		// do nothing if file exists
		File generatedFile = new File(filePath.replaceAll(".properties", "-generated.properties"));
		
		//========================================================
		// Create Default File
		StringBuilder generatedContents = new StringBuilder();
		generatedContents.append("###################################################################################################");
		generatedContents.append("\n#                                   !!!!!!!!!!!!!!!!!");
		generatedContents.append("\n#                                   !!! IMPORTANT !!!");
		generatedContents.append("\n#                                   !!!!!!!!!!!!!!!!!");
		generatedContents.append("\n# ONLY CHANGE THE SALT STRINGS IN THIS FILE RIGHT AFTER INSTALLATION! ");
		generatedContents.append("\n# MAKE A BACKUP OF THIS FILE IF YOU EVER CHANGE ITS VALUES! ");
		generatedContents.append("\n# IF YOU LOSE YOUR CUSTOMIZED FILE, YOUR APPLICATION WON'T BE ABLE TO DECRYPT YOUR DATA ANYMORE.");
		generatedContents.append("\n# ");
		generatedContents.append("\n# This file is created on application startup with random values.");
		generatedContents.append("\n# It can be used to replace the default salt.properties when setting up the application.");
		generatedContents.append("\n# ");
		generatedContents.append("\n###################################################################################################");
		generatedContents.append("\n");
		
		for(DefaultSalterFields field : DefaultSalterFields.values()) {
			generatedContents.append(field.toString())
					.append("=")	
					.append(CFW.Random.stringAlphaNumSpecial(256))
					.append("\n")
					;
		}
		
		Files.write(generatedContents.toString().getBytes(), generatedFile);
	}
	
	/**************************************************************
	 * 
	 **************************************************************/
	private String getSalt(DefaultSalterFields field)  {
		//--------------------------------
		// Fallback to default if file is missing
		if(saltingProperties == null) {
			return field.defaultValue();
		}
		
		//--------------------------------
		// Load from File or default
		return saltingProperties.getProperty(field.toString(), field.defaultValue());
		
	}
	
	@Override	public String credentialsAccountSalt() {	return getSalt(DefaultSalterFields.saltCredentialsAccount);		}
	@Override	public String credentialsPWSalt() {			return getSalt(DefaultSalterFields.saltCredentialsPW);		}
	@Override	public String credentialsTokenSalt() {		return getSalt(DefaultSalterFields.saltCredentialsToken);	}
	@Override 	public String credentialsSecretSalt() {		return getSalt(DefaultSalterFields.saltCredentialsSecret);	}
	@Override 	public String credentialsDomainSalt() {		return getSalt(DefaultSalterFields.saltCredentialsDomain);	}
	@Override 	public String credentialsHostnameSalt() {	return getSalt(DefaultSalterFields.saltCredentialsHostname);	}
	@Override 	public String credentialsUrlSalt() {		return getSalt(DefaultSalterFields.saltCredentialsUrl);	}
	@Override 	public String credentialsDataSalt() {		return getSalt(DefaultSalterFields.saltCredentialsData);	}
	@Override 	public String credentialsCustomSalt() {		return getSalt(DefaultSalterFields.saltCredentialsCustom);	}
	
	
	@Override 	public String dbJDBCSalt() {		return getSalt(DefaultSalterFields.saltDBJDBC);	}
	@Override 	public String dbMySQLSalt() {		return getSalt(DefaultSalterFields.saltDBMySQL);	}
	@Override 	public String dbMSSQLSalt() {		return getSalt(DefaultSalterFields.saltDBMSSQL);	}
	@Override 	public String dbOracleSalt() {		return getSalt(DefaultSalterFields.saltDBOracle);	}
	@Override 	public String dbPostgresSalt() {	return getSalt(DefaultSalterFields.saltDBPostgres);	}

}
