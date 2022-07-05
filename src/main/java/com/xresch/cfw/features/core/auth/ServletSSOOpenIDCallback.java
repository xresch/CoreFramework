package com.xresch.cfw.features.core.auth;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.xresch.cfw.features.usermgmt.CFWSessionData;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;

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
		CFWSessionData sessionData = CFW.Context.Session.getSessionData();
		
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
			String ssoStateString = sessionData.removeCustom(SSOOpenIDConnectProvider.PROPERTY_SSO_STATE);

			if (authResponse == null || !authResponse.getState().equals(new State(ssoStateString))) {
				new HTMLResponse("Unexpected authentication Response");
			    CFW.Messages.addErrorMessage("Unexpected response from authentication provider. Please try to sign-on again.");
			    return;
			}
			
			//------------------------------------
			// Retrieve Authentication Code
			String providerIDString = sessionData.removeCustom(SSOOpenIDConnectProvider.PROPERTY_SSO_PROVIDER_ID);
			
			SSOOpenIDConnectProvider provider = SSOOpenIDConnectProviderManagement.getEnvironment(Integer.parseInt(providerIDString));
			OIDCProviderMetadata providerMetadata = provider.getProviderMetadata();
			
			AuthenticationSuccessResponse successResponse = (AuthenticationSuccessResponse) authResponse;
			AuthorizationCode code = successResponse.getAuthorizationCode();
			
			URI redirectURI = successResponse.getRedirectionURI();
			
			//------------------------------------
			// Retrieve Tokens, UserInfo and do Login
			Tokens tokens = fetchTokens(provider, code, redirectURI);
			
			if(tokens == null) {
				//error
				return;
			}
			// TODO Verification to be done!
			UserInfo info = fetchUserInfo(tokens, providerMetadata);
			
			//------------------------------------
			// Do Login and Redirect
			String username = info.getPreferredUsername();
			String email = info.getEmailAddress();
			String firstname = info.getGivenName();
			String lastname = info.getFamilyName();
			User user = LoginUtils.fetchUserCreateIfNotExists(username, email, firstname, lastname, true);
			
			String targetURL = sessionData.removeCustom(SSOOpenIDConnectProvider.PROPERTY_SSO_TARGET_URL);

			LoginUtils.loginUserAndCreateSession(request, response, user, targetURL);
			
			//System.out.println("======= Retrieved user info ======");
			//System.out.println(info.toJSONString());
			
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
		
		String secretString = provider.clientSecret();
		if(secretString == null) {
			secretString = "";
		}
		Secret clientSecret = new Secret(secretString);
		
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
			new CFWLog(logger).severe("Error response received during single sign on: "+error.getDescription()+"(code="+error.getCode()+")");
			return null;
		}
			
		AccessTokenResponse accessTokenResponse = tokenResponse.toSuccessResponse();
		accessTokenResponse.getTokens().getAccessToken();
		
		return accessTokenResponse.getTokens();

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
//
//		ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
//		
//		//JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL("https://demo.c2id.com/jwks.json"));
//		JWKSet parsedSet = JWKSet.parse("MIICpzCCAY8CBgGBbLxgizANBgkqhkiG9w0BAQsFADAXMRUwEwYDVQQDDAxjZndsb2NhbGhvc3QwHhcNMjIwNjE2MTMzNjMzWhcNMzIwNjE2MTMzODEzWjAXMRUwEwYDVQQDDAxjZndsb2NhbGhvc3QwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCA8R9ll4yU+QPgV3tkzX/S0fPx8b0V10YB8tU/SjwnjamuOtwsXB5QuMITDw6TQMDNX8MEr56PKrPe2I8VMuOP3B/uhSstOZrVFS0G4quEZpsgVUDOzkVMbcaV8wYgxHlqDKRGUk2C8BhAgjaqwtie3tfhZDAOjTqTCfo1Xc7g9+zIdTBP4L5qlmWycfbdC7syYGJ8YsPco/NrCkqHnwF2L4gYB0/GGFRcareaaWWvyAlBI1Jm7ZahYkLuF1s3yD/7lylrxX3vC5rp1zcEpfcSesH+vyQFbtLrA7isABIxpGaKL0H4c45+V47OFl6fCv1RdsfFXqkSU2CGDYao7Mz7AgMBAAEwDQYJKoZIhvcNAQELBQADggEBAAPENqpCBQNkrFeotbSUIKkThAnv8RbGhbfwopD0+afYR+BjUP/nLi/Qjaeopigtm1ogSvWjhAHWOsNgti5a914c8XYK2mPwitm3fjAoet4wQeFxinWruzYe3Cyaa0lo3d56c7kUxU0l3bxbZUOUtyf4iOfNnX0VjSS6VmoftcZ7jprpMcDVbKIRLAK+HyJrz1CcvOuUUGAT/s7v7F9aCztGnk19DGfGW9ab4NFRCLEQ0229t01PpLQ/V9lG3dwPn1eWgsrLNpfxJtir+QK61tcFPd9hk15ZpVVvolthi1WpFEZ6M+Q8EYt9FGRHQ6cjvDIJJQ+00dY5Gd/0oYYb1Oo=");
//		
//		JWKSource<SecurityContext> keySource = new ImmutableJWKSet<SecurityContext>(parsedSet);
//
//		// The expected JWS algorithm of the access tokens (agreed out-of-band)
//		JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
//		
//		JWSKeySelector<SecurityContext> keySelector =
//			    new JWSVerificationKeySelector<>(expectedJWSAlg, keySource);
//		
//		jwtProcessor.setJWSKeySelector(keySelector);
//
//		// Set the required JWT claims for access tokens issued by the Connect2id
//		// server, may differ with other servers
//		jwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier(
//		    new JWTClaimsSet.Builder().issuer("https://demo.c2id.com").build(),
//		    new HashSet<>(Arrays.asList("sub", "iat", "exp", "scp", "cid", "jti"))));
//
//		// Process the token
//		SecurityContext ctx = null; // optional context parameter, not required here
//		JWTClaimsSet claimsSet = jwtProcessor.process(accessToken, ctx);
//
//		// Print out the token claims set
//		System.out.println(claimsSet.toJSONObject());
//	}
	
}