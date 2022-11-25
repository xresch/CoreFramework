package com.xresch.cfw.features.core.auth.openid;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
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
public class SSOProviderSettingsOpenID extends SSOProviderSettings {
	
	//public static final String GRANTTYPE_CLIENT_CREDENTIALS = "client_credentials";
	public static final String GRANTTYPE_AUTHORIZATION_CODE = "authorization_code";
	
	public static final String PROPERTY_SSO_STATE = "ssoState";
	public static final String PROPERTY_SSO_CODE_VERIFIER = "ssoCodeVerifier";
	public static final String PROPERTY_SSO_TARGET_URL = "ssopTargetURL";

	private static Logger logger = CFWLog.getLogger(SSOProviderSettingsOpenID.class.getName());
	
	//public static final String SETTINGS_TYPE = "OpenID Connect Provider";
	public static final String SETTINGS_TYPE = "SSO OpenID";
	
	private Scope SCOPE = null;
	//public static final Scope DEFAULT_SCOPE = new Scope("openid", "allatclaims");
	
	public enum SSOOpenIDConnectProviderFields{
		PROVIDER_URL,
		WELL_KNOWN_PATH,
		CLIENT_ID,
		CLIENT_SECRET,
		ADDITIONAL_SCOPE,
		GRANT_TYPE,
		RESOURCE,
		JSON_CUSTOM_PARAMETERS
	}
		
//	private CFWField<String> providerURL = CFWField.newString(FormFieldType.TEXT, SSOOpenIDConnectProviderFields.PROVIDER_URL)
//			.setDescription("The url of the OpenID Connect provider.");
	
	private CFWField<String> grantType = CFWField.newString(FormFieldType.SELECT, SSOOpenIDConnectProviderFields.GRANT_TYPE)
			.setDescription("The grant type used for this client.")
			.addOption(GRANTTYPE_AUTHORIZATION_CODE, "Authorization Code Grant with PKCE")
			//.addOption(GRANTTYPE_CLIENT_CREDENTIALS, "Client Credentials")
			.setValue(GRANTTYPE_AUTHORIZATION_CODE);
	
	private CFWField<String> wellknownPath = CFWField.newString(FormFieldType.TEXT, SSOOpenIDConnectProviderFields.WELL_KNOWN_PATH)
			.setDescription("The path to the .well-known provider configuration.");
	
	private CFWField<String> clientID = CFWField.newString(FormFieldType.TEXT, SSOOpenIDConnectProviderFields.CLIENT_ID)
			.setDescription("The id used for this client.");
	
//	private CFWField<String> clientSecret = CFWField.newString(FormFieldType.TEXT, SSOOpenIDConnectProviderFields.CLIENT_SECRET)
//			.setDescription("The secret used for this client.")
//			.setValue("");
	
	private CFWField<String> resource = CFWField.newString(FormFieldType.TEXT, SSOOpenIDConnectProviderFields.RESOURCE)
			.setDescription("(Optional)The value for the resource parameter used for client credential grant flow.")
			.setValue("");
	
	private CFWField<String> additionalScope = CFWField.newString(FormFieldType.TEXT, SSOOpenIDConnectProviderFields.ADDITIONAL_SCOPE)
			.setDescription("Comma separated list of additional scopes. For ADFS, add 'allatclaims'.")
			.setValue(null);
	
	private CFWField<LinkedHashMap<String, String>> customParams = CFWField.newValueLabel(SSOOpenIDConnectProviderFields.JSON_CUSTOM_PARAMETERS)
			.setLabel("Custom Parameters")
			.setDescription("Custom parameters that should be added to the authentication request.");
	
	private OIDCProviderMetadata providerMetadata = null;
	
	public SSOProviderSettingsOpenID() {
		initializeFields();
	}
		
	private void initializeFields() {
		this.addFields(
				//providerURL
				  grantType
				, wellknownPath
				, clientID
				//, clientSecret
				, resource
				, additionalScope
				, customParams
			);
	}
		
		
	@Override
	public boolean isDeletable(int settingsID) {
		return true;
	}
	
	public boolean isDefined() {
		if(wellknownPath.getValue() != null
		&& clientID.getValue() != null) {
			return true;
		}
		
		return false;
	}
	
	/******************************************************************************
	 * Create Scope
	 * 
	 ******************************************************************************/
	public Scope getScope() {
		
		if(SCOPE == null) {
			SCOPE = new Scope("openid", "profile", "email");
			
			if( !Strings.isNullOrEmpty(additionalScope.getValue()) ) {
				String scopesString = additionalScope.getValue();
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
	public URI createRedirectURI(HttpServletRequest request, String targetURL) {
		
		try {
			OIDCProviderMetadata providerMetadata = getProviderMetadata();
			
			//---------------------------------------
			// Authentication Request
			// The client ID provisioned by the OpenID provider when
			// the client was registered
			ClientID clientID = new ClientID(this.clientID());
			
			URI endpointURI = providerMetadata.getAuthorizationEndpointURI();

			// The client callback URL
			String serverURL = CFW.HTTP.getServerURL(request);
			URI callbackURI = new URI(serverURL + FeatureCore.SERVLET_PATH_SSO_OPENID);
			CodeVerifier codeVerifier = new CodeVerifier();
			
			// Generate random state string to securely pair the callback to this request
			// add to session so it can be retrieved by class ServletSSOOpenIDCallback
			State state = new State();
			
			HttpSession session = request.getSession();
			
			session.setAttribute(PROPERTY_SSO_STATE, state.getValue());
			session.setAttribute(PROPERTY_SSO_CODE_VERIFIER, codeVerifier);
			session.setAttribute(SSOProviderSettings.PROPERTY_SSO_PROVIDER_ID, ""+this.getDefaultObject().id());
			session.setAttribute(PROPERTY_SSO_TARGET_URL, targetURL);
			
			// Generate nonce for the ID token
			Nonce nonce = new Nonce();

			//-------------------------------------------
			// Compose the OpenID authentication request 
			Builder authRequestBuilder = new AuthenticationRequest.Builder(
			    new ResponseType("code"),
			    this.getScope(),
			    clientID,
			    callbackURI)
			    .endpointURI(endpointURI)
			    .state(state)
			    .nonce(nonce)
			    .codeChallenge(codeVerifier, CodeChallengeMethod.S256)
			    .customParameter("resource", this.resource());
			
			LinkedHashMap<String, String> params = customParams.getValue();
			if(params != null && params.size() > 0) {
				for(Entry<String, String> entry : params.entrySet()) {
					authRequestBuilder.customParameter(entry.getKey(), entry.getValue());
				}
			}
			
			AuthenticationRequest authRequest = authRequestBuilder.build();
			
			//-------------------------------------------
			// The URI to send the user-user browser to the OpenID provider
			// E.g.
			// https://c2id.com/login?
			// client_id=123
			// &response_type=code
			// &scope=openid
			// &redirect_uri=https%3A%2F%2Fclient.com%2Fcallback
			// &state=6SK5S15Lwdp3Pem_55m-ayudGwno0eglKq6ZEWaykG8
			// &nonce=d_Y4LmbzpNHTkzTKJv6v59-OmqB_F2kNr8CbL-R2xWI
			//System.out.println(authRequest.toURI());
			
			return authRequest.toURI();
			
		}catch(Exception e) {
			new CFWLog(logger).severe("Exception occured while creating redirect to OpenID Connect Provider: "+e.getMessage(), e);
		} 
		
		return null;
		
	}
			
//	public String providerURL() {
//		return providerURL.getValue();
//	}
//	
//	public SSOOpenIDConnectProvider providerURL(String value) {
//		this.providerURL.setValue(value);
//		return this;
//	}
	
	public String wellknownURL() {
		return wellknownPath.getValue();
	}
	
	public SSOProviderSettingsOpenID wellknownURL(String value) {
		this.wellknownPath.setValue(value);
		return this;
	}
		
	public String clientID() {
		return clientID.getValue();
	}
	
	public SSOProviderSettingsOpenID clientID(String value) {
		this.clientID.setValue(value);
		return this;
	}
	
//	public String clientSecret() {
//		return clientSecret.getValue();
//	}
//	
//	public SSOOpenIDConnectProvider clientSecret(String value) {
//		this.clientSecret.setValue(value);
//		return this;
//	}
	
	public String grantType() {
		return grantType.getValue();
	}
	
	public SSOProviderSettingsOpenID grantType(String value) {
		this.grantType.setValue(value);
		return this;
	}
	
	public String resource() {
		return resource.getValue();
	}
	
	public SSOProviderSettingsOpenID resource(String value) {
		this.resource.setValue(value);
		return this;
	}
	
	public LinkedHashMap<String, String> customParams() {
		return customParams.getValue();
	}
	
	public SSOProviderSettingsOpenID customParams(LinkedHashMap<String, String> value) {
		this.customParams.setValue(value);
		return this;
	}
	
	/******************************************************************************
	 * 
	 ******************************************************************************/
	public OIDCProviderMetadata getProviderMetadata()
			throws URISyntaxException, MalformedURLException, IOException, ParseException {
		
		//---------------------------------------
		// Return if already discovered
		if(providerMetadata != null) {
			return providerMetadata;
		}
		
		//---------------------------------------
		// Discover Provider
		
		//providerURLString = (providerURLString.endsWith("/")) ? providerURLString : providerURLString+"/";
		
		URI wellknownURI = new URI(this.wellknownURL());
		URL providerConfigurationURL = wellknownURI.toURL();
		InputStream stream = providerConfigurationURL.openStream();
		
		// Read all data from URL
		String providerInfo = null;
		try (java.util.Scanner s = new java.util.Scanner(stream)) {
		  providerInfo = s.useDelimiter("\\A").hasNext() ? s.next() : "";
		}
		
		providerMetadata = OIDCProviderMetadata.parse(providerInfo);
		
		return providerMetadata;
	}

	@Override
	protected String getSettingsType() {
		return SETTINGS_TYPE;
	}
			
}
