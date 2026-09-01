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
		
		, saltDefaultA("salt-default-A-0;6d?{3[H1hCiCCS1GK)P=[Q[d*A/ta:)n86UwG[(7-6ut=VEN*}BF6M1Y5>c?[l-7NQ9c&g[a1*e]j3cN47{_y5-8&%m:}K=M%???1F&/JMs))1ac9H1d8/>{2OZUoe")
		, saltDefaultB("salt-default-B-s)Ek<oQ0g>[[e-!o27U=<ASww/{I+3/B.2Fi{]z%76FH3Ai9{7KN{9Zj:7+(++==Vy+pq4.?x>.*Wjq>;.=?]30M772).aNn-3<%:H%A;82%/G!?/N0d4?_B3DA]]7//")
		, saltDefaultC("salt-default-C-YBK!Ho_}77go.&S}5h:e9d2{/373rX}z+.KbEN?X(&&E/<O5%3x[jx[iN,q+_(:SQ6Qd%.ks*N.L7oF:-,3=Z*VE0brsUT0!V[1<%NM4:[CGr7=/}(ga=(Fyz*7b5}qf")
		, saltDefaultD("salt-default-D-JZ1U:XKfZml>]*/hl?CCb6}&zmd!+)?VV&H<iz:027K=3V?Tp7Dbu7lnEY:5QG5u&8s3:Y-qJ3%mMr)U?/*D4/lVOI)Ik}{PjAF4!u!%?nj3&kuVrLw(EQ90-k[Cw}6(")
		, saltDefaultE("salt-default-E-TOwL<?41ZI]-L%6D&OC<Job</wT?+rH9-:C(3H<l?p:+bbPjhW&!m46R]9E.,KPMC=bgJ9qq4W:}%T4lz7n[.E&S1u/jm59z7!!wX5{q!zI(0VIY}hNY]i3<v]+R+{YQ")
		, saltDefaultF("salt-default-F->15f-*?/x734X33JH?8][44VuA,s6K=g)9Q5-N*%0b!TT?kuckTH)JZv+R<mkQTM8(xyef1b>oKYbc+);LP+O8.?U8(sN%A2+(8/3m{NIeXY+e_;vYsf[,Tt1,Lr*!--")
		, saltDefaultG("salt-default-G-E{yrd+I3h&]W}3TwG<%[mV{vx9LA-x(;,o!&?Sh[H/u4F-23)f{1+zV((_-,ItR>W(7(E.V?0q/pt-%0.*t<A?qP]e1?2JN6LOre0[]69=rAN}>-3154KNAdS>4>1}%(")
		, saltDefaultH("salt-default-H-0f:&:q2ZZa%bf)i}2]18>?o4B>Wvl{y:9;1lF6N_9+L:Y[8y??PMaV11-1nN0{}2t!>,3U8<!U+/.2:lW>QpPS%nUT/I?,RU:,!]+}&tnb}rlI(I]}}X;9(+<_)2ZBs%")
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
	
	@Override 	public String defaultASalt() {	return getSalt(DefaultSalterFields.saltDefaultA);	}
	@Override 	public String defaultBSalt() {	return getSalt(DefaultSalterFields.saltDefaultB);	}
	@Override 	public String defaultCSalt() {	return getSalt(DefaultSalterFields.saltDefaultC);	}
	@Override 	public String defaultDSalt() {	return getSalt(DefaultSalterFields.saltDefaultD);	}
	@Override 	public String defaultESalt() {	return getSalt(DefaultSalterFields.saltDefaultE);	}
	@Override 	public String defaultFSalt() {	return getSalt(DefaultSalterFields.saltDefaultF);	}
	@Override 	public String defaultGSalt() {	return getSalt(DefaultSalterFields.saltDefaultG);	}
	@Override 	public String defaultHSalt() {	return getSalt(DefaultSalterFields.saltDefaultH);	}

}
