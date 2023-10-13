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
import com.xresch.cfw.features.eav.EAVEntity.EAVEntityFields;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023
 * @license MIT-License
 **************************************************************************************************************/
public class CFWDBEAVEntity {

	private static Class<EAVEntity> cfwObjectClass = EAVEntity.class;		
	
	private static final Logger logger = CFWLog.getLogger(CFWDBEAVEntity.class.getName());
	
	
	// Cache of "category + entityName" and entities
	// used to reduce DB calls
	private static Cache<String, EAVEntity> entityCache = CFW.Caching.addCache("CFW EAV Entity", 
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
	private static boolean 	create(EAVEntity item) 		{ 
		
		boolean result = CFWDBDefaultOperations.create(prechecksCreateUpdate, item);
		
		return result;
	}

	/********************************************************************************************
	 * Creates a new entity if it not already exists
	 * @param category the category of the entity
	 * @param entityName the name of the entity to create
	 * @return true if created, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean oneTimeCreate(String category, String entityName) {
		return oneTimeCreate(new EAVEntity(category, entityName));
	}
	
	/********************************************************************************************
	 * Creates a new entity if it not already exists
	 * @param entity with the values that should be inserted. ID should be set by the user.
	 * @return true if created, false otherwise
	 * 
	 ********************************************************************************************/
	public static boolean oneTimeCreate(EAVEntity entity) {
		
		if(entity == null) {
			return false;
		}
		
		boolean result = true; 
		if( !checkExists(entity.category(), entity.name()) ) {
			
			result &= create(entity);
			
		}
		
		return result;
	}
		
	//####################################################################################################
	// UPDATE
	//####################################################################################################
	public static boolean 	update(EAVEntity item) 		{ return CFWDBDefaultOperations.update(prechecksCreateUpdate, item); }
	
	//####################################################################################################
	// DELETE
	//####################################################################################################
	public static boolean 	deleteByID(int id) 					{ return CFWDBDefaultOperations.deleteFirstBy(prechecksDelete, cfwObjectClass, EAVEntityFields.PK_ID.toString(), id); }
	
	//####################################################################################################
	// SELECT
	//####################################################################################################
	public static EAVEntity selectByID(String id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, EAVEntityFields.PK_ID.toString(), id);
	}
	
	public static EAVEntity selectByID(int id ) {
		return CFWDBDefaultOperations.selectFirstBy(cfwObjectClass, EAVEntityFields.PK_ID.toString(), id);
	}
	
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static EAVEntity selecFirstBy(String category, String entityName, boolean createIfNotExists) {
		
		if(createIfNotExists) {
			oneTimeCreate(category, entityName);
		}
		
		EAVEntity entity = null;
		try {
			entity = entityCache.get(category+"-"+entityName, new Callable<EAVEntity>() {

				@Override
				public EAVEntity call() throws Exception {
					
					return (EAVEntity)new CFWSQL(new EAVEntity())
							.select()
							.where(EAVEntityFields.CATEGORY, category)
							.and(EAVEntityFields.NAME, entityName)
							.getFirstAsObject()
							;
				}
				
			});
			
		} catch (ExecutionException e) {
			new CFWLog(logger).severe("Error while reading EAV entity from cache or database.", e);
		}

		return entity;	
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static boolean checkExists(String category, String entityName) {
		
		return 0 < new CFWSQL(new EAVEntity())
				.queryCache()
				.selectCount()
				.where(EAVEntityFields.CATEGORY, category)
				.and(EAVEntityFields.NAME, entityName)
				.executeCount();
		
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public static int getCount() {
		
		return new CFWSQL(new EAVEntity())
				.queryCache()
				.selectCount()
				.executeCount();
		
	}
	
	
}
