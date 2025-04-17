package com.xresch.cfw.features.core.acme;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.OrderBuilder;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.util.KeyPairUtils;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw._main.CFWProperties;
import com.xresch.cfw.features.notifications.Notification;
import com.xresch.cfw.logging.CFWLog;

public class CFWACMEClient {
	
	private static final String CERTIFICATE_NAME = "ACMECertificate";

	private static final Logger logger = CFWLog.getLogger(CFW.class.getName());
	
	private static boolean IS_INITIALIZED = false;
	private static boolean IS_FETCHED = false;
	
//	private static final String ACME_SERVER = "acme://letsencrypt.org";
//	private static final String ACME_EMAIL = "example@example.com";
//	private static final String DOMAIN = "example.com";
	
	private static String ACME_FOLDER;
	private static String ACCOUNT_KEY_FILE;
	private static String DOMAIN_KEY_FILE;
	private static String CERTIFICATE_FILE;
	private static String KEYSTORE_FILE;
	
	private static final Map<String, String> challenges = new HashMap<>();
	
	/******************************************************************
	 * 
	 ******************************************************************/
	private static void initialize() {
		
		if(!IS_INITIALIZED) {
			ACME_FOLDER = CFW.Properties.HTTPS_ACME_FOLDER;
			ACCOUNT_KEY_FILE = ACME_FOLDER + "/account.key";
			DOMAIN_KEY_FILE = ACME_FOLDER + "/domain.key";
			CERTIFICATE_FILE = ACME_FOLDER + "/certificate.pem";
			KEYSTORE_FILE = ACME_FOLDER + "/keystore.jks";
			
			IS_INITIALIZED = true;
		}
	}
	
	
	/******************************************************************
	 * 
	 ******************************************************************/
	public static void fetchCACertificate() throws Exception {
		
		initialize();
		
		//Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		
		new File(ACME_FOLDER).mkdirs(); // Ensure the directory exists

		if (certificateNeedsRenewal()) {
			new CFWLog(logger).info("ACME: Renew certificate from certificate authority.");
			renewCertificate();
		}
		
		IS_FETCHED = true;
	}

	
	/******************************************************************
	 * 
	 ******************************************************************/
	public static Map<String, String> getChallenges(){
		return challenges;
	}
	
	/******************************************************************
	 * 
	 ******************************************************************/
    private static boolean certificateNeedsRenewal() throws Exception {
        	
    	//-----------------------
		// Check if exists
        if (!Files.exists(Paths.get(KEYSTORE_FILE))) {
            return true;
        }
        
        //-----------------------
		// Get KeyStore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (InputStream in = new FileInputStream(KEYSTORE_FILE)) {
            keyStore.load(in, CFW.Properties.HTTPS_ACME_PASSWORD.toCharArray());
        }
        
        //-----------------------
		// Get Certificate
        X509Certificate cert = (X509Certificate) keyStore.getCertificate(CERTIFICATE_NAME);
        if (cert == null) {
            return true;
        }
        
        //-----------------------
		// Check expired
        int threshold = CFW.Properties.HTTPS_ACME_RENEWAL_THRESHOLD;
        long millisUntilExpiry = cert.getNotAfter().getTime() - new Date().getTime();
        long daysUntilExpiry = TimeUnit.MILLISECONDS.toDays(millisUntilExpiry);
       
        return daysUntilExpiry < threshold;

    }
    

	/******************************************************************
	 * 
	 ******************************************************************/
	private static void renewCertificate() throws Exception {
		
		//--------------------------------
		// Check terms accepted
		if(!CFW.Properties.HTTPS_ACME_ACCEPT_TERMS) {
			throw new Exception("ACME: You need to accept the terms of service."); 
		}
		
		//--------------------------------
		// Prepare
		KeyPair accountKeyPair = loadOrCreateKeyPair(ACCOUNT_KEY_FILE);
		Session session = new Session(CFW.Properties.HTTPS_ACME_URL);
		Account account = new AccountBuilder()
							.agreeToTermsOfService()
							.addEmail(CFW.Properties.HTTPS_ACME_EMAIL)
							.useKeyPair(accountKeyPair)
							.create(session)
							;

		//--------------------------------
		// Order 
		
		OrderBuilder orderBuilder = account.newOrder();
		
		String domains = CFW.Properties.HTTPS_ACME_DOMAINS;
		String[] domainsSplit = domains.split(",");
		
		for(String domain : domainsSplit) {
			if(!Strings.isNullOrEmpty(domain)) {
				orderBuilder.domain(domain.trim());
			}
		}
		
		Order order = orderBuilder.create();
		Authorization auth = order.getAuthorizations().get(0);
		Optional<Challenge> challengeOptional = auth.findChallenge(Http01Challenge.TYPE);
		
		if (challengeOptional.isEmpty()) {
			throw new Exception("ACME: HTTP-01 challenge is not available");
		}

		//--------------------------------
		// Challenge
		Http01Challenge challenge = (Http01Challenge)challengeOptional.get();

		challenges.put(challenge.getToken(), challenge.getAuthorization());
		
		new CFWLog(logger).info("ACME: Challenge Token: "+ challenge.getToken());
		new CFWLog(logger).info("ACME: Challenge Authorization:" + challenge.getAuthorization());

		challenge.trigger();
		
		//--------------------------------
		// Wait for challenge valid
		int MAX_CHALLENGE_TRIES = 20;
		int tries = 0;
		
		while (
			!challenge.getStatus().equals(Status.VALID)	
			&& tries < MAX_CHALLENGE_TRIES
		){
			TimeUnit.SECONDS.sleep(3);
			challenge.fetch();
			tries++;
			new CFWLog(logger).info("ACME: Challenge Count: "+ tries);
		}
		
		//--------------------------------
		// Finalize Order 
		KeyPair domainKeyPair = loadOrCreateKeyPair(DOMAIN_KEY_FILE);
		order.execute(domainKeyPair);
		order.fetch();
				
		//--------------------------------
		// Get Certificate
		Certificate certificate = order.getCertificate();
		new CFWLog(logger).info("ACME: Certificate:" + certificate.getCertificate().toString());
	
		// Note: This causes a compilation error, changed certificateNeedsRenewal()-method to 
		// check java Keystore file instead.
//        try (FileWriter fw = new FileWriter(CERTIFICATE_FILE)) {
//            for (X509Certificate cert : certificate.getCertificateChain()) {
//            	new CFWLog(logger).info("ACME: Store Certificate with Signature: " + cert.getSignature());
//                fw.write("-----BEGIN CERTIFICATE-----\n");
//                fw.write(Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(cert.getEncoded()));
//                fw.write("\n-----END CERTIFICATE-----\n");
//            }
//        }catch (Exception e) {
//			new CFWLog(logger).severe("ACME: Exception while storing certificate: " + CERTIFICATE_FILE);
//		}
        
        //--------------------------------
        // Store into Keystore
        String password = CFW.Properties.HTTPS_ACME_PASSWORD;
        java.security.KeyStore keyStore = java.security.KeyStore.getInstance("JKS");
        keyStore.load(null, null);
        keyStore.setKeyEntry(
        		  CERTIFICATE_NAME
        		, domainKeyPair.getPrivate()
        		, password.toCharArray()
        		, certificate.getCertificateChain().toArray(new java.security.cert.Certificate[0])
        	);
        try (FileOutputStream out = new FileOutputStream(KEYSTORE_FILE)) {
            keyStore.store(out, password.toCharArray());
        }
		
	}
	
	/******************************************************************
	 * 
	 ******************************************************************/
	private static KeyPair loadOrCreateKeyPair(String fileName) throws IOException {
		File file = new File(fileName);
		
		if (file.exists()) {
			return KeyPairUtils.readKeyPair( new FileReader(file) );
		} else {
			KeyPair keyPair = KeyPairUtils.createKeyPair(2048);
			new CFWLog(logger).info("ACME: Create KeyPair: " + fileName);
			KeyPairUtils.writeKeyPair(keyPair, new FileWriter(file) );
			return keyPair;
		}
	}

	/******************************************************************
	 * 
	 ******************************************************************/
	public static SslContextFactory getSSLContextFactory() {
		
		//----------------------------
		// Create Default Factory
		SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
		sslContextFactory.setKeyStorePath(CFWProperties.HTTPS_KEYSTORE_PATH);
		sslContextFactory.setKeyStorePassword(CFWProperties.HTTPS_KEYSTORE_PASSWORD);
		sslContextFactory.setKeyManagerPassword(CFWProperties.HTTPS_KEYMANAGER_PASSWORD);
		
		//----------------------------
		// Create ACME Factory
		if( CFW.Properties.HTTPS_ACME_ENABLED ) {
			
			//----------------------------
			// If fetched return ACME Factory
			if(IS_FETCHED) {
				sslContextFactory = new SslContextFactory.Server();
				sslContextFactory.setKeyStorePath(KEYSTORE_FILE);
				sslContextFactory.setKeyStorePassword(CFW.Properties.HTTPS_ACME_PASSWORD);
			}
			
		}
		
		return sslContextFactory;

	}
}
