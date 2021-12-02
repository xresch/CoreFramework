package com.xresch.cfw.features.query;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public interface Parseable {
	
	public void parse(CFWQueryParserContext context) throws ParseException;

}