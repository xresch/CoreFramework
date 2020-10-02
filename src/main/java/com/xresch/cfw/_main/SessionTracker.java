package com.xresch.cfw._main;

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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw.features.usermgmt.User;

public class SessionTracker implements HttpSessionListener, HttpSessionIdListener, ServletContextListener {
	
	private final Server server;
	private static SessionHandler sessionHandler;
	private String contextPath;
	
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
		System.out.println("contextInitialized");
		contextPath = sce.getServletContext().getContextPath();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		Collection<SessionTracker> trackers = this.server.getBeans(SessionTracker.class);
		trackers.removeIf((tracker) -> tracker.getContextPath().equals(sce.getServletContext().getContextPath()));
	}

	@Override
	public void sessionCreated(HttpSessionEvent se) {
		synchronized (sessionIDs) {
			System.out.println("Add Session");
			sessionIDs.add(se.getSession().getId());
		}
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		synchronized (sessionIDs) {
			System.out.println("Destroy Session");
			sessionIDs.remove(se.getSession().getId());
		}

	}

	@Override
	public void sessionIdChanged(HttpSessionEvent event, String oldSessionId) {
		synchronized (sessionIDs) {
			System.out.println("Change Session");
			sessionIDs.add(oldSessionId);
			sessionIDs.add(event.getSession().getId());
		}
	}
	
	public static void printSessionDetails(){
		synchronized (sessionIDs) {
			for(String id : sessionIDs.toArray(new String[] {})) {
				Session session = sessionHandler.getSession(id);
				SessionData data = (SessionData)session.getAttribute(CFW.SESSION_DATA);
				
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
				SessionData data = (SessionData)session.getAttribute(CFW.SESSION_DATA);
				
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