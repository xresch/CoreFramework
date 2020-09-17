package com.xresch.cfw._main;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;
import javax.servlet.SessionTrackingMode;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.server.session.DefaultSessionCache;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.NullSessionDataStore;
import org.eclipse.jetty.server.session.SessionCache;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;

import com.xresch.cfw.handler.AuthenticationHandler;
import com.xresch.cfw.handler.HTTPSRedirectHandler;
import com.xresch.cfw.handler.RedirectDefaultPageHandler;
import com.xresch.cfw.handler.RequestHandler;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.utils.CFWHttp.CFWHttpResponse;
import com.xresch.cfw.utils.HandlerChainBuilder;

import io.prometheus.client.jetty.JettyStatisticsCollector;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWApplicationExecutor {
	
	private static final Logger logger = CFWLog.getLogger(CFW.class.getName());
	
	private Server server;
	private MultipartConfigElement globalMultipartConfig;
	private boolean isStarted = false;
	
	ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
	
	private String defaultURL = "/";
	static DefaultSessionIdManager idmanager;
	private SessionHandler sessionHandler;	
	
	public WebAppContext applicationContext;
	
	public CFWAppInterface application;
	
	public CFWApplicationExecutor(CFWAppInterface application) throws Exception {  
		this.application = application;
		
		sessionHandler = CFWApplicationExecutor.createSessionHandler("/");
		
    	//---------------------------------------
    	// Create Server 
        server = CFWApplicationExecutor.createServer();
        applicationContext = new WebAppContext();
        applicationContext.setContextPath("/");
        applicationContext.setServer(server);
        applicationContext.setSessionHandler(sessionHandler);
        applicationContext.setErrorHandler(CFWApplicationExecutor.createErrorHandler());

    	//---------------------------------------
    	// Default Multipart Config
        int maxSize = 1024*1024*CFW.Properties.APPLICATION_MAX_UPLOADSIZE;
        globalMultipartConfig = new MultipartConfigElement(null, maxSize, maxSize, maxSize);
         
	}
	
	
	/**************************************************************************************************
	 * Check if the a servlet is already mapped to the specified path.
	 **************************************************************************************************/
	public boolean isServletPathUsed(String path){
		boolean alreadyExists = false;
		for(ServletMapping mappings : servletContext.getServletHandler().getServletMappings()) {
			
			for(String pathSpec : mappings.getPathSpecs()) {
				if(path.equals(pathSpec)) {
					alreadyExists = true;
					break;
				}
			}
			if(alreadyExists) {
				break;
			}
		}
		
		return alreadyExists;
	}
	/**************************************************************************************************
	 * Adds a servlet to the secure application context that needs authentication to access.
	 * The resulting path will be CFW.Properties.BASE_URL + "/app" + path.
	 * @param the relative path of the context,
	 **************************************************************************************************/
	public ServletHolder addAppServlet(Class<? extends Servlet> clazz, String path){
		return this.servletContext.addServlet(clazz, "/app"+path);
	}
	
	/**************************************************************************************************
	 * Adds a servlet to the secure application context that needs authentication to access.
	 * The resulting path will be CFW.Properties.BASE_URL + "/app" + path.
	 * 
	 * @param the relative path of the context, CFWConfig.BASE_URL will be prepended.
	 **************************************************************************************************/
	public void addAppServlet(ServletHolder holder, String path){
		this.servletContext.addServlet(holder, "/app"+path);
	}
	
	/**************************************************************************************************
	 * Adds a servlet to the secure application context that needs authentication to access.
	 * The resulting path will be CFW.Properties.BASE_URL + path.
	 * 
	 * @param the relative path of the context, CFWConfig.BASE_URL will be prepended.
	 **************************************************************************************************/
	public ServletHolder addUnsecureServlet(Class<? extends Servlet> clazz, String path){
		return this.servletContext.addServlet(clazz, path);
	}
	
	/**************************************************************************************************
	 * Adds a servlet to the secure application context that needs authentication to access.
	 * The resulting path will be CFW.Properties.BASE_URL + path.
	 * 
	 * @param the relative path of the context, CFWConfig.BASE_URL will be prepended.
	 **************************************************************************************************/
	public void addUnsecureServlet(ServletHolder holder, String path){
		this.servletContext.addServlet(holder, path);
	}
	
	/**************************************************************************************************
	 * Adds a servlet to the secure application context that needs authentication to access.
	 * The resulting path will be CFW.Properties.BASE_URL + path.
	 * 
	 * @param the relative path of the context, CFWConfig.BASE_URL will be prepended.
	 * @return 
	 **************************************************************************************************/
	public HttpSession createNewSession(HttpServletRequest request){
		
		HttpSession session = servletContext.getSessionHandler().getSession(request.getSession().getId());
		if(session == null) {
			session = servletContext.getSessionHandler().newHttpSession(request);
		}
		
		return session;
	}
	
	
	public Server getServer() {
		return server;
	}

	public MultipartConfigElement getGlobalMultipartConfig() {
		return globalMultipartConfig;
	}

	public String getDefaultURL() {
		return defaultURL;
	}

	/**************************************************************************************************
	 * Set the default URL for redirects, e.g after login.
	 * @param defaultURL 
	 * @param isAppServlet prepends "/app" if true
	 **************************************************************************************************/
	public void setDefaultURL(String defaultURL, boolean isAppServlet) {
		if(isAppServlet) {
			this.defaultURL = "/app"+defaultURL;
		}else {
			this.defaultURL = defaultURL;
		}
	}
	
	/**************************************************************************************************
	 * Create an error handler.
	 * @throws Exception
	 **************************************************************************************************/
	public static ErrorHandler createErrorHandler() {
//	    ErrorPageErrorHandler errorHandler = new ErrorPageErrorHandler();
//	    errorHandler.addErrorPage(404, "/missing.html");
//	    context.setErrorHandler(errorHandler);
	    
		// Extend ErrorHandler and overwrite methods to create custom error page
	    ErrorHandler handler = new ErrorHandler();
	    return handler;
	}
	   

	/***********************************************************************
	 * Setup and returns a SessionHandler
	 * @param string 
	 ***********************************************************************/
	public static SessionHandler createSessionHandler(String path) {
	
	    SessionHandler sessionHandler = new SessionHandler();
	    
	    
	    sessionHandler.setSessionIdManager(CFWApplicationExecutor.idmanager);
	    // workaround maxInactiveInterval=-1 issue
	    // set inactive interval in RequestHandler
	    sessionHandler.setMaxInactiveInterval(CFW.Properties.SESSION_TIMEOUT);
	    sessionHandler.setHttpOnly(false);
	    sessionHandler.setUsingCookies(true);
	    sessionHandler.setSecureRequestOnly(false);
	    
	    HashSet<SessionTrackingMode> trackingModes = new HashSet<>();
	    trackingModes.add(SessionTrackingMode.COOKIE);
	    sessionHandler.setSessionTrackingModes(trackingModes);
	    sessionHandler.getSessionCookieConfig().setPath(path);
	    sessionHandler.getSessionCookieConfig().setHttpOnly(true);
	    sessionHandler.getSessionCookieConfig().setName("CFWSESSIONID");
	    
	    //prevent URL rewrite
	    sessionHandler.setSessionIdPathParameterName("none");
	    
	    // Explicitly set Session Cache and null Datastore.
	    // This is normally done by default,
	    // but is done explicitly here for demonstration.
	    // If more than one context is to be deployed, it is
	    // simpler to use SessionCacheFactory and/or
	    // SessionDataStoreFactory instances set as beans on 
	    // the server.
	    SessionCache cache = new DefaultSessionCache(sessionHandler);
	    cache.setSessionDataStore(new NullSessionDataStore());
	    sessionHandler.setSessionCache(cache);
	
	    return sessionHandler;
	}

	/***********************************************************************
	 * Setup and returns a ResourceHandler
	 ***********************************************************************/
	public static ContextHandler createResourceHandler() {
	
	    ResourceHandler resourceHandler = new ResourceHandler();
	    // Configure the ResourceHandler. Setting the resource base indicates where the files should be served out of.
	    // In this example it is the current directory but it can be configured to anything that the jvm has access to.
	    resourceHandler.setDirectoriesListed(false);
	    resourceHandler.setResourceBase("./resources");
	
	    // Add the ResourceHandler to the server.
	    ContextHandler resourceContextHandler = new ContextHandler();
	    resourceContextHandler.setContextPath("/resources");
	    
	    GzipHandler resourceGzipHandler = new GzipHandler();
	    
	    resourceContextHandler.setHandler(resourceGzipHandler);
	    resourceGzipHandler.setHandler(resourceHandler);
	    
	    return resourceContextHandler;
	}

	/***********************************************************************
	 * Create a Server with the defined HTTP and HTTPs settings in the 
	 * cfw.properties.
	 * @return Server instance
	 ***********************************************************************/
	private static Server createServer() {
		Server server = new Server();
		ArrayList<Connector> connectorArray = new ArrayList<>();
		
		CFWApplicationExecutor.idmanager = new DefaultSessionIdManager(server);
	    server.setSessionIdManager(CFWApplicationExecutor.idmanager);
		
		if(CFWProperties.HTTP_ENABLED) {
			HttpConfiguration httpConf = new HttpConfiguration();
			httpConf.setSecurePort(CFWProperties.HTTPS_PORT);
			httpConf.setSecureScheme("https");
		    // Add support for X-Forwarded headers
			httpConf.addCustomizer( new org.eclipse.jetty.server.ForwardedRequestCustomizer());

			ServerConnector httpConnector = new ServerConnector(server, new HttpConnectionFactory(httpConf));
			httpConnector.setName("unsecured");
			httpConnector.setHost(CFWProperties.HTTP_CONNECTOR_HOST);
			httpConnector.setPort(CFWProperties.HTTP_PORT);
			connectorArray.add(httpConnector);
		}
		
		if(CFWProperties.HTTPS_ENABLED) {
			HttpConfiguration httpsConf = new HttpConfiguration();
			httpsConf.addCustomizer(new SecureRequestCustomizer());
			httpsConf.setSecurePort(CFWProperties.HTTPS_PORT);
			httpsConf.setSecureScheme("https");
			// Add support for X-Forwarded headers
			httpsConf.addCustomizer( new org.eclipse.jetty.server.ForwardedRequestCustomizer());
		
			SslContextFactory sslContextFactory = new SslContextFactory();
			sslContextFactory.setKeyStorePath(CFWProperties.HTTPS_KEYSTORE_PATH);
			sslContextFactory.setKeyStorePassword(CFWProperties.HTTPS_KEYSTORE_PASSWORD);
			sslContextFactory.setKeyManagerPassword(CFWProperties.HTTPS_KEYMANAGER_PASSWORD);
			
			ServerConnector httpsConnector = new ServerConnector(server,
					new SslConnectionFactory(sslContextFactory, "http/1.1"),
					new HttpConnectionFactory(httpsConf));
			httpsConnector.setName("secured");
			httpsConnector.setHost(CFWProperties.HTTP_CONNECTOR_HOST);
			httpsConnector.setPort(CFWProperties.HTTPS_PORT);
			
			connectorArray.add(httpsConnector);
		}
		
		server.setConnectors(connectorArray.toArray(new Connector[] {}));
		
		return server;
	}
		
	/**************************************************************************************************
	 * @throws Exception
	 **************************************************************************************************/
	public void start() throws Exception {
		
		if(isStarted) {
			return;
		}
        //###################################################################
        // Create Handler chain
        //###################################################################
        //----------------------------------
        // Build Handler Chain
        ContextHandler contextHandler = new ContextHandler("/");	 
        servletContext.setSessionHandler(createSessionHandler("/"));
        
        //------------------------------------
        //Prometheus Statistics
        StatisticsHandler stats = new StatisticsHandler();
        new JettyStatisticsCollector(stats).register();
        
        //----------------------------------
        // Build Handler Chain
        new HandlerChainBuilder(contextHandler)
	        .chain(new GzipHandler())
	        .chain(stats)
	    	.chain(new RequestHandler())
	    	//.chain(createSPNEGOSecurityHandler())
	        .chain(new AuthenticationHandler("/app", defaultURL))
	        .chain(servletContext);
                
        //###################################################################
        // Create Handler Collection
        //###################################################################
        
        //Connect all relevant Handlers
        ArrayList<Handler> handlerArray = new ArrayList<>();
        handlerArray.add(new ShutdownHandler(CFW.Properties.APPLICATION_ID, true, true));
        handlerArray.add(new HTTPSRedirectHandler());
        handlerArray.add(new RedirectDefaultPageHandler(defaultURL));
        handlerArray.add(CFWApplicationExecutor.createResourceHandler());
        handlerArray.add(contextHandler);
        
        HandlerCollection handlerCollection = new HandlerCollection();
        handlerCollection.setHandlers(handlerArray.toArray(new Handler[] {}));
        server.setHandler(handlerCollection);
        
        //###################################################################
        // Startup
        //###################################################################
        CFW.Context.App.setApp(this);
        server.start();
        server.join();
	}

		
	/**************************************************************************************************
	 * 
	 **************************************************************************************************/
	public static void sendStopRequest() {
		
		System.out.println("Try to stop running application instance.");
		
		//----------------------------------
		// Resolve Port to use
		String protocol = "http";
		int port = CFW.Properties.HTTP_PORT;
		if(!CFW.Properties.HTTP_ENABLED && CFW.Properties.HTTPS_ENABLED) {
			protocol = "https";
			port = CFW.Properties.HTTPS_PORT;
		}
		
		//----------------------------------
		// Try Stop 
        try {
        	URL url = new URL(protocol, "localhost", port, "/cfw/shutdown?token="+CFW.Properties.APPLICATION_ID);
        	CFWHttpResponse response = CFW.HTTP.sendGETRequest(url.toString());
        	 

             if(response.getStatus() == 200) {
            	 System.out.println("Shutdown successful.");
            	 
             }else {
            	 System.err.println("Jetty returned response code HTTP "+response.getStatus());
             }
             
             System.out.println(response.getResponseBody());
             
        } catch (IOException ex) {
            System.err.println("Stop Jetty failed: " + ex.getMessage());
        }
	}
	
	/**************************************************************************************************
	 * 
	 **************************************************************************************************/
	public void stop() {
		
		CFWLog log = new CFWLog(logger);
		log.info("Shutdown request recieved");

		
		//----------------------------------
		// Shutdown server
		try {
			// Jetty Default Shutdown
			server.stop();
		} catch (Exception e) {
			log.severe("Error while stopping applicationserver:"+e.getMessage(), e);
		}
		
		//----------------------------------
		// Stop Features 
	    ArrayList<CFWAppFeature> features = CFW.Registry.Features.getFeatureInstances();
	    
	    for(CFWAppFeature feature : features) {
	    	feature.stopFeature();
	    }
	    
		//----------------------------------
		// Stop Application
	    application.stopApp();
	    
		//----------------------------------
		// Shutdown Database
		CFW.DB.stopDBServer();
			
		System.exit(0);
       
	}
	
	/**************************************************************************************************
	 * 
	 **************************************************************************************************/
	public String dumpServletContext() {
        return servletContext.dump();
	}


}
