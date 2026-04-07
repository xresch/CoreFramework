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
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.graalvm.polyglot.Value;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWProperties;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.utils.CFWState.CFWStateOption;
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
	private static PoolingHttpClientConnectionManager connectionManager = null;
	
	private static KeyStore cachedKeyStore = null;
	
	private static String proxyPACScript = null;
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

	static final Counter outgoingHTTPCallsCounter = Counter.build()
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
			javascriptEngine.set(CFW.Scripting.createJavascriptContext().putMemberWithFunctions( new CFWHttpPacScriptMethods()) );
			
			//------------------------------
			// Add to engine if pac loaded
			loadPacFile();
			
			//Prepend method calls with CFWHttpPacScriptMethods
			proxyPACScript = CFWHttpPacScriptMethods.preparePacScript(proxyPACScript);

			CFWScriptingContext polyglot = getScriptContext();
			if(proxyPACScript != null) {
			    polyglot.addScript("proxy.js", proxyPACScript);
			    //polyglot.executeScript("FindProxyForURL('localhost:9090/test', 'localhost');");
			}else {
				new CFWLog(logger).warn("Proxy PAC file was not loaded properly.");
			}
		}
		
		return javascriptEngine.get();
	}
	
	/******************************************************************************************************
	 * 
	 ******************************************************************************************************/
	private static void loadPacFile() {
		
		if(CFW.Properties.PROXY_ENABLED && proxyPACScript == null) {
		
			if(CFW.Properties.PROXY_PAC.toLowerCase().startsWith("http")) {
				//------------------------------
				// Get PAC from URL
				CFWHttpResponse response = null;
				
				try {	
					
					// IMPORTANT: Cannot use 
					HttpURLConnection connection = (HttpURLConnection)new URL(CFW.Properties.PROXY_PAC).openConnection();
					if(connection != null) {
						connection.setRequestMethod("GET");
						connection.connect();
						
						response = new CFWHttpResponse(connection);
					}
			    
				} catch (Exception e) {
					new CFWLog(logger)
						.severe("Exception occured.", e);
				} 
				
				//------------------------------
				// Cache PAC Contents
				if(response != null && response.getStatus() <= 299) {
					proxyPACScript = response.getResponseBody();
					
					if(proxyPACScript == null || !proxyPACScript.contains("FindProxyForURL")) {
						CFW.Messages.addErrorMessage("The Proxy .pac-File seems not be in the expected format.");
						proxyPACScript = null;
					}
				}else {
					CFW.Messages.addErrorMessage("Error occured while retrieving .pac-File from URL. (HTTP Code: "+response.getStatus()+")");
				}
			}else {
				//------------------------------
				// Load from Disk
				proxyPACScript = CFW.Files.getFileContent(CFW.Context.Request.getRequest(), CFW.Properties.PROXY_PAC);
				
				if(proxyPACScript == null || !proxyPACScript.contains("FindProxyForURL")) {
					CFW.Messages.addErrorMessage("The Proxy .pac-File seems not be in the expected format.");
					proxyPACScript = null;
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
		
		if(CFW.Properties.PROXY_ENABLED && proxyPACScript != null) {
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
	public static void httpClientAddProxy(HttpClientBuilder clientBuilder) {
		
						
	    clientBuilder.setRoutePlanner(new HttpRoutePlanner() {
			
			@Override
			public HttpRoute determineRoute(HttpHost target, HttpContext context) throws HttpException {
				

				
				//----------------------------------
				// Build full target URL from HttpHost
				String scheme = target.getSchemeName();
				String host = target.getHostName();
				int port = target.getPort();
				boolean isSecure = "https".equalsIgnoreCase(scheme);
				
				StringBuilder urlBuilder = new StringBuilder();
				urlBuilder.append(scheme).append("://").append(host);
				
				if (port > 0) {
				    urlBuilder.append(":").append(port);
				}
				
				String url = urlBuilder.toString();
                
				//----------------------------------
				// Doing this because someone had the
				// grandiose idea to throw an exception
				// when the port is not set(-1).
				int finalPort = port;
				if (port <= 0) {
					if(isSecure) {	finalPort = 443; }
					else		 {	finalPort = 80; }
						
				}
				HttpHost finalTarget = new HttpHost(scheme, host, finalPort);
				
				//--------------------------------
				// Check Do nothing
				if(CFW.Properties.PROXY_ENABLED == false
				|| Strings.isNullOrEmpty(proxyPACScript)) {
					return new HttpRoute(finalTarget); 
				}
				
				//----------------------------------
				// Resolve proxies for THIS request
				ArrayList<CFWProxy> proxiesArray = getProxies(url);

				//----------------------------------
				// No proxy → DIRECT
				if (proxiesArray == null || proxiesArray.isEmpty()) {
				    return new HttpRoute(finalTarget);
				}
				
				//--------------------------------------------------
				// Iterate PAC Proxies until address is resolved
				for(CFWProxy cfwProxy : proxiesArray) {
					
					if(cfwProxy.type.trim().toUpperCase().equals("DIRECT")) {
						return new HttpRoute(finalTarget); // no proxy required
					}else {
						
						InetSocketAddress address = new InetSocketAddress(cfwProxy.host, cfwProxy.port);
						if(address.isUnresolved()) { 
							continue;
						};

						HttpHost proxy = new HttpHost(cfwProxy.host, cfwProxy.port);

						return new HttpRoute(finalTarget, null, proxy, isSecure);
					}
				}
				
				//-------------------------------------
				// None of the addresses were resolved
				new CFWLog(logger).warn("The proxy addresses couldn't be resolved.");
				
				return new HttpRoute(target);
			}
		});
	    
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
		return new CFWHttpRequestBuilder(url);	    	
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
    
	/******************************************************************************************************
	 * Returns the connection manager used for all the connections.
	 * @return 
	 * 
	 ******************************************************************************************************/
	public static PoolingHttpClientConnectionManager getConnectionManager() {
		
		synchronized (logger) {
			
			if(connectionManager == null) {

				try{
					connectionManager = new PoolingHttpClientConnectionManager(getSocketFactoryRegistry());
				}catch(Exception e) {
					connectionManager = new PoolingHttpClientConnectionManager();
				}
				
				connectionManager.setMaxTotal(2000);
				connectionManager.setDefaultMaxPerRoute(200);

				
				connectionManager.setDefaultConnectionConfig(
						ConnectionConfig.custom()
					        .setConnectTimeout( Timeout.ofMilliseconds(5000) )
					        .setSocketTimeout(Timeout.ofMilliseconds(10000) )
					        .build()
				        );
				
				connectionManager.setDefaultSocketConfig(SocketConfig.custom()
						    .setSoKeepAlive(true)
						    .setTcpNoDelay(true)
						    .setSoLinger(TimeValue.ofSeconds(5))
						    .build()
					    );
			}
		}
		return connectionManager;
	}
	
	/**************************************************************************************
	 * Set SSL Context
	 * 
	 **************************************************************************************/
	private static Registry<ConnectionSocketFactory> getSocketFactoryRegistry() throws Exception {
		
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
			
			return socketFactoryRegistry;
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
	public static void setSSLContext(HttpClientBuilder builder) throws Exception {
		
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
	
	protected class CFWProxy {
		public String type;
		public String host;
		public int port = 80;
		
	}
}
