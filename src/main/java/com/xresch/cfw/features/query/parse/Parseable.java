package com.xresch.cfw.features.query.parse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public interface Parseable {
	
	public void parse(CFWQueryParser context) throws ParseException;

}
