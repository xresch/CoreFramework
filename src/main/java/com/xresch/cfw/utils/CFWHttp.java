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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWHttp {
	
	private static Logger logger = CFWLog.getLogger(CFWHttp.class.getName());
	
	private static CFWScriptEngine javascriptEngine = CFW.Scripting.createJavascriptEngine(CFWHttpPacScriptMethods.class);
	
	private static String proxyPAC = null;
	private static Cache<String, ArrayList<CFWProxy>> resolvedProxiesCache = CFW.Caching.addCache("CFW Proxies", 
			CacheBuilder.newBuilder()
				.initialCapacity(100)
				.maximumSize(1000)
				.expireAfterAccess(1, TimeUnit.HOURS)
		);
	private static CFWHttp instance = new CFWHttp();
	
	public static String encode(String toEncode) {
		
		try {
			return URLEncoder.encode(toEncode, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			new CFWLog(logger)
				.severe("Exception while encoding: "+e.getMessage(), e);	
		}
		
		return toEncode;
	}
	
	/******************************************************************************************************
	 * Returns an encoded parameter string with leading '&'.
	 ******************************************************************************************************/
	public static String  encode(String paramName, String paramValue) {
		
		return "&" + encode(paramName) + "=" + encode(paramValue);
	}
	
	/******************************************************************************************************
	 * Creates a url with the given parameters;
	 ******************************************************************************************************/
	public static String  buildURL(String urlWithPath, HashMap<String,String> params) {
		
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
			
			//------------------------------
			// Add to engine if load successful
			if(proxyPAC != null) {
				javascriptEngine.addScript(proxyPAC);
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
						
						Object result = javascriptEngine.executeJavascript("FindProxyForURL", urlToCall, hostname);
						if(result != null) {
							String[] proxyArray = result.toString()
								.split(";");
							
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
//							System.out.println("==== Proxies ====");
//							System.out.println(CFW.JSON.toJSON(proxyArray));
//							System.out.println(CFW.JSON.toJSON(proxies));
							
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
	 * @param params the parameters which should be added to the request.
	 * @return String response
	 ******************************************************************************************************/
	public static CFWHttpResponse sendGETRequest(String url, HashMap<String, String> params) {
		return sendGETRequest(buildURL(url, params));
	}
		
	/******************************************************************************************************
	 * Send a HTTP GET request and returns the result or null in case of error.
	 * @param url used for the request.
	 * @return String response
	 ******************************************************************************************************/
	public static CFWHttpResponse sendGETRequest(String url) {
		
		try {			
			HttpURLConnection connection = createProxiedURLConnection(url);
			if(connection != null) {
				connection.setRequestMethod("GET");
				connection.connect();
				
				return instance.new CFWHttpResponse(connection);
			}
	    
		} catch (Exception e) {
			new CFWLog(logger)
				.severe("Exception occured.", e);
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
	/******************************************************************************************************
	 * Inner Class for HTTP Response
	 ******************************************************************************************************/
	public class CFWHttpResponse {
		
		private Logger responseLogger = CFWLog.getLogger(CFWHttpResponse.class.getName());
		
		private String body;
		private int status = 500;
		private Map<String, List<String>> headers;
		
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
			}finally {
				if(in != null) {
					try {
						in.close();
					} catch (IOException e) {
						new CFWLog(responseLogger)
							.severe("Exception occured while closing http stream.", e);
					}
				}
			}
		}

		public String getResponseBody() {
			return body;
		}

		public int getStatus() {
			return status;
		}

		public Map<String, List<String>> getHeaders() {
			return headers;
		}
		
	}
	
	protected class CFWProxy {
		public String type;
		public String host;
		public int port = 80;
		
	}

	/**************************************************************************************
	 * Returns the API token if provided.
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
}
