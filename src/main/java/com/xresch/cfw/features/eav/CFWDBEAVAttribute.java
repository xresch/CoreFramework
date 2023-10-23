package com.xresch.cfw.features.eav;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWDBDefaultOperations;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.PrecheckHandler;
import com.xresch.cfw.features.eav.EAVAttribute.EAVAttributeFields;
import com.xresch.cfw.features.eav.EAVAttribute.EAVAttributeFields;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBEAVAttribute {

	private static Class<EAVAttribute> cfwObjectClass = EAVAttribute.class;		
	
	private static final Logger logger = CFWLog.getLogger(CFWDBEAVAttribute.class.getName());
	
	// Cache of "entityID + attributeName" and attributes
	// used to reduce DB calls
	private static Cache<String, EAVAttribute> attributeCacheByName = CFW.Caching.addCache("CFW EAV Attribute(Name)", 
			CacheBuilder.newBuilder()
				.initialCapacity(50)
				.maximumSize(5000)
				.expireAfterAccess(1, TimeUnit.HOURS)
		);
	
	// Cache of integer and attributes
	// used to reduce DB calls
	private static Cache<Integer, EAVAttribute> attributeCacheByID = CFW.Caching.addCache("CFW EAV Attribute(ID)", 
			CacheBuilder.newBuilder()
				.initialCapacity(50)
				.maximumSize(5000)
				.expireAfterAccess(1, TimeUnit.HOURS)
		);
	
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
	private static boolean 	create(EAVAttribute item) 		{ 
		
		boolean result = CFWDBDefaultOperations.create(prechecksCreateUpdate, item);
		
		return result;
	}

	/********************************************************************************************
	 * Creates a new attribute if it not already exists
	 * @param category the category of the attribute
	 * @param attributeName the name of the attribute to create
	 * @return true if created, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean oneTimeCreate(int entityID, String attributeName) {
		return oneTimeCreate(new EAVAttribute(entityID, attributeName));
	}
	
	/********************************************************************************************
	 * Creates a new attribute if it not already exists
	 * @param attribute with the values that should be inserted. ID should be set by the user.
	 * @return true if created, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean oneTimeCreate(EAVAttribute attribute) {
		
		if(attribute == null) {
			return false;
		}
		
		boolean result = true; 
		if( !checkExists(attribute.foreignKeyEntity(), attribute.name()) ) {
			
			result &= create(attribute);
			
		}
		
		return result;
	}
		
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(EAVAttribute item) 		{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, item); }
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean 	deleteByID(int id) 					{ return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, cfwObjectClass, EAVAttributeFields.PK_ID.toString(), id); }
	
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static EAVAttribute selectByID(String id ) {
		return selectByID(Integer.parseInt(id));
	}
	
	public static EAVAttribute selectByID(int id ) {
		
		EAVAttribute attribute = null;
		try {
			attribute = attributeCacheByID.get(id, new Callable<EAVAttribute>() {

				@Override
				public EAVAttribute call() throws Exception {
					
					return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, EAVAttributeFields.PK_ID.toString(), id);
				}
				
			});
			
		} catch (ExecutionException e) {
			new CFWLog(logger).severe("Error while reading EAV attribute from cache or database.", e);
		}

		return attribute;
		
	}
	
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static EAVAttribute selecFirstBy(int entityID, String attributeName, boolean createIfNotExists) {
		
		if(createIfNotExists) {
			oneTimeCreate(entityID, attributeName);
		}else if(!checkExists(entityID, attributeName)) {
			return null;
		}
		
		EAVAttribute attribute = null;
		try {
			attribute = attributeCacheByName.get(entityID+"-"+attributeName, new Callable<EAVAttribute>() {

				@Override
				public EAVAttribute call() throws Exception {
					
					return (EAVAttribute)new CFWSQL(new EAVAttribute())
							.select()
							.where(EAVAttributeFields.FK_ID_ENTITY, entityID)
							.and(EAVAttributeFields.NAME, attributeName)
							.getFirstAsObject()
							;
				}
				
			});
			
		} catch (ExecutionException e) {
			new CFWLog(logger).severe("Error while reading EAV attribute from cache or database.", e);
		}

		return attribute;	
		

				
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static boolean checkExists(int entityID, String attributeName) {
		return checkExists(""+entityID, attributeName);
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static boolean checkExists(String entityID, String attributeName) {
		
		boolean result = 0 < new CFWSQL(new EAVAttribute())
				.queryCache()
				.selectCount()
				.where(EAVAttributeFields.FK_ID_ENTITY, entityID)
				.and(EAVAttributeFields.NAME, attributeName)
				.executeCount();
				
		return result;
		
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static int getCount() {
		
		return new CFWSQL(new EAVAttribute())
				.queryCache()
				.selectCount()
				.executeCount();
		
	}
	

	
		
}
