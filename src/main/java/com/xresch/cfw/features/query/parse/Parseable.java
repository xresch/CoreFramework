package com.xresch.cfw.features.query.parse;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public interface Parseable {
	
	public void parse(CFWQueryParser context) throws ParseException;

}
