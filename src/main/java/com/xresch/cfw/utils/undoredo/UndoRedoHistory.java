package com.xresch.cfw.utils.undoredo;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import com.xresch.cfw.utils.LimitedSizeStack;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class UndoRedoHistory<T>{
	
	private int historyPointer = 0;
	
	private LimitedSizeStack< ArrayList<UndoRedoOperation<T>> > operationBundleStack;
	
	ArrayList<UndoRedoOperation<T>> currentOperationBundle = null;
	
	/**************************************************************
	 * 
	 **************************************************************/
	public UndoRedoHistory(int maxhistoryStates) {
		 operationBundleStack = new LimitedSizeStack< ArrayList<UndoRedoOperation<T>> >(maxhistoryStates);
	}
	
	/**************************************************************
	 * 
	 **************************************************************/
	public void clear() {
		 operationBundleStack.clear();
		 currentOperationBundle = null;
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
			currentOperationBundle = new ArrayList<UndoRedoOperation<T>>();
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
		
		//-----------------------------------------
		// Check Bundle State
		if(currentOperationBundle == null) {
			return;
		}
		
		if(currentOperationBundle.size() == 0) {
			currentOperationBundle = null;
			return;
		}
		
		//-----------------------------------------
		// If pointer is not at the end of the history
		// stack, remove all entries after the current position.
		int nextIndex = historyPointer + 1;
		
		while(nextIndex < (operationBundleStack.size()-1) ) {
			operationBundleStack.remove(nextIndex);
		}
		
		//-----------------------------------------
		// Add Bundle to Stack and set pointer
		operationBundleStack.add(currentOperationBundle);
		historyPointer = operationBundleStack.size() - 1;
		currentOperationBundle = null;
	
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
	public void addOperation(UndoRedoOperation<T> operation) {
		
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
	public void addOperationBundled(UndoRedoOperation<T> operation) throws IllegalStateException {
		
		if(currentOperationBundle != null) {
			currentOperationBundle.add(operation);
		}else {
			throw new IllegalStateException("Operation Bundle has to be started before operation can be added.");
		}
	}
	
	/**************************************************************
	 * Executes an undo operation. The method will not execute
	 * every operation included in a bundle, but will directly execute
	 * the first undo operation in a bundle. This is useful when 
	 * the data contains a full state of some data and not a partial 
	 * edit.
	 * Does nothing if there are no more undo states.
	 * This method does not want to throw anything but we make it 
	 * throw things anyway.
	 * 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * 
	 **************************************************************/
	public void executeUndoDirect() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException  {
		
		
		if(historyPointer >= 0 && operationBundleStack.size() > 0) {
			
			ArrayList<UndoRedoOperation<T>> operationBundle = operationBundleStack.get(historyPointer);
			
			if(operationBundle.size() > 0) {
				UndoRedoOperation<T> operation = operationBundle.get(0);
				operation.executeUndo();
			}
			
			historyPointer--;
		}
		
	}
	
	/**************************************************************
	 * Executes a redo operation. The method will not execute
	 * every operation included in a bundle, but will directly execute
	 * the last redo operation in a bundle. This is useful when 
	 * the data contains a full state of some data and not a partial 
	 * edit.
	 * Does nothing if there are no more undo states.
	 * This method prefers to throw exceptions rather then throwing
	 * itself out of the window.
	 * 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * 
	 **************************************************************/
	public void executeRedoDirect() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException  {
		
		if( historyPointer < (operationBundleStack.size() - 1) && operationBundleStack.size() > 0 ) {
			
			historyPointer++;
			
			ArrayList<UndoRedoOperation<T>> operationBundle = operationBundleStack.get(historyPointer);
			
			if(operationBundle.size() > 0) {
				UndoRedoOperation<T> operation = operationBundle.get(operationBundle.size()-1);
				operation.executeRedo();
			}
			
			
		}
		
	}
		
}
