package com.xresch.cfw.features.query;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public interface Evaluatable {
	
	public QueryPartValue evaluate(ArrayList< LinkedHashMap<String, Object> > objectlist, Object... params);

}
