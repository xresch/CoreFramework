package com.xresch.cfw.utils.undoredo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;



/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class UndoRedoOperation<T>{
	
	private Object classInstance;
	private Method undoMethod;
	private Method redoMethod;
	private T undoData;
	private  T redoData;
	
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
							, T undoData
							, T redoData) {
	
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