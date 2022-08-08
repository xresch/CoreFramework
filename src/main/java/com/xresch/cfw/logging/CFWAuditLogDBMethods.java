package com.xresch.cfw.logging;

import java.sql.ResultSet;
import java.util.logging.Logger;

import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.logging.CFWAuditLog.CFWAuditLogFields;
import com.xresch.cfw.utils.ResultSetUtils.ResultSetAsJsonReader;

/**************************************************************************************************************
 * @author Reto Scheiwiller
 **************************************************************************************************************/
public class CFWAuditLogDBMethods {
	
	private static Class<CFWAuditLog> cfwObjectClass = CFWAuditLog.class;
	
	public static Logger logger = CFWLog.getLogger(CFWAuditLogDBMethods.class.getName());
		
	//####################################################################################################
	// Preckeck Initialization
	//####################################################################################################
	private static PrecheckHandler prechecksCreateUpdate =  new PrecheckHandler() {
		public boolean doCheck(CFWObject object) {
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
	public static boolean create(CFWAuditLog item) 	{
		
		if(item == null) {
			new CFWLog(logger)
				.warn("The audit log item cannot be null", new Throwable());
			return false;
		}
		
		return new CFWSQL(item)
						.queryCache()
						.insert()
						; 
	}
	
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(CFWAuditLog... items) 	{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, items); }
	public static boolean 	update(CFWAuditLog item) 		{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, item); }
			
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static CFWAuditLog selectByID(String id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWAuditLogFields.PK_ID.toString(), id);
	}
	
	public static CFWAuditLog selectByID(int id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWAuditLogFields.PK_ID.toString(), id);
	}
	
	public static CFWAuditLog selectFirstByName(String name) { 
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, CFWAuditLogFields.ACTION.toString(), name);
	}
	
	
	public static ResultSetAsJsonReader selectByTimerange(long earliestMillis, long latestMillis) { 
		
		return new CFWSQL(new CFWAuditLog())
				.select()
				.where()
					.custom("TIMESTAMP >= DATEADD(MILLISECOND, ?, DATE '1970-01-01')", earliestMillis)
					.custom("AND TIMESTAMP <= DATEADD(MILLISECOND, ?, DATE '1970-01-01')", latestMillis)
				.getAsJSONReader()
				;

	}

		
	
	public static int getCount() {
		
		return new CFWAuditLog()
				.queryCache(CFWAuditLogDBMethods.class, "getCount")
				.selectCount()
				.executeCount();
		
	}
	
	
	
	
	

	


		
}
