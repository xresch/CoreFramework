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
import com.xresch.cfw.features.eav.EAVValue.EAVValueFields;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBEAVValue {

	private static Class<EAVValue> cfwObjectClass = EAVValue.class;		
	
	private static final Logger logger = CFWLog.getLogger(CFWDBEAVValue.class.getName());
	
	// Cache of "entityID-attributeID-value" and values
	// used to reduce DB calls
	private static Cache<String, EAVValue> valueCacheByName = CFW.Caching.addCache("CFW EAV Value(Name)", 
			CacheBuilder.newBuilder()
				.initialCapacity(50)
				.maximumSize(50000)
				.expireAfterAccess(1, TimeUnit.HOURS)
		);
	
	// Cache of id and values
	// used to reduce DB calls
	private static Cache<Integer, EAVValue> valueCacheByID = CFW.Caching.addCache("CFW EAV Value(ID)", 
			CacheBuilder.newBuilder()
				.initialCapacity(50)
				.maximumSize(50000)
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
	private static Integer 	createGetPrimaryKey(EAVValue item) 		{ 
		
		return CFWDBDefaultOperations.createGetPrimaryKey(prechecksCreateUpdate, item);

	}

	/********************************************************************************************
	 * Creates a new attribute if it not already exists
	 * @param category the category of the attribute
	 * @param attributeName the name of the attribute to create
	 * @return id if created, null otherwise
	 * 
	 ********************************************************************************************/
	public static Integer oneTimeCreate(int entityID, int attributeID, String value) {
		return oneTimeCreate(new EAVValue(entityID, attributeID, value));
	}
	
	/********************************************************************************************
	 * Creates a new value if it not already exists
	 * @param value with the values that should be inserted. ID should be set by the user.
	 * @return id if created, null otherwise
	 * 
	 ********************************************************************************************/
	public static Integer oneTimeCreate(EAVValue value) {
		
		if(value == null) {
			return null;
		}
		
		boolean result = true; 
		if( !checkExists(value.foreignKeyEntity(), value.foreignKeyAttribute(), value.value()) ) {
			
			return createGetPrimaryKey(value);
			
		}
		
		return null;
	}
		
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(EAVValue item) 		{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, item); }
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean 	deleteByID(int id) 					{ return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, cfwObjectClass, EAVValueFields.PK_ID.toString(), id); }
	
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static EAVValue selectByID(String id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, EAVValueFields.PK_ID.toString(), id);
	}
	
	public static EAVValue selectByID(int id) {
		EAVValue valueObject = null;
		try {
			valueObject = valueCacheByID.get(id, new Callable<EAVValue>() {
				@Override
				public EAVValue call() throws Exception {
					return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, EAVValueFields.PK_ID.toString(), id);
				}
				
			});
			
		} catch (ExecutionException e) {
			new CFWLog(logger).severe("Error while reading EAV attribute from cache or database.", e);
		}

		return valueObject;
		
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static EAVValue selecFirstBy(int entityID, int attributeID, boolean createIfNotExists) {
		
		if(createIfNotExists) {
			oneTimeCreate(entityID, attributeID, null);
		}
		
		return (EAVValue)new CFWSQL(new EAVValue())
					.select()
					.where(EAVValueFields.FK_ID_ENTITY, entityID)
					.and(EAVValueFields.FK_ID_ATTR, attributeID)
					.getFirstAsObject()
					;
				
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static EAVValue selecFirstBy(int entityID, int attributeID, String value, boolean createIfNotExists) {
		

		if(createIfNotExists) {
			oneTimeCreate(entityID, attributeID, value);
		}else if(!checkExists(entityID, attributeID, value)) {
			return null;
		}
		
		
		String cacheKey = entityID+"-"+attributeID+"-"+value;
		EAVValue valueObject = null;
		try {

			valueObject = valueCacheByName.get(cacheKey, new Callable<EAVValue>() {

				@Override
				public EAVValue call() throws Exception {
					
					if(value != null) {
						return (EAVValue)new CFWSQL(new EAVValue())
								.select()
								.where(EAVValueFields.FK_ID_ENTITY, entityID)
								.and(EAVValueFields.FK_ID_ATTR, attributeID)
								.and(EAVValueFields.VALUE, value)
								.getFirstAsObject()
								;
					}else {
						return (EAVValue)new CFWSQL(new EAVValue())
								.select()
								.where(EAVValueFields.FK_ID_ENTITY, entityID)
								.and(EAVValueFields.FK_ID_ATTR, attributeID)
								.isNull(EAVValueFields.VALUE)
								.getFirstAsObject()
								;
					}
				}
				
			});
			
		} catch (ExecutionException e) {
			new CFWLog(logger).severe("Error while reading EAV attribute from cache or database.", e);
		}

		return valueObject;
				
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static boolean checkExists(int entityID, int attributeID, String value) {
		return checkExists(""+entityID, ""+attributeID, value);
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static boolean checkExists(String entityID, String attributeID, String value) {
		
		if(value != null) {
			return 0 < new CFWSQL(new EAVValue())
				.queryCache(cfwObjectClass, "-isNotNull")
				.selectCount()
				.where(EAVValueFields.FK_ID_ENTITY, entityID)
				.and(EAVValueFields.FK_ID_ATTR, attributeID)
				.and(EAVValueFields.VALUE, value)
				.executeCount()
				;
				
		}else {
			return 0 < new CFWSQL(new EAVValue())
					.queryCache(cfwObjectClass, "-isNull")
					.selectCount()
					.where(EAVValueFields.FK_ID_ENTITY, entityID)
					.and(EAVValueFields.FK_ID_ATTR, attributeID)
					.isNull(EAVValueFields.VALUE)
					.executeCount()
					;
		}
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static int getCount() {
		
		return new CFWSQL(new EAVValue())
				.queryCache()
				.selectCount()
				.executeCount();
		
	}
	

	
		
}
