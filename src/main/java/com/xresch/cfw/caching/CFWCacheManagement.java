package com.xresch.cfw.caching;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw.logging.CFWLog;

@SuppressWarnings("rawtypes")
public class CFWCacheManagement {
	
	private static final Logger logger = CFWLog.getLogger(CFWCacheManagement.class.getName());
	
	private static SortedMap<String, Cache> cacheMap = Collections.synchronizedSortedMap(new TreeMap<>());
	
	/************************************************************************
	 * 
	 ************************************************************************/
	@SuppressWarnings("unchecked")
	public static <K1 extends Object, V1 extends Object> Cache<K1, V1> addCache(
			String cacheName,  CacheBuilder cacheBuilder) {
		
		if(cacheMap.containsKey(cacheName)) {
			new CFWLog(logger)
				.severe("Failed to add cache. The cache name '"+cacheName+"' is already used.", new IllegalArgumentException());
			return null;
		}
		
		Cache<K1, V1> cache = cacheBuilder
				.recordStats()
				.build();
		
		cacheMap.put(cacheName, cache);
		
		return cache;
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static JsonArray getCacheStatisticsAsJSON() {
		
		JsonArray array = new JsonArray();
		
		for(Entry<String, Cache> entry : cacheMap.entrySet()) {
			
			String name = entry.getKey();
			Cache cache = entry.getValue();
			CacheStats stats = cache.stats();
			JsonObject object = new JsonObject();

			object.addProperty("name", name);
			object.addProperty("size", cache.size());
			object.addProperty("hit_count", stats.hitCount());
			object.addProperty("hit_rate", stats.hitRate());
			object.addProperty("miss_count", stats.missCount());
			object.addProperty("miss_rate", stats.missRate());
			object.addProperty("eviction_count", stats.evictionCount());
			object.addProperty("load_penalty_avg", stats.averageLoadPenalty());
			object.addProperty("request_count", stats.requestCount());
			array.add(object);
		}
		
		return array;
		
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static JsonArray clearAllCaches() {
		
		JsonArray array = new JsonArray();
		
		for(Entry<String, Cache> entry : cacheMap.entrySet()) {
			Cache cache = entry.getValue();
			cache.invalidateAll();
			cache.cleanUp();
		}
		
		return array;
		
	}
	

}
