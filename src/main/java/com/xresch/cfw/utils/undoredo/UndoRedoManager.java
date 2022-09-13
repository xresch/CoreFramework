package com.xresch.cfw.utils.undoredo;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.logging.CFWLog;

/**************************************************************************************************************
 * Class used to manage and handle Undo/Redo Operations
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class UndoRedoManager<T> {
	
	private static Logger logger = CFWLog.getLogger(UndoRedoManager.class.getName());
	
	private Cache<String, UndoRedoHistory<T>> historyCache;
	
	/*********************************************************************************
	 * 
	 * @param cacheName
	 * @param cacheMaxSize
	 *********************************************************************************/
	public UndoRedoManager(String cacheName, int cacheMaxSize) {
		
		historyCache = CFW.Caching.addCache("UndoRedoManager:"+cacheName, 
				CacheBuilder.newBuilder()
					.initialCapacity(1)
					.maximumSize(cacheMaxSize)
					.expireAfterAccess(12, TimeUnit.HOURS)
			);
		
	}
	
	/*********************************************************************************
	 * Returns the History for the given ID from the cache.
	 * If there is nothing in cache creates a new one.
	 * 
	 * @param historyID ID to identify the cache. This is normally a combination of 
	 *        UserID plus the ID of the item the user is working on.
	 *        
	 *********************************************************************************/
	public UndoRedoHistory<T> getHistory(String historyID) throws ExecutionException {
		
		return historyCache.get(historyID, new Callable<UndoRedoHistory<T>>() {

			@Override
			public UndoRedoHistory<T> call() throws Exception {
				return new UndoRedoHistory<T>(30);
			}

		});

	}
	
	

	
	
	
		
}
