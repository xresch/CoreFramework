package com.xresch.cfw.features.core.auth;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.SerializeException;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.Tokens;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import com.nimbusds.openid.connect.sdk.AuthenticationErrorResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponseParser;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.UserInfoSuccessResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

import net.minidev.json.JSONObject;

/**************************************************************************************************************
 * Servlet which will handle the response from the identity provider(IdP).
 * 
 * @author Reto Scheiwiller, (c) Copyright 2020
 * @license MIT-License
 **************************************************************************************************************/
public class ServletSSOOpenIDCallback extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = CFWLog.getLogger(ServletSSOOpenIDCallback.class.getName());
		
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {		
		// When the call back URI is invoked the response parameters
		// will be encoded in the query string, parse them
		// https://thisapp:8888/callback?state=6SK5S15Lwdp3Pem_55m-ayudGwno0eglKq6ZEWaykG8&code=eemeuWi9reingee0
		String requestURL = request.getRequestURL()+"?"+request.getQueryString();
		
		AuthenticationResponse authResponse;
		try {
			authResponse = AuthenticationResponseParser.parse(
			    new URI(requestURL));
	
			//------------------------------------
			// Handle Errors 
			if (authResponse instanceof AuthenticationErrorResponse) {
				new HTMLResponse("Single Sign-On Error");
				ErrorObject errorObject = authResponse.toErrorResponse().getErrorObject();
				CFW.Messages.addErrorMessage("Error Code: "+errorObject.getCode());
			    CFW.Messages.addErrorMessage("Error Description: "+errorObject.getDescription());
			    return;
			}
			
			//------------------------------------
			// Verify State is correct
			String ssoStateString = CFW.Context.Session.getSessionData().getCustom(
					SSOOpenIDConnectProvider.PROPERTY_SSO_STATE
				);
			System.out.println(ssoStateString);
			if (authResponse == null || !authResponse.getState().equals(new State(ssoStateString))) {
				new HTMLResponse("Unexpected authentication Response");
			    CFW.Messages.addErrorMessage("Unexpected response from authentication provider. Please try to sign-on again.");
			    return;
			}
			
			//------------------------------------
			// Retrieve Authentication Code
			String providerIDString = CFW.Context.Session.getSessionData().getCustom(
					SSOOpenIDConnectProvider.PROPERTY_SSO_PROVIDER_ID
				);
			SSOOpenIDConnectProvider provider = SSOOpenIDConnectProviderManagement.getEnvironment(Integer.parseInt(providerIDString));
			OIDCProviderMetadata providerMetadata = provider.getProviderMetadata();
			
			AuthenticationSuccessResponse successResponse = (AuthenticationSuccessResponse) authResponse;
			AuthorizationCode code = successResponse.getAuthorizationCode();
			
			URI redirectURI = successResponse.getRedirectionURI();
			
			//------------------------------------
			// Retrieve Tokens, UserInfo and do Login
			Tokens tokens = fetchTokens(provider, code, redirectURI);
			// TODO Verification to be done!
			UserInfo info = fetchUserInfo(tokens, providerMetadata);
			
			//------------------------------------
			// Do Login and Redirect
			String username = info.getPreferredUsername();
			String email = info.getEmailAddress();
			String firstname = info.getGivenName();
			String lastname = info.getFamilyName();
			User user = LoginUtils.fetchUserCreateIfNotExists(username, email, firstname, lastname, true);
			
			System.out.println("redirectURI: "+redirectURI);
			LoginUtils.loginUserAndCreateSession(request, response, user, CFW.Context.App.getApp().getDefaultURL());
			
			System.out.println("======= Retrieved user info ======");
			System.out.println(info.toJSONString());
			
		} catch (Exception e) {
			new HTMLResponse("Exception Occured");
			new CFWLog(logger).severe("Error occured during authentication process: "+e.getMessage(), e);
			e.printStackTrace();
		}
    }
		
	
	/*******************************************************************
	 * @return 
	 * @throws IOException 
	 * @throws URISyntaxException 
	 * @throws ParseException 
	 * @throws MalformedURLException 
	 ******************************************************************/
	protected Tokens fetchTokens(SSOOpenIDConnectProvider provider, AuthorizationCode code, URI redirectURI ) 
			throws MalformedURLException, ParseException, URISyntaxException, IOException {
	   
		//-------------------------------
		// Prepare Token Request
		OIDCProviderMetadata providerMetadata = provider.getProviderMetadata();
		ClientID clientID = new ClientID(provider.clientID());
		Secret clientSecret = new Secret(provider.clientSecret());
		TokenRequest tokenReq = 
				new TokenRequest(
						providerMetadata.getTokenEndpointURI(),
						new ClientSecretBasic(clientID, clientSecret),
						new AuthorizationCodeGrant( code, redirectURI)
		);
		
		//-------------------------------
		// Send Request
		HTTPResponse tokenHTTPResp = null;
		tokenHTTPResp = tokenReq.toHTTPRequest().send();

		//-------------------------------	
		// Parse and Check Response
		TokenResponse tokenResponse = null;

		tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);

		if (tokenResponse instanceof TokenErrorResponse) {
			ErrorObject error = ((TokenErrorResponse) tokenResponse).getErrorObject();
			// TODO error handling
		}
			
		AccessTokenResponse accessTokenResponse = tokenResponse.toSuccessResponse();
		accessTokenResponse.getTokens().getAccessToken();
		
		return accessTokenResponse.getTokens();
//		OIDCAccessTokenResponse accessTokenResponse = (OIDCAccessTokenResponse) tokenResponse;
//		accessTokenResponse.getAccessToken();
//		accessTokenResponse.getIDToken();
	}
	
	/*******************************************************************
	 * 
	 ******************************************************************/
	private UserInfo fetchUserInfo(Tokens tokens, OIDCProviderMetadata providerMetadata) {
		
		BearerAccessToken accessToken = tokens.getBearerAccessToken();
		
		UserInfoRequest userInfoReq = 
				new UserInfoRequest(
						providerMetadata.getUserInfoEndpointURI(),
						accessToken
				);

		HTTPResponse userInfoHTTPResp = null;
		try {
			userInfoHTTPResp = userInfoReq.toHTTPRequest().send();
		} catch (SerializeException | IOException e) {
			// TODO proper error handling
		}

		UserInfoResponse userInfoResponse = null;
		try {
			userInfoResponse = UserInfoResponse.parse(userInfoHTTPResp);
		} catch (ParseException e) {
			// TODO proper error handling
		}

		if (userInfoResponse instanceof UserInfoErrorResponse) {
			ErrorObject error = ((UserInfoErrorResponse) userInfoResponse).getErrorObject();
			// TODO error handling
		}

		UserInfoSuccessResponse successResponse = (UserInfoSuccessResponse) userInfoResponse;
		
		//JSONObject claims = successResponse.getUserInfo().toJSONObject();
		
		return successResponse.getUserInfo();
	}
	/*******************************************************************
	 * 
	 ******************************************************************/
//	private ReadOnlyJWTClaimsSet verifyIdToken(JWT idToken, OIDCProviderMetadata providerMetadata) {
//		RSAPublicKey providerKey = null;
//		try {
//			JSONObject key = getProviderRSAJWK(providerMetadata.getJWKSetURI().toURL().openStream());
//			providerKey = RSAKey.parse(key).toRSAPublicKey();
//		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | java.text.ParseException e) {
//			// TODO error handling
//		}
//
//		DefaultJWTDecoder jwtDecoder = new DefaultJWTDecoder();
//		jwtDecoder.addJWSVerifier(new RSASSAVerifier(providerKey));
//		ReadOnlyJWTClaimsSet claims = null;
//		try {
//			claims = jwtDecoder.decodeJWT(idToken);
//		} catch (JOSEException | java.text.ParseException e) {
//			// TODO error handling
//		}
//
//		return claims;
//	}

	
	/*******************************************************************
	 * 
	 ******************************************************************/
	private JSONObject getProviderRSAJWK(InputStream is) throws ParseException {
		// Read all data from stream
		StringBuilder sb = new StringBuilder();
		try (Scanner scanner = new Scanner(is);) {
			while (scanner.hasNext()) {
				sb.append(scanner.next());
			}
		}

		// Parse the data as json
		String jsonString = sb.toString();
		JSONObject json = JSONObjectUtils.parse(jsonString);

		// Find the RSA signing key
		JSONArray keyList = (JSONArray) json.get("keys");
		for (Object key : keyList) {
			JSONObject k = (JSONObject) key;
			if (k.get("use").equals("sig") && k.get("kty").equals("RSA")) {
				return k;
			}
		}
		return null;
	}
}