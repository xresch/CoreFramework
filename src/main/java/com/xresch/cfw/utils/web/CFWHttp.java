package com.xresch.cfw.utils.web;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.auth.AuthSchemeFactory;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.KerberosConfig;
import org.apache.hc.client5.http.auth.KerberosCredentials;
import org.apache.hc.client5.http.auth.NTCredentials;
import org.apache.hc.client5.http.auth.StandardAuthScheme;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
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
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.core5.util.Timeout;
import org.graalvm.polyglot.Value;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWProperties;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.utils.CFWState.CFWStateOption;
import com.xresch.cfw.utils.CFWTime.CFWTimeUnit;
import com.xresch.cfw.utils.scriptengine.CFWScriptingContext;

import io.prometheus.client.Counter;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWHttp {
	
	private static Logger logger = CFWLog.getLogger(CFWHttp.class.getName());
	
	//Use Threadlocal to avoid polyglot multi thread exceptions
	private static ThreadLocal<CFWScriptingContext> javascriptEngine = new ThreadLocal<CFWScriptingContext>();
	
	private static KeyStore cachedKeyStore = null;
	
	private static String proxyPAC = null;
	private static Cache<String, ArrayList<CFWProxy>> resolvedProxiesCache = CFW.Caching.addCache("CFW Proxies", 
			CacheBuilder.newBuilder()
				.initialCapacity(100)
				.maximumSize(1000)
				.expireAfterAccess(1, TimeUnit.HOURS)
		);
	private static CFWHttp instance = new CFWHttp();
	
	public enum CFWHttpAuthMethod{
		/* Digest authentication with the Apache HttpClient */
		  BASIC
		  /* Digest authentication using Basic Header */
		, BASIC_HEADER
		  /* Digest authentication with the Apache HttpClient */
		, DIGEST
		  /* NTLM: untested, deprecated, experimental */
		, NTLM
		  /* Kerberos: untested, deprecated, experimental */
		, KERBEROS 
	}

	private static final Counter outgoingHTTPCallsCounter = Counter.build()
	         .name("cfw_http_outgoing_calls_total")
	         .help("Number of outgoing HTTP calls executed with the CFWHTTP utils.")
	         .labelNames("method")
	         .register();
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public static String encode(String toEncode) {
		
		if(toEncode == null) {
			return null;
		}
		try {
			return URLEncoder.encode(toEncode, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			new CFWLog(logger)
				.severe("Exception while encoding: "+e.getMessage(), e);	
		}
		
		return toEncode;
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	public static String decode(String toDecode) {
		
		if(toDecode == null) {
			return null;
		}
		try {
			return URLDecoder.decode(toDecode, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			new CFWLog(logger)
				.severe("Exception while decoding: "+e.getMessage(), e);	
		}
		
		return toDecode;
	}
	
	
	/******************************************************************************************************
	 * Returns an encoded parameter string with leading '&'.
	 ******************************************************************************************************/
	public static String  encode(String paramName, String paramValue) {
		
		return "&" + encode(paramName) + "=" + encode(paramValue);
	}
	
	
	/******************************************************************************************************
	 * Creates a query string from the given parameters.
	 ******************************************************************************************************/
	public static String  buildQueryString(HashMap<String,String> params) {
		
		StringBuilder builder = new StringBuilder();
		
		for(Entry<String,String> param : params.entrySet()) {
			builder.append(encode(param.getKey(), param.getValue()));
		}
		
		//remove leading "&"
		return builder.substring(1);
	}
	

	/**************************************************************
	 * Check if the given URL can connect to a port and if a
	 * response is received. 
	 * 
	 * @param url including port, if port is missing, 80 is used for
	 * http and 443 for https
	 * @param checkErrorResponse set to true if the response should
	 * be checked to be below HTTP Status Code 400
	 * @return status RED or GREEN
	 **************************************************************/
	public static CFWStateOption checkURLGetsResponse(String urlToCheck, boolean checkErrorResponse) {
		
		//------------------------------
		// Check can resolve
		if(checkURLCanResolve(urlToCheck) == CFWStateOption.RED) {
			return CFWStateOption.RED;
		}
		
		//------------------------------
		// Get Response
		CFWHttpResponse response = CFW.HTTP.sendGETRequest(urlToCheck);

		return response.getState(checkErrorResponse);
		
	}
	/**************************************************************
	 * Check if the given URL can connect to a port or if it is 
	 * unresolved.
	 * @param url including port, if port is missing, 80 is used for
	 * http and 443 for https
	 * @return status RED or GREEN
	 **************************************************************/
	public static CFWStateOption checkURLCanResolve(String urlToCheck) {
		
		//-----------------------------
		// Check Is Valid URL
		URL url;
		try {
			url = new URI(urlToCheck).toURL();
		} catch (Exception e) {
			return CFWStateOption.RED;
		}
		
		//-----------------------------
		// Get Port
		int port = url.getPort();
		
		if(port == -1) {
			if(url.getProtocol().toLowerCase().equals("https")) {
				port = 443;
			}else {
				port = 80;
			}
		}
		
		//-----------------------------
		// Check Can Connect
		InetSocketAddress address = new InetSocketAddress(url.getHost(), port );
				
		if( ! address.isUnresolved() ) {
			return CFWStateOption.GREEN;
		}else{
			return CFWStateOption.RED;
		}
	}
		
	
	/******************************************************************************************************
	 * Creates a url with the given parameters;
	 ******************************************************************************************************/
	public static String  buildURL(String urlWithPath, HashMap<String,String> params) {
		
		if(params != null && !params.isEmpty()) {
			
			if(urlWithPath.endsWith("?") || urlWithPath.endsWith("/")) {
				urlWithPath = urlWithPath.substring(0, urlWithPath.length()-1);
			}
			StringBuilder builder = new StringBuilder(urlWithPath);
			
			builder.append("?");
			
			for(Entry<String,String> param : params.entrySet()) {
				builder.append(encode(param.getKey(), param.getValue()));
			}
			
			return builder.toString();
		}
		
		return urlWithPath;
		
	}
	
	/******************************************************************************************************
	 * Redirects to the referer of the request.
	 * @throws IOException 
	 ******************************************************************************************************/
	public static void redirectToReferer( HttpServletRequest request, HttpServletResponse response ) throws IOException {
		response.sendRedirect(response.encodeRedirectURL(request.getHeader("referer")));
	}
	
	/******************************************************************************************************
	 * Redirects to the specified url.
	 * @throws IOException 
	 ******************************************************************************************************/
	public static void redirectToURL(HttpServletResponse response, String url ) {
		try {
			response.sendRedirect(response.encodeRedirectURL(url));
		} catch (IOException e) {
			new CFWLog(logger)
				.severe("Error sending redirect.", e);
		}
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	private static CFWScriptingContext getScriptContext() {
		
		if(javascriptEngine.get() == null) {
			javascriptEngine.set( CFW.Scripting.createJavascriptContext().putMemberWithFunctions( new CFWHttpPacScriptMethods()) );
			
			//------------------------------
			// Add to engine if pac loaded
			loadPacFile();
			if(proxyPAC != null) {
				//Prepend method calls with CFWHttpPacScriptMethods
				proxyPAC = CFWHttpPacScriptMethods.preparePacScript(proxyPAC);

				CFWScriptingContext polyglot = getScriptContext();

			    polyglot.addScript("proxy.pac", proxyPAC);
			    polyglot.executeScript("FindProxyForURL('localhost:9090/test', 'localhost');");

			}
		}
		
		return javascriptEngine.get();
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	private static void loadPacFile() {
		
		if(CFW.Properties.PROXY_ENABLED && proxyPAC == null) {
		
			if(CFW.Properties.PROXY_PAC.toLowerCase().startsWith("http")) {
				//------------------------------
				// Get PAC from URL
				CFWHttpResponse response = null;
				
				try {	
					
					response = CFWHttp.newRequestBuilder(CFW.Properties.PROXY_PAC)
							.GET()
							.send()
							;
//					HttpURLConnection connection = (HttpURLConnection)new URL(CFW.Properties.PROXY_PAC).openConnection();
//					if(connection != null) {
//						connection.setRequestMethod("GET");
//						connection.connect();
//						
//						response = instance.new CFWHttpResponse(connection);
//					}
			    
				} catch (Exception e) {
					new CFWLog(logger)
						.severe("Exception occured.", e);
				} 
				
				//------------------------------
				// Cache PAC Contents
				if(response != null && response.getStatus() <= 299) {
					proxyPAC = response.getResponseBody();
					
					if(proxyPAC == null || !proxyPAC.contains("FindProxyForURL")) {
						CFW.Messages.addErrorMessage("The Proxy .pac-File seems not be in the expected format.");
						proxyPAC = null;
					}
				}else {
					CFW.Messages.addErrorMessage("Error occured while retrieving .pac-File from URL. (HTTP Code: "+response.getStatus()+")");
				}
			}else {
				//------------------------------
				// Load from Disk
				proxyPAC = CFW.Files.getFileContent(CFW.Context.Request.getRequest(), CFW.Properties.PROXY_PAC);
				
				if(proxyPAC == null || !proxyPAC.contains("FindProxyForURL")) {
					CFW.Messages.addErrorMessage("The Proxy .pac-File seems not be in the expected format.");
					proxyPAC = null;
				}
			}
		}
	}

	/******************************************************************************************************
	 * Returns a String Array with URLs used as proxies. If the string is "DIRECT", it means the URL should
	 * be called without a proxy. This method will only return "DIRECT" when "cfw_proxy_enabled" is set to 
	 * false.
	 * 
	 * @param urlToCall used for the request.
	 * @return ArrayList<String> with proxy URLs
	 * @throws  
	 ******************************************************************************************************/
	public static ArrayList<CFWProxy> getProxies(String urlToCall) {
		
		//------------------------------
		// Get Proxy PAC
		//------------------------------
		loadPacFile();
		
		//------------------------------
		// Get Proxy List
		//------------------------------
		
		ArrayList<CFWProxy> proxyArray = null;
		
		if(CFW.Properties.PROXY_ENABLED && proxyPAC != null) {
			 URL tempURL;
			try {
				tempURL = new URL(urlToCall);
				String hostname = tempURL.getHost();
				
				proxyArray = resolvedProxiesCache.get(hostname, new Callable<ArrayList<CFWProxy>>() {
					@Override
					public ArrayList<CFWProxy> call() throws Exception {
						
						ArrayList<CFWProxy> proxies = new ArrayList<CFWProxy>();
						
						Value result = getScriptContext().executeScript("FindProxyForURL('"+urlToCall+"', '"+hostname+"');");
						if(result != null) {
							String[] proxyArray = result.asString().split(";");
							
							for(String proxyDef : proxyArray) {
								if(proxyDef.trim().isEmpty()) { continue; }
								
								String[] splitted = proxyDef.trim().split(" ");
								CFWProxy cfwProxy = instance.new CFWProxy();
								
								String port;
								
								cfwProxy.type = splitted[0];
								if(splitted.length > 1) {
									String hostport = splitted[1];
									if(hostport.indexOf(":") != -1) {
										cfwProxy.host = hostport.substring(0, hostport.indexOf(":"));
										port = hostport.substring(hostport.indexOf(":")+1);
									}else {
										cfwProxy.host = hostport;
										port = "80";	
									}
									
									try {
										cfwProxy.port = Integer.parseInt(port);
									}catch(Throwable e) {
										new CFWLog(logger)
											.silent(true)
											.severe("Error parsing port to integer.", e);
									}
									proxies.add(cfwProxy);
								}
							}							
						}
						return proxies;
					}
										
				});
				
			} catch (MalformedURLException | ExecutionException e) {
				new CFWLog(logger)
					.severe("Resolving proxies failed.", e);
			}
		}
		
		if (proxyArray == null || proxyArray.size() == 0){
			proxyArray = new ArrayList<CFWProxy>(); 
			CFWProxy direct = instance.new CFWProxy();
			direct.type = "DIRECT";
			proxyArray.add(direct);
		}
		return proxyArray;
	}

	/******************************************************************************************************
	 * Returns a Proxy retrieved from the Proxy PAC Config.
	 * Returns null if there is no Proxy needed or proxies are disabled.
	 * 
	 ******************************************************************************************************/
	public static Proxy getProxy(String url) {
		
		if(CFW.Properties.PROXY_ENABLED == false) {
			return null;
		}else {

			ArrayList<CFWProxy> proxiesArray = getProxies(url);
			
			//--------------------------------------------------
			// Iterate PAC Proxies until address is resolved
			for(CFWProxy cfwProxy : proxiesArray) {

				if(cfwProxy.type.trim().toUpperCase().equals("DIRECT")) {
					return null;
				}else {
					
					InetSocketAddress address = new InetSocketAddress(cfwProxy.host, cfwProxy.port);
					if(address.isUnresolved()) { 
						continue;
					}
					Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
					return proxy;
				}
				
			}
			
		}
		
		return null;
		
	}
	/******************************************************************************************************
	 * Return a URL or null in case of exceptions.
	 * 
	 ******************************************************************************************************/
	public static HttpURLConnection createProxiedURLConnection(String url) {
		
		try {
			if(CFW.Properties.PROXY_ENABLED == false) {
				return (HttpURLConnection)new URL(url).openConnection();
			}else {
				HttpURLConnection proxiedConnection = null;
				ArrayList<CFWProxy> proxiesArray = getProxies(url);
				
				//--------------------------------------------------
				// Return direct if no proxies were returned by PAC
				if(proxiesArray.isEmpty()) {
					return (HttpURLConnection)new URL(url).openConnection();
				}
				
				//--------------------------------------------------
				// Iterate PAC Proxies until address is resolved
				for(CFWProxy cfwProxy : proxiesArray) {

					if(cfwProxy.type.trim().toUpperCase().equals("DIRECT")) {
						proxiedConnection = (HttpURLConnection)new URL(url).openConnection();
					}else {
						
						InetSocketAddress address = new InetSocketAddress(cfwProxy.host, cfwProxy.port);
						if(address.isUnresolved()) { 
							continue;
						};
						Proxy proxy = new Proxy(Proxy.Type.HTTP, address);

						proxiedConnection = (HttpURLConnection)new URL(url).openConnection(proxy);
					}
					return proxiedConnection;
				}
				
				//-------------------------------------
				// None of the addresses were resolved
				new CFWLog(logger)
					.warn("The proxy addresses couldn't be resolved.");
			}
		} catch (MalformedURLException e) {
			new CFWLog(logger)
				.severe("The URL is malformed.", e);
			
		} catch (IOException e) {
			new CFWLog(logger)
				.severe("An IO error occured.", e);
		}
		
		return null;
		
	}
	/******************************************************************************************************
	 * If proxy is enabled, adds a HttpHost to the client.
	 * 
	 * @param clientBuilder the client that should get a proxy
	 * @param targetURL the URL that should be called over a proxy (not the url of the proxy host)
	 * 
	 ******************************************************************************************************/
	public static void httpClientAddProxy(HttpClientBuilder clientBuilder, String targetURL) {
		
		if(CFW.Properties.PROXY_ENABLED == false) {
			return; // nothing todo
		}else {
			ArrayList<CFWProxy> proxiesArray = getProxies(targetURL);
			
			//--------------------------------------------------
			// Return direct if no proxies were returned by PAC
			if(proxiesArray.isEmpty()) {
				return; 
			}
			
			//--------------------------------------------------
			// Iterate PAC Proxies until address is resolved
			for(CFWProxy cfwProxy : proxiesArray) {
				
				if(cfwProxy.type.trim().toUpperCase().equals("DIRECT")) {
					return; // no proxy required
				}else {
					
					InetSocketAddress address = new InetSocketAddress(cfwProxy.host, cfwProxy.port);
					if(address.isUnresolved()) { 
						continue;
					};

					
					HttpHost proxy = new HttpHost(cfwProxy.host, cfwProxy.port);

					DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
					clientBuilder.setRoutePlanner(routePlanner);
				}
				return ; // done
			}
			
			//-------------------------------------
			// None of the addresses were resolved
			new CFWLog(logger)
			.warn("The proxy addresses couldn't be resolved.");
		}

		return ;
		
	}
	
	
	/******************************************************************************************************
	 * Send a HTTP GET request and returns the result or null in case of error.
	 * @param url used for the request.
	 * @return CFWHttpResponse response or null
	 ******************************************************************************************************/
	public static CFWHttpResponse sendGETRequest(String url) {
		return sendGETRequest(url, null, null);
	}
	
	/******************************************************************************************************
	 * Log details about get and post request
	 * @param requestBody TODO
	 ******************************************************************************************************/
	private static void logFinerRequestInfo(String method, String url, HashMap<String, String> params, HashMap<String, String> headers, String requestBody) {
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
	
	/******************************************************************************************************
	 * Send a HTTP GET request and returns the result or null in case of error.
	 * @param url used for the request.
	 * @param params the parameters which should be added to the request or null
	 * @return CFWHttpResponse response or null
	 ******************************************************************************************************/
	public static void addBasicAuthorizationHeader(HashMap<String, String> params, String basicUsername, String basicPassword) {
		
		String valueToEncode = basicUsername + ":" + basicPassword;
		params.put("Authorization", "Basic "+Base64.getEncoder().encodeToString(valueToEncode.getBytes()));	    	
	}
	
	/******************************************************************************************************
	 * Creates a request builder for chained building of requests.
	 * @param url used for the request.
	 ******************************************************************************************************/
	public static CFWHttpRequestBuilder newRequestBuilder(String url) {
		return instance.new CFWHttpRequestBuilder(url);	    	
	}
	
	/******************************************************************************************************
	 * Send a HTTP GET request and returns the result or null in case of error.
	 * @param url used for the request.
	 * @param params the parameters which should be added to the request or null
	 * @return CFWHttpResponse response or null
	 ******************************************************************************************************/
	public static CFWHttpResponse sendGETRequest(String url, HashMap<String, String> params) {
		return sendGETRequest(url, params, null);	    	
	}
	
	/******************************************************************************************************
	 * Send a HTTP GET request and returns the result or null in case of error.
	 * @param url used for the request.
	 * @param params the parameters which should be added to the request or null
	 * @param headers the HTTP headers for the request or null
	 * @return CFWHttpResponse response or null
	 ******************************************************************************************************/
	public static CFWHttpResponse sendGETRequest(String url, HashMap<String, String> params, HashMap<String, String> headers) {

		return CFWHttp.newRequestBuilder(url)
				.GET()
				.headers(headers)
				.params(params)
				.send();
//		try {
//			//-----------------------------------
//			// Handle params
//			url = buildURL(url, params);
//			
//			//-----------------------------------
//			// Handle headers
//			HttpURLConnection connection = createProxiedURLConnection(url);
//			if(connection != null) {
//				if(headers != null && !headers.isEmpty() ) {
//					for(Entry<String, String> header : headers.entrySet()) {
//						connection.setRequestProperty(header.getKey(), header.getValue());
//					}
//				}
//				
//				//-----------------------------------
//				// Connect and create response
//				if(connection != null) {
//					connection.setRequestMethod("GET");
//					connection.connect();
//					
//					outgoingHTTPCallsCounter.labels("GET").inc();
//					return instance.new CFWHttpResponse(connection);
//				}
//				
//			}
//	    
//		} catch (Exception e) {
//			new CFWLog(logger)
//				.severe("Exception occured: "+e.getMessage(), e);
//		} 
//		
//		return null;
	    	
	}
		

	/******************************************************************************************************
	 * Send a HTTP POST request and returns the result or null in case of error.
	 * @param url used for the request.
	 * @param params the parameters which should be added to the requests post body or null
	 * @param headers the HTTP headers for the request or null
	 * @return String response
	 ******************************************************************************************************/
	public static CFWHttpResponse sendPOSTRequest(String url, HashMap<String, String> params, HashMap<String, String> headers) {
		
		return CFWHttp.newRequestBuilder(url)
					.POST()
					.headers(headers)
					.params(params)
					.send();

		
//		try {
//
//			url = buildURL(url, params);
//			HttpURLConnection connection = createProxiedURLConnection(url);
//			connection.setRequestMethod("POST");
//			connection.setInstanceFollowRedirects(true);
//			
//			if(connection != null) {
//				
//				//-----------------------------------
//				// Handle headers
////				if(headers != null ) {
////					for(Entry<String, String> header : headers.entrySet())
////					connection.setRequestProperty(header.getKey(), header.getValue());
////				}
//				
//				//-----------------------------------
//				// Add Params to Request Body
//				
//				// to be checked, not working properly
////				if(params != null ) {
////					
////					String paramsQuery = buildQueryString(params);
////					System.out.println("params: "+paramsQuery);
////					connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded"); 
////					connection.setRequestProperty( "charset", "utf-8");
////					connection.setRequestProperty( "Content-Length", Integer.toString( paramsQuery.length() ));
////					
////					connection.setDoOutput(true);
////					connection.connect();
////					try(OutputStream outStream = connection.getOutputStream()) {
////					    byte[] input = paramsQuery.getBytes("utf-8");
////					    outStream.write(input, 0, input.length);      
////					    outStream.flush();
////					}
////				}
//				
//				//-----------------------------------
//				// Connect and create response
//				if(connection != null) {			
//					outgoingHTTPCallsCounter.labels("POST").inc();
//					return instance.new CFWHttpResponse(connection);
//				}
//			}
//		} catch (Exception e) {
//			new CFWLog(logger)
//				.severe("Exception occured: "+e.getMessage(), e);
//		} 
//		
//		return null;
	    	
	}
	
//	/******************************************************************************************************
//	 * Send a HTTP POST request sending JSON with a Content-Type header "application/json; charset=UTF-8".
//	 * Returns the result or null in case of error.
//	 * 
//	 * @param url used for the request.
//	 * @param body the content of the POST body
//	 * @return String response
//	 ******************************************************************************************************/
//	public static CFWHttpResponse sendPOSTRequestJSON(String url, String body) {
//		return sendPOSTRequest(url, "application/json; charset=UTF-8", body);
//	}
//	
//	/******************************************************************************************************
//	 * Send a HTTP POST request and returns the result or null in case of error.
//	 * @param url used for the request.
//	 * @param contentType the value for the Content-Type header, e.g. " "application/json; charset=UTF-8", or null
//	 * @param body the content of the POST body
//	 * @return String response
//	 ******************************************************************************************************/
//	public static CFWHttpResponse sendPOSTRequest(String url, String contentType, String body) {
//		
//		
//		try {
//			HttpURLConnection connection = createProxiedURLConnection(url);
//			if(connection != null) {
//				connection.setRequestMethod("POST");
//				
//				if(!Strings.isNullOrEmpty(contentType)) {
//					connection.setRequestProperty("Content-Type", contentType);
//				}
//				connection.setDoOutput(true);
//				connection.connect();
//				try(OutputStream outStream = connection.getOutputStream()) {
//				    byte[] input = body.getBytes("utf-8");
//				    outStream.write(input, 0, input.length);           
//				}
//				
//				outgoingHTTPCallsCounter.labels("POST").inc();
//				return instance.new CFWHttpResponse(connection);
//			}
//	    
//		} catch (Exception e) {
//			new CFWLog(logger)
//				.severe("Exception occured.", e);
//		} 
//		
//		return null;
//	    	
//	}
	
	
	
	/******************************************************************************************************
	 * Creates a map of all cookies in a request.
	 * @param request
	 * @return HashMap containing the key value pairs of the cookies in the response
	 ******************************************************************************************************/
	public static HashMap<String,String> getCookiesAsMap(HttpServletRequest request) {
		
		HashMap<String,String> cookieMap = new HashMap<String,String>();
		
		for(Cookie cookie : request.getCookies()) {
			cookieMap.put(cookie.getName(), cookie.getValue());
		}
		
		return cookieMap;
	}
	
	/******************************************************************************************************
	 * Get the cookie
	 * @param url used for the request.
	 * @return String response
	 ******************************************************************************************************/
	public static Cookie getRequestCookie(HttpServletRequest request, String cookieKey) {
		
		if(request.getCookies() != null){
			for(Cookie cookie : request.getCookies()){
				if(cookie.getName().equals(cookieKey)){
					return cookie;
				}
			}
		}
		
		return null;
		
	}
	
	/******************************************************************************************************
	 * Get the body content of the request.
	 * @param url used for the request.
	 * @return String response
	 ******************************************************************************************************/
	public static String getRequestBody(HttpServletRequest request){

	    String body = null;
	    StringBuilder stringBuilder = new StringBuilder();
        
	    try ( 
	    	InputStream inputStream = request.getInputStream();
		    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream)) 
	    ) {
	    	if (inputStream != null) {
		    	char[] charBuffer = new char[128];
	            int bytesRead = -1;
	            
	            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
	                stringBuilder.append(charBuffer, 0, bytesRead);
	            }
	    	} else {
	            stringBuilder.append("");
	        }
	    } catch (IOException e) {
	    	new CFWLog(logger)
				.severe("Exception occured while reading request body. ", e);
	    } 
        
	    body = stringBuilder.toString();
	    return body;
	}
	
	
		
	/******************************************************************************************************
	 * Get clientIP
	 ******************************************************************************************************/
	public static String getClientIP(HttpServletRequest request) {

        String remoteAddr = "";

        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }

        return remoteAddr;
    }
	
	/**************************************************************************************
	 * Returns the API token if provided and if the requested servlet is /app/api.
	 * @param request
	 * @return token or null
	 **************************************************************************************/
	public static String getCFWAPIToken(HttpServletRequest request) {
		
		if(request.getRequestURI().equals("/app/api")) {
			String token = request.getParameter("apitoken");
			if(!Strings.isNullOrEmpty(token)) { return token; }
	
			token = request.getHeader("API-Token");
			if(!Strings.isNullOrEmpty(token)) { return token; }
	
		}
		
		return null;
	}
	
	
	/**************************************************************************************
	 * Gets the server URL as used by the client side from a servlet request.
	 * Returned string will look like "https://www.servername.io:1234" without slash at the
	 * end of the string.
	 * 
	 * @param request
	 * @return String
	 **************************************************************************************/
	public static String getServerURL(HttpServletRequest request) {
		return 
			request.getScheme() 
		  + "://"
	      + request.getServerName()
	      + ":" 
	      + request.getServerPort()   
	    ;
	}
	
	/******************************************************************************************************
	 * Inner Class for HTTP Response
	 ******************************************************************************************************/
	public class CFWHttpRequestBuilder {
		
		private static final String HEADER_CONTENT_TYPE = "Content-Type";
		private CFWHttpAuthMethod authMethod = CFWHttpAuthMethod.BASIC;
		private String username = null;
		private char[] pwdArray = null;
		String method = "GET";
		String URL = null;
		String requestBody = null;
		String requestBodyContentType = "plain/text; charset=UTF-8";
		private boolean autoCloseClient = true;
		long responseTimeoutMillis = CFWTimeUnit.m.toMillis(10); //default timeout of  10 minutes

		private HashMap<String, String> params = new HashMap<>();
		private HashMap<String, String> headers = new HashMap<>();
		
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
		
		/***********************************************
		 * Add a header
		 ***********************************************/
		public CFWHttpRequestBuilder header(String name, String value) {
			headers.put(name, value);
			return this;
		}
		


		
		/***********************************************
		 * Adds a map of headers
		 ***********************************************/
		public CFWHttpRequestBuilder headers(Map<String, String> headerMap) {
			
			if(headerMap == null) { return this; }
			
			this.headers.putAll(headerMap);
			return this;
			
		}
		
		/***********************************************
		 * Toggle autoCloseClient
		 ***********************************************/
		public CFWHttpRequestBuilder autoCloseClient(boolean autoCloseClient) {
			this.autoCloseClient = autoCloseClient;
			return this;
		}
		
		/***********************************************
		 * Get autoCloseClient
		 ***********************************************/
		public boolean autoCloseClient() {
			return this.autoCloseClient;
		}
		
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
			this.requestBody = content;
			return this;
		}
		
		/***********************************************
		 * Add a request Body
		 ***********************************************/
		public CFWHttpRequestBuilder body(String contentType, String content) {
			this.requestBodyContentType = contentType;
			this.header(HEADER_CONTENT_TYPE, contentType);
			this.requestBody = content;
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
			return  buildURL(URL, params);
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
				//HttpURLConnection connection = createProxiedURLConnection(urlWithParams);
				//connection.setRequestMethod(method);
				//connection.setInstanceFollowRedirects(true);
				

				HttpUriRequestBase requestBase = new HttpUriRequestBase(method, URI.create(urlWithParams));
				
				if(requestBase != null) {
					
										
					//-----------------------------------
					// Handle POST Body
					if(requestBody != null) {
						
						StringEntity bodyEntity = new StringEntity(requestBody);
						requestBase.setEntity(bodyEntity);
						
//						if(headers.containsKey(HEADER_CONTENT_TYPE)) {
//							connection.setRequestProperty(HEADER_CONTENT_TYPE, headers.get(HEADER_CONTENT_TYPE));
//						}else if(!Strings.isNullOrEmpty(requestBodyContentType)) {
//							connection.setRequestProperty(HEADER_CONTENT_TYPE, requestBodyContentType);
//						}
//						connection.setDoOutput(true);
//						connection.connect();
//						try(OutputStream outStream = connection.getOutputStream()) {
//						    byte[] input = requestBody.getBytes("utf-8");
//						    outStream.write(input, 0, input.length);           
//						}
					}
					
					

					//----------------------------------
					// Create HTTP Client
					
					HttpClientBuilder clientBuilder = HttpClientBuilder.create();
					clientBuilder.setDefaultRequestConfig(
								 RequestConfig
										.custom()
										.setResponseTimeout(Timeout.of(responseTimeoutMillis, TimeUnit.MILLISECONDS) )
										.build()
							);
					httpClientAddProxy(clientBuilder, URL);
					setSSLContext(clientBuilder);

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
								//CFWHttp.addBasicAuthorizationHeader(headers, username, new String(pwdArray));
								AuthScope authScopeBasic = new AuthScope(targetHost, null, new BasicScheme().getName());
								credProviderBuilder.add(authScopeBasic, username, pwdArray);
								registryBuilder.register(StandardAuthScheme.BASIC, BasicSchemeFactory.INSTANCE);
							break;
							
							//------------------------------
							// Basic 
							case BASIC_HEADER:
								CFWHttp.addBasicAuthorizationHeader(headers, username, new String(pwdArray));
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

						if (this.authMethod != CFWHttpAuthMethod.BASIC_HEADER) {
							//---------------------------------
							// Scheme Factory
							Registry<AuthSchemeFactory> schemeFactoryRegistry = registryBuilder.build();
							
							//---------------------------------
							// Credential Provider
							clientBuilder
								.setDefaultAuthSchemeRegistry(schemeFactoryRegistry)
								.setDefaultCredentialsProvider(credProviderBuilder.build());
						}
						
					}
					
					//-----------------------------------
					// Handle headers
					if(headers != null ) {
						for(Entry<String, String> header : headers.entrySet()) {
							requestBase.addHeader(header.getKey(), header.getValue());
						}
					}

					//-----------------------------------
					// Connect and create response
					CFWHttp.logFinerRequestInfo(method, URL, params, headers, requestBody);	
					outgoingHTTPCallsCounter.labels(method).inc();

					CloseableHttpClient httpClient = clientBuilder.build();

					CFWHttpResponse response = instance.new CFWHttpResponse(httpClient, requestBase, autoCloseClient);
					return response;
					
				}
			} catch (Throwable e) {
				new CFWLog(logger)
					.severe("Exception occured: "+e.getMessage(), e);
			} 

			return null;		
		}
	}
	
	/**************************************************************************************
	 * Loads the keystore, caches it and then returns the cached instance.
	 * 
	 * @return keystore or null if it could not be loaded 
	 **************************************************************************************/
    private static KeyStore getCachedKeyStore() {
    	
    	if(cachedKeyStore == null) {

	    	//-------------------------------------
	    	// Settings
	    	String path = CFWProperties.HTTPS_KEYSTORE_PATH; // or .p12
	    	String keystorePW = CFWProperties.HTTPS_KEYSTORE_PASSWORD;
	
	    	String keystoreType = "PKCS12";
			if(path.endsWith("jks")) {
				keystoreType = "JKS";
			}
			
    		new CFWLog(logger)
    				.custom("path", path)
    				.custom("type", keystoreType)
    				.finest("Loading Keystore")
    				;
	    	//-------------------------------------
	    	// Load Keystore
			try (FileInputStream keyStoreStream = new FileInputStream(path)) { 
				cachedKeyStore = KeyStore.getInstance(keystoreType); // or "PKCS12"
				cachedKeyStore.load(keyStoreStream, keystorePW.toCharArray());
			    
			}catch (Exception e) {
				new CFWLog(logger).severe("Error loading keystore: "+e.getMessage(), e);
			}
    	}
    	
    	return cachedKeyStore;
	    
    }
    
	/**************************************************************************************
	 * Set SSL Context
	 * @throws KeyStoreException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyManagementException 
	 **************************************************************************************/
    private static void addKeyStore(SSLContextBuilder builder) throws Exception {
    	
    	//-------------------------------------
    	// Initialize
    	KeyStore keyStore = getCachedKeyStore();
    	String keyManagerPW = CFWProperties.HTTPS_KEYMANAGER_PASSWORD;
		
	    //-------------------------------------
    	// Add to Context Builder
    	if(keyStore != null) {
		    if( !Strings.isNullOrEmpty(keyManagerPW) ) {
		    	builder.loadKeyMaterial(keyStore, keyManagerPW.toCharArray());
		    }else {
		    	builder.loadKeyMaterial(keyStore, null);
		    }
    	}

    }
    
    
	/**************************************************************************************
	 * Set SSL Context
	 * 
	 **************************************************************************************/
	private static void setSSLContext(HttpClientBuilder builder) throws Exception {
		
		//=====================================================
		// Initialize Connection Manager
		//=====================================================

			//-------------------------------
			// Trust anything
			final TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
		    
			//-------------------------------
			// Create SSL Context Builder
			final SSLContextBuilder sslContextBuilder = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy); 
			addKeyStore(sslContextBuilder);
			
			//-------------------------------
			// Connection Factory
			final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContextBuilder.build(), NoopHostnameVerifier.INSTANCE);
			
			final Registry<ConnectionSocketFactory> socketFactoryRegistry = 
		        RegistryBuilder.<ConnectionSocketFactory> create()
		        .register("https", sslsf )
		        .register("http", new PlainConnectionSocketFactory() )
		        .build();
			
			//-------------------------------
			// Connection Manager
			BasicHttpClientConnectionManager trustAllAndClientCertConnectionManager =
						new BasicHttpClientConnectionManager(socketFactoryRegistry);
		
		//=====================================================
		// Add to Builder
		//=====================================================
	    builder.setConnectionManager(trustAllAndClientCertConnectionManager);

	}
	
	/******************************************************************************************************
	 * Inner Class for HTTP Response
	 ******************************************************************************************************/
	public class CFWHttpResponse {
		
		private Logger responseLogger = CFWLog.getLogger(CFWHttpResponse.class.getName());
		
		private URL url;
		private String body;
		private int status = 500;
		private long duration = -1;
		private Header[] headers;
		
		private boolean errorOccured = false;
		private String errorMessage = null;
		
		CloseableHttpClient httpClient = null;
		private HttpServletResponse response = null;
		
		/******************************************************************************************************
		 * 
		 ******************************************************************************************************/
		public CFWHttpResponse(CloseableHttpClient httpClient, HttpUriRequestBase requestBase, boolean autoCloseClient) {
			
			this.httpClient = httpClient;
			
			//----------------------------------
			// Get URL
			try {
				url = requestBase.getUri().toURL();
			} catch (Exception e) {
				errorOccured = true;
				new CFWLog(responseLogger).severe("URL is malformed:"+e.getMessage(), e);
				return ;
			}
			
			//----------------------------------
			// Send Request and Read Response
			long startMillis = System.currentTimeMillis();
			try {
				
				Boolean success = httpClient.execute(requestBase, new HttpClientResponseHandler<Boolean>() {
					@Override
					public Boolean handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
						if(response != null) {
							status = response.getCode();
							headers = response.getHeaders();
							
							HttpEntity entity = response.getEntity();
							if(entity != null) {
								body = EntityUtils.toString(response.getEntity());
							}

						}
						return true;
					}
				});
				
			} catch (IOException e) {
				errorOccured = true;
				errorMessage = e.getMessage();
				new CFWLog(responseLogger).warn("Exception occured during HTTP request:"+e.getMessage(), e);
				
			}finally {
				long endMillis = System.currentTimeMillis();
				duration = endMillis - startMillis;
				
				if(autoCloseClient) {
					close();
				}
			}
		}
		
		/******************************************************************************************************
		 * 
		 ******************************************************************************************************/
		public void close() {
			if(httpClient != null) {
				httpClient.close(CloseMode.IMMEDIATE);
			}
		}
		
		/******************************************************************************************************
		 * 
		 ******************************************************************************************************/
		public boolean errorOccured() {
			return errorOccured;
		}
		
		/******************************************************************************************************
		 * 
		 ******************************************************************************************************/
		public String errorMessage() {
			return errorMessage;
		}
		
		/******************************************************************************************************
		 * 
		 ******************************************************************************************************/
		public URL getURL() {
			return url;
		}
		
		/******************************************************************************************************
		 * Get the body content of the response.
		 * @return String or null on error
		 ******************************************************************************************************/
		public String getResponseBody() {
			return body;
		}
		
		/******************************************************************************************************
		 * Get the body content of the response as a JsonObject.
		 * @param url used for the request.
		 * @return JsonObject or null in case of issues
		 ******************************************************************************************************/
		public JsonElement getResponseBodyAsJsonElement(){
			
			//----------------------------------
			// Check Body
			if(Strings.isNullOrEmpty(body)) {
				new CFWLog(responseLogger).severe("Http Response was empty, cannot convert to a JsonElement.", new Exception());
				return null;
			}
			
			if(!body.trim().startsWith("{")
			&& !body.trim().startsWith("[")
			) {
				String messagePart = (body.length() <= 100) ? body : body.substring(0, 95)+"... (truncated)";
				new CFWLog(responseLogger).severe("Http Response was not a JsonElement: "+messagePart, new Exception());
				return null;
			}
			
			//----------------------------------
			// Create Object
			JsonElement jsonElement = CFW.JSON.fromJson(body);

			if(jsonElement == null) {
				new CFWLog(responseLogger).severe("Error occured while converting http response body to JSON Element.", new Exception());
			}
			
			return jsonElement;

		}
		
		/******************************************************************************************************
		 * Get the body content of the response as a JsonObject.
		 * @param url used for the request.
		 * @return JsonObject or null in case of issues
		 ******************************************************************************************************/
		public JsonObject getResponseBodyAsJsonObject(){
			
			//----------------------------------
			// Check Body
			if(Strings.isNullOrEmpty(body)) {
				new CFWLog(responseLogger).severe("Http Response was empty, cannot convert to a JsonElement.", new Exception());
				return null;
			}
			
			if(!body.trim().startsWith("{")) {
				String messagePart = (body.length() <= 100) ? body : body.substring(0, 95)+"... (truncated)";
				new CFWLog(responseLogger).severe("Http Response was not a JsonObject: "+messagePart, new Exception());
				return null;
			}
			
			//----------------------------------
			// Create Object
			JsonElement jsonElement = CFW.JSON.fromJson(body);
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			
			if(jsonObject == null) {
				new CFWLog(responseLogger).severe("Error occured while converting http response body to JSON Object.", new Exception());
			}
			
			return jsonObject;

		}
		
		/******************************************************************************************************
		 * Get the body content of the response as a JsonArray.
		 * @param url used for the request.
		 * @return JsonArray never null, empty array on error
		 ******************************************************************************************************/
		public JsonArray getResponseBodyAsJsonArray(){
			
			//----------------------------------
			// Check Body
			if(Strings.isNullOrEmpty(body)) {
				new CFWLog(responseLogger).severe("Http Response was empty, cannot convert to JSON.", new Exception());
				return null;
			}
			
			if(!body.trim().startsWith("{")
			&& !body.trim().startsWith("[")
			) {
				String messagePart = (body.length() <= 100) ? body : body.substring(0, 95)+"... (truncated)";
				new CFWLog(responseLogger).severe("Http Response was not JSON: "+messagePart, new Exception());
				return null;
			}
			
			//----------------------------------
			// CreateArray
			JsonArray jsonArray = new JsonArray();
			
			JsonElement jsonElement = CFW.JSON.fromJson(body);
			if(jsonElement == null || jsonElement.isJsonNull()) {
				return jsonArray;
			}
			
			if(jsonElement.isJsonArray()) {
				jsonArray = jsonElement.getAsJsonArray();
			}else if(jsonElement.isJsonObject()) {
				JsonObject object = jsonElement.getAsJsonObject();
				if(object.get("error") != null) {
					new CFWLog(responseLogger).severe("Error occured while reading http response: "+object.get("error").toString());
					CFW.Messages.addErrorMessage("Error: ");
					return jsonArray;
				}else {
					new CFWLog(responseLogger).severe("Error occured while reading http response:"+CFW.JSON.toString(jsonElement));
				}
			}
			
			return jsonArray;
		}

		/******************************************************************************************************
		 * 
		 ******************************************************************************************************/
		public int getStatus() {
			return status;
		}
		
		/******************************************************************************************************
		 * Returns a state for this response
		 * @param checkErrorResponse toggle if HTTP Status should be checked to be below 400.
		 ******************************************************************************************************/
		public CFWStateOption getState(boolean checkErrorResponse) {
			//------------------------------
			// Check Error Occured
			if( ! this.errorOccured() ) {
				
				//------------------------------
				// Check Status Code
				if(checkErrorResponse) {
					if( this.getStatus() < 400 ) {
						return CFWStateOption.GREEN;
					}else {
						return CFWStateOption.RED;
					}
				}
				
				return CFWStateOption.GREEN;
			}else {
				return CFWStateOption.RED;
			}

		}

		/******************************************************************************************************
		 * Returns the approximate duration that was needed for executing and reading the request.
		 ******************************************************************************************************/
		public long getDuration() {
			return duration;
		}
		
		/******************************************************************************************************
		 * 
		 ******************************************************************************************************/
		public Header[] getHeaders() {
			return headers;
		}
		
		/******************************************************************************************************
		 * 
		 ******************************************************************************************************/
		public JsonObject getHeadersAsJson() {
			
			JsonObject object = new JsonObject();
			for(Header entry : headers) {
				
				if(entry.getName() != null) {
					object.addProperty(entry.getName(), entry.getValue());
				}
			}
			
			return object;
		}
		
	}
	
	protected class CFWProxy {
		public String type;
		public String host;
		public int port = 80;
		
	}
}
