package com.xresch.cfw.features.core.auth.saml;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.common.base.Strings;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest.Builder;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWProperties;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.features.core.auth.SSOProviderSettings;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022 
 * @license MIT-License
 **************************************************************************************************************/
public class SSOProviderSettingsSAML extends SSOProviderSettings {
	
	//public static final String GRANTTYPE_CLIENT_CREDENTIALS = "client_credentials";
	public static final String GRANTTYPE_AUTHORIZATION_CODE = "authorization_code";
	
	public static final String PROPERTY_SSO_STATE = "ssoState";
	public static final String PROPERTY_SSO_CODE_VERIFIER = "ssoCodeVerifier";
	public static final String PROPERTY_SSO_PROVIDER_ID = "ssoProviderID";
	public static final String PROPERTY_SSO_TARGET_URL = "ssopTargetURL";

	private static Logger logger = CFWLog.getLogger(SSOProviderSettingsSAML.class.getName());
	
	public static final String SETTINGS_TYPE = "SSO SAML";
	
	private static Scope SCOPE = null;
	//public static final Scope DEFAULT_SCOPE = new Scope("openid", "allatclaims");
	
	public enum SSOProviderSettingsSAMLFields{
		IDP_ENTITY_URI,
		IDP_SSO_URL,
		IDP_X509_CERTIFICATE,
		SP_ENTITY_URI,
		SP_X509_CERTIFICATE,
		SP_X509_CERTIFICATE_NEW,
		SP_PKCS8_PRIVATE_KEY,
	}
	
//	x String idpEntityIDURL			= "http://localhost:8080/realms/myrealm/protocol/saml/descriptor";
//	String idpSSOEnpointURL 		= "http://localhost:8080/realms/myrealm/protocol/saml";
//	String idpSLOEnpointURL 		= "http://localhost:8080/realms/myrealm/protocol/saml";
//	x String idpX509Cert 				= "MIICnTCCAYUCBgGBZ5UxFzANBgkqhkiG9w0BAQsFADASMRAwDgYDVQQDDAdteXJlYWxtMB4XDTIyMDYxNTEzMzUzOVoXDTMyMDYxNTEzMzcxOVowEjEQMA4GA1UEAwwHbXlyZWFsbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAJdXvIQe5ygZy6PdQzDIfh60c1MwmspRRPM9NUlT3katnzjHOemQDw0cLoPD0YZVmlFadFyOp5+xac8sIsobCgMU4kbeYCuQsSiHf/0cqOjcWn/g3elPKGMsuoNjvm3f+fRru9gZXp5puuUop9eZTbvp6/b6wTjKQbamRMtCq9BVYP3Gs+cPwu9dLD4BegRTniwUIvDEsiU9Xf3yLkF1ZQnIck2+3h8bLzkw9i19mKxRtKsJMcrz0x28xRYrWWEdNfmld6GFKmxejYKM9OLWPdGsnvXicovv1/xAjflgEd+x7pEWVb47cekw1aerJbGwGo3745rdGXAKaVfDd+LyNW0CAwEAATANBgkqhkiG9w0BAQsFAAOCAQEAGJS5EqpPhEI46YXovnxtCawj45ti0K2ASeYe5q6I+LbXvXwNgSVof1OKjkNIIorRH1Z67LYPxUR5XK3G9G4GDFNnZjof+6z5bvmragcuGdSE0DPmWL2VYr1XOGFCDNJgyWurLb3zn11I9Ykb5AFU6f9VNQnIz07PR4dpksM9imDLZLvMCoDjdoAGPCu6cXxo0DirNMt00KSX4awEQDzMyQG+4B1xN8k1hF0Wra9JMBosEHX8X49Tsg4164NzUFVHbW1lfFzQ8N761J1uEp4JQx6bpWdmkPPjBuselO60W7lQGQHsvkit6eLi9f1uX2BwPJKzad//WrCwE1Q7LVyjWw==";
//	
//	x String spX509cert				= "MIICrzCCAZcCBgGEyMwWgzANBgkqhkiG9w0BAQsFADAbMRkwFwYDVQQDDBBjZndsb2NhbGhvc3RzYW1sMB4XDTIyMTEzMDEzNDcwOVoXDTMyMTEzMDEzNDg0OVowGzEZMBcGA1UEAwwQY2Z3bG9jYWxob3N0c2FtbDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAISJdMmbFauQYPzVF9clqCcei7swYSVQjMrRDCKG1HfgKd+29emUJ+hHslOzowt+fJuW+CJR1ARS1g5Bd79hjRpepSdBrex6AtGX0GKwZu3hTnmDn4+dGgUgvGjOgmsHpkA6Ln/4g6RltPTFHmux1cbtshWktQZ6kc+7fCzHVWKrwnJTo2TqmR7SDV8vseIq3VmVTHqQs44e7UdiwXm3VZt0CWOqVFf/YPxxhqUChYrsrVd40gWeG3JJ0dsIhgRVYzSYd78Wmrz2Hzqr4TAcAPTNyDP9j1FZ8HS7Z/8b7XUFeRVK9Eel1XQvHUQyeFwGj6QhIxqcFzdKOC3g7X33FUMCAwEAATANBgkqhkiG9w0BAQsFAAOCAQEAUGFl7UKzkJagkQvCwhUfVYPw4zwvdDOgaMYTadPqlTZbWPNKewVIATtL7mHJ5Pu9bITwEC4tQxGa0hoA/OvYF2N6UCW6Xm6gaJxHAK9T3bPSIi1tyQBdpIL8lYTHsmTpUWzEKkmXHRcPrRh3SPeSxjv/CZ5QAWPcHBWEllFFs8mijODKQ+MIxtE4y954HrNBjI9nz9v33BAyAx31H4OKh9jw0jnzpZqQbs9v4NyuDbJBo10nrSgB6OsvwNAMlzWk5Es+3IoRG5JY0rehIqrsJokxmn+dCCdEnua1ojhyl1CJlSjx2YGyRqClasDmw1hKTI8itgIaMErFaYsY+AO/vQ==";
//	x String spX509certNew			= spX509cert;
//	x String spPrivateKey				= "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCEiXTJmxWrkGD81RfXJagnHou7MGElUIzK0QwihtR34CnftvXplCfoR7JTs6MLfnyblvgiUdQEUtYOQXe/YY0aXqUnQa3segLRl9BisGbt4U55g5+PnRoFILxozoJrB6ZAOi5/+IOkZbT0xR5rsdXG7bIVpLUGepHPu3wsx1Viq8JyU6Nk6pke0g1fL7HiKt1ZlUx6kLOOHu1HYsF5t1WbdAljqlRX/2D8cYalAoWK7K1XeNIFnhtySdHbCIYEVWM0mHe/Fpq89h86q+EwHAD0zcgz/Y9RWfB0u2f/G+11BXkVSvRHpdV0Lx1EMnhcBo+kISManBc3Sjgt4O199xVDAgMBAAECggEATy6YGYKP9cnyR9s/vQgAaC61qIYE4/g1xU4Tg+Utttiz67YxQPWEyh9biOo/tLRC2eneIRLmKhcbT7UJR8uOM3zsCoIQ2MEkQfgDRZLCS8hZy/s5LuHbE8k1ByCphiwxxRl9gnMEowkojTvfKtQ6Nfj4djnK9S3xQzxtuYr1llbOUZuRDmImvCA1GLmLKlVuZp9pzcmI9uG/ZMcp7u/ESW+Gw2FVarvfooBYaRql7CaqI7RDj/l4mZKgvlOmqY2M2oixIZle/KqDPBJlTJNXZE4gU1drypVXb11g0mwDFm70VdvmwFi3HAh/OpGFiCcBLebov2dtmG3IvQVQynLTcQKBgQDLOUW6HGHkmLjYVDouS5hdlv9Odnv5ZiHcNHIy0kXE/bZ8NxjFgSac+N6nIak7Hvsb2vUpPreK/JO6F5FaXB2LwZSFXKISD8p6vArrcnNQ4v44J1IAeSgcUYaDW1ka6I+4djt8mvEyORVqpr0HBT1MyC4j4DOc+iTds1zcy88kFwKBgQCm9MhGMVQJ3BV1lSk+4npUKZdczNY3XJPt5QLYwiKFqckBVyUv7dcJg3MaqXkgHR09/XWvaxVShbjZlAVpNrZNbPASlxx8Su3yrPYhuV2aMd1XXbUSAKIQkqPLVnxyk6o16a/VpCgy5fLGCjYw0kPET7B44/iJGELOq/VB2i+XtQKBgHFTVMC+BxD04U8xWOhsG2FFTMWyaNvgyk0DqhMREvsRCGwoRVYN+Txbw72rlbV0R093QHNpl+yXgMGrVtDuwUMoBeyAhZhQ2farWeOGBSw8CMvDkYTWCzoPdFVX4U6SFWMl+3I27P22u2yn4o1BrLdegexboCyPiXNgDA7MUIytAoGAFUbcvxVKQHdrxLBdsUXrkQ472/e+1Q9XStoEotsayy34D9OrSZBl9zBpWtx+MzmCoIPMm65p6TphdFkI13/Be9yGO9hGKRDjginItEOLSjtQmfG3QbQS80m81g0PjwqChpxhbDifZt0nM1XZ0h75w+rj8oQbCF2vJeeEOgA0UIECgYAHTShM1lhSgiRnCLafgjwB6BlgsRgwbiu+2AbYXRjbfLMjmaDfzFbPBnyb1IamZIVcPZxsG4DZykfiAozXukE8U648t4TsdM+VZmDx5hyLTAYkzILguEHkp98ghoWpuSR8k/g0XOUhscDoSVNx5T5bn7cNSfrU+23ihFqUZvvF9A==";	
	
		
	private CFWField<String> idpEntityURI = CFWField.newString(FormFieldType.TEXT, SSOProviderSettingsSAMLFields.IDP_ENTITY_URI)
			.setLabel("IdP Entity URI")
			.setDescription("Identifier of the IdP entity  (must be a URI).");
	
	private CFWField<String> idpSSOURL = CFWField.newString(FormFieldType.TEXT, SSOProviderSettingsSAMLFields.IDP_SSO_URL)
			.setLabel("IdP SSO URL")
			.setDescription(" URL Target of the IdP where the SP will send the Authentication Request Message.");
	
	private CFWField<String> idpX509Cert = CFWField.newString(FormFieldType.TEXTAREA, SSOProviderSettingsSAMLFields.IDP_X509_CERTIFICATE)
			.setLabel("IdP X509 Certificate")
			.setDescription("The certificate of the IdP.");
	
	private CFWField<String> spEntityURI = CFWField.newString(FormFieldType.TEXT, SSOProviderSettingsSAMLFields.SP_ENTITY_URI)
			.setLabel("SP Entity URI")
			.setDescription("Identifier of the SP entity.");
	

	private CFWField<String> spX509Cert = CFWField.newString(FormFieldType.TEXTAREA, SSOProviderSettingsSAMLFields.SP_X509_CERTIFICATE)
			.setLabel("SP X509 Certificate")
			.setDescription("The certificate of the Service Provider.");

	private CFWField<String> spX509CertNew = CFWField.newString(FormFieldType.TEXTAREA, SSOProviderSettingsSAMLFields.SP_X509_CERTIFICATE_NEW)
			.setLabel("SP X509 Certificate New")
			.setDescription("Future Service Provider certificate, to be used during key roll over.");
	
	private CFWField<String> spPrivateKey = CFWField.newString(FormFieldType.TEXTAREA, SSOProviderSettingsSAMLFields.SP_PKCS8_PRIVATE_KEY)
			.setLabel("SP PKCS8 Private Key")
			.setDescription("The private key of the Service Provider.");
	
	
	
	private OIDCProviderMetadata providerMetadata = null;
	
	public SSOProviderSettingsSAML() {
		initializeFields();
	}
		
	private void initializeFields() {
		this.addFields(
				  idpEntityURI
				, idpSSOURL
				, idpX509Cert
				, spEntityURI
				, spX509Cert
				, spX509CertNew
				, spPrivateKey
			);
	}
		
		
	@Override
	public boolean isDeletable(int settingsID) {
		return true;
	}
	
	public boolean isDefined() {
		
		return true;
		
//		if(idpMetadataURL.getValue() != null
//		&& clientID.getValue() != null) {
//			return true;
//		}
		
//		return false;
	}
	
	/******************************************************************************
	 * Create Scope
	 * 
	 ******************************************************************************/
	public Scope getScope() {
		
		if(SCOPE == null) {
			SCOPE = new Scope("openid", "profile", "email");
			
			if( !Strings.isNullOrEmpty(spX509CertNew.getValue()) ) {
				String scopesString = spX509CertNew.getValue();
				for(String scope : scopesString.split(",")) {
					SCOPE.add(scope.trim());
				}
			}
		}
		
		return SCOPE;
	}
	/******************************************************************************
	 * NimbusDS connect2id
	 * https://connect2id.com/products/nimbus-oauth-openid-connect-sdk/guides/java-cookbook-for-openid-connect-public-clients
	 * @throws ParseException 
	 * @throws  
	 * 
	 ******************************************************************************/
	public URI createRedirectURI(HttpServletRequest request, HttpServletResponse response, String targetURL) {
		
		return URI.create( "/cfw/saml2/login?url="+CFW.HTTP.encode(targetURL) );
		
	}
			

	public String idpEntityURI() {
		return idpEntityURI.getValue();
	}
	
	public SSOProviderSettingsSAML idpEntityURI(String value) {
		this.idpEntityURI.setValue(value);
		return this;
	}
	
	public String idpSSOURL() {
		return idpSSOURL.getValue();
	}
	
	public SSOProviderSettingsSAML idpSSOURL(String value) {
		this.idpSSOURL.setValue(value);
		return this;
	}
		
	public String idpX509Cert() {
		return idpX509Cert.getValue();
	}
	
	public SSOProviderSettingsSAML idpX509Cert(String value) {
		this.idpX509Cert.setValue(value);
		return this;
	}
	
	

	public String spEntityURI() {
		return spEntityURI.getValue();
	}
	
	public SSOProviderSettingsSAML spEntityURI(String value) {
		this.spEntityURI.setValue(value);
		return this;
	}
	
	public String spX509Cert() {
		return spX509Cert.getValue();
	}
	
	public SSOProviderSettingsSAML spX509Cert(String value) {
		this.spX509Cert.setValue(value);
		return this;
	}
	
	public String spX509CertNew() {
		return spX509CertNew.getValue();
	}
	
	public SSOProviderSettingsSAML spX509CertNew(String value) {
		this.spX509CertNew.setValue(value);
		return this;
	}
	
	public String spPrivateKey() {
		return spPrivateKey.getValue();
	}
	
	public SSOProviderSettingsSAML spPrivateKey(String value) {
		this.spPrivateKey.setValue(value);
		return this;
	}
	
	@Override
	protected String getSettingsType() {
		return SETTINGS_TYPE;
	}
	
	
	/**********************************************************************************
	 * Returns SAML properties for the onelogin framework
	 * 
	 **********************************************************************************/
	public Properties createSAMLProperties(HttpServletRequest request) {
		
		Properties properties = new Properties();
		
		//========================================
		// Specify Dynamic Values
		String serverURL 				= CFW.HTTP.getServerURL(request);
		String spAssertionConsumerService = serverURL+"/cfw/saml2/acs";
		
		String idpEntityIDURI			= this.idpEntityURI();
		String idpSSOEnpointURL 		= this.idpSSOURL();
		String idpSLOEnpointURL 		= "";
		String idpX509Cert 				= this.idpX509Cert();
		
		String spEntityID 				= this.spEntityURI();
		String spX509cert				= this.spX509Cert();
		String spX509certNew			= this.spX509CertNew();
		String spPrivateKey				= this.spPrivateKey();
		Boolean toogleSigning			= false;
	
		//  If 'strict' is True, then the Java Toolkit will reject unsigned
		//  or unencrypted messages if it expects them signed or encrypted
		//  Also will reject the messages if not strictly follow the SAML
		properties.put("onelogin.saml2.strict", "false");
	
		// Enable debug mode (to print errors)
		properties.put("onelogin.saml2.debug", "true");
	
		//////////////////////////////////////////////////////////////////////////////////////////////////
		//// Service Provider Data that we are deploying 
		//////////////////////////////////////////////////////////////////////////////////////////////////
	
		//  Identifier of the SP entity  (must be a URI)
		properties.put("onelogin.saml2.sp.entityid", spEntityID);
	
		// Specifies info about where and how the <AuthnResponse> message MUST be
		// returned to the requester, in this case our SP.
		// URL Location where the <Response> from the IdP will be returned
		properties.put("onelogin.saml2.sp.assertion_consumer_service.url", spAssertionConsumerService);
	
		// SAML protocol binding to be used when returning the <Response>
		// message.  Onelogin Toolkit supports for this endpoint the
		// HTTP-POST binding only
		properties.put("onelogin.saml2.sp.assertion_consumer_service.binding", "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
	
		// Specifies info about where and how the <Logout Response> message MUST be
		// returned to the requester, in this case our SP.
		properties.put("onelogin.saml2.sp.single_logout_service.url", "http://localhost:8080/java-saml-tookit-jspsample/sls.jsp");
	
		// SAML protocol binding to be used when returning the <LogoutResponse> or sending the <LogoutRequest>
		// message.  Onelogin Toolkit supports for this endpoint the
		// HTTP-Redirect binding only
		properties.put("onelogin.saml2.sp.single_logout_service.binding", "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect");
	
		// Specifies constraints on the name identifier to be used to
		// represent the requested subject.
		// Take a look on core/src/main/java/com/onelogin/saml2/util/Constants.java to see the NameIdFormat supported
		properties.put("onelogin.saml2.sp.nameidformat", "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");

		if(spX509cert != null) {
			// Usually x509cert and privateKey of the SP are provided by files placed at
			// the certs folder. But we can also provide them with the following parameters
			properties.put("onelogin.saml2.sp.x509cert", spX509cert);
		}
		
		if(spX509certNew != null) {
			// Future SP certificate, to be used during SP Key roll over
			properties.put("onelogin.saml2.sp.x509certNew",  spX509certNew);
		}
		
		if(spPrivateKey != null) {
			// Requires Format PKCS#8   BEGIN PRIVATE KEY       
			// If you have     PKCS#1   BEGIN RSA PRIVATE KEY  convert it by openssl pkcs8 -topk8 -inform pem -nocrypt -in sp.rsa_key -outform pem -out sp.pem
			properties.put("onelogin.saml2.sp.privatekey",  spPrivateKey);
		}
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//// Identity Provider Data that we want connect with our SP ////
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
		if(idpEntityIDURI != null) {
			// Identifier of the IdP entity  (must be a URI)
			properties.put("onelogin.saml2.idp.entityid", idpEntityIDURI);
		}

		if(idpSSOEnpointURL != null) {
			// SSO endpoint info of the IdP. (Authentication Request protocol)
			// URL Target of the IdP where the SP will send the Authentication Request Message
			// Example: properties.put("onelogin.saml2.idp.single_sign_on_service.url", "http://localhost:8080/java-saml-tookit-jspsample/acs.jsp
			properties.put("onelogin.saml2.idp.single_sign_on_service.url", idpSSOEnpointURL);
		}
		
		// SAML protocol binding to be used when returning the <Response>
		// message.  Onelogin Toolkit supports for this endpoint the
		// HTTP-Redirect binding only
		properties.put("onelogin.saml2.idp.single_sign_on_service.binding", "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect");
	
		if(idpSLOEnpointURL != null) {
			// SLO endpoint info of the IdP.
			// URL Location of the IdP where the SP will send the SLO Request
			properties.put("onelogin.saml2.idp.single_logout_service.url", idpSLOEnpointURL);
		}
		
		// Optional SLO Response endpoint info of the IdP.
		// URL Location of the IdP where the SP will send the SLO Response. If left blank, same URL as properties.put("onelogin.saml2.idp.single_logout_service.url will be used.
		// Some IdPs use a separate URL for sending a logout request and response, use this property to set the separate response url
		properties.put("onelogin.saml2.idp.single_logout_service.response.url", "");
	
		// SAML protocol binding to be used when returning the <Response>
		// message.  Onelogin Toolkit supports for this endpoint the
		// HTTP-Redirect binding only
		properties.put("onelogin.saml2.idp.single_logout_service.binding", "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect");
		
		if(idpX509Cert != null) {
			// Public x509 certificate of the IdP
			properties.put("onelogin.saml2.idp.x509cert", idpX509Cert);
		}
		// Instead of using the whole x509cert you can use a fingerprint in order to
		// validate a SAMLResponse (but you still need the x509cert to validate LogoutRequest and LogoutResponse using the HTTP-Redirect binding).
		// But take in mind that the fingerprint, is a hash, so at the end is open to a collision attack that can end on a signature validation bypass,
		// that why we don't recommend it use for production environments.
		// (openssl x509 -noout -fingerprint -in "idp.crt" to generate it,
		// or add for example the -sha256 , -sha384 or -sha512 parameter)
		//
		// If a fingerprint is provided, then the certFingerprintAlgorithm is required in order to
		// let the toolkit know which Algorithm was used. Possible values: sha1, sha256, sha384 or sha512
		// 'sha1' is the default value.
		// properties.put("onelogin.saml2.idp.certfingerprint", "
		// properties.put("onelogin.saml2.idp.certfingerprint_algorithm", "sha1
	
		// Security settings
		//
	
		// Indicates that the nameID of the <samlp:logoutRequest> sent by this SP
		// will be encrypted.
		properties.put("onelogin.saml2.security.nameid_encrypted", "false");
	
		// Indicates whether the <samlp:AuthnRequest> messages sent by this SP
		// will be signed.              [The Metadata of the SP will offer this info]
		properties.put("onelogin.saml2.security.authnrequest_signed", toogleSigning);
	
		// Indicates whether the <samlp:logoutRequest> messages sent by this SP
		// will be signed.
		properties.put("onelogin.saml2.security.logoutrequest_signed", toogleSigning);
	
		// Indicates whether the <samlp:logoutResponse> messages sent by this SP
		// will be signed.
		properties.put("onelogin.saml2.security.logoutresponse_signed", toogleSigning);
	
		// Indicates a requirement for the <samlp:Response>, <samlp:LogoutRequest> and
		// <samlp:LogoutResponse> elements received by this SP to be signed.
		properties.put("onelogin.saml2.security.want_messages_signed", toogleSigning);
	
		// Indicates a requirement for the <saml:Assertion> elements received by this SP to be signed.
		properties.put("onelogin.saml2.security.want_assertions_signed", toogleSigning);
	
		// Indicates a requirement for the Metadata of this SP to be signed.
		// Right now supported null (in order to not sign) or true (sign using SP private key) 
		properties.put("onelogin.saml2.security.sign_metadata", "");
	
		// Indicates a requirement for the Assertions received by this SP to be encrypted
		properties.put("onelogin.saml2.security.want_assertions_encrypted", "false");
	
		// Indicates a requirement for the NameID received by this SP to be encrypted
		properties.put("onelogin.saml2.security.want_nameid_encrypted", "false");
	
	
		// Authentication context.
		// Set Empty and no AuthContext will be sent in the AuthNRequest,
		// Set comma separated values urn:oasis:names:tc:SAML:2.0:ac:classes:urn:oasis:names:tc:SAML:2.0:ac:classes:Password
		properties.put("onelogin.saml2.security.requested_authncontext", "urn:oasis:names:tc:SAML:2.0:ac:classes:urn:oasis:names:tc:SAML:2.0:ac:classes:Password");
	
		// Allows the authn comparison parameter to be set, defaults to 'exact'
		properties.put("onelogin.saml2.security.requested_authncontextcomparison", "exact");
	
		// Indicates if the SP will validate all received xmls.
		// (In order to validate the xml, 'strict' and 'wantXMLValidation' must be true).
		properties.put("onelogin.saml2.security.want_xml_validation", "true");
	
		// Algorithm that the toolkit will use on signing process. Options:
		// Algorithm that the toolkit will use on signing process. Options:
		//  'http://www.w3.org/2000/09/xmldsig#rsa-sha1'
		//  'http://www.w3.org/2000/09/xmldsig#dsa-sha1'
		//  'http://www.w3.org/2001/04/xmldsig-more#rsa-sha256'
		//  'http://www.w3.org/2001/04/xmldsig-more#rsa-sha384'
		//  'http://www.w3.org/2001/04/xmldsig-more#rsa-sha512'
		properties.put("onelogin.saml2.security.signature_algorithm", "http://www.w3.org/2000/09/xmldsig#rsa-sha1");
																	 
		// Organization
//		properties.put("onelogin.saml2.organization.name", "SP Java");
//		properties.put("onelogin.saml2.organization.displayname", "SP Java Example");
//		properties.put("onelogin.saml2.organization.url", "http://sp.example.com");
//		properties.put("onelogin.saml2.organization.lang", "en");
	
		// Contacts
//		properties.put("onelogin.saml2.contacts.technical.given_name", "Technical Guy");
//		properties.put("onelogin.saml2.contacts.technical.email_address", "technical@example.com");
//		properties.put("onelogin.saml2.contacts.support.given_name", "Support Guy");
//		properties.put("onelogin.saml2.contacts.support.email_address", "support@example.com");
	
		// Prefix used in generated Unique IDs.
		// Optional, defaults to ONELOGIN_ or full ID is like ONELOGIN_ebb0badd-4f60-4b38-b20a-a8e01f0592b1.
		// At minimun, the prefix can be non-numeric character such as "_".
		// properties.put("onelogin.saml2.unique_id_prefix", "_
		
		return properties;
	}
			
}
