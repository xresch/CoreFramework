package com.xresch.cfw.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
		
		historyCache = CFW.Caching.addCache("UndoRedo:"+cacheName, 
				CacheBuilder.newBuilder()
					.initialCapacity(1)
					.maximumSize(cacheMaxSize)
					.expireAfterAccess(24, TimeUnit.HOURS)
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
	
	
	//##################################################################################################
	// CLASS: UndoRedoHistory
	//##################################################################################################
	
	/*********************************************************************************
	 * 
	 *********************************************************************************/
	public class UndoRedoHistory<Y>{
		
		private int historyPointer = 0;
		
		private LimitedSizeStack< ArrayList<UndoRedoOperation<Y>> > operationBundleStack;
		
		ArrayList<UndoRedoOperation<Y>> currentOperationBundle = null;
		
		/**************************************************************
		 * 
		 **************************************************************/
		public UndoRedoHistory(int maxhistoryStates) {
			 operationBundleStack = new LimitedSizeStack< ArrayList<UndoRedoOperation<Y>> >(maxhistoryStates);
		}
		
		/**************************************************************
		 * Starts an operation bundle.
		 * All operations added to the history will be packed into a 
		 * single bundle until operationBundleEnd() is called.
		 * All the operations in a bundle will be undoed/redoed with
		 * a single execution of executeRedo()/executeUndo().
		 * 
		 **************************************************************/
		public void operationBundleStart() {
			if(currentOperationBundle == null) {
				currentOperationBundle = new ArrayList<UndoRedoOperation<Y>>();
			}
		}
		
		/**************************************************************
		 * 
		 **************************************************************/
		public boolean operationBundleIsStarted() {
			return currentOperationBundle != null;
		}
		
		/**************************************************************
		 * Ends the current operation bundle and adds it to the history.
		 * Does nothing if no Bundle is started.
		 **************************************************************/
		public void operationBundleEnd() {
			
			if(currentOperationBundle != null) {
				
				//-----------------------------------------
				// If pointer is not at the end of the history
				// stack, remove all entries after the current position.
				
				int bundleLastIndex = operationBundleStack.size() - 1;
				int nextIndex = historyPointer + 1;
				
				while(nextIndex < bundleLastIndex ) {
					operationBundleStack.remove(nextIndex);
				}
				
				//-----------------------------------------
				// Add Bundle to Stack and set pointer
				operationBundleStack.add(currentOperationBundle);
				historyPointer = operationBundleStack.size() - 1;
				currentOperationBundle = null;
			}
		}
		
		/**************************************************************
		 * Adds an operation to the currently active bundle.
		 * If there is an active bundle, adds it to the bundle.
		 * If there is no active bundle, adds the operation to its 
		 * own bundle as a single operation.
		 * This method does not intend to throw anything, not even 
		 * butter to make it a butter-fly.
		 * 
		 * @throws no it doesn't
		 * 
		 **************************************************************/
		public void addOperation(UndoRedoOperation<Y> operation) {
			
			if(currentOperationBundle != null) {
				currentOperationBundle.add(operation);
			}else {
				operationBundleStart();
					currentOperationBundle.add(operation);
				operationBundleEnd();
			}
		}
		
		/**************************************************************
		 * Adds an operation to the currently active bundle.
		 * Throws an exception if there is no active bundle. 
		 * Might also throw a snowball at the nearest available snowman to start
		 * the 76th battle of the 3rd ultimate snowball war.
		 * 
		 * @throws IllegalStateException if no bundle is started.
		 **************************************************************/
		public void addOperationBundled(UndoRedoOperation<Y> operation) throws IllegalStateException {
			
			if(currentOperationBundle != null) {
				currentOperationBundle.add(operation);
			}else {
				throw new IllegalStateException("Operation Bundle has to be started before operation can be added.");
			}
		}
		
		/**************************************************************
		 * Executes an undo operation.
		 * Does nothing if there are no more undo states.
		 * This method does not want to throw anything but we make it 
		 * throw things anyway.
		 * 
		 * @throws InvocationTargetException 
		 * @throws IllegalArgumentException 
		 * @throws IllegalAccessException 
		 * 
		 **************************************************************/
		public void executeUndo() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException  {
			
			if(historyPointer > 0) {
				UndoRedoOperation<Y> operation = currentOperationBundle.get(historyPointer);
				operation.executeUndo();
				historyPointer--;
			}
			
		}
		
		/**************************************************************
		 * Executes a redo operation.
		 * Does nothing if there are no more undo states.
		 * This method prefers to throw exceptions rather then throwing
		 * itself out of the window.
		 * 
		 * @throws InvocationTargetException 
		 * @throws IllegalArgumentException 
		 * @throws IllegalAccessException 
		 * 
		 **************************************************************/
		public void executeRedo() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException  {
			
			if( historyPointer < (operationBundleStack.size() - 1) ) {
				UndoRedoOperation<Y> operation = currentOperationBundle.get(historyPointer);
				operation.executeRedo();
				historyPointer++;
			}
			
		}
			
	}
	
	
	//##################################################################################################
	// CLASS: UndoRedoOperation
	//##################################################################################################
	
	/*********************************************************************************
	 * 
	 *********************************************************************************/
	public class UndoRedoOperation<Z>{
		
		private Object classInstance;
		private Method undoMethod;
		private Method redoMethod;
		private Z undoData;
		private  Z redoData;
		
		/**************************************************************
		 * Creates a new undo/redo operation.
		 * 
		 * @param classInstance the class instance for regular methods.
		 *        Provide null if methods are static.
		 *        
		 * @param undoMethod the method that executes the undo operation.
		 *        Has to have exactly a single parameter that takes a
		 *        value of the same type as undoData.
		 *        
		 * @param redoMethod the method that executes the redo operation.
		 *        Has to have exactly a single parameter that takes a
		 *        value of the same type as redoData.
		 *        
		 * @param undoData The data needed for executing the undo operation.
		 *        (State before undoed action was executed)
		 *        
		 * @param redoData The data needed for executing the redo operation.
		 *        (State after the redoed action was executed)
		 *        
		 **************************************************************/
		public UndoRedoOperation( Object classInstance
								, Method undoMethod
								,Method redoMethod
								, Z undoData
								, Z redoData) {
		
			this.undoMethod = undoMethod;
			this.redoMethod = redoMethod;
			this.undoData = undoData;
			this.redoData = redoData;
			
		}
		
		/**************************************************************
		 * Executes the undoMethod passing the undoData to the method.
		 * Can throw stuff like exceptions and teddybaers!
		 * 
		 * @throws IllegalAccessException
		 * @throws IllegalArgumentException
		 * @throws InvocationTargetException
		 * @throws Teddybears
		 * 
		 **************************************************************/
		public void executeUndo() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			this.undoMethod.invoke(classInstance, undoData);
		}
		
		/**************************************************************
		 * Executes the redoMethod passing the redoData to the method.
		 * Can throw stuff like exceptions and spiked wrecking balls!
		 * 
		 * @throws IllegalAccessException
		 * @throws IllegalArgumentException
		 * @throws InvocationTargetException
		 * @throws Spiked Wrecking Balls
		 * 
		 **************************************************************/
		public void executeRedo() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			this.redoMethod.invoke(classInstance, redoData);
		}
		
	}
	
		
}
