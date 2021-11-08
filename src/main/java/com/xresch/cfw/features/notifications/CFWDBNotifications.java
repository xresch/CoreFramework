package com.xresch.cfw.features.notifications;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.jobs.CFWJob;
import com.xresch.cfw.features.jobs.CFWJob.CFWJobFields;
import com.xresch.cfw.features.notifications.Notification.NotificationFields;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBNotifications {

		
	private static Class<Notification> cfwObjectClass = Notification.class;
	
	private static final Logger logger = CFWLog.getLogger(CFWDBNotifications.class.getName());
		
	//private static final String[] auditLogFieldnames = new String[] { CFWNotificationFields.PK_ID.toString()};
	
	//####################################################################################################
	// Preckeck Initialization
	//####################################################################################################
	private static PrecheckHandler prechecksCreate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			Notification Notification = (Notification)object;
			
			if(Notification.title() == null || Notification.title().isEmpty()) {
				new CFWLog(logger)
					.warn("Please specify a title for the notification.", new Throwable());
				return false;
			}

			return true;
		}
	};
	
	private static PrecheckHandler prechecksUpdate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			Notification note = (Notification)object;
			
			if(note.title() == null || note.title().isEmpty()) {
				new CFWLog(logger)
					.warn("The title of the Notification cannot be null.", new Throwable());
				return false;
			}
			
			return true;
		}
	};
	
	private static PrecheckHandler prechecksDelete =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			Notification note = (Notification)object;
			
			if(note.foreignKeyUser().intValue() == CFW.Context.Request.getUser().id()) {
				return true;
			}
			
			return false;
		}
	};
		
	//####################################################################################################
	// CREATE
	//####################################################################################################
	public static boolean	create(Notification... items) 	{ return CFWDBDefaultOperations.create(prechecksCreate,items); }
	public static boolean 	create(Notification item) 		{ return CFWDBDefaultOperations.create(prechecksCreate, item);}
	public static Integer 	createGetPrimaryKey(Notification item) { return CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreate, item);}
	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(Notification... items) 	{ return CFWDBDefaultOperations.update(prechecksUpdate, items); }
	public static boolean 	update(Notification item) 		{ return CFWDBDefaultOperations.update(prechecksUpdate, item); }
		
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean 	deleteByID(int id) 	{ 
		return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, cfwObjectClass, NotificationFields.PK_ID.toString(), id); 
	}
	
	public static boolean 	deleteMultipleByID(String IDs) 	{ 

		if(Strings.isNullOrEmpty(IDs)) { return true; }
		
		if(!IDs.matches("(\\d,?)+")) {
			new CFWLog(logger).severe("The Notification ID's '"+IDs+"' are not a comma separated list of strings.");
			return false;
		}

		boolean success = true;
		for(String id : IDs.split(",")) {
			success &= deleteByID(Integer.parseInt(id));
		}

		return success;
	}
	
	public static boolean deleteAllForCurrentUser() 	{ 
		User user = CFW.Context.Request.getUser();
		
		boolean success = true;
		for(Notification notification : selectAllByUser(user)) {
			success &= deleteByID(notification.id());
		}
		
		return success;
	}
	
	
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static Notification selectByID(int id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, NotificationFields.PK_ID.toString(), id);
	}
	
	public static ArrayList<Notification> selectAllByUser(User user ) {
		return new CFWSQL(new Notification())
			.queryCache()
			.select()
			.where(NotificationFields.FK_ID_USER, user.id())
			.orderbyDesc(NotificationFields.TIMESTAMP.toString())
			.getAsObjectListConvert(Notification.class);
	}
	
	/*******************************************************
	 * 
	 *******************************************************/
	public static String getPartialNotificationListAsJSONForUser(String pageSize, String pageNumber, String filterquery, String sortby, boolean isAscending) {
		return getPartialNotificationListAsJSONForUser(Integer.parseInt(pageSize), Integer.parseInt(pageNumber), filterquery, sortby, isAscending);
	}
	
	/*******************************************************
	 * 
	 *******************************************************/
	public static String getPartialNotificationListAsJSONForUser(int pageSize, int pageNumber, String filterquery, String sortby, boolean isAscending) {	
		
		//-------------------------------------
		// Filter with disabled fulltext search
		int userID = CFW.Context.Request.getUser().id();
		
		CFWSQL query;
		if(Strings.isNullOrEmpty(filterquery)) {
			//-------------------------------------
			// Unfiltered
			query =  new CFWSQL(new Notification())
				//.queryCache(CFWDBJob.class, "getPartialJobListAsJSONForUser-FilterEmpty-sort-"+isAscending)
				.columnSubqueryTotalRecords()
				.select()
				.where(NotificationFields.FK_ID_USER, userID)
				;
		}else {
			//-------------------------------------
			// Filtered
			String wildcardString = "%"+filterquery+"%";
			
			query =  new CFWSQL(new Notification())
					//.queryCache(CFWDBJob.class, "getPartialJobListAsJSONForUser-FilteredSearch-sort-"+isAscending)
					.columnSubqueryTotalRecords()
					.select()
					.where(NotificationFields.FK_ID_USER, userID)
					.and()
						.custom("(")
							.like(NotificationFields.TITLE, wildcardString)
							.or().like(NotificationFields.MESSAGE, wildcardString)
							.or().like(NotificationFields.MESSAGE_TYPE, wildcardString)
						.custom(")")
					;
		}
		
		//-------------------------------------
		// Sorting
		query.orderbyDesc(NotificationFields.TIMESTAMP.toString());

		//-------------------------------------
		// Limit Offset and Execute
		return query.limit(pageSize)
			.offset(pageSize*(pageNumber-1))
			.getAsJSON();
				
	}

	/*******************************************************
	 * 
	 *******************************************************/
	public static String getUnreadCountAndSeverityForCurrentUser() {
		return getUnreadCountAndSeverityByUser(CFW.Context.Request.getUser());
	}
	/*******************************************************
	 * 
	 *******************************************************/
	public static String getUnreadCountAndSeverityByUser(User user) {
		return new CFWSQL(new Notification())
			.queryCache()
			.loadSQLResource(FeatureNotifications.PACKAGE_RESOURCE, "sql_unreadCountAndSeverity.sql", user.id())
			.getAsJSON();
	}
	
	/*******************************************************
	 * 
	 *******************************************************/
	public static String markAsReadForCurrentUser() {
		return new CFWSQL(new Notification())
				.queryCache()
				.loadSQLResource(FeatureNotifications.PACKAGE_RESOURCE, "sql_markAsRead.sql", CFW.Context.Request.getUser().id())
				.getAsJSON();
	}


		
}
