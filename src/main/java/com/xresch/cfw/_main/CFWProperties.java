package com.xresch.cfw._main;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.utils.LinkedProperties;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWProperties {

	public static final Logger logger = CFWLog.getLogger(CFWProperties.class.getName());
	private static final LinkedProperties configProperties = new LinkedProperties();
	
	//##########################################################################################
	// APPLICATION
	//##########################################################################################
	/** Application name, will be used for creating the base url (Property=cfw_application_name, Default="myapp") */
	public static String APPLICATION_NAME = "myapp";
	
	/** Application ID, used to make your application more secure. (Property=cfw_application_id, Default="change_me_now") */
	public static String APPLICATION_ID = "change_me_now";
		
	/** Default maximum upload size for files in megabyte. (Property=cfw_application_max_uploadsize, Default=200) */
	public static int APPLICATION_MAX_UPLOADSIZE = 200;
	
	/** Default maximum upload size for files in bytes. (Property=cfw_application_max_formsize, Default=5000000) */
	public static int APPLICATION_MAX_FORM_SIZE = 5 * 1000 * 1000;
	
	/** Reset the admin password on the next restart. (Property=cfw_reset_admin_pw, Default=false) */
	public static boolean RESET_ADMIN_PW = false;
	
	//##########################################################################################
	// SERVER
	//##########################################################################################
	
	/** Application URL as used by the application user to access the application. will be used to create Links(e.g. in eMails). (Property=cfw_application_url, Default=null) */
	public static String APPLICATION_URL = null;
	
	/** The mode in which the application should start(FULL, APP, DB). (Property=cfw_mode, "FULL") */
	public static String MODE = "FULL";
	
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
		
	/**  (Property=cfw_https_acme_enabled, Default=false */
	public static boolean HTTPS_ACME_ENABLED = false;
	
	/**  (Property=cfw_https_keystore_path, Default=./config/keystore.jks) */
	public static String HTTPS_KEYSTORE_PATH = "./config/keystore.jks";
	
	/**  (Property=cfw_https_keystore_password, Default="") */
	public static String HTTPS_KEYSTORE_PASSWORD  = "";
	
	/**  (Property=cfw_https_keymanager_password, Default="") */
	public static String HTTPS_KEYMANAGER_PASSWORD = "";
	
	/** Max size in bytes for request headers. (Property=cfw_http_max_request_header_size, Default=65535) */
	public static int HTTP_MAX_REQUEST_HEADER_SIZE = 65535;
	
	/**  (Property=cfw_https_acme_folder, Default=./config/acme */
	public static String HTTPS_ACME_FOLDER = "./config/acme";
	
	/**  (Property=cfw_https_acme_url, Default="acme://letsencrypt.org" */
	public static String HTTPS_ACME_URL = "acme://letsencrypt.org";
	
	/**  (Property=cfw_https_acme_email, Default="" */
	public static String HTTPS_ACME_EMAIL = "";
	
	/**  (Property=cfw_https_acme_domains, Default="" */
	public static String HTTPS_ACME_DOMAINS = "";
	
	/**  (Property=cfw_https_acme_password, Default="" */
	public static String HTTPS_ACME_PASSWORD = "";
	
	/**  (Property=cfw_https_accept_terms, Default=false */
	public static boolean HTTPS_ACME_ACCEPT_TERMS = false;
	
	/**  (Property=cfw_https_acme_renewal_threshold, Default=14 */
	public static int HTTPS_ACME_RENEWAL_THRESHOLD = 14;

			
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
	/** number of threads used to run jobs(=max jobs that can run in parallel)  (Property=cfw_job_threads, Default=10) */
	public static int JOB_THREADS = 10;
	
	//##########################################################################################
	// AUTHENTICATION
	//##########################################################################################	
	/** Enables or disables the Authentication. (Property=cfw_authentication_enabled, Default="true") */
	public static boolean AUTHENTICATION_ENABLED = true;
	
	/** Enables or disables the SSO SAML2 feature. (Property=cfw_authentication_saml2_enabled, Default="false") */
	public static boolean AUTHENTICATION_SAML2_ENABLED = false;
	
	/** The path to the saml.properties file. (Property=cfw_authentication_saml2_configfile, Default="./config/SAML/saml2.properties") */
	public static String AUTHENTICATION_SAML2_CONFIGFILE = "./config/SAML/saml2.properties";
	
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
	
	/** The query for filtering the users, can be used to filter by groups. (Property=authentication_ldap_query, Default="({usernameAttribute}={username})") */
	public static String LDAP_QUERY = "({usernameAttribute}={username})";
	
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
	// MAILING
	//##########################################################################################	
	
	/** Maximum upload size in megabytes.  (Property=cfw_filemanager_max_upload_size, Default="4000") */
	public static int CFW_FILEMANAGER_MAX_UPLOAD_SIZE = 4000;
	
	/** Temp folder to be used, by default the value will be taken from java.io.tmpdir.  (Property=cfw_filemanager_temp_folder, Default="") */
	public static String CFW_FILEMANAGER_TEMP_FOLDER = null;
	
	/** File size in megabytes, files bigger than this will be temorarily written to the disk to prevent out of memory issues. (Property=cfw_filemanager_temp_threshold=, Default="587") */
	public static int CFW_FILEMANAGER_TEMP_THRESHOLD = 50;
	
	//##########################################################################################
	// DATABASE
	//##########################################################################################	
	
	/** The JVM Timezone, needed for h2 to treat give proper epoch times when handling time columns. (Property=cfw_jvm_timezone, Default="UTC") */
	public static String JVM_TIMEZONE = "UTC";
	
	/** The name of the database server. (Property=cfw_h2_mode, Default="SERVER") */
	public static String DB_MODE = "SERVER";
	
	/** The name of the database server. (Property=cfw_h2_server, Default="localhost") */
	public static String DB_SERVER = "localhost";
	
	/** The directory where the database and other data should be stored. (Property=cfw_h2_path, Default="false") */
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
		logConfiguration();
		
		APPLICATION_ID					= CFWProperties.configAsString("cfw_application_id", APPLICATION_ID);
		APPLICATION_NAME				= CFWProperties.configAsString("cfw_application_name", APPLICATION_NAME);
		APPLICATION_MAX_UPLOADSIZE		= CFWProperties.configAsInt("cfw_application_max_uploadsize", APPLICATION_MAX_UPLOADSIZE);
		APPLICATION_MAX_FORM_SIZE		= CFWProperties.configAsInt("cfw_application_max_formsize", APPLICATION_MAX_FORM_SIZE);
		
		RESET_ADMIN_PW 					= CFWProperties.configAsBoolean("cfw_reset_admin_pw", RESET_ADMIN_PW);
		
		APPLICATION_URL					= CFWProperties.configAsString("cfw_application_url", APPLICATION_URL);
		MODE							= CFWProperties.configAsString("cfw_mode", MODE);
		
		HTTP_ENABLED 					= CFWProperties.configAsBoolean("cfw_http_enabled", HTTP_ENABLED);
		HTTP_CONNECTOR_HOST				= CFWProperties.configAsString("cfw_http_connector_host", HTTP_CONNECTOR_HOST);
		HTTP_PORT 						= CFWProperties.configAsInt("cfw_http_port", HTTP_PORT);
		HTTP_REDIRECT_TO_HTTPS			= CFWProperties.configAsBoolean("cfw_http_redirect_to_https", HTTP_REDIRECT_TO_HTTPS);

		HTTPS_ENABLED 					= CFWProperties.configAsBoolean("cfw_https_enabled", HTTPS_ENABLED);
		HTTPS_PORT 						= CFWProperties.configAsInt("cfw_https_port", HTTPS_PORT);

		HTTPS_ACME_ENABLED	 			= CFWProperties.configAsBoolean("cfw_https_acme_enabled", HTTPS_ACME_ENABLED);
		
		HTTPS_KEYSTORE_PATH 			= CFWProperties.configAsString("cfw_https_keystore_path", HTTPS_KEYSTORE_PATH);
		HTTPS_KEYSTORE_PASSWORD			= CFWProperties.configAsString("cfw_https_keystore_password", HTTPS_KEYSTORE_PASSWORD);
		HTTPS_KEYMANAGER_PASSWORD		= CFWProperties.configAsString("cfw_https_keymanager_password", HTTPS_KEYMANAGER_PASSWORD);
		
		HTTPS_ACME_FOLDER 				= CFWProperties.configAsString("cfw_https_acme_folder", HTTPS_ACME_FOLDER);
		HTTPS_ACME_URL					= CFWProperties.configAsString("cfw_https_acme_url", HTTPS_ACME_URL);
		HTTPS_ACME_EMAIL 				= CFWProperties.configAsString("cfw_https_acme_email", HTTPS_ACME_EMAIL);
		HTTPS_ACME_DOMAINS 				= CFWProperties.configAsString("cfw_https_acme_domains", HTTPS_ACME_DOMAINS);
		HTTPS_ACME_PASSWORD 			= CFWProperties.configAsString("cfw_https_acme_password", HTTPS_ACME_PASSWORD);
		HTTPS_ACME_ACCEPT_TERMS 		= CFWProperties.configAsBoolean("cfw_https_accept_terms", HTTPS_ACME_ACCEPT_TERMS);
		HTTPS_ACME_RENEWAL_THRESHOLD 	= CFWProperties.configAsInt("cfw_https_acme_renewal_threshold", HTTPS_ACME_RENEWAL_THRESHOLD);
		
		HTTP_MAX_REQUEST_HEADER_SIZE 	= CFWProperties.configAsInt("cfw_http_max_request_header_size", HTTP_MAX_REQUEST_HEADER_SIZE);
		
		PROXY_ENABLED 					= CFWProperties.configAsBoolean("cfw_proxy_enabled", PROXY_ENABLED);
		PROXY_PAC						= CFWProperties.configAsString("cfw_proxy_pac", PROXY_PAC);
		
		JOB_THREADS 					= CFWProperties.configAsInt("cfw_job_threads", JOB_THREADS);
		AUTHENTICATION_METHOD 			= CFWProperties.configAsString("authentication_method", AUTHENTICATION_METHOD);
		AUTHENTICATION_ENABLED 			= CFWProperties.configAsBoolean("cfw_authentication_enabled", AUTHENTICATION_ENABLED);
		AUTHENTICATION_SAML2_ENABLED 	= CFWProperties.configAsBoolean("cfw_authentication_saml2_enabled", AUTHENTICATION_SAML2_ENABLED);
		AUTHENTICATION_SAML2_CONFIGFILE = CFWProperties.configAsString("cfw_authentication_saml2_configfile", AUTHENTICATION_SAML2_CONFIGFILE);
		
		AUTHENTICATION_CSV_FILE			= CFWProperties.configAsString("authentication_csv_file", AUTHENTICATION_CSV_FILE);
		
		LDAP_URL 						= CFWProperties.configAsString("authentication_ldap_url", LDAP_URL);
		LDAP_USER 						= CFWProperties.configAsString("authentication_ldap_user", LDAP_USER);
		LDAP_SSL_ENABLED 				= CFWProperties.configAsBoolean("authentication_ldap_ssl_enabled", LDAP_SSL_ENABLED);
		LDAP_PASSWORD 					= CFWProperties.configAsString("authentication_ldap_password", LDAP_PASSWORD);
		LDAP_SEARCHBASE 				= CFWProperties.configAsString("authentication_ldap_searchbase", LDAP_SEARCHBASE);
		LDAP_QUERY		 				= CFWProperties.configAsString("authentication_ldap_query", LDAP_QUERY);
		
		LDAP_ATTRIBUTE_USERNAME 		= CFWProperties.configAsString("authentication_ldap_attribute_username", LDAP_ATTRIBUTE_USERNAME);
		LDAP_ATTRIBUTE_EMAIL 			= CFWProperties.configAsString("authentication_ldap_attribute_mail", LDAP_ATTRIBUTE_EMAIL);
		LDAP_ATTRIBUTE_FIRSTNAME		= CFWProperties.configAsString("authentication_ldap_attribute_firstname", LDAP_ATTRIBUTE_FIRSTNAME);
		LDAP_ATTRIBUTE_LASTNAME			= CFWProperties.configAsString("authentication_ldap_attribute_lastname", LDAP_ATTRIBUTE_LASTNAME);
				
		JVM_TIMEZONE					= CFWProperties.configAsString("cfw_jvm_timezone", JVM_TIMEZONE);
		DB_MODE							= CFWProperties.configAsString("cfw_h2_mode", DB_MODE);
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

		
		CFW_FILEMANAGER_MAX_UPLOAD_SIZE	= CFWProperties.configAsInt("cfw_filemanager_max_upload_size", CFW_FILEMANAGER_MAX_UPLOAD_SIZE);
		CFW_FILEMANAGER_TEMP_FOLDER		= CFWProperties.configAsString("cfw_filemanager_temp_folder", CFW_FILEMANAGER_TEMP_FOLDER);
		CFW_FILEMANAGER_TEMP_THRESHOLD	= CFWProperties.configAsInt("cfw_filemanager_temp_threshold", CFW_FILEMANAGER_TEMP_THRESHOLD);

	}
	
	
	/******************************************************************************
	 * Initialize the configuration with the given properties file.
	 * @param key
	 * @return
	 *******************************************************************************/
	public static void logConfiguration() {
		
		CFWLog log = new CFWLog(logger);
		log.config("################################################");
		log.config("##            LOADED PROPERTIES               ##");
		log.config("################################################");

		for (Object key : configProperties.orderedKeys()) {
			log.config(key+"='"+configProperties.getProperty((String)key)+"'");
		}
		log.config("################################################");
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
