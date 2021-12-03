package com.xresch.cfw.features.query;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.xresch.cfw.features.query.parse.QueryPartValue;

public interface Evaluatable {
	
	public QueryPartValue evaluate(ArrayList< LinkedHashMap<String, Object> > objectlist, Object... params);

}
