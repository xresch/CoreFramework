package com.xresch.cfw.features.query;

public class CFWQueryValue {
	
	private CFWQueryValueType type;
	
	public enum CFWQueryValueType{
		NUMBER,
		STRING,
		BOOLEAN,
		JSON,
	}
		
	public CFWQueryValue(CFWQueryValueType type, Object value){
		
	}

}
