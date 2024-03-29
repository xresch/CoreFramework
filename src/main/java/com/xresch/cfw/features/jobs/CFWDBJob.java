package com.xresch.cfw.features.jobs;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWSchedule;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.jobs.CFWJob.CFWJobFields;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * @author Reto Scheiwiller
 **************************************************************************************************************/
public class CFWDBJob {
	
	private static Class<CFWJob> cfwObjectClass = CFWJob.class;
	
	public static Logger logger = CFWLog.getLogger(CFWDBJob.class.getName());
		
	//####################################################################################################
	// Preckeck Initialization
	//####################################################################################################
	private static PrecheckHandler prechecksCreateUpdate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			CFWJob job = (CFWJob)object;
						
			if(job == null || job.jobname().isEmpty()) {
				new CFWLog(logger)
					.warn("Please specify a name for the job.", new Throwable());
				return false;
			}
			
			//---------------------------------
			// Set Schedule fields for sorting
			CFWSchedule schedule = job.schedule();
			Timestamp stamp = new Timestamp(schedule.timeframeStart());
			job.scheduleStart(stamp);
			job.scheduleEnd(schedule.toStringScheduleEnd());
			job.scheduleInterval(schedule.toStringScheduleInterval());
			
			return true;
		}
	};
	
	
	private static PrecheckHandler prechecksDelete =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {	
			return true;
		}
	};
		
	//####################################################################################################
	// CREATE
	//####################################################################################################
	//public static boolean	create(CFWJob... items) 	{ return CFWDBDefaultOperations.create(prechecksCreateUpdate, items); }
	public static boolean 	create(CFWJob item) { 
		Integer id = CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreateUpdate, item);
		if (id != null) {
			CFWJob job = CFW.DB.Jobs.selectByID(id);
			CFW.Registry.Jobs.addJob(job);
			return true;
		}
		
		return false;
	}
	
	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	/**************************************************************
	 * Updates the job in the DB and in the scheduler.
	 * Use this method if the scheduling might have changed,
	 **************************************************************/
	public static boolean update(CFWJob item){ 
		
		if( CFWDBDefaultOperations.update(prechecksCreateUpdate, item)) {
			CFW.Registry.Jobs.updateJob(item);
			return true;
		}
		
		return false;
	}
	
	/**************************************************************
	 *  Updates the last run of the job with the current time and
	 *  the messages.
	 **************************************************************/
	public static boolean updateLastRun(String jobID){ 
		
		CFWJob jobData = new CFWJob();
		jobData.getPrimaryKeyField().setValue(Integer.parseInt(jobID));
		jobData.lastRun(new Timestamp(new Date().getTime()));
		jobData.lastRunMessages(CFW.Context.Request.getAlertsAsJSONArray());
		CFW.Context.Request.clearMessages();
		
		return new CFWSQL(jobData)
				.update(CFWJobFields.LAST_RUN_TIME, CFWJobFields.JSON_LASTRUN_MESSAGES);
	}
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	
	/*******************************************************
	 * 
	 *******************************************************/
	public static boolean deleteByID(int id) { 
		
		CFWJob job = CFW.DB.Jobs.selectByID(id);	
		return executeDelete(job);
	}
	
	/*******************************************************
	 * 
	 *******************************************************/
	public static boolean deleteFirstByCustomInteger(int customInteger) {
		CFWJob job = CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWJobFields.CUSTOM_INTEGER.toString(), customInteger);
		
		return executeDelete(job);
	}
	
	/*******************************************************
	 * 
	 *******************************************************/
	private static boolean executeDelete(CFWJob job) { 
	
		if (CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, cfwObjectClass, CFWJobFields.PK_ID.toString(), job.id()) ) {
			CFW.Registry.Jobs.removeJob(job);
			return true;
		}
		
		return false;
	}

	//####################################################################################################
	// DUPLICATE
	//####################################################################################################
	public static boolean duplicateByID(String id ) {
		CFWJob job = selectByID(id);
		if(job != null) {
			job.id(null);
			job.jobname( job.jobname()+"(Copy)");
			return create(job);
		}
		
		return false;
	}
		
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static CFWJob selectByID(String id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWJobFields.PK_ID.toString(), id);
	}
	
	/*******************************************************
	 * 
	 *******************************************************/
	public static CFWJob selectByID(int id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWJobFields.PK_ID.toString(), id);
	}
	
	/*******************************************************
	 * 
	 *******************************************************/
	public static CFWJob selectFirstByCustomInteger(int customInteger) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWJobFields.CUSTOM_INTEGER.toString(), customInteger);
	}
	
	/*******************************************************
	 * 
	 *******************************************************/
	public static ArrayList<CFWObject> getEnabledJobs() {
		
		return new CFWSQL(new CFWJob())
				.queryCache()
				.select()
				.where(CFWJobFields.IS_ENABLED, true)
				.getAsObjectList();
		
	}
	
	/*******************************************************
	 * 
	 *******************************************************/
	public static String getJobListAsJSON() {
		
		return new CFWSQL(new CFWJob())
				.queryCache()
				.select()
				.getAsJSON();
		
	}
	
	/*******************************************************
	 * 
	 *******************************************************/
	public static String getPartialJobListAsJSONForUser(String pageSize, String pageNumber, String filterquery, String sortby, boolean isAscending) {
		return getPartialJobListAsJSONForUser(Integer.parseInt(pageSize), Integer.parseInt(pageNumber), filterquery, sortby, isAscending);
	}
	
	/*******************************************************
	 * 
	 *******************************************************/
	public static String getPartialJobListAsJSONForUser(int pageSize, int pageNumber, String filterquery, String sortby, boolean isAscending) {	
		
		//-------------------------------------
		// Filter with fulltext search
		// Enabled by CFWObject.enableFulltextSearch()
		// on the CFWJob Object
		int userID = CFW.Context.Request.getUser().id();
		
		CFWSQL query;
		if(Strings.isNullOrEmpty(filterquery)) {
			//-------------------------------------
			// Unfiltered
			query =  new CFWSQL(new CFWJob())
				//.queryCache(CFWDBJob.class, "getPartialJobListAsJSONForUser-FilterEmpty-sort-"+isAscending)
				.columnSubqueryTotalRecords()
				.select()
				.where(CFWJobFields.FK_ID_USER, userID)
				;
		}else {
			//-------------------------------------
			// Filter with fulltext search
			// Enabled by CFWObject.enableFulltextSearch()
			// on the Person Object
			String wildcardString = "%"+filterquery+"%";
			
			query =  new CFWSQL(new CFWJob())
					//.queryCache(CFWDBJob.class, "getPartialJobListAsJSONForUser-FilteredSearch-sort-"+isAscending)
					.columnSubqueryTotalRecords()
					.select()
					.where(CFWJobFields.FK_ID_USER, userID)
					.and()
						.custom("(")
							.like(CFWJobFields.JOB_NAME, wildcardString)
							.or().like(CFWJobFields.DESCRIPTION, wildcardString)
							.or().like(CFWJobFields.TASK_NAME, wildcardString)
							.or().like(CFWJobFields.JSON_PROPERTIES, wildcardString)
						.custom(")")
					;
		}
		
		//-------------------------------------
		// Sorting
		if(Strings.isNullOrEmpty(sortby)) {
			sortby = CFWJobFields.JOB_NAME.toString();
		}
		
		if(isAscending) {
			query.orderby(sortby);
		}else {
			query.orderbyDesc(sortby);
		}
		
		
		//-------------------------------------
		// Limit Offset and Execute
		return query.limit(pageSize)
			.offset(pageSize*(pageNumber-1))
			.getAsJSON();
				
	}
	
	/*******************************************************
	 * 
	 *******************************************************/
	public static String getPartialJobListAsJSONForAdmin(String pageSize, String pageNumber, String filterquery, String sortby, boolean isAscending) {
		return getPartialJobListAsJSONForAdmin(Integer.parseInt(pageSize), Integer.parseInt(pageNumber), filterquery, sortby, isAscending);
	}
	
	/*******************************************************
	 * 
	 *******************************************************/
	public static String getPartialJobListAsJSONForAdmin(int pageSize, int pageNumber, String searchString, String sortby, boolean isAscending) {	
		
		//-------------------------------------
		// Filter with fulltext search
		// Enabled by CFWObject.enableFulltextSearch()
		// on the CFWJob Object
		
		CFWSQL query;
		if(Strings.isNullOrEmpty(searchString)) {
			//-------------------------------------
			// Unfiltered
			query = new CFWSQL(new CFWJob())
					// do not cache
					//.queryCache(CFWDBJob.class, "getPartialJobListAsJSONForAdmin-SearchEmpty-sort-"+isAscending)
					.columnSubquery("OWNER", CFW.DB.Users.USERNAME_SUBQUERY)
					.columnSubqueryTotalRecords()
					.select();
			
		}else {
			
			//-------------------------------------
			// Filter with fulltext search
			// Enabled by CFWObject.enableFulltextSearch()
			// on the Person Object
			String wildcardString = "%"+searchString+"%";
			
			query = new CFWSQL(new CFWJob())
					//do not cache
					//.queryCache(CFWDBJob.class, "getPartialJobListAsJSONForAdmin-SearchQuery-sort-"+isAscending)
					.columnSubquery("OWNER", CFW.DB.Users.USERNAME_SUBQUERY)
					.columnSubqueryTotalRecords()
					.select()
					.whereLike(CFWJobFields.JOB_NAME, wildcardString)
						.or().like(CFWJobFields.DESCRIPTION, wildcardString)
						.or().like(CFWJobFields.TASK_NAME, wildcardString)
						.or().like(CFWJobFields.JSON_PROPERTIES, wildcardString)
						.or().custom("LOWER(("+CFW.DB.Users.USERNAME_SUBQUERY+")) LIKE LOWER(?)", wildcardString);
		}
		
		//-------------------------------------
		// Sorting
		if(Strings.isNullOrEmpty(sortby)) {
			sortby = CFWJobFields.JOB_NAME.toString();
		}
		
		if(isAscending) {
			query.orderby(sortby);
		}else {
			query.orderbyDesc(sortby);
		}
		
		//-------------------------------------
		// Limit Offset and Execute
		return query.limit(pageSize)
			.offset(pageSize*(pageNumber-1))
			.getAsJSON();
				
	}
	
	/*******************************************************
	 * 
	 *******************************************************/
	public static boolean checkIsCurrentUserOwner(String jobID) {
		
		return 0 < new CFWSQL(new CFWJob())
				.queryCache()
				.selectCount()
				.where(CFWJobFields.FK_ID_USER, CFW.Context.Request.getUser().id())
				.and(CFWJobFields.PK_ID, jobID.trim())
				.executeCount();
		
	}
	
	/*******************************************************
	 * 
	 *******************************************************/
	public static int getCount() {
		
		return new CFWSQL(new CFWJob())
				.queryCache()
				.selectCount()
				.executeCount();
		
	}
	
	/*******************************************************
	 * Counts number of jobs that have the given value 
	 * defined in the column CUSTOM_INTEGER
	 *******************************************************/
	public static int getCountByCustomInteger(int customInteger) {
		
		return new CFWSQL(new CFWJob())
				.queryCache()
				.selectCount()
				.where(CFWJobFields.CUSTOM_INTEGER, customInteger)
				.executeCount();
		
	}
	
	
	
	
	

	


		
}
