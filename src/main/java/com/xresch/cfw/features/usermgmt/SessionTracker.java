package com.xresch.cfw.features.usermgmt;

import java.util.Collection;
import java.util.HashSet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionIdListener;
import javax.servlet.http.HttpSessionListener;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.Session;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.component.LifeCycle;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;

import io.prometheus.client.Gauge;

public class SessionTracker implements HttpSessionListener, HttpSessionIdListener, ServletContextListener {
	
	private final Server server;
	private static SessionHandler sessionHandler;
	private String contextPath;
	
	private static final Gauge sessionCounter = Gauge.build()
	         .name("cfw_sessions_count")
	         .help("Total number of session currently open in the application.")
	         .register();
	
	//Do not hold references to session object itself, as it might mess up GC.
	private static HashSet<String> sessionIDs = new HashSet<>();

	public SessionTracker(Server server, SessionHandler handler) {
		this.server = server;
		sessionHandler = handler;
		sessionHandler.addEventListener(this);
	}

	public String getContextPath() {
		return contextPath;
	}

	public SessionHandler getSessionHandler() {
		return sessionHandler;
	}

	public HashSet<String> getSessionIds() {
		return sessionIDs;
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		contextPath = sce.getServletContext().getContextPath();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		Collection<SessionTracker> trackers = this.server.getBeans(SessionTracker.class);
		trackers.removeIf((tracker) -> tracker.getContextPath().equals(sce.getServletContext().getContextPath()));
	}

	@Override
	public void sessionCreated(HttpSessionEvent se) {
		addSessionToTracking(se.getSession().getId());
	}
	


	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		removeSessionFromTracking(se.getSession().getId());
	}

	@Override
	public void sessionIdChanged(HttpSessionEvent event, String oldSessionId) {
		changeIDOfTrackedSession(oldSessionId, event.getSession().getId());
	}
	
	public void removeSessionFromTracking(String id) {
		synchronized (sessionIDs) {
			if(sessionIDs.contains(id)) {
				sessionIDs.remove(id);
				sessionCounter.dec();
			}
		}
	}
	
	public void addSessionToTracking(String id) {
		synchronized (sessionIDs) {
			if(!sessionIDs.contains(id)) {
				sessionIDs.add(id);
				sessionCounter.inc();
			}
		}
	}
	
	public void changeIDOfTrackedSession(String oldID, String newID) {
		synchronized (sessionIDs) {
			if(sessionIDs.contains(oldID)) {
				sessionIDs.remove(oldID);
			}
			sessionIDs.add(newID);
		}
	}
	
	public static void updateUserRights(int userID){
		synchronized (sessionIDs) {
			
			for(String id : sessionIDs.toArray(new String[] {})) {
				Session session = sessionHandler.getSession(id);
				
				if(session != null) {
					
					CFWSessionData data = (CFWSessionData)session.getAttribute(CFW.SESSION_DATA);

					if(data != null) {

						User user = data.getUser();
						if(user != null && user.id() != null && user.id() == userID) {

							data.loadUserPermissions();
						}
					}
				}
			}
		}
	}
	
	public static void printSessionDetails(){
		synchronized (sessionIDs) {
			for(String id : sessionIDs.toArray(new String[] {})) {
				Session session = sessionHandler.getSession(id);
				CFWSessionData data = (CFWSessionData)session.getAttribute(CFW.SESSION_DATA);
				
				if(data != null) {
					System.out.println("======== Session Data ======");
					System.out.println("SessionID: "+id);
					System.out.println("CreationTime: "+session.getCreationTime());
					System.out.println("LastAccessedTime: "+session.getLastAccessedTime());
					System.out.println("MaxInactiveInterval: "+session.getMaxInactiveInterval());
					
					User user = data.getUser();
					if(user != null) {
						System.out.println("Username: "+user.username());
						System.out.println("Client IP: "+data.getClientIP());
					}
				}
			}
		}
	}
	
	public static String getSessionDetailsAsJSON(){
		
		JsonArray array = new JsonArray();
		
		synchronized (sessionIDs) {
			
			for(String id : sessionIDs.toArray(new String[] {})) {
				Session session = sessionHandler.getSession(id);
				
				if(session == null) { continue ; };
				CFWSessionData data = (CFWSessionData)session.getAttribute(CFW.SESSION_DATA);
				
				if(data != null) {
					JsonObject sessionDetails = new JsonObject();
					int sessionTimoutMillis = session.getMaxInactiveInterval()*1000;
					long creationMillis = session.getCreationTime();
					long lastAccessMillis = session.getLastAccessedTime();
					long lifetimeMillis = System.currentTimeMillis() - creationMillis;
					long expirationMillis = session.getLastAccessedTime() + sessionTimoutMillis;
					
					sessionDetails.addProperty("CLIENT_IP", data.getClientIP());
					sessionDetails.addProperty("SESSION_ID", session.getId());
					sessionDetails.addProperty("SESSION_ID_EXTENDED", session.getExtendedId());
					sessionDetails.addProperty("CREATION_TIME", creationMillis);
					sessionDetails.addProperty("LAST_ACCESS_TIME", lastAccessMillis);
					sessionDetails.addProperty("SESSION_TIMOUT", sessionTimoutMillis);
					sessionDetails.addProperty("ALIVE_TIME", lifetimeMillis);
					sessionDetails.addProperty("EXPIRATION_TIME", expirationMillis);
					sessionDetails.addProperty("IS_LOGGED_IN", data.isLoggedIn());
					User user = data.getUser();
					if(user != null) {
						sessionDetails.addProperty("USERNAME", user.username());
						sessionDetails.addProperty("FIRSTNAME", user.firstname());
						sessionDetails.addProperty("LASTNAME", user.lastname());
						
					}
					
					array.add(sessionDetails);
					
				}
			}
		}
		
		return array.toString();
	}

}
