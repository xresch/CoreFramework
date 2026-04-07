package com.xresch.cfw.utils.web;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.auth.AuthSchemeFactory;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.KerberosConfig;
import org.apache.hc.client5.http.auth.KerberosCredentials;
import org.apache.hc.client5.http.auth.NTCredentials;
import org.apache.hc.client5.http.auth.StandardAuthScheme;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.auth.BasicSchemeFactory;
import org.apache.hc.client5.http.impl.auth.CredentialsProviderBuilder;
import org.apache.hc.client5.http.impl.auth.DigestScheme;
import org.apache.hc.client5.http.impl.auth.DigestSchemeFactory;
import org.apache.hc.client5.http.impl.auth.KerberosScheme;
import org.apache.hc.client5.http.impl.auth.KerberosSchemeFactory;
import org.apache.hc.client5.http.impl.auth.NTLMScheme;
import org.apache.hc.client5.http.impl.auth.NTLMSchemeFactory;
import org.apache.hc.client5.http.impl.auth.SPNegoSchemeFactory;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import com.google.common.base.Joiner;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.utils.web.CFWHttp.CFWHttpAuthMethod;
import com.xresch.xrutils.utils.XRTimeUnit;

/******************************************************************************************************
 * Inner Class for HTTP Response
 ******************************************************************************************************/
public class CFWHttpRequestBuilder {
	
	private static Logger logger = CFWLog.getLogger(CFWHttpRequestBuilder.class.getName());
	
	private static final String SYNC_LOCK_CLIENT = "Sync Lock Client";
	
	private static final String HEADER_CONTENT_TYPE = "content-type";
	private CFWHttpAuthMethod authMethod = CFWHttpAuthMethod.BASIC;
	
	private Charset bodyCharset 		=  StandardCharsets.UTF_8;
	private static CloseableHttpClient httpClientSingle;
	private String username = null;
	private char[] pwdArray = null;
	String method = "GET";
	String URL = null;
	String body = null;
	String requestBodyContentType = "plain/text; charset=UTF-8";
	//private boolean autoCloseClient = true;
	long responseTimeoutMillis = XRTimeUnit.m.toMillis(10); //default timeout of  10 minutes

	private HashMap<String, String> params = new HashMap<>();
	private HashMap<String, String> lowercaseHeaders = new HashMap<>();
	
	public CFWHttpRequestBuilder(String urlNoParams) {
		this.URL = urlNoParams;
	}
	
	/***********************************************
	 * Set method to GET
	 ***********************************************/
	public CFWHttpRequestBuilder GET() {
		method = "GET";
		return this;
	}
	
	/***********************************************
	 * Set method to POST
	 ***********************************************/
	public CFWHttpRequestBuilder POST() {
		method = "POST";
		return this;
	}
	
	/***************************************************************************
	 * Set request method to PUT
	 ***************************************************************************/
	public CFWHttpRequestBuilder PUT() {
		method = "PUT";
		return this;
	}
	
	/***************************************************************************
	 * Set request method to DELETE
	 ***************************************************************************/
	public CFWHttpRequestBuilder DELETE() {
		method = "DELETE";
		return this;
	}
	
	/***************************************************************************
	 * Set request method to a customized method.
	 ***************************************************************************/
	public CFWHttpRequestBuilder METHOD(String method) {
		this.method = method;
		return this;
	}
	
	/***********************************************
	 * Add a parameter to the request.
	 ***********************************************/
	public CFWHttpRequestBuilder param(String name, String value) {
		params.put(name, value);
		return this;
	}
	
	
	/***********************************************
	 * Adds a map of parameters
	 ***********************************************/
	public CFWHttpRequestBuilder params(Map<String, String> paramsMap) {
		
		if(paramsMap == null) { return this; }
		
		this.params.putAll(paramsMap);
		return this;
		
	}
	
	/***************************************************************************
	 * Add a header
	 ***************************************************************************/
	public CFWHttpRequestBuilder header(String name, String value) {
		lowercaseHeaders.put(name.trim().toLowerCase(), value);
		return this;
	}
	
	
	/***************************************************************************
	 * Adds a map of headers
	 ***************************************************************************/
	public CFWHttpRequestBuilder headers(Map<String, String> headerMap) {
		
		if(headerMap == null) { return this; }
		
		for(Entry<String, String> entry : headerMap.entrySet()) {
			header(entry.getKey(), entry.getValue());
		}
		return this;
		
	}
	
	
	/***********************************************
	 * Get autoCloseClient
	 ***********************************************/
//	public boolean autoCloseClient() {
//		return this.autoCloseClient;
//	}
	
	/***********************************************
	 * Set Basic Authentication
	 ***********************************************/
	public CFWHttpRequestBuilder setAuthCredentialsBasic(String username, String password) {
		return setAuthCredentials(CFWHttpAuthMethod.BASIC, username, password);
	}
	
	/***********************************************
	 * Set authentication credentials
	 ***********************************************/
	public CFWHttpRequestBuilder setAuthCredentials(CFWHttpAuthMethod authMethod, String username, String password) {
	
		this.authMethod = (authMethod != null ) ? authMethod : CFWHttpAuthMethod.BASIC;
		this.username = username;
		this.pwdArray = (password != null ) ? password.toCharArray() : "".toCharArray();

		return this;
	}
	
	/***********************************************
	 * Add a request Body
	 ***********************************************/
	public CFWHttpRequestBuilder body(String content) {
		this.body = content;
		return this;
	}
	
	/***********************************************
	 * Add a request Body
	 ***********************************************/
	public CFWHttpRequestBuilder body(String contentType, String content) {
		this.requestBodyContentType = contentType;
		this.header(HEADER_CONTENT_TYPE, contentType);
		this.body = content;
		return this;
	}
	
	/***********************************************
	 * Add a request Body in JSON format UTF-8 encoding
	 ***********************************************/
	public CFWHttpRequestBuilder bodyJSON(String content) {
		return this.body("application/json; charset=UTF-8", content);
	}
	
	/***********************************************
	 * Add a response timeout.
	 ***********************************************/
	public CFWHttpRequestBuilder timeout(long responseTimeoutMillis) {
		this.responseTimeoutMillis = responseTimeoutMillis;
		return this;
	}
	
	/***********************************************
	 * Add a request Body in JSON format UTF-8 encoding
	 ***********************************************/
	public String buildURLwithParams() {
		return  CFWHttp.buildURL(URL, params);
	}
	/***************************************************************************
	 * Build and send the request. Returns a 
	 * PRFHttpResponse or null in case of errors.
	 ***************************************************************************/
	private static CloseableHttpClient getClient() throws Exception {
		
		synchronized (SYNC_LOCK_CLIENT) {
			
			if(httpClientSingle == null) {
			//if(httpClient.get() == null) {
				HttpClientBuilder clientBuilder = 
						HttpClientBuilder.create()
								.setConnectionManagerShared(true)
								//.setUserAgent(CFWHttp.defaultUserAgent())
								.setConnectionManager(CFWHttp.getConnectionManager())
								.setKeepAliveStrategy( (response, context) -> { return TimeValue.ofSeconds(60); }) // avoid ephemeral port exhaustion
								.setConnectionReuseStrategy(DefaultConnectionReuseStrategy.INSTANCE)
								//.evictExpiredConnections()
								//.evictIdleConnections(TimeValue.ofSeconds(30))
								;
			
				CFWHttp.httpClientAddProxy(clientBuilder);
				
				httpClientSingle = clientBuilder.build();
				// httpClient.set( clientBuilder.build());
			}
			
		}
        
		return httpClientSingle;
		//return httpClient.get();
	}
	/******************************************************************************************************
	 * Log details about get and post request
	 * @param requestBody TODO
	 ******************************************************************************************************/
	static void logFinerRequestInfo(String method, String url, HashMap<String, String> params, HashMap<String, String> headers, String requestBody) {
		if(logger.isLoggable(Level.FINER)) {
			
			String paramsString = (params == null) ? "null" : Joiner.on("&").withKeyValueSeparator("=").join(params);
			String headersString = (headers == null) ? "null" : Joiner.on(",").withKeyValueSeparator("=").join(headers);
			
			new CFWLog(logger)
				.custom("CFWHttp-method", method)
				.custom("CFWHttp-url", url)
				.custom("CFWHttp-params", paramsString)
				.custom("CFWHttp-headers", headersString)
				.custom("CFWHttp-body", requestBody)
				.finer("CFWHTTP Request Details");;

		}
	}
	/***********************************************
	 * Build and send the request. Returns a 
	 * CFWHttpResponse or null in case of errors.
	 ***********************************************/
	@SuppressWarnings("deprecation")
	public CFWHttpResponse send() {
		
		try {
			
			//---------------------------------
			// Create URL
			String urlWithParams = buildURLwithParams();

			
			//---------------------------------
			// Create Request Base
			HttpUriRequestBase requestBase = new HttpUriRequestBase(method, URI.create(urlWithParams));
			requestBase.setConfig(
						 RequestConfig
								.custom()
								.setResponseTimeout(Timeout.ofMilliseconds(responseTimeoutMillis) )
								//.setRedirectsEnabled( ! disableFollowRedirects )
								.build()
					);
			if(requestBase != null) {
				
									
			//-----------------------------------
			// Handle POST Body			
			if (body != null) {

			    ContentType type;

			    if (!lowercaseHeaders.containsKey(HEADER_CONTENT_TYPE)) {
			        type = ContentType.create("text/plain", bodyCharset);
			    } else {
			        type = ContentType.parse(lowercaseHeaders.get(HEADER_CONTENT_TYPE));

			        if (type.getCharset() == null) {
			            type = type.withCharset(bodyCharset);
			        }
			    }

			    requestBase.setEntity( new StringEntity(body, type) );
			}
				
			//----------------------------------
	        // Create HTTP Context (per request!)
	        HttpClientContext context = HttpClientContext.create();
	       
	        //----------------------------------
	        // Set Cookie Store (per request)
	        context.setCookieStore(new BasicCookieStore());
	        
	        //----------------------------------
			// Set Auth mechanism
			if(username != null) {
				
				//---------------------------------
				// Credential Provider
				String scheme = requestBase.getUri().getScheme();
				String hostname = requestBase.getUri().getHost();
				int port = requestBase.getUri().getPort();
				HttpHost targetHost = new HttpHost(scheme, hostname, port);

				CredentialsProviderBuilder credProviderBuilder = CredentialsProviderBuilder.create();
				
				RegistryBuilder<AuthSchemeFactory> registryBuilder = RegistryBuilder.<AuthSchemeFactory>create();
				
				switch(this.authMethod) {
				
					//------------------------------
					// Basic 
					case BASIC:
						//PRFHttp.addBasicAuthorizationHeader(headers, username, new String(pwdArray));
						AuthScope authScopeBasic = new AuthScope(targetHost, null, new BasicScheme().getName());
						credProviderBuilder.add(authScopeBasic, username, pwdArray);
						registryBuilder.register(StandardAuthScheme.BASIC, BasicSchemeFactory.INSTANCE);
					break;
					
					//------------------------------
					// Basic 
					case BASIC_HEADER:
						CFWHttp.addBasicAuthorizationHeader(lowercaseHeaders, username, new String(pwdArray));
					break;
					
					
					//------------------------------
					// Digest
					case DIGEST:
						AuthScope authScopeDigest = new AuthScope(targetHost, null, new DigestScheme().getName());
						credProviderBuilder.add(authScopeDigest, username, pwdArray);
						registryBuilder.register(StandardAuthScheme.DIGEST, DigestSchemeFactory.INSTANCE);
					break;
					
					//------------------------------
					// NTLM
					case NTLM:
						String ntlmUsername = username;
						String ntlmDomain = null;
						if(username.contains("@")) {
							String[] splitted = username.split("@");
							ntlmUsername = splitted[0];
							ntlmDomain = splitted[1];
						}
						AuthScope authScopeNTLM = new AuthScope(targetHost, null, new NTLMScheme().getName());
						
						NTCredentials ntlmCreds = new NTCredentials(pwdArray, ntlmUsername, ntlmDomain, null);
						credProviderBuilder.add(authScopeNTLM, ntlmCreds);
						registryBuilder.register(StandardAuthScheme.NTLM, NTLMSchemeFactory.INSTANCE);
					break;
						
					//------------------------------
					// KERBEROS (experimental)
					case KERBEROS:
						GSSManager manager = GSSManager.getInstance();
						GSSName name = manager.createName(username, GSSName.NT_USER_NAME);
					    GSSCredential gssCred = manager.createCredential(name,GSSCredential.DEFAULT_LIFETIME, (Oid) null, GSSCredential.INITIATE_AND_ACCEPT);
					    
						AuthScope authScopeKerberos = new AuthScope(targetHost, null, new KerberosScheme().getName());
					
						KerberosCredentials kerbCred = new KerberosCredentials(gssCred);
						credProviderBuilder.add(authScopeKerberos, kerbCred);
						registryBuilder.register(StandardAuthScheme.SPNEGO, new SPNegoSchemeFactory(
											                KerberosConfig.custom()
									                        .setStripPort(KerberosConfig.Option.DEFAULT)
									                        .setUseCanonicalHostname(KerberosConfig.Option.DEFAULT)
									                        .build(),
									                SystemDefaultDnsResolver.INSTANCE))
									        .register(StandardAuthScheme.KERBEROS, KerberosSchemeFactory.DEFAULT);
					break;
					
					default:
					break;
				
				}
				
				//---------------------------------
				// Credential Provider
				if (this.authMethod != CFWHttpAuthMethod.BASIC_HEADER) {
					context.setCredentialsProvider(credProviderBuilder.build());
	                context.setAuthSchemeRegistry(registryBuilder.build());
				}
				
			}
				
				//-----------------------------------
				// Handle headers
				if(lowercaseHeaders != null ) {
					for(Entry<String, String> header : lowercaseHeaders.entrySet()) {
						requestBase.addHeader(header.getKey(), header.getValue());
					}
				}

				//-----------------------------------
				// Connect and create response
				logFinerRequestInfo(method, URL, params, lowercaseHeaders, body);	
				CFWHttp.outgoingHTTPCallsCounter.labels(method).inc();

				CFWHttpResponse response = new CFWHttpResponse(this, getClient(), requestBase, context);
				return response;
				
			}
		} catch (Throwable e) {
			new CFWLog(logger).severe("Exception occured: "+e.getMessage(), e);
		} 

		return null;		
	}
}