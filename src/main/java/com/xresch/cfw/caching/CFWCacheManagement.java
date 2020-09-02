package com.xresch.cfw.caching;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.checkerframework.checker.units.qual.K;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@SuppressWarnings("rawtypes")
public class CFWCacheManagement {
	
	private static SortedMap<String, Cache> cacheMap = Collections.synchronizedSortedMap(new TreeMap<>());
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static <K1 extends Object, V1 extends Object> Cache<K1, V1> addCache(String cacheName, CacheBuilder cacheBuilder) {
		
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
			object.addProperty("hitCount", stats.hitCount());
			object.addProperty("hitRate", stats.hitRate());
			object.addProperty("missCount", stats.missCount());
			object.addProperty("missRate", stats.missRate());
			object.addProperty("missRate", stats.evictionCount());
			
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
