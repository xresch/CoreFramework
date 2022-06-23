package com.xresch.cfw.features.core.auth;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022 
 * @license MIT-License
 **************************************************************************************************************/
public class SSOOpenIDConnectProvider extends AbstractContextSettings {
	
	public static final String PROPERTY_SSO_STATE = "ssoState";
	public static final String PROPERTY_SSO_PROVIDER_ID = "ssoProviderID";
	public static final String PROPERTY_SSO_TARGET_URL = "ssopTargetURL";

	private static Logger logger = CFWLog.getLogger(SSOOpenIDConnectProvider.class.getName());
	
	public static final String SETTINGS_TYPE = "OpenID Connect Provider";
	
	public enum PrometheusEnvironmentFields{
		PROVIDER_URL,
		WELL_KNOWN_PATH,
		CLIENT_ID,
		CLIENT_SECRET,
		JSON_CUSTOM_PARAMETERS
	}
			
	private CFWField<String> providerURL = CFWField.newString(FormFieldType.TEXT, PrometheusEnvironmentFields.PROVIDER_URL)
			.setDescription("The url of the OpenID Connect provider.");
	
	private CFWField<String> wellknownPath = CFWField.newString(FormFieldType.TEXT, PrometheusEnvironmentFields.WELL_KNOWN_PATH)
			.setDescription("The path to the .well-known provider configuration.");
	
	private CFWField<String> clientID = CFWField.newString(FormFieldType.TEXT, PrometheusEnvironmentFields.CLIENT_ID)
			.setDescription("The id used for this client.");
	
	private CFWField<String> clientSecret = CFWField.newString(FormFieldType.TEXT, PrometheusEnvironmentFields.CLIENT_SECRET)
			.setDescription("The secret used for this client.");
	
	private CFWField<LinkedHashMap<String, String>> customParams = CFWField.newValueLabel(PrometheusEnvironmentFields.JSON_CUSTOM_PARAMETERS)
			.setDescription("Custom parameters that should be added to the authentication request.");
	
	private OIDCProviderMetadata providerMetadata = null;
	
	public SSOOpenIDConnectProvider() {
		initializeFields();
	}
		
	private void initializeFields() {
		this.addFields(providerURL, wellknownPath, clientID, clientSecret, customParams);
	}
		
		
	@Override
	public boolean isDeletable(int settingsID) {
		return true;
	}
	
	public boolean isDefined() {
		if(providerURL.getValue() != null
		&& clientID.getValue() != null) {
			return true;
		}
		
		return false;
	}
			
	public String providerURL() {
		return providerURL.getValue();
	}
	
	public SSOOpenIDConnectProvider providerURL(String value) {
		this.providerURL.setValue(value);
		return this;
	}
	
	public String wellknownURL() {
		return wellknownPath.getValue();
	}
	
	public SSOOpenIDConnectProvider wellknownURL(String value) {
		this.wellknownPath.setValue(value);
		return this;
	}
		
	public String clientID() {
		return clientID.getValue();
	}
	
	public SSOOpenIDConnectProvider clientID(String value) {
		this.clientID.setValue(value);
		return this;
	}
	
	public String clientSecret() {
		return clientSecret.getValue();
	}
	
	public SSOOpenIDConnectProvider clientSecret(String value) {
		this.clientSecret.setValue(value);
		return this;
	}
	
	public LinkedHashMap<String, String> customParams() {
		return customParams.getValue();
	}
	
	public SSOOpenIDConnectProvider customParams(LinkedHashMap<String, String> value) {
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
		String providerURLString = this.providerURL();
		providerURLString = (providerURLString.endsWith("/")) ? providerURLString : providerURLString+"/";
		
		URI issuerURI = new URI(providerURLString);
		URL providerConfigurationURL = issuerURI.resolve(this.wellknownURL()).toURL();
		InputStream stream = providerConfigurationURL.openStream();
		
		// Read all data from URL
		String providerInfo = null;
		try (java.util.Scanner s = new java.util.Scanner(stream)) {
		  providerInfo = s.useDelimiter("\\A").hasNext() ? s.next() : "";
		}
		
		providerMetadata = OIDCProviderMetadata.parse(providerInfo);
		
		return providerMetadata;
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
			URI callback = new URI(serverURL + FeatureCore.SERVLET_PATH_SSO_OPENID);

			// Generate random state string to securely pair the callback to this request
			// add to session so it can be retrieved by class ServletSSOOpenIDCallback
			State state = new State();
			CFW.Context.Session.getSessionData().setCustom(PROPERTY_SSO_STATE, state.getValue());
			CFW.Context.Session.getSessionData().setCustom(PROPERTY_SSO_PROVIDER_ID, ""+this.getDefaultObject().id());
			CFW.Context.Session.getSessionData().setCustom(PROPERTY_SSO_TARGET_URL, targetURL);
			
			// Generate nonce for the ID token
			Nonce nonce = new Nonce();

			// Compose the OpenID authentication request (for the code flow)
			AuthenticationRequest authRequest = new AuthenticationRequest.Builder(
			    new ResponseType("code"),
			    new Scope("openid", "profile", "email"),
			    clientID,
			    callback)
			    .endpointURI(endpointURI)
			    .state(state)
			    .nonce(nonce)
			    .build();

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
	
		
}
