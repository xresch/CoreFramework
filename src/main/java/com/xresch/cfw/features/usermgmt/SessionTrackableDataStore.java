package com.xresch.cfw.features.usermgmt;

import org.eclipse.jetty.server.session.DatabaseAdaptor;
import org.eclipse.jetty.server.session.JDBCSessionDataStore;
import org.eclipse.jetty.server.session.SessionData;

public class SessionTrackableDataStore extends JDBCSessionDataStore {
	
	SessionTracker tracker;

	public SessionTrackableDataStore(SessionTracker tracker,
									 SessionTableSchema schema, 
									 DatabaseAdaptor adaptor, 
									 int gradePeriodSec, 
									 int savePeriodSec)
    {
		this.tracker = tracker;
        this.setDatabaseAdaptor(adaptor);
        this.setSessionTableSchema(schema);
        this.setGracePeriodSec(gradePeriodSec);
        this.setSavePeriodSec(savePeriodSec);
    }
	
	@Override
	public SessionData doLoad(String id) throws Exception {

		SessionData data = super.doLoad(id);
		
		tracker.addSessionToTracking(id);
		
		return data;
	}
	
	@Override
	public boolean delete(String id) throws Exception {

		boolean success = super.delete(id);
		tracker.removeSessionFromTracking(id);
		
		return success;
	}
	

}
