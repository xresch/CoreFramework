package com.xresch.cfw.features.jobs;

import java.util.logging.Logger;

import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.jobs.Job.JobFields;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * @author Reto Scheiwiller
 **************************************************************************************************************/
public class JobDBMethods {
	
	private static Class<Job> cfwObjectClass = Job.class;
	
	public static Logger logger = CFWLog.getLogger(JobDBMethods.class.getName());
		
	//####################################################################################################
	// Preckeck Initialization
	//####################################################################################################
	private static PrecheckHandler prechecksCreateUpdate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			
			Job job = (Job)object;
			
			if(job == null || job.name().isEmpty()) {
				new CFWLog(logger)
					.warn("Please specify a firstname for the job.", new Throwable());
				return false;
			}

			return true;
		}
	};
	
	
	private static PrecheckHandler prechecksDelete =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
			Job job = (Job)object;
			
//			if(job != null && job.likesTiramisu() == true) {
//				new CFWLog(logger)
//				.severe("The Job '"+job.firstname()+"' cannot be deleted as people that like tiramisu will prevail for all eternity!", new Throwable());
//				return false;
//			}
//			
			return true;
		}
	};
		
	//####################################################################################################
	// CREATE
	//####################################################################################################
	public static boolean	create(Job... items) 	{ return CFWDBDefaultOperations.create(prechecksCreateUpdate, items); }
	public static boolean 	create(Job item) 		{ return CFWDBDefaultOperations.create(prechecksCreateUpdate, item);}
	public static Integer 	createGetPrimaryKey(Job item) { return CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreateUpdate, item);}
	
	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(Job... items) 	{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, items); }
	public static boolean 	update(Job item) 		{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, item); }
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean 	deleteByID(int id) 					{ return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, cfwObjectClass, JobFields.PK_ID.toString(), id); }
	public static boolean 	deleteMultipleByID(String itemIDs) 	{ return CFWDBDefaultOperations.deleteMultipleByID(prechecksDelete, cfwObjectClass, itemIDs); }
	
	//####################################################################################################
	// DUPLICATE
	//####################################################################################################
	public static boolean duplicateByID(String id ) {
		Job job = selectByID(id);
		if(job != null) {
			job.id(null);
			return create(job);
		}
		
		return false;
	}
		
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static Job selectByID(String id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, JobFields.PK_ID.toString(), id);
	}
	
	public static Job selectByID(int id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, JobFields.PK_ID.toString(), id);
	}
	
	public static Job selectFirstByName(String name) { 
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, JobFields.NAME.toString(), name);
	}
	
	
	
	public static String getJobListAsJSON() {
		
		return new CFWSQL(new Job())
				.queryCache()
				.select()
				.getAsJSON();
		
	}
	
	public static String getPartialJobListAsJSON(String pageSize, String pageNumber, String filterquery) {
		return getPartialJobListAsJSON(Integer.parseInt(pageSize), Integer.parseInt(pageNumber), filterquery);
	}
	
	
	public static String getPartialJobListAsJSON(int pageSize, int pageNumber, String filterquery) {	
		
		//-------------------------------------
		// Filter with fulltext search
		// Enabled by CFWObject.enableFulltextSearch()
		// on the Job Object
		return new CFWSQL(new Job())
				.fulltextSearch(filterquery, pageSize, pageNumber)
				.getAsJSON();
				
	}
	
	public static int getCount() {
		
		return new Job()
				.queryCache(JobDBMethods.class, "getCount")
				.selectCount()
				.getCount();
		
	}
	
	
	
	
	

	


		
}
