package com.xresch.cfw.utils;

import java.util.Stack;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class LimitedSizeStack<T> extends Stack<T> {

	private static final long serialVersionUID = 1L;
	
	private int maxSize;

	/*******************************************************************************
	 * 
	 * @param maxSize that this stack should have.
	 *******************************************************************************/
    public LimitedSizeStack(int maxSize) {
        super();
        this.maxSize = maxSize;
    }

	/*******************************************************************************
	 * Add an object to the stack.
	 * If maxSize is reached, removes the oldest object in the stack.
	 *******************************************************************************/
    @Override
    public T push(T object) {
        while (this.size() >= maxSize) {
            this.remove(0);
        }
        return super.push(object);
    }
}
