package com.pengtoolbox.cfw._main;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.pengtoolbox.cfw.utils.LinkedProperties;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWProperties {

	public static final LinkedProperties configProperties = new LinkedProperties();
	
	//##########################################################################################
	// APPLICATION
	//##########################################################################################
	/** Application name, will be used for creating the base url (Property=cfw_application_name, Default="myapp") */
	public static String APPLICATION_NAME = "myapp";
	
	/** Application ID, used to make your application more secure. (Property=cfw_application_id, Default="change_me_now") */
	public static String APPLICATION_ID = "change_me_now";
		
	/** Default maximum upload size for files in megabyte. (Property=cfw_application_max_uploadsize, Default=200) */
	public static int APPLICATION_MAX_UPLOADSIZE = 200;
	
	/** Reset the admin password on the next restart. (Property=cfw_reset_admin_pw, Default=false) */
	public static boolean RESET_ADMIN_PW = false;
	
	
	//##########################################################################################
	// HTTP
	//##########################################################################################
	/** Enables or disables the HTTP connector. (Property=cfw_http_enabled, Default=true) */
	public static boolean HTTP_ENABLED = true;
	
	/** The port for the HTTP connector. (Property=cfw_http_connector_host, "0.0.0.0") */
	public static String HTTP_CONNECTOR_HOST = "0.0.0.0";
	
	/** The port for the HTTP connector. (Property=cfw_http_port, Default=80) */
	public static int HTTP_PORT = 80;
	
	/** The port for the HTTP connector. (Property=cfw_http_redirect_to_https, Default=true) */
	public static boolean HTTP_REDIRECT_TO_HTTPS = true;
	
	/** Enables or disables the HTTPS connector. (Property=cfw_https_enabled, Default=true) */
	public static boolean HTTPS_ENABLED = true;
	
	/**  (Property=cfw_https_port, Default=443) */
	public static int HTTPS_PORT = 443;
	
	/**  (Property=cfw_https_keystore_path, Default=./config/keystore.jks) */
	public static String HTTPS_KEYSTORE_PATH = "./config/keystore.jks";
	
	/**  (Property=cfw_https_keystore_password, Default="") */
	public static String HTTPS_KEYSTORE_PASSWORD  = "";
	
	/**  (Property=cfw_https_keymanager_password, Default="") */
	public static String HTTPS_KEYMANAGER_PASSWORD = "";
	
	
	//##########################################################################################
	// PROXY
	//##########################################################################################	
	/**  (Property=cfw_proxy_enabled, Default=false) */
	public static boolean PROXY_ENABLED = false;
	
	/**  (Property=cfw_proxy_pac, Default="") */
	public static String PROXY_PAC = "";
	
	
	//##########################################################################################
	// PERFORMANCE
	//##########################################################################################		
	/** Session Time in Seconds. (Property=cfw_session_timeout, Default=36000) */
	public static int SESSION_TIMEOUT = 36000;
	
	/** Time in seconds to cache resources. (Property=cfw_browser_resource_maxage, Default="36000") */
	public static int BROWSER_RESOURCE_MAXAGE = 36000;
	
	
	//##########################################################################################
	// AUTHENTICATION
	//##########################################################################################	
	/** Enables or disables the Authentication. (Property=cfw_authentication_enabled, Default="false") */
	public static boolean AUTHENTICATION_ENABLED = false;
	
	/** The authentication method which should be used. (Property=authentication_method, Default="csv") */
	public static String AUTHENTICATION_METHOD = "csv";
	
	/** The path to the csv file with credentials. (Property=authentication_csv_file, Default="./config/credentials.csv") */
	public static String AUTHENTICATION_CSV_FILE = "./config/credentials.csv"; 
	
	/** The URL used for LDAP authentication. (Property=authentication_ldap_url, Default="") */
	public static String LDAP_URL = "";
	
	/**  Define if the LDAP connection should be done using ssl. (Property=authentication_ldap_ssl_enabled, Default=false) */
	public static boolean LDAP_SSL_ENABLED = false;
	
	/** The User used for LDAP authentication. (Property=authentication_ldap_user, Default="") */
	public static String LDAP_USER = "";
	
	/** The URL used for LDAP authentication. (Property=authentication_ldap_password, Default="") */
	public static String LDAP_PASSWORD = "";
	
	/** The URL used for LDAP authentication. (Property=authentication_ldap_searchbase, Default="") */
	public static String LDAP_SEARCHBASE = "";
	
	/** The name of the attribute storing the username. (Property=authentication_ldap_user_attribute, Default="") */
	public static String LDAP_ATTRIBUTE_USERNAME = "";
	
	/** The name of the attribute storing the email of the user.  (Property=authentication_ldap_attribute_mail, Default="") */
	public static String LDAP_ATTRIBUTE_EMAIL = "";
	
	/** The name of the attribute storing the firstname of the user.  (Property=authentication_ldap_attribute_firstname, Default="") */
	public static String LDAP_ATTRIBUTE_FIRSTNAME = "";

	/** The name of the attribute storing the lastname of the user.  (Property=authentication_ldap_attribute_lastname, Default="") */
	public static String LDAP_ATTRIBUTE_LASTNAME = "";
	

	//##########################################################################################
	// MAILING
	//##########################################################################################	
	
	/** Choose if you want the application to send mails.  (Property=cfw_mail_enabled, Default="false") */
	public static boolean MAIL_ENABLED = false;
	
	/** The smtp host used for sending mails.  (Property=cfw_mail_smtp_host, Default="") */
	public static String MAIL_SMTP_HOST = "";
	
	/** The port of the smtp server.  (Property=cfw_mail_smtp_port=, Default="587") */
	public static int MAIL_SMTP_PORT = 587;
	
	/** The method used to authenticate against the smtp server, either of:  NONE | TLS | SSL.  (Property=cfw_mail_smtp_authentication, Default="") */
	public static String MAIL_SMTP_AUTHENTICATION = "";
	
	/** The email used for login to the smtp server .  (Property=cfw_mail_smtp_login_mail, Default="") */
	public static String MAIL_SMTP_LOGIN_MAIL = "";
	
	/** The password used for login to the smtp server.  (Property=cfw_mail_smtp_login_password, Default="") */
	public static String MAIL_SMTP_LOGIN_PASSWORD = "";
	
	/** The eMail used as the from-address for repliable mails.  (Property=cfw_mail_smtp_frommail_reply, Default="") */
	public static String MAIL_SMTP_FROMMAIL_REPLY = "";
	
	/** The eMail used as the from-address for no-reply mails.  (Property=cfw_mail_smtp_frommail_noreply, Default="") */
	public static String MAIL_SMTP_FROMMAIL_NOREPLY = "";
	

	
	//##########################################################################################
	// DATABASE
	//##########################################################################################	
	
	/** The name of the database server. (Property=cfw_h2_server, Default="localhost") */
	public static String DB_SERVER = "localhost";
	
	/** The directory where the database should be stored. (Property=cfw_h2_path, Default="false") */
	public static String DB_STORE_PATH = "./datastore";
	
	/** The name of the database. (Property=cfw_h2_db_name, Default="h2database") */
	public static String DB_NAME = "h2database";
	
	/** The port of the database. (Property=cfw_h2_port, Default="8081") */
	public static int DB_PORT = 8081;

	/** The username for the database. (Property=cfw_h2_username, Default="sa") */
	public static String DB_USERNAME = "sa";
	
	/** The password for the database. (Property=cfw_h2_password, Default="sa") */
	public static String DB_PASSWORD = "sa";


	/******************************************************************************
	 * Initialize the configuration with the given properties file.
	 * @param key
	 * @return
	 *******************************************************************************/
	public static void loadProperties(String configFilePath) throws IOException {
		
		CFWProperties.configProperties.load(new FileReader(new File(configFilePath)));
		printConfiguration();
		
		APPLICATION_ID					= CFWProperties.configAsString("cfw_application_id", APPLICATION_ID);
		APPLICATION_NAME				= CFWProperties.configAsString("cfw_application_name", APPLICATION_NAME);
		APPLICATION_MAX_UPLOADSIZE		= CFWProperties.configAsInt("cfw_application_max_uploadsize", APPLICATION_MAX_UPLOADSIZE);
		RESET_ADMIN_PW 					= CFWProperties.configAsBoolean("cfw_reset_admin_pw", RESET_ADMIN_PW);
						
		HTTP_ENABLED 					= CFWProperties.configAsBoolean("cfw_http_enabled", HTTP_ENABLED);
		HTTP_CONNECTOR_HOST				= CFWProperties.configAsString("cfw_http_connector_host", HTTP_CONNECTOR_HOST);
		HTTP_PORT 						= CFWProperties.configAsInt("cfw_http_port", HTTP_PORT);
		HTTP_REDIRECT_TO_HTTPS			= CFWProperties.configAsBoolean("cfw_http_redirect_to_https", HTTP_REDIRECT_TO_HTTPS);

		HTTPS_ENABLED 					= CFWProperties.configAsBoolean("cfw_https_enabled", HTTPS_ENABLED);
		HTTPS_PORT 						= CFWProperties.configAsInt("cfw_https_port", HTTPS_PORT);
		
		HTTPS_KEYSTORE_PATH 			= CFWProperties.configAsString("cfw_https_keystore_path", HTTPS_KEYSTORE_PATH);
		HTTPS_KEYSTORE_PASSWORD			= CFWProperties.configAsString("cfw_https_keystore_password", HTTPS_KEYSTORE_PASSWORD);
		HTTPS_KEYMANAGER_PASSWORD		= CFWProperties.configAsString("cfw_https_keymanager_password", HTTPS_KEYMANAGER_PASSWORD);
		
		PROXY_ENABLED 					= CFWProperties.configAsBoolean("cfw_proxy_enabled", PROXY_ENABLED);
		PROXY_PAC						= CFWProperties.configAsString("cfw_proxy_pac", PROXY_PAC);
		
		SESSION_TIMEOUT					= CFWProperties.configAsInt("cfw_session_timeout", SESSION_TIMEOUT);
		BROWSER_RESOURCE_MAXAGE 		= CFWProperties.configAsInt("cfw_browser_resource_maxage", BROWSER_RESOURCE_MAXAGE);
		
		AUTHENTICATION_METHOD 			= CFWProperties.configAsString("authentication_method", AUTHENTICATION_METHOD);
		AUTHENTICATION_ENABLED 			= CFWProperties.configAsBoolean("cfw_authentication_enabled", AUTHENTICATION_ENABLED);
		AUTHENTICATION_CSV_FILE			= CFWProperties.configAsString("authentication_csv_file", AUTHENTICATION_CSV_FILE);
		
		LDAP_URL 						= CFWProperties.configAsString("authentication_ldap_url", LDAP_URL);
		LDAP_USER 						= CFWProperties.configAsString("authentication_ldap_user", LDAP_USER);
		LDAP_SSL_ENABLED 				= CFWProperties.configAsBoolean("authentication_ldap_ssl_enabled", LDAP_SSL_ENABLED);
		LDAP_PASSWORD 					= CFWProperties.configAsString("authentication_ldap_password", LDAP_PASSWORD);
		LDAP_SEARCHBASE 				= CFWProperties.configAsString("authentication_ldap_searchbase", LDAP_SEARCHBASE);
		LDAP_ATTRIBUTE_USERNAME 		= CFWProperties.configAsString("authentication_ldap_attribute_username", LDAP_ATTRIBUTE_USERNAME);
		LDAP_ATTRIBUTE_EMAIL 			= CFWProperties.configAsString("authentication_ldap_attribute_mail", LDAP_ATTRIBUTE_EMAIL);
		LDAP_ATTRIBUTE_FIRSTNAME		= CFWProperties.configAsString("authentication_ldap_attribute_firstname", LDAP_ATTRIBUTE_FIRSTNAME);
		LDAP_ATTRIBUTE_LASTNAME			= CFWProperties.configAsString("authentication_ldap_attribute_lastname", LDAP_ATTRIBUTE_LASTNAME);
		
		DB_SERVER						= CFWProperties.configAsString("cfw_h2_server", DB_SERVER);
		DB_PORT							= CFWProperties.configAsInt("cfw_h2_port", DB_PORT);
		DB_STORE_PATH					= CFWProperties.configAsString("cfw_h2_path", DB_STORE_PATH);
		DB_NAME							= CFWProperties.configAsString("cfw_h2_db_name", DB_NAME);
		DB_USERNAME						= CFWProperties.configAsString("cfw_h2_username", DB_USERNAME);
		DB_PASSWORD						= CFWProperties.configAsString("cfw_h2_password", DB_PASSWORD);
		
		MAIL_ENABLED					= CFWProperties.configAsBoolean("cfw_mail_enabled", MAIL_ENABLED);
		MAIL_SMTP_HOST					= CFWProperties.configAsString("cfw_mail_smtp_host", MAIL_SMTP_HOST);
		MAIL_SMTP_PORT					= CFWProperties.configAsInt("cfw_mail_smtp_port", MAIL_SMTP_PORT);
		MAIL_SMTP_AUTHENTICATION		= CFWProperties.configAsString("cfw_mail_smtp_authentication", MAIL_SMTP_AUTHENTICATION);
		MAIL_SMTP_LOGIN_MAIL			= CFWProperties.configAsString("cfw_mail_smtp_login_mail", MAIL_SMTP_LOGIN_MAIL);
		MAIL_SMTP_LOGIN_PASSWORD		= CFWProperties.configAsString("cfw_mail_smtp_login_password", MAIL_SMTP_LOGIN_PASSWORD);
		MAIL_SMTP_FROMMAIL_REPLY		= CFWProperties.configAsString("cfw_mail_smtp_frommail_reply", MAIL_SMTP_FROMMAIL_REPLY);
		MAIL_SMTP_FROMMAIL_NOREPLY		= CFWProperties.configAsString("cfw_mail_smtp_frommail_noreply", MAIL_SMTP_FROMMAIL_NOREPLY);

	}
	
	
	/******************************************************************************
	 * Initialize the configuration with the given properties file.
	 * @param key
	 * @return
	 *******************************************************************************/
	public static void printConfiguration() {
		
		System.out.println("################################################");
		System.out.println("##            LOADED CONFIGURATION            ##");
		System.out.println("################################################");

		for (Object key : configProperties.orderedKeys()) {
			System.out.println(key+"='"+configProperties.getProperty((String)key)+"'");
		}
		System.out.println("################################################");
	}

	
	/******************************************************************************
	 * Retrieve
	 * @param key
	 * @return
	 *******************************************************************************/
	public static boolean configAsBoolean(String key, boolean defaultValue){
		
		return configProperties.getOrDefault(key, defaultValue).toString().toLowerCase().equals("true") ? true : false;
	}

	public static int configAsInt(String key, int defaultValue){
		
		return (int)Integer.parseInt((String)configProperties.getOrDefault(key, ""+defaultValue));
	}

	public static String configAsString(String key, String defaultValue){
		
		return (String)configProperties.getOrDefault(key, defaultValue);
	}


}
