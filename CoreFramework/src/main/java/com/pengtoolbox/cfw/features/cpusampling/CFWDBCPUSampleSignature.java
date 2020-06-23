package com.pengtoolbox.cfw.features.cpusampling;

import java.util.HashMap;
import java.util.logging.Logger;

import com.pengtoolbox.cfw.features.cpusampling.CPUSampleSignature.CPUSampleSignatureFields;
import com.pengtoolbox.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class CFWDBCPUSampleSignature {
	
	public static Logger logger = CFWLog.getLogger(CFWDBCPUSampleSignature.class.getName());
		
	/********************************************************************************************
	 * Creates a new signature in the DB and returns it's primary key.
	 * @param signature to create
	 * @return id or null if not successful
	 * 
	 ********************************************************************************************/
	public static Integer insertGetID(CPUSampleSignature signature) {
		
		if(signature == null) {
			new CFWLog(logger)
				.method("create")
				.warn("The signature cannot be null");
			return null;
		}
		
		if(signature.signature() == null || signature.signature().isEmpty()) {
			new CFWLog(logger)
				.method("create")
				.warn("Please specify a name for the signature to create.");
			return null;
		}
		
		if(checkStatsMethodSignatureExists(signature)) {
			new CFWLog(logger)
				.method("create")
				.warn("The signature '"+signature.signature()+"' cannot be created as a signature with this name already exists.");
			return null;
		}
		
		return signature
				.queryCache(CFWDBCPUSampleSignature.class, "create")
				.insertGetPrimaryKey();
	}
	
	/***************************************************************
	 * Select a signature by it's name.
	 * @param id of the signature
	 * @return Returns a signature or null if not found or in case of exception.
	 ****************************************************************/
	public static CPUSampleSignature selectByName(String name) {
		
		return (CPUSampleSignature)new CPUSampleSignature()
				.queryCache(CFWDBCPUSampleSignature.class, "selectByName")
				.select()
				.where(CPUSampleSignatureFields.SIGNATURE.toString(), name)
				.getFirstObject();

	}
	
	/***************************************************************
	 * Select a signature by it's ID.
	 * @param id of the signature
	 * @return Returns a signature or null if not found or in case of exception.
	 ****************************************************************/
	public static CPUSampleSignature selectByID(int id ) {

		return (CPUSampleSignature)new CPUSampleSignature()
				.queryCache(CFWDBCPUSampleSignature.class, "selectByID")
				.select()
				.where(CPUSampleSignatureFields.PK_ID.toString(), id)
				.getFirstObject();
		
	}
	
	/***************************************************************
	 * Select a signature by it's ID and return it as JSON string.
	 * @param id of the signature
	 * @return Returns a signature or null if not found or in case of exception.
	 ****************************************************************/
	public static String getSignatureAsJSON(String id) {
		
		return new CPUSampleSignature()
				.queryCache(CFWDBCPUSampleSignature.class, "getSignatureAsJSON")
				.select()
				.where(CPUSampleSignatureFields.PK_ID.toString(), Integer.parseInt(id))
				.getAsJSON();
		
	}
	
	/***************************************************************
	 * Select a signature by it's ID and return it as JSON string.
	 * @param id of the signature
	 * @return Returns a signature or null if not found or in case of exception.
	 ****************************************************************/
	public static HashMap<Object, Object> getSignaturesAsKeyValueMap() {
		
		return new CPUSampleSignature()
				.queryCache(CFWDBCPUSampleSignature.class, "getSignaturesAsKeyValueMap")
				.select()
				.getKeyValueMap(CPUSampleSignatureFields.SIGNATURE.toString(), 
								CPUSampleSignatureFields.PK_ID.toString());
		
	}

	
	/***************************************************************
	 * Return a list of all user signatures as json string.
	 * 
	 * @return Returns a result set with all users or null.
	 ****************************************************************/
	public static String getSignatureListAsJSON() {
		return new CPUSampleSignature()
				.queryCache(CFWDBCPUSampleSignature.class, "getSignatureListAsJSON")
				.select()
				.orderby(CPUSampleSignatureFields.SIGNATURE.toString())
				.getAsJSON();
	}
	
		
	/****************************************************************
	 * Deletes the signature by id.
	 * @param id of the user
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static boolean deleteByID(int id) {
				
		return new CPUSampleSignature()
				.queryCache(CFWDBCPUSampleSignature.class, "deleteByID")
				.delete()
				.where(CPUSampleSignatureFields.PK_ID.toString(), id)
				.executeDelete();
					
	}	
	
	/****************************************************************
	 * Deletes the signature by id.
	 * @param id of the user
	 * @return true if successful, false otherwise.
	 ****************************************************************/
	public static boolean deleteByName(String name) {
		
		CPUSampleSignature signature = selectByName(name);
		
		return new CPUSampleSignature()
				.queryCache(CFWDBCPUSampleSignature.class, "deleteByName")
				.delete()
				.where(CPUSampleSignatureFields.SIGNATURE.toString(), name)
				.executeDelete();
					
	}
	
	
	/****************************************************************
	 * Check if the signature exists by name.
	 * 
	 * @param signature to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkStatsMethodSignatureExists(CPUSampleSignature signature) {
		if(signature != null) {
			return checkStatsMethodSignatureExists(signature.signature());
		}
		return false;
	}
	
	/****************************************************************
	 * Check if the signature exists by name.
	 * 
	 * @param signaturename to check
	 * @return true if exists, false otherwise or in case of exception.
	 ****************************************************************/
	public static boolean checkStatsMethodSignatureExists(String signatureName) {
		
		int count = new CPUSampleSignature()
				.queryCache(CFWDBCPUSampleSignature.class, "checkStatsMethodSignatureExists")
				.selectCount()
				.where(CPUSampleSignatureFields.SIGNATURE.toString(), signatureName)
				.getCount();
		
		return (count > 0);
		
	}
	
}
