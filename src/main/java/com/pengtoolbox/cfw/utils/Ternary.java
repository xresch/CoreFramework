package com.pengtoolbox.cfw.utils;

/***********************************************************
 * Enum used to simulate Ternary Logic where Binary logic
 * is not enough to handle the implementation.
 * 
 * @author Reto Scheiwiller, 2019
 *
 ***********************************************************/
public enum Ternary{
	TRUE(true), FALSE(false), DONTCARE(null);
	
	private Object o = null;
	private Ternary(Object o) {
		this.o = o;
	}
	
	/*******************************************
	 * Return boolean or false if DONTCARE.
	 * @return true or false
	 *******************************************/
	public boolean toBoolean() {
		if(o instanceof Boolean) {
			return ((Boolean)o);
		}
		return false;
	}
	
	/*******************************************
	 * Return boolean or false if NEITHER.
	 * @return "true", "false" or "dontcare"
	 *******************************************/
	public String toString() {
		if(o instanceof Boolean) {
			return ((Boolean)o).toString();
		}
		return "dontcare";
	}
	
}