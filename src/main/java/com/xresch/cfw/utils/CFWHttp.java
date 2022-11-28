package com.xresch.cfw.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
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

import org.graalvm.polyglot.Value;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

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
	
	private static String proxyPAC = null;
	private static Cache<String, ArrayList<CFWProxy>> resolvedProxiesCache = CFW.Caching.addCache("CFW Proxies", 
			CacheBuilder.newBuilder()
				.initialCapacity(100)
				.maximumSize(1000)
				.expireAfterAccess(1, TimeUnit.HOURS)
		);
	private static CFWHttp instance = new CFWHttp();
	

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
					HttpURLConnection connection = (HttpURLConnection)new URL(CFW.Properties.PROXY_PAC).openConnection();
					if(connection != null) {
						connection.setRequestMethod("GET");
						connection.connect();
						
						response = instance.new CFWHttpResponse(connection);
					}
			    
				} catch (Exception e) {
					new CFWLog(logger)
						.severe("Exception occured.", e);
				} 
				
				//------------------------------
				// Cache PAC Contents
				if(response != null && response.getStatus() <= 299) {
					proxyPAC = response.getResponseBody();
					
					if(proxyPAC == null || !proxyPAC.contains("FindProxyForURL")) {
						CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The Proxy .pac-File seems not be in the expected format.");
						proxyPAC = null;
					}
				}else {
					CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Error occured while retrieving .pac-File from URL. (HTTP Code: "+response.getStatus()+")");
				}
			}else {
				//------------------------------
				// Load from Disk
				proxyPAC = CFW.Files.getFileContent(CFW.Context.Request.getRequest(), CFW.Properties.PROXY_PAC);
				
				if(proxyPAC == null || !proxyPAC.contains("FindProxyForURL")) {
					CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The Proxy .pac-File seems not be in the expected format.");
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
		
		CFWHttp.logFinerRequestInfo("GET", url, params, headers, null);
		
		try {
			//-----------------------------------
			// Handle params
			url = buildURL(url, params);
			
			//-----------------------------------
			// Handle headers
			HttpURLConnection connection = createProxiedURLConnection(url);
			if(connection != null) {
				if(headers != null && !headers.isEmpty() ) {
					for(Entry<String, String> header : headers.entrySet()) {
						connection.setRequestProperty(header.getKey(), header.getValue());
					}
				}
				
				//-----------------------------------
				// Connect and create response
				if(connection != null) {
					connection.setRequestMethod("GET");
					connection.connect();
					
					outgoingHTTPCallsCounter.labels("GET").inc();
					return instance.new CFWHttpResponse(connection);
				}
				
			}
	    
		} catch (Exception e) {
			new CFWLog(logger)
				.severe("Exception occured: "+e.getMessage(), e);
		} 
		
		return null;
	    	
	}
		

	/******************************************************************************************************
	 * Send a HTTP POST request and returns the result or null in case of error.
	 * @param url used for the request.
	 * @param params the parameters which should be added to the requests post body or null
	 * @param headers the HTTP headers for the request or null
	 * @return String response
	 ******************************************************************************************************/
	public static CFWHttpResponse sendPOSTRequest(String url, HashMap<String, String> params, HashMap<String, String> headers) {
		
		CFWHttp.logFinerRequestInfo("POST", url, params, headers, null);	
		
		try {

			url = buildURL(url, params);
			HttpURLConnection connection = createProxiedURLConnection(url);
			connection.setRequestMethod("POST");
			connection.setInstanceFollowRedirects(true);
			
			if(connection != null) {
				
				//-----------------------------------
				// Handle headers
				if(headers != null ) {
					for(Entry<String, String> header : headers.entrySet())
					connection.setRequestProperty(header.getKey(), header.getValue());
				}
				
				//-----------------------------------
				// Add Params to Request Body
				
				// to be checked, not working properly
//				if(params != null ) {
//					
//					String paramsQuery = buildQueryString(params);
//					System.out.println("params: "+paramsQuery);
//					connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded"); 
//					connection.setRequestProperty( "charset", "utf-8");
//					connection.setRequestProperty( "Content-Length", Integer.toString( paramsQuery.length() ));
//					
//					connection.setDoOutput(true);
//					connection.connect();
//					try(OutputStream outStream = connection.getOutputStream()) {
//					    byte[] input = paramsQuery.getBytes("utf-8");
//					    outStream.write(input, 0, input.length);      
//					    outStream.flush();
//					}
//				}
				
				//-----------------------------------
				// Connect and create response
				if(connection != null) {			
					outgoingHTTPCallsCounter.labels("POST").inc();
					return instance.new CFWHttpResponse(connection);
				}
			}
		} catch (Exception e) {
			new CFWLog(logger)
				.severe("Exception occured: "+e.getMessage(), e);
		} 
		
		return null;
	    	
	}
	
	/******************************************************************************************************
	 * Send a HTTP POST request sending JSON with a Content-Type header "application/json; charset=UTF-8".
	 * Returns the result or null in case of error.
	 * 
	 * @param url used for the request.
	 * @param body the content of the POST body
	 * @return String response
	 ******************************************************************************************************/
	public static CFWHttpResponse sendPOSTRequestJSON(String url, String body) {
		return sendPOSTRequest(url, "application/json; charset=UTF-8", body);
	}
	
	/******************************************************************************************************
	 * Send a HTTP POST request and returns the result or null in case of error.
	 * @param url used for the request.
	 * @param contentType the value for the Content-Type header, e.g. " "application/json; charset=UTF-8", or null
	 * @param body the content of the POST body
	 * @return String response
	 ******************************************************************************************************/
	public static CFWHttpResponse sendPOSTRequest(String url, String contentType, String body) {
		
		
		try {
			HttpURLConnection connection = createProxiedURLConnection(url);
			if(connection != null) {
				connection.setRequestMethod("POST");
				
				if(!Strings.isNullOrEmpty(contentType)) {
					connection.setRequestProperty("Content-Type", contentType);
				}
				connection.setDoOutput(true);
				connection.connect();
				try(OutputStream outStream = connection.getOutputStream()) {
				    byte[] input = body.getBytes("utf-8");
				    outStream.write(input, 0, input.length);           
				}
				
				outgoingHTTPCallsCounter.labels("POST").inc();
				return instance.new CFWHttpResponse(connection);
			}
	    
		} catch (Exception e) {
			new CFWLog(logger)
				.severe("Exception occured.", e);
		} 
		
		return null;
	    	
	}
	
	
	
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
	 * Gets the server URL without any path or query parameters form a servlet request.
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
		
		String method = "GET";
		String URL = null;
		String requestBody = null;
		String requestBodyContentType = "plain/text; charset=UTF-8";
		
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
		 * Add a header
		 ***********************************************/
		public CFWHttpRequestBuilder authenticationBasic(String username, String password) {
			CFWHttp.addBasicAuthorizationHeader(headers, username, password);
			return this;
		}
		
		/***********************************************
		 * Add a request Body
		 ***********************************************/
		public CFWHttpRequestBuilder body(String contentType, String content) {
			this.requestBodyContentType = contentType;
			this.header("Content-Type", contentType);
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
		 * Add a request Body in JSON format UTF-8 encoding
		 ***********************************************/
		public String buildURLwithParams() {
			return  buildURL(URL, params);
		}
		
		/***********************************************
		 * Build and send the request. Returns a 
		 * CFWHttpResponse or null in case of errors.
		 ***********************************************/
		public CFWHttpResponse send() {
			
			CFWHttp.logFinerRequestInfo(method, URL, params, headers, requestBody);	
			
			try {
				
				//---------------------------------
				// Create URL
				String urlWithParams = buildURLwithParams();
				HttpURLConnection connection = createProxiedURLConnection(urlWithParams);
				connection.setRequestMethod(method);
				connection.setInstanceFollowRedirects(true);
				
				if(connection != null) {
					
					//-----------------------------------
					// Handle headers
					if(headers != null ) {
						for(Entry<String, String> header : headers.entrySet())
						connection.setRequestProperty(header.getKey(), header.getValue());
					}
					
					//-----------------------------------
					// Handle POST Body
					if(requestBody != null) {
						if(!Strings.isNullOrEmpty(requestBodyContentType)) {
							connection.setRequestProperty("Content-Type", requestBodyContentType);
						}
						connection.setDoOutput(true);
						connection.connect();
						try(OutputStream outStream = connection.getOutputStream()) {
						    byte[] input = requestBody.getBytes("utf-8");
						    outStream.write(input, 0, input.length);           
						}
					}

					//-----------------------------------
					// Connect and create response
					if(connection != null) {			
						outgoingHTTPCallsCounter.labels(method).inc();
						CFWHttpResponse response = instance.new CFWHttpResponse(connection);
						return response;
					}
				}
			} catch (Throwable e) {
				new CFWLog(logger)
					.severe("Exception occured: "+e.getMessage(), e);
			} 

			return null;		
		}
	}
	
	
	/******************************************************************************************************
	 * Inner Class for HTTP Response
	 ******************************************************************************************************/
	public class CFWHttpResponse {
		
		private Logger responseLogger = CFWLog.getLogger(CFWHttpResponse.class.getName());
		
		private String body;
		private int status = 500;
		private Map<String, List<String>> headers;
		
		private boolean errorOccured = false;
		
		public CFWHttpResponse(HttpURLConnection conn) {
			
			BufferedReader in = null;
			StringBuilder builder = new StringBuilder();
			
			try{
				
				//------------------------------
				// connect if not already done
				conn.connect();
				
				//------------------------------
				// Read Response Body
				status = conn.getResponseCode();
				headers = conn.getHeaderFields();

				//------------------------------
				// Get Response Stream
				if (conn.getResponseCode() <= 299) {
				    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				} else {
				    in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
				}
				
				//------------------------------
				// Read Response Body
		        String inputLine;
		
		        while ((inputLine = in.readLine()) != null) {
		        	builder.append(inputLine);
		        	builder.append("\n");
		        }
		        
		        body = builder.toString();
		        
		        
			}catch(Exception e) {
				new CFWLog(responseLogger)
					.severe("Exception occured while accessing URL: "+e.getMessage(), e);
				errorOccured = true;
			}finally {
				if(in != null) {
					try {
						conn.disconnect();
						in.close();
					} catch (IOException e) {
						new CFWLog(responseLogger)
							.severe("Exception occured while closing http stream.", e);
					}
				}
			}
		}

		public boolean errorOccured() {
			return errorOccured;
		}
		
		/******************************************************************************************************
		 * Get the body content of the response.
		 * @param url used for the request.
		 * @return String or null on error
		 ******************************************************************************************************/
		public String getResponseBody() {
			return body;
		}
		
		
		/******************************************************************************************************
		 * Get the body content of the response as a JsonObject.
		 * @param url used for the request.
		 * @return JsonArray or null
		 ******************************************************************************************************/
		public JsonObject getResponseBodyAsJsonObject(){
			
			JsonElement jsonElement = CFW.JSON.fromJson(body);
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			
			if(jsonObject == null) {
				new CFWLog(responseLogger).severe("Error occured while converting http response body to JSON Object.");
			}
			
			return jsonObject;

		}
		
		/******************************************************************************************************
		 * Get the body content of the response as a JsonArray.
		 * @param url used for the request.
		 * @return JsonArray or null
		 ******************************************************************************************************/
		public JsonArray getResponseBodyAsJsonArray(){
			
			JsonElement jsonElement = CFW.JSON.fromJson(body);
			
			JsonArray jsonArray = null;
			if(jsonElement.isJsonArray()) {
				jsonArray = jsonElement.getAsJsonArray();
			}else if(jsonElement.isJsonObject()) {
				JsonObject object = jsonElement.getAsJsonObject();
				if(object.get("error") != null) {
					new CFWLog(responseLogger).severe("Error occured while reading http response: "+object.get("error").toString());
					CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Error: ");
					return null;
				}else {
					new CFWLog(responseLogger).severe("Error occured while reading http response.");
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
		 * 
		 ******************************************************************************************************/
		public Map<String, List<String>> getHeaders() {
			return headers;
		}
		
		/******************************************************************************************************
		 * 
		 ******************************************************************************************************/
		public JsonObject getHeadersAsJson() {
			
			JsonObject object = new JsonObject();
			for(Entry<String, List<String>> entry : headers.entrySet()) {
				
				if(entry.getKey() != null) {
					object.addProperty(entry.getKey(), Joiner.on(", ").join(entry.getValue()));
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
