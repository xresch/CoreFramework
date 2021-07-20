package com.xresch.cfw.features.jobs;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
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
					.warn("Please specify a firstname for the job.", new Throwable());
				return false;
			}

			return true;
		}
	};
	
	
	private static PrecheckHandler prechecksDelete =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			CFWJob job = (CFWJob)object;
			
//			if(job != null && job.likesTiramisu() == true) {
//				new CFWLog(logger)
//				.severe("The CFWJob '"+job.firstname()+"' cannot be deleted as people that like tiramisu will prevail for all eternity!", new Throwable());
//				return false;
//			}
//			
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
			CFW.Registry.Jobs.startJob(job);
			return true;
		}
		
		return false;
	}
	
	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(CFWJob item){ 
		
		if( CFWDBDefaultOperations.update(prechecksCreateUpdate, item)) {
			CFW.Registry.Jobs.updateJob(item);
			return true;
		}
		
		return false;
	}
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean deleteByID(int id) { 
		
		CFWJob job = CFW.DB.Jobs.selectByID(id);
		
		if (CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, cfwObjectClass, CFWJobFields.PK_ID.toString(), id) ) {
			CFW.Registry.Jobs.stopJob(job);
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
	public static CFWJob selectFirstByName(String name) { 
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWJobFields.JOB_NAME.toString(), name);
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
	public static String getPartialJobListAsJSONForUser(String pageSize, String pageNumber, String filterquery) {
		return getPartialJobListAsJSONForUser(Integer.parseInt(pageSize), Integer.parseInt(pageNumber), filterquery);
	}
	
	/*******************************************************
	 * 
	 *******************************************************/
	public static String getPartialJobListAsJSONForUser(int pageSize, int pageNumber, String filterquery) {	
		
		//-------------------------------------
		// Filter with fulltext search
		// Enabled by CFWObject.enableFulltextSearch()
		// on the CFWJob Object
		int userID = CFW.Context.Request.getUser().id();
		
		if(Strings.isNullOrEmpty(filterquery)) {
			//-------------------------------------
			// Unfiltered
			return new CFWSQL(new CFWJob())
				.queryCache()
				.columnSubquery("TOTAL_RECORDS", "COUNT(*) OVER()")
				.select()
				.where(CFWJobFields.FK_ID_USER, userID)
				.limit(pageSize)
				.offset(pageSize*(pageNumber-1))
				.getAsJSON();
		}else {
			//-------------------------------------
			// Filter with fulltext search
			// Enabled by CFWObject.enableFulltextSearch()
			// on the Person Object
			return new CFWSQL(new CFWJob())
					.queryCache()
					.select()
					.fulltextSearchLucene()
						.custom(filterquery)
						.build(pageSize, pageNumber)
					.where(CFWJobFields.FK_ID_USER, userID)
					.getAsJSON();
		}
				
	}
	
	/*******************************************************
	 * 
	 *******************************************************/
	public static String getPartialJobListAsJSONForAdmin(String pageSize, String pageNumber, String filterquery) {
		return getPartialJobListAsJSONForAdmin(Integer.parseInt(pageSize), Integer.parseInt(pageNumber), filterquery);
	}
	
	/*******************************************************
	 * 
	 *******************************************************/
	public static String getPartialJobListAsJSONForAdmin(int pageSize, int pageNumber, String searchString) {	
		
		//-------------------------------------
		// Filter with fulltext search
		// Enabled by CFWObject.enableFulltextSearch()
		// on the CFWJob Object

		
		if(Strings.isNullOrEmpty(searchString)) {
				//-------------------------------------
				// Unfiltered
				return new CFWSQL(new CFWJob())
					.queryCache(CFWDBJob.class, "getPartialJobListAsJSONForAdmin-SearchEmpty")
					.columnSubquery("OWNER", "SELECT USERNAME FROM CFW_USER WHERE PK_ID = FK_ID_USER")
					.columnSubquery("TOTAL_RECORDS", "COUNT(*) OVER()")
					.select()
					.limit(pageSize)
					.offset(pageSize*(pageNumber-1))
					.getAsJSON();

			}else {
				
				//-------------------------------------
				// Filter with fulltext search
				// Enabled by CFWObject.enableFulltextSearch()
				// on the Person Object
				String ownerSubquery = "SELECT USERNAME FROM CFW_USER WHERE PK_ID = FK_ID_USER";
				CFWSQL customFilter = new CFWSQL(new CFWJob())
						.queryCache(CFWDBJob.class, "getPartialJobListAsJSONForAdmin-SearchQuery")
						.columnSubquery("OWNER", ownerSubquery)
						.columnSubquery("TOTAL_RECORDS", "COUNT(*) OVER()")
						.select()
						.whereLike(CFWJobFields.JOB_NAME, "%"+searchString+"%")
						.or().like(CFWJobFields.DESCRIPTION, "%"+searchString+"%")
						.or().like(CFWJobFields.TASK_NAME, "%"+searchString+"%")
						.or().like(CFWJobFields.JSON_PROPERTIES, "%"+searchString+"%")
						.or().like("("+ownerSubquery+")", "%"+searchString+"%");
				
				
				return customFilter.limit(pageSize)
					.offset(pageSize*(pageNumber-1))
					.getAsJSON();
			}
				
	}
	
	/*******************************************************
	 * 
	 *******************************************************/
	public static int getCount() {
		
		return new CFWJob()
				.queryCache(CFWDBJob.class, "getCount")
				.selectCount()
				.getCount();
		
	}
	
	
	
	
	

	


		
}
