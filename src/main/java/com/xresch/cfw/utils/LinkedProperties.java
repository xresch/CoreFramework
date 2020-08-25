package com.xresch.cfw.utils;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;

public class LinkedProperties extends Properties {
	
	private static final long serialVersionUID = 1L;
	
	private final HashSet<Object> keys = new LinkedHashSet<Object>();


    public Iterable<Object> orderedKeys() {
        return Collections.list(keys());
    }

    public Enumeration<Object> keys() {
        return Collections.<Object>enumeration(keys);
    }

    public synchronized Object put(Object key, Object value) {
        keys.add(key);
        return super.put(key, value);
    }
    
    public synchronized void putAll(Map<? extends Object, ? extends Object> map) {
        keys.addAll(map.keySet());
        super.putAll(map);
    }
}

