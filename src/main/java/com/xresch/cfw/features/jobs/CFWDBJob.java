package com.xresch.cfw.features.jobs;

import java.util.logging.Logger;

import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.jobs.CFWJob.JobFields;
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
	public static boolean	create(CFWJob... items) 	{ return CFWDBDefaultOperations.create(prechecksCreateUpdate, items); }
	public static boolean 	create(CFWJob item) 		{ return CFWDBDefaultOperations.create(prechecksCreateUpdate, item);}
	public static Integer 	createGetPrimaryKey(CFWJob item) { return CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreateUpdate, item);}
	
	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(CFWJob... items) 	{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, items); }
	public static boolean 	update(CFWJob item) 		{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, item); }
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean 	deleteByID(int id) 					{ return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, cfwObjectClass, JobFields.PK_ID.toString(), id); }
	public static boolean 	deleteMultipleByID(String itemIDs) 	{ return CFWDBDefaultOperations.deleteMultipleByID(prechecksDelete, cfwObjectClass, itemIDs); }
	
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
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, JobFields.PK_ID.toString(), id);
	}
	
	public static CFWJob selectByID(int id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, JobFields.PK_ID.toString(), id);
	}
	
	public static CFWJob selectFirstByName(String name) { 
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, JobFields.JOB_NAME.toString(), name);
	}
	
	
	
	public static String getJobListAsJSON() {
		
		return new CFWSQL(new CFWJob())
				.queryCache()
				.select()
				.getAsJSON();
		
	}
	
	public static String getPartialJobListAsJSONForAdmin(String pageSize, String pageNumber, String filterquery) {
		return getPartialJobListAsJSONForAdmin(Integer.parseInt(pageSize), Integer.parseInt(pageNumber), filterquery);
	}
	
	
	public static String getPartialJobListAsJSONForAdmin(int pageSize, int pageNumber, String filterquery) {	
		
		//-------------------------------------
		// Filter with fulltext search
		// Enabled by CFWObject.enableFulltextSearch()
		// on the CFWJob Object
		return new CFWSQL(new CFWJob())
				.fulltextSearch(filterquery, pageSize, pageNumber)
				.getAsJSON();
				
	}
	
	public static int getCount() {
		
		return new CFWJob()
				.queryCache(CFWDBJob.class, "getCount")
				.selectCount()
				.getCount();
		
	}
	
	
	
	
	

	


		
}
