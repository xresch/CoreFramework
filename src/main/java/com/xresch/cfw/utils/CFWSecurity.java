package com.xresch.cfw.utils;

import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0
 *          International
 **************************************************************************************************************/

//--------------------------------------------------
// Encryption Constants
// IMPORTANT!!! Do not change these values, you will
// break any application already using this mechanism.

public class CFWSecurity {

	// internal salt to make it even more complicated to recreate a password
	// Don't change this if you don't want to mess up existing passwords!
	public static final String INTERNAL_SALT = "1a@2v#3r%9s&7k?";

	private static Logger logger = CFWLog.getLogger(CFWSecurity.class.getName());

	private static PolicyFactory htmlPolicy = new HtmlPolicyBuilder().allowCommonBlockElements()
			.allowCommonInlineFormattingElements().allowStandardUrlProtocols().allowStyling()
			.allowElements("a", "table", "thead", "tbody", "th", "tr", "td", "div", "i", "b", "strong", "ol", "ul",
					"li", "font", "h1", "h2", "h3", "h4", "h5", "h6")
			.allowAttributes("href").onElements("a").allowAttributes("size").onElements("font")
			.allowAttributes("class", "style").globally().toFactory();

	private static String ENCRYPT_PREFIX = "cfwsecenc:";
	private static String ENCRYPT_ALGORITHM = "AES";
	
	private static final String[][] htmlEscapes = new String[][] { 
		{ "&", "&amp;" }, 
		{ "<", "&lt;" }, 
		{ ">", "&gt;" },
		//{ "\"", "&quot;" },  allow quotes to not mess up JSON strings
		{ "\'", "&#x27;" }, 
		// { "/", "&#x2F;" }, allow forward slashes for URLs
		};
		
	/******************************************************************************
	 * Creates a salted SHA512 password hash and returns a string of 127 or less
	 * bytes. Removes the first character of the resulting hash string. This adds as
	 * well some more complexity to the hashing algorithm.
	 * 
	 * @param password
	 * @param salt
	 * @return hash with 127 or less bytes
	 ******************************************************************************/
	public static String createPasswordHash(String password, String salt) {

		try {
			// getInstance() method is called with algorithm SHA-512
			MessageDigest md = MessageDigest.getInstance("SHA-512");

			byte[] messageDigest = md.digest((INTERNAL_SALT + password + salt).getBytes());

			// Convert byte array into signum representation
			BigInteger no = new BigInteger(1, messageDigest);

			// Convert message digest into hex value
			String hashtext = no.toString(16);

			return hashtext.substring(1);
		}

		// For specifying wrong message digest algorithms
		catch (NoSuchAlgorithmException e) {
			new CFWLog(logger).severe("Error creating password hash.", e);
			throw new RuntimeException(e);
		}
	}

	/******************************************************************************
	 * Creates a random Salt for a Password.
	 * 
	 * @param byteCount number of bytes to create
	 * @return
	 ******************************************************************************/
	public static String createPasswordSalt(int byteCount) {

		StringBuilder builder = new StringBuilder();

		Random random = CFWRandom.getInstance();
		for (int i = 0; i < byteCount; i++) {
			builder.append(CFWRandom.ALPHA_NUMS_SPECIALS.charAt(random.nextInt(CFWRandom.ALPHA_NUMS_SPECIALS.length() - 1)));
		}

		return builder.toString();

	}

	/******************************************************************************
	 * Masks the last X percentage of characters of a string with an asterisk.
	 * 
	 * @param string to mask
	 * @param percentage from 1 to 100
	 * @return
	 ******************************************************************************/
	public static String maskString(String string, int percentage) {

		int length = string.length();
		int charsToMaskCount = Math.round((float)length / 100 * percentage);

		String mask ="";
		for(int i = 0; i < charsToMaskCount; i++) { mask += "*";}
		
		string = string.substring(0, length-charsToMaskCount)+mask;
		
		return string;

	}
	/*************************************************************************************
	 * Escape HTML entities to avoid potential html code in a string.
	 *************************************************************************************/
	public static String escapeHTMLEntities(String string) {

		if (string != null) {
			for (String[] esc : htmlEscapes) {
				string = string.replace(esc[0], esc[1]);
			}
		}
		return string;
	}
	
	/*************************************************************************************
	 * return true if the string contains any of the characters or strings
	 *************************************************************************************/
	public static boolean containsSequence(String string, String... sequences) {

		if (string != null) {
			for (String containsThis : sequences) {
				if (string.contains(containsThis) ) return true;
			}
		}
		return false;
	}
	
	/*************************************************************************************
	 * Escape SQL entities to prevent SQL injection.
	 * Code from OWASP ESAPI under BSD License:
	 * https://github.com/ESAPI/esapi-java-legacy/blob/develop/src/main/java/org/owasp/esapi/codecs/MySQLCodec.java
	 * 
	 * <pre>
	 *   NUL (0x00) --> \0  [This is a zero, not the letter O]
	 *   BS  (0x08) --> \b
	 *   TAB (0x09) --> \t
	 *   LF  (0x0a) --> \n
	 *   CR  (0x0d) --> \r
	 *   SUB (0x1a) --> \Z
	 *   "   (0x22) --> \"
	 *   %   (0x25) --> \%
	 *   '   (0x27) --> \'
	 *   \   (0x5c) --> \\
	 *   _   (0x5f) --> \_ 
	 *   <br>
	 *   all other non-alphanumeric characters with ASCII values less than 256  --> \c
	 *   where 'c' is the original non-alphanumeric character.
	 *************************************************************************************/
	public static String encodeSQLParameter( Character c ) {
		char ch = c.charValue();
		if ( ch == 0x00 ) return "\\0";
		if ( ch == 0x08 ) return "\\b";
		if ( ch == 0x09 ) return "\\t";
		if ( ch == 0x0a ) return "\\n";
		if ( ch == 0x0d ) return "\\r";
		if ( ch == 0x1a ) return "\\Z";
		if ( ch == 0x22 ) return "\\\"";
		if ( ch == 0x25 ) return "\\%";
		if ( ch == 0x27 ) return "\\'";
		if ( ch == 0x5c ) return "\\\\";
		if ( ch == 0x5f ) return "\\_";
	    return "\\" + c;
	}

	
	/******************************************************************************
	 * Sanitizes HTML with OWASP HTML sanitizer. Removes Javascript.
	 * 
	 * @param byteCount number of bytes to create
	 * @return
	 ******************************************************************************/
	public static String sanitizeHTML(String htmlString) {

		return htmlPolicy.sanitize(htmlString);

	}
	
	
	/******************************************************************************************************
	 * Returns the value encrypted.
	 ******************************************************************************************************/
	public static String encryptValue(String value, String encryptionSalt) {
		if(encryptionSalt == null 
		|| value == null ) {
			return value;
		}else {
			String encryptedValue = null;
			try { 
				//---------------------------
				// Prepare Cipher
				Key key = new SecretKeySpec(encryptionSalt.getBytes(), ENCRYPT_ALGORITHM);
		        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGORITHM);
		        cipher.init(Cipher.ENCRYPT_MODE, key);
		      
		        //---------------------------
		      	// Encode Value
		        byte[] encodedBytes = cipher.doFinal(value.toString().getBytes());
		        encryptedValue = Base64.getEncoder().encodeToString(encodedBytes);
		        encryptedValue = ENCRYPT_PREFIX + encryptedValue;
		       
			}catch (Exception e) {
				new CFWLog(logger)
					.severe("Could not encrypt the value.", e);
			}
			
			return encryptedValue;
		}
    }
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public static String decryptValue(String value, String encryptionSalt) {
		if(encryptionSalt == null 
		|| value == null
		|| !value.toString().startsWith(ENCRYPT_PREFIX)) {
			return value;
		}else {
			String decryptedValue = null;
			try {
				Key key = new SecretKeySpec(encryptionSalt.getBytes(), ENCRYPT_ALGORITHM);
		        Cipher cipher;
	
					cipher = Cipher.getInstance(ENCRYPT_ALGORITHM);
	
		        cipher.init(Cipher.DECRYPT_MODE, key);
		        String encryptedValue = value.toString().replaceFirst(ENCRYPT_PREFIX, "");
		        byte[] decryptedBytes = Base64.getDecoder().decode(encryptedValue);
		        byte[] decodedValue = cipher.doFinal(decryptedBytes);
		        decryptedValue = new String(decodedValue);
		        
			} catch (Exception e) {
				new CFWLog(logger)
					.severe("Could not decrypt value.", e);
			}
			
			return decryptedValue;
		}
    }

}
